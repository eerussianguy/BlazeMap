package com.eerussianguy.blazemap.feature.maps;

public enum MinimapZoom {
    SHORT(0.75F),
    NEAR(0.50F),
    MEDIUM(0.25F),
    FAR(0.00F);

    public static final MinimapZoom[] VALUES = values();

    private static MinimapZoom byId(int id)
    {
        return id >= VALUES.length ? SHORT : VALUES[id];
    }

    public final float trim;

    MinimapZoom(float trim) {
        this.trim = trim / 2F;
    }

    public MinimapZoom next()
    {
        return byId(ordinal() + 1);
    }
}
