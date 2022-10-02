package com.eerussianguy.blazemap.util;

import net.minecraftforge.common.ForgeConfigSpec;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.MapType;

public class MapTypeAdapter implements IConfigAdapter<BlazeRegistry.Key<MapType>> {
    private final ForgeConfigSpec.ConfigValue<String> target;

    public MapTypeAdapter(ForgeConfigSpec.ConfigValue<String> target) {
        this.target = target;
    }

    @Override
    public BlazeRegistry.Key<MapType> get() {
        return BlazeMapAPI.MAPTYPES.findOrCreate(target.get());
    }

    @Override
    public void set(BlazeRegistry.Key<MapType> value) {
        target.set(value.toString());
    }
}
