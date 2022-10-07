package com.eerussianguy.blazemap.api.maps;

import java.util.function.Consumer;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.mojang.blaze3d.platform.NativeImage;

@FunctionalInterface
public interface DimensionTileStorage {
    /**
     * Gives access to a Region's section of a specific map layer. <br>
     * It is a 32-bit ABGR image where each pixel represents an NxN square of blocks in world.
     * The exact size of the image depends on the given TileResolution.
     * <br>
     * <br>
     * <b>  WARNING!  </b><br>
     * <br>
     * In order to ensure thread safety please do all NativeImage related processing inside the Consumer code. <br>
     * <b>DO NOT</b> attempt to save a reference to the image to handle it later.<br>
     * <br>
     * You have been warned.<br>
     * <br>
     *
     * @throws IllegalArgumentException if the layer is not in the availableLayers Set.
     * @author LordFokas
     */
    void consumeTile(BlazeRegistry.Key<Layer> layer, RegionPos region, TileResolution resolution, Consumer<NativeImage> consumer);
}