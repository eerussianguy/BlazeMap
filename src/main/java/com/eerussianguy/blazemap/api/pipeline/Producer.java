package com.eerussianguy.blazemap.api.pipeline;

import com.eerussianguy.blazemap.api.BlazeRegistry;

public interface Producer {
    BlazeRegistry.Key<DataType<MasterDatum>> getOutputID();
}
