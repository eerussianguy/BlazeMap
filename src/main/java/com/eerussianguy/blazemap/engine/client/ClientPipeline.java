package com.eerussianguy.blazemap.engine.client;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.MapType;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.Layer;
import com.eerussianguy.blazemap.api.util.LayerRegion;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.engine.*;
import com.eerussianguy.blazemap.engine.async.*;
import com.eerussianguy.blazemap.util.Helpers;
import com.mojang.blaze3d.platform.NativeImage;

import static com.eerussianguy.blazemap.util.Profilers.Client.*;

class ClientPipeline extends Pipeline {
    private static final PipelineProfiler PIPELINE_PROFILER = new PipelineProfiler(
        COLLECTOR_TIME_PROFILER,
        COLLECTOR_LOAD_PROFILER,
        TRANSFORMER_TIME_PROFILER,
        TRANSFORMER_LOAD_PROFILER,
        PROCESSOR_TIME_PROFILER,
        PROCESSOR_LOAD_PROFILER
    );

    private final StorageAccess.Internal storage;
    public final StorageAccess addonStorage;
    public final Set<Key<MapType>> availableMapTypes;
    public final Set<Key<Layer>> availableLayers;
    private final Layer[] layers;
    public final int numLayers;
    private final Map<Key<Layer>, Map<RegionPos, LayerRegionTile>> regions = new HashMap<>();
    private final DebouncingDomain<LayerRegionTile> dirtyRegions;
    private final PriorityLock lock = new PriorityLock();
    private boolean active;

    protected ClientPipeline(
        AsyncChain.Root async, DebouncingThread debouncer, ResourceKey<Level> dimension, StorageAccess.Internal storage
    ) {
        super(
            async, debouncer, PIPELINE_PROFILER, dimension, Helpers::levelOrThrow,
            BlazeMapAPI.MAPTYPES.keys().stream().map(k -> k.value().getLayers()).flatMap(Set::stream)
                .map(k -> k.value().getInputIDs()).map(ids -> BlazeMapAPI.COLLECTORS.keys().stream().filter(k -> ids.contains(k.value().getOutputID()))
                    .collect(Collectors.toUnmodifiableSet())).flatMap(Set::stream).collect(Collectors.toUnmodifiableSet()),
            BlazeMapAPI.TRANSFORMERS.keys().stream().filter(k -> k.value().shouldExecuteInDimension(dimension)).collect(Collectors.toUnmodifiableSet()),
            BlazeMapAPI.PROCESSORS.keys().stream().filter(k -> k.value().shouldExecuteInDimension(dimension)).collect(Collectors.toUnmodifiableSet())
        );
        this.storage = storage;
        this.addonStorage = storage.addon();

        // Set up views (immutable sets) for the available maps and layers, fast access collector array.
        this.availableMapTypes = BlazeMapAPI.MAPTYPES.keys().stream().filter(m -> m.value().shouldRenderInDimension(dimension)).collect(Collectors.toUnmodifiableSet());
        this.availableLayers = availableMapTypes.stream().map(k -> k.value().getLayers()).flatMap(Set::stream).filter(l -> l.value().shouldRenderInDimension(dimension)).collect(Collectors.toUnmodifiableSet());
        this.layers = availableLayers.stream().map(Key::value).toArray(Layer[]::new);
        this.numLayers = layers.length;

        // Set up debouncing mechanisms
        this.dirtyRegions = new DebouncingDomain<>(region -> async.runOnDataThread(() -> {
            REGION_LOAD_PROFILER.hit();
            REGION_TIME_PROFILER.begin();
            region.save();
            REGION_TIME_PROFILER.end();
        }), 1000, 30000);
        debouncer.add(dirtyRegions);
    }

    // Redraw tiles based on MD changes
    // Check what MDs changed, mark dependent layers and processors as dirty
    // Ask layers to redraw tiles, if applicable:
    // - if tile was redrawn:
    // -  - mark dependent map types as changed
    // -  - update map files with new tile
    // -  - add LayerRegion to the list of updated images to send a notification for
    @Override
    @SuppressWarnings("rawtypes")
    protected void onPipelineOutput(ChunkPos chunkPos, Set<Key<DataType>> diff, MapView view) {
        try {
            RegionPos regionPos = new RegionPos(chunkPos);
            List<LayerRegion> updates = new LinkedList<>();
            LAYER_LOAD_PROFILER.hit();
            LAYER_TIME_PROFILER.begin();

            for(Layer layer : layers) {
                NativeImage layerChunkTile = new NativeImage(NativeImage.Format.RGBA, 16, 16, true);
                view.setFilter(UnsafeGenerics.stripKeys(layer.getInputIDs())); // the layer should only access declared collectors

                // only generate updates if the renderer populates the tile
                // this is determined by the return value of renderTile being true
                if(layer.renderTile(layerChunkTile, view)) {
                    Key<Layer> layerID = layer.getID();

                    // update this chunk of the region
                    LayerRegionTile layerRegionTile = getLayerRegionTile(layerID, regionPos, false);
                    layerRegionTile.updateTile(layerChunkTile, chunkPos);

                    // asynchronously save this region later
                    dirtyRegions.push(layerRegionTile);

                    // updates for the listeners
                    updates.add(new LayerRegion(layerID, regionPos));
                }
            }

            if(updates.size() > 0) {
                async.runOnGameThread(() -> sendMapUpdates(updates));
            }
        }
        finally {
            LAYER_TIME_PROFILER.end();
        }

    }

    private LayerRegionTile getLayerRegionTile(Key<Layer> layer, RegionPos region, boolean priority) {
        try {
            if(priority) lock.lockPriority();
            else lock.lock();
            return regions
                .computeIfAbsent(layer, $ -> new HashMap<>())
                .computeIfAbsent(region, $ -> {
                    LayerRegionTile layerRegionTile = new LayerRegionTile(storage, layer, region);
                    layerRegionTile.tryLoad();
                    return layerRegionTile;
                });
        }
        finally {
            lock.unlock();
        }
    }

    // TODO: figure out why void gives generic errors but null Void is OK. Does it have to be an Object?
    private Void sendMapUpdates(List<LayerRegion> updates) {
        if(active) {
            for(LayerRegion update : updates) {
                BlazeMapClientEngine.notifyLayerRegionChange(update);
            }
        }
        return null;
    }

    public void shutdown() {
        active = false;
        // TODO: Release all memory dedicated to caches and such. Close resources. Flush to disk.
        regions.clear();
    }

    public ClientPipeline activate() {
        active = true;
        return this;
    }

    public void consumeTile(Key<Layer> layer, RegionPos region, Consumer<NativeImage> consumer) {
        if(!availableLayers.contains(layer))
            throw new IllegalArgumentException("Layer " + layer + " not available for dimension " + dimension);
        getLayerRegionTile(layer, region, true).consume(consumer);
    }
}
