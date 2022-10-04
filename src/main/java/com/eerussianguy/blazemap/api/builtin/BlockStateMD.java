package com.eerussianguy.blazemap.api.builtin;

import net.minecraft.world.level.block.state.BlockState;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;

public class BlockStateMD extends MasterDatum {
    public final BlockState[][] states;

    public BlockStateMD(BlockState[][] states) {
        this.states = states;
    }

    @Override
    public BlazeRegistry.Key<DataType<MasterDatum>> getID() {
        return BlazeMapReferences.MasterData.BLOCK_STATE;
    }

    @Override
    public boolean equals(Object other) {
        return false;
    }
}
