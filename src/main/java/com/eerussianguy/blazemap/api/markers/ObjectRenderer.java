package com.eerussianguy.blazemap.api.markers;

import net.minecraft.client.renderer.MultiBufferSource;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.mojang.blaze3d.vertex.PoseStack;

public interface ObjectRenderer<T> extends BlazeRegistry.RegistryEntry {
    void render(T object, PoseStack stack, MultiBufferSource buffers, double zoom, SearchTargeting search);

    BlazeRegistry.Key<ObjectRenderer<?>> getID();
}
