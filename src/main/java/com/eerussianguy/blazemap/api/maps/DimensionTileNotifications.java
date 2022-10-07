package com.eerussianguy.blazemap.api.maps;

import java.util.function.Consumer;

@FunctionalInterface
public interface DimensionTileNotifications {
    /**
     * Add a listener to be notified when a Region's section of a specific map layer changes.
     *
     * @author LordFokas
     */
    void addUpdateListener(Consumer<LayerRegion> listener);
}
