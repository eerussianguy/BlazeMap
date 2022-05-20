package com.eerussianguy.blazemap;

import java.util.function.Function;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

import static com.eerussianguy.blazemap.BlazeMap.MOD_ID;

/**
 * Forge configs happen to be a very simple way to serialize things across saves and hold data within a particular instance
 * It is not necessarily expected that the player will be editing the config
 * We are free to use key binds to allow what is essentially config editing on the fly
 */
public class ClientConfig {
    public final BooleanValue enableMinimap;

    ClientConfig(ForgeConfigSpec.Builder innerBuilder) {
        Function<String, ForgeConfigSpec.Builder> builder = name -> innerBuilder.translation(MOD_ID + ".config.server." + name);

        innerBuilder.push("general");

        enableMinimap = builder.apply("enableMinimap").comment("Enable the minimap?").define("enableMinimap", true);

        innerBuilder.pop();
    }
}
