package com.eerussianguy.blazemap.api.pipeline;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.BlazeRegistry.RegistryEntry;

/**
 * Collectors collect MasterData from chunks that need updating to be processed later.
 * This operation is executed synchronously in the main game thread.
 *
 * MasterData is consumed by Layers and Processors asynchronously in the data crunching threads.
 *
 * @author LordFokas
 */
public abstract class Collector<T extends MasterDatum> implements RegistryEntry, Producer {
    protected static final BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();
    protected final Key<Collector<MasterDatum>> id;
    protected final Key<DataType<MasterDatum>> output;

    public Collector(Key<Collector<MasterDatum>> id, Key<DataType<MasterDatum>> output) {
        this.id = id;
        this.output = output;
    }

    public Key<Collector<MasterDatum>> getID() {
        return id;
    }

    public abstract T collect(Level level, int minX, int minZ, int maxX, int maxZ);

    protected static boolean isWater(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(POS.set(x, y, z));
        return state.getFluidState().is(FluidTags.WATER);
    }

    protected static boolean isLeaves(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(POS.set(x, y, z));
        return state.is(BlockTags.LEAVES) || state.isAir() || state.getMaterial().isReplaceable();
    }

    @Override
    public Key<DataType<MasterDatum>> getOutputID() {
        return output;
    }
}
