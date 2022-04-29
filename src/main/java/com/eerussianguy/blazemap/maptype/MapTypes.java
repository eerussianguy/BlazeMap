package com.eerussianguy.blazemap.maptype;

import com.eerussianguy.blazemap.Helpers;
import com.eerussianguy.blazemap.layer.Layers;

public class MapTypes
{
    public static void init() { }

    public static final MapType DEFAULT = MapType.register(Helpers.identifier("default"), new MapType(Layers.DAYTIME));
}
