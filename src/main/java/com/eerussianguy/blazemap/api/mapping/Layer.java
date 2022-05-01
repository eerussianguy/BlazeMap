package com.eerussianguy.blazemap.api.mapping;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Layer<T extends MasterData>
{
    private final ResourceLocation id;
    private final List<Collector<?>> collectors;

    public Layer(ResourceLocation id, Collector<?>... collectors)
    {
        this.id = id;
        this.collectors = Arrays.stream(collectors).collect(Collectors.toList());
    }

    public ResourceLocation getID()
    {
        return id;
    }

    public List<Collector<?>> getCollectors()
    {
        return collectors;
    }



    public boolean shouldRenderForWorld(ResourceKey<Level> world){
        return true;
    }
}
