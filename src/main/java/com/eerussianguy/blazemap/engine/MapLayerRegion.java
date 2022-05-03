package com.eerussianguy.blazemap.engine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MapLayerRegion {
    private final ResourceLocation layer;
    private final RegionPos region;
    private final File file;
    private BufferedImage image;

    public MapLayerRegion(ResourceLocation layer, RegionPos region, File worldDir){
        this.layer = layer;
        this.region = region;
        File layerDir = new File(worldDir, layer.toString().replace(':', '+'));
        this.file = new File(layerDir, region.toString()+".png");
        image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
    }

    public void tryLoad(){
        if(file.exists()){
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();

                // TODO: this is temporary (aka more permanent than "forever")
                throw new RuntimeException(e);
            }
        }else{
            file.getParentFile().mkdirs();
        }
    }

    public void save(){
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();

            // TODO: this is temporary (aka more permanent than "forever")
            throw new RuntimeException(e);
        }
    }

    public void updateTile(BufferedImage tile, ChunkPos chunk){
        int xOffset = chunk.getRegionLocalX() << 4;
        int zOffset = chunk.getRegionLocalZ() << 4;

        for(int x=0; x<16; x++)
        for(int z=0; z<16; z++){
            image.setRGB(xOffset + x, zOffset + z, tile.getRGB(x, z));
        }
    }
}
