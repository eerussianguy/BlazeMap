package com.eerussianguy.blazemap.feature.maps;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.BlazeMapConfig;
import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.util.IScreenSkipsMinimap;
import com.eerussianguy.blazemap.feature.BlazeMapFeatures;
import com.eerussianguy.blazemap.gui.Image;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

public class WorldMapGui extends Screen implements IScreenSkipsMinimap, IMapHost {
    private static final TextComponent EMPTY = new TextComponent("");
    private static final ResourceLocation ICON = Helpers.identifier("textures/mod_icon.png");
    private static final ResourceLocation NAME = Helpers.identifier("textures/mod_name.png");
    public static final double MIN_ZOOM = 0.25, MAX_ZOOM = 16;
    private static boolean showWidgets = true;

    public static void open() {
        Minecraft.getInstance().setScreen(new WorldMapGui());
    }


    // =================================================================================================================


    private double zoom = 1;
    private final ResourceKey<Level> dimension;
    private final MapRenderer mapRenderer;
    private final MapConfigSynchronizer synchronizer;
    private final List<MapType> mapTypes;
    private final int layersBegin;
    private Widget legend;

    public WorldMapGui() {
        super(EMPTY);
        mapRenderer = new MapRenderer(-1, -1, Helpers.identifier("dynamic/map/worldmap"), MIN_ZOOM, MAX_ZOOM, true);
        synchronizer = new MapConfigSynchronizer(mapRenderer, BlazeMapConfig.CLIENT.worldMap);
        dimension = Minecraft.getInstance().level.dimension();
        mapTypes = BlazeMapAPI.MAPTYPES.keys().stream().map(BlazeRegistry.Key::value).filter(m -> m.shouldRenderInDimension(dimension)).collect(Collectors.toUnmodifiableList());
        layersBegin = 50 + (mapTypes.size() * 20);
    }

    @Override
    public boolean isLayerVisible(BlazeRegistry.Key<Layer> layerID) {
        return mapRenderer.isLayerVisible(layerID);
    }

    @Override
    public void toggleLayer(BlazeRegistry.Key<Layer> layerID) {
        synchronizer.toggleLayer(layerID);
    }

    @Override
    public MapType getMapType() {
        return mapRenderer.getMapType();
    }

    @Override
    public void setMapType(MapType map) {
        synchronizer.setMapType(map);
        updateLegend();
    }

    @Override
    public void drawTooltip(PoseStack stack, Component component, int x, int y) {
        renderTooltip(stack, component, x, y);
    }

    @Override
    public Iterable<? extends GuiEventListener> getChildren() {
        return children();
    }

    @Override
    protected void init() {
        double scale = getMinecraft().getWindow().getGuiScale();
        mapRenderer.resize((int) (width * scale), (int) (height * scale));

        addRenderableOnly(new Image(ICON, 5, 5, 20, 20));
        addRenderableOnly(new Image(NAME, 30, 5, 110, 20));
        int y = 20;
        for(MapType mapType : mapTypes) {
            BlazeRegistry.Key<MapType> key = mapType.getID();
            int px = 7, py = (y += 20);
            addRenderableWidget(new MapTypeButton(px, py, 16, 16, key, this));
            MapType map = key.value();
            int layerY = layersBegin;
            List<BlazeRegistry.Key<Layer>> childLayers = map.getLayers().stream().collect(Collectors.toList());
            Collections.reverse(childLayers);
            for(BlazeRegistry.Key<Layer> layer : childLayers) {
                if(layer.value().isOpaque()) continue;
                LayerButton lb = new LayerButton(px, layerY, 16, 16, layer, map, this);
                layerY += 20;
                lb.checkVisible();
                addRenderableWidget(lb);
            }
        }

        updateLegend();
    }

    private void updateLegend() {
        legend = mapRenderer.getMapType().getLayers().iterator().next().value().getLegendWidget();
    }

    @Override
    public boolean mouseDragged(double cx, double cy, int button, double dx, double dy) {
        double scale = getMinecraft().getWindow().getGuiScale();
        mapRenderer.moveCenter(-(int) (dx * scale / zoom), -(int) (dy * scale / zoom));
        return super.mouseDragged(cx, cy, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        boolean zoomed;
        if(scroll > 0) {
            zoomed = synchronizer.zoomIn();
        }
        else {
            zoomed = synchronizer.zoomOut();
        }
        zoom = mapRenderer.getZoom();
        return zoomed;
    }

    @Override
    public void render(PoseStack stack, int i0, int i1, float f0) {
        float scale = (float) getMinecraft().getWindow().getGuiScale();
        fillGradient(stack, 0, 0, this.width, this.height, 0xFF333333, 0xFF333333);

        stack.pushPose();
        stack.scale(1F / scale, 1F / scale, 1);
        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        mapRenderer.render(stack, buffers);
        buffers.endBatch();
        stack.popPose();

        if(legend != null) {
            stack.pushPose();
            stack.translate(width - 5, height - 5, 0);
            legend.render(stack, -1, -1, 0);
            stack.popPose();
        }

        if(showWidgets) {
            int maps = mapTypes.size();
            if(maps > 0) {
                stack.pushPose();
                stack.translate(5, 38, 0);
                RenderHelper.fillRect(stack.last().pose(), 20, maps * 20, Colors.WIDGET_BACKGROUND);
                stack.popPose();
            }
            long layers = mapRenderer.getMapType().getLayers().stream().map(k -> k.value()).filter(l -> !l.isOpaque() && l.shouldRenderInDimension(dimension)).count();
            if(layers > 0) {
                stack.pushPose();
                stack.translate(5, layersBegin - 2, 0);
                RenderHelper.fillRect(stack.last().pose(), 20, layers * 20, Colors.WIDGET_BACKGROUND);
                stack.popPose();
            }
            stack.pushPose();
            super.render(stack, i0, i1, f0);
            stack.popPose();
        }
    }

    @Override
    public void onClose() {
        mapRenderer.close();
        synchronizer.save();
        super.onClose();
    }

    @Override
    public boolean keyPressed(int key, int x, int y) {
        if(key == BlazeMapFeatures.KEY_MAPS.getKey().getValue()) {
            this.onClose();
            return true;
        }

        if(key == GLFW.GLFW_KEY_F1) {
            showWidgets = !showWidgets;
            return true;
        }

        int dx = 0;
        int dz = 0;
        if(key == GLFW.GLFW_KEY_W) {
            dz -= 16;
        }
        if(key == GLFW.GLFW_KEY_S) {
            dz += 16;
        }
        if(key == GLFW.GLFW_KEY_D) {
            dx += 16;
        }
        if(key == GLFW.GLFW_KEY_A) {
            dx -= 16;
        }
        if(dx != 0 || dz != 0) {
            mapRenderer.moveCenter(dx, dz);
            return true;
        }
        return super.keyPressed(key, x, y);
    }

    @Override
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }
}
