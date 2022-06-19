package com.eerussianguy.blazemap.feature;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.feature.mapping.*;
import com.eerussianguy.blazemap.feature.maps.MinimapRenderer;
import com.eerussianguy.blazemap.feature.maps.WorldMapGui;
import com.eerussianguy.blazemap.feature.waypoints.WaypointManager;
import com.mojang.blaze3d.platform.InputConstants;

public class BlazeMapFeatures {
    public static final KeyMapping OPEN_FULL_MAP = new KeyMapping("blazemap.key.open_full_map", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, BlazeMap.MOD_NAME);

    public static void initMapping() {
        BlazeMapAPI.COLLECTORS.register(new TerrainHeightCollector());
        BlazeMapAPI.COLLECTORS.register(new WaterLevelCollector());
        BlazeMapAPI.COLLECTORS.register(new AerialViewCollector());
        BlazeMapAPI.COLLECTORS.register(new NetherCollector());

        BlazeMapAPI.LAYERS.register(new TerrainHeightLayer());
        BlazeMapAPI.LAYERS.register(new WaterLevelLayer());
        BlazeMapAPI.LAYERS.register(new TerrainIsolinesLayer());
        BlazeMapAPI.LAYERS.register(new BlockColorLayer());
        BlazeMapAPI.LAYERS.register(new NetherLayer());

        BlazeMapAPI.MAPTYPES.register(new TopographyMapType());
        BlazeMapAPI.MAPTYPES.register(new AerialViewMapType());
        BlazeMapAPI.MAPTYPES.register(new NetherMapType());
    }

    public static void initMiniMap() {
        MinimapRenderer.INSTANCE.setMapType(BlazeMapAPI.MAPTYPES.get(BlazeMapReferences.MapTypes.TOPOGRAPHY));
    }

    public static void initFullMap() {
        ClientRegistry.registerKeyBinding(OPEN_FULL_MAP);

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(WorldMapGui::onDimensionChange);
        bus.addListener((InputEvent.KeyInputEvent evt) -> {
            if(OPEN_FULL_MAP.isDown()) {
                WorldMapGui.open();
            }
        });
    }

    public static void initWaypoints() {
        MinecraftForge.EVENT_BUS.register(WaypointManager.class);
    }
}
