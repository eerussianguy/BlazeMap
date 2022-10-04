package com.eerussianguy.blazemap.util;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraftforge.common.ForgeConfigSpec;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry.Key;
import com.eerussianguy.blazemap.api.pipeline.Layer;

public class LayerListAdapter implements IConfigAdapter<List<Key<Layer>>> {
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> target;

    public LayerListAdapter(ForgeConfigSpec.ConfigValue<List<? extends String>> target) {
        this.target = target;
    }

    @Override
    public List<Key<Layer>> get() {
        return target.get().stream().map(BlazeMapAPI.LAYERS::findOrCreate).collect(Collectors.toList());
    }

    @Override
    public void set(List<Key<Layer>> value) {
        target.set(value.stream().map(Key::toString).collect(Collectors.toList()));
    }
}
