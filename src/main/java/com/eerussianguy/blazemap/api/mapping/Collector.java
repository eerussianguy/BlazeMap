package com.eerussianguy.blazemap.api.mapping;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import com.eerussianguy.blazemap.api.BlazeRegistry;


public abstract class Collector<T extends MasterDatum> implements BlazeRegistry.Registerable<Collector<T>> {
    protected static final BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();
    private final BlazeRegistry.Key<Collector<T>> id;

    @SuppressWarnings("unchecked")
    public Collector(BlazeRegistry.Key<? extends Collector<T>> id) {
        this.id = (BlazeRegistry.Key<Collector<T>>) id;
    }

    public BlazeRegistry.Key<Collector<T>> getID() {
        return id;
    }

    public abstract T collect(Level level, BlockPos.MutableBlockPos mutable, int minX, int minZ, int maxX, int maxZ);

    protected static boolean isWater(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(POS.set(x, y, z));
        return state.getFluidState().getTags().anyMatch(t -> t.equals(FluidTags.WATER));
    }

    protected static boolean isLeaves(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(POS.set(x, y, z));
        return state.is(BlockTags.LEAVES) || state.isAir() || state.getMaterial() == Material.VEGETABLE;
    }
}
