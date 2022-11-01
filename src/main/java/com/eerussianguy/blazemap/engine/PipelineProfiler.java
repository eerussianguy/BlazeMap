package com.eerussianguy.blazemap.engine;

import com.eerussianguy.blazemap.util.Profiler;

public class PipelineProfiler {
    public final Profiler.TimeProfilerSync collectorTime;
    public final Profiler.LoadProfiler collectorLoad;
    public final Profiler.TimeProfilerAsync transformerTime;
    public final Profiler.LoadProfiler transformerLoad;
    public final Profiler.TimeProfilerAsync processorTime;
    public final Profiler.LoadProfiler processorLoad;

    public PipelineProfiler(
        Profiler.TimeProfilerSync collectorTime,
        Profiler.LoadProfiler collectorLoad,
        Profiler.TimeProfilerAsync transformerTime,
        Profiler.LoadProfiler transformerLoad,
        Profiler.TimeProfilerAsync processorTime,
        Profiler.LoadProfiler processorLoad
    ) {
        this.collectorTime = collectorTime;
        this.collectorLoad = collectorLoad;
        this.transformerTime = transformerTime;
        this.transformerLoad = transformerLoad;
        this.processorTime = processorTime;
        this.processorLoad = processorLoad;
    }
}
