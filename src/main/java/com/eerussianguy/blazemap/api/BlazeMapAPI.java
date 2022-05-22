package com.eerussianguy.blazemap.api;

import com.eerussianguy.blazemap.api.mapping.Collector;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.mapping.MasterDatum;

public class BlazeMapAPI {
    public static final BlazeRegistry<Collector<MasterDatum>> COLLECTORS = new BlazeRegistry<>();
    public static final BlazeRegistry<Layer> LAYERS = new BlazeRegistry<>();
    public static final BlazeRegistry<MapType> MAPTYPES = new BlazeRegistry<>();
}
