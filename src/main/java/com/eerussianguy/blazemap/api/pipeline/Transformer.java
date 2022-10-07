package com.eerussianguy.blazemap.api.pipeline;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.BlazeRegistry.RegistryEntry;
import com.eerussianguy.blazemap.api.util.IDataSource;

/**
 * Transformers can produce one MasterDatum by reading other MasterData, without needing access to the world.
 * This enables Transformers to run in the background threads, meaning they have less impact on the game's performance
 * and are therefore preferable over Collectors whenever possible.
 *
 * Transformers execute after all Collectors, so that they can intake the data they produced safely. This also enables
 * them to process data on the client that was produced by a collector on the server, when Blaze Map is present on both.
 *
 * Transformers are assumed to be deterministic, so if their input didn't change their output also won't change.
 * This means Blaze Map may decide not to execute a Transformer if it determines it won't generate any new data.
 *
 * Transformers aren't meant to do any data processing other than producing more MasterData from existing ones.
 * For processing use cases, see Processor.
 */
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
