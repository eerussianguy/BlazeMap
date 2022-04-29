package com.eerussianguy.blazemap.maptype;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import com.eerussianguy.blazemap.layer.Layer;
import org.jetbrains.annotations.Nullable;

public class MapType
{
    private static final Map<ResourceLocation, MapType> REGISTRY = new HashMap<>();

    public static synchronized MapType register(ResourceLocation id, MapType instance)
    {
        if (REGISTRY.containsKey(id))
        {
            throw new IllegalArgumentException("Duplicate key: " + id);
        }
        REGISTRY.put(id, instance);
        return instance;
    }

    private final List<Layer<?>> layers;

    public MapType(Layer<?>... layers)
    {
        this.layers = Arrays.stream(layers).collect(Collectors.toList());
    }

    public MapType(List<Layer<?>> layers)
    {
        this.layers = layers;
    }

    public List<Layer<?>> getLayers()
    {
        return layers;
    }

    @Cancelable
    public static class CreationEvent extends Event
    {
        private List<Layer<?>> layers;

        public CreationEvent(List<Layer<?>> layers)
        {
            this.layers = layers;
        }

        public void replaceAllLayers(List<Layer<?>> layers)
        {
            this.layers = layers;
        }

        public List<Layer<?>> getLayers()
        {
            return layers;
        }

        @Nullable
        public Layer<?> removeLayer(ResourceLocation id)
        {
            for (Layer<?> layer : layers)
            {
                if (layer.getID().equals(id))
                {
                    return layer;
                }
            }
            return null;
        }
    }
}
