package com.eerussianguy.blazemap.feature.mapping;

import java.awt.*;

import net.minecraft.network.chat.TextComponent;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.BlockColorMD;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.mojang.blaze3d.platform.NativeImage;

public class BlockColorLayer extends Layer {

    public BlockColorLayer() {
        super(BlazeMapReferences.Layers.BLOCK_COLOR, Helpers.translate("blazemap.block_color"), BlazeMapReferences.Collectors.BLOCK_COLOR, BlazeMapReferences.Collectors.WATER_LEVEL);
    }

    @Override
    public boolean renderTile(NativeImage tile, IDataSource data) {
        int[][] blockColors = ((BlockColorMD) data.get(BlazeMapReferences.Collectors.BLOCK_COLOR)).colors();
        int[][] depth = ((WaterLevelMD) data.get(BlazeMapReferences.Collectors.WATER_LEVEL)).level;

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                float point = ((float) Math.min(depth[x][z], 30)) / 50F;
                int blockColor = Colors.abgr(new Color(blockColors[x][z]));
                tile.setPixelRGBA(x, z, Colors.interpolate(blockColor, 0, OPAQUE, 1, point));
            }
        }
        return true;
    }
}
