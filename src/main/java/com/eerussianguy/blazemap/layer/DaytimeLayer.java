package com.eerussianguy.blazemap.layer;

import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.data.BlockStateDataReader;

public class DaytimeLayer extends Layer<BlockStateDataReader>
{
    public DaytimeLayer(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public int compositePosition(BlockStateDataReader data, int x, int z)
    {
        return data.colorFor(x, z);
    }
}
