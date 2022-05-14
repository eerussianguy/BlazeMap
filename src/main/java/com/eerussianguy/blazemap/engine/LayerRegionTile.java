package com.eerussianguy.blazemap.engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import com.eerussianguy.blazemap.engine.async.PriorityLock;

public class LayerRegionTile
{
    private final PriorityLock lock = new PriorityLock();
    private final File file;
    private BufferedImage image;

    public LayerRegionTile(ResourceLocation layer, RegionPos region, File worldDir)
    {
        File layerDir = new File(worldDir, layer.toString().replace(':', '+'));
        this.file = new File(layerDir, region.toString() + ".png");
        image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
    }

    public void tryLoad()
    {
        if (file.exists())
        {
            try
            {
                lock.lockPriority();
                image = ImageIO.read(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();

                // TODO: this is temporary (aka more permanent than "forever")
                throw new RuntimeException(e);
            }
            finally
            {
                lock.unlock();
            }
        }
        else
        {
            file.getParentFile().mkdirs();
        }
    }

    public void save()
    {
        try
        {
            lock.lock();
            ImageIO.write(image, "png", file);
        }
        catch (IOException e)
        {
            e.printStackTrace();

            // TODO: this is temporary (aka more permanent than "forever")
            throw new RuntimeException(e);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void updateTile(BufferedImage tile, ChunkPos chunk)
    {
        int xOffset = chunk.getRegionLocalX() << 4;
        int zOffset = chunk.getRegionLocalZ() << 4;

        try
        {
            lock.lock();
            for (int x = 0; x < 16; x++)
            {
                for (int z = 0; z < 16; z++)
                {
                    image.setRGB(xOffset + x, zOffset + z, tile.getRGB(x, z));
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void consume(Consumer<BufferedImage> consumer)
    {
        try
        {
            lock.lockPriority();
            consumer.accept(image);
        }
        finally
        {
            lock.unlock();
        }
    }
}
