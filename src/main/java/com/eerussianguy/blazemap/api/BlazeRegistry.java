package com.eerussianguy.blazemap.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public class BlazeRegistry<T> {
    private final Map<Key<T>, Registerable<T>> objects;
    private final Class<T> cls;

    public BlazeRegistry(Class<T> cls) {
        this.objects = new HashMap<>();
        this.cls = cls;
    }

    public boolean exists(Key<? extends T> key) {
        return objects.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public T get(Key<T> key) {
        return (T) objects.get(key);
    }

    @SuppressWarnings("unchecked")
    public void register(Registerable<? extends T> object) {
        requireInstanceOfT(object);
        Key<? extends T> key = object.getID();
        if(objects.containsKey(key)) throw new IllegalArgumentException("Key " + key.toString() + " is already set!");
        objects.put((Key<T>) key, (Registerable<T>) object);
    }

    @SuppressWarnings("unchecked")
    public void replace(Registerable<? extends T> object) {
        requireInstanceOfT(object);
        Key<? extends T> key = object.getID();
        if(!objects.containsKey(key)) throw new IllegalArgumentException("Key " + key.toString() + " is not set!");
        objects.put((Key<T>) key, (Registerable<T>) object);
    }

    public Set<Key<T>> keys() {
        return objects.keySet();
    }

    private void requireInstanceOfT(Registerable<? extends T> obj) {
        obj.getClass().asSubclass(cls);
    }

    public interface Registerable<T> {
        Key<T> getID();
    }

    public static class Key<T> extends ResourceLocation {
        private final BlazeRegistry<T> registry;

        public Key(BlazeRegistry<T> registry, String fqdn) {
            super(fqdn);
            Objects.requireNonNull(registry);
            this.registry = registry;
        }

        public Key(BlazeRegistry<T> registry, String modid, String resource) {
            super(modid, resource);
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
