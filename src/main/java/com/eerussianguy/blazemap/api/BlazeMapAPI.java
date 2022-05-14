package com.eerussianguy.blazemap.api;

import com.eerussianguy.blazemap.api.mapping.Collector;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class BlazeMapAPI
{
    public static final Registry<Collector<?>> COLLECTORS = new Registry<>();
    public static final Registry<Layer> LAYERS = new Registry<>();
    public static final Registry<MapType> MAPTYPES = new Registry<>();
}
