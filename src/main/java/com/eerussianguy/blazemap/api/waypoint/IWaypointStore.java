package com.eerussianguy.blazemap.api.waypoint;

import java.util.Collection;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface IWaypointStore {
    Collection<Waypoint> getWaypoints(ResourceKey<Level> dimension);

    void addWaypoint(Waypoint waypoint);

    void removeWaypoint(Waypoint waypoint);

    boolean hasWaypoint(Waypoint waypoint);

    boolean hasWaypoint(ResourceLocation id);
}
