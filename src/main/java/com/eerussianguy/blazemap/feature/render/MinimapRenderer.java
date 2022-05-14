package com.eerussianguy.blazemap.feature.render;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;

import com.eerussianguy.blazemap.Helpers;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class MinimapRenderer implements AutoCloseable
{
    private MapType mapType;

    private final RenderType textRenderType;
    private final DynamicTexture texture;

    public MinimapRenderer()
    {
        this.textRenderType = RenderType.text(Helpers.identifier("default"));
        this.texture = new DynamicTexture(512, 512, false);
    }

    public void setMapType(MapType type)
    {
        mapType = type;
    }

    public void replaceData()
    {

    }

    public void updateTexture()
    {
        for (int u = 0; u < 128; u++)
        {
            for (int v = 0; v < 128; v++)
            {
                final int idx = u + v * 128;

            }
        }
        texture.upload();
    }

    @Override
    public void close() throws Exception
    {

    }
}
