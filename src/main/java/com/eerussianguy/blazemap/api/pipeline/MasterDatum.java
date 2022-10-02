package com.eerussianguy.blazemap.api.pipeline;

import com.eerussianguy.blazemap.api.BlazeRegistry;

public interface MasterDatum {
    BlazeRegistry.Key<DataType<MasterDatum>> getID();
}
