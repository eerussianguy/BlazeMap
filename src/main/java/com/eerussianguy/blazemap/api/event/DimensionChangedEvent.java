package com.eerussianguy.blazemap.api.event;

import java.util.Set;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.maps.DimensionTileNotifications;
import com.eerussianguy.blazemap.api.maps.DimensionTileStorage;
import com.eerussianguy.blazemap.api.maps.Layer;
import com.eerussianguy.blazemap.api.maps.MapType;
import com.eerussianguy.blazemap.api.markers.IMarkerStorage;
import com.eerussianguy.blazemap.api.markers.MapLabel;
import com.eerussianguy.blazemap.api.markers.Waypoint;
import com.eerussianguy.blazemap.api.util.IStorageAccess;

/**
 * Fired after the client enters a new dimension. <br>
 * <br>
 * Is triggered by: <br>
 * - PlayerEvent.PlayerChangedDimensionEvent <br>
 * - ClientPlayerNetworkEvent.LoggedInEvent <br>
 * <br>
 * However, when fired, the Blaze Map engine is ready to operate in this new dimension,
 * meaning code wanting to interact with Blaze Map in response to a dimension change
 * should use this event instead of the aforementioned ones.
 *
 * @author LordFokas
 */
public class DimensionChangedEvent extends Event {
    /**
     * The new dimension
     */
    public final ResourceKey<Level> dimension;

    /**
     * Set of map types available in the new dimension
     */
    public final Set<BlazeRegistry.Key<MapType>> availableMapTypes;

    /**
     * Set of map layers available in the new dimension
     */
    public final Set<BlazeRegistry.Key<Layer>> availableLayers;

    /**
     * Allows setting listeners to tile change notifications for this dimension <br>
     * Listeners do not persist between dimensions or visits to the same dimension and must be set every time
     */
    public final DimensionTileNotifications tileNotifications;

    /**
     * Storage containing all rendered map layer images
     */
    public final DimensionTileStorage tileStorage;

    /**
     * Volatile storage containing all the addon map labels for this dimension
     */
    public final IMarkerStorage.Layered<MapLabel> labels;

    /**
     * Permanent storage containing all the player waypoints for this dimension
     */
    public final IMarkerStorage<Waypoint> waypoints;

    /**
     * Client file storage where all map data for this dimension is stored
     */
    public final IStorageAccess dimensionStorage;

    public DimensionChangedEvent(
        ResourceKey<Level> dimension,
        Set<BlazeRegistry.Key<MapType>> mapTypes,
        Set<BlazeRegistry.Key<Layer>> layers,
        DimensionTileNotifications notifications,
        DimensionTileStorage tiles,
        IMarkerStorage.Layered<MapLabel> labels,
        IMarkerStorage<Waypoint> waypoints,
        IStorageAccess storageAccess
    ) {
        this.dimension = dimension;
        this.availableMapTypes = mapTypes;
        this.availableLayers = layers;
        this.tileNotifications = notifications;
        this.tileStorage = tiles;
        this.labels = labels;
        this.waypoints = waypoints;
        this.dimensionStorage = storageAccess;
    }
}
