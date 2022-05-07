package com.eerussianguy.blazemap.api.event;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

import com.eerussianguy.blazemap.api.util.LayerRegion;
import com.eerussianguy.blazemap.api.util.RegionPos;

/**
 * Fired after the client enters a new dimension. <br>
 * <br>
 * Is triggered by: <br>
 * - PlayerEvent.PlayerChangedDimensionEvent <br>
 * - ClientPlayerNetworkEvent.LoggedInEvent <br>
 * <br>
 * However, when fired, the Blaze Map engine is ready to operate in this new dimension,
 * meaning code wanting to interact with Blaze Map in response to a dimension change
 * should use this event instead of the aforementioned ones.
 *
 * @author LordFokas
 */
public class DimensionChangedEvent extends Event {
    /**
     * The new dimension
     */
    public final ResourceKey<Level> dimension;

    /**
     * Set of map types available in the new dimension
     */
    public final Set<ResourceLocation> availableMapTypes;

    /**
     * Set of map layers available in the new dimension
     */
    public final Set<ResourceLocation> availableLayers;

    /**
     * Allows setting listeners to tile change notifications for this dimension <br>
     * Listeners do not persist between dimensions or visits to the same dimension and must be ser every time
     */
    public final DimensionTileNotifications tileNotifications;

    /**
     * Storage containing all rendered map layer images
     */
    public final DimensionTileStorage tileStorage;

    /**
     * Directory in the client where all map data for this dimension is stored
     */
    public final File dimensionStorageDir;

    public DimensionChangedEvent(ResourceKey<Level> dimension, Set<ResourceLocation> mapTypes, Set<ResourceLocation> layers, DimensionTileNotifications notifications, DimensionTileStorage tiles, File dir) {
        this.dimension = dimension;
        this.availableMapTypes = mapTypes;
        this.availableLayers = layers;
        this.tileNotifications = notifications;
        this.tileStorage = tiles;
        this.dimensionStorageDir = dir;
    }


    @FunctionalInterface
    public interface DimensionTileStorage {
        /**
         * Gives access to a Region's section of a specific map layer. <br>
         * It is a 512x512 32-bit ARGB image where each pixel represents 1 block in-world.
         * <br>
         * <br>
         * <b>  WARNING!  </b><br>
         * <br>
         * In order to ensure thread safety please do all BufferedImage related processing inside the Consumer code. <br>
         * <b>DO NOT</b> attempt to save a reference to the image to handle it later.<br>
         * <br>
         * You have been warned.<br>
         * <br>
         *
         * @throws IllegalArgumentException if the layer is not in the availableLayers Set.
         * @author LordFokas
         */
        void consumeTile(ResourceLocation layer, RegionPos region, Consumer<BufferedImage> consumer);
    }

    @FunctionalInterface
    public interface DimensionTileNotifications {
        /**
         * Add a listener to be notified when a Region's section of a specific map layer changes.
         *
         * @author LordFokas
         */
        void addUpdateListener(Consumer<LayerRegion> listener);
    }
}
