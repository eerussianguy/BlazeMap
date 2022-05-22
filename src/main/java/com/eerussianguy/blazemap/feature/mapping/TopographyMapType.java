package com.eerussianguy.blazemap.feature.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class TopographyMapType extends MapType {
    public TopographyMapType() {
        super(
            BlazeMapReferences.MapTypes.TOPOGRAPHY,

            BlazeMapReferences.Layers.TERRAIN_HEIGHT,
            BlazeMapReferences.Layers.TERRAIN_ISOLINES,
            BlazeMapReferences.Layers.WATER_LEVEL
        );
    }
}
