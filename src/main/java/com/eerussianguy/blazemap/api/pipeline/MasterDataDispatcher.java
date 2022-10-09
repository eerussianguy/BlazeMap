package com.eerussianguy.blazemap.api.pipeline;

import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import com.eerussianguy.blazemap.api.BlazeRegistry;

@FunctionalInterface
public interface MasterDataDispatcher {
    void dispatch(ResourceKey<Level> dimension, ChunkPos pos, List<MasterDatum> data, Set<BlazeRegistry.Key<DataType<MasterDatum>>> diff, String source, LevelChunk chunk);
}
