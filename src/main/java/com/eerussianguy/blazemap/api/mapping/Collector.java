package com.eerussianguy.blazemap.api.mapping;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.eerussianguy.blazemap.api.BlazeRegistry;


public abstract class Collector<T extends MasterDatum> implements BlazeRegistry.Registerable<Collector<T>> {
    private static final BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();

    protected static boolean isWater(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState(POS.setX(x).setY(y).setZ(z));
        return state.getFluidState().getTags().anyMatch(t -> t.equals(FluidTags.WATER));
    }
    private final BlazeRegistry.Key<Collector<T>> id;

    @SuppressWarnings("unchecked")
    public Collector(BlazeRegistry.Key<? extends Collector<T>> id) {
        this.id = (BlazeRegistry.Key<Collector<T>>) id;
    }

    public BlazeRegistry.Key<Collector<T>> getID() {
        return id;
    }

    public abstract T collect(Level level, BlockPos.MutableBlockPos mutable, int minX, int minZ, int maxX, int maxZ);
}
