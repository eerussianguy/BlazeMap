package com.eerussianguy.blazemap.feature.mapping;

import java.awt.image.BufferedImage;

import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.IMapView;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MasterData;

public class TerrainHeightLayer extends Layer
{
    private static final int OPAQUE = 0xFF000000;

    public TerrainHeightLayer()
    {
        super(BlazeMapReferences.LAYER_TERRAIN_HEIGHT, BlazeMapReferences.MD_TERRAIN_HEIGHT);
    }

    @Override
    public boolean renderTile(BufferedImage tile, IMapView<ResourceLocation, MasterData> data)
    {
        TerrainHeightMD terrain = data.get(BlazeMapReferences.MD_TERRAIN_HEIGHT, TerrainHeightMD.class);
        float down = 1.0F / ((float) terrain.sea - terrain.minY);
        float up = 1.0F / ((float) terrain.maxY - terrain.sea);
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
            {
                int h = terrain.heightmap[x][z] + 1;
                if (h < terrain.sea)
                {
                    int red = ((int) (255F * ((float) h) * down)) << 16;
                    tile.setRGB(x, z, OPAQUE | red);
                    continue;
                }
                if (h > terrain.sea)
                {
                    int tone = (int) (255F * ((float) h) * up);
                    tile.setRGB(x, z, OPAQUE | (tone << 16) | 0xFF00 | tone);
                    continue;
                }
                tile.setRGB(x, z, OPAQUE | 0x0088FF);
            }
        return true;
    }
}
