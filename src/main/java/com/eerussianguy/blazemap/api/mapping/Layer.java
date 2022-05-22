package com.eerussianguy.blazemap.api.mapping;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.mojang.blaze3d.platform.NativeImage;

public abstract class Layer implements BlazeRegistry.Registerable<Layer> {
    protected static final int OPAQUE = 0xFF000000;

    private final BlazeRegistry.Key<Layer> id;
    private final Set<BlazeRegistry.Key<Collector<?>>> collectors;

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public Layer(BlazeRegistry.Key<? extends Layer> id, BlazeRegistry.Key<? extends Collector<?>>... collectors) {
        this.id = (BlazeRegistry.Key<Layer>) id;
        // yes I'm a TypeScript developer... how did you guess?                  X as any as Y;
        this.collectors = (Set<BlazeRegistry.Key<Collector<?>>>) (Object) Arrays.stream(collectors).collect(Collectors.toUnmodifiableSet());
    }

    public BlazeRegistry.Key<Layer> getID() {
        return id;
    }

    public Set<BlazeRegistry.Key<Collector<?>>> getCollectors() {
        return collectors;
    }

    public boolean shouldRenderInDimension(ResourceKey<Level> dimension) {
        return true;
    }

    public abstract boolean renderTile(NativeImage tile, IDataSource data);
}
