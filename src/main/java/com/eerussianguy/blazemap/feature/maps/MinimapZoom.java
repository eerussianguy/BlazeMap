package com.eerussianguy.blazemap.feature.maps;

public enum MinimapZoom {
    SHORT(0.75F),
    NEAR(0.50F),
    MEDIUM(0.25F),
    FAR(0.00F);

    public final float trim;

    MinimapZoom(float trim) {
        this.trim = trim / 2F;
    }
}
