package com.eerussianguy.blazemap.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.level.ChunkPos;

import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MasterData;

public class MasterDataCache {
    private final Map<ChunkPos, Set<Entry<?>>> cache = new HashMap<>();

    public void put(ChunkPos pos, Set<Entry<?>> entries) {
        cache.put(pos, entries);
    }

    public void trimAround(ChunkPos origin) {
        cache.keySet().removeIf(pos -> origin.getChessboardDistance(pos) > 16);
    }

    public void stitchAround(ChunkPos origin) {

    }

    private record Entry<T extends MasterData>(Layer layer, T data) {}
}
