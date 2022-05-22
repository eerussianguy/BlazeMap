package com.eerussianguy.blazemap.api;

import com.eerussianguy.blazemap.api.mapping.Collector;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class BlazeMapAPI {
    @SuppressWarnings("unchecked")
    public static final BlazeRegistry<Collector<?>> COLLECTORS = new BlazeRegistry<>((Class<Collector<?>>) (Class<?>) Collector.class);
    public static final BlazeRegistry<Layer> LAYERS = new BlazeRegistry<>(Layer.class);
    public static final BlazeRegistry<MapType> MAPTYPES = new BlazeRegistry<>(MapType.class);
}
