package com.eerussianguy.blazemap.api.pipeline;

import com.eerussianguy.blazemap.api.BlazeRegistry;

public abstract class MasterDatum {

    public abstract BlazeRegistry.Key<DataType<MasterDatum>> getID();

    public abstract boolean equals(Object other);
}