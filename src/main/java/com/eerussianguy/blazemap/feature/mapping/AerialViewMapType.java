package com.eerussianguy.blazemap.feature.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.util.Helpers;

public class AerialViewMapType extends MapType {

    public AerialViewMapType() {
        super(
            BlazeMapReferences.MapTypes.AERIAL_VIEW,
            Helpers.translate("blazemap.aerial_view"),

            BlazeMapReferences.Layers.BLOCK_COLOR,
            BlazeMapReferences.Layers.TERRAIN_ISOLINES
        );
    }
}
