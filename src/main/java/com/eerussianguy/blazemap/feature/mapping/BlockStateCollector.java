package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.BlockStateMD;
import com.eerussianguy.blazemap.api.pipeline.Collector;

public class BlockStateCollector extends Collector<BlockStateMD> {
    public BlockStateCollector() {
        super(
            BlazeMapReferences.Collectors.BLOCK_STATE,
            BlazeMapReferences.MasterData.BLOCK_STATE
        );
    }

    @Override
    public BlockStateMD collect(Level level, int minX, int minZ, int maxX, int maxZ) {
        return null;
    }
}
