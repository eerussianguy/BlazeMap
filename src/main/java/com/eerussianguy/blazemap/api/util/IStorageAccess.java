package com.eerussianguy.blazemap.api.util;

import java.io.IOException;

import net.minecraft.resources.ResourceLocation;

public interface IStorageAccess {
    boolean exists(ResourceLocation node);
    MinecraftStreams.Input read(ResourceLocation node) throws IOException;
    MinecraftStreams.Output write(ResourceLocation node) throws IOException;
}
