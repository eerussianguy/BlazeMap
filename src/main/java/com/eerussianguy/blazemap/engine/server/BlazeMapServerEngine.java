package com.eerussianguy.blazemap.engine.server;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.event.BlazeRegistryEvent;
import com.eerussianguy.blazemap.engine.PipelineProfiler;
import com.eerussianguy.blazemap.engine.StorageAccess;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.AsyncDataCruncher;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;
import com.eerussianguy.blazemap.engine.client.BlazeMapClientEngine;

import static com.eerussianguy.blazemap.util.Profilers.Server.*;

public class BlazeMapServerEngine {
    private static final Map<ResourceKey<Level>, ServerPipeline> PIPELINES = new HashMap<>();
    private static DebouncingThread debouncer;
    private static AsyncChain.Root async;
    private static MinecraftServer server;
    private static boolean frozenRegistries;
    private static boolean isRunning;
    private static StorageAccess.Internal storage;
    private static StorageAccess addonStorage;
    private static PipelineProfiler profiler;

    // Initialize in a client side context.
    // Some resources are shared with the client, there's no need to be greedy.
    public static void initForIntegrated() {
        async = new AsyncChain.Root(BlazeMapClientEngine.cruncher(), BlazeMapServerEngine::submit);
        debouncer = BlazeMapClientEngine.debouncer();
        init();
        frozenRegistries = true;
    }

    // Initialize in a dedicated server context.
    // Since there is no client to share computing resources with, instantiate them all.
    public static void initForDedicated() {
        AsyncDataCruncher dataCruncher = new AsyncDataCruncher("Blaze Map (Server)");
        async = new AsyncChain.Root(dataCruncher, BlazeMapServerEngine::submit);
        debouncer = new DebouncingThread("Blaze Map (Server)");
        init();
        frozenRegistries = false;
    }

    // Common context initialization routine.
    private static void init() {
        MinecraftForge.EVENT_BUS.register(BlazeMapServerEngine.class);
        profiler = new PipelineProfiler(
            COLLECTOR_TIME_PROFILER,
            COLLECTOR_LOAD_PROFILER,
            TRANSFORMER_TIME_PROFILER,
            TRANSFORMER_LOAD_PROFILER,
            PROCESSOR_TIME_PROFILER,
            PROCESSOR_LOAD_PROFILER
        );
    }

    private static void submit(Runnable task) {
        if(server == null) return;
        server.submit(task);
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        if(!frozenRegistries) {
            IEventBus bus = MinecraftForge.EVENT_BUS;
            bus.post(new BlazeRegistryEvent.CollectorRegistryEvent());
            bus.post(new BlazeRegistryEvent.TransformerRegistryEvent());
            bus.post(new BlazeRegistryEvent.ProcessorRegistryEvent());
            BlazeMapAPI.COLLECTORS.freeze();
            BlazeMapAPI.TRANSFORMERS.freeze();
            BlazeMapAPI.PROCESSORS.freeze();
            frozenRegistries = true;
        }

        isRunning = true;
        server = event.getServer();
        storage = new StorageAccess.Internal(server.getWorldPath(LevelResource.ROOT).toFile(), "blazemap-server");
        addonStorage = storage.addon();
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {
        isRunning = false;
        server = null;
        storage = null;
        addonStorage = null;
        PIPELINES.clear();
    }

    public static void onChunkChanged(ResourceKey<Level> dim, ChunkPos pos) {
        if(!isRunning) return;
        getPipeline(dim).onChunkChanged(pos);
    }

    private static ServerPipeline getPipeline(ResourceKey<Level> dimension) {
        return PIPELINES.computeIfAbsent(dimension, d -> new ServerPipeline(async, debouncer, profiler, d, () -> server.getLevel(d)));
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static DebouncingThread debouncer() {
        return debouncer;
    }
}