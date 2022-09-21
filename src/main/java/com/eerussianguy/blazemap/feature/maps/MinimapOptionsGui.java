package com.eerussianguy.blazemap.feature.maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.util.IScreenSkipsMinimap;
import com.eerussianguy.blazemap.gui.BlazeGui;
import com.eerussianguy.blazemap.util.Helpers;
import com.mojang.blaze3d.vertex.PoseStack;

public class MinimapOptionsGui extends BlazeGui implements IScreenSkipsMinimap, IMapHost {
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
    public boolean isLayerVisible(Key<Layer> layerID) {
        return mapRenderer.isLayerVisible(layerID);
    }

    @Override
    public void toggleLayer(Key<Layer> layerID) {
        mapRenderer.toggleLayer(layerID);
    }

    @Override
    public MapType getMapType() {
        return mapRenderer.getMapType();
    }

    @Override
    public void setMapType(MapType map) {
        mapRenderer.setMapType(map);
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
            addRenderableWidget(new MapTypeButton(left + px, top + py, 18, 18, mapID, this));
            MapType map = mapID.value();
            int lpx = 15, lpy = 99;
            for(Key<Layer> layerID : map.getLayers()){
                Layer layer = layerID.value();
                if(!layer.shouldRenderInDimension(dimension) || layer.isOpaque()) continue;
                if(lpx > 96){
                    lpx = 15;
                    lpy += 20;
                }
                LayerButton lb = new LayerButton(left + lpx, top + lpy, 18, 18, layerID, map, this);
                lb.checkVisible();
                addRenderableWidget(lb);
                lpx += 20;
            }
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
}
