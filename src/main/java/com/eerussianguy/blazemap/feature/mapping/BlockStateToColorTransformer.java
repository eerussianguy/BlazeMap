package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.BlockColorMD;
import com.eerussianguy.blazemap.api.builtin.BlockStateMD;
import com.eerussianguy.blazemap.api.pipeline.Transformer;
import com.eerussianguy.blazemap.api.util.IDataSource;

public class BlockStateToColorTransformer extends Transformer<BlockColorMD> {
    public BlockStateToColorTransformer() {
        super(
            BlazeMapReferences.Transformers.BLOCK_STATE_TO_COLOR,
            BlazeMapReferences.MasterData.BLOCK_COLOR,
            BlazeMapReferences.MasterData.BLOCK_STATE
        );
    }

    @Override
    public BlockColorMD transform(IDataSource data) {
        BlockStateMD md = (BlockStateMD) data.get(BlazeMapReferences.MasterData.BLOCK_STATE);
        Minecraft mc = Minecraft.getInstance();
        BlockColors blockColors = mc.getBlockColors();
        int[][] colors = new int[16][16];

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                colors[x][z] = blockColors.getColor(md.states[x][z], mc.level, null);
            }
        }

        return new BlockColorMD(null);
    }
}
