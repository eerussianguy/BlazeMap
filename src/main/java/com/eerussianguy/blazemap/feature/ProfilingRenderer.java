package com.eerussianguy.blazemap.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

import com.eerussianguy.blazemap.engine.client.BlazeMapClientEngine;
import com.eerussianguy.blazemap.engine.client.LayerRegionTile;
import com.eerussianguy.blazemap.engine.server.BlazeMapServerEngine;
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

    private ProfilingRenderer() {}

    public void draw(PoseStack stack, MultiBufferSource buffers) {
        // ping load profilers
        Profilers.Client.COLLECTOR_LOAD_PROFILER.ping();
        Profilers.Client.PROCESSOR_LOAD_PROFILER.ping();
        Profilers.Client.TRANSFORMER_LOAD_PROFILER.ping();
        Profilers.Client.LAYER_LOAD_PROFILER.ping();
        Profilers.Client.TILE_LOAD_PROFILER.ping();
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

        boolean isClient = BlazeMapClientEngine.isClientSource();
        String side = isClient ? "Client" : "Server";
        int c = BlazeMapClientEngine.numCollectors();
        int p = BlazeMapClientEngine.numProcessors();
        int t = BlazeMapClientEngine.numTransformers();
        int l = BlazeMapClientEngine.numLayers();

        float w = 250, h = 195;
        if(c > 0) h += 60;
        if(p > 0) h += 50;
        if(t > 0) h += 50;
        if(l > 0) h += 100;
        RenderHelper.fillRect(buffers, matrix, w, h, 0xA0000000);

        float y = 5F;
        String fps = Minecraft.getInstance().fpsString.split(" ")[0];
        fontRenderer.drawInBatch("Client Debug Info", 5F, y, 0xFF0000, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch(String.format("[ %s fps ]", fps), 120F, y, 0xFFAAAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);


        // Overlay profiling
        fontRenderer.drawInBatch("Overlays", 5F, y += 20, 0x0088FF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        drawTimeProfiler(Profilers.DEBUG_TIME_PROFILER, y += 15, "Debug", fontRenderer, matrix, buffers);

        // TODO: put the load profiler to use and adjust load rate according to current FPS
        y = drawSubsystem(Profilers.Minimap.TEXTURE_LOAD_PROFILER, Profilers.Minimap.TEXTURE_TIME_PROFILER, y + 10, "Minimap", "[ last second ]", fontRenderer, matrix, buffers, "frame load");

        // Engine Miscellaneous
        fontRenderer.drawInBatch("Client Engine", 5F, y += 30, 0x0088FF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch("MD Source: " + side + " / " + BlazeMapClientEngine.getMDSource(), 15F, y += 10, 0xFFFFAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch("Parallel Pool: " + BlazeMapClientEngine.cruncher().poolSize() + " threads", 15F, y += 10, 0xFFFFAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        int size = LayerRegionTile.getLoaded(), tiles = LayerRegionTile.getInstances();
        fontRenderer.drawInBatch(String.format("Layer Region Tiles: %d (%d MB)", tiles, size), 15F, y += 10, 0xFFFFAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);

        // Cartography Pipeline Profiling
        fontRenderer.drawInBatch("Client Pipeline", 5F, y += 30, 0x0088FF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        if(c > 0) {
            fontRenderer.drawInBatch(String.format(FMT, "Dirty Chunks", BlazeMapClientEngine.dirtyChunks()), 15F, y += 15, 0xCCCCCC, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
            y = drawSubsystem(Profilers.Client.COLLECTOR_LOAD_PROFILER, Profilers.Client.COLLECTOR_TIME_PROFILER, y + 5, String.format(FMT, "MD Collect", c), "[ last second ]", fontRenderer, matrix, buffers, "tick load");
        }
        if(p > 0) {
            y = drawSubsystem(Profilers.Client.PROCESSOR_LOAD_PROFILER, Profilers.Client.PROCESSOR_TIME_PROFILER, y + 10, String.format(FMT, "MD Process", p), "[ last second ]", fontRenderer, matrix, buffers, "delay");
        }
        if(t > 0) {
            y = drawSubsystem(Profilers.Client.TRANSFORMER_LOAD_PROFILER, Profilers.Client.TRANSFORMER_TIME_PROFILER, y + 10, String.format(FMT, "MD Transform", t), "[ last second ]", fontRenderer, matrix, buffers, "delay");
        }
        if(l > 0) {
            int d = BlazeMapClientEngine.dirtyTiles();
            y = drawSubsystem(Profilers.Client.LAYER_LOAD_PROFILER, Profilers.Client.LAYER_TIME_PROFILER, y + 10, String.format(FMT, "Layer Render", l), "[ last second ]", fontRenderer, matrix, buffers, "delay");
            y = drawSubsystem(Profilers.Client.TILE_LOAD_PROFILER, Profilers.Client.TILE_TIME_PROFILER, y + 10, String.format(FMT, "Dirty Tiles", d), "[ last minute ]", fontRenderer, matrix, buffers, "delay");
        }

        if(BlazeMapServerEngine.isRunning()) {
            stack.translate(w + 5, 0, 0);
            drawProfilingInfoServer(stack, buffers, fontRenderer);
        }
    }

    private void drawProfilingInfoServer(PoseStack stack, MultiBufferSource buffers, Font fontRenderer) {
        Matrix4f matrix = stack.last().pose();

        int c = BlazeMapServerEngine.numCollectors();
        int t = BlazeMapServerEngine.numTransformers();
        int p = BlazeMapServerEngine.numProcessors();

        float w = 250, h = 40;
        if(c > 0) h += 60;
        if(p > 0) h += 50;
        if(t > 0) h += 50;
        RenderHelper.fillRect(buffers, matrix, w, h, 0xA0000000);


        float y = 5f;
        int tps = BlazeMapServerEngine.avgTPS();
        fontRenderer.drawInBatch("Server Debug Info", 5F, y, 0xFF0000, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        fontRenderer.drawInBatch(String.format("[ %d tps ]", tps), 120F, y, 0xFFAAAA, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);

        fontRenderer.drawInBatch(String.format(FMT, "Server Pipelines", BlazeMapServerEngine.numPipelines()), 5F, y += 20, 0x0088FF, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
        if(c > 0) {
            fontRenderer.drawInBatch(String.format(FMT, "Dirty Chunks", BlazeMapServerEngine.dirtyChunks()), 15F, y += 15, 0xCCCCCC, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
            y = drawSubsystem(Profilers.Server.COLLECTOR_LOAD_PROFILER, Profilers.Server.COLLECTOR_TIME_PROFILER, y + 5, String.format(FMT, "MD Collect", c), "[ last second ]", fontRenderer, matrix, buffers, "tick load");
        }
        if(p > 0) {
            y = drawSubsystem(Profilers.Server.PROCESSOR_LOAD_PROFILER, Profilers.Server.PROCESSOR_TIME_PROFILER, y + 10, String.format(FMT, "MD Process", p), "[ last second ]", fontRenderer, matrix, buffers, "delay");
        }
        if(t > 0) {
            y = drawSubsystem(Profilers.Server.TRANSFORMER_LOAD_PROFILER, Profilers.Server.TRANSFORMER_TIME_PROFILER, y + 10, String.format(FMT, "MD Transform", t), "[ last second ]", fontRenderer, matrix, buffers, "delay");
        }
    }


    // =================================================================================================================
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
