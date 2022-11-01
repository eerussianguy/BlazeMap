package com.eerussianguy.blazemap.feature.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.BlockColorMD;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.maps.Layer;
import com.eerussianguy.blazemap.api.maps.TileResolution;
import com.eerussianguy.blazemap.api.util.ArrayAggregator;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.mojang.blaze3d.platform.NativeImage;

public class BlockColorLayer extends Layer {

    public BlockColorLayer() {
        super(
            BlazeMapReferences.Layers.BLOCK_COLOR,
            Helpers.translate("blazemap.block_color"),

            BlazeMapReferences.MasterData.BLOCK_COLOR,
            BlazeMapReferences.MasterData.WATER_LEVEL
        );
    }

    @Override
    public boolean renderTile(NativeImage tile, TileResolution resolution, IDataSource data, int xGridOffset, int zGridOffset) {
        BlockColorMD blocks = (BlockColorMD) data.get(BlazeMapReferences.MasterData.BLOCK_COLOR);
        if(blocks == null) return false;

        int[][] blockColors = blocks.colors;
        int[][] depths = ((WaterLevelMD) data.get(BlazeMapReferences.MasterData.WATER_LEVEL)).level;

        foreachPixel(resolution, (x, z) -> {
            int depth = ArrayAggregator.avg(relevantData(resolution, x, z, depths));
            int color = ArrayAggregator.avgColor(relevantData(resolution, x, z, blockColors));
            float point = ((float) Math.min(depth, 30)) / 50F;
            tile.setPixelRGBA(x, z, Colors.interpolate(Colors.abgr(OPAQUE | color), 0, OPAQUE, 1, point));
        });

        return true;
    }
}
