package com.eerussianguy.blazemap.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import com.eerussianguy.blazemap.util.Helpers;

public class BlazeNetwork {
    private static final String ENGINE_VERSION = "1";
    private static final ResourceLocation ENGINE_CHANNEL = Helpers.identifier("engine");
    public static final SimpleChannel ENGINE = make(ENGINE_CHANNEL, ENGINE_VERSION);

    public static void init() {
        int id = 0;
        ENGINE.registerMessage(id++, PacketChunkMDUpdate.class, PacketChunkMDUpdate::encode, PacketChunkMDUpdate::decode, (msg, ctx) -> msg.handle(ctx.get()));
    }

    private static SimpleChannel make(ResourceLocation channel, String version) {
        return NetworkRegistry.newSimpleChannel(channel, () -> version, version::equals, version::equals);
    }
}
