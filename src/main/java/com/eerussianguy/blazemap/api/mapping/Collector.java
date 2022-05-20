package com.eerussianguy.blazemap.api.mapping;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;


public abstract class Collector<T extends MasterData> {
    private final ResourceLocation id;

    public Collector(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getID() {
        return id;
    }

    public abstract T collect(Level level, BlockPos.MutableBlockPos mutable, int minX, int minZ, int maxX, int maxZ);
}
