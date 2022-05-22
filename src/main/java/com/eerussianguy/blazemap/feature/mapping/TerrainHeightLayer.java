package com.eerussianguy.blazemap.feature.mapping;

import java.awt.Color;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.eerussianguy.blazemap.util.Colors;
import com.mojang.blaze3d.platform.NativeImage;

public class TerrainHeightLayer extends Layer {

    public TerrainHeightLayer() {
        super(BlazeMapReferences.Layers.TERRAIN_HEIGHT, BlazeMapReferences.MasterData.TERRAIN_HEIGHT, BlazeMapReferences.MasterData.WATER_LEVEL);
    }

    // Normalization
    // WORLD TOP =  1
    // SEA LEVEL =  0
    // BEDROCK   = -1
    private enum Gradient { // Entry names are just a rough approximation of Y level. Don't mean a thing.
        WORLD_TOP(1, new Color(0xFFFFFF)),
        CLOUDS(.75F, new Color(0xAADDFF)),
        MOUNTAINS(.5F, new Color(0X666688)),
        HILLS(.25F, new Color(0x00AA00)),
        SEA_LEVEL(0, new Color(0xCCFF00)),
        UNDERGROUND(-.05F, new Color(0XFFCC00)),
        DEEPSLATE(-.5F, new Color(0x990000)),
        BEDROCK(-1F, new Color(0x222222));

        final float keypoint;
        final int color;

        Gradient(float keypoint, Color color) {
            this.keypoint = keypoint;
            // NativeImage colors are ABGR. Mojang has no standards. I blame Microsoft.
            this.color = OPAQUE | color.getBlue() << 16 | color.getGreen() << 8 | color.getRed();
        }
    }

    @Override
    public boolean renderTile(NativeImage tile, IDataSource data) {
        TerrainHeightMD terrain = (TerrainHeightMD) data.get(BlazeMapReferences.MasterData.TERRAIN_HEIGHT);
        WaterLevelMD water = (WaterLevelMD) data.get(BlazeMapReferences.MasterData.WATER_LEVEL);
        float down = -1.0F / ((float) terrain.sea - terrain.minY);
        float up = 1.0F / ((float) terrain.maxY - terrain.sea);
        for(int x = 0; x < 16; x++) {
            next_pixel:
            for(int z = 0; z < 16; z++) {
                int h = terrain.heightmap[x][z] - water.level[x][z];
                int height = h - terrain.sea;
                int depth = terrain.sea - h;
                float point = h == terrain.sea ? 0 : h < terrain.sea ? down * (depth) : up * (height);
                Gradient top = Gradient.WORLD_TOP;
                for(Gradient bottom : Gradient.values()) {
                    float epsilon = bottom.keypoint - point;
                    if(epsilon < 0.005F && epsilon > -0.005F) {
                        tile.setPixelRGBA(x, z, bottom.color);
                        continue next_pixel;
                    }
                    if(point > bottom.keypoint){
                        tile.setPixelRGBA(x, z, Colors.interpolate(bottom.color, bottom.keypoint, top.color, top.keypoint, point));
                        break;
                    }else{
                        top = bottom;
                    }
                }
            }
        }
        return true;
    }
}
