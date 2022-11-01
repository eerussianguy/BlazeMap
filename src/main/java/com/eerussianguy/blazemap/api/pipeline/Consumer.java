package com.eerussianguy.blazemap.api.pipeline;

import java.util.Set;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;

public interface Consumer {
    Set<Key<DataType<MasterDatum>>> getInputIDs();
}
