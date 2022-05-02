package com.eerussianguy.blazemap.engine;

import com.eerussianguy.blazemap.api.IMapView;

import java.util.Map;
import java.util.Set;

public class MapView<K, V> implements IMapView<K, V> {
    private final Map<K, V> source;
    private Set<K> filter;

    public MapView(Map<K, V> source){
        this.source = source;
    }

    public void setFilter(Set<K> filter){
        this.filter = filter;
    }

    @Override
    public V get(K key) {
        if(!filter.contains(key)) return null;
        return source.get(key);
    }

    @Override
    public <U extends V> U get(K key, Class<U> cls) {
        return (U) get(key);
    }
}
