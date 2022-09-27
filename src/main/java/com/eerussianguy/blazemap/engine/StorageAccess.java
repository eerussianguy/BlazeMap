package com.eerussianguy.blazemap.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import net.minecraft.resources.ResourceLocation;

import com.eerussianguy.blazemap.api.util.IStorageAccess;
import com.eerussianguy.blazemap.api.util.MinecraftStreams;

public class StorageAccess implements IStorageAccess {
    private final File dir;

    public StorageAccess(File dir) {
        this.dir = dir;
    }

    @Override
    public boolean exists(ResourceLocation node) {
        return getFile(node).exists();
    }

    @Override
    public MinecraftStreams.Input read(ResourceLocation node) throws IOException {
        return new MinecraftStreams.Input(new FileInputStream(getFile(node)));
    }

    @Override
    public MinecraftStreams.Output write(ResourceLocation node) throws IOException {
        File file = getFile(node);
        file.getParentFile().mkdirs();
        return new MinecraftStreams.Output(new FileOutputStream(file));
    }

    private File getFile(ResourceLocation node) {
        Objects.requireNonNull(node);
        File mod = new File(dir, node.getNamespace());
        mod.mkdirs();
        return new File(mod, node.getPath());
    }
}
