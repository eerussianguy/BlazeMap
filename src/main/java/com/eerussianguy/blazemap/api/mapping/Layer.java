package com.eerussianguy.blazemap.api.mapping;

import net.minecraft.resources.ResourceLocation;

public abstract class Layer<T extends MasterData>
{
    private final ResourceLocation id;

    public Layer(ResourceLocation id)
    {
        this.id = id;
    }

    public ResourceLocation getID()
    {
        return id;
    }

    protected abstract int compositePosition(T data, int x, int z);
}
