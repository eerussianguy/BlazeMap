package com.eerussianguy.blazemap.engine;

import com.eerussianguy.blazemap.ClientUtils;
import com.eerussianguy.blazemap.engine.async.AsyncDataCruncher;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlazeMapEngine {
    private static final Map<ResourceKey<Level>, CartographyPipeline> PIPELINES = new HashMap<>();
    private static DebouncingThread debouncer;
    private static AsyncChain.Root async;
    private static AsyncDataCruncher dataCruncher;
    private static CartographyPipeline activePipeline;
    private static PacketListener listener;
    private static String serverID;
    private static File serverDir;

    public static void init(){
        MinecraftForge.EVENT_BUS.register(BlazeMapEngine.class);
        dataCruncher = new AsyncDataCruncher("Blaze Map");
        async = new AsyncChain.Root(dataCruncher, ClientUtils::runOnMainThread);
        debouncer = new DebouncingThread("BlazeMap Engine");
    }

    public static AsyncChain.Root async(){
        return async;
    }

    public static DebouncingThread debouncer(){
        return debouncer;
    }

    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event){
        if(event.getPlayer() == null) return;
        serverID = ClientUtils.getServerID();
        serverDir = new File(ClientUtils.getBaseDir(), serverID);
        LocalPlayer player = event.getPlayer();
        player.connection.getConnection().channel().pipeline().addFirst(listener = new PacketListener());
        switchToPipeline(player.level.dimension());
    }

    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent event){
        if(event.getPlayer() == null) return;
        event.getPlayer().connection.getConnection().channel().pipeline().remove(listener);
        PIPELINES.clear();
        if(activePipeline != null){
            activePipeline.shutdown();
            activePipeline = null;
        }
        serverID = null;
        serverDir = null;
    }

    @SubscribeEvent
    public static void onChangeWorld(PlayerEvent.PlayerChangedDimensionEvent event){
        switchToPipeline(event.getTo());
    }

    private static void switchToPipeline(ResourceKey<Level> level){
        if(activePipeline != null){
            if(activePipeline.world.equals(level)) return;
            activePipeline.shutdown();
        }
        activePipeline = PIPELINES.computeIfAbsent(level, l -> new CartographyPipeline(serverDir, l));
    }

    public static void onChunkLoaded(){
        // activePipeline.markChunkDirty(pos);
    }

    public static void onChunkChanged(ChunkPos pos){
        if(activePipeline == null) return;
        activePipeline.markChunkDirty(pos);
    }

    private static void onClientBoundPacket(Object packet){
        if(packet instanceof ClientboundBlockUpdatePacket){
            onChunkChanged(new ChunkPos(((ClientboundBlockUpdatePacket) packet).getPos()));
        }else if(packet instanceof ClientboundSectionBlocksUpdatePacket){
            Set<ChunkPos> chunks = new HashSet<>();
            ((ClientboundSectionBlocksUpdatePacket) packet).runUpdates((blockPos, blockState) -> chunks.add(new ChunkPos(blockPos)));
            for(ChunkPos pos : chunks) onChunkChanged(pos);
        }
    }

    private static class PacketListener extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
            dataCruncher.submit(() -> BlazeMapEngine.onClientBoundPacket(packet));
            super.channelRead(ctx, packet);
        }
    };
}
