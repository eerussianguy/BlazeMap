package com.eerussianguy.blazemap.core;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.core.mapping.TerrainHeightCollector;
import com.eerussianguy.blazemap.core.mapping.TerrainHeightLayer;
import com.eerussianguy.blazemap.core.mapping.TopographyMapType;

public class BlazeMapCore {
    /*
    public static void init(){
        // We initialize the mapping components through our own API because it is so modular
        // and flexible that we can build these features on top of it.
        // Also, this way if we break the API we'll notice immediately.

        // Master Data Collectors
        BlazeMapAPI.COLLECTORS.set(BlazeMapReferences.MD_WATER_LEVEL, null);
        BlazeMapAPI.COLLECTORS.set(BlazeMapReferences.MD_TERRAIN_HEIGHT, new HeightmapCollector(null));
        BlazeMapAPI.COLLECTORS.set(BlazeMapReferences.MD_BLOCK_COLOR, null);

        // Map Layers
        BlazeMapAPI.LAYERS.set(BlazeMapReferences.LAYER_WATER_LEVEL, null);
        BlazeMapAPI.LAYERS.set(BlazeMapReferences.LAYER_TERRAIN_HEIGHT, null);
        BlazeMapAPI.LAYERS.set(BlazeMapReferences.LAYER_BLOCK_COLOR, new DaytimeLayer(null));

        // Map Types
        BlazeMapAPI.MAPTYPES.set(BlazeMapReferences.MAP_AERIAL_VIEW, null);
        BlazeMapAPI.MAPTYPES.set(BlazeMapReferences.MAP_TOPOGRAPHY, null);
    }
    */

    // TODO: remove debug
    public static void init(){
        BlazeMapAPI.COLLECTORS.set(BlazeMapReferences.MD_TERRAIN_HEIGHT, new TerrainHeightCollector());
        BlazeMapAPI.LAYERS.set(BlazeMapReferences.LAYER_TERRAIN_HEIGHT, new TerrainHeightLayer());
        BlazeMapAPI.MAPTYPES.set(BlazeMapReferences.MAP_TOPOGRAPHY, new TopographyMapType());
    }
}
