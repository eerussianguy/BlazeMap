package com.eerussianguy.blazemap.core.mapping;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.IMapView;
import com.eerussianguy.blazemap.api.builtin.TerrainHeightMD;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MasterData;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;

public class TerrainHeightLayer extends Layer {

    public TerrainHeightLayer() {
        super(BlazeMapReferences.LAYER_TERRAIN_HEIGHT, BlazeMapReferences.MD_TERRAIN_HEIGHT);
    }

    @Override
    public boolean renderTile(BufferedImage tile, IMapView<ResourceLocation, MasterData> data) {
        TerrainHeightMD terrain = data.get(BlazeMapReferences.MD_TERRAIN_HEIGHT, TerrainHeightMD.class);
        for(int x=0; x<16; x++)
        for(int z=0; z<16; z++){
            tile.setRGB(x, z, 0xFF808080);
        }
        return true;
    }
}
