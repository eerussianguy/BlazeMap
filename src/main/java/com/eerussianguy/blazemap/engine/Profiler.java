package com.eerussianguy.blazemap.engine;

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

    public static class TimeProfiler extends Profiler {
        private long start;

        public TimeProfiler(int rollSize) {
            this.roll = new long[rollSize];
        }

        public void begin() {
            start = System.nanoTime();
        }

        public void end() {
            roll[idx] = System.nanoTime() - start;
            idx = (idx + 1) % roll.length;
            recalculate();
        }
    }

    public static class LoadProfiler extends Profiler {
        private final int interval;
        private long last;

        public LoadProfiler(int rollSize, int interval) {
            this.roll = new long[rollSize];
            this.interval = interval;
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
