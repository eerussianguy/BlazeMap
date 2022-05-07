package com.eerussianguy.blazemap.api.event;

import java.io.File;

import net.minecraftforge.eventbus.api.Event;

/**
 * Fired by the Blaze Map engine after the game connects to a new server. <br>
 * The engine is not yet ready to serve mapping related requests, for that see DimensionChangedEvent.
 *
 * @author LordFokas
 */
public class ServerChangedEvent extends Event {
    /** The internal ID used to represent this server */
    public final String serverID;

    /** The directory where Blaze Map stores all the data for this server */
    public final File serverStorageDir;

    public ServerChangedEvent(String serverID, File serverDir){
        this.serverID = serverID;
        this.serverStorageDir = serverDir;
    }
}
