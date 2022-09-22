package com.eerussianguy.blazemap.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

public abstract class BlazeGui extends Screen {
    private static final TextComponent EMPTY = new TextComponent("");
    public static final ResourceLocation SLOT = Helpers.identifier("textures/gui/slot.png");
    public static final ResourceLocation GUI = Helpers.identifier("textures/gui/gui.png");

    protected final RenderType background, slot;
    protected int guiWidth, guiHeight;
    protected int left, top;

    protected BlazeGui(int guiWidth, int guiHeight) {
        this(EMPTY, guiWidth, guiHeight);
    }

    protected BlazeGui(@Nonnull Component title, int guiWidth, int guiHeight) {
        super(title);
        this.minecraft = Minecraft.getInstance();
        this.guiWidth = guiWidth;
        this.guiHeight = guiHeight;
        this.background = RenderType.text(GUI);
        this.slot = RenderType.text(SLOT);
    }

    @Override
    protected void init() {
        super.init();
        this.left = (width - guiWidth) / 2;
        this.top = (height - guiHeight) / 2;
    }

    @Override
    public void render(PoseStack stack, int i0, int i1, float f0) {
        renderBackground(stack);

        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        renderFrame(stack, buffers);
        stack.translate(left, top, 0.05F);
        if(title != EMPTY) {
            renderLabel(stack, buffers, title, 12, 12, true);
        }
        renderComponents(stack, buffers);
        stack.popPose();
        buffers.endBatch();

        stack.pushPose();
        stack.translate(0, 0, 0.1F);
        super.render(stack, i0, i1, f0);
        stack.popPose();
    }

    protected void renderFrame(PoseStack stack, MultiBufferSource buffers) {
        stack.pushPose();
        stack.translate(left, top, 0);
        RenderHelper.drawFrame(buffers.getBuffer(background), stack, guiWidth, guiHeight, 8);
        stack.popPose();
    }

    protected abstract void renderComponents(PoseStack stack, MultiBufferSource buffers);

    protected void renderLabel(PoseStack stack, MultiBufferSource buffers, Component text, int x, int y, boolean shadow) {
        this.font.drawInBatch(text, x, y, shadow ? Colors.WHITE : Colors.LABEL_COLOR, shadow, stack.last().pose(), buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    protected void renderSlot(PoseStack stack, MultiBufferSource buffers, int x, int y, int width, int height) {
        stack.pushPose();
        stack.translate(x, y, 0);
        RenderHelper.drawFrame(buffers.getBuffer(slot), stack, width, height, 1);
        stack.popPose();
    }
}
