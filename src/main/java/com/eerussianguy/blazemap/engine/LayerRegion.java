package com.eerussianguy.blazemap.engine;

import java.util.Objects;

import net.minecraft.resources.ResourceLocation;

public class LayerRegion {
    public final ResourceLocation layer;
    public final RegionPos region;

    public LayerRegion(ResourceLocation layer, RegionPos region) {
        this.layer = layer;
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        LayerRegion that = (LayerRegion) o;
        return Objects.equals(layer, that.layer) && Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(layer, region);
    }
}
