package com.eerussianguy.blazemap.api.mapping;

import net.minecraft.nbt.CompoundTag;

public interface MasterData
{
    CompoundTag serialize();

    MasterData deserialize(CompoundTag nbt);
}
