package com.eerussianguy.blazemap.util;

import java.io.*;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class MinecraftStreams {
    public static class Output extends DataOutputStream {
        public Output(OutputStream out) {
            super(out);
        }

        public void writeResourceLocation(ResourceLocation resourceLocation) throws IOException {
            writeUTF(resourceLocation.toString());
        }

        public void writeDimensionKey(ResourceKey<Level> dimension) throws IOException {
            writeResourceLocation(dimension.registry());
            writeResourceLocation(dimension.location());
        }

        public void writeBlockPos(BlockPos pos) throws IOException {
            writeLong(pos.asLong());
        }
    }

    public static class Input extends DataInputStream {
        public Input(InputStream in) {
            super(in);
        }

        public ResourceLocation readResourceLocation() throws IOException {
            return new ResourceLocation(readUTF());
        }

        public ResourceKey<Level> readDimensionKey() throws IOException {
            ResourceLocation registry = readResourceLocation();
            ResourceLocation location = readResourceLocation();
            return ResourceKey.create(ResourceKey.createRegistryKey(registry), location);
        }

        public BlockPos readBlockPos() throws IOException {
            return BlockPos.of(readLong());
        }
    }
}
