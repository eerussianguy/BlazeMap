package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.mapping.Collector;

public class TerrainHeightCollector extends Collector<TerrainHeightMD> {

    public TerrainHeightCollector() {
        super(BlazeMapReferences.MD_TERRAIN_HEIGHT);
    }

    @Override
    public TerrainHeightMD collect(Level level, int minX, int minZ, int maxX, int maxZ) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        int height = level.getHeight();
        int sea = level.getSeaLevel();
        int[][] heightmap = new int[16][16];

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
        for(int x = 0; x < 16; x++) {
            pos.setX(minX + x);
            for(int z = 0; z < 16; z++) {
                pos.setZ(minZ + z);
                heightmap[x][z] = minY;
                for(int y = maxY; y >= minY; y--) {
                    pos.setY(y);
                    BlockState bs = level.getBlockState(pos);
                    if(!bs.isAir()) {
                        heightmap[x][z] = y;
                        break;
                    }
                }
            }
        }

        return new TerrainHeightMD(minY, maxY, height, sea, minX, minZ, heightmap);
    }
}
