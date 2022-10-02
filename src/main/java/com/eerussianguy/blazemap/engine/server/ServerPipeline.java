package com.eerussianguy.blazemap.engine.server;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.pipeline.*;
import com.eerussianguy.blazemap.engine.MapView;
import com.eerussianguy.blazemap.engine.Pipeline;
import com.eerussianguy.blazemap.engine.PipelineProfiler;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;

class ServerPipeline extends Pipeline {


    protected ServerPipeline(
        AsyncChain.Root async, DebouncingThread debouncer, PipelineProfiler profiler,
        ResourceKey<Level> dimension, Supplier<Level> level
    ) {
        super(
            async, debouncer, profiler, dimension, level,
            BlazeMapAPI.COLLECTORS.keys().stream().collect(Collectors.toUnmodifiableSet()),
            BlazeMapAPI.TRANSFORMERS.keys().stream().filter(k -> k.value().shouldExecuteInDimension(dimension)).collect(Collectors.toUnmodifiableSet()),
            BlazeMapAPI.PROCESSORS.keys().stream().filter(k -> k.value().shouldExecuteInDimension(dimension)).collect(Collectors.toUnmodifiableSet())
        );
    }

    @Override
    protected void onPipelineOutput(ChunkPos pos, Set<Key<DataType>> diff, MapView view) {
        // TODO: send chunk MD snapshot
    }
}
