package com.eerussianguy.blazemap.feature.maps;

import com.eerussianguy.blazemap.ClientConfig.MapConfig;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class MapConfigSynchronizer {
    private final MapConfig config;
    private final MapRenderer map;
    private MapRenderer current;


    public MapConfigSynchronizer(MapRenderer map, MapConfig config) {
        this.config = config;
        this.map = map;
        this.current = map;
        load();
    }

    public void load() {
        current.setMapType(config.activeMap.get().value());
        current.setDisabledLayers(config.disabledLayers.get());
        current.setZoom(config.zoom.get());
    }

    public void save() {
        config.activeMap.set(current.getMapType().getID());
        config.disabledLayers.set(current.getDisabledLayers());
        config.zoom.set(current.getZoom());
    }

    public boolean setMapType(MapType mapType) {
        if(!current.setMapType(mapType)) return false;
        config.activeMap.set(current.getMapType().getID());
        return true;
    }

    public boolean setZoom(double zoom) {
        if(!current.setZoom(zoom)) return false;
        config.zoom.set(current.getZoom());
        return true;
    }

    public boolean zoomIn() {
        return setZoom(current.getZoom() * 2);
    }

    public boolean zoomOut() {
        return setZoom(current.getZoom() / 2);
    }

    public void override(MapRenderer renderer) {
        this.current = renderer;
        load();
    }

    public void clear() {
        this.current = map;
        load();
    }
}
