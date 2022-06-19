package com.eerussianguy.blazemap.feature.mapping;

import java.awt.*;

import net.minecraft.network.chat.TextComponent;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.eerussianguy.blazemap.util.Colors;
import com.mojang.blaze3d.platform.NativeImage;

public class NetherLayer extends Layer {
    public NetherLayer() {
        super(BlazeMapReferences.Layers.NETHER, new TextComponent("Nether Terrain"), BlazeMapReferences.Collectors.NETHER);
    }

    private enum Gradient {
        CEILING(0.5f, new Color(0x9E9E9E)),
        HIGH_LEVEL(0.3f, new Color(0xFF6666)),
        MID_LEVEL(.5F, new Color(0X9C3A3A)),
        SHORE_LEVEL(0, new Color(0x7A672F)),
        LAVA_LEVEL(-0.05f, new Color(0xED6A28)),
        BEDROCK(-1F, new Color(0x784617));

        public static final Gradient[] VALUES = values();

        final float keypoint;
        final int color;

        Gradient(float keypoint, Color color) {
            this.keypoint = keypoint;
            // NativeImage colors are ABGR. Mojang has no standards. I blame Microsoft.
            this.color = Colors.abgr(color);
        }
    }


    @Override
    public boolean renderTile(NativeImage tile, IDataSource data) {
        TerrainHeightMD terrain = (TerrainHeightMD) data.get(BlazeMapReferences.Collectors.NETHER);
        float down = -1.0F / ((float) terrain.sea - terrain.minY);
        float up = 1.0F / ((float) terrain.maxY - terrain.sea);
        for(int x = 0; x < 16; x++) {
            next_pixel:
            for(int z = 0; z < 16; z++) {
                int h = terrain.heightmap[x][z];
                int height = h - terrain.sea;
                int depth = terrain.sea - h;
                float point = h == terrain.sea ? 0 : h < terrain.sea ? down * (depth) : up * (height);
                Gradient top = Gradient.CEILING;
                for(Gradient bottom : Gradient.VALUES) {
                    float epsilon = bottom.keypoint - point;
                    if(epsilon < 0.005F && epsilon > -0.005F) {
                        tile.setPixelRGBA(x, z, bottom.color);
                        continue next_pixel;
                    }
                    if(point > bottom.keypoint) {
                        tile.setPixelRGBA(x, z, Colors.interpolate(bottom.color, bottom.keypoint, top.color, top.keypoint, point));
                        break;
                    }
                    else {
                        top = bottom;
                    }
                }
            }
        }
        return true;
    }
}
