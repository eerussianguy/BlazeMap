package com.eerussianguy.blazemap.util;

import java.util.Arrays;

public abstract class Profiler {
    protected long[] roll;
    protected long min, max;
    protected double avg;
    protected int idx;

    public synchronized double getAvg() {
        return avg;
    }

    public synchronized double getMin() {
        return min;
    }

    public synchronized double getMax() {
        return max;
    }

    protected void recalculate() {
        double sum = 0;
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for(long v : roll) {
            if(v < min) min = v;
            if(v > max) max = v;
            sum += v;
        }

        synchronized(this) {
            this.avg = sum / roll.length;
            this.min = min;
            this.max = max;
        }
    }

    public static abstract class TimeProfiler extends Profiler {
        protected boolean populated = false;

        public abstract void begin();

        public abstract void end();

        public static class Dummy extends TimeProfiler {
            @Override
            public void begin() {}

            @Override
            public void end() {}
        }
    }

    public static class TimeProfilerSync extends TimeProfiler {
        private long start;

        public TimeProfilerSync(int rollSize) {
            this.roll = new long[rollSize];
        }

        @Override
        public void begin() {
            start = System.nanoTime();
        }

        @Override
        public void end() {
            if(populated) {
                roll[idx] = System.nanoTime() - start;
                idx = (idx + 1) % roll.length;
                recalculate();
            }
            else {
                long delta = System.nanoTime() - start;
                Arrays.fill(roll, delta);
                synchronized(this) {
                    avg = min = max = delta;
                }
                populated = true;
            }
        }
    }

    public static class TimeProfilerAsync extends TimeProfiler {
        private final ThreadLocal<Long> start = new ThreadLocal<>();

        public TimeProfilerAsync(int rollSize) {
            this.roll = new long[rollSize];
        }

        @Override
        public void begin() {
            start.set(System.nanoTime());
        }

        @Override
        public synchronized void end() {
            if(populated) {
                roll[idx] = System.nanoTime() - start.get();
                idx = (idx + 1) % roll.length;
                recalculate();
            }
            else {
                long delta = System.nanoTime() - start.get();
                Arrays.fill(roll, delta);
                avg = min = max = delta;
                populated = true;
            }
        }
    }

    public static class LoadProfiler extends Profiler {
        public final int interval;
        public final String unit;
        private long last;

        public LoadProfiler(int rollSize, int interval) {
            this.roll = new long[rollSize];
            this.interval = interval;
            switch(interval) {
                case 16 -> this.unit = "f";
                case 50 -> this.unit = "t";
                case 1000 -> this.unit = "s";
                default -> this.unit = "?";
            }
        }

        public void hit() {
            update(1);
        }

        public void ping() {
            update(0);
        }

        private synchronized void update(int i) {
            long now = System.currentTimeMillis() / interval;
            if(now == last) {
                if(i == 0) return;
                roll[idx] += i;
            }
            else {
                idx = (idx + 1) % roll.length;
                roll[idx] = i;
                last = now;
            }
            recalculate();
        }
    }
}
