package com.eerussianguy.blazemap.util;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;

public class RenderHelper {
    public static final RenderType SOLID = RenderType.text(Helpers.identifier("textures/solid.png"));

    public static void drawTexturedQuad(ResourceLocation texture, int color, PoseStack stack, int px, int py, int w, int h) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        setShaderColor(color);
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(stack, px, py, 0, 0, 0, w, h, w, h);
    }

    public static void setShaderColor(int color) {
        float a = ((float) ((color >> 24) & 0xFF)) / 255F;
        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) ((color) & 0xFF)) / 255F;
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void drawQuad(VertexConsumer vertices, Matrix4f matrix, float w, float h) {
        drawQuad(vertices, matrix, w, h, Colors.NO_TINT);
    }

    public static void drawQuad(VertexConsumer vertices, Matrix4f matrix, float w, float h, int color) {
        float a = ((float) ((color >> 24) & 0xFF)) / 255F;
        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) ((color) & 0xFF)) / 255F;
        vertices.vertex(matrix, 0.0F, h, -0.01F).color(r, g, b, a).uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, w, h, -0.01F).color(r, g, b, a).uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, w, 0.0F, -0.01F).color(r, g, b, a).uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, 0.0F, 0.0F, -0.01F).color(r, g, b, a).uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }

    public static void fillRect(MultiBufferSource buffers, Matrix4f matrix, float w, float h, int color) {
        drawQuad(buffers.getBuffer(SOLID), matrix, w, h, color);
    }
}
