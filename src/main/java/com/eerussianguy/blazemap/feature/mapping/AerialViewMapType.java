package com.eerussianguy.blazemap.feature.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.MapType;
import com.eerussianguy.blazemap.util.Helpers;

public class AerialViewMapType extends MapType {

    public AerialViewMapType() {
        super(
            BlazeMapReferences.MapTypes.AERIAL_VIEW,
            Helpers.translate("blazemap.aerial_view"),
            Helpers.identifier("textures/map_icons/map_aerial.png"),

            BlazeMapReferences.Layers.BLOCK_COLOR
        );
    }
}
