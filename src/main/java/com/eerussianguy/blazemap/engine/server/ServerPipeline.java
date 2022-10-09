package com.eerussianguy.blazemap.engine.server;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.event.ServerPipelineInitEvent;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDataDispatcher;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;
import com.eerussianguy.blazemap.api.pipeline.PipelineType;
import com.eerussianguy.blazemap.engine.*;
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

    private final StorageAccess.Internal storage;
    private final StorageAccess addonStorage;
    private final MasterDataDispatcher dispatcher;

    public ServerPipeline(AsyncChain.Root async, DebouncingThread debouncer, ResourceKey<Level> dimension, Supplier<Level> level, StorageAccess.Internal storage) {
        super(
            async, debouncer, SERVER_PIPELINE_PROFILER, dimension, level,
            BlazeMapAPI.COLLECTORS.keys().stream().filter(k -> k.value().shouldExecuteIn(dimension, PipelineType.SERVER)).collect(Collectors.toUnmodifiableSet()),
            BlazeMapAPI.TRANSFORMERS.keys().stream().filter(k -> k.value().shouldExecuteIn(dimension, PipelineType.SERVER)).collect(Collectors.toUnmodifiableSet()),
            BlazeMapAPI.PROCESSORS.keys().stream().filter(k -> k.value().shouldExecuteIn(dimension, PipelineType.SERVER)).collect(Collectors.toUnmodifiableSet())
        );

        this.storage = storage;
        this.addonStorage = storage.addon();

        ServerPipelineInitEvent event = new ServerPipelineInitEvent(dimension, addonStorage, this::dispatch);
        MinecraftForge.EVENT_BUS.post(event);
        this.dispatcher = event.getDispatcher();
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void onPipelineOutput(ChunkPos pos, Set<Key<DataType>> diff, MapView view, ChunkMDCache cache) {
        dispatcher.dispatch(dimension, pos, cache.data(), UnsafeGenerics.mdKeys(diff), BlazeMapServerEngine.getMDSource(), level.get().getChunk(pos.x, pos.z));
    }

    private void dispatch(ResourceKey<Level> dimension, ChunkPos pos, List<MasterDatum> data, Set<BlazeRegistry.Key<DataType<MasterDatum>>> diff, String source, LevelChunk chunk) {
        new PacketChunkMDUpdate(dimension, pos, data, source).send(chunk);
    }
}
