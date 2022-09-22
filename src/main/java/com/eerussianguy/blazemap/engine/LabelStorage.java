package com.eerussianguy.blazemap.engine;

import java.util.*;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.markers.IMarkerStorage;
import com.eerussianguy.blazemap.api.markers.MapLabel;

public class LabelStorage implements IMarkerStorage.Layered<MapLabel> {
    private final HashMap<Key<Layer>, HashMap<ResourceLocation, MapLabel>> layers = new HashMap<>();
    private final HashMap<Key<Layer>, Collection<MapLabel>> views = new HashMap<>();
    private final HashSet<ResourceLocation> labelIDs = new HashSet<>();
    private final ResourceKey<Level> dimension;

    public LabelStorage(ResourceKey<Level> dimension){
        this.dimension = dimension;
    }

    @Override
    public Collection<MapLabel> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<MapLabel> getInLayer(Key<Layer> layerID) {
        return views.computeIfAbsent(layerID, l -> Collections.unmodifiableCollection(inLayer(l).values()));
    }

    @Override
    public void add(MapLabel marker) {
        if(!dimension.equals(marker.getDimension())) return;
        ResourceLocation id = marker.getID();
        if(labelIDs.contains(id)) throw new IllegalStateException("Marker already exists in storage");
        inLayer(marker.getLayerID()).put(id, marker);
        labelIDs.add(id);
    }

    @Override
    public void remove(MapLabel label) {
        ResourceLocation id = label.getID();
        if(labelIDs.contains(id)){
            inLayer(label.getLayerID()).remove(id);
            labelIDs.remove(id);
        }
    }

    @Override
    public void remove(ResourceLocation id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ResourceLocation id, Key<Layer> layerID) {
        if(labelIDs.contains(id)){
            HashMap<ResourceLocation, MapLabel> labels = inLayer(layerID);
            if(!labels.containsKey(id)) throw new IllegalArgumentException("Marker is not in specified layer");
            labels.remove(id);
            labelIDs.remove(id);
        }
    }

    @Override
    public boolean has(ResourceLocation id) {
        return labelIDs.contains(id);
    }

    private HashMap<ResourceLocation, MapLabel> inLayer(Key<Layer> layer){
        return layers.computeIfAbsent(layer, l -> new HashMap<>());
    }
}