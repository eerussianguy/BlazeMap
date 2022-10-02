package com.eerussianguy.blazemap.engine;

import java.util.Set;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.pipeline.Collector;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;
import com.eerussianguy.blazemap.api.pipeline.Transformer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class UnsafeGenerics {
    public static <T extends MasterDatum> Key<DataType> stripKey(Key<DataType<T>> key) {
        return (Key<DataType>) (Object) key;
    }

    public static Set<Key<DataType>> stripKeys(Set<Key<DataType<MasterDatum>>> keys) {
        return (Set<Key<DataType>>) (Object) keys;
    }

    static Set<Key<Collector>> stripCollectors(Set<Key<Collector<MasterDatum>>> list) {
        return (Set<Key<Collector>>) (Object) list;
    }

    static Set<Key<Transformer>> stripTransformers(Set<Key<Transformer<MasterDatum>>> list) {
        return (Set<Key<Transformer>>) (Object) list;
    }
}
