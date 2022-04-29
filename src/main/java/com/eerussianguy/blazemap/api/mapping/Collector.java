package com.eerussianguy.blazemap.api.mapping;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

public abstract class Collector<T extends MasterData>
{
    protected abstract T collect(Level level, ChunkAccess chunk, int minX, int minZ, int maxX, int maxZ);
}
