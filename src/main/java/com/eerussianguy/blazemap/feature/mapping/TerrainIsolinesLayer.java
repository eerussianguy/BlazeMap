package com.eerussianguy.blazemap.feature.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.maps.Layer;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.eerussianguy.blazemap.util.Helpers;
import com.mojang.blaze3d.platform.NativeImage;

public class TerrainIsolinesLayer extends Layer {
    private static final int FULL = 0x5F444444;
    private static final int HALF = 0x3F444444;

    private static int height(TerrainHeightMD terrain, WaterLevelMD water, int x, int z, int def) {
        if(x < 0 || z < 0 || x > 15 || z > 15) return def;
        return terrain.heightmap[x][z] - water.level[x][z];
    }

    private static int delta(int h, int n, int p) {
        int h1 = h - (h % 4);
        int h2 = h - (h % 8);
        if(h2 > n) return FULL;
        if(h1 > n) return p == FULL ? FULL : HALF;
        return p;
    }

    public TerrainIsolinesLayer() {
        super(
            BlazeMapReferences.Layers.TERRAIN_ISOLINES,
            Helpers.translate("blazemap.terrain_isolines"),
            Helpers.identifier("textures/map_icons/layer_terrain_isolines.png"),

            BlazeMapReferences.MasterData.TERRAIN_HEIGHT,
            BlazeMapReferences.MasterData.WATER_LEVEL
        );
    }

    @Override
    public boolean renderTile(NativeImage tile, IDataSource data) {
        TerrainHeightMD terrain = (TerrainHeightMD) data.get(BlazeMapReferences.MasterData.TERRAIN_HEIGHT);
        WaterLevelMD water = (WaterLevelMD) data.get(BlazeMapReferences.MasterData.WATER_LEVEL);
        for(int x = 0; x < 16; x++)
            for(int z = 0; z < 16; z++) {
                int p = 0, h = terrain.heightmap[x][z] - water.level[x][z];
                p = delta(h, height(terrain, water, x + 1, z, h), p);
                p = delta(h, height(terrain, water, x - 1, z, h), p);
                p = delta(h, height(terrain, water, x, z + 1, h), p);
                p = delta(h, height(terrain, water, x, z - 1, h), p);
                tile.setPixelRGBA(x, z, p);
            }
        return true;
    }
}
