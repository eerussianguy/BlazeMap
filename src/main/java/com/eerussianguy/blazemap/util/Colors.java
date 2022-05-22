package com.eerussianguy.blazemap.util;

public class Colors {
    public static int layerBlend(int bottom, int top){
        if((top & 0xFF000000) == 0xFF000000) return top; // top is opaque, use top
        if((top & 0xFF000000) == 0) return bottom; // top is transparent, use bottom
        if((bottom & 0xFF000000) == 0) return top; // bottom is transparent, use top

        return 0xFF000000; // TODO: implement proper color blending
    }
}
