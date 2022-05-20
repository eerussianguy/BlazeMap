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
        private boolean populated = false;
        private long start;

        public TimeProfiler(int rollSize) {
            this.roll = new long[rollSize];
        }

        public void begin() {
            start = System.nanoTime();
        }

        public void end() {
            if(populated) {
                roll[idx] = System.nanoTime() - start;
                idx = (idx + 1) % roll.length;
                recalculate();
            }
            else {
                long delta = System.nanoTime() - start;
                for(int i = 0; i < roll.length; i++) roll[i] = delta;
                synchronized(this) {
                    avg = min = max = delta;
                }
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
