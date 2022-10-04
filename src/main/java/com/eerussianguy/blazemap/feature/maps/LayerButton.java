package com.eerussianguy.blazemap.feature.maps;

import net.minecraft.client.gui.components.ImageButton;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.MapType;
import com.eerussianguy.blazemap.api.pipeline.Layer;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

public class LayerButton extends ImageButton {
    private final Key<Layer> key;
    private final MapType parent;
    private final IMapHost host;

    public LayerButton(int px, int py, int w, int h, Key<Layer> key, MapType parent, IMapHost host) {
        super(px, py, w, h, 0, 0, 0, key.value().getIcon(), w, h, button -> {
            host.toggleLayer(key);
        }, key.value().getName());
        this.key = key;
        this.parent = parent;
        this.host = host;
        checkVisible();
    }

    @Override
    public void render(PoseStack stack, int mx, int my, float partial) {
        if(host.isLayerVisible(key))
            RenderHelper.setShaderColor(0xFFFFDD00);
        else
            RenderHelper.setShaderColor(Colors.NO_TINT);
        super.render(stack, mx, my, partial);
        RenderHelper.setShaderColor(Colors.NO_TINT);
    }

    @Override
    public void renderToolTip(PoseStack stack, int x, int y) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        host.drawTooltip(stack, key.value().getName(), x, y);
    }

    public void checkVisible() {
        this.visible = host.getMapType() == parent;
    }
}
