package com.eerussianguy.blazemap.feature.mapping;

import java.awt.*;

import net.minecraft.client.gui.components.Widget;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.maps.Layer;
import com.eerussianguy.blazemap.api.maps.TileResolution;
import com.eerussianguy.blazemap.api.util.ArrayAggregator;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.mojang.blaze3d.platform.NativeImage;

public class TerrainHeightLayer extends Layer {

    public TerrainHeightLayer() {
        super(
            BlazeMapReferences.Layers.TERRAIN_HEIGHT,
            Helpers.translate("blazemap.terrain_height"),

            BlazeMapReferences.MasterData.TERRAIN_HEIGHT,
            BlazeMapReferences.MasterData.WATER_LEVEL
        );
    }

    /**
     * Entry names are just a rough approximation of Y level. Don't mean a thing.
     */
    private enum Gradient {
        WORLD_TOP(1, new Color(0xFFFFFF)),
        CLOUDS(.75F, new Color(0xFFFFFF)),
        MOUNTAINS(.5F, new Color(0xAADDFF)),
        HILLS(.25F, new Color(0X666688)),
        SEA_LEVEL(.05F, new Color(0x00AA00)),
        UNDERGROUND(-.05F, new Color(0xFFFF00)),
        DEEPSLATE(-.5F, new Color(0xFF4400)),
        BEDROCK(-1F, new Color(0x990000));

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
    public boolean renderTile(NativeImage tile, TileResolution resolution, IDataSource data, int xGridOffset, int zGridOffset) {
        TerrainHeightMD terrain = (TerrainHeightMD) data.get(BlazeMapReferences.MasterData.TERRAIN_HEIGHT);
        WaterLevelMD water = (WaterLevelMD) data.get(BlazeMapReferences.MasterData.WATER_LEVEL);
        float down = -1.0F / ((float) terrain.sea - terrain.minY);
        float up = 1.0F / ((float) terrain.maxY - terrain.sea);

        foreachPixel(resolution, (x, z) -> {
            int h = ArrayAggregator.avg(relevantData(resolution, x, z, terrain.heightmap));
            int d = ArrayAggregator.avg(relevantData(resolution, x, z, water.level));
            paintGradient(tile, x, z, h - d, terrain.sea, down, up);
        });

        return true;
    }

    @Override
    public Widget getLegendWidget() {
        return new TerrainHeightLegendWidget();
    }

    private static final int DOWNSIZE = 4;

    public static NativeImage getLegend(int min, int sea, int max) {
        int delta = (max - min) / DOWNSIZE;
        float down = -1.0F / ((float) sea - min);
        float up = 1.0F / ((float) max - sea);
        NativeImage legend = new NativeImage(1, delta, true);
        for(int y = 0; y < delta; y++) {
            paintGradient(legend, 0, (delta - y) - 1, (y * DOWNSIZE) + min, sea, down, up);
        }
        return legend;
    }

    private static void paintGradient(NativeImage tile, int x, int y, int h, int sea, float down, float up) {
        int height = h - sea;
        int depth = sea - h;
        float point = h == sea ? 0 : h < sea ? down * (depth) : up * (height);
        Gradient top = Gradient.WORLD_TOP;
        for(Gradient bottom : Gradient.VALUES) {
            float epsilon = bottom.keypoint - point;
            if(epsilon < 0.005F && epsilon > -0.005F) {
                tile.setPixelRGBA(x, y, bottom.color);
                return;
            }
            if(point > bottom.keypoint) {
                tile.setPixelRGBA(x, y, Colors.interpolate(bottom.color, bottom.keypoint, top.color, top.keypoint, point));
                break;
            }
            else {
                top = bottom;
            }
        }
    }
}
