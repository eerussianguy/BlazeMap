package com.eerussianguy.blazemap.engine;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.mapping.*;
import com.eerussianguy.blazemap.api.util.LayerRegion;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.DebouncingDomain;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;
import com.eerussianguy.blazemap.engine.async.PriorityLock;
import com.eerussianguy.blazemap.util.Helpers;
import com.mojang.blaze3d.platform.NativeImage;

import static com.eerussianguy.blazemap.util.Profilers.Engine.*;

public class CartographyPipeline {
    public final File dimensionDir;
    public final ResourceKey<Level> dimension;
    public final Set<Key<MapType>> availableMapTypes;
    public final Set<Key<Layer>> availableLayers;
    private final Collector<MasterDatum>[] collectors;
    private final Map<Key<Layer>, List<MapType>> mapTriggers = new HashMap<>();
    private final Map<Key<Collector<MasterDatum>>, List<Layer>> layerTriggers = new HashMap<>();
    private final Map<Key<Collector<MasterDatum>>, List<Processor>> processorTriggers = new HashMap<>();
    private final Map<Key<Layer>, Map<RegionPos, LayerRegionTile>> regions = new HashMap<>();
    private final DebouncingDomain<LayerRegionTile> dirtyRegions;
    private final DebouncingDomain<ChunkPos> dirtyChunks;
    private final PriorityLock lock = new PriorityLock();
    private boolean active;


    public CartographyPipeline(File serverDir, ResourceKey<Level> dimension) {
        this.dimensionDir = new File(serverDir, dimension.location().toString().replace(':', '+'));
        this.dimensionDir.mkdirs();
        this.dimension = dimension;

        // Trim dependencies:
        // - Check what map types render on this dimension
        // - Check what layers those map types use
        // - Discard layers that don't render for this dimension
        // - List all needed MD collectors for those layers.
        // Build dependents tree:
        // - What layers depend on each MD collector?
        // - What maps depend on each layer?
        final Set<Key<MapType>> maps = new HashSet<>();
        final Set<Key<Layer>> layers = new HashSet<>();
        final Map<Key<Collector<MasterDatum>>, Collector<MasterDatum>> collectors = new HashMap<>();
        for(Key<MapType> mapID : BlazeMapAPI.MAPTYPES.keys()) {
            MapType map = mapID.value();
            if(!map.shouldRenderInDimension(dimension)) continue;
            maps.add(mapID);
            for(Key<Layer> layerID : map.getLayers()) {
                Layer layer = layerID.value();
                if(layer == null)
                    throw new RuntimeException("Layer " + layerID + " was not registered.");
                if(!layer.shouldRenderInDimension(dimension)) continue;
                mapTriggers.computeIfAbsent(layerID, $ -> new ArrayList<>(8)).add(map);
                if(layers.contains(layerID)) continue;
                layers.add(layerID);
                for(Key<Collector<MasterDatum>> collectorID : layer.getCollectors()) {
                    Collector<MasterDatum> collector = collectorID.value();
                    if(collector == null)
                        throw new RuntimeException("Collector " + collectorID + " was not registered.");
                    layerTriggers.computeIfAbsent(collectorID, $ -> new ArrayList<>(8)).add(layer);
                    if(collectors.containsKey(collectorID)) continue;
                    collectors.put(collectorID, collector);
                }
            }
        }

        // Set up master data processors
        for(Key<Processor> processorID : BlazeMapAPI.PROCESSORS.keys()) {
            Processor processor = processorID.value();
            if(!processor.shouldExecuteInDimension(dimension)) continue;
            for(Key<Collector<MasterDatum>> collectorID : processor.getCollectors()) {
                Collector<MasterDatum> collector = collectorID.value();
                if(collector == null)
                    throw new RuntimeException("Collector " + collectorID + " was not registered.");
                processorTriggers.computeIfAbsent(collectorID, $ -> new ArrayList<>(8)).add(processor);
                if(collectors.containsKey(collectorID)) continue;
                collectors.put(collectorID, collector);
            }
        }

        // Set up views (immutable sets) for the available maps and layers, fast access collector array.
        this.availableMapTypes = Collections.unmodifiableSet(maps);
        this.availableLayers = Collections.unmodifiableSet(layers);
        // noinspection unchecked
        this.collectors = collectors.values().toArray(Collector[]::new);

        // Set up debouncing mechanisms
        AsyncChain.Root async = BlazeMapEngine.async();
        DebouncingThread thread = BlazeMapEngine.debouncer();
        this.dirtyRegions = new DebouncingDomain<>(region -> async.runOnDataThread(() -> {
            REGION_LOAD_PROFILER.hit();
            REGION_TIME_PROFILER.begin();
            region.save();
            REGION_TIME_PROFILER.end();
        }), 1000, 30000);
        this.dirtyChunks = new DebouncingDomain<>(this::processDirtyChunk, 500, 5000);
        thread.add(dirtyRegions);
        thread.add(dirtyChunks);
    }

