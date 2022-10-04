package com.eerussianguy.blazemap.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;
import com.eerussianguy.blazemap.engine.client.BlazeMapClientEngine;

public class PacketChunkMDUpdate {
    public final ResourceKey<Level> dimension;
    public final ChunkPos pos;
    public final List<MasterDatum> data;

    public PacketChunkMDUpdate(ResourceKey<Level> dimension, ChunkPos pos, List<MasterDatum> data) {
        this.dimension = dimension;
        this.pos = pos;
        this.data = data;
    }

    public void send(LevelChunk chunk) {
        BlazeNetwork.ENGINE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), this);
    }

    void handle(NetworkEvent.Context context) {
        BlazeMapClientEngine.submitChanges(dimension, pos, data);
        context.setPacketHandled(true);
    }

    static void encode(PacketChunkMDUpdate packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension.registry());
        buffer.writeResourceLocation(packet.dimension.location());
        buffer.writeChunkPos(packet.pos);
        buffer.writeInt(packet.data.size());
        for(MasterDatum md : packet.data) {
            BlazeRegistry.Key<DataType<MasterDatum>> key = md.getID();
            buffer.writeResourceLocation(key.location);
            key.value().serialize(buffer, md);
        }
    }

    static PacketChunkMDUpdate decode(FriendlyByteBuf buffer) {
        ResourceLocation registry = buffer.readResourceLocation();
        ResourceLocation location = buffer.readResourceLocation();
        ResourceKey<Level> dimension = ResourceKey.create(ResourceKey.createRegistryKey(registry), location);
        ChunkPos pos = buffer.readChunkPos();
        int count = buffer.readInt();
        List<MasterDatum> data = new ArrayList<>(count);
        for(int i = 0; i < count; i++) {
            BlazeRegistry.Key<DataType<MasterDatum>> key = BlazeMapAPI.MASTER_DATA.findOrCreate(buffer.readResourceLocation());
            data.add(key.value().deserialize(buffer));
        }
        return new PacketChunkMDUpdate(dimension, pos, data);
    }
}
