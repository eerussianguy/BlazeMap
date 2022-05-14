package com.eerussianguy.blazemap.feature.render;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.Helpers;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.engine.BlazeMapEngine;
import com.eerussianguy.blazemap.engine.RegionPos;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;

public class MinimapRenderer implements AutoCloseable
{
    public static final MinimapRenderer INSTANCE = new MinimapRenderer(Minecraft.getInstance().textureManager);

    private static final int[][] OFFSETS = Util.make(() -> {
        final int[][] offsets = new int[8][2];
        int idx = 0;
        for (int x = -1; x <= 1; x++)
        {
            for (int z = -1; z <= 1; z++)
            {
                if (!(x == 0 && z == 0))
                {
                    offsets[idx++] = new int[] {x, z};
                }
            }
        }
        return offsets;
    });

    private MapType mapType;
    private boolean requiresUpload = true;

    private final RenderType textRenderType;
    private final DynamicTexture texture;

    MinimapRenderer(TextureManager manager)
    {
        this.textRenderType = RenderType.text(Helpers.identifier("default"));
        this.texture = new DynamicTexture(512, 512, false);
        manager.register(Helpers.identifier("minimap"), this.texture);
    }

    public void setMapType(MapType type)
    {
        mapType = type;
    }

    public void draw(PoseStack stack, MultiBufferSource buffers)
    {
        if (requiresUpload)
        {
            upload();
            requiresUpload = false;
        }
    }

    public void upload()
    {
        LocalPlayer player = Helpers.getPlayer();
        if (player != null)
        {
            final BlockPos playerPos = player.blockPosition();
            final RegionPos originRegion = new RegionPos(playerPos);
            for (ResourceLocation layer : mapType.getLayers())
            {
                for (int[] offset : OFFSETS)
                {
                    final RegionPos currentRegion = originRegion.offset(offset[0], offset[1]);
                    BlazeMapEngine.Hooks.consumeTile(layer, currentRegion, data ->
                        consume(playerPos, currentRegion, texture, data)
                    );
                }

            }
        }

        texture.upload();
    }

    private void consume(BlockPos center, RegionPos currentRegion, DynamicTexture minimap, NativeImage tileImage)
    {
        NativeImage minimapPixels = minimap.getPixels();
        if (minimapPixels != null)
        {
            BlockPos corner = center.offset(-256, 0, -256);
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 512; x++)
            {
                for (int z = 0; z < 512; z++)w
                {
                    mutable.setWithOffset(corner, x, 0, z);
                    final ChunkPos chunkPos = new ChunkPos(mutable);
                    final RegionPos pixelRegion = new RegionPos(chunkPos);
                    if (pixelRegion.equals(currentRegion))
                    {
                        minimapPixels.setPixelRGBA(x, z, tileImage.getPixelRGBA(chunkPos.getRegionLocalX() << 4, chunkPos.getRegionLocalZ() << 4));
                    }
                }
            }
        }

    }

    @Override
    public void close()
    {
        texture.close();
    }
}
