package com.eerussianguy.blazemap.api.pipeline;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.BlazeRegistry.RegistryEntry;
import com.eerussianguy.blazemap.api.util.IDataSource;

public abstract class Transformer<T extends MasterDatum> implements RegistryEntry, PipelineComponent, Producer, Consumer {
    private final Key<Transformer<MasterDatum>> id;
    private final Key<DataType<MasterDatum>> output;
    private final Set<Key<DataType<MasterDatum>>> inputs;

    @SafeVarargs
    public Transformer(Key<Transformer<MasterDatum>> id, Key<DataType<MasterDatum>> output, Key<DataType<MasterDatum>>... inputs) {
        this.id = id;
        this.output = output;
        this.inputs = Arrays.stream(inputs).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Key<Transformer<MasterDatum>> getID() {
        return id;
    }

    @Override
    public Key<DataType<MasterDatum>> getOutputID() {
        return output;
    }

    public Set<Key<DataType<MasterDatum>>> getInputIDs() {
        return inputs;
    }

    public abstract T transform(IDataSource data);
}
