package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.mapping.Collector;

public class NetherCollector extends Collector<TerrainHeightMD> {
    public NetherCollector() {
        super(BlazeMapReferences.Collectors.NETHER);
    }

    @Override
    public TerrainHeightMD collect(Level level, int minX, int minZ, int maxX, int maxZ) {
        final int[][] heightmap = new int[16][16];

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int height = 110;
                while(isNotAir(level, minX + x, height - 1, minZ + z)) {
                    height--;
                    if(height <= level.getMinBuildHeight()) break;
                }
                if(height > level.getMinBuildHeight()) {
                    while(isNotBaseStone(level, minX + x, height - 1, minZ + z)) {
                        height--;
                        if(height <= level.getMinBuildHeight()) break;
                    }
                }
                heightmap[x][z] = height;
            }
        }

        return new TerrainHeightMD(level.getMinBuildHeight(), level.getMaxBuildHeight(), level.getHeight(), level.getSeaLevel(), minX, minZ, heightmap);
    }

    private boolean isNotAir(Level level, int x, int y, int z) {
        return !level.getBlockState(POS.set(x, y, z)).isAir();
    }

    private boolean isNotBaseStone(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(POS.set(x, y, z));
        return !state.getMaterial().isSolid();
    }
}
