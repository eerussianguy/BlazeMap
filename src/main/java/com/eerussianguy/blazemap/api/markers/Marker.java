package com.eerussianguy.blazemap.api.markers;

import java.awt.*;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class Marker<T extends Marker<T>> {
    private final ResourceLocation id;
    private final ResourceKey<Level> dimension;
    private final BlockPos.MutableBlockPos position;
    private String label;
    private ResourceLocation icon;
    private int color;
    private float rotation;

    protected Marker(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, String label, ResourceLocation icon) {
        this(id, dimension, position, label, icon, -1, 0);
    }

    protected Marker(ResourceLocation id, ResourceKey<Level> dimension, BlockPos position, String label, ResourceLocation icon, int color, float rotation) {
        this.id = id;
        this.dimension = dimension;
        this.position = new BlockPos.MutableBlockPos().set(position);
        this.label = label;
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

    public String getLabel() {
        return label;
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

    @SuppressWarnings("unchecked")
    public T setPosition(BlockPos position) {
        this.position.set(position);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setLabel(String label) {
        this.label = label;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setIcon(ResourceLocation icon) {
        this.icon = icon;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setColor(int color) {
        this.color = color;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setRotation(float rotation) {
        this.rotation = rotation;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T randomizeColor() {
        float hue = ((float) System.nanoTime() % 360) / 360F;
        this.color = Color.HSBtoRGB(hue, 1, 1);
        return (T) this;
    }
}
