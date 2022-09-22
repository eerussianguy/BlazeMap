package com.eerussianguy.blazemap.feature;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;

import com.eerussianguy.blazemap.BlazeMapConfig;
import com.eerussianguy.blazemap.feature.maps.MinimapRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import static com.eerussianguy.blazemap.BlazeMap.MOD_NAME;

public class Overlays {
    public static final IIngameOverlay MINIMAP = OverlayRegistry.registerOverlayTop(MOD_NAME + " Minimap", Overlays::renderMinimap);
    public static final IIngameOverlay PROFILER = OverlayRegistry.registerOverlayTop(MOD_NAME + " Profiler", Overlays::renderProfiler);

    public static void reload() {
        OverlayRegistry.enableOverlay(MINIMAP, BlazeMapConfig.CLIENT.minimap.enabled.get());
        OverlayRegistry.enableOverlay(PROFILER, BlazeMapConfig.CLIENT.enableDebug.get());
    }

    public static void renderMinimap(ForgeIngameGui gui, PoseStack stack, float partialTicks, int width, int height) {
        stack.pushPose();
        stack.scale(0.5f, 0.5f, 1f);
        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        MinimapRenderer.INSTANCE.draw(stack, buffers, gui, width * 2, height * 2);
        buffers.endBatch();
        stack.popPose();
    }

    public static void renderProfiler(ForgeIngameGui gui, PoseStack stack, float partialTicks, int width, int height) {
        stack.pushPose();
        stack.scale(0.5f, 0.5f, 1f);
        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        ProfilingRenderer.INSTANCE.draw(stack, buffers, gui, width * 2, height * 2);
        buffers.endBatch();
        stack.popPose();
    }
}
