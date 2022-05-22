package com.eerussianguy.blazemap.feature.maps;

public enum MinimapSize {
    SMALL(1.00F),
    MEDIUM(1.25F),
    LARGE(1.50F),
    HUGE(2.00F);

    public final float scale;

    MinimapSize(float scale) {
        this.scale = scale;
    }
}
