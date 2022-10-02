package com.eerussianguy.blazemap.engine;

import java.util.*;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.pipeline.*;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.engine.async.AsyncChain;
import com.eerussianguy.blazemap.engine.async.DebouncingDomain;
import com.eerussianguy.blazemap.engine.async.DebouncingThread;

import static com.eerussianguy.blazemap.engine.UnsafeGenerics.*;

// If you're not a fan of unchecked casts, raw generics, cheesy unsafe generics double casts through Object and so on,
// this is not a safe place for you. The code in here needs to be succinct, and we cannot afford to write a dozen type
// parameterizations every single line, it would be unreadable and hard to maintain.
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class Pipeline {
    protected static final ThreadLocal<MapView> MAP_VIEWS = ThreadLocal.withInitial(MapView::new);

    private final PipelineProfiler profiler;
    protected final AsyncChain.Root async;
    protected final DebouncingDomain<ChunkPos> dirtyChunks;
    public final ResourceKey<Level> dimension;
    protected final Supplier<Level> level;
    protected final Set<Key<Collector>> availableCollectors;
    protected final Set<Key<Transformer>> availableTransformers;
    protected final Set<Key<Processor>> availableProcessors;
    private final Collector<MasterDatum>[] collectors;
    private final List<Transformer> transformers;
    private final List<Processor> processors;
    public final int numCollectors, numProcessors, numTransformers;


    protected Pipeline(
        AsyncChain.Root async, DebouncingThread debouncer, PipelineProfiler profiler,
        ResourceKey<Level> dimension, Supplier<Level> level,
        Set<Key<Collector<MasterDatum>>> availableCollectors,
        Set<Key<Transformer<MasterDatum>>> availableTransformers,
        Set<Key<Processor>> availableProcessors
    ) {
        this.async = async;
        this.dirtyChunks = new DebouncingDomain<>(this::begin, 500, 5000);
        this.profiler = profiler;

        this.dimension = dimension;
        this.level = level;

        this.availableCollectors = stripCollectors(availableCollectors);
        this.availableTransformers = stripTransformers(availableTransformers);
        this.availableProcessors = availableProcessors;

        debouncer.add(dirtyChunks);
        collectors = availableCollectors.stream().map(Key::value).toArray(Collector[]::new);
        transformers = this.availableTransformers.stream().map(Key::value).toList();
        processors = this.availableProcessors.stream().map(Key::value).toList();

        numCollectors = collectors.length;
        numTransformers = transformers.size();
        numProcessors = processors.size();
    }


    // =================================================================================================================
    // Pipeline IO
    public void onChunkChanged(ChunkPos pos) {
        dirtyChunks.push(pos);
    }

    public void insertMasterData(ChunkPos pos, List<MasterDatum> data) {
        async.runOnDataThread(() -> processMasterData(pos, data));
    }

    protected abstract void onPipelineOutput(ChunkPos pos, Set<Key<DataType>> diff, MapView view);


    // =================================================================================================================
    // Pipeline internals
    private void begin(ChunkPos pos) {
        async.startOnGameThread($ -> this.runCollectors(pos))
            .thenOnDataThread(data -> this.processMasterData(pos, data))
            .start();
    }

    // TODO: figure out why void gives generic errors but null Void is OK. Does it have to be an Object?
    private Void processMasterData(ChunkPos pos, List<MasterDatum> collectedData) {
        if(collectedData.size() == 0) return null;
        ChunkMDCache cache = new ChunkMDCache(); // FIXME: load chunk MD cache from disk
        MapView view = MAP_VIEWS.get().setSource(cache);

        // Diff collected data
        Set<Key<DataType>> diff = new HashSet<>();
        diffMD(collectedData, cache, diff);

        List<MasterDatum> transformedData = this.runTransformers(diff, view);

        // Diff transformed data
        diffMD(transformedData, cache, diff);

        // Cache MD
        cache.persist(); // TODO: double check that persistence is done

        this.onPipelineOutput(pos, diff, view);
        this.runProcessors(pos, diff, view);
        return null;
    }

    private static void diffMD(List<MasterDatum> data, ChunkMDCache cache, Set<Key<DataType>> diff) {
        for(MasterDatum md : data) {
            if(cache.diff(md)) {
                diff.add(stripKey(md.getID()));
            }
        }
    }


    // =================================================================================================================
    // Pipeline execution steps
    private List<MasterDatum> runCollectors(ChunkPos pos) {
        try {
            profiler.collectorLoad.hit();
            profiler.collectorTime.begin();
            Level level = this.level.get();
            if(!level.getChunkSource().hasChunk(pos.x, pos.z)) return Collections.EMPTY_LIST;

            int x0 = pos.getMinBlockX();
            int x1 = pos.getMaxBlockX();
            int z0 = pos.getMinBlockZ();
            int z1 = pos.getMaxBlockZ();

            List<MasterDatum> data = new ArrayList<>(32);
            for(Collector collector : collectors) {
                data.add(collector.collect(level, x0, z0, x1, z1));
            }
            return data;
        }
        finally {
            profiler.collectorTime.end();
        }
    }

    private List<MasterDatum> runTransformers(Set<Key<DataType>> diff, MapView view) {
        Transformer[] transformers = this.transformers.stream().filter(t -> !Collections.disjoint(t.getInputIDs(), diff)).toArray(Transformer[]::new);
        if(transformers.length == 0) return Collections.EMPTY_LIST;

        try {
            profiler.transformerLoad.hit();
            profiler.transformerTime.begin();
            List<MasterDatum> data = new ArrayList<>(32);
            for(Transformer transformer : transformers) {
                view.setFilter(transformer.getInputIDs());
                data.add(transformer.transform(view));
            }
            return data;
        }
        finally {
            profiler.transformerTime.end();
        }
    }

    private void runProcessors(ChunkPos chunk, Set<Key<DataType>> diff, MapView view) {
        Processor[] processors = this.processors.stream().filter(p -> !Collections.disjoint(p.getInputIDs(), diff)).toArray(Processor[]::new);
        if(processors.length == 0) return;

        try {
            profiler.processorLoad.hit();
            profiler.processorTime.begin();
            RegionPos region = new RegionPos(chunk);
            for(Processor processor : processors) {
                view.setFilter(stripKeys(processor.getInputIDs()));
                processor.execute(dimension, region, chunk, view);
            }
        }
        finally {
            profiler.processorTime.end();
        }
    }
}
