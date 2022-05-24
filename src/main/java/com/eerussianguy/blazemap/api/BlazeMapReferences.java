package com.eerussianguy.blazemap.api;

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
}
