package com.eerussianguy.blazemap.api;

import java.util.*;

import net.minecraft.resources.ResourceLocation;

public class BlazeRegistry<T> {
    private final Map<Key<T>, RegistryEntry> objects;
    private final List<Key<T>> orderedKeys;
    private final List<Key<T>> orderedKeysView;
    private boolean frozen = false;

    public BlazeRegistry() {
        this.objects = new HashMap<>();
        this.orderedKeys = new ArrayList<>();
        this.orderedKeysView = Collections.unmodifiableList(orderedKeys);
    }

    public boolean exists(Key<? extends T> key) {
        return objects.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public T get(Key<T> key) {
        return (T) objects.get(key);
    }

    @SuppressWarnings("unchecked")
    public void register(RegistryEntry object) {
        if(frozen) throw new IllegalStateException("Registry is frozen!");
        Key<T> key = (Key<T>) object.getID();
        if(objects.containsKey(key)) throw new IllegalArgumentException("Key " + key.toString() + " is already set!");
        objects.put(key, object);
        orderedKeys.add(key);
    }

    @SuppressWarnings("unchecked")
    public void replace(RegistryEntry object) {
        if(frozen) throw new IllegalStateException("Registry is frozen!");
        Key<T> key = (Key<T>) object.getID();
        if(!objects.containsKey(key)) throw new IllegalArgumentException("Key " + key.toString() + " is not set!");
        objects.put(key, object);
    }

    public List<Key<T>> keys() {
        return orderedKeysView;
    }

    public void freeze() {
        if(frozen) throw new IllegalStateException("Registry is already frozen!");
        this.frozen = true;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public interface RegistryEntry {
        Key<?> getID();
    }

    public static class Key<T> {
        private final BlazeRegistry<T> registry;
        public final ResourceLocation location;
        private T cached = null;

        public Key(BlazeRegistry<T> registry, String path) {
            Objects.requireNonNull(registry);
            this.registry = registry;
            this.location = new ResourceLocation(path);
        }

        public Key(BlazeRegistry<T> registry, String namespace, String path) {
            Objects.requireNonNull(registry);
            this.registry = registry;
            this.location = new ResourceLocation(namespace, path);
        }

        public T value() {
            if(cached != null)
                return cached;
            T value = registry.get(this);
            if(registry.isFrozen())
                cached = value;
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof Key k) {
                if(registry != k.registry) return false;
                return k.location.equals(location);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), registry);
        }

        @Override
        public String toString() {
            return location.toString();
        }
    }
}
