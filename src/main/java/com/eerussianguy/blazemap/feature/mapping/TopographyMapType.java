package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class TopographyMapType extends MapType {
    public TopographyMapType() {
        super(
            BlazeMapReferences.MapTypes.TOPOGRAPHY,
            new TextComponent("Topography"),

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
