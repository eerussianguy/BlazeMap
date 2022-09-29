package com.eerussianguy.blazemap.api;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.api.mapping.Collector;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.mapping.MasterDatum;

public class BlazeMapReferences {
    public static final String MODID = "blazemap";

    public static class MapTypes {
        public static final BlazeRegistry.Key<MapType> AERIAL_VIEW = new BlazeRegistry.Key<>(BlazeMapAPI.MAPTYPES, MODID, "aerial_view");
        public static final BlazeRegistry.Key<MapType> TOPOGRAPHY = new BlazeRegistry.Key<>(BlazeMapAPI.MAPTYPES, MODID, "topography");
        public static final BlazeRegistry.Key<MapType> NETHER = new BlazeRegistry.Key<>(BlazeMapAPI.MAPTYPES, MODID, "nether");
    }

    public static class Layers {
        public static final BlazeRegistry.Key<Layer> WATER_LEVEL = new BlazeRegistry.Key<>(BlazeMapAPI.LAYERS, MODID, "water_level");
        public static final BlazeRegistry.Key<Layer> TERRAIN_HEIGHT = new BlazeRegistry.Key<>(BlazeMapAPI.LAYERS, MODID, "terrain_height");
        public static final BlazeRegistry.Key<Layer> TERRAIN_ISOLINES = new BlazeRegistry.Key<>(BlazeMapAPI.LAYERS, MODID, "terrain_isolines");
        public static final BlazeRegistry.Key<Layer> BLOCK_COLOR = new BlazeRegistry.Key<>(BlazeMapAPI.LAYERS, MODID, "block_color");
        public static final BlazeRegistry.Key<Layer> NETHER = new BlazeRegistry.Key<>(BlazeMapAPI.LAYERS, MODID, "nether");
    }

    public static class Collectors {
        public static final BlazeRegistry.Key<Collector<MasterDatum>> WATER_LEVEL = new BlazeRegistry.Key<>(BlazeMapAPI.COLLECTORS, MODID, "water_level");
        public static final BlazeRegistry.Key<Collector<MasterDatum>> TERRAIN_HEIGHT = new BlazeRegistry.Key<>(BlazeMapAPI.COLLECTORS, MODID, "terrain_height");
        public static final BlazeRegistry.Key<Collector<MasterDatum>> BLOCK_COLOR = new BlazeRegistry.Key<>(BlazeMapAPI.COLLECTORS, MODID, "block_color");
        public static final BlazeRegistry.Key<Collector<MasterDatum>> NETHER = new BlazeRegistry.Key<>(BlazeMapAPI.COLLECTORS, MODID, "nether");
    }

    public static class Icons {
        public static final List<ResourceLocation> ALL_WAYPOINTS;
        public static final ResourceLocation WAYPOINT = new ResourceLocation(MODID, "textures/waypoints/waypoint.png");
        public static final ResourceLocation HOUSE = new ResourceLocation(MODID, "textures/waypoints/house.png");
        public static final ResourceLocation CAVE = new ResourceLocation(MODID, "textures/waypoints/cave.png");
        public static final ResourceLocation INGOT = new ResourceLocation(MODID, "textures/waypoints/ingot.png");
        public static final ResourceLocation SWORD = new ResourceLocation(MODID, "textures/waypoints/sword.png");
        public static final ResourceLocation AXE = new ResourceLocation(MODID, "textures/waypoints/axe.png");
        public static final ResourceLocation PICKAXE = new ResourceLocation(MODID, "textures/waypoints/pickaxe.png");
        public static final ResourceLocation SHOVEL = new ResourceLocation(MODID, "textures/waypoints/shovel.png");
        public static final ResourceLocation HOE = new ResourceLocation(MODID, "textures/waypoints/hoe.png");

        static {
            ALL_WAYPOINTS = List.of(WAYPOINT, HOUSE, CAVE, INGOT, SWORD, AXE, PICKAXE, SHOVEL, HOE);
        }
    }
}
