package com.eerussianguy.blazemap.engine.server;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.engine.ChunkMDCache;
import com.eerussianguy.blazemap.engine.MapView;
import com.eerussianguy.blazemap.engine.Pipeline;
import com.eerussianguy.blazemap.engine.PipelineProfiler;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;
import com.eerussianguy.blazemap.network.PacketChunkMDUpdate;

import static com.eerussianguy.blazemap.util.Profilers.Server.*;

class ServerPipeline extends Pipeline {
    private static final PipelineProfiler SERVER_PIPELINE_PROFILER = new PipelineProfiler(
        COLLECTOR_TIME_PROFILER,
        COLLECTOR_LOAD_PROFILER,
        TRANSFORMER_TIME_PROFILER,
        TRANSFORMER_LOAD_PROFILER,
        PROCESSOR_TIME_PROFILER,
        PROCESSOR_LOAD_PROFILER
    );


    public ServerPipeline(AsyncChain.Root async, DebouncingThread debouncer, ResourceKey<Level> dimension, Supplier<Level> level) {
        super(
            async, debouncer, SERVER_PIPELINE_PROFILER, dimension, level,
            BlazeMapAPI.COLLECTORS.keys().stream().collect(Collectors.toUnmodifiableSet()),
            BlazeMapAPI.TRANSFORMERS.keys().stream().filter(k -> k.value().shouldExecuteInDimension(dimension)).collect(Collectors.toUnmodifiableSet()),
            BlazeMapAPI.PROCESSORS.keys().stream().filter(k -> k.value().shouldExecuteInDimension(dimension)).collect(Collectors.toUnmodifiableSet())
        );
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void onPipelineOutput(ChunkPos pos, Set<Key<DataType>> diff, MapView view, ChunkMDCache cache) {
        new PacketChunkMDUpdate(dimension, pos, cache.data()).send(level.get().getChunk(pos.x, pos.z));
    }
}
