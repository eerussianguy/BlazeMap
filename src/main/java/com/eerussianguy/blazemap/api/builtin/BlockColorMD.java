package com.eerussianguy.blazemap.api.builtin;

import net.minecraft.nbt.CompoundTag;

import com.eerussianguy.blazemap.api.pipeline.MasterDatum;

public record BlockColorMD(int[][] colors) implements MasterDatum {

    @Override
    public CompoundTag serialize() {
        return null;
    }

    @Override
    public MasterDatum deserialize(CompoundTag nbt) {
        return null;
    }
}
