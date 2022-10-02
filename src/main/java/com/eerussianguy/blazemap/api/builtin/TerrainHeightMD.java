package com.eerussianguy.blazemap.api.builtin;

import net.minecraft.nbt.CompoundTag;

import com.eerussianguy.blazemap.api.pipeline.MasterDatum;

public class TerrainHeightMD implements MasterDatum {
    public final int minY, maxY, height, sea, minX, minZ;
    public final int[][] heightmap;

    public TerrainHeightMD(int minY, int maxY, int height, int sea, int minX, int minZ, int[][] heightmap) {
        this.minY = minY;
        this.maxY = maxY;
        this.height = height;
        this.sea = sea;

        this.minX = minX;
        this.minZ = minZ;

        this.heightmap = heightmap;
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
