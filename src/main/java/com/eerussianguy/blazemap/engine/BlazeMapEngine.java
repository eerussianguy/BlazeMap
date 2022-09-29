package com.eerussianguy.blazemap.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.event.*;
import com.eerussianguy.blazemap.api.mapping.*;
import com.eerussianguy.blazemap.api.markers.*;
import com.eerussianguy.blazemap.api.util.*;
import com.eerussianguy.blazemap.engine.async.*;
import com.eerussianguy.blazemap.util.Helpers;

public class BlazeMapEngine {
    private static final Set<Consumer<LayerRegion>> TILE_CHANGE_LISTENERS = new HashSet<>();
    private static final Map<ResourceKey<Level>, CartographyPipeline> PIPELINES = new HashMap<>();
    private static final Map<ResourceKey<Level>, IMarkerStorage<Waypoint>> WAYPOINTS = new HashMap<>();
    private static final ResourceLocation WAYPOINT_STORAGE = Helpers.identifier("waypoints.bin");

    private static DebouncingThread debouncer;
    private static AsyncDataCruncher dataCruncher;
    private static AsyncChain.Root async;
    private static CartographyPipeline activePipeline;
    private static IMarkerStorage.Layered<MapLabel> activeLabels;
    private static IMarkerStorage<Waypoint> activeWaypoints;
    private static IStorageFactory<IMarkerStorage<Waypoint>> waypointStorageFactory;
    private static String serverID;
    private static StorageAccess.Internal storage;
    private static boolean frozenRegistries;
    private static boolean clientSource;
    private static String mdSource;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(BlazeMapEngine.class);
        dataCruncher = new AsyncDataCruncher("Blaze Map");
        async = new AsyncChain.Root(dataCruncher, Helpers::runOnMainThread);
        debouncer = new DebouncingThread("Blaze Map Engine");
        frozenRegistries = false;
    }

    public static AsyncChain.Root async() {
        return async;
    }

    public static AsyncDataCruncher cruncher() {
        return dataCruncher;
    }

    public static DebouncingThread debouncer() {
        return debouncer;
    }

    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event) {
        if(!frozenRegistries) {
            IEventBus bus = MinecraftForge.EVENT_BUS;
            bus.post(new BlazeRegistryEvent.CollectorRegistryEvent());
            bus.post(new BlazeRegistryEvent.LayerRegistryEvent());
            bus.post(new BlazeRegistryEvent.MapTypeRegistryEvent());
            bus.post(new BlazeRegistryEvent.ProcessorRegistryEvent());
            BlazeMapAPI.MAPTYPES.freeze();
            BlazeMapAPI.LAYERS.freeze();
            BlazeMapAPI.COLLECTORS.freeze();
            BlazeMapAPI.PROCESSORS.freeze();
            frozenRegistries = true;
        }

        LocalPlayer player = event.getPlayer();
        if(player == null) return;
        serverID = Helpers.getServerID();
        storage = new StorageAccess.Internal(Helpers.getClientSideStorageDir());
        ServerJoinedEvent serverJoined = new ServerJoinedEvent(serverID, storage.addon());
        MinecraftForge.EVENT_BUS.post(serverJoined);
        waypointStorageFactory = serverJoined.getWaypointStorageFactory();
        switchToPipeline(player.level.dimension());
        clientSource = true;
        mdSource = "unknown";
    }

    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        PIPELINES.clear();
        WAYPOINTS.clear();
        if(activePipeline != null) {
            activePipeline.shutdown();
            activePipeline = null;
        }
        activeLabels = null;
        activeWaypoints = null;
        serverID = null;
        storage = null;
        waypointStorageFactory = null;
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
        activePipeline = PIPELINES.computeIfAbsent(dimension, d -> new CartographyPipeline(storage.internal(d.location()), d)).activate();
        activeLabels = new LabelStorage(dimension);

        IStorageAccess fileStorage = activePipeline.addonStorage;
        activeWaypoints = WAYPOINTS.computeIfAbsent(dimension, d -> waypointStorageFactory.create(
            () -> fileStorage.read(WAYPOINT_STORAGE),
            () -> fileStorage.write(WAYPOINT_STORAGE),
            () -> fileStorage.exists(WAYPOINT_STORAGE)
        ));

        TILE_CHANGE_LISTENERS.clear();
        DimensionChangedEvent event = new DimensionChangedEvent(
            dimension,
            activePipeline.availableMapTypes,
            activePipeline.availableLayers,
            TILE_CHANGE_LISTENERS::add,
            activePipeline::consumeTile,
            activeLabels,
            activeWaypoints,
            fileStorage
        );
        MinecraftForge.EVENT_BUS.post(event);
    }

    public static void onChunkChanged(ChunkPos pos, String source) {
        if(activePipeline == null || !clientSource) {
            BlazeMap.LOGGER.warn("Ignoring chunk update for {}, pipeline: {}, clientSource: {}:", pos, activePipeline, clientSource);
            return;
        }
        mdSource = source;
        activePipeline.markChunkDirty(pos);
    }

    public static void submitChanges(ResourceKey<Level> dimension, Map<ChunkPos, Map<BlazeRegistry.Key<Collector<?>>, MasterDatum>> data) {

    }

    public static String getMDSource() {
        return mdSource;
    }

    public static boolean isClientSource() {
        return clientSource;
    }

    public static int numCollectors() {
        return activePipeline.numCollectors;
    }

    public static int numProcessors() {
        return activePipeline.numProcessors;
    }

    public static int numTransformers() {
        return activePipeline.numTransformers;
    }

    public static int numLayers() {
        return activePipeline.numLayers;
    }

    static void notifyLayerRegionChange(LayerRegion layerRegion) {
        for(Consumer<LayerRegion> listener : TILE_CHANGE_LISTENERS) {
            listener.accept(layerRegion);
        }
    }
}
