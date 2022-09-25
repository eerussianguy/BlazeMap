package com.eerussianguy.blazemap.api.event;

import java.io.File;
import java.util.Objects;

import net.minecraftforge.eventbus.api.Event;

import com.eerussianguy.blazemap.api.markers.IMarkerStorage;
import com.eerussianguy.blazemap.api.markers.IStorageFactory;
import com.eerussianguy.blazemap.api.markers.Waypoint;

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
     * The directory where Blaze Map stores all the data for this server
     */
    public final File serverStorageDir;

    /**
     * The factory that creates IMarkerStorage instances to permanently store waypoints for this server
     */
    private IStorageFactory<IMarkerStorage<Waypoint>> waypointStorageFactory;

    public ServerJoinedEvent(String serverID, File serverDir) {
        this.serverID = serverID;
        this.serverStorageDir = serverDir;
        this.waypointStorageFactory = (i, o) -> new IMarkerStorage.Dummy<>() {};
    }

    public IStorageFactory<IMarkerStorage<Waypoint>> getWaypointStorageFactory() {
        return waypointStorageFactory;
    }

    public void setWaypointStorageFactory(IStorageFactory<IMarkerStorage<Waypoint>> waypointStorageFactory) {
        this.waypointStorageFactory = Objects.requireNonNull(waypointStorageFactory);
    }
}
