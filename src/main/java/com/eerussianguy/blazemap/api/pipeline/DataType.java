package com.eerussianguy.blazemap.api.pipeline;

import java.io.IOException;

import net.minecraft.network.FriendlyByteBuf;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.util.MinecraftStreams;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

/**
 * DataType is actually not data, but the (de)serializer for a specific kind of MasterDatum.
 * The reason for this is explained in depth in the javadocs for BlazeMapAPI.MASTER_DATA
 */
public interface DataType<T extends MasterDatum> extends BlazeRegistry.RegistryEntry {
    void serialize(MinecraftStreams.Output stream, T datum) throws IOException;

    T deserialize(MinecraftStreams.Input stream) throws IOException;

    default void serialize(FriendlyByteBuf buffer, T datum) {
        MinecraftStreams.Output stream = new MinecraftStreams.Output(new ByteBufOutputStream(buffer));
        try {
            serialize(stream, datum);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    default T deserialize(FriendlyByteBuf buffer) {
        MinecraftStreams.Input stream = new MinecraftStreams.Input(new ByteBufInputStream(buffer));
        try {
            return deserialize(stream);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
