package com.eerussianguy.blazemap.feature;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.feature.mapping.*;

public class BlazeMapFeaturesCommon {
    public static void initMapping() {
        BlazeMapAPI.MASTER_DATA.register(new TerrainHeightSerializer(BlazeMapReferences.MasterData.TERRAIN_HEIGHT));
        BlazeMapAPI.MASTER_DATA.register(new WaterLevelSerializer());
//        BlazeMapAPI.MASTER_DATA.register(new BlockStateSerializer());
//        BlazeMapAPI.MASTER_DATA.register(new BlockColorSerializer());
        BlazeMapAPI.MASTER_DATA.register(new TerrainHeightSerializer(BlazeMapReferences.MasterData.NETHER));

        BlazeMapAPI.COLLECTORS.register(new TerrainHeightCollector());
        BlazeMapAPI.COLLECTORS.register(new WaterLevelCollector());
//        BlazeMapAPI.COLLECTORS.register(new BlockStateCollector());
//        BlazeMapAPI.COLLECTORS.register(new BlockColorCollector());
        BlazeMapAPI.COLLECTORS.register(new NetherCollector());

//        BlazeMapAPI.TRANSFORMERS.register(new BlockStateToColorTransformer());
    }
}
