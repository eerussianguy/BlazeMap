package com.eerussianguy.blazemap.api;

import com.eerussianguy.blazemap.api.mapping.*;

public class BlazeMapAPI {
    public static final BlazeRegistry<Collector<MasterDatum>> COLLECTORS = new BlazeRegistry<>();
    public static final BlazeRegistry<Layer> LAYERS = new BlazeRegistry<>();
    public static final BlazeRegistry<MapType> MAPTYPES = new BlazeRegistry<>();

    public static final BlazeRegistry<Processor> PROCESSORS = new BlazeRegistry<>();
}
