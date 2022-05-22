package com.eerussianguy.blazemap.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public class BlazeRegistry<T> {
    private final Map<Key<T>, Registerable> objects;

    public BlazeRegistry() {
        this.objects = new HashMap<>();
    }

    public boolean exists(Key<? extends T> key) {
        return objects.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public T get(Key<T> key) {
        return (T) objects.get(key);
    }

    @SuppressWarnings("unchecked")
    public void register(Registerable object) {
        Key<T> key = (Key<T>) object.getID();
        if(objects.containsKey(key)) throw new IllegalArgumentException("Key " + key.toString() + " is already set!");
        objects.put(key, object);
    }

    @SuppressWarnings("unchecked")
    public void replace(Registerable object) {
        Key<T> key = (Key<T>) object.getID();
        if(!objects.containsKey(key)) throw new IllegalArgumentException("Key " + key.toString() + " is not set!");
        objects.put(key, object);
    }

    public Set<Key<T>> keys() {
        return objects.keySet();
    }

    public interface Registerable {
        Key<?> getID();
    }

    public static class Key<T> extends ResourceLocation {

        private final BlazeRegistry<T> registry;

        public Key(BlazeRegistry<T> registry, String path) {
            super(path);
            Objects.requireNonNull(registry);
            this.registry = registry;
        }

        public Key(BlazeRegistry<T> registry, String namespace, String resource) {
            super(namespace, resource);
            Objects.requireNonNull(registry);
            this.registry = registry;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof Key k) {
                if(registry != k.registry) return false;
                return super.equals(k);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), registry);
        }
    }
}
