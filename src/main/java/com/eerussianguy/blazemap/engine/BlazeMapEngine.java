package com.eerussianguy.blazemap.engine;

import com.eerussianguy.blazemap.ClientUtils;
import com.eerussianguy.blazemap.engine.threading.AsyncDataCruncher;
import com.eerussianguy.blazemap.engine.threading.ThreadHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BlazeMapEngine {
    private static Map<ResourceKey<Level>, CartographyPipeline> pipelines = new HashMap<>();
    private static ThreadHandler.Root threading;
    private static AsyncDataCruncher dataCruncher;
    private static CartographyPipeline activePipeline = null;
    private static String serverID;
    private static File serverDir;

    public static void init(){
        MinecraftForge.EVENT_BUS.register(BlazeMapEngine.class);
        dataCruncher = new AsyncDataCruncher("Blaze Map");
        threading = new ThreadHandler.Root(dataCruncher, null);
    }

    public static ThreadHandler.Root threading(){
        return threading;
    }

    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event){
        serverID = ClientUtils.getServerID();
        serverDir = new File(ClientUtils.getBaseDir(), serverID);
    }

    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent event){
        pipelines.clear();
        activePipeline.shutdown();
        activePipeline = null;
        serverID = null;
        serverDir = null;
    }

    @SubscribeEvent
    public static void onChangeWorld(PlayerEvent.PlayerChangedDimensionEvent event){
        activePipeline.shutdown();
        activePipeline = pipelines.computeIfAbsent(event.getTo(), level -> new CartographyPipeline(serverDir, level));
    }

    @SubscribeEvent
    public static void onChunkChanged(){
        activePipeline.markChunkDirty(pos);
    }

    @SubscribeEvent
    public static void onChunkLoaded(){
        activePipeline.markChunkDirty(pos);
    }
}
