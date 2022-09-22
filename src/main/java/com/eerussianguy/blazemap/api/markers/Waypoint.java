package com.eerussianguy.blazemap.api.markers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapReferences;

public class Waypoint extends Marker<Waypoint>{
    public Waypoint(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, String name) {
        this(id, dimension, position, name, BlazeMapReferences.Icons.WAYPOINT, -1, 0);
    }

    public Waypoint(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, String name, ResourceLocation icon) {
        this(id, dimension, position, name, icon, -1, 0);
    }

    public Waypoint(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, String name, ResourceLocation icon, int color) {
        this(id, dimension, position, name, icon, color, 0);
    }

    public Waypoint(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, String name, ResourceLocation icon, int color, float rotation) {
        super(id, dimension, position, name, icon, color, rotation);
    }
}
