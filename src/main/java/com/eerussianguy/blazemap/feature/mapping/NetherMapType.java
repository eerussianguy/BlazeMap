package com.eerussianguy.blazemap.feature.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class NetherMapType extends MapType {

    public NetherMapType() {
        super(BlazeMapReferences.MapTypes.NETHER, BlazeMapReferences.Layers.NETHER);
    }
}
