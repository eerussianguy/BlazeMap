package com.eerussianguy.blazemap.layer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.data.DataReader;

public abstract class Layer<T extends DataReader>
{
    private static final Map<ResourceLocation, Layer<?>> REGISTRY = new HashMap<>();

    public static synchronized <T extends DataReader> Layer<T> register(ResourceLocation id, Layer<T> instance)
    {
        if (REGISTRY.containsKey(id))
        {
            throw new IllegalArgumentException("Duplicate key: " + id);
        }
        REGISTRY.put(id, instance);
        return instance;
    }

    private final ResourceLocation id;

    public Layer(ResourceLocation id)
    {
        this.id = id;
    }

    public ResourceLocation getID()
    {
        return id;
    }

    abstract int compositePosition(T data, int x, int z);
}
