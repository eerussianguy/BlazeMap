package com.eerussianguy.blazemap.api.pipeline;

import com.eerussianguy.blazemap.api.BlazeRegistry;

public interface Producer<T extends MasterDatum> {
    BlazeRegistry.Key<DataType<T>> getOutputID();
}
