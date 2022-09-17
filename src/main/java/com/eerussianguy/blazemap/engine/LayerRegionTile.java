package com.eerussianguy.blazemap.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import net.minecraft.world.level.ChunkPos;

import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.engine.async.PriorityLock;
import com.mojang.blaze3d.platform.NativeImage;

public class LayerRegionTile {
    private final PriorityLock lock = new PriorityLock();
    private final File file;
    private NativeImage image;
    private boolean isEmpty = true;

    public LayerRegionTile(BlazeRegistry.Key<Layer> layer, RegionPos region, File worldDir) {
        File layerDir = new File(worldDir, layer.location.toString().replace(':', '+'));
        this.file = new File(layerDir, region.toString() + ".png");
        image = new NativeImage(NativeImage.Format.RGBA, 512, 512, false);
    }

    public void tryLoad() {
        if(file.exists()) {
            try {
                lock.lockPriority();
                image = NativeImage.read(Files.newInputStream(file.toPath()));
                isEmpty = false;
            }
            catch(IOException e) {
                e.printStackTrace();

                // TODO: this is temporary (aka more permanent than "forever")
                throw new RuntimeException(e);
            }
            finally {
                lock.unlock();
            }
        }
        else {
            file.getParentFile().mkdirs();
        }
    }

    public void save() {
        if(isEmpty) return;
        try {
            lock.lock();
            image.writeToFile(file);
        }
        catch(IOException e) {
            e.printStackTrace();

            // TODO: this is temporary (aka more permanent than "forever")
            throw new RuntimeException(e);
        }
        finally {
            lock.unlock();
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
}
