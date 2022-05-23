package com.eerussianguy.blazemap.feature;

import net.minecraftforge.common.MinecraftForge;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.feature.mapping.*;
import com.eerussianguy.blazemap.feature.maps.MinimapRenderer;
import com.eerussianguy.blazemap.feature.waypoints.WaypointManager;

public class BlazeMapFeatures {
    public static void initMapping() {
        BlazeMapAPI.COLLECTORS.register(new TerrainHeightCollector());
        BlazeMapAPI.COLLECTORS.register(new WaterLevelCollector());
        BlazeMapAPI.COLLECTORS.register(new AerialViewCollector());

        BlazeMapAPI.LAYERS.register(new TerrainHeightLayer());
        BlazeMapAPI.LAYERS.register(new WaterLevelLayer());
        BlazeMapAPI.LAYERS.register(new TerrainIsolinesLayer());
        BlazeMapAPI.LAYERS.register(new BlockColorLayer());

        BlazeMapAPI.MAPTYPES.register(new TopographyMapType());
        BlazeMapAPI.MAPTYPES.register(new AerialViewMapType());
    }

    public static void initMiniMap() {
        MinimapRenderer.INSTANCE.setMapType(BlazeMapAPI.MAPTYPES.get(BlazeMapReferences.MapTypes.TOPOGRAPHY));
    }

    public static void initFullMap() {

    }

    public static void initWaypoints() {
        MinecraftForge.EVENT_BUS.register(WaypointManager.class);
    }
}
