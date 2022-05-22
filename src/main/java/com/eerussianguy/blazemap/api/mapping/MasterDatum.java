package com.eerussianguy.blazemap.api.mapping;

import net.minecraft.nbt.CompoundTag;

public interface MasterDatum {
    CompoundTag serialize();

    MasterDatum deserialize(CompoundTag nbt);
}
