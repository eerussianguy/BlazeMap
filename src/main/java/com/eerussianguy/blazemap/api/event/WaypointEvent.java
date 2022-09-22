package com.eerussianguy.blazemap.api.event;

import net.minecraftforge.eventbus.api.Event;

import com.eerussianguy.blazemap.api.markers.Waypoint;

public class WaypointEvent extends Event {
    public final Waypoint waypoint;

    protected WaypointEvent(Waypoint waypoint) {
        this.waypoint = waypoint;
    }

    public static class Created extends WaypointEvent {
        public Created(Waypoint waypoint) {
            super(waypoint);
        }
    }

    public static class Removed extends WaypointEvent {
        public Removed(Waypoint waypoint) {
            super(waypoint);
        }
    }
}
