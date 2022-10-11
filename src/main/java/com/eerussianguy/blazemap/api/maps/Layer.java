package com.eerussianguy.blazemap.api.maps;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.BlazeRegistry.RegistryEntry;
import com.eerussianguy.blazemap.api.pipeline.Consumer;
import com.eerussianguy.blazemap.api.pipeline.DataType;
import com.eerussianguy.blazemap.api.pipeline.MasterDatum;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.mojang.blaze3d.platform.NativeImage;

/**
 * In Blaze Map, maps are composed of several layers superimposed on each others.
 * Layers read one or more MasterDatum objects previously collected by Collectors and use it
 * to generate a layer image for the respective chunk. These images are later stitched together
 * by the engine to generate a layer image for the whole region (LayerRegionTile).
 *
 * All operations are thread-safe by default (read data, paint image) and are executed in parallel
 * in the engine's background threads. Layers are meant exclusively to generate map tiles, for other
 * forms of data processing and analysis please use a Processor instead.
 *
 * @author LordFokas
 */
public abstract class Layer implements RegistryEntry, Consumer {
    protected static final int OPAQUE = 0xFF000000;

    private final Key<Layer> id;
    private final Set<Key<DataType<MasterDatum>>> inputs;
    private final TranslatableComponent name;
    private final ResourceLocation icon;
    private final boolean opaque;

    @SafeVarargs
    public Layer(Key<Layer> id, TranslatableComponent name, Key<DataType<MasterDatum>>... inputs) {
        this.id = id;
        this.name = name;
        this.icon = null;
        this.inputs = Arrays.stream(inputs).collect(Collectors.toUnmodifiableSet());
        this.opaque = true;
    }

    @SafeVarargs
    public Layer(Key<Layer> id, TranslatableComponent name, ResourceLocation icon, Key<DataType<MasterDatum>>... inputs) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.inputs = Arrays.stream(inputs).collect(Collectors.toUnmodifiableSet());
        this.opaque = false;
    }

    public Key<Layer> getID() {
        return id;
    }

    @Override
    public Set<Key<DataType<MasterDatum>>> getInputIDs() {
        return inputs;
    }

    public boolean shouldRenderInDimension(ResourceKey<Level> dimension) {
        return true;
    }

    public final boolean isOpaque() {
        return opaque;
    }

    public abstract boolean renderTile(NativeImage tile, TileResolution resolution, IDataSource data, int xGridOffset, int zGridOffset);

    public TranslatableComponent getName() {
        return name;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    /**
     * Used by the World Map (fullscreen map) to display a legend in the bottom right corner.
     * The widget will be asked to render while translated to the corner of the screen,
     * so it must render backwards (towards the left and up) in order to stay inside the screen.
     *
     * The translation to the corner may subtract a small margin to make all legends have a consistent margin with the border.
     *
     * This only applies to opaque (bottom) layers, which are the first layer of the current map type,
     * however not all such layers must have one and returning null is the default action.
     */
    public Widget getLegendWidget() {
        return null;
    }


    // =================================================================================================================
    // Common helper functions for easier layer rendering

    /**
     * Allows to run code once for each pixel of the chunk tile.
     * Automatically accounts for the fact chunk tile sizes vary with resolution.
     */
    protected static void foreachPixel(TileResolution resolution, PixelConsumer consumer) {
        for(int x = 0; x < resolution.chunkWidth; x++) {
            for(int z = 0; z < resolution.chunkWidth; z++) {
                consumer.accept(x, z);
            }
        }
    }

    @FunctionalInterface
    protected interface PixelConsumer {
        void accept(int x, int y);
    }

    /**
     * When running at lower resolutions than FULL, this utility allows to collect all data points that will be rendered
     * into a single pixel. Meant to be used with ArrayAggregator or similar utilities, to aggregate the multiple data
     * points into a single value.
     */
    protected static int[] relevantData(TileResolution resolution, int x, int z, int[][] data) {
        int[] objects = new int[resolution.pixelWidth * resolution.pixelWidth];
        x *= resolution.pixelWidth;
        z *= resolution.pixelWidth;
        int idx = 0;

        for(int dx = 0; dx < resolution.pixelWidth; dx++) {
            for(int dz = 0; dz < resolution.pixelWidth; dz++) {
                objects[idx++] = data[dx + x][dz + z];
            }
        }

        return objects;
    }

    /**
     * When running at lower resolutions than FULL, this utility allows to collect all data points that will be rendered
     * into a single pixel. Meant to be used with ArrayAggregator or similar utilities, to aggregate the multiple data
     * points into a single value.
     */
    protected static float[] relevantData(TileResolution resolution, int x, int z, float[][] data) {
        float[] objects = new float[resolution.pixelWidth * resolution.pixelWidth];
        x *= resolution.pixelWidth;
        z *= resolution.pixelWidth;
        int idx = 0;

        for(int dx = 0; dx < resolution.pixelWidth; dx++) {
            for(int dz = 0; dz < resolution.pixelWidth; dz++) {
                objects[idx++] = data[dx + x][dz + z];
            }
        }

        return objects;
    }

    /**
     * When running at lower resolutions than FULL, this utility allows to collect all data points that will be rendered
     * into a single pixel. Meant to be used with ArrayAggregator or similar utilities, to aggregate the multiple data
     * points into a single value.
     */
    @SuppressWarnings("unchecked")
    protected static <T> T[] relevantData(TileResolution resolution, int x, int z, T[][] data, Class<T> cls) {
        T[] objects = (T[]) Array.newInstance(cls, resolution.pixelWidth * resolution.pixelWidth);
        x *= resolution.pixelWidth;
        z *= resolution.pixelWidth;
        int idx = 0;

        for(int dx = 0; dx < resolution.pixelWidth; dx++) {
            for(int dz = 0; dz < resolution.pixelWidth; dz++) {
                objects[idx++] = data[dx + x][dz + z];
            }
        }

        return objects;
    }
}
