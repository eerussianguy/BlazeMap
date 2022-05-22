package com.eerussianguy.blazemap.feature.maps;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.event.DimensionChangedEvent;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.util.Profiler;
import com.eerussianguy.blazemap.util.Profilers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class MinimapRenderer implements AutoCloseable {
    public static final MinimapRenderer INSTANCE = new MinimapRenderer(Minecraft.getInstance().textureManager);
    private static final int SIZE = 512, SIZE_HALF = SIZE / 2;

    private static final int[][] OFFSETS = new int[][] {
        new int[] {-1, -1}, new int[] {0, -1}, new int[] {1, -1},
        new int[] {-1, 0}, new int[] {0, 0}, new int[] {1, 0},
        new int[] {-1, 1}, new int[] {0, 1}, new int[] {1, 1}
    };

    private final RenderType textureRenderType;
    private final RenderType backgroundRenderType;
    private final RenderType playerRenderType;
    private final DynamicTexture texture;
    private MapType mapType;
    private boolean requiresUpload = true;
    private boolean debugEnabled = false;
    private DimensionChangedEvent.DimensionTileStorage tileStorage;
    private BlockPos last = BlockPos.ZERO;
    private MinimapSize size = MinimapSize.LARGE;
    private MinimapZoom zoom = MinimapZoom.MEDIUM;
    private Set<BlazeRegistry.Key<Layer>> DISABLED_LAYERS = new HashSet<>();

    MinimapRenderer(TextureManager manager) {
        this.texture = new DynamicTexture(SIZE, SIZE, false);
        ResourceLocation textureResource = Helpers.identifier("minimap");
        ResourceLocation mapBackground = Helpers.identifier("textures/map.png");
        ResourceLocation playerMarker = Helpers.identifier("textures/player.png");
        manager.register(textureResource, this.texture);
        this.textureRenderType = RenderType.text(textureResource);
        this.backgroundRenderType = RenderType.text(mapBackground);
        this.playerRenderType = RenderType.text(playerMarker);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onDimensionChanged(DimensionChangedEvent event) {
        this.tileStorage = event.tileStorage;
        event.tileNotifications.addUpdateListener(layerRegion -> this.requiresUpload = true);
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public void setMapType(MapType type) {
        mapType = type;
    }

    public void setMapSize(MinimapSize size) {
        this.size = size;
    }

    public void setMapZoom(MinimapZoom zoom) {
        this.zoom = zoom;
    }

    public void setLayerStatus(BlazeRegistry.Key<Layer> layer, boolean enabled){
        if(enabled){
            DISABLED_LAYERS.remove(layer);
        }else{
            DISABLED_LAYERS.add(layer);
        }
        requiresUpload = true;
    }

    public void upload() {
        LocalPlayer player = Helpers.getPlayer();
        if(player != null) {
            final BlockPos playerPos = player.blockPosition();
            final RegionPos originRegion = new RegionPos(playerPos);
            texture.getPixels().fillRect(0, 0, SIZE, SIZE, 0);
            for(BlazeRegistry.Key<Layer> layer : mapType.getLayers()) {
                if(DISABLED_LAYERS.contains(layer)) continue;
                for(int[] offset : OFFSETS) {
                    final RegionPos currentRegion = originRegion.offset(offset[0], offset[1]);
                    if(!currentRegion.containsSquare(playerPos, SIZE_HALF)) continue;
                    tileStorage.consumeTile(layer, currentRegion, data ->
                        consume(playerPos, currentRegion, texture, data)
                    );
                }
            }
        }

        texture.upload();
    }

    private void consume(BlockPos center, RegionPos currentRegion, DynamicTexture minimap, NativeImage tileImage) {
        NativeImage minimapPixels = minimap.getPixels();
        if(minimapPixels != null) {
            BlockPos corner = center.offset(-SIZE_HALF, 0, -SIZE_HALF);
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            int pixelRegionX, pixelRegionZ;
            for(int x = 0; x < SIZE; x++) {
                for(int z = 0; z < SIZE; z++) {
                    mutable.setWithOffset(corner, x, 0, z);
                    pixelRegionX = mutable.getX() >> 9;
                    pixelRegionZ = mutable.getZ() >> 9;
                    if(currentRegion.x == pixelRegionX && currentRegion.z == pixelRegionZ) {
                        int color = Colors.layerBlend(
                            minimapPixels.getPixelRGBA(x, z),
                            tileImage.getPixelRGBA(mutable.getX() & 0x01FF, mutable.getZ() & 0x01FF)
                        );
                        minimapPixels.setPixelRGBA(x, z, color);
                    }
                }
            }
        }
    }

    public void draw(PoseStack stack, MultiBufferSource buffers, ForgeIngameGui gui, int width, int height) {
        BlockPos pos = Helpers.getPlayer().blockPosition();

        if(requiresUpload || !pos.equals(last)) {
            last = pos;
            Profilers.Minimap.TEXTURE_LOAD_PROFILER.hit();
            Profilers.Minimap.TEXTURE_TIME_PROFILER.begin();
            upload();
            requiresUpload = false;
            Profilers.Minimap.TEXTURE_TIME_PROFILER.end();
        }

        // Prepare to render minimap
        Profilers.Minimap.DRAW_TIME_PROFILER.begin();
        stack.pushPose();
        Matrix4f matrix4f = stack.last().pose();

        int w = 136, h = 148, m = 8;

        // Translate to corner and apply scale
        stack.translate(width, 0, 0);
        stack.scale(size.scale, size.scale, 0);

        // Render map background
        stack.translate(-(w + m), m, 0);
        drawQuad(buffers.getBuffer(this.backgroundRenderType), matrix4f, w, h);

        // Render actual map tiles
        stack.translate(4, 4, 0);
        drawQuad(buffers.getBuffer(this.textureRenderType), matrix4f, 128, 128, zoom.trim);

        // Render player marker
        stack.translate(64, 64, 0);
        stack.mulPose(Vector3f.ZP.rotationDegrees(Helpers.getPlayer().getRotationVector().y));
        stack.translate(-6, -6, 0);
        drawQuad(buffers.getBuffer(this.playerRenderType), matrix4f, 12, 12);

        stack.popPose();
        stack.pushPose();

        // Translate to corner and apply scale
        stack.translate(width, 0, 0);
        stack.scale(size.scale, size.scale, 0);

        // Render player coordinates
        stack.translate(-(w + m), h - 4, 0);
        stack.scale(0.5F, 0.5F, 0);
        Matrix4f matrix = stack.last().pose();
        Font fontRenderer = Minecraft.getInstance().font;
        String position = pos.toShortString();
        int offset = (w * 2 - fontRenderer.width(position)) / 2;
        fontRenderer.drawInBatch(position, offset, 0F, 0xDDDDDD, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);

        // Finish
        stack.popPose();
        Profilers.Minimap.DRAW_TIME_PROFILER.end();

        if(debugEnabled) {
            Profilers.Minimap.DEBUG_TIME_PROFILER.begin();

            // ping load profilers
            Profilers.Engine.COLLECTOR_LOAD_PROFILER.ping();
            Profilers.Engine.LAYER_LOAD_PROFILER.ping();
            Profilers.Engine.REGION_LOAD_PROFILER.ping();
            Profilers.Minimap.TEXTURE_LOAD_PROFILER.ping();

            stack.pushPose();
            stack.translate(5, 5, 0);
            stack.scale(0.75F, 0.75F, 0);
            drawDebugInfo(stack, buffers, fontRenderer, pos);
            stack.popPose();
            Profilers.Minimap.DEBUG_TIME_PROFILER.end();
        }
    }

    private void drawDebugInfo(PoseStack stack, MultiBufferSource buffers, Font fontRenderer, BlockPos pos) {
        Matrix4f matrix = stack.last().pose();

        VertexConsumer playerVertices = buffers.getBuffer(this.backgroundRenderType);
        float w = 250, h = 270, o = 0;
        playerVertices.vertex(matrix, o, h, -0.01F).color(0, 0, 0, 120).uv(0.25F, 0.75F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        playerVertices.vertex(matrix, w, h, -0.01F).color(0, 0, 0, 120).uv(0.75F, 0.75F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        playerVertices.vertex(matrix, w, o, -0.01F).color(0, 0, 0, 120).uv(0.75F, 0.25F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        playerVertices.vertex(matrix, o, o, -0.01F).color(0, 0, 0, 120).uv(0.25F, 0.25F).uv2(LightTexture.FULL_BRIGHT).endVertex();

        float y = 5F;
        fontRenderer.drawInBatch("Debug Info", 5F, y, 0xFF0000, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch("Player Region: " + new RegionPos(pos), 5F, y += 10, 0xCCCCCC, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        drawTimeProfiler(Profilers.Minimap.DEBUG_TIME_PROFILER, y += 10, "Debug Info", fontRenderer, matrix, buffers);
        drawTimeProfiler(Profilers.Minimap.DRAW_TIME_PROFILER, y += 10, "Minimap Draw", fontRenderer, matrix, buffers);
        y = drawSubsystem(Profilers.Minimap.TEXTURE_LOAD_PROFILER, Profilers.Minimap.TEXTURE_TIME_PROFILER, y + 10, "Texture Upload      [ last second ]", fontRenderer, matrix, buffers, "frame load");

        // Cartography Pipeline Profiling
        fontRenderer.drawInBatch("Cartography Pipeline", 5F, y += 30, 0x0088FF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        y = drawSubsystem(Profilers.Engine.COLLECTOR_LOAD_PROFILER, Profilers.Engine.COLLECTOR_TIME_PROFILER, y + 10, "MD Collect      [ last second ]", fontRenderer, matrix, buffers, "tick load");
        y = drawSubsystem(Profilers.Engine.LAYER_LOAD_PROFILER, Profilers.Engine.LAYER_TIME_PROFILER, y + 10, "Layer Render      [ last second ]", fontRenderer, matrix, buffers, "delay");
        y = drawSubsystem(Profilers.Engine.REGION_LOAD_PROFILER, Profilers.Engine.REGION_TIME_PROFILER, y + 10, "Region Save      [ last minute ]", fontRenderer, matrix, buffers, "delay");
    }

    private static float drawSubsystem(Profiler.LoadProfiler load, Profiler.TimeProfiler time, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers, String type) {
        fontRenderer.drawInBatch(label, 5F, y += 5, 0xCCCCCC, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        drawTimeProfiler(time, y += 10, "    \u0394 ", fontRenderer, matrix, buffers);
        drawLoadProfiler(load, y += 10, "    # ", fontRenderer, matrix, buffers);
        drawSubsystemLoad(load, time, y += 10, "    \u03C1 ", fontRenderer, matrix, buffers, type);
        return y + 5;
    }

    private static void drawTimeProfiler(Profiler.TimeProfiler profiler, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers) {
        double at = profiler.getAvg() / 1000D, nt = profiler.getMin() / 1000D, xt = profiler.getMax() / 1000D;
        String au = "\u03BC", nu = "\u03BC", xu = "\u03BC";
        if(at >= 1000) {
            at /= 1000D;
            au = "m";
        }
        if(nt >= 1000) {
            nt /= 1000D;
            nu = "m";
        }
        if(xt >= 1000) {
            xt /= 1000D;
            xu = "m";
        }
        String time = String.format("%s: %.2f%ss [ %.1f%ss - %.1f%ss ]", label, at, au, nt, nu, xt, xu);
        fontRenderer.drawInBatch(time, 5F, y, 0xFFFFAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    private static void drawLoadProfiler(Profiler.LoadProfiler profiler, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers) {
        String u = profiler.unit;
        String load = String.format("%s: %.2f\u0394/%s [ %.0f\u0394/%s - %.0f\u0394/%s ]", label, profiler.getAvg(), u, profiler.getMin(), u, profiler.getMax(), u);
        fontRenderer.drawInBatch(load, 5F, y, 0xAAAAFF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    private static void drawSubsystemLoad(Profiler.LoadProfiler load, Profiler.TimeProfiler time, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers, String type) {
        double l = load.getAvg();
        double t = time.getAvg() / 1000D;
        double w = l * t;
        double p = 100 * w / (load.interval * 1000);
        String u = "\u03BC";
        if(w >= 1000) {
            w /= 1000D;
            u = "m";
        }
        String profile = String.format("%s: %.2f%ss/%s  |  %.3f%% %s", label, w, u, load.unit, p, type);
        fontRenderer.drawInBatch(profile, 5F, y, 0xFFAAAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    private static void drawQuad(VertexConsumer vertices, Matrix4f matrix, float w, float h) {
        drawQuad(vertices, matrix, w, h, 0);
    }

    private static void drawQuad(VertexConsumer vertices, Matrix4f matrix, float w, float h, float trim) {
        vertices.vertex(matrix, 0.0F, h, -0.01F).color(255, 255, 255, 255).uv(0.0F+trim, 1.0F-trim).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, w, h, -0.01F).color(255, 255, 255, 255).uv(1.0F-trim, 1.0F-trim).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, w, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F-trim, 0.0F+trim).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F+trim, 0.0F+trim).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }

    @Override
    public void close() {
        texture.close();
    }
}
