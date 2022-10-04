package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MaterialColor;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.BlockColorMD;
import com.eerussianguy.blazemap.api.pipeline.Collector;

public class BlockColorCollector extends Collector<BlockColorMD> {

    public BlockColorCollector() {
        super(
            BlazeMapReferences.Collectors.BLOCK_COLOR,
            BlazeMapReferences.MasterData.BLOCK_COLOR
        );
    }

    @Override
    public BlockColorMD collect(Level level, int minX, int minZ, int maxX, int maxZ) {
        final int[][] colors = new int[16][16];
        final BlockColors blockColors = Minecraft.getInstance().getBlockColors();


        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, minX + x, minZ + z);

                int color = -1;
                while(color == 0 || color == -1) {
                    POS.set(x + minX, y, z + minZ);
                    final BlockState state = level.getBlockState(POS);
                    color = blockColors.getColor(state, level, POS, 0);
                    if(color <= 0) {
                        MaterialColor mapColor = state.getMapColor(level, POS);
                        if(mapColor != MaterialColor.NONE) {
                            color = mapColor.col;
                        }
                    }
                    y--;
                    if(y <= level.getMinBuildHeight()) {
                        break;
                    }
                }
                if(color != 0 && color != -1) {
                    colors[x][z] = color;
                }
            }
        }
        return new BlockColorMD(colors);
    }
}
