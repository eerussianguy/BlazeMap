package com.eerussianguy.blazemap.api.mapping;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry;
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
public abstract class Layer implements BlazeRegistry.RegistryEntry {
    protected static final int OPAQUE = 0xFF000000;

    private final BlazeRegistry.Key<Layer> id;
    private final Set<BlazeRegistry.Key<Collector<MasterDatum>>> collectors;
    private final TranslatableComponent name;
    private final ResourceLocation icon;
    private final boolean opaque;

    @SafeVarargs
    public Layer(BlazeRegistry.Key<Layer> id, TranslatableComponent name, BlazeRegistry.Key<Collector<MasterDatum>>... collectors) {
        this.id = id;
        this.name = name;
        this.icon = null;
        this.collectors = Arrays.stream(collectors).collect(Collectors.toUnmodifiableSet());
        this.opaque = true;
    }

    @SafeVarargs
    public Layer(BlazeRegistry.Key<Layer> id, TranslatableComponent name, ResourceLocation icon, BlazeRegistry.Key<Collector<MasterDatum>>... collectors) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.collectors = Arrays.stream(collectors).collect(Collectors.toUnmodifiableSet());
        this.opaque = false;
    }

    public BlazeRegistry.Key<Layer> getID() {
        return id;
    }

    public Set<BlazeRegistry.Key<Collector<MasterDatum>>> getCollectors() {
        return collectors;
    }

    public boolean shouldRenderInDimension(ResourceKey<Level> dimension) {
        return true;
    }

    public final boolean isOpaque() {
        return opaque;
    }

    public abstract boolean renderTile(NativeImage tile, IDataSource data);

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
    public Widget getLegendWidget(){
        return null;
    }
}
