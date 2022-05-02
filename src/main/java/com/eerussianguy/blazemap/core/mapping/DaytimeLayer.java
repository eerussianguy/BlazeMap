package com.eerussianguy.blazemap.core.mapping;

import com.eerussianguy.blazemap.api.IMapView;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MasterData;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;

public class DaytimeLayer extends Layer<BlockStateMD>
{
    public DaytimeLayer(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public boolean renderTile(BufferedImage tile, IMapView<ResourceLocation, MasterData> data) {
        return true;
    }
}
