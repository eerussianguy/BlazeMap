package com.eerussianguy.blazemap.data;

import java.util.*;

import net.minecraft.world.level.ChunkPos;

import com.eerussianguy.blazemap.layer.Layer;

public class MasterData
{
    private static final Map<ChunkPos, Set<Entry<?>>> CACHE = new HashMap<>();

    public static void put(ChunkPos pos, Set<Entry<?>> entries)
    {
        CACHE.put(pos, entries);
    }

    public static void trimAround(ChunkPos origin)
    {
        CACHE.keySet().removeIf(pos -> origin.getChessboardDistance(pos) > 16);
    }

    public static void stitchAround(ChunkPos origin)
    {

    }

    private record Entry<T extends DataReader>(Layer<T> layer, T data) {}
}
