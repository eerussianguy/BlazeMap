package com.eerussianguy.blazemap.api.builtin;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;

public class WaterLevelMD extends MasterDatum {
    public final int sea, minX, minZ;
    public final int[][] level;

    public WaterLevelMD(int sea, int minX, int minZ, int[][] level) {
        this.sea = sea;
        this.minX = minX;
        this.minZ = minZ;

        this.level = level;
    }

    @Override
    public BlazeRegistry.Key<DataType<MasterDatum>> getID() {
        return BlazeMapReferences.MasterData.WATER_LEVEL;
    }

    @Override
    public boolean equals(Object other) {
        return false;
    }
}
