package com.eerussianguy.blazemap.engine.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

import net.minecraft.world.level.ChunkPos;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.pipeline.Layer;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.engine.StorageAccess;
import com.eerussianguy.blazemap.engine.async.PriorityLock;
import com.mojang.blaze3d.platform.NativeImage;

public class LayerRegionTile {
    private static final Object MUTEX = new Object();
    private static int instances = 0, loaded = 0;

    private final PriorityLock lock = new PriorityLock();
    private final File file, buffer;
    private NativeImage image;
    private boolean isEmpty = true;

    public LayerRegionTile(StorageAccess.Internal storage, BlazeRegistry.Key<Layer> layer, RegionPos region) {
        this.file = storage.getFile(layer.location, region + ".png");
        this.buffer = storage.getFile(layer.location, region + ".buffer");
        image = new NativeImage(NativeImage.Format.RGBA, 512, 512, true);
    }

    public void tryLoad() {
        if(file.exists()) {
            try {
                lock.lockPriority();
                image = NativeImage.read(Files.newInputStream(file.toPath()));
                isEmpty = false;
            }
            catch(IOException e) {
                // FIXME: this needs to hook into a reporting mechanism AND possibly automated LRT regeneration
                BlazeMap.LOGGER.error("Error loading LayerRegionTile: {}", file, e);
            }
            finally {
                lock.unlock();
            }
        }
        else {
            file.getParentFile().mkdirs();
        }
        onCreate();
    }

    public void save() {
        if(isEmpty) return;

        // Save image into buffer
        try {
            lock.lock();
            image.writeToFile(buffer);
        }
        catch(IOException e) {
            e.printStackTrace();
            // FIXME: this needs to hook into a reporting mechanism
            BlazeMap.LOGGER.error("Error saving LayerRegionTile buffer: {}", buffer, e);
        }
        finally {
            lock.unlock();
        }

        // Move buffer to real image path
        try {
            Files.move(buffer.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            buffer.delete();
        }
        catch(IOException e) {
            e.printStackTrace();
            // FIXME: this needs to hook into a reporting mechanism
            BlazeMap.LOGGER.error("Error moving LayerRegionTile buffer to image: {} {}", buffer, file, e);
        }
    }

    public void updateTile(NativeImage tile, ChunkPos chunk) {
        int xOffset = chunk.getRegionLocalX() << 4;
        int zOffset = chunk.getRegionLocalZ() << 4;

        try {
            lock.lock();
            for(int x = 0; x < 16; x++) {
                for(int z = 0; z < 16; z++) {
                    image.setPixelRGBA(xOffset + x, zOffset + z, tile.getPixelRGBA(x, z));
                }
            }
            isEmpty = false;
        }
        finally {
            lock.unlock();
        }
    }

    public void consume(Consumer<NativeImage> consumer) {
        if(isEmpty) return;
        try {
            lock.lockPriority();
            consumer.accept(image);
        }
        finally {
            lock.unlock();
        }
    }

    private void onCreate() {
        synchronized(MUTEX) {
            instances++;
            if(!isEmpty) loaded++;
        }
    }

    private void onDestroy() {
        synchronized(MUTEX) {
            instances--;
            if(!isEmpty) loaded--;
        }
    }

    public static int getInstances() {
        synchronized(MUTEX) {
            return instances;
        }
    }

    public static int getLoaded() {
        synchronized(MUTEX) {
            return loaded;
        }
    }
}
