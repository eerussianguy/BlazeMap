package com.eerussianguy.blazemap.feature.maps;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

import com.eerussianguy.blazemap.util.Profiler;
import com.mojang.math.Matrix4f;

public class DebugRenderUtils {
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
