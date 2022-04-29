package com.eerussianguy.blazemap;

import java.awt.*;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import static com.eerussianguy.blazemap.BlazeMap.MOD_ID;

public class Helpers
{
    public static final Direction[] DIRECTIONS = Direction.values();
    public static ResourceLocation identifier(String name)
    {
        return new ResourceLocation(MOD_ID, name);
    }

    public static int randomBrightColor(Random random)
    {
        return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat()).brighter().getRGB();
    }

}
