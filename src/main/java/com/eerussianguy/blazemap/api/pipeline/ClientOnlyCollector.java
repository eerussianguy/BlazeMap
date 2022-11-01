package com.eerussianguy.blazemap.api.pipeline;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry;

/**
 * Special type of collector that runs exclusively in client pipelines, even when the master data is being sent from
 * the server. Like with FakeLayer, the engine special cases instances of this specific class, so just replicating the
 * behavior is not enough to achieve the same results.
 */
public abstract class ClientOnlyCollector<T extends MasterDatum> extends Collector<T> {
    public ClientOnlyCollector(BlazeRegistry.Key<Collector<MasterDatum>> id, BlazeRegistry.Key<DataType<MasterDatum>> output) {
        super(id, output);
    }

    @Override
    public final boolean shouldExecuteIn(ResourceKey<Level> dimension, PipelineType pipeline) {
        return shouldExecuteIn(dimension) && pipeline.isClient;
    }

    protected boolean shouldExecuteIn(ResourceKey<Level> dimension) {
        return true;
    }
}
