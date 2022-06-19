package com.eerussianguy.blazemap.feature.mapping;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.mapping.MapType;

public class NetherMapType extends MapType {

    public NetherMapType() {
        super(BlazeMapReferences.MapTypes.NETHER, new TextComponent("Nether"), BlazeMapReferences.Layers.NETHER);
    }

    @Override
    public boolean shouldRenderInDimension(ResourceKey<Level> dimension) {
        return dimension.equals(Level.NETHER);
    }
}
