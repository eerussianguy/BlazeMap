package com.eerussianguy.blazemap.api.util;

import java.io.*;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.api.BlazeRegistry;

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

        public <T> void writeKey(BlazeRegistry.Key<T> key) throws IOException {
            writeResourceLocation(key.location);
        }

        public void writeBlockPos(BlockPos pos) throws IOException {
            writeLong(pos.asLong());
        }

        public void writeChunkPos(ChunkPos pos) throws IOException {
            writeLong(pos.toLong());
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

        public <T> BlazeRegistry.Key<T> readKey(BlazeRegistry<T> registry) throws IOException {
            ResourceLocation location = readResourceLocation();
            return registry.findOrCreate(location);
        }

        public BlockPos readBlockPos() throws IOException {
            return BlockPos.of(readLong());
        }

        public ChunkPos readChunkPos() throws IOException {
            return new ChunkPos(readLong());
        }
    }
}
