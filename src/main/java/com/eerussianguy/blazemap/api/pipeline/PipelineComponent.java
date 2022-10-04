package com.eerussianguy.blazemap.api.pipeline;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface PipelineComponent {
    default boolean shouldExecuteIn(ResourceKey<Level> dimension, PipelineType pipeline) {
        return pipeline.isServer || pipeline.isStandalone;
    }
}