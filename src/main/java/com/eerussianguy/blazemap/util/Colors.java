package com.eerussianguy.blazemap.util;

public class Colors {
    public static int layerBlend(int bottom, int top){
        if((top & 0xFF000000) == 0xFF000000) return top; // top is opaque, use top
        if((top & 0xFF000000) == 0) return bottom; // top is transparent, use bottom
        if((bottom & 0xFF000000) == 0) return top; // bottom is transparent, use top

        return 0xFF000000; // TODO: implement proper color blending
    }

    public static int interpolate(int color1, float key1, int color2, float key2, float point){
        point = (point - key1) / (key2 - key1);
        int b0 = interpolate((color1 >> 24) & 0xFF, (color2 >> 24) & 0xFF, point);
        int b1 = interpolate((color1 >> 16) & 0xFF, (color2 >> 16) & 0xFF, point);
        int b2 = interpolate((color1 >>  8) & 0xFF, (color2 >>  8) & 0xFF, point);
        int b3 = interpolate(color1 & 0xFF, color2 & 0xFF, point);
        return b0 << 24 | b1 << 16 | b2 << 8 | b3;
    }

    public static int interpolate(int a, int b, float p){
        a *= (1F-p);
        b *= p;
        return Math.max(0, Math.min(255, a + b));
    }
}
