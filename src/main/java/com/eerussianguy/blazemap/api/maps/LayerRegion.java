package com.eerussianguy.blazemap.api.maps;

import java.util.Objects;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.util.RegionPos;

public class LayerRegion {
    public final BlazeRegistry.Key<Layer> layer;
    public final RegionPos region;

    public LayerRegion(BlazeRegistry.Key<Layer> layer, RegionPos region) {
        this.layer = layer;
        this.region = region;
    }

    @Override
    public int hashCode() {
        return Objects.hash(layer, region);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        LayerRegion that = (LayerRegion) o;
        return Objects.equals(layer, that.layer) && Objects.equals(region, that.region);
    }
}
