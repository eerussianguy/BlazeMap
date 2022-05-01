package com.eerussianguy.blazemap.engine;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.mapping.Collector;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.mapping.MasterData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.io.File;
import java.util.*;

public class CartographyPipeline {
    private final MasterDataCache mdCache = new MasterDataCache();
    private final File levelDir;

    private final Set<MapType> maptypes = new HashSet<>();
    private final Set<Layer<?>> layers = new HashSet<>();
    private final Set<Collector<?>> collectors = new HashSet<>();
    private final Map<Layer<?>, List<MapType>> mapTriggers = new HashMap<>();
    private final Map<Collector<?>, List<Layer<?>>> layerTriggers = new HashMap<>();

    public CartographyPipeline(File serverDir, ResourceKey<Level> world) {
        this.levelDir = new File(serverDir, world.getRegistryName().toString().replace(':', '+'));
        this.levelDir.mkdirs();

        for (ResourceLocation key : BlazeMapAPI.MAPTYPES.keys()) {
            MapType maptype = BlazeMapAPI.MAPTYPES.get(key);
            if(!maptype.shouldRenderForWorld(world)) continue;
            maptypes.add(maptype);
            for(Layer<?> layer : maptype.getLayers()){
                if(!layer.shouldRenderForWorld(world)) continue;
                mapTriggers.computeIfAbsent(layer, $ -> new ArrayList<>(8)).add(maptype);
                if(layers.contains(layer)) continue;
                layers.add(layer);
                for(Collector<?> collector : layer.getCollectors()){
                    layerTriggers.computeIfAbsent(collector, $ -> new ArrayList<>(8)).add(layer);
                    if(collectors.contains(collector)) continue;
                    collectors.add(collector);
                }
            }
        }
    }

    // TODO: make chunk update orders wait for a few ms in a queue and start processing when timed out.
    public void markChunkDirty(ChunkPos pos) {
        BlazeMapEngine.threading()
            .startOnGameThread($ -> this.collectFromChunk(pos))
            .thenOnDataThread(this::processMasterData)
            .thenOnGameThread(this::sendMapUpdates)
            .start();
    }

    private Map<ResourceLocation, MasterData> collectFromChunk(ChunkPos pos) {
        Map<ResourceLocation, MasterData> data = new HashMap<>();
        int x0 = pos.getMinBlockX();
        int x1 = pos.getMaxBlockX();
        int z0 = pos.getMinBlockZ();
        int z1 = pos.getMaxBlockZ();
        for(Collector<?> collector : collectors){
            data.put(collector.getID(), collector.collect(null, null, x0, z0, x1, z1));
        }
        return data;
    }

    private List<Event> processMasterData(Map<ResourceLocation, MasterData> data){
        List<Event> updateEvents = new LinkedList<>();
        Set<Layer<?>> dirtyLayers = new HashSet<>();
        Set<MapType> dirtyMaps = new HashSet<>();

        return updateEvents;
    }

    // TODO: figure out why void gives generic errors but null Void is OK. Does it have to be an Object?
    private Void sendMapUpdates(List<Event> events){
        for(Event event : events){
            MinecraftForge.EVENT_BUS.post(event);
        }
        return null;
    }

    public void shutdown() {

    }
}
