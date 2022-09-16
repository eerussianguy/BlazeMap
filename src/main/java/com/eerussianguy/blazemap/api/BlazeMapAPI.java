package com.eerussianguy.blazemap.api;

import com.eerussianguy.blazemap.api.mapping.*;
import com.eerussianguy.blazemap.api.waypoint.IWaypointStore;

public class BlazeMapAPI {
    public static final BlazeRegistry<Collector<MasterDatum>> COLLECTORS = new BlazeRegistry<>();
    public static final BlazeRegistry<Layer> LAYERS = new BlazeRegistry<>();
    public static final BlazeRegistry<MapType> MAPTYPES = new BlazeRegistry<>();

    public static final BlazeRegistry<Processor> PROCESSORS = new BlazeRegistry<>();

    private static IWaypointStore waypointStore;

    public static IWaypointStore getWaypointStore() {
        return waypointStore;
    }

    public static void setWaypointStore(IWaypointStore waypointStore) {
        if(BlazeMapAPI.waypointStore == null) {
            BlazeMapAPI.waypointStore = waypointStore;
        }
    }
}
