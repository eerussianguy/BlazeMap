package com.eerussianguy.blazemap.api.mapping;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.mojang.blaze3d.platform.NativeImage;

public abstract class Layer implements BlazeRegistry.RegistryEntry {
    protected static final int OPAQUE = 0xFF000000;

    private final BlazeRegistry.Key<Layer> id;
    private final Set<BlazeRegistry.Key<Collector<MasterDatum>>> collectors;
    private final Component name;

    @SafeVarargs
    public Layer(BlazeRegistry.Key<Layer> id, Component name, BlazeRegistry.Key<Collector<MasterDatum>>... collectors) {
        this.id = id;
        this.name = name;
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

    public Component getName() {
        return name;
    }
}
