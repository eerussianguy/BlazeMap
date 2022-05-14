package com.eerussianguy.blazemap.engine;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.Helpers;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.AsyncDataCruncher;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;
import com.mojang.blaze3d.platform.NativeImage;

public class BlazeMapEngine
{
    private static final Map<ResourceKey<Level>, CartographyPipeline> PIPELINES = new HashMap<>();
    private static DebouncingThread debouncer;
    private static AsyncChain.Root async;
    private static AsyncDataCruncher dataCruncher;
    private static CartographyPipeline activePipeline;
    private static String serverID;
    private static File serverDir;

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(BlazeMapEngine.class);
        dataCruncher = new AsyncDataCruncher("Blaze Map");
        async = new AsyncChain.Root(dataCruncher, Helpers::runOnMainThread);
        debouncer = new DebouncingThread("Blaze Map Engine");
    }

    public static AsyncChain.Root async()
    {
        return async;
    }

    public static DebouncingThread debouncer()
    {
        return debouncer;
    }

    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        LocalPlayer player = event.getPlayer();
        if (player == null) return;
        serverID = Helpers.getServerID();
        serverDir = new File(Helpers.getBaseDir(), serverID);
        switchToPipeline(player.level.dimension());
    }

    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        PIPELINES.clear();
        if (activePipeline != null)
        {
            activePipeline.shutdown();
            activePipeline = null;
        }
        serverID = null;
        serverDir = null;
    }

    @SubscribeEvent
    public static void onChangeWorld(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        switchToPipeline(event.getTo());
    }

    private static void switchToPipeline(ResourceKey<Level> dimension)
    {
        if (activePipeline != null)
        {
            if (activePipeline.world.equals(dimension)) return;
            activePipeline.shutdown();
        }
        activePipeline = PIPELINES.computeIfAbsent(dimension, d -> new CartographyPipeline(serverDir, d)).activate();
        Hooks.notifyDimensionChange(dimension);
    }

    public static void onChunkChanged(ChunkPos pos)
    {
        if (activePipeline == null)
        {
            BlazeMap.LOGGER.warn("Ignoring chunk update for {}", pos);
            return;
        }
        activePipeline.markChunkDirty(pos);
    }

    public static class Hooks
    {
        private static final Set<Consumer<ResourceKey<Level>>> DIMENSION_CHANGE_LISTENERS = new HashSet<>();
        private static final Set<Consumer<LayerRegion>> LAYER_REGION_CHANGE_LISTENERS = new HashSet<>();

        public File getCurrentDimensionDir()
        {
            if (activePipeline == null) return null;
            return activePipeline.dimensionDir;
        }

        public File getCurrentServerDir()
        {
            return serverDir;
        }

        public static void addDimensionChangeListener(Consumer<ResourceKey<Level>> listener)
        {
            DIMENSION_CHANGE_LISTENERS.add(listener);
        }

        public static void removeDimensionChangeListener(Consumer<ResourceKey<Level>> listener)
        {
            DIMENSION_CHANGE_LISTENERS.remove(listener);
        }

        private static void notifyDimensionChange(ResourceKey<Level> dimension)
        {
            for (Consumer<ResourceKey<Level>> listener : DIMENSION_CHANGE_LISTENERS)
            {
                listener.accept(dimension);
            }
        }

        public static void addLayerRegionChangeListener(Consumer<LayerRegion> listener)
        {
            LAYER_REGION_CHANGE_LISTENERS.add(listener);
        }

        public static void removeLayerRegionChangeListener(Consumer<LayerRegion> listener)
        {
            LAYER_REGION_CHANGE_LISTENERS.remove(listener);
        }

        static void notifyLayerRegionChange(LayerRegion layerRegion)
        {
            for (Consumer<LayerRegion> listener : LAYER_REGION_CHANGE_LISTENERS)
            {
                listener.accept(layerRegion);
            }
        }

        public static void consumeTile(LayerRegion layerRegion, Consumer<NativeImage> consumer)
        {
            activePipeline.consumeTile(layerRegion.layer, layerRegion.region, consumer);
        }

        public static void consumeTile(ResourceLocation layer, RegionPos region, Consumer<NativeImage> consumer)
        {
            activePipeline.consumeTile(layer, region, consumer);
        }

        public static Set<ResourceLocation> getAvailableLayers()
        {
            if (activePipeline == null) return Collections.emptySet();
            else return activePipeline.availableLayers;
        }

        public static Set<ResourceLocation> getAvailableMapTypes()
        {
            if (activePipeline == null) return Collections.emptySet();
            else return activePipeline.availableMapTypes;
        }
    }
}
