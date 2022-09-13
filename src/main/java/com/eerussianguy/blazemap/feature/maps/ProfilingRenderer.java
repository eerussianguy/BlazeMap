package com.eerussianguy.blazemap.feature.maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;

import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.Profiler;
import com.eerussianguy.blazemap.util.Profilers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

public class ProfilingRenderer {
    public static final ProfilingRenderer INSTANCE = new ProfilingRenderer();

    ProfilingRenderer() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void draw(PoseStack stack, MultiBufferSource buffers, ForgeIngameGui gui, int width, int height) {
        // ping load profilers
        Profilers.Engine.COLLECTOR_LOAD_PROFILER.ping();
        Profilers.Engine.LAYER_LOAD_PROFILER.ping();
        Profilers.Engine.REGION_LOAD_PROFILER.ping();
        Profilers.Minimap.TEXTURE_LOAD_PROFILER.ping();

        if(Minecraft.getInstance().screen instanceof WorldMapGui) return;

        Profilers.Minimap.DEBUG_TIME_PROFILER.begin();

        // get player position
        LocalPlayer player = Helpers.getPlayer();
        if(player == null) return;
        BlockPos pos = player.blockPosition();

        // draw profiling information
        stack.pushPose();
        stack.translate(5, 5, 0);
        stack.scale(0.75F, 0.75F, 0);
        drawProfilingInfo(stack, buffers, Minecraft.getInstance().font, pos);
        stack.popPose();

        Profilers.Minimap.DEBUG_TIME_PROFILER.end();
    }

    private void drawProfilingInfo(PoseStack stack, MultiBufferSource buffers, Font fontRenderer, BlockPos pos) {
        Matrix4f matrix = stack.last().pose();

        // TODO: this is probably very wrong but right now I just need it to work.
        VertexConsumer playerVertices = buffers.getBuffer(RenderType.text(Helpers.identifier("minimap")));
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

    public static float drawSubsystem(Profiler.LoadProfiler load, Profiler.TimeProfiler time, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers, String type) {
        fontRenderer.drawInBatch(label, 5F, y += 5, 0xCCCCCC, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        drawTimeProfiler(time, y += 10, "    \u0394 ", fontRenderer, matrix, buffers);
        drawLoadProfiler(load, y += 10, "    # ", fontRenderer, matrix, buffers);
        drawSubsystemLoad(load, time, y += 10, "    \u03C1 ", fontRenderer, matrix, buffers, type);
        return y + 5;
    }

    public static void drawTimeProfiler(Profiler.TimeProfiler profiler, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers) {
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

    public static void drawLoadProfiler(Profiler.LoadProfiler profiler, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers) {
        String u = profiler.unit;
        String load = String.format("%s: %.2f\u0394/%s [ %.0f\u0394/%s - %.0f\u0394/%s ]", label, profiler.getAvg(), u, profiler.getMin(), u, profiler.getMax(), u);
        fontRenderer.drawInBatch(load, 5F, y, 0xAAAAFF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    public static void drawSubsystemLoad(Profiler.LoadProfiler load, Profiler.TimeProfiler time, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers, String type) {
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
}
