package com.eerussianguy.blazemap.api.mapping;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.mojang.blaze3d.platform.NativeImage;

public abstract class Layer implements BlazeRegistry.RegistryEntry {
    protected static final int OPAQUE = 0xFF000000;

    private final BlazeRegistry.Key<Layer> id;
    private final Set<BlazeRegistry.Key<Collector<MasterDatum>>> collectors;
    private final TranslatableComponent name;
    private final ResourceLocation icon;

    @SafeVarargs
    public Layer(BlazeRegistry.Key<Layer> id, TranslatableComponent name, ResourceLocation icon, BlazeRegistry.Key<Collector<MasterDatum>>... collectors) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.collectors = Arrays.stream(collectors).collect(Collectors.toUnmodifiableSet());
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

    public abstract boolean renderTile(NativeImage tile, IDataSource data);

    public TranslatableComponent getName() {
        return name;
    }

    public ResourceLocation getIcon() {
        return icon;
    }
}
