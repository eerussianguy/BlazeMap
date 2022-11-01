package com.eerussianguy.blazemap.api.event;

import java.util.Objects;

import net.minecraftforge.eventbus.api.Event;

import com.eerussianguy.blazemap.api.markers.IMarkerStorage;
import com.eerussianguy.blazemap.api.markers.IStorageFactory;
import com.eerussianguy.blazemap.api.markers.Waypoint;
import com.eerussianguy.blazemap.api.util.IStorageAccess;

/**
 * Fired by the Blaze Map engine after the game connects to a new server. <br>
 * The engine is not yet ready to serve mapping related requests, for that see DimensionChangedEvent.
 *
 * @author LordFokas
 */
public class ServerJoinedEvent extends Event {
    /**
     * The internal ID used to represent this server
     */
    public final String serverID;

    /**
     * The file storage where Blaze Map stores all the data for this server
     */
    public final IStorageAccess serverStorage;

    /**
     * The factory that creates IMarkerStorage instances to permanently store waypoints for this server
     */
    private IStorageFactory<IMarkerStorage<Waypoint>> waypointStorageFactory;

    /**
     * If the server we connected to has Blaze Map present
     */
    public final boolean serverHasBlazeMap;

    public ServerJoinedEvent(String serverID, IStorageAccess storage, boolean serverHasBlazeMap) {
        this.serverID = serverID;
        this.serverStorage = storage;
        this.serverHasBlazeMap = serverHasBlazeMap;
        this.waypointStorageFactory = (i, o, e) -> new IMarkerStorage.Dummy<>() {};
    }

    public IStorageFactory<IMarkerStorage<Waypoint>> getWaypointStorageFactory() {
        return waypointStorageFactory;
    }

    public void setWaypointStorageFactory(IStorageFactory<IMarkerStorage<Waypoint>> waypointStorageFactory) {
        this.waypointStorageFactory = Objects.requireNonNull(waypointStorageFactory);
    }
}
