package com.eerussianguy.blazemap.engine;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.Helpers;
import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.mapping.Collector;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.mapping.MasterData;
import com.eerussianguy.blazemap.api.util.LayerRegion;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.DebouncingDomain;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;
import com.eerussianguy.blazemap.engine.async.PriorityLock;
import com.mojang.blaze3d.platform.NativeImage;

public class CartographyPipeline {
    public static final Profiler.TimeProfiler COLLECTOR_TIME_PROFILER = new Profiler.TimeProfiler(20);
    public static final Profiler.LoadProfiler COLLECTOR_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
    public static final Profiler.TimeProfiler LAYER_TIME_PROFILER = new Profiler.TimeProfiler(20);
    public static final Profiler.LoadProfiler LAYER_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);

    public final File dimensionDir;
    public final ResourceKey<Level> dimension;
    private boolean active;

    private final Map<ResourceLocation, Collector<?>> collectors = new HashMap<>();
    private final Map<ResourceLocation, List<MapType>> mapTriggers = new HashMap<>();
    private final Map<ResourceLocation, List<Layer>> layerTriggers = new HashMap<>();
    public final Set<ResourceLocation> availableMapTypes;
    public final Set<ResourceLocation> availableLayers;

    private final Map<ResourceLocation, Map<RegionPos, LayerRegionTile>> regions = new HashMap<>();
    private final DebouncingDomain<LayerRegionTile> dirtyRegions;
    private final DebouncingDomain<ChunkPos> dirtyChunks;
    private final PriorityLock lock = new PriorityLock();


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
        final Set<ResourceLocation> maps = new HashSet<>();
        final Set<ResourceLocation> layers = new HashSet<>();
        for(ResourceLocation key : BlazeMapAPI.MAPTYPES.keys()) {
            MapType maptype = BlazeMapAPI.MAPTYPES.get(key);
            if(!maptype.shouldRenderInDimension(dimension)) continue;
            maps.add(key);
            for(ResourceLocation layerID : maptype.getLayers()) {
                Layer layer = BlazeMapAPI.LAYERS.get(layerID);
                if(layer == null)
                    throw new IllegalArgumentException("Layer " + layerID + " was not registered.");
                if(!layer.shouldRenderInDimension(dimension)) continue;
                mapTriggers.computeIfAbsent(layerID, $ -> new ArrayList<>(8)).add(maptype);
                if(layers.contains(layerID)) continue;
                layers.add(layerID);
                for(ResourceLocation collectorID : layer.getCollectors()) {
                    Collector<?> collector = BlazeMapAPI.COLLECTORS.get(collectorID);
                    if(collector == null)
                        throw new IllegalArgumentException("Layer " + collectorID + " was not registered.");
                    layerTriggers.computeIfAbsent(collectorID, $ -> new ArrayList<>(8)).add(layer);
                    if(collectors.containsKey(collectorID)) continue;
                    collectors.put(collectorID, collector);
                }
            }
        }

        // Set up views (immutable sets) for the available maps and layers
        this.availableMapTypes = Collections.unmodifiableSet(maps);
        this.availableLayers = Collections.unmodifiableSet(layers);

        // Set up debouncing mechanisms
        AsyncChain.Root async = BlazeMapEngine.async();
        DebouncingThread thread = BlazeMapEngine.debouncer();
        this.dirtyRegions = new DebouncingDomain<>(region -> async.runOnDataThread(region::save), 1000, 30000);
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

    private Map<ResourceLocation, MasterData> collectFromChunk(ChunkPos pos) {
        COLLECTOR_LOAD_PROFILER.hit();
        COLLECTOR_TIME_PROFILER.begin();
        Map<ResourceLocation, MasterData> data = new HashMap<>();
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

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for(Collector<?> collector : collectors.values()) {
            data.put(collector.getID(), collector.collect(level, mutable, x0, z0, x1, z1));
        }

        COLLECTOR_TIME_PROFILER.end();
        return data;
    }

    // Redraw tiles based on MD changes
    // Check what MDs changed, mark dependent layers for redraw
    // Ask layers to redraw tiles, if applicable:
    // - if tile was redrawn:
    // -  - mark dependent map types as changed
    // -  - update map files with new tile
    // -  - add LayerRegion to the list of updated images to return
    private List<LayerRegion> processMasterData(Map<ResourceLocation, MasterData> data, ChunkPos chunkPos) {
        LAYER_LOAD_PROFILER.hit();
        LAYER_TIME_PROFILER.begin();
        Set<Layer> dirtyLayers = new HashSet<>();
        for(Map.Entry<ResourceLocation, MasterData> entry : data.entrySet()) {
            if(entry.getValue() != null) {
                // TODO: more advanced diffing
                dirtyLayers.addAll(layerTriggers.get(entry.getKey()));
            }
        }

        List<LayerRegion> updates = new LinkedList<>();
        RegionPos regionPos = new RegionPos(chunkPos);
        MapView<ResourceLocation, MasterData> view = new MapView<>(data);
        for(Layer layer : dirtyLayers) {
            NativeImage layerChunkTile = new NativeImage(NativeImage.Format.RGBA, 16, 16, false);
            view.setFilter(layer.getCollectors()); // the layer should only access declared collectors

            // only generate updates if the renderer populates the tile
            // this is determined by the return value of renderTile being true
            if(layer.renderTile(layerChunkTile, view)) {
                ResourceLocation layerID = layer.getID();

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
        return updates;
    }

    private LayerRegionTile getLayerRegionTile(ResourceLocation layer, RegionPos region, boolean priority) {
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
        // TODO: Drop all LayerRegionTiles from the map. Easier said than done.
    }

    public CartographyPipeline activate() {
        active = true;
        return this;
    }

    public void consumeTile(ResourceLocation layer, RegionPos region, Consumer<NativeImage> consumer) {
        if(!mapTriggers.containsKey(layer))
            throw new IllegalArgumentException("Layer " + layer + " not available for dimension " + dimension);
        getLayerRegionTile(layer, region, false).consume(consumer);
    }
}
