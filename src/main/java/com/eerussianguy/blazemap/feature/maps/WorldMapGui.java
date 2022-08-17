package com.eerussianguy.blazemap.feature.maps;

import java.util.*;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.eerussianguy.blazemap.BlazeMapConfig;
import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.event.DimensionChangedEvent;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

public class WorldMapGui extends Screen {
    private static final TextComponent EMPTY = new TextComponent("");
    private static final ResourceLocation ICON = Helpers.identifier("textures/mod_icon.png");
    private static final ResourceLocation NAME = Helpers.identifier("textures/mod_name.png");
    private static final ResourceLocation _TYPE = Helpers.identifier("textures/map_type.png");
    private static DimensionChangedEvent.DimensionTileStorage tileStorage;
    private static HashMap<BlazeRegistry.Key<MapType>, List<BlazeRegistry.Key<Layer>>> disabledLayers = new HashMap<>();

    private final BlockPos.MutableBlockPos center;
    private final ResourceLocation textureResource = Helpers.identifier("fullmap");
    private int nativeWidth, nativeHeight;
    private boolean needsUpdate = false;
    private DynamicTexture mapTexture;
    private RenderType renderType;
    private MapType mapType = MinimapRenderer.INSTANCE.getMapType();
    private RegionPos[][] offsets;
    private List<BlazeRegistry.Key<Layer>> disabled;

    public static void open() {
        Minecraft.getInstance().setScreen(new WorldMapGui());
    }

    public static void onDimensionChange(DimensionChangedEvent evt) {
        tileStorage = evt.tileStorage;
    }

    public WorldMapGui() {
        super(EMPTY);
        this.center = new BlockPos.MutableBlockPos();
        disabled = disabledLayers.computeIfAbsent(mapType.getID(), $ -> new LinkedList<>());
    }

    @Override
    protected void init() {
        ResourceKey<Level> dim = getMinecraft().level.dimension();
        if(!mapType.shouldRenderInDimension(dim)) {
            for(BlazeRegistry.Key<MapType> next : BlazeMapAPI.MAPTYPES.keys()) {
                MapType type = next.value();
                if(type.shouldRenderInDimension(dim)) {
                    setType(type);
                    break;
                }
            }
        }

        centerOnPlayer();
        createImage();

        addRenderableOnly(new Image(ICON, 5, 5, 20, 20));
        addRenderableOnly(new Image(NAME, 30, 5, 110, 20));

        int y = 20;
        for(BlazeRegistry.Key<MapType> key : BlazeMapAPI.MAPTYPES.keys()) {
            addRenderableWidget(new MapTypeButton(7, y += 20, 16, 16, _TYPE, key));
        }
    }

    @Override
    public boolean keyPressed(int key, int x, int y) {
        int dx = 0;
        int dz = 0;
        if (key == GLFW.GLFW_KEY_W) {
            dz += 16;
        }
        if (key == GLFW.GLFW_KEY_S) {
            dz -= 16;
        }
        if (key == GLFW.GLFW_KEY_D) {
            dx -= 16;
        }
        if (key == GLFW.GLFW_KEY_A) {
            dx += 16;
        }
        if (dx != 0 || dz != 0)
        {
            setCenter(center.getX() + dx, center.getZ() + dz);
            return true;
        }
        return super.keyPressed(key, x, y);
    }

    public void setType(MapType mapType) {
        if(this.mapType == mapType) return;
        if(!mapType.shouldRenderInDimension(getMinecraft().level.dimension())) return;
        this.mapType = mapType;
        this.disabled = disabledLayers.computeIfAbsent(mapType.getID(), $ -> new LinkedList<>());
        this.needsUpdate = true;
        for(Widget widget : WorldMapGui.this.renderables) {
            if(widget instanceof LayerButton lb) {
                lb.checkVisible();
            }
        }
    }

    public void toggleLayer(BlazeRegistry.Key<Layer> layer) {
        if(disabled.contains(layer)) disabled.remove(layer);
        else disabled.add(layer);
        needsUpdate = true;
    }

