package com.eerussianguy.blazemap.core.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class TopographyMapType extends MapType {
    public TopographyMapType(){
        super(BlazeMapReferences.LAYER_TERRAIN_HEIGHT);
    }
}
