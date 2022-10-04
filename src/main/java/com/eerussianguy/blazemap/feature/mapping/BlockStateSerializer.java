package com.eerussianguy.blazemap.feature.mapping;

import java.io.IOException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.builtin.BlockStateMD;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.util.MinecraftStreams;

public class BlockStateSerializer implements DataType<BlockStateMD> {
    @Override
    public BlazeRegistry.Key<?> getID() {
        return BlazeMapReferences.MasterData.BLOCK_STATE;
    }

    @Override
    public void serialize(MinecraftStreams.Output stream, BlockStateMD datum) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockStateMD deserialize(MinecraftStreams.Input stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void serialize(FriendlyByteBuf buffer, BlockStateMD datum) {
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {

            }
        }
    }

    @Override
    public BlockStateMD deserialize(FriendlyByteBuf buffer) {
        BlockState[][] states = new BlockState[16][16];

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {

            }
        }

        return new BlockStateMD(states);
    }
}
