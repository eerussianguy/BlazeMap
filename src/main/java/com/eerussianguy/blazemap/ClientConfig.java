package com.eerussianguy.blazemap;

import java.util.function.Function;

import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

import static com.eerussianguy.blazemap.BlazeMap.MOD_ID;

public class ClientConfig
{
    public final IntValue particleAttempts;
    public final IntValue particleDistance;

    public final BooleanValue souls;
    public final BooleanValue leaves;
    public final BooleanValue snowballs;

    public final IntValue leavesCacheSize;
    public final DoubleValue leavesVariationDistance;
    public final IntValue extraGrassRarity;

    public final BooleanValue forceForgeLighting;

    ClientConfig(Builder innerBuilder)
    {
        Function<String, Builder> builder = name -> innerBuilder.translation(MOD_ID + ".config.server." + name);

        innerBuilder.push("general");

        innerBuilder.pop();
    }
}
