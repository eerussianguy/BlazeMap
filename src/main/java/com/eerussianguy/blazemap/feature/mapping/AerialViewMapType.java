package com.eerussianguy.blazemap.feature.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class AerialViewMapType extends MapType {

    public AerialViewMapType()
    {
        super(BlazeMapReferences.MapTypes.AERIAL_VIEW, BlazeMapReferences.Layers.BLOCK_COLOR, BlazeMapReferences.Layers.TERRAIN_ISOLINES);
    }
}
