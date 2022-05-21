package com.eerussianguy.blazemap.api.util;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.mapping.Collector;
import com.eerussianguy.blazemap.api.mapping.MasterDatum;

public interface IDataSource {
    <T extends MasterDatum> T get(BlazeRegistry.Key<Collector<T>> key);
}
