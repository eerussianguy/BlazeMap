package com.eerussianguy.blazemap.api.util;

import net.minecraft.world.level.ChunkPos;

public class RegionPos {
    public final int x;
    public final int z;

    public RegionPos(ChunkPos pos) {
        this.x = pos.getRegionX();
        this.z = pos.getRegionZ();
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
