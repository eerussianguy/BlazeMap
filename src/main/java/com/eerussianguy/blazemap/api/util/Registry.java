package com.eerussianguy.blazemap.api.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public class Registry<T> {
    private final Map<ResourceLocation, T> objects;

    public Registry(Map<ResourceLocation, T> objects) {
        this.objects = objects;
    }

    public Registry() {
        this(new HashMap<>());
    }

    public boolean exists(ResourceLocation key) {
        return objects.containsKey(key);
    }

    public T get(ResourceLocation key) {
        return objects.get(key);
    }

    public void set(ResourceLocation key, T value) {
        if(objects.containsKey(key)) throw new IllegalArgumentException("Key " + key.toString() + " is already set!");
        objects.put(key, value);
    }

    public void replace(ResourceLocation key, T value) {
        if(!objects.containsKey(key)) throw new IllegalArgumentException("Key " + key.toString() + " is not set!");
        objects.put(key, value);
    }

    public Set<ResourceLocation> keys() {
        return objects.keySet();
    }
}
