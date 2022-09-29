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
    protected final File dir;

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
        return new MinecraftStreams.Output(new FileOutputStream(file));
    }

    protected File getFile(ResourceLocation node) {
        Objects.requireNonNull(node);
        File mod = new File(dir, node.getNamespace());
        mod.mkdirs();
        return new File(mod, node.getPath());
    }

    public static class Internal extends StorageAccess {
        private static final String OLD_PATTERN = "%s+%s";
        private static final String NEW_PATTERN = "[%s] %s";

        public Internal(File dir, String child){
            this(new File(dir, child));
        }
        public Internal(File dir) {
            super(dir);
            dir.mkdirs();
        }

        @Override
        public File getFile(ResourceLocation node) {
            Objects.requireNonNull(node);
            File file = new File(dir, String.format(OLD_PATTERN, node.getNamespace(), node.getPath()));
            file.getParentFile().mkdirs();
            return file;
        }

        public File getFile(ResourceLocation node, String file) {
            Objects.requireNonNull(node);
            File d = new File(dir, String.format(OLD_PATTERN, node.getNamespace(), node.getPath()));
            d.mkdirs();
            return new File(d, file);
        }

        public StorageAccess addon(){
            return new StorageAccess(dir);
        }

        public StorageAccess addon(ResourceLocation node){
            return new StorageAccess(getFile(node));
        }

        public StorageAccess.Internal internal(ResourceLocation node){
            return new Internal(getFile(node));
        }
    }
}