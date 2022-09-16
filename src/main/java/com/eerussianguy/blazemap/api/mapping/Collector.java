package com.eerussianguy.blazemap.api.mapping;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.eerussianguy.blazemap.api.BlazeRegistry;

/**
 * Collectors collect MasterData from chunks that need updating to be processed later.
 * This operation is executed synchronously in the main game thread.
 *
 * MasterData is consumed by Layers and Processors asynchronously in the data crunching threads.
 *
 * @author LordFokas
 */
public abstract class Collector<T extends MasterDatum> implements BlazeRegistry.RegistryEntry {
    protected static final BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();
    protected final BlazeRegistry.Key<Collector<MasterDatum>> id;

    public Collector(BlazeRegistry.Key<Collector<MasterDatum>> id) {
        this.id = id;
    }

    public BlazeRegistry.Key<Collector<MasterDatum>> getID() {
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
}
