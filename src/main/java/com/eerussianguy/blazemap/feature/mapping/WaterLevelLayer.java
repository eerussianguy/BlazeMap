package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.network.chat.TextComponent;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.mojang.blaze3d.platform.NativeImage;

public class WaterLevelLayer extends Layer {

    public WaterLevelLayer() {
        super(BlazeMapReferences.Layers.WATER_LEVEL, new TextComponent("Water Depth"), BlazeMapReferences.Collectors.WATER_LEVEL);
    }

    @Override
    public boolean renderTile(NativeImage tile, IDataSource data) {
        WaterLevelMD water = (WaterLevelMD) data.get(BlazeMapReferences.Collectors.WATER_LEVEL);
        for(int x = 0; x < 16; x++)
            for(int z = 0; z < 16; z++) {
                int d = water.level[x][z];
                if(d > 0) {
                    float brightness = 1F;
                    int blue = 191, green = 95;
                    if(d > 30) d = 30;
                    brightness -= ((float) d) * 0.02F;
                    blue *= brightness;
                    green *= brightness;
                    tile.setPixelRGBA(x, z, OPAQUE | blue << 16 | green << 8);
                }
                else {
                    tile.setPixelRGBA(x, z, 0);
                }
            }
        return true;
    }
}
