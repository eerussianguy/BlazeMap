package com.eerussianguy.blazemap.collector;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import com.eerussianguy.blazemap.data.DataReader;

public abstract class Collector<T extends DataReader>
{
    private static final Map<ResourceLocation, Collector<?>> REGISTRY = new HashMap<>();

    public static synchronized <T extends DataReader> Collector<T> register(ResourceLocation id, Collector<T> instance)
    {
        if (REGISTRY.containsKey(id))
        {
            throw new IllegalArgumentException("Duplicate key: " + id);
        }
        REGISTRY.put(id, instance);
        return instance;
    }

    abstract T collect(Level level, ChunkAccess chunk, int minX, int minZ, int maxX, int maxZ);
}
