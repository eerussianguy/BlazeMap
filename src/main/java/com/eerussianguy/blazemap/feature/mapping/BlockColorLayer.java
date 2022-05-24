package com.eerussianguy.blazemap.feature.mapping;

import java.awt.*;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.BlockColorMD;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.mojang.blaze3d.platform.NativeImage;

public class BlockColorLayer extends Layer {

    public BlockColorLayer() {
        super(BlazeMapReferences.Layers.BLOCK_COLOR, BlazeMapReferences.Collectors.BLOCK_COLOR);
    }

    @Override
    public boolean renderTile(NativeImage tile, IDataSource data) {
        int[][] blockColors = ((BlockColorMD) data.get(BlazeMapReferences.Collectors.BLOCK_COLOR)).colors();

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                tile.setPixelRGBA(x, z, abgr(new Color(blockColors[x][z])));
            }
        }
        return true;
    }
}
