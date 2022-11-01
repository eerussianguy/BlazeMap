package com.eerussianguy.blazemap.util;

public class Profilers {
    public static final Profiler.TimeProfilerSync DEBUG_TIME_PROFILER = new Profiler.TimeProfilerSync(60);

    public static class Server {
        public static final Profiler.TimeProfilerSync COLLECTOR_TIME_PROFILER = new Profiler.TimeProfilerSync(20);
        public static final Profiler.LoadProfiler COLLECTOR_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
        public static final Profiler.TimeProfilerAsync PROCESSOR_TIME_PROFILER = new Profiler.TimeProfilerAsync(20);
        public static final Profiler.LoadProfiler PROCESSOR_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
        public static final Profiler.TimeProfilerAsync TRANSFORMER_TIME_PROFILER = new Profiler.TimeProfilerAsync(20);
        public static final Profiler.LoadProfiler TRANSFORMER_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
    }

    public static class Client {
        public static final Profiler.TimeProfilerSync COLLECTOR_TIME_PROFILER = new Profiler.TimeProfilerSync(20);
        public static final Profiler.LoadProfiler COLLECTOR_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
        public static final Profiler.TimeProfilerAsync PROCESSOR_TIME_PROFILER = new Profiler.TimeProfilerAsync(20);
        public static final Profiler.LoadProfiler PROCESSOR_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
        public static final Profiler.TimeProfilerAsync TRANSFORMER_TIME_PROFILER = new Profiler.TimeProfilerAsync(20);
        public static final Profiler.LoadProfiler TRANSFORMER_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
        public static final Profiler.TimeProfilerAsync LAYER_TIME_PROFILER = new Profiler.TimeProfilerAsync(20);
        public static final Profiler.LoadProfiler LAYER_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
        public static final Profiler.TimeProfilerAsync TILE_TIME_PROFILER = new Profiler.TimeProfilerAsync(60);
        public static final Profiler.LoadProfiler TILE_LOAD_PROFILER = new Profiler.LoadProfiler(60, 1000);
    }

    public static class Minimap {
        public static final Profiler.TimeProfilerSync DRAW_TIME_PROFILER = new Profiler.TimeProfilerSync(60);
        public static final Profiler.TimeProfilerSync TEXTURE_TIME_PROFILER = new Profiler.TimeProfilerSync(60);
        public static final Profiler.LoadProfiler TEXTURE_LOAD_PROFILER = new Profiler.LoadProfiler(60, 16);
    }
}
