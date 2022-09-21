package com.eerussianguy.blazemap.feature.maps;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.util.IScreenSkipsMinimap;
import com.eerussianguy.blazemap.gui.BlazeGui;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

public class MinimapOptionsGui extends BlazeGui implements IScreenSkipsMinimap {
    private static final TranslatableComponent MAP_TYPES = Helpers.translate("blazemap.gui.minimap_options.map_types");
    private static final TranslatableComponent LAYERS = Helpers.translate("blazemap.gui.minimap_options.layers");

    public static void open() {
        Minecraft.getInstance().setScreen(new MinimapOptionsGui());
    }

    private final MapRenderer mapRenderer = new MapRenderer(MinimapRenderer.SIZE, MinimapRenderer.SIZE, Helpers.identifier("dynamic/map/minimap_preview"), 0.5, 8);

    public MinimapOptionsGui() {
        super(Helpers.translate("blazemap.gui.minimap_options.title"), 270, 154);
    }

    @Override
    protected void init() {
        super.init();
        ResourceKey<Level> dimension = getMinecraft().level.dimension();

        int px = 15, py = 38;
        for(Key<MapType> mapID : BlazeMapAPI.MAPTYPES.keys()){
            if(!mapID.value().shouldRenderInDimension(dimension)) continue;
            if(px > 96){
                px = 15;
                py += 20;
            }
            addRenderableWidget(new MapTypeButton(left + px, top + py, 18, 18, mapID));
            px += 20;
        }
    }

    @Override
    protected void renderComponents(PoseStack stack, MultiBufferSource buffers) {
        renderLabel(stack, buffers, MAP_TYPES, 12, 25, false);
        renderSlot(stack, buffers, 12, 36, 104, 45);

        renderLabel(stack, buffers, LAYERS, 12, 86, false);
        renderSlot(stack, buffers, 12, 97, 104, 45);

        renderMap(stack, buffers);
    }

    private void renderMap(PoseStack stack, MultiBufferSource buffers){
        stack.pushPose();
        stack.translate(guiWidth - 141, 13, 0);
        renderSlot(stack, buffers, -1, -1, 130, 130);
        stack.scale(0.25F, 0.25F, 1);
        mapRenderer.render(stack, buffers);
        stack.popPose();
    }



    private class MapTypeButton extends ImageButton {
        private final Key<MapType> key;
        private final List<LayerButton> layers = new ArrayList<>();

        public MapTypeButton(int px, int py, int w, int h, Key<MapType> key) {
            super(px, py, w, h, 0, 0, 0, key.value().getIcon(), w, h, button -> {
                mapRenderer.setMapType(key.value());

                for(GuiEventListener widget : MinimapOptionsGui.this.children()){
                    if(widget instanceof LayerButton lb){
                        lb.checkVisible();
                    }
                }
            }, key.value().getName());
            this.key = key;

            MapType map = key.value();
            ResourceKey<Level> dimension = getMinecraft().level.dimension();
            int lpx = 15, lpy = 99;
            for(Key<Layer> layerID : map.getLayers()){
                Layer layer = layerID.value();
                if(!layer.shouldRenderInDimension(dimension) || layer.isOpaque()) continue;
                if(lpx > 96){
                    lpx = 15;
                    lpy += 20;
                }
                LayerButton lb = new LayerButton(left + lpx, top + lpy, 18, 18, layerID, map);
                addRenderableWidget(lb);
                layers.add(lb);
                lpx += 20;
            }
        }

        @Override
        public void renderToolTip(PoseStack stack, int x, int y) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            TranslatableComponent component = key.value().getName();
            MinimapOptionsGui.this.renderTooltip(stack, component, x, y);
        }

        @Override
        public void render(PoseStack stack, int mx, int my, float partial) {
            if(key.equals(mapRenderer.getMapType().getID()))
                RenderHelper.setShaderColor(0xFFFFDD00);
            else
                RenderHelper.setShaderColor(Colors.NO_TINT);

            super.render(stack, mx, my, partial);
            RenderHelper.setShaderColor(Colors.NO_TINT);
        }
    }

    private class LayerButton extends ImageButton {
        private final BlazeRegistry.Key<Layer> key;
        private final MapType parent;

        public LayerButton(int px, int py, int w, int h, BlazeRegistry.Key<Layer> key, MapType parent) {
            super(px, py, w, h, 0, 0, 0, key.value().getIcon(), w, h, button -> {
                mapRenderer.toggleLayer(key);
            }, key.value().getName());
            this.key = key;
            this.parent = parent;
            checkVisible();
        }

        @Override
        public void render(PoseStack stack, int mx, int my, float partial) {
            if(mapRenderer.isLayerVisible(key))
                RenderHelper.setShaderColor(0xFFFFDD00);
            else
                RenderHelper.setShaderColor(Colors.NO_TINT);

            super.render(stack, mx, my, partial);
            RenderHelper.setShaderColor(Colors.NO_TINT);
        }

        @Override
        public void renderToolTip(PoseStack stack, int x, int y) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            TranslatableComponent component = key.value().getName();
            MinimapOptionsGui.this.renderTooltip(stack, component, x, y);
        }

        public void checkVisible() {
            this.visible = mapRenderer.getMapType() == parent;
        }
    }
}
