package com.eerussianguy.blazemap.feature.maps;

import java.util.*;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.BlazeMapConfig;
import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.waypoint.Waypoint;
import com.eerussianguy.blazemap.feature.BlazeMapFeatures;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

public class WorldMapGui extends Screen {
    private static final TextComponent EMPTY = new TextComponent("");
    private static final ResourceLocation ICON = Helpers.identifier("textures/mod_icon.png");
    private static final ResourceLocation NAME = Helpers.identifier("textures/mod_name.png");
    private static final HashMap<BlazeRegistry.Key<MapType>, List<BlazeRegistry.Key<Layer>>> disabledLayers = new HashMap<>();
    private static final double MIN_ZOOM = 0.25, MAX_ZOOM = 16;
    private static boolean showWidgets = true;

    public static void open() {
        Minecraft.getInstance().setScreen(new WorldMapGui());
    }


    // =================================================================================================================


    private List<BlazeRegistry.Key<Layer>> disabled;
    private double zoom = 1;

    private final MapRenderer mapRenderer;

    public WorldMapGui() {
        super(EMPTY);
        disabled = new LinkedList<>(); // disabledLayers.computeIfAbsent(mapType.getID(), $ -> new LinkedList<>());
        mapRenderer = new MapRenderer(-1, -1, Helpers.identifier("dynamic/map/worldmap"), MIN_ZOOM, MAX_ZOOM);
    }

    @Override
    protected void init() {
        double scale = getMinecraft().getWindow().getGuiScale();
        mapRenderer.resize((int) (width * scale), (int) (height * scale));

        ResourceKey<Level> dim = Minecraft.getInstance().level.dimension();
        addRenderableOnly(new Image(ICON, 5, 5, 20, 20));
        addRenderableOnly(new Image(NAME, 30, 5, 110, 20));
        int y = 20;
        for(BlazeRegistry.Key<MapType> key : BlazeMapAPI.MAPTYPES.keys()) {
            if(!key.value().shouldRenderInDimension(dim)) continue;
            addRenderableWidget(new MapTypeButton(7, y += 20, 16, 16, key));
        }
    }

