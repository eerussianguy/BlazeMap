package com.eerussianguy.blazemap.api.waypoint;

import java.awt.*;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapReferences;

public class Waypoint {
    private final ResourceLocation id;
    private final ResourceKey<Level> dimension;
    private final BlockPos.MutableBlockPos position;
    private String name;
    private ResourceLocation icon;
    private int color;
    private float rotation;

    public Waypoint(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, String name) {
        this(id, dimension, position, name, BlazeMapReferences.Icons.WAYPOINT, -1, 0);
    }

    public Waypoint(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, String name, ResourceLocation icon) {
        this(id, dimension, position, name, icon, -1, 0);
    }

    public Waypoint(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, String name, ResourceLocation icon, int color, float rotation) {
        this.id = id;
        this.dimension = dimension;
        this.position = new BlockPos.MutableBlockPos().set(position);
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.rotation = rotation;
    }

    public final ResourceLocation getID() {
        return id;
    }

    public final ResourceKey<Level> getDimension() {
        return dimension;
    }

    public final BlockPos getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public float getRotation() {
        return rotation;
    }

    public Waypoint setPosition(BlockPos position) {
        this.position.set(position);
        return this;
    }

    public Waypoint setName(String name) {
        this.name = name;
        return this;
    }

    public Waypoint setIcon(ResourceLocation icon) {
        this.icon = icon;
        return this;
    }

    public Waypoint setColor(int color) {
        this.color = color;
        return this;
    }

    public Waypoint setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public Waypoint randomizeColor() {
        float hue = ((float) System.nanoTime() % 360) / 360F;
        this.color = Color.HSBtoRGB(hue, 1, 1);
        return this;
    }
}
