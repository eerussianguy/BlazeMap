package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.pipeline.Collector;

public class TerrainHeightCollector extends Collector<TerrainHeightMD> {

    public TerrainHeightCollector() {
        super(
            BlazeMapReferences.Collectors.TERRAIN_HEIGHT,
            BlazeMapReferences.MasterData.TERRAIN_HEIGHT
        );
    }

    @Override
    public TerrainHeightMD collect(Level level, int minX, int minZ, int maxX, int maxZ) {

        final int[][] heightmap = new int[16][16];

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, minX + x, minZ + z);
                boolean foundLeaves = false;
                while(isLeaves(level, minX + x, height - 1, minZ + z)) {
                    height--;
                    if(height <= level.getMinBuildHeight()) break;
                    foundLeaves = true;
                }
                while(foundLeaves && isSkippableAfterLeaves(level, minX + x, height - 1, minZ + z)) {
                    height--;
                    if(height <= level.getMinBuildHeight()) break;
                }
                heightmap[x][z] = height;
            }
        }

        return new TerrainHeightMD(BlazeMapReferences.MasterData.TERRAIN_HEIGHT, level.getMinBuildHeight(), level.getMaxBuildHeight(), level.getHeight(), level.getSeaLevel(), minX, minZ, heightmap);
    }

    protected static boolean isSkippableAfterLeaves(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(POS.set(x, y, z));
        return state.is(BlockTags.LEAVES) || state.isAir() || state.is(BlockTags.LOGS) || state.getMaterial().isReplaceable();
    }
}
