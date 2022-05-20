package com.eerussianguy.blazemap.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class RegionPos {
    public static final RegionPos ORIGIN = new RegionPos(BlockPos.ZERO);

    public final int x;
    public final int z;

    public RegionPos(BlockPos pos) {
        this(pos.getX() >> 9, pos.getZ() >> 9);
    }

    public RegionPos(ChunkPos pos) {
        this(pos.x >> 5, pos.z >> 5);
    }

    public RegionPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public RegionPos offset(int x, int z) {
        if(x == 0 && z == 0) return this;
        return new RegionPos(this.x + x, this.z + z);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        RegionPos regionPos = (RegionPos) o;
        return x == regionPos.x && z == regionPos.z;
    }

    @Override
    public int hashCode() {
        return x * 65536 + z;
    }

    @Override
    public String toString() {
        return "[" + x + "," + z + ']';
    }
}