    @Override
    public boolean mouseDragged(double cx, double cy, int button, double dx, double dy) {
        double scale = getMinecraft().getWindow().getGuiScale();
        mapRenderer.moveCenter(- (int) (dx * scale / zoom), - (int) (dy * scale / zoom));
        return super.mouseDragged(cx, cy, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        // zoom in or out by a factor of 2, and clamp the value between min and max zoom.
        double prevZoom = zoom;
        zoom = Math.max(MIN_ZOOM, Math.min(zoom * (scroll > 0 ? 2 : 0.5), MAX_ZOOM));
        if(prevZoom == zoom) return false;
        return mapRenderer.setZoom(zoom);
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

        if(showWidgets) {
            stack.pushPose();
            super.render(stack, i0, i1, f0);
            stack.popPose();
        }
    }

    /*
    private void debugMapInfo(PoseStack stack, MultiBufferSource buffers) {
        if(!BlazeMapConfig.CLIENT.enableDebug.get()) return;

        stack.translate(150, 10, 0);
        stack.scale(0.5F, 0.5F, 0.5F);
        Matrix4f matrix = stack.last().pose();
        Font font = getMinecraft().font;

        NativeImage texture = mapTexture.getPixels();
        BlockPos begin = center.offset(-texture.getWidth() / 2, 0, -texture.getHeight() / 2);
        font.drawInBatch("Center: " + center.toShortString(), 0, 0, 0xFFFF0000, false, matrix, buffers, true, 0, LightTexture.FULL_BRIGHT);
        font.drawInBatch("Begin: " + begin.toShortString(), 0, 10, 0xFFFF0000, false, matrix, buffers, true, 0, LightTexture.FULL_BRIGHT);
        font.drawInBatch("Map Size: " + mapWidth + " x " + mapHeight, 0, 20, 0xFFFF0000, false, matrix, buffers, true, 0, LightTexture.FULL_BRIGHT);
        font.drawInBatch("Zoom Factor: " + zoom + "x", 0, 30, 0xFFFF0000, false, matrix, buffers, true, 0, LightTexture.FULL_BRIGHT);

        double render = RENDER.getAvg() / 1000;
        double upload = UPLOAD.getAvg() / 1000;
        String ru = "\u03BCs";
        String uu = "\u03BCs";
        if(render > 1000) {
            render /= 1000;
            ru = "ms";
        }
        if(upload > 1000) {
            upload /= 1000;
            uu = "ms";
        }
        font.drawInBatch(String.format("Render time: %.2f%s", render, ru), 0, 50, 0xFFFF0000, false, matrix, buffers, true, 0, LightTexture.FULL_BRIGHT);
        font.drawInBatch(String.format("Upload time: %.2f%s", upload, uu), 0, 60, 0xFFFF0000, false, matrix, buffers, true, 0, LightTexture.FULL_BRIGHT);
    }
    */

    @Override
    public void onClose() {
        MinimapRenderer.INSTANCE.setMapType(mapRenderer.getMapType());
        List<String> layers = disabled.stream().map(BlazeRegistry.Key::toString).collect(Collectors.toList());
        BlazeMapConfig.CLIENT.disabledLayers.set(layers);
        mapRenderer.close();
        super.onClose();
    }

    @Override
    public boolean keyPressed(int key, int x, int y) {
        if(key == BlazeMapFeatures.OPEN_FULL_MAP.getKey().getValue()) {
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

    @Override // TODO: this is debug code. Remove later.
    public boolean mouseClicked(double x, double y, int button) {
        if(button == GLFW.GLFW_MOUSE_BUTTON_3) {
            float scale = (float) getMinecraft().getWindow().getGuiScale();
            BlazeMapAPI.getWaypointStore().addWaypoint(new Waypoint(
                Helpers.identifier("waypoint-" + System.currentTimeMillis()),
                getMinecraft().level.dimension(),
                mapRenderer.fromBegin((int) (scale * x / zoom), 0, (int) (scale * y / zoom)),
                "Test"
            ).randomizeColor());
            mapRenderer.updateWaypoints();
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    private static class Image implements Widget {
        private final int posX, posY, width, height;
        private final ResourceLocation image;
        private int color = Colors.NO_TINT;

        public Image(ResourceLocation image, int posX, int posY, int width, int height) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.image = image;
        }

        public Image color(int color) {
            this.color = color;
            return this;
        }

        @Override
        public void render(PoseStack stack, int i0, int i1, float f0) {
            RenderHelper.drawTexturedQuad(image, color, stack, posX, posY, width, height);
        }
    }

    private class MapTypeButton extends ImageButton {
        private final BlazeRegistry.Key<MapType> key;
        private final List<LayerButton> layers = new ArrayList<>();

        public MapTypeButton(int px, int py, int w, int h, BlazeRegistry.Key<MapType> key) {
            super(px, py, w, h, 0, 0, 0, key.value().getIcon(), w, h, button -> {
                mapRenderer.setMapType(key.value());
            }, key.value().getName());
            this.key = key;
            MapType map = key.value();
            this.active = map.shouldRenderInDimension(getMinecraft().level.dimension());

            int layerX = px + 20;
            for(BlazeRegistry.Key<Layer> layer : map.getLayers()) {
                LayerButton lb = new LayerButton(layerX, py, 16, 16, layer, map);
                layers.add(lb);
                layerX += 20;
                addRenderableWidget(lb);
            }
        }

        @Override
        public void renderToolTip(PoseStack stack, int x, int y) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            TranslatableComponent component = key.value().getName();
            WorldMapGui.this.renderTooltip(stack, component, x, y);
        }

        @Override
        public void render(PoseStack stack, int mx, int my, float partial) {
            if(!active)
                RenderHelper.setShaderColor(0xFF666666);
            else if(key.equals(mapRenderer.getMapType().getID()))
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
            this.active = key.value().shouldRenderInDimension(getMinecraft().level.dimension());
            checkVisible();
        }

        @Override
        public void render(PoseStack stack, int mx, int my, float partial) {
            if(!active)
                RenderHelper.setShaderColor(0xFF666666);
            else if(mapRenderer.isLayerVisible(key))
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
            WorldMapGui.this.renderTooltip(stack, component, x, y);
        }

        public void checkVisible() {
            this.visible = mapRenderer.getMapType() == parent;
        }
    }
}
