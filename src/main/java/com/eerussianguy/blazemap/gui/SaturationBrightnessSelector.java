package com.eerussianguy.blazemap.gui;

import java.awt.*;
import java.util.function.BiConsumer;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

public class SaturationBrightnessSelector implements Widget, GuiEventListener, NarratableEntry {
    private final int x, y, w, h;
    private float hue, s, b;
    private BiConsumer<Float, Float> responder;

    public SaturationBrightnessSelector(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Override
    public void render(PoseStack stack, int mx, int my, float p) {
        stack.pushPose();
        stack.translate(x, y, 0);
        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer slot = buffers.getBuffer(RenderType.text(BlazeGui.SLOT));
        RenderHelper.drawFrame(slot, stack, w, h, 1);

        Matrix4f matrix = stack.last().pose();
        VertexConsumer vertices = buffers.getBuffer(RenderHelper.SOLID);
        int color = Color.HSBtoRGB(hue / 360, 1, 1);
        vertices.vertex(matrix, 1.0F, h - 1, -0.01F).color(0xFF000000).uv(0, 1).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, w - 1, h - 1, -0.01F).color(0xFF000000).uv(1, 1).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, w - 1, 1.0F, -0.01F).color(color).uv(1, 0).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, 1.0F, 1.0F, -0.01F).color(0xFFFFFFFF).uv(0, 0).uv2(LightTexture.FULL_BRIGHT).endVertex();

        buffers.endBatch();
        stack.popPose();
    }

    public void setHue360(float hue) {
        this.hue = hue;
    }

    public void setResponder(BiConsumer<Float, Float> responder) {
        this.responder = responder;
    }

    public void setSB(float s, float b) {
        this.s = s;
        this.b = b;
        if(responder != null) {
            responder.accept(s, b);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        mx -= x;
        my -= y;
        if(mx <= 1 || mx >= w - 1 || my <= 1 || my >= h - 1) return false;
        int rw = w - 2;
        int rh = h - 2;
        mx--;
        my--;
        setSB((float) mx / rw, 1F - ((float) my / rh));
        return true;
    }

    @Override
    public NarrationPriority narrationPriority() {return NarrationPriority.NONE;}

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {}
}
