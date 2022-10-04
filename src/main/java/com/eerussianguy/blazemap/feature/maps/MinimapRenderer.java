package com.eerussianguy.blazemap.feature.maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ForgeIngameGui;

import com.eerussianguy.blazemap.BlazeMapConfig;
import com.eerussianguy.blazemap.api.MapType;
import com.eerussianguy.blazemap.api.util.IScreenSkipsMinimap;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.Profilers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

public class MinimapRenderer implements AutoCloseable {
    public static final MinimapRenderer INSTANCE = new MinimapRenderer();
    public static final int SIZE = 512;
    public static final double MIN_ZOOM = 0.5, MAX_ZOOM = 8;

    private final RenderType backgroundRenderType;
    private BlockPos last = BlockPos.ZERO;
    public final MapConfigSynchronizer synchronizer;
    private final MapRenderer mapRenderer;

    public MinimapRenderer() {
        ResourceLocation mapBackground = Helpers.identifier("textures/map.png");
        this.backgroundRenderType = RenderType.text(mapBackground);
        this.mapRenderer = new MapRenderer(SIZE, SIZE, Helpers.identifier("dynamic/map/minimap"), MIN_ZOOM, MAX_ZOOM, true)
            .setProfilers(Profilers.Minimap.DRAW_TIME_PROFILER, Profilers.Minimap.TEXTURE_TIME_PROFILER);
        this.synchronizer = new MapConfigSynchronizer(mapRenderer, BlazeMapConfig.CLIENT.minimap);
    }

    public void setMapType(MapType mapType) {
        synchronizer.setMapType(mapType);
    }

    public MapType getMapType() {
        return mapRenderer.getMapType();
    }

    public void draw(PoseStack stack, MultiBufferSource buffers, ForgeIngameGui gui, int width, int height) {
        if(Minecraft.getInstance().screen instanceof IScreenSkipsMinimap) return;

        LocalPlayer player = Helpers.getPlayer();
        if(player == null) return;
        BlockPos pos = player.blockPosition();

        if(!pos.equals(last)) {
            last = pos;
            mapRenderer.setCenter(pos.getX(), pos.getZ());
        }

        // Prepare to render minimap
        Profilers.Minimap.DRAW_TIME_PROFILER.begin();
        stack.pushPose();
        Matrix4f matrix4f = stack.last().pose();

        int w = 136, h = 148, m = 8;

        final MinimapSize size = BlazeMapConfig.CLIENT.minimap.overlaySize.get();
        // Translate to corner and apply scale
        stack.translate(width, 0, 0);
        stack.scale(size.scale, size.scale, 0);

        // Render map background
        stack.translate(-(w + m), m, 0);
        RenderHelper.drawQuad(buffers.getBuffer(this.backgroundRenderType), matrix4f, w, h);

        // Render actual map tiles
        stack.translate(4, 4, 0);
        stack.scale(0.25F, 0.25F, 1);
        mapRenderer.render(stack, buffers);

        stack.popPose();
        stack.pushPose();

        // Translate to corner and apply scale
        stack.translate(width, 0, 0);
        stack.scale(size.scale, size.scale, 0);

        // Render player coordinates
        stack.translate(-(w + m), h - 4, 0);
        stack.scale(0.5F, 0.5F, 0);
        Matrix4f matrix = stack.last().pose();
        Font fontRenderer = Minecraft.getInstance().font;
        String position = pos.toShortString();
        int offset = (w * 2 - fontRenderer.width(position)) / 2;
        fontRenderer.drawInBatch(position, offset, 0F, 0xDDDDDD, false, matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);

        // Finish
        stack.popPose();
        Profilers.Minimap.DRAW_TIME_PROFILER.end();
    }

    @Override
    public void close() {
        mapRenderer.close();
        synchronizer.save();
    }
}
