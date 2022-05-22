package com.eerussianguy.blazemap.util;

public class Profilers {
    public static class Engine {
        public static final Profiler.TimeProfiler COLLECTOR_TIME_PROFILER = new Profiler.TimeProfiler(20);
        public static final Profiler.LoadProfiler COLLECTOR_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
        public static final Profiler.TimeProfiler LAYER_TIME_PROFILER = new Profiler.TimeProfiler(20);
        public static final Profiler.LoadProfiler LAYER_LOAD_PROFILER = new Profiler.LoadProfiler(20, 50);
        public static final Profiler.TimeProfiler REGION_TIME_PROFILER = new Profiler.TimeProfiler(60);
        public static final Profiler.LoadProfiler REGION_LOAD_PROFILER = new Profiler.LoadProfiler(60, 1000);
    }

    public static class Minimap {
        public static final Profiler.TimeProfiler DRAW_TIME_PROFILER = new Profiler.TimeProfiler(60);
        public static final Profiler.TimeProfiler DEBUG_TIME_PROFILER = new Profiler.TimeProfiler(60);
        public static final Profiler.TimeProfiler TEXTURE_TIME_PROFILER = new Profiler.TimeProfiler(60);
        public static final Profiler.LoadProfiler TEXTURE_LOAD_PROFILER = new Profiler.LoadProfiler(60, 16);
    }
}
