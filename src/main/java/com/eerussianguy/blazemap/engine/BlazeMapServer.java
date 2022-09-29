package com.eerussianguy.blazemap.engine;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.event.BlazeRegistryEvent;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.AsyncDataCruncher;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;
import com.eerussianguy.blazemap.util.Helpers;

public class BlazeMapServer {
    private static DebouncingThread debouncer;
    private static AsyncChain.Root async;
    private static MinecraftServer server;
    private static boolean frozenRegistries;
    private static boolean isRunning;
    private static StorageAccess.Internal storage;
    private static StorageAccess addonStorage;

    // Initialize in a client side context.
    // Some resources are shared with the client, there's no need to be greedy.
    public static void initForIntegrated() {
        async = new AsyncChain.Root(BlazeMapEngine.cruncher(), Helpers::runOnMainThread); // TODO: this is Helpers::runOnMainThread is the wrong task queue to use, find the integrated server one;
        debouncer = BlazeMapEngine.debouncer();
        init();
        frozenRegistries = true;
    }

    // Initialize in a dedicated server context.
    // Since there is no client to share computing resources with, instantiate them all.
    public static void initForDedicated() {
        AsyncDataCruncher dataCruncher = new AsyncDataCruncher("Blaze Map (Server)");
        async = new AsyncChain.Root(dataCruncher, Helpers::runOnMainThread); // TODO: this is Helpers::runOnMainThread is the wrong task queue to use, find the integrated server one;
        debouncer = new DebouncingThread("Blaze Map (Server)");
        init();
        frozenRegistries = false;
    }

    // Common context initialization routine.
    private static void init() {
        MinecraftForge.EVENT_BUS.register(BlazeMapServer.class);
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        if(!frozenRegistries) {
            IEventBus bus = MinecraftForge.EVENT_BUS;
            bus.post(new BlazeRegistryEvent.CollectorRegistryEvent());
            bus.post(new BlazeRegistryEvent.ProcessorRegistryEvent());
            BlazeMapAPI.COLLECTORS.freeze();
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
    }

    public static boolean isRunning(){
        return isRunning;
    }
}