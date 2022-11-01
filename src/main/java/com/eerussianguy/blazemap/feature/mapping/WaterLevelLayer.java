package com.eerussianguy.blazemap.feature.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.maps.Layer;
import com.eerussianguy.blazemap.api.maps.TileResolution;
import com.eerussianguy.blazemap.api.util.ArrayAggregator;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.eerussianguy.blazemap.util.Helpers;
import com.mojang.blaze3d.platform.NativeImage;

public class WaterLevelLayer extends Layer {

    public WaterLevelLayer() {
        super(
            BlazeMapReferences.Layers.WATER_LEVEL,
            Helpers.translate("blazemap.water_depth"),
            Helpers.identifier("textures/map_icons/layer_water.png"),

            BlazeMapReferences.MasterData.WATER_LEVEL
        );
    }

    @Override
    public boolean renderTile(NativeImage tile, TileResolution resolution, IDataSource data, int xGridOffset, int zGridOffset) {
        WaterLevelMD water = (WaterLevelMD) data.get(BlazeMapReferences.MasterData.WATER_LEVEL);

        foreachPixel(resolution, (x, z) -> {
            int d = ArrayAggregator.avg(relevantData(resolution, x, z, water.level));
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
        });

        return true;
    }
}
