package com.eerussianguy.blazemap.api.builtin;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;

public class BlockColorMD extends MasterDatum {
    public final int[][] colors;

    public BlockColorMD(int[][] colors) {
        this.colors = colors;
    }

    @Override
    public BlazeRegistry.Key<DataType<MasterDatum>> getID() {
        return BlazeMapReferences.MasterData.BLOCK_COLOR;
    }

    @Override
    public boolean equals(Object other) {
        return false;
    }
}
