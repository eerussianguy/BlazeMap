package com.eerussianguy.blazemap.api.event;

import net.minecraftforge.eventbus.api.Event;

import com.eerussianguy.blazemap.api.markers.MapLabel;

public class MapLabelEvent extends Event {
    public final MapLabel label;

    protected MapLabelEvent(MapLabel label) {
        this.label = label;
    }

    public static class Created extends MapLabelEvent {
        public Created(MapLabel label) {
            super(label);
        }
    }

    public static class Removed extends MapLabelEvent {
        public Removed(MapLabel label) {
            super(label);
        }
    }
}
