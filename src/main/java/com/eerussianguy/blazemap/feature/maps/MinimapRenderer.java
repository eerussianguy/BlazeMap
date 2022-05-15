package com.eerussianguy.blazemap.feature.maps;

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

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.eerussianguy.blazemap.Helpers;
import com.eerussianguy.blazemap.api.event.DimensionChangedEvent;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

public class MinimapRenderer implements AutoCloseable
{
    public static final MinimapRenderer INSTANCE = new MinimapRenderer(Minecraft.getInstance().textureManager);


    private static final int[][] OFFSETS = Util.make(() -> {
        final int[][] offsets = new int[9][2];
        int idx = 0;
        for (int x = -1; x <= 1; x++)
        {
            for (int z = -1; z <= 1; z++)
            {
                offsets[idx++] = new int[] {x, z};
            }
        }
        return offsets;
    });

    private MapType mapType;
    private boolean requiresUpload = true;
    private DimensionChangedEvent.DimensionTileStorage tileStorage;
    private final RenderType textRenderType;
    private final RenderType textureRenderType;
    private final DynamicTexture texture;

    MinimapRenderer(TextureManager manager)
    {
        this.textRenderType = RenderType.text(Helpers.identifier("default"));
        this.texture = new DynamicTexture(512, 512, false);
        ResourceLocation textureResource = Helpers.identifier("minimap");
        manager.register(textureResource, this.texture);
        this.textureRenderType = RenderType.text(textureResource);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onDimensionChanged(DimensionChangedEvent event)
    {
        this.tileStorage = event.tileStorage;
        event.tileNotifications.addUpdateListener(layerRegion -> this.requiresUpload = true);
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

        int uv2 = 0xF000F0;
        Matrix4f matrix4f = stack.last().pose();
        VertexConsumer vertexconsumer = buffers.getBuffer(this.textureRenderType);
        vertexconsumer.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(uv2).endVertex();
        vertexconsumer.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(uv2).endVertex();
        vertexconsumer.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(uv2).endVertex();
        vertexconsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(uv2).endVertex();
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
                    tileStorage.consumeTile(layer, currentRegion, data ->
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
                for (int z = 0; z < 512; z++)
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
