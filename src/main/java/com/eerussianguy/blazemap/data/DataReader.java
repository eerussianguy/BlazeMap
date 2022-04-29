package com.eerussianguy.blazemap.data;

import net.minecraft.nbt.CompoundTag;

public abstract class DataReader
{
    abstract int colorFor(int x, int z);

    abstract CompoundTag serialize();

    abstract DataReader deserialize(CompoundTag nbt);
}
