package com.eerussianguy.blazemap.api.markers;

import net.minecraft.client.renderer.MultiBufferSource;

import com.mojang.blaze3d.vertex.PoseStack;

public interface ObjectRenderer<T> {
    void render(T object, PoseStack stack, MultiBufferSource buffers, double zoom);
}
