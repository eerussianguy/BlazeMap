package com.eerussianguy.blazemap.api.mapping;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.util.IDataSource;
import com.eerussianguy.blazemap.api.util.RegionPos;

/**
 * Like Layers, Processors consume one or more MasterDatum objects for a given chunk, however
 * instead of producing an image processors are free to use the data however they see fit.
 *
 * Processors are not part of the main map rendering pipeline and are instead available for addons
 * to implement more advanced features.
 *
 * Processors operate in asynchronously and in parallel in the engine's background threads. Given its
 * operations are determined by addon developers, care must be taken to ensure all external interactions
 * are made thread-safe.
 *
 * @author LordFokas
 */
public abstract class Processor implements BlazeRegistry.RegistryEntry {
    private final BlazeRegistry.Key<Layer> id;
    private final Set<BlazeRegistry.Key<Collector<MasterDatum>>> collectors;

    @SafeVarargs
    public Processor(BlazeRegistry.Key<Layer> id, BlazeRegistry.Key<Collector<MasterDatum>>... collectors) {
        this.id = id;
        this.collectors = Arrays.stream(collectors).collect(Collectors.toUnmodifiableSet());
    }

    public BlazeRegistry.Key<Layer> getID() {
        return id;
    }

    public Set<BlazeRegistry.Key<Collector<MasterDatum>>> getCollectors() {
        return collectors;
    }

    public boolean shouldExecuteInDimension(ResourceKey<Level> dimension) {
        return true;
    }

    public abstract boolean execute(ResourceKey<Level> dimension, RegionPos regions, ChunkPos chunk, IDataSource data);
}
