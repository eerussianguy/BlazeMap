package com.eerussianguy.blazemap.feature.mapping;

import java.io.IOException;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.builtin.BlockColorMD;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.util.MinecraftStreams;

public class BlockColorSerializer implements DataType<BlockColorMD> {
    @Override
    public BlazeRegistry.Key<?> getID() {
        return BlazeMapReferences.MasterData.BLOCK_COLOR;
    }

    @Override
    public void serialize(MinecraftStreams.Output stream, BlockColorMD datum) throws IOException {
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                stream.writeInt(datum.colors[x][z]);
            }
        }
    }

    @Override
    public BlockColorMD deserialize(MinecraftStreams.Input stream) throws IOException {
        int[][] colors = new int[16][16];

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                colors[x][z] = stream.readInt();
            }
        }

        return new BlockColorMD(colors);
    }
}
