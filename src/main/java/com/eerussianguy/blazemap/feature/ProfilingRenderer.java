package com.eerussianguy.blazemap.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;

import com.eerussianguy.blazemap.engine.BlazeMapEngine;
import com.eerussianguy.blazemap.feature.maps.WorldMapGui;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.Profiler;
import com.eerussianguy.blazemap.util.Profilers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

public class ProfilingRenderer {
    public static final ProfilingRenderer INSTANCE = new ProfilingRenderer();
    private static final String FMT = "%s : %d";

    ProfilingRenderer() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void draw(PoseStack stack, MultiBufferSource buffers, ForgeIngameGui gui, int width, int height) {
        // ping load profilers
        Profilers.Engine.COLLECTOR_LOAD_PROFILER.ping();
        Profilers.Engine.PROCESSOR_LOAD_PROFILER.ping();
        Profilers.Engine.TRANSFORMER_LOAD_PROFILER.ping();
        Profilers.Engine.LAYER_LOAD_PROFILER.ping();
        Profilers.Engine.REGION_LOAD_PROFILER.ping();
        Profilers.Minimap.TEXTURE_LOAD_PROFILER.ping();

        if(Minecraft.getInstance().screen instanceof WorldMapGui) return;

        Profilers.DEBUG_TIME_PROFILER.begin();

        // get player position
        LocalPlayer player = Helpers.getPlayer();
        if(player == null) return;
        BlockPos pos = player.blockPosition();

        // draw profiling information
        stack.pushPose();
        stack.translate(5, 5, 0);
        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        stack.scale(4F / scale, 4F / scale, 1);
        drawProfilingInfo(stack, buffers, Minecraft.getInstance().font, pos);
        stack.popPose();

        Profilers.DEBUG_TIME_PROFILER.end();
    }

    private void drawProfilingInfo(PoseStack stack, MultiBufferSource buffers, Font fontRenderer, BlockPos pos) {
        Matrix4f matrix = stack.last().pose();

        boolean isClient = BlazeMapEngine.isClientSource();
        String side = isClient ? "Client" : "Server";
        int c = BlazeMapEngine.numCollectors();
        int p = BlazeMapEngine.numProcessors();
        int t = BlazeMapEngine.numTransformers();
        int l = BlazeMapEngine.numLayers();

        float w = 250, h = 180;
        if(c > 0) h += 50;
        if(p > 0) h += 50;
        if(t > 0) h += 50;
        if(l > 0) h += 100;
        RenderHelper.fillRect(buffers, matrix, w, h, 0xA0000000);

        float y = 5F;
        fontRenderer.drawInBatch("Client Debug Info", 5F, y, 0xFF0000, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);

        // Overlay profiling
        fontRenderer.drawInBatch("Overlays", 5F, y += 20, 0x0088FF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        drawTimeProfiler(Profilers.DEBUG_TIME_PROFILER, y += 15, "Debug", fontRenderer, matrix, buffers);
        y = drawSubsystem(Profilers.Minimap.TEXTURE_LOAD_PROFILER, Profilers.Minimap.TEXTURE_TIME_PROFILER, y + 10, "Minimap", "[ last second ]", fontRenderer, matrix, buffers, "frame load");

        // Engine Miscellaneous
        fontRenderer.drawInBatch("Engine Miscellaneous", 5F, y += 30, 0x0088FF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch("MD Source: " + side + " / " + BlazeMapEngine.getMDSource(), 15F, y += 10, 0xFFFFAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch("Parallel Pool: " + BlazeMapEngine.cruncher().poolSize() + " threads", 15F, y += 10, 0xFFFFAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);

        // Cartography Pipeline Profiling
        fontRenderer.drawInBatch("Cartography Pipeline", 5F, y += 30, 0x0088FF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        if(c > 0) {
            y = drawSubsystem(Profilers.Engine.COLLECTOR_LOAD_PROFILER, Profilers.Engine.COLLECTOR_TIME_PROFILER, y + 10, String.format(FMT, "MD Collect", c), "[ last second ]", fontRenderer, matrix, buffers, "tick load");
        }
        if(p > 0) {
            y = drawSubsystem(Profilers.Engine.PROCESSOR_LOAD_PROFILER, Profilers.Engine.PROCESSOR_TIME_PROFILER, y + 10, String.format(FMT, "MD Process", p), "[ last second ]", fontRenderer, matrix, buffers, "delay");
        }
        if(t > 0) {
            y = drawSubsystem(Profilers.Engine.TRANSFORMER_LOAD_PROFILER, Profilers.Engine.TRANSFORMER_TIME_PROFILER, y + 10, String.format(FMT, "MD Transform", t), "[ last second ]", fontRenderer, matrix, buffers, "delay");
        }
        if(l > 0) {
            y = drawSubsystem(Profilers.Engine.LAYER_LOAD_PROFILER, Profilers.Engine.LAYER_TIME_PROFILER, y + 10, String.format(FMT, "Layer Render", l), "[ last second ]", fontRenderer, matrix, buffers, "delay");
            y = drawSubsystem(Profilers.Engine.REGION_LOAD_PROFILER, Profilers.Engine.REGION_TIME_PROFILER, y + 10, "Region Save", "[ last minute ]", fontRenderer, matrix, buffers, "delay");
        }
    }

    public static float drawSubsystem(Profiler.LoadProfiler load, Profiler.TimeProfiler time, float y, String label, String roll, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers, String type) {
        fontRenderer.drawInBatch(label, 15F, y += 5, 0xCCCCCC, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch(roll, 120F, y, 0xCCCCCC, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        drawTimeProfiler(time, y += 10, "\u0394 ", fontRenderer, matrix, buffers);
        drawLoadProfiler(load, y += 10, "# ", fontRenderer, matrix, buffers);
        drawSubsystemLoad(load, time, y += 10, "\u03C1 ", fontRenderer, matrix, buffers, type);
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
        String avg = String.format("%s: %.2f%ss", label, at, au);
        String dst = String.format("[ %.1f%ss - %.1f%ss ]", nt, nu, xt, xu);
        fontRenderer.drawInBatch(avg, 15F, y, 0xFFFFAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch(dst, 120F, y, 0xFFFFAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    public static void drawLoadProfiler(Profiler.LoadProfiler profiler, float y, String label, Font fontRenderer, Matrix4f matrix, MultiBufferSource buffers) {
        String u = profiler.unit;
        String avg = String.format("%s: %.2f\u0394/%s", label, profiler.getAvg(), u);
        String dst = String.format("[ %.0f\u0394/%s - %.0f\u0394/%s ]", profiler.getMin(), u, profiler.getMax(), u);
        fontRenderer.drawInBatch(avg, 15F, y, 0xAAAAFF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch(dst, 120F, y, 0xAAAAFF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
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
        String con = String.format("%s: %.2f%ss/%s", label, w, u, load.unit);
        String pct = String.format("%.3f%% %s", p, type);
        fontRenderer.drawInBatch(con, 15F, y, 0xFFAAAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch(pct, 120F, y, 0xFFAAAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }
}
