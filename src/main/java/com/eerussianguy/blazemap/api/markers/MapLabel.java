package com.eerussianguy.blazemap.api.markers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.pipeline.Layer;

public class MapLabel extends Marker<MapLabel> {
    private final Key<Layer> layerID;
    private int width, height;
    private boolean usesZoom;

    public MapLabel(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, Key<Layer> layerID, String name) {
        this(id, dimension, position, layerID, name, BlazeMapReferences.Icons.WAYPOINT, 32, 32, -1, 0, true);
    }

    public MapLabel(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, Key<Layer> layerID, String name, ResourceLocation icon, int width, int height) {
        this(id, dimension, position, layerID, name, icon, width, height, -1, 0, true);
    }

    public MapLabel(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, Key<Layer> layerID, String name, ResourceLocation icon, int width, int height, int color, float rotation, boolean usesZoom) {
        super(id, dimension, position, name, icon, color, rotation);
        this.layerID = layerID;
        this.width = width;
        this.height = height;
        this.usesZoom = usesZoom;
    }

    public final Key<Layer> getLayerID() {
        return layerID;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public MapLabel setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public boolean getUsesZoom() {
        return usesZoom;
    }

    public MapLabel setUsesZoom(boolean usesZoom) {
        this.usesZoom = usesZoom;
        return this;
    }
}
