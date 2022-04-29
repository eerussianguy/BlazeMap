package com.eerussianguy.blazemap.core.mapping;

import com.eerussianguy.blazemap.api.mapping.Collector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapCollector extends Collector<BlockStateMD>
{
    private final Heightmap.Types type;

    public HeightmapCollector(Heightmap.Types type)
    {
        this.type = type;
    }

    @Override
    public BlockStateMD collect(Level level, ChunkAccess chunk, int minX, int minZ, int maxX, int maxZ)
    {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockState[] states = new BlockState[16 * 16];
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                mutable.set(minX + x, chunk.getHeight(type, x, z) - 1, minZ + z);
                states[x + 16 * z] = level.getBlockState(mutable);
            }
        }
        return new BlockStateMD(states);
    }
}
