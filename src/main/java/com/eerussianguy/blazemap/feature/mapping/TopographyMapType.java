package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.MapType;
import com.eerussianguy.blazemap.util.Helpers;

public class TopographyMapType extends MapType {
    public TopographyMapType() {
        super(
            BlazeMapReferences.MapTypes.TOPOGRAPHY,
            Helpers.translate("blazemap.topography"),
            Helpers.identifier("textures/map_icons/map_topography.png"),

            BlazeMapReferences.Layers.TERRAIN_HEIGHT,
            BlazeMapReferences.Layers.TERRAIN_ISOLINES,
            BlazeMapReferences.Layers.WATER_LEVEL
        );
    }

    @Override
    public boolean shouldRenderInDimension(ResourceKey<Level> dimension) {
        return !dimension.equals(Level.NETHER);
    }
}
