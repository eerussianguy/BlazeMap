package com.eerussianguy.blazemap.api.pipeline;

import com.eerussianguy.blazemap.api.util.MinecraftStreams;

/**
 * DataType is actually not data, but the (de)serializer for a specific kind of MasterDatum.
 * The reason for this is explained in depth in the javadocs for BlazeMapAPI.MASTER_DATA
 */
public interface DataType<T extends MasterDatum> {
    void serialize(MinecraftStreams.Output stream, T datum);

    T deserialize(MinecraftStreams.Input stream);
}
