package com.eerussianguy.blazemap;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;

import static com.eerussianguy.blazemap.BlazeMap.MOD_ID;

public class Helpers {
    public static final Direction[] DIRECTIONS = Direction.values();
    private static File baseDir;

    public static ResourceLocation identifier(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static int randomBrightColor(Random random) {
        return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat()).brighter().getRGB();
    }

    public static ClientLevel levelOrThrow()
    {
        return Objects.requireNonNull(Minecraft.getInstance().level);
    }

    public static String getServerID() {
        Minecraft mc = Minecraft.getInstance();
        if(mc.hasSingleplayerServer()) {
            return "SP@" + mc.getSingleplayerServer().getWorldData().getLevelName();
        }
        else {
            return "MP@" + mc.getCurrentServer().ip.replace(':', '+');
        }
    }

    public static File getBaseDir() {
        if(baseDir == null) baseDir = new File(Minecraft.getInstance().gameDirectory, MOD_ID);
        return baseDir;
    }

    public static void runOnMainThread(Runnable r) {
        Minecraft.getInstance().tell(r);
    }

    public static <T> void writeCodec(Codec<T> codec, T value, CompoundTag tag, String field)
    {
        tag.put(field, codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, BlazeMap.LOGGER::error));
    }

    public static <T> T decodeCodec(Codec<T> codec, CompoundTag tag, String field)
    {
        return codec.parse(NbtOps.INSTANCE, tag.get(field)).getOrThrow(false, BlazeMap.LOGGER::error);
    }

}
