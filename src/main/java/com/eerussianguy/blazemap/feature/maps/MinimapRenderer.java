package com.eerussianguy.blazemap.feature.maps;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.ForgeConfigSpec;

import com.eerussianguy.blazemap.BlazeMapConfig;
import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.event.DimensionChangedEvent;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.Profilers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

public class MinimapRenderer implements AutoCloseable {
    public static void enableLayer(BlazeRegistry.Key<?> key) {
        ForgeConfigSpec.ConfigValue<List<? extends String>> opt = BlazeMapConfig.CLIENT.disabledLayers;
        List<? extends String> list = opt.get();
        if(list.contains(key.toString())) {
            list.remove(key.toString());
            opt.set(list);
        }
    }

    public static void disableLayer(BlazeRegistry.Key<?> key) {
        ForgeConfigSpec.ConfigValue<List<? extends String>> opt = BlazeMapConfig.CLIENT.disabledLayers;
        // noinspection unchecked
        List<String> list = (List<String>) opt.get();
        if(!list.contains(key.toString())) {
            list.add(key.toString());
            opt.set(list);
        }
    }

    public static final MinimapRenderer INSTANCE = new MinimapRenderer();
    private static final int SIZE = 512;

    public static void onDimensionChange(DimensionChangedEvent evt) {
        INSTANCE.setMapType(BlazeMapReferences.MapTypes.AERIAL_VIEW.value());
    }

    private final RenderType backgroundRenderType;
    private MapType mapType;
    private BlockPos last = BlockPos.ZERO;
    private final MapRenderer mapRenderer;

    public MinimapRenderer() {
        ResourceLocation mapBackground = Helpers.identifier("textures/map.png");
        this.backgroundRenderType = RenderType.text(mapBackground);
        mapRenderer = new MapRenderer(SIZE, SIZE, Helpers.identifier("dynamic/map/minimap"), 0.5, 8);
    }

    public void setMapType(MapType type) {
        mapType = type;
        mapRenderer.setMapType(mapType);
    }

    public MapType getMapType() {
        return mapType;
    }

    public void draw(PoseStack stack, MultiBufferSource buffers, ForgeIngameGui gui, int width, int height) {
        if(Minecraft.getInstance().screen instanceof WorldMapGui) return;

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

        final MinimapSize size = BlazeMapConfig.CLIENT.minimapSize.get();
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
    }
}
