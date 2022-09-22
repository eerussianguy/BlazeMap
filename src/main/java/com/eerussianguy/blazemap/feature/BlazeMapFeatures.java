package com.eerussianguy.blazemap.feature;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.event.ServerJoinedEvent;
import com.eerussianguy.blazemap.feature.mapping.*;
import com.eerussianguy.blazemap.feature.maps.MapRenderer;
import com.eerussianguy.blazemap.feature.maps.MinimapOptionsGui;
import com.eerussianguy.blazemap.feature.maps.MinimapRenderer;
import com.eerussianguy.blazemap.feature.maps.WorldMapGui;
import com.eerussianguy.blazemap.feature.waypoints.WaypointStore;
import com.mojang.blaze3d.platform.InputConstants;

public class BlazeMapFeatures {
    public static final KeyMapping KEY_MAPS = new KeyMapping("blazemap.key.maps", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, BlazeMap.MOD_NAME);
    public static final KeyMapping KEY_ZOOM = new KeyMapping("blazemap.key.zoom", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, BlazeMap.MOD_NAME);
    public static final KeyMapping KEY_WAYPOINTS = new KeyMapping("blazemap.key.waypoints", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, BlazeMap.MOD_NAME);

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

    public static void initMaps() {
        ClientRegistry.registerKeyBinding(KEY_MAPS);
        ClientRegistry.registerKeyBinding(KEY_ZOOM);
        ClientRegistry.registerKeyBinding(KEY_WAYPOINTS);

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(MapRenderer::onDimensionChange);
        bus.addListener((InputEvent.KeyInputEvent evt) -> {
            if(KEY_MAPS.isDown()) {
                if(Screen.hasShiftDown()) {
                    MinimapOptionsGui.open();
                }
                else {
                    WorldMapGui.open();
                }
            }
            if(KEY_WAYPOINTS.isDown()) {
                if(Screen.hasShiftDown()) {
                    // Open Waypoint Manager
                }
                else {
                    // Open Waypoint Creator
                }
            }
            if(KEY_ZOOM.isDown()) {
                if(Screen.hasShiftDown()) {
                    MinimapRenderer.INSTANCE.synchronizer.zoomOut();
                }
                else {
                    MinimapRenderer.INSTANCE.synchronizer.zoomIn();
                }
            }
        });
    }

    public static void initWaypoints() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(WorldMapGui::onDimensionChanged); // TODO: remove, debug
        bus.addListener(EventPriority.HIGHEST, (ServerJoinedEvent evt) -> evt.setWaypointStorageFactory(WaypointStore::new));
    }
}
