package com.eerussianguy.blazemap.api.pipeline;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.BlazeRegistry.RegistryEntry;
import com.eerussianguy.blazemap.api.util.IDataSource;

public abstract class Transformer<T extends MasterDatum> implements RegistryEntry, Producer<T>, Consumer {
    private final Key<Transformer<MasterDatum>> id;
    private final Key<DataType<T>> output;
    private final Set<Key<DataType<MasterDatum>>> inputs;

    @SafeVarargs
    public Transformer(Key<Transformer<MasterDatum>> id, Key<DataType<T>> output, Key<DataType<MasterDatum>>... inputs) {
        this.id = id;
        this.output = output;
        this.inputs = Arrays.stream(inputs).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Key<Transformer<MasterDatum>> getID() {
        return id;
    }

    @Override
    public Key<DataType<T>> getOutputID() {
        return output;
    }

    public Set<Key<DataType<MasterDatum>>> getInputIDs() {
        return inputs;
    }

    public boolean shouldExecuteInDimension(ResourceKey<Level> dimension) {
        return true;
    }

    public abstract T transform(IDataSource data);
}
