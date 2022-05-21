package com.eerussianguy.blazemap.feature.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.mojang.blaze3d.platform.NativeImage;

public class TerrainHeightLayer extends Layer {

    public TerrainHeightLayer() {
        super(BlazeMapReferences.Layers.TERRAIN_HEIGHT, BlazeMapReferences.MasterData.TERRAIN_HEIGHT, BlazeMapReferences.MasterData.WATER_LEVEL);
    }

    @Override
    public boolean renderTile(NativeImage tile, IDataSource data) {
        TerrainHeightMD terrain = data.get(BlazeMapReferences.MasterData.TERRAIN_HEIGHT);
        WaterLevelMD water = data.get(BlazeMapReferences.MasterData.WATER_LEVEL);
        float down = 1.0F / ((float) terrain.sea - terrain.minY);
        float up = 1.0F / ((float) terrain.maxY - terrain.sea);
        for(int x = 0; x < 16; x++)
            for(int z = 0; z < 16; z++) {
                int h = terrain.heightmap[x][z] - water.level[x][z];
                if(h < terrain.sea) {
                    int red = (int) (255F * ((float) h) * down);
                    tile.setPixelRGBA(x, z, OPAQUE | red);
                }
                else {
                    int tone = (int) (255F * ((float) h) * up);
                    tile.setPixelRGBA(x, z, OPAQUE | (tone << 16) | 0xFF00 | tone);
                }
            }
        return true;
    }
}
