package com.eerussianguy.blazemap.api.mapping;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.util.IMapView;

public abstract class Layer {
    private final ResourceLocation id;
    private final Set<ResourceLocation> collectors;

    public Layer(ResourceLocation id, ResourceLocation... collectors) {
        this.id = id;
        this.collectors = Arrays.stream(collectors).collect(Collectors.toUnmodifiableSet());
    }

    public ResourceLocation getID() {
        return id;
    }

    public Set<ResourceLocation> getCollectors() {
        return collectors;
    }


    public boolean shouldRenderForWorld(ResourceKey<Level> world) {
        return true;
    }

    public abstract boolean renderTile(BufferedImage tile, IMapView<ResourceLocation, MasterData> data);
}