    public boolean isLayerVisible(BlazeRegistry.Key<Layer> layer) {
        return !disabled.contains(layer);
    }

    public void setCenter(int x, int z) {
        this.center.set(x, 0, z);
        makeOffsets();
        needsUpdate = true;
    }

    public void centerOnPlayer() {
        Vec3 pos = Helpers.getPlayer().position();
        setCenter((int) pos.x, (int) pos.z);
    }

    @Override
    public boolean mouseDragged(double cx, double cy, int button, double dx, double dy) {
        double scale = getMinecraft().getWindow().getGuiScale();
        setCenter(this.center.getX() - (int) (dx * scale), this.center.getZ() - (int) (dy * scale));
        return super.mouseDragged(cx, cy, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void makeOffsets() {
        Window window = getMinecraft().getWindow();
        this.nativeWidth = window.getScreenWidth();
        this.nativeHeight = window.getScreenHeight();

        int w2 = nativeWidth / 2;
        int h2 = nativeHeight / 2;
        RegionPos begin = new RegionPos(center.offset(-w2, 0, -h2));
        RegionPos end = new RegionPos(center.offset(w2, 0, h2));

        int dx = end.x - begin.x + 2;
        int dz = end.z - begin.z + 2;

        offsets = new RegionPos[dx][dz];
        for(int x = 0; x < dx; x++) {
            for(int z = 0; z < dz; z++) {
                offsets[x][z] = begin.offset(x, z);
            }
        }
    }

    private void createImage() {
        makeOffsets();
        this.mapTexture = new DynamicTexture(nativeWidth, nativeHeight, false);
        getMinecraft().getTextureManager().register(textureResource, mapTexture);
        renderType = RenderType.text(textureResource);
        needsUpdate = true;
    }

    @Override
    public void render(PoseStack stack, int i0, int i1, float f0) {
        if(needsUpdate) updateTexture();

        fillGradient(stack, 0, 0, this.width, this.height, 0xFF333333, 0xFF333333);

        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        stack.pushPose();
        renderTiles(stack, buffers);
        stack.popPose();
        buffers.endBatch();

        stack.pushPose();
        super.render(stack, i0, i1, f0);
        stack.popPose();
    }

    @Override
    public void onClose()
    {
        MinimapRenderer.INSTANCE.setMapType(mapType);
        List<String> layers = disabled.stream().map(ResourceLocation::toString).collect(Collectors.toList());
        BlazeMapConfig.CLIENT.disabledLayers.set(layers);
        super.onClose();
    }

    private void renderTiles(PoseStack stack, MultiBufferSource.BufferSource buffers) {
        drawQuad(buffers.getBuffer(renderType), stack.last().pose(), width, height);
    }

    private static void drawTexturedQuad(ResourceLocation texture, int color, PoseStack stack, int px, int py, int w, int h) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        setShaderColor(color);
        RenderSystem.setShaderTexture(0, texture);
        blit(stack, px, py, 0, 0, 0, w, h, w, h);
    }

    private static void setShaderColor(int color) {
        float a = ((float) ((color >> 24) & 0xFF)) / 255F;
        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) ((color) & 0xFF)) / 255F;
        RenderSystem.setShaderColor(r, g, b, a);
    }

    private static void drawQuad(VertexConsumer vertices, Matrix4f matrix, float w, float h) {
        vertices.vertex(matrix, 0.0F, h, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, w, h, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, w, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }

    private void updateTexture() {
        NativeImage texture = mapTexture.getPixels();
        int h = texture.getHeight();
        int w = texture.getWidth();
        texture.fillRect(0, 0, w, h, 0);

        final int cx = center.getX() & 0x01FF;
        final int cz = center.getZ() & 0x01FF;

        for(BlazeRegistry.Key<Layer> layer : mapType.getLayers()) {
            if(!isLayerVisible(layer)) continue;
            for(int ox = 0; ox < offsets.length; ox++) {
                for(int oz = 0; oz < offsets[ox].length; oz++) {
                    final RegionPos region = offsets[ox][oz];
                    final int rx = ox, rz = oz;
                    tileStorage.consumeTile(layer, region, source -> {
                        xx:
                        for(int x = 0; x < source.getWidth(); x++) {
                            for(int y = 0; y < source.getHeight(); y++) {
                                int tx = (rx * 512) + x - cx;
                                int ty = (rz * 512) + y - cz;
                                if(tx < 0 || tx >= w) continue xx;
                                if(ty < 0 || ty >= h) continue;
                                int color = Colors.layerBlend(texture.getPixelRGBA(tx, ty), source.getPixelRGBA(x, y));
                                texture.setPixelRGBA(tx, ty, color);
                            }
                        }
                    });
                }
            }
        }
        mapTexture.upload();
        needsUpdate = false;
    }

    private static class Image implements Widget {
        private final int posX, posY, width, height;
        private final ResourceLocation image;
        private int color = 0xFFFFFFFF;

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
            drawTexturedQuad(image, color, stack, posX, posY, width, height);
        }
    }

    private class MapTypeButton extends ImageButton {
        private final BlazeRegistry.Key<MapType> key;
        private final List<LayerButton> layers = new ArrayList<>();

        public MapTypeButton(int px, int py, int w, int h, ResourceLocation image, BlazeRegistry.Key<MapType> key) {
            super(px, py, w, h, 0, 0, 0, image, w, h, button -> {
                WorldMapGui.this.setType(key.value());
            }, key.value().getName());
            this.key = key;
            MapType map = key.value();
            this.active = map.shouldRenderInDimension(getMinecraft().level.dimension());

            int layerX = px + 20;
            for(BlazeRegistry.Key<Layer> layer : map.getLayers()) {
                LayerButton lb = new LayerButton(layerX, py, 16, 16, _TYPE, layer, map);
                layers.add(lb);
                layerX += 20;
                addRenderableWidget(lb);
            }
        }

        @Override
        public void renderToolTip(PoseStack stack, int x, int y) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            TranslatableComponent component = key.value().getName();
            if (active)
            {
                component.append(Helpers.translate("blazemap.enabled"));
            }
            WorldMapGui.this.renderTooltip(stack, component, x, y);
        }

        @Override
        public void render(PoseStack stack, int mx, int my, float partial) {
            if(!active)
                setShaderColor(0xFF666666);
            else if(key.equals(mapType.getID()))
                setShaderColor(0xFFFFDD00);
            else
                setShaderColor(0xFFFFFFFF);

            super.render(stack, mx, my, partial);
            setShaderColor(0xFFFFFFFF);
        }
    }

    private class LayerButton extends ImageButton {
        private final BlazeRegistry.Key<Layer> key;
        private final MapType parent;

        public LayerButton(int px, int py, int w, int h, ResourceLocation image, BlazeRegistry.Key<Layer> key, MapType parent) {
            super(px, py, w, h, 0, 0, 0, image, w, h, button -> {
                WorldMapGui.this.toggleLayer(key);
            }, key.value().getName());
            this.key = key;
            this.parent = parent;
            this.active = key.value().shouldRenderInDimension(getMinecraft().level.dimension());
            checkVisible();
        }

        @Override
        public void render(PoseStack stack, int mx, int my, float partial) {
            if(!active)
                setShaderColor(0xFF666666);
            else if(isLayerVisible(key))
                setShaderColor(0xFFFFDD00);
            else
                setShaderColor(0xFFFFFFFF);

            super.render(stack, mx, my, partial);
            setShaderColor(0xFFFFFFFF);
        }

        @Override
        public void renderToolTip(PoseStack stack, int x, int y) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            TranslatableComponent component = key.value().getName();
            if (active)
            {
                component.append(Helpers.translate("blazemap.enabled"));
            }
            WorldMapGui.this.renderTooltip(stack, component, x, y);
        }

        public void checkVisible() {
            this.visible = mapType == parent;
        }
    }
}
