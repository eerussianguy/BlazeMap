package com.eerussianguy.blazemap.feature.maps;

import com.eerussianguy.blazemap.util.Helpers;

public enum MinimapZoom {
    SHORT(0.75F),
    NEAR(0.50F),
    MEDIUM(0.25F),
    FAR(0.00F);

    public static final MinimapZoom[] VALUES = values();

    private static MinimapZoom byId(int id) {
        return VALUES[Helpers.clamp(0, id, VALUES.length - 1)];
    }

    public final float trim;

    MinimapZoom(float trim) {
        this.trim = trim / 2F;
    }

    public MinimapZoom next() {
        return byId(ordinal() + 1);
    }

    public MinimapZoom prev() {
        return byId(ordinal() - 1);
    }
}
