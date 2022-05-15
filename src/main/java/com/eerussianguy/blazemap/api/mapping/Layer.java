package com.eerussianguy.blazemap.api.mapping;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.util.IMapView;
import com.mojang.blaze3d.platform.NativeImage;

public abstract class Layer
{
    private final ResourceLocation id;
    private final Set<ResourceLocation> collectors;

    public Layer(ResourceLocation id, ResourceLocation... collectors)
    {
        this.id = id;
        this.collectors = Arrays.stream(collectors).collect(Collectors.toUnmodifiableSet());
    }

    public ResourceLocation getID()
    {
        return id;
    }

    public Set<ResourceLocation> getCollectors()
    {
        return collectors;
    }


    public boolean shouldRenderInDimension(ResourceKey<Level> dimension)
    {
        return true;
    }

    public abstract boolean renderTile(NativeImage tile, IMapView<ResourceLocation, MasterData> data);
}
