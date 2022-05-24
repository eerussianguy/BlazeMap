package com.eerussianguy.blazemap;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class BlazeMapConfig {
    public static final ClientConfig CLIENT = register(ClientConfig::new);

    public static void init() {}

    private static <C> C register(Function<ForgeConfigSpec.Builder, C> factory) {
        Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(factory);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, specPair.getRight());
        return specPair.getLeft();
    }
}
