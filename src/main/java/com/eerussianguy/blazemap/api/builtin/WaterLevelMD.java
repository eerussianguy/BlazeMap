package com.eerussianguy.blazemap.api.builtin;

import net.minecraft.nbt.CompoundTag;

import com.eerussianguy.blazemap.api.pipeline.MasterDatum;

public class WaterLevelMD implements MasterDatum {
    public final int sea, minX, minZ;
    public final int[][] level;

    public WaterLevelMD(int sea, int minX, int minZ, int[][] level) {
        this.sea = sea;
        this.minX = minX;
        this.minZ = minZ;

        this.level = level;
    }

    @Override
    public CompoundTag serialize() {
        return null;
    }

    @Override
    public MasterDatum deserialize(CompoundTag nbt) {
        return null;
    }
}
