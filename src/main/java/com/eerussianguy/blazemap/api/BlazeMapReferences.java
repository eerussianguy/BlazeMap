package com.eerussianguy.blazemap.api;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.api.pipeline.Collector;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.Layer;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;

import static com.eerussianguy.blazemap.api.BlazeMapAPI.*;
import static com.eerussianguy.blazemap.api.BlazeRegistry.Key;

public class BlazeMapReferences {
    public static final String MODID = "blazemap";

    public static class MasterData {
        public static final Key<DataType<MasterDatum>> BLOCK_COLOR = new Key<>(MASTER_DATA, MODID, "block_color");
        public static final Key<DataType<MasterDatum>> NETHER = new Key<>(MASTER_DATA, MODID, "nether");
        public static final Key<DataType<MasterDatum>> TERRAIN_HEIGHT = new Key<>(MASTER_DATA, MODID, "terrain_height");
        public static final Key<DataType<MasterDatum>> WATER_LEVEL = new Key<>(MASTER_DATA, MODID, "water_level");
    }

    public static class Collectors {
        public static final Key<Collector<MasterDatum>> WATER_LEVEL = new Key<>(COLLECTORS, MODID, "water_level");
        public static final Key<Collector<MasterDatum>> TERRAIN_HEIGHT = new Key<>(COLLECTORS, MODID, "terrain_height");
        public static final Key<Collector<MasterDatum>> BLOCK_COLOR = new Key<>(COLLECTORS, MODID, "block_color");
        public static final Key<Collector<MasterDatum>> NETHER = new Key<>(COLLECTORS, MODID, "nether");
    }

    public static class Transformers {}

    public static class Processors {}

    public static class Layers {
        public static final Key<Layer> WATER_LEVEL = new Key<>(LAYERS, MODID, "water_level");
        public static final Key<Layer> TERRAIN_HEIGHT = new Key<>(LAYERS, MODID, "terrain_height");
        public static final Key<Layer> TERRAIN_ISOLINES = new Key<>(LAYERS, MODID, "terrain_isolines");
        public static final Key<Layer> BLOCK_COLOR = new Key<>(LAYERS, MODID, "block_color");
        public static final Key<Layer> NETHER = new Key<>(LAYERS, MODID, "nether");
    }

    public static class MapTypes {
        public static final Key<MapType> AERIAL_VIEW = new Key<>(MAPTYPES, MODID, "aerial_view");
        public static final Key<MapType> TOPOGRAPHY = new Key<>(MAPTYPES, MODID, "topography");
        public static final Key<MapType> NETHER = new Key<>(MAPTYPES, MODID, "nether");
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
