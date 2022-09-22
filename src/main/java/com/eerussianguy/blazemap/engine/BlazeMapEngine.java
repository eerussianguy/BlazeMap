package com.eerussianguy.blazemap.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.event.BlazeRegistryEvent;
import com.eerussianguy.blazemap.api.event.DimensionChangedEvent;
import com.eerussianguy.blazemap.api.event.ServerJoinedEvent;
import com.eerussianguy.blazemap.api.markers.IMarkerStorage;
import com.eerussianguy.blazemap.api.markers.IStorageFactory;
import com.eerussianguy.blazemap.api.markers.MapLabel;
import com.eerussianguy.blazemap.api.markers.Waypoint;
import com.eerussianguy.blazemap.api.util.LayerRegion;
import com.eerussianguy.blazemap.api.util.MinecraftStreams;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.AsyncDataCruncher;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;
import com.eerussianguy.blazemap.util.Helpers;

public class BlazeMapEngine {
    private static final Set<Consumer<LayerRegion>> TILE_CHANGE_LISTENERS = new HashSet<>();
    private static final Map<ResourceKey<Level>, CartographyPipeline> PIPELINES = new HashMap<>();
    private static final Map<ResourceKey<Level>, IMarkerStorage.Layered<MapLabel>> LABELS = new HashMap<>();
    private static final Map<ResourceKey<Level>, IMarkerStorage<Waypoint>> WAYPOINTS = new HashMap<>();

    private static DebouncingThread debouncer;
    private static AsyncDataCruncher dataCruncher;
    private static AsyncChain.Root async;
    private static CartographyPipeline activePipeline;
    private static IMarkerStorage.Layered<MapLabel> activeLabels;
    private static IMarkerStorage<Waypoint> activeWaypoints;
    private static IStorageFactory<IMarkerStorage<Waypoint>> waypointStorageFactory;
    private static String serverID;
    private static File serverDir;
    private static boolean frozenRegistries = false;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(BlazeMapEngine.class);
        dataCruncher = new AsyncDataCruncher("Blaze Map");
        async = new AsyncChain.Root(dataCruncher, Helpers::runOnMainThread);
        debouncer = new DebouncingThread("Blaze Map Engine");
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
        serverDir = Helpers.getClientSideStorageDir();
        serverDir.mkdirs();
        ServerJoinedEvent serverJoined = new ServerJoinedEvent(serverID, serverDir);
        MinecraftForge.EVENT_BUS.post(serverJoined);
        waypointStorageFactory = serverJoined.getWaypointStorageFactory();
        switchToPipeline(player.level.dimension());
    }

    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        PIPELINES.clear();
        LABELS.clear();
        WAYPOINTS.clear();
        if(activePipeline != null) {
            activePipeline.shutdown();
            activePipeline = null;
        }
        activeLabels = null;
        activeWaypoints = null;
        serverID = null;
        serverDir = null;
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
        activePipeline = PIPELINES.computeIfAbsent(dimension, d -> new CartographyPipeline(serverDir, d)).activate();
        activeLabels = LABELS.computeIfAbsent(dimension, LabelStorage::new);
        activeWaypoints = WAYPOINTS.computeIfAbsent(dimension, d -> {
            File waypoints = new File(activePipeline.dimensionDir, "waypoints.bin");
            return waypointStorageFactory.create(
                () -> new MinecraftStreams.Input(new FileInputStream(waypoints)),
                () -> new MinecraftStreams.Output(new FileOutputStream(waypoints))
            );
        });

        TILE_CHANGE_LISTENERS.clear();
        DimensionChangedEvent event = new DimensionChangedEvent(
            dimension,
            activePipeline.availableMapTypes,
            activePipeline.availableLayers,
            TILE_CHANGE_LISTENERS::add,
            activePipeline::consumeTile,
            activeLabels,
            activeWaypoints,
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
