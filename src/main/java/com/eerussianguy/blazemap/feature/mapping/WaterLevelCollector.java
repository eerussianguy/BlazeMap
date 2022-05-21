package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.mapping.Collector;

public class WaterLevelCollector extends Collector<WaterLevelMD> {

    public WaterLevelCollector() {
        super(BlazeMapReferences.MasterData.WATER_LEVEL);
    }

    @Override
    public WaterLevelMD collect(Level level, BlockPos.MutableBlockPos mutable, int minX, int minZ, int maxX, int maxZ) {

        final int[][] water = new int[16][16];

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int depth = 0, height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, minX + x, minZ + z) - 1;
                while(isWater(level, minX + x, height - depth, minZ + z)) {
                    depth++;
                    if(height - depth < level.getMinBuildHeight()) break;
                }
                water[x][z] = depth;
            }
        }

        return new WaterLevelMD(level.getSeaLevel(), minX, minZ, water);
    }
}
