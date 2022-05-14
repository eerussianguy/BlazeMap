package com.eerussianguy.blazemap.feature;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.feature.mapping.TerrainHeightCollector;
import com.eerussianguy.blazemap.feature.mapping.TerrainHeightLayer;
import com.eerussianguy.blazemap.feature.mapping.TopographyMapType;

public class BlazeMapFeatures
{
    public static void initMapping()
    {
        BlazeMapAPI.COLLECTORS.set(BlazeMapReferences.MD_TERRAIN_HEIGHT, new TerrainHeightCollector());
        BlazeMapAPI.LAYERS.set(BlazeMapReferences.LAYER_TERRAIN_HEIGHT, new TerrainHeightLayer());
        BlazeMapAPI.MAPTYPES.set(BlazeMapReferences.MAP_TOPOGRAPHY, new TopographyMapType());
    }

    public static void initMiniMap()
    {

    }

    public static void initFullMap()
    {

    }

    public static void initWaypoints()
    {

    }
}
