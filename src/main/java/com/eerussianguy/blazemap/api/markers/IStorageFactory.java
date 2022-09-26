package com.eerussianguy.blazemap.api.markers;

import java.util.function.Supplier;

import com.eerussianguy.blazemap.api.util.IOSupplier;
import com.eerussianguy.blazemap.api.util.MinecraftStreams;

public interface IStorageFactory<T extends IMarkerStorage<?>> {
    T create(IOSupplier<MinecraftStreams.Input> input, IOSupplier<MinecraftStreams.Output> output, Supplier<Boolean> exists);
}
