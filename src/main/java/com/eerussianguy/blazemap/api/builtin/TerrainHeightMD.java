package com.eerussianguy.blazemap.api.builtin;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;

public class TerrainHeightMD extends MasterDatum {
    private final BlazeRegistry.Key<DataType<MasterDatum>> id;
    public final int minY, maxY, height, sea;
    public final int[][] heightmap;

    public TerrainHeightMD(BlazeRegistry.Key<DataType<MasterDatum>> id, int minY, int maxY, int height, int sea, int[][] heightmap) {
        this.id = id;
        this.minY = minY;
        this.maxY = maxY;
        this.height = height;
        this.sea = sea;

        this.heightmap = heightmap;
    }

    @Override
    public BlazeRegistry.Key<DataType<MasterDatum>> getID() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        return false;
    }
}
