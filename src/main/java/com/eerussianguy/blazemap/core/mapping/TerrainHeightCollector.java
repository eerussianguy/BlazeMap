package com.eerussianguy.blazemap.core.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.mapping.Collector;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

public class TerrainHeightCollector extends Collector<TerrainHeightMD> {

    public TerrainHeightCollector() {
        super(BlazeMapReferences.MD_TERRAIN_HEIGHT);
    }

    @Override
    public TerrainHeightMD collect(Level level, ChunkAccess chunk, int minX, int minZ, int maxX, int maxZ) {
        TerrainHeightMD md = new TerrainHeightMD();

        return md;
    }
}
