package com.eerussianguy.blazemap.api;

import net.minecraft.resources.ResourceLocation;

public class BlazeMapReferences
{
    public static final String MODID = "blazemap";

    // Map Types
    public static final ResourceLocation MAP_AERIAL_VIEW = new ResourceLocation(MODID, "map_aerial_view");
    public static final ResourceLocation MAP_TOPOGRAPHY = new ResourceLocation(MODID, "map_topography");

    // Map Layers
    public static final ResourceLocation LAYER_WATER_LEVEL = new ResourceLocation(MODID, "layer_water_level");
    public static final ResourceLocation LAYER_TERRAIN_HEIGHT = new ResourceLocation(MODID, "layer_terrain_height");
    public static final ResourceLocation LAYER_BLOCK_COLOR = new ResourceLocation(MODID, "layer_block_color");

    // Master Data Collectors
    public static final ResourceLocation MD_WATER_LEVEL = new ResourceLocation(MODID, "md_water_level");
    public static final ResourceLocation MD_TERRAIN_HEIGHT = new ResourceLocation(MODID, "md_terrain_height");
    public static final ResourceLocation MD_BLOCK_COLOR = new ResourceLocation(MODID, "md_block_color");
}
