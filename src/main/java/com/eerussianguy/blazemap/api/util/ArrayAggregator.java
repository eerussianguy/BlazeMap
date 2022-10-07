package com.eerussianguy.blazemap.api.util;

public class ArrayAggregator {

    public static int avgColor(int[] colors) {
        int b0 = 0, b1 = 0, b2 = 0, b3 = 0;

        for(int color : colors) {
            b0 += (color >> 24) & 0xFF;
            b1 += (color >> 16) & 0xFF;
            b2 += (color >> 8) & 0xFF;
            b3 += color & 0xFF;
        }

        final int count = colors.length;
        b0 /= count;
        b1 /= count;
        b2 /= count;
        b3 /= count;

        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    public static int avg(int[] values) {
        int sum = 0;
        for(int value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    public static int max(int[] values) {
        int max = Integer.MIN_VALUE;
        for(int value : values) {
            if(value > max) {
                max = value;
            }
        }
        return max;
    }
}
