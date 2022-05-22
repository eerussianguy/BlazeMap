package com.eerussianguy.blazemap.engine;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.api.event.DimensionChangedEvent;
import com.eerussianguy.blazemap.api.event.ServerJoinedEvent;
import com.eerussianguy.blazemap.api.util.LayerRegion;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.AsyncDataCruncher;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;

public class BlazeMapEngine {
    private static final Set<Consumer<LayerRegion>> TILE_CHANGE_LISTENERS = new HashSet<>();
    private static final Map<ResourceKey<Level>, CartographyPipeline> PIPELINES = new HashMap<>();
    private static DebouncingThread debouncer;
    private static AsyncChain.Root async;
    private static AsyncDataCruncher dataCruncher;
    private static CartographyPipeline activePipeline;
    private static String serverID;
    private static File serverDir;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(BlazeMapEngine.class);
        dataCruncher = new AsyncDataCruncher("Blaze Map");
        async = new AsyncChain.Root(dataCruncher, Helpers::runOnMainThread);
        debouncer = new DebouncingThread("Blaze Map Engine");
    }

    public static AsyncChain.Root async() {
        return async;
    }

    public static DebouncingThread debouncer() {
        return debouncer;
    }

    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event) {
        LocalPlayer player = event.getPlayer();
        if(player == null) return;
        serverID = Helpers.getServerID();
        serverDir = new File(Helpers.getBaseDir(), serverID);
        serverDir.mkdirs();
        MinecraftForge.EVENT_BUS.post(new ServerJoinedEvent(serverID, serverDir));
        switchToPipeline(player.level.dimension());
    }

    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        PIPELINES.clear();
        if(activePipeline != null) {
            activePipeline.shutdown();
            activePipeline = null;
        }
        serverID = null;
        serverDir = null;
    }

    @SubscribeEvent
    public static void onChangeWorld(PlayerEvent.PlayerChangedDimensionEvent event) {
        switchToPipeline(event.getTo());
    }

    private static void switchToPipeline(ResourceKey<Level> dimension) {
        if(activePipeline != null) {
            if(activePipeline.dimension.equals(dimension)) return;
            activePipeline.shutdown();
        }
        activePipeline = PIPELINES.computeIfAbsent(dimension, d -> new CartographyPipeline(serverDir, d)).activate();

        TILE_CHANGE_LISTENERS.clear();
        DimensionChangedEvent event = new DimensionChangedEvent(
            dimension,
            activePipeline.availableMapTypes,
            activePipeline.availableLayers,
            TILE_CHANGE_LISTENERS::add,
            activePipeline::consumeTile,
            activePipeline.dimensionDir
        );
        MinecraftForge.EVENT_BUS.post(event);
    }

    public static void onChunkChanged(ChunkPos pos) {
        if(activePipeline == null) {
            BlazeMap.LOGGER.warn("Ignoring chunk update for {}", pos);
            return;
        }
        activePipeline.markChunkDirty(pos);
    }

    static void notifyLayerRegionChange(LayerRegion layerRegion) {
        for(Consumer<LayerRegion> listener : TILE_CHANGE_LISTENERS) {
            listener.accept(layerRegion);
        }
    }
}
