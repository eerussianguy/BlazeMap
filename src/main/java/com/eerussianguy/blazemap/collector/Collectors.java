package com.eerussianguy.blazemap.collector;

import net.minecraft.world.level.levelgen.Heightmap;

import com.eerussianguy.blazemap.Helpers;
import com.eerussianguy.blazemap.data.BlockStateDataReader;

public class Collectors
{
    public static void init() { }

    public static final Collector<BlockStateDataReader> WORLD_SURFACE = Collector.register(Helpers.identifier("world_surface"), new HeightmapCollector(Heightmap.Types.WORLD_SURFACE));

}
