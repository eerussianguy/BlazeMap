package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.mapping.Collector;

public class TerrainHeightCollector extends Collector<TerrainHeightMD> {

    public TerrainHeightCollector() {
        super(BlazeMapReferences.MasterData.TERRAIN_HEIGHT);
    }

    @Override
    public TerrainHeightMD collect(Level level, BlockPos.MutableBlockPos mutable, int minX, int minZ, int maxX, int maxZ) {

        final int[][] heightmap = new int[16][16];

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                heightmap[x][z] = level.getHeight(Heightmap.Types.MOTION_BLOCKING, minX + x, minZ + z);
            }
        }

        return new TerrainHeightMD(level.getMinBuildHeight(), level.getMaxBuildHeight(), level.getHeight(), level.getSeaLevel(), minX, minZ, heightmap);
    }
}
