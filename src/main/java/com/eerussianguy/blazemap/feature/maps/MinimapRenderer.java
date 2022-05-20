package com.eerussianguy.blazemap.feature.maps;

import net.minecraft.Util;
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

import com.eerussianguy.blazemap.Helpers;
import com.eerussianguy.blazemap.api.event.DimensionChangedEvent;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.engine.CartographyPipeline;
import com.eerussianguy.blazemap.engine.Profiler;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class MinimapRenderer implements AutoCloseable {
    public static final MinimapRenderer INSTANCE = new MinimapRenderer(Minecraft.getInstance().textureManager);
    private static final float SIZE = 256F;

    private static final int[][] OFFSETS = Util.make(() -> {
        final int[][] offsets = new int[9][2];
        int idx = 0;
        for(int x = -1; x <= 1; x++) {
            for(int z = -1; z <= 1; z++) {
                offsets[idx++] = new int[] {x, z};
            }
        }
        return offsets;
    });

    private MapType mapType;
    private boolean requiresUpload = true;
    private boolean debugEnabled = true;
    private DimensionChangedEvent.DimensionTileStorage tileStorage;
    private final RenderType textureRenderType;
    private final RenderType backgroundRenderType;
    private final RenderType playerRenderType;
    private final DynamicTexture texture;
    private final Profiler.TimeProfiler drawProfiler, debugProfiler;

    MinimapRenderer(TextureManager manager) {
        this.texture = new DynamicTexture(512, 512, false);
        ResourceLocation textureResource = Helpers.identifier("minimap");
        ResourceLocation mapBackground = Helpers.identifier("textures/map.png");
        ResourceLocation playerMarker = Helpers.identifier("textures/player.png");
        manager.register(textureResource, this.texture);
        this.textureRenderType = RenderType.text(textureResource);
        this.backgroundRenderType = RenderType.text(mapBackground);
        this.playerRenderType = RenderType.text(playerMarker);
        MinecraftForge.EVENT_BUS.register(this);

        this.drawProfiler = new Profiler.TimeProfiler(60);
        this.debugProfiler = new Profiler.TimeProfiler(60);
    }

    @SubscribeEvent
    public void onDimensionChanged(DimensionChangedEvent event) {
        this.tileStorage = event.tileStorage;
        event.tileNotifications.addUpdateListener(layerRegion -> this.requiresUpload = true);
    }

    public void setMapType(MapType type) {
        mapType = type;
    }

    public void draw(PoseStack stack, MultiBufferSource buffers, ForgeIngameGui gui) {
        if(requiresUpload) {
            upload();
            requiresUpload = false;
        }

        // Prepare to render minimap
        drawProfiler.begin();
        stack.pushPose();
        Matrix4f matrix4f = stack.last().pose();

        // Render map background
        stack.translate(10, 10, 0);
        VertexConsumer backgroundVertices = buffers.getBuffer(this.backgroundRenderType);
        backgroundVertices.vertex(matrix4f, 0.0F, 320, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        backgroundVertices.vertex(matrix4f, 320, 320, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        backgroundVertices.vertex(matrix4f, 320, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        backgroundVertices.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();

        // Render actual map tiles
        stack.translate(32, 32, 0);
        VertexConsumer mapVertices = buffers.getBuffer(this.textureRenderType);
        mapVertices.vertex(matrix4f, 0.0F, SIZE, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        mapVertices.vertex(matrix4f, SIZE, SIZE, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        mapVertices.vertex(matrix4f, SIZE, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        mapVertices.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();

        // Render player marker
        stack.translate(128, 128, 0);
        stack.mulPose(Vector3f.ZP.rotationDegrees(Helpers.getPlayer().getRotationVector().y));
        stack.translate(-8, -8, 0);
        VertexConsumer playerVertices = buffers.getBuffer(this.playerRenderType);
        playerVertices.vertex(matrix4f, 0.0F, 16, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        playerVertices.vertex(matrix4f, 16, 16, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        playerVertices.vertex(matrix4f, 16, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        playerVertices.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();

        stack.popPose();
        stack.pushPose();

        // Render player coordinates
        stack.translate(10, 300, 0);
        Matrix4f matrix = stack.last().pose();
        Font fontRenderer = Minecraft.getInstance().font;
        BlockPos pos = Helpers.getPlayer().blockPosition();
        String position = pos.toShortString();
        int offset = (320 - fontRenderer.width(position)) / 2;
        fontRenderer.drawInBatch(position, offset, 0F, 0x222222, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);

        // Finish
        stack.popPose();
        drawProfiler.end();

        if(debugEnabled) {
            debugProfiler.begin();
            CartographyPipeline.COLLECTOR_LOAD_PROFILER.ping();
            CartographyPipeline.LAYER_LOAD_PROFILER.ping();
            stack.pushPose();
            stack.translate(10, 340, 0);
            drawDebugInfo(stack, buffers, fontRenderer, pos);
            stack.popPose();
            debugProfiler.end();
        }
    }

    private void drawDebugInfo(PoseStack stack, MultiBufferSource buffers, Font fontRenderer, BlockPos pos) {
        Matrix4f matrix = stack.last().pose();

        VertexConsumer playerVertices = buffers.getBuffer(this.backgroundRenderType);
        float w = 250, h = 140, o = 0;
        playerVertices.vertex(matrix, o, h, -0.01F).color(0, 0, 0, 120).uv(0.25F, 0.75F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        playerVertices.vertex(matrix, w, h, -0.01F).color(0, 0, 0, 120).uv(0.75F, 0.75F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        playerVertices.vertex(matrix, w, o, -0.01F).color(0, 0, 0, 120).uv(0.75F, 0.25F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        playerVertices.vertex(matrix, o, o, -0.01F).color(0, 0, 0, 120).uv(0.25F, 0.25F).uv2(LightTexture.FULL_BRIGHT).endVertex();

        float y = 5F;
        fontRenderer.drawInBatch("Debug Info", 5F, y, 0xFF0000, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch("Player Region: " + new RegionPos(pos), 5F, y += 10, 0xCCCCCC, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        drawTimeProfiler(debugProfiler, y += 10, "Debug Info", fontRenderer, matrix, buffers);
        drawTimeProfiler(drawProfiler, y += 10, "Minimap Draw", fontRenderer, matrix, buffers);
        y = drawSubsystem(CartographyPipeline.COLLECTOR_LOAD_PROFILER, CartographyPipeline.COLLECTOR_TIME_PROFILER, y + 10, "MD Collect", fontRenderer, matrix, buffers);
        y = drawSubsystem(CartographyPipeline.LAYER_LOAD_PROFILER, CartographyPipeline.LAYER_TIME_PROFILER, y + 10, "Layer Render", fontRenderer, matrix, buffers);
    }

    private static float drawSubsystem(Profiler.LoadProfiler load, Profiler.TimeProfiler time, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers) {
        fontRenderer.drawInBatch(label, 5F, y += 5, 0xCCCCCC, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        drawTimeProfiler(time, y += 10, "    t ", fontRenderer, matrix, buffers);
        drawLoadProfiler(load, y += 10, "    \u0394 ", fontRenderer, matrix, buffers);
        drawSubsystemLoad(load, time, y += 10, "    \u03C1 ", fontRenderer, matrix, buffers);
        return y + 5;
    }

    private static void drawTimeProfiler(Profiler.TimeProfiler profiler, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers) {
        String time = String.format("%s: %.2f\u03BCs [ %.1f\u03BCs - %.1f\u03BCs ]", label, profiler.getAvg() / 1000D, profiler.getMin() / 1000D, profiler.getMax() / 1000D);
        fontRenderer.drawInBatch(time, 5F, y, 0xFFFFAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    private static void drawLoadProfiler(Profiler.LoadProfiler profiler, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers) {
        String load = String.format("%s: %.2f\u0394/t [ %.0f\u0394/t - %.0f\u0394/t ]", label, profiler.getAvg(), profiler.getMin(), profiler.getMax());
        fontRenderer.drawInBatch(load, 5F, y, 0xAAAAFF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    private static void drawSubsystemLoad(Profiler.LoadProfiler load, Profiler.TimeProfiler time, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers) {
        double l = load.getAvg();
        double t = time.getAvg() / 1000D;
        double w = l * t;
        double p = 100 * w / 50_000D;
        String profile = String.format("%s: %.2f\u03BCs/t  |  %.3f%% load", label, w, p);
        fontRenderer.drawInBatch(profile, 5F, y, 0xFFAAAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    public void upload() {
        LocalPlayer player = Helpers.getPlayer();
        if(player != null) {
            final BlockPos playerPos = player.blockPosition();
            final RegionPos originRegion = new RegionPos(playerPos);
            for(ResourceLocation layer : mapType.getLayers()) {
                for(int[] offset : OFFSETS) {
                    final RegionPos currentRegion = originRegion.offset(offset[0], offset[1]);
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
            BlockPos corner = center.offset(-256, 0, -256);
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            int pixelRegionX, pixelRegionZ;
            for(int x = 0; x < 512; x++) {
                for(int z = 0; z < 512; z++) {
                    mutable.setWithOffset(corner, x, 0, z);
                    pixelRegionX = mutable.getX() >> 9;
                    pixelRegionZ = mutable.getZ() >> 9;
                    if(currentRegion.x == pixelRegionX && currentRegion.z == pixelRegionZ) {
                        minimapPixels.setPixelRGBA(x, z, tileImage.getPixelRGBA(mutable.getX() & 0x01FF, mutable.getZ() & 0x01FF));
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        texture.close();
    }
}
