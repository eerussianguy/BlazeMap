package com.eerussianguy.blazemap.api.pipeline;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.BlazeRegistry.RegistryEntry;
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
public abstract class Processor implements RegistryEntry, Consumer {
    private final Key<Processor> id;
    private final Set<Key<DataType<MasterDatum>>> inputs;

    @SafeVarargs
    public Processor(Key<Processor> id, Key<DataType<MasterDatum>>... inputs) {
        this.id = id;
        this.inputs = Arrays.stream(inputs).collect(Collectors.toUnmodifiableSet());
    }

    public Key<Processor> getID() {
        return id;
    }

    @Override
    public Set<Key<DataType<MasterDatum>>> getInputIDs() {
        return inputs;
    }

    public boolean shouldExecuteInDimension(ResourceKey<Level> dimension) {
        return true;
    }

    public abstract boolean execute(ResourceKey<Level> dimension, RegionPos region, ChunkPos chunk, IDataSource data);
}
