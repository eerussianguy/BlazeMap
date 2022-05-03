package com.eerussianguy.blazemap.api.mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class MapType
{
    private final Set<ResourceLocation> layers;

    public MapType(ResourceLocation... layers)
    {
        this.layers = Arrays.stream(layers).collect(Collectors.toUnmodifiableSet());
    }

    public MapType(Set<ResourceLocation> layers)
    {
        this.layers = Collections.unmodifiableSet(layers);
    }

    public Set<ResourceLocation> getLayers()
    {
        return layers;
    }

    public boolean shouldRenderForWorld(ResourceKey<Level> world){
        return true;
    }
}
