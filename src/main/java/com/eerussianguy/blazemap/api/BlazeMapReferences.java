package com.eerussianguy.blazemap.api;

import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.builtin.WaterLevelMD;
import com.eerussianguy.blazemap.api.mapping.Collector;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class BlazeMapReferences {
    public static final String MODID = "blazemap";

    public static class MapTypes {
        public static final BlazeRegistry.Key<MapType> AERIAL_VIEW = new BlazeRegistry.Key<>(BlazeMapAPI.MAPTYPES, MODID, "map_aerial_view");
        public static final BlazeRegistry.Key<MapType> TOPOGRAPHY = new BlazeRegistry.Key<>(BlazeMapAPI.MAPTYPES, MODID, "map_topography");
    }

    public static class Layers {
        public static final BlazeRegistry.Key<Layer> WATER_LEVEL = new BlazeRegistry.Key<>(BlazeMapAPI.LAYERS, MODID, "layer_water_level");
        public static final BlazeRegistry.Key<Layer> TERRAIN_HEIGHT = new BlazeRegistry.Key<>(BlazeMapAPI.LAYERS, MODID, "layer_terrain_height");
        public static final BlazeRegistry.Key<Layer> TERRAIN_ISOLINES = new BlazeRegistry.Key<>(BlazeMapAPI.LAYERS, MODID, "layer_terrain_isolines");
        public static final BlazeRegistry.Key<Layer> BLOCK_COLOR = new BlazeRegistry.Key<>(BlazeMapAPI.LAYERS, MODID, "layer_block_color");
    }

    @SuppressWarnings("ALL")
    public static class MasterData {
        public static final BlazeRegistry.Key<Collector<WaterLevelMD>> WATER_LEVEL = new BlazeRegistry.Key(BlazeMapAPI.COLLECTORS, MODID, "md_water_level");
        public static final BlazeRegistry.Key<Collector<TerrainHeightMD>> TERRAIN_HEIGHT = new BlazeRegistry.Key(BlazeMapAPI.COLLECTORS, MODID, "md_terrain_height");
        public static final BlazeRegistry.Key<Collector<?>> BLOCK_COLOR = new BlazeRegistry.Key(BlazeMapAPI.COLLECTORS, MODID, "md_block_color");
    }
}
