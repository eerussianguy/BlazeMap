package com.eerussianguy.blazemap.engine;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.mapping.MapType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class CartographyPipeline {
    private final MasterDataCache mdCache = new MasterDataCache();

    public CartographyPipeline(ResourceKey<Level> world){
        for(ResourceLocation key : BlazeMapAPI.MAPTYPES.keys()){
            MapType map = BlazeMapAPI.MAPTYPES.get(key);

        }
    }

    public void processChunk(ChunkPos pos){

    }
}
