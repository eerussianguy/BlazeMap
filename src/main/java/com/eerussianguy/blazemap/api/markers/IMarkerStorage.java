package com.eerussianguy.blazemap.api.markers;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.mapping.Layer;

public interface IMarkerStorage<T extends Marker<T>> {
    Collection<T> getAll();

    void add(T marker);

    default void remove(T marker){ remove(marker.getID()); }
    void remove(ResourceLocation id);

    default boolean has(T marker){ return has(marker.getID()); }
    boolean has(ResourceLocation id);

    interface Layered<T extends Marker<T>> extends IMarkerStorage<T> {
        Collection<T> getInLayer(BlazeRegistry.Key<Layer> layerID);
        void remove(ResourceLocation id, BlazeRegistry.Key<Layer> layerID);
    }

    interface Dummy<T extends Marker<T>> extends IMarkerStorage<T>{
        @SuppressWarnings("unchecked")
        default Collection<T> getAll(){ return Collections.EMPTY_LIST; }
        default void add(T marker){ }
        default void remove(T marker){ }
        default void remove(ResourceLocation id){ }
        default boolean has(T marker){ return false; }
        default boolean has(ResourceLocation id){ return false; }
    }
}
