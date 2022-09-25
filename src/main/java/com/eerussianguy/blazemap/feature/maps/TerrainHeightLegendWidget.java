package com.eerussianguy.blazemap.feature.maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.feature.mapping.TerrainHeightLayer;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

public class TerrainHeightLegendWidget implements Widget {
    private static NativeImage legend;
    private static RenderType type;
    private static int min;
    private static int max;

    private static RenderType getLegend() {
        if(type == null) {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            min = level.getMinBuildHeight();
            int sea = level.getSeaLevel();
            max = level.getMaxBuildHeight();
            legend = TerrainHeightLayer.getLegend(min, sea, max);
            DynamicTexture texture = new DynamicTexture(legend);
            ResourceLocation path = Helpers.identifier("dynamic/legend/terrain_height");
            mc.getTextureManager().register(path, texture);
            type = RenderType.text(path);
        }
        return type;
    }

    @Override
    public void render(PoseStack stack, int i, int j, float k) {
        if(legend == null) getLegend();

        int height = legend.getHeight();
        stack.translate(-28, -(height + 8), 0);

        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        RenderHelper.fillRect(buffers, stack.last().pose(), 28, height + 8, 0xA0000000);

        stack.pushPose();
        stack.translate(16, 4, 0);
        RenderHelper.drawQuad(buffers.getBuffer(getLegend()), stack.last().pose(), 10, height);
        stack.popPose();

        var font = Minecraft.getInstance().font;
        stack.pushPose();
        stack.translate(0, 2, 0);
        stack.scale(0.5F, 0.5F, 1);
        for(int y = max; y >= min; y -= 64) {
            String label = y + "";
            stack.pushPose();
            stack.translate(28 - font.width(label), 0, 0);
            font.drawInBatch(label, 0, 0, Colors.WHITE, false, stack.last().pose(), buffers, false, 0, LightTexture.FULL_BRIGHT);
            stack.popPose();
            stack.translate(0, 32, 0);
        }
        stack.popPose();

        buffers.endBatch();
    }
}
