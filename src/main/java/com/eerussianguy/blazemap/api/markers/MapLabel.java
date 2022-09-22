package com.eerussianguy.blazemap.api.markers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.mapping.Layer;

public class MapLabel extends Marker<MapLabel> {
    private final Key<Layer> layerID;

    public MapLabel(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, Key<Layer> layerID, String name) {
        this(id, dimension, position, layerID, name, BlazeMapReferences.Icons.WAYPOINT, -1, 0);
    }

    public MapLabel(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, Key<Layer> layerID, String name, ResourceLocation icon) {
        this(id, dimension, position, layerID, name, icon, -1, 0);
    }

    public MapLabel(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, Key<Layer> layerID, String name, ResourceLocation icon, int color, float rotation) {
        super(id, dimension, position, name, icon, color, rotation);
        this.layerID = layerID;
    }

    public final Key<Layer> getLayerID(){
        return layerID;
    }
}
