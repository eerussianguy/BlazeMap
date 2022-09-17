package com.eerussianguy.blazemap.api.mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry;

/**
 * Each available map in Blaze Map is defined by MapType.
 * These objects do no processing of any sort and merely define a structure.
 * The provided list of layers defines what layers are rendered on the screen,
 * the first layer being rendered on the bottom and the last on top.
 *
 * @author LordFokas
 */
public abstract class MapType implements BlazeRegistry.RegistryEntry {
    private final BlazeRegistry.Key<MapType> id;
    private final Set<BlazeRegistry.Key<Layer>> layers;
    private final TranslatableComponent name;
    private final ResourceLocation icon;

    @SafeVarargs
    public MapType(BlazeRegistry.Key<MapType> id, TranslatableComponent name, ResourceLocation icon, BlazeRegistry.Key<Layer>... layers) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.layers = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(layers)));
    }

    public Set<BlazeRegistry.Key<Layer>> getLayers() {
        return layers;
    }

    @Override
    public BlazeRegistry.Key<MapType> getID() {
        return id;
    }

    public boolean shouldRenderInDimension(ResourceKey<Level> dimension) {
        return true;
    }

    public TranslatableComponent getName() {
        return name;
    }

    public ResourceLocation getIcon() {
        return icon;
    }
}
