package com.eerussianguy.blazemap.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;

@SuppressWarnings("rawtypes")
public class ChunkMDCache {
    private final Map<Key<DataType>, MasterDatum> cache = new HashMap<>(16);
    private boolean dirty = false;

    public MasterDatum get(Key<DataType> key) {
        return cache.get(key);
    }

    public boolean diff(MasterDatum datum) {
        Key<DataType> key = UnsafeGenerics.stripKey(datum.getID());
        MasterDatum old = cache.get(key);
        if(!datum.equals(old)) {
            cache.put(key, datum);
            dirty = true;
            return true;
        }
        return false;
    }

    public void persist() {
        if(!dirty) return;
        // TODO: implement persistence
    }

    public List<MasterDatum> data() {
        return cache.values().stream().toList();
    }
}
