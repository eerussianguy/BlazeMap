package com.eerussianguy.blazemap.engine;

import java.util.Set;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;
import com.eerussianguy.blazemap.api.util.IDataSource;

@SuppressWarnings("rawtypes")
public class MapView implements IDataSource {
    private ChunkMDCache source;
    private Set<Key<DataType>> filter;

    public MapView() {}

    public MapView setSource(ChunkMDCache source) {
        this.source = source;
        return this;
    }

    public void setFilter(Set<Key<DataType>> filter) {
        this.filter = filter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends MasterDatum> T get(Key<DataType<T>> key) {
        if(!filter.contains(key)) return null;
        return (T) source.get(UnsafeGenerics.stripKey(key));
    }
}
