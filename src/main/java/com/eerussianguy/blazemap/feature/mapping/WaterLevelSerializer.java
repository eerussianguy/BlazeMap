package com.eerussianguy.blazemap.feature.mapping;

import java.io.IOException;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.util.MinecraftStreams;

public class WaterLevelSerializer implements DataType<WaterLevelMD> {
    @Override
    public BlazeRegistry.Key<?> getID() {
        return BlazeMapReferences.MasterData.WATER_LEVEL;
    }

    @Override
    public void serialize(MinecraftStreams.Output stream, WaterLevelMD water) throws IOException {
        stream.writeShort(water.sea);

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                stream.writeShort(water.level[x][z]);
            }
        }
    }

    @Override
    public WaterLevelMD deserialize(MinecraftStreams.Input stream) throws IOException {
        short sea = stream.readShort();

        int[][] level = new int[16][16];

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                level[x][z] = stream.readShort();
            }
        }

        return new WaterLevelMD(sea, level);
    }
}
