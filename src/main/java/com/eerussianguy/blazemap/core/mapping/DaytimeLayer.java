package com.eerussianguy.blazemap.core.mapping;

import com.eerussianguy.blazemap.api.mapping.Layer;
import net.minecraft.resources.ResourceLocation;

public class DaytimeLayer extends Layer<BlockStateMD>
{
    public DaytimeLayer(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public int compositePosition(BlockStateMD data, int x, int z)
    {
        return data.colorFor(x, z);
    }
}
