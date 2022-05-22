package com.eerussianguy.blazemap.engine;

import java.util.Map;
import java.util.Set;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.mapping.Collector;
import com.eerussianguy.blazemap.api.mapping.MasterDatum;
import com.eerussianguy.blazemap.api.util.IDataSource;

public class MapView implements IDataSource {
    private final Map<BlazeRegistry.Key<Collector<?>>, MasterDatum> source;
    private Set<BlazeRegistry.Key<Collector<?>>> filter;

    public MapView(Map<BlazeRegistry.Key<Collector<?>>, MasterDatum> source) {
        this.source = source;
    }

    public void setFilter(Set<BlazeRegistry.Key<Collector<?>>> filter) {
        this.filter = filter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends MasterDatum> T get(BlazeRegistry.Key<Collector<T>> key) {
        if(!filter.contains(key)) return null;
        return (T) source.get(key);
    }
}
