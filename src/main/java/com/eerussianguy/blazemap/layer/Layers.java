package com.eerussianguy.blazemap.layer;

import com.eerussianguy.blazemap.Helpers;
import com.eerussianguy.blazemap.data.BlockStateDataReader;

public class Layers
{
    public static void init() { }

    public static final Layer<BlockStateDataReader> DAYTIME = Layer.register(Helpers.identifier("daytime"), new DaytimeLayer(Helpers.identifier("daytime")));
}
