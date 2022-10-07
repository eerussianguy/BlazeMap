package com.eerussianguy.blazemap.api.maps;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.mojang.blaze3d.platform.NativeImage;

/**
 * A FakeLayer is an invisible layer that does not render in the map, for when you need a layer but you don't want a layer.
 * FakeLayers are treated exactly the same way in every situation, with these differences:
 * - They do not generate a tile, nor does the engine ask them to.
 * - They are never considered opaque. This means they must have an icon as they are not expected to be the bottom layer of any map.
 *
 * In practical terms, what this means is that you can use FakeLayers to put markers on the map.
 * Using multiple fake layers enables you to segregate markers, and disabling the layers on the side panel will hide only
 * this set of markers.
 *
 * @author LordFokas
 */
public class FakeLayer extends Layer {

    public FakeLayer(BlazeRegistry.Key<Layer> id, TranslatableComponent name, ResourceLocation icon) {
        super(id, name, icon);
    }

    @Override // can't render
    public final boolean renderTile(NativeImage tile, IDataSource data) {
        throw new UnsupportedOperationException("FakeLayers do not render: " + getID());
    }

    @Override // can't have a legend
    public final Widget getLegendWidget() {
        return null;
    }
}
