package com.eerussianguy.blazemap.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateDataReader extends DataReader
{
    private final BlockState[] states;

    public BlockStateDataReader(BlockState[] states)
    {
        this.states = states;
    }

    @Override
    public int colorFor(int x, int z)
    {
        return states[x + 16 * z].getMaterial().getColor().col;
    }

    @Override
    public CompoundTag serialize()
    {
        final CompoundTag tag = new CompoundTag();
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                final int idx = x + 16 * z;
                tag.put("state" + idx, NbtUtils.writeBlockState(states[idx]));
            }
        }
        return tag;
    }

    @Override
    public BlockStateDataReader deserialize(CompoundTag tag)
    {
        final BlockState[] states = new BlockState[16 * 16];
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                final int idx = x + 16 * z;
                states[idx] = NbtUtils.readBlockState(tag.getCompound("state" + idx));
            }
        }
        return new BlockStateDataReader(states);
    }
}