    public void markChunkDirty(ChunkPos pos) {
        dirtyChunks.push(pos);
    }

    private void processDirtyChunk(ChunkPos pos) {
        BlazeMapEngine.async()
            .startOnGameThread($ -> this.collectFromChunk(pos))
            .thenOnDataThread(md -> this.processMasterData(md, pos))
            .thenOnGameThread(this::sendMapUpdates)
            .start();
    }

    private Map<Key<Collector<MasterDatum>>, MasterDatum> collectFromChunk(ChunkPos pos) {
        COLLECTOR_LOAD_PROFILER.hit();
        COLLECTOR_TIME_PROFILER.begin();
        Map<Key<Collector<MasterDatum>>, MasterDatum> data = new HashMap<>();
        Level level = Helpers.levelOrThrow();

        // Do not collect data (thus skipping through the rest of the pipeline)
        // if this chunk is not currently in client cache, as that will return an empty chunk
        // which causes the map tiles to render wrongly
        if(!level.getChunkSource().hasChunk(pos.x, pos.z)) {
            return data;
        }

        int x0 = pos.getMinBlockX();
        int x1 = pos.getMaxBlockX();
        int z0 = pos.getMinBlockZ();
        int z1 = pos.getMaxBlockZ();

        for(Collector<MasterDatum> collector : collectors) {
            data.put(collector.getID(), collector.collect(level, x0, z0, x1, z1));
        }

        COLLECTOR_TIME_PROFILER.end();
        return data;
    }

    // Redraw tiles and process data based on MD changes
    // Check what MDs changed, mark dependent layers and processors as dirty
    // Ask layers to redraw tiles, if applicable:
    // - if tile was redrawn:
    // -  - mark dependent map types as changed
    // -  - update map files with new tile
    // -  - add LayerRegion to the list of updated images to return
    // Before return asynchronously run dirty processors in the background
    private List<LayerRegion> processMasterData(Map<Key<Collector<MasterDatum>>, MasterDatum> data, ChunkPos chunkPos) {
        if(data.size() == 0) {
            // noinspection unchecked
            return Collections.EMPTY_LIST;
        }

        Set<Layer> dirtyLayers = new HashSet<>();
        Set<Processor> dirtyProcessors = new HashSet<>();
        for(Map.Entry<Key<Collector<MasterDatum>>, MasterDatum> entry : data.entrySet()) {
            if(entry.getValue() != null) {
                // TODO: more advanced diffing
                Key<Collector<MasterDatum>> collectorID = entry.getKey();
                dirtyLayers.addAll(layerTriggers.computeIfAbsent(collectorID, $ -> new ArrayList<>(8)));
                dirtyProcessors.addAll(processorTriggers.computeIfAbsent(collectorID, $ -> new ArrayList<>(8)));
            }
        }

        RegionPos regionPos = new RegionPos(chunkPos);
        MapView view = new MapView(data);
        List<LayerRegion> updates = new LinkedList<>();

        if(dirtyLayers.size() > 0) {
            LAYER_LOAD_PROFILER.hit();
            LAYER_TIME_PROFILER.begin();
            for(Layer layer : dirtyLayers) {
                NativeImage layerChunkTile = new NativeImage(NativeImage.Format.RGBA, 16, 16, true);
                view.setFilter(layer.getCollectors()); // the layer should only access declared collectors

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
            LAYER_TIME_PROFILER.end();
        }

        if(dirtyProcessors.size() > 0) {
            BlazeMapEngine.async().runOnDataThread(() -> {
                PROCESSOR_LOAD_PROFILER.hit();
                PROCESSOR_TIME_PROFILER.begin();
                for(Processor processor : dirtyProcessors) {
                    view.setFilter(processor.getCollectors());
                    processor.execute(dimension, regionPos, chunkPos, view);
                }
                PROCESSOR_TIME_PROFILER.end();
            });
        }

        return updates;
    }

    private LayerRegionTile getLayerRegionTile(Key<Layer> layer, RegionPos region, boolean priority) {
        try {
            if(priority) lock.lockPriority();
            else lock.lock();
            return regions
                .computeIfAbsent(layer, $ -> new HashMap<>())
                .computeIfAbsent(region, $ -> {
                    LayerRegionTile layerRegionTile = new LayerRegionTile(layer, region, dimensionDir);
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
                BlazeMapEngine.notifyLayerRegionChange(update);
            }
        }
        return null;
    }

    public void shutdown() {
        active = false;
        // TODO: Release all memory dedicated to caches and such. Close resources. Flush to disk.
        regions.clear();
    }

    public CartographyPipeline activate() {
        active = true;
        return this;
    }

    public void consumeTile(Key<Layer> layer, RegionPos region, Consumer<NativeImage> consumer) {
        if(!mapTriggers.containsKey(layer))
            throw new IllegalArgumentException("Layer " + layer + " not available for dimension " + dimension);
        getLayerRegionTile(layer, region, true).consume(consumer);
    }
}
