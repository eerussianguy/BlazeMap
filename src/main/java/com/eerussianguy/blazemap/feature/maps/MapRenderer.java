package com.eerussianguy.blazemap.feature.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.event.DimensionChangedEvent;
import com.eerussianguy.blazemap.api.event.MapLabelEvent;
import com.eerussianguy.blazemap.api.event.WaypointEvent;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.api.markers.IMarkerStorage;
import com.eerussianguy.blazemap.api.markers.MapLabel;
import com.eerussianguy.blazemap.api.markers.Waypoint;
import com.eerussianguy.blazemap.api.util.RegionPos;
import com.eerussianguy.blazemap.engine.BlazeMapEngine;
import com.eerussianguy.blazemap.engine.async.AsyncAwaiter;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.Profiler;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class MapRenderer implements AutoCloseable {
    private static final ResourceLocation PLAYER = Helpers.identifier("textures/player.png");
    private static final List<MapRenderer> RENDERERS = new ArrayList<>(4);
    private static DimensionChangedEvent.DimensionTileStorage tileStorage;
    private static ResourceKey<Level> dimension;
    private static IMarkerStorage<Waypoint> waypointStorage;
    private static IMarkerStorage.Layered<MapLabel> labelStorage;

    public static void onDimensionChange(DimensionChangedEvent evt) {
        tileStorage = evt.tileStorage;
        dimension = evt.dimension;
        waypointStorage = evt.waypoints;
        labelStorage = evt.labels;
    }

    public static void onWaypointAdded(WaypointEvent.Created event) {
        RENDERERS.forEach(r -> r.add(event.waypoint));
    }

    public static void onWaypointRemoved(WaypointEvent.Removed event) {
        RENDERERS.forEach(r -> r.remove(event.waypoint));
    }

    public static void onMapLabelAdded(MapLabelEvent.Created event) {
        RENDERERS.forEach(r -> r.add(event.label));
    }

    public static void onMapLabelRemoved(MapLabelEvent.Removed event) {
        RENDERERS.forEach(r -> r.remove(event.label));
    }


    // =================================================================================================================


    private Profiler.TimeProfiler renderTimer = new Profiler.TimeProfiler.Dummy();
    private Profiler.TimeProfiler uploadTimer = new Profiler.TimeProfiler.Dummy();

    private MapType mapType;
    private List<BlazeRegistry.Key<Layer>> disabled, visible;
    private final HashMap<BlazeRegistry.Key<MapType>, List<BlazeRegistry.Key<Layer>>> disabledLayers = new HashMap<>();
    private final List<Waypoint> waypoints = new ArrayList<>(16);
    private final List<MapLabel> labels = new ArrayList<>(16);

    private final ResourceLocation textureResource;
    private DynamicTexture mapTexture;
    private RenderType renderType;
    private boolean needsUpdate = true;

    private int width, height;
    private int mapWidth, mapHeight;
    private final BlockPos.MutableBlockPos center, begin, end;
    private RegionPos[][] offsets;
    private final double minZoom, maxZoom;
    private double zoom = 1;
    private final boolean renderNames;

    public MapRenderer(int width, int height, ResourceLocation textureResource, double minZoom, double maxZoom, boolean renderNames) {
        this.center = new BlockPos.MutableBlockPos();
        this.begin = new BlockPos.MutableBlockPos();
        this.end = new BlockPos.MutableBlockPos();
        this.textureResource = textureResource;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;

        selectMapType();
        centerOnPlayer();

        if(width > 0 && height > 0) {
            this.resize(width, height);
        }

        this.renderNames = renderNames;

        RENDERERS.add(this);
    }

    private void selectMapType() {
        if(dimension != null && (mapType == null || !mapType.shouldRenderInDimension(dimension))) {
            for(BlazeRegistry.Key<MapType> next : BlazeMapAPI.MAPTYPES.keys()) {
                MapType type = next.value();
                if(type.shouldRenderInDimension(dimension)) {
                    setMapType(type);
                    break;
                }
            }
        }
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        selectMapType();
        createImage();
    }

    private void createImage() {
        makeOffsets();
        if(mapTexture != null) {
            mapTexture.close();
        }
        mapWidth = (int) (width / zoom);
        mapHeight = (int) (height / zoom);
        mapTexture = new DynamicTexture(mapWidth, mapHeight, false);
        Minecraft.getInstance().getTextureManager().register(textureResource, mapTexture);
        renderType = RenderType.text(textureResource);
        needsUpdate = true;
    }

    private void makeOffsets() {
        this.mapWidth = (int) (width / zoom);
        this.mapHeight = (int) (height / zoom);

        int w2 = mapWidth / 2, h2 = mapHeight / 2;
        RegionPos b = new RegionPos(begin.set(center.offset(-w2, 0, -h2)));
        RegionPos e = new RegionPos(end.set(center.offset(w2, 0, h2)));

        int dx = e.x - b.x + 1;
        int dz = e.z - b.z + 1;

        offsets = new RegionPos[dx][dz];
        for(int x = 0; x < dx; x++) {
            for(int z = 0; z < dz; z++) {
                offsets[x][z] = b.offset(x, z);
            }
        }

        updateWaypoints();
        updateLabels();
    }

    public void updateWaypoints() {
        waypoints.clear();
        waypoints.addAll(waypointStorage.getAll().stream().filter(w -> inRange(w.getPosition())).collect(Collectors.toList()));
    }

    private void add(Waypoint waypoint) {
        if(inRange(waypoint.getPosition())) {
            waypoints.add(waypoint);
        }
    }

    private void remove(Waypoint waypoint) {
        waypoints.remove(waypoint);
    }

    public void updateLabels() {
        labels.clear();
        visible.forEach(layer -> labels.addAll(labelStorage.getInLayer(layer).stream().filter(l -> inRange(l.getPosition())).collect(Collectors.toList())));
    }

    private void add(MapLabel label) {
        if(inRange(label.getPosition()) && visible.contains(label.getLayerID())) {
            labels.add(label);
        }
    }

    private void remove(MapLabel label) {
        labels.remove(label);
    }

    private void updateVisibleLayers() {
        visible = mapType.getLayers().stream().filter(l -> !disabled.contains(l) && l.value().shouldRenderInDimension(dimension)).collect(Collectors.toList());
        updateLabels();
    }

    private boolean inRange(BlockPos pos) {
        int x = pos.getX();
        int z = pos.getZ();
        return x >= begin.getX() && x <= end.getX() && z >= begin.getZ() && z <= end.getZ();
    }


    // =================================================================================================================


    public void render(PoseStack stack, MultiBufferSource buffers) {
        if(needsUpdate) updateTexture();

        stack.pushPose();
        Matrix4f matrix = stack.last().pose();

        RenderHelper.fillRect(buffers, matrix, this.width, this.height, 0xFF333333);
        RenderHelper.drawQuad(buffers.getBuffer(renderType), matrix, width, height);

        stack.pushPose();
        for(MapLabel l : labels) {
            renderMarker(buffers, stack, l.getPosition(), l.getIcon(), l.getColor(), l.getWidth(), l.getHeight(), l.getRotation(), l.getUsesZoom(), null);
        }
        for(Waypoint w : waypoints) {
            renderMarker(buffers, stack, w.getPosition(), w.getIcon(), w.getColor(), 32, 32, w.getRotation(), true, renderNames ? w.getLabel() : null);
        }
        LocalPlayer player = Helpers.getPlayer();
        renderMarker(buffers, stack, player.blockPosition(), PLAYER, Colors.NO_TINT, 48, 48, player.getRotationVector().y, false, null);
        stack.popPose();

        stack.popPose();
    }

    private void updateTexture() {
        NativeImage texture = mapTexture.getPixels();
        if(texture == null) return;
        int textureH = texture.getHeight();
        int textureW = texture.getWidth();
        texture.fillRect(0, 0, textureW, textureH, 0);

        int cornerXOffset = ((begin.getX() % 512) + 512) % 512;
        int cornerZOffset = ((begin.getZ() % 512) + 512) % 512;
        int regionCount = offsets.length * offsets[0].length;

        renderTimer.begin();
        if(regionCount > 24) {
            AsyncAwaiter jobs = new AsyncAwaiter(regionCount);
            for(int regionIndexX = 0; regionIndexX < offsets.length; regionIndexX++) {
                for(int regionIndexZ = 0; regionIndexZ < offsets[regionIndexX].length; regionIndexZ++) {
                    generateMapTileAsync(texture, textureW, textureH, cornerXOffset, cornerZOffset, regionIndexX, regionIndexZ, jobs);
                }
            }
            jobs.await();
        }
        else {
            for(int regionIndexX = 0; regionIndexX < offsets.length; regionIndexX++) {
                for(int regionIndexZ = 0; regionIndexZ < offsets[regionIndexX].length; regionIndexZ++) {
                    generateMapTile(texture, textureW, textureH, cornerXOffset, cornerZOffset, regionIndexX, regionIndexZ);
                }
            }
        }
        renderTimer.end();

        uploadTimer.begin();
        mapTexture.upload();
        uploadTimer.end();

        needsUpdate = false;
    }

    // Run generateMapTile in an engine background thread. Useful for parallelizing massive workloads.
    private void generateMapTileAsync(NativeImage texture, int textureW, int textureH, int cornerXOffset, int cornerZOffset, int regionIndexX, int regionIndexZ, AsyncAwaiter jobs) {
        BlazeMapEngine.async().runOnDataThread(() -> {
            generateMapTile(texture, textureW, textureH, cornerXOffset, cornerZOffset, regionIndexX, regionIndexZ);
            jobs.done();
        });
    }

    private void generateMapTile(NativeImage texture, int textureW, int textureH, int cornerXOffset, int cornerZOffset, int regionIndexX, int regionIndexZ) {
        for(BlazeRegistry.Key<Layer> layer : mapType.getLayers()) {
            if(!isLayerVisible(layer)) continue;
            final RegionPos region = offsets[regionIndexX][regionIndexZ];
            tileStorage.consumeTile(layer, region, source -> {
                for(int x = (region.x * 512) < begin.getX() ? cornerXOffset : 0; x < source.getWidth(); x++) {
                    int textureX = (regionIndexX * 512) + x - cornerXOffset;
                    if(textureX < 0 || textureX >= textureW) continue;

                    for(int y = (region.z * 512) < begin.getZ() ? cornerZOffset : 0; y < source.getHeight(); y++) {
                        int textureY = (regionIndexZ * 512) + y - cornerZOffset;
                        if(textureY < 0 || textureY >= textureH) continue;

                        int color = Colors.layerBlend(texture.getPixelRGBA(textureX, textureY), source.getPixelRGBA(x, y));
                        texture.setPixelRGBA(textureX, textureY, color);
                    }
                }
            });
        }
    }

    private void renderMarker(MultiBufferSource buffers, PoseStack stack, BlockPos position, ResourceLocation marker, int color, double width, double height, float rotation, boolean zoom, String name) {
        stack.pushPose();
        stack.scale((float) this.zoom, (float) this.zoom, 1);
        int dx = position.getX() - begin.getX();
        int dy = position.getZ() - begin.getZ();
        stack.translate(dx, dy, 0);
        if(!zoom) {
            stack.scale(1F / (float) this.zoom, 1F / (float) this.zoom, 1);
        }
        stack.mulPose(Vector3f.ZP.rotationDegrees(rotation));
        if(name != null){
            stack.pushPose();
            stack.translate((4 + (width / 2)), -8, 0);
            stack.scale(2, 2, 0);
            Minecraft.getInstance().font.drawInBatch(name, 0, 0, color, true, stack.last().pose(), buffers, false, 0, LightTexture.FULL_BRIGHT);
            stack.popPose();
        }
        stack.translate(-width / 2, -height / 2, 0);
        VertexConsumer vertices = buffers.getBuffer(RenderType.text(marker));
        RenderHelper.drawQuad(vertices, stack.last().pose(), (float) width, (float) height, color);
        stack.popPose();
    }


    // =================================================================================================================


    public boolean setMapType(MapType mapType) {
        if(this.mapType == mapType || dimension == null) return false;
        if(!mapType.shouldRenderInDimension(dimension)) return false;
        this.mapType = mapType;
        this.disabled = disabledLayers.computeIfAbsent(mapType.getID(), $ -> new LinkedList<>());
        updateVisibleLayers();
        this.needsUpdate = true;
        return true;
    }

    public MapType getMapType() {
        return mapType;
    }

    List<BlazeRegistry.Key<Layer>> getDisabledLayers() {
        return this.disabled;
    }

    void setDisabledLayers(List<BlazeRegistry.Key<Layer>> layers) {
        this.disabled.clear();
        this.disabled.addAll(layers);
        updateVisibleLayers();
        this.needsUpdate = true;
    }

    public MapRenderer setProfilers(Profiler.TimeProfiler render, Profiler.TimeProfiler upload) {
        this.renderTimer = render;
        this.uploadTimer = upload;
        return this;
    }

    public boolean setZoom(double zoom) {
        double prevZoom = this.zoom;
        zoom = Helpers.clamp(minZoom, zoom, maxZoom);
        if(prevZoom == zoom) return false;
        this.zoom = zoom;
        if(width > 0 && height > 0) {
            createImage();
        }
        return true;
    }

    public double getZoom() {
        return zoom;
    }

    public boolean toggleLayer(BlazeRegistry.Key<Layer> layer) {
        if(!mapType.getLayers().contains(layer)) return false;
        if(disabled.contains(layer)) disabled.remove(layer);
        else disabled.add(layer);
        updateVisibleLayers();
        needsUpdate = true;
        return true;
    }

    public boolean isLayerVisible(BlazeRegistry.Key<Layer> layer) {
        return !disabled.contains(layer);
    }

    public void setCenter(int x, int z) {
        this.center.set(x, 0, z);
        makeOffsets();
        needsUpdate = true;
    }

    public void moveCenter(int x, int z) {
        setCenter(center.getX() + x, center.getZ() + z);
    }

    public void centerOnPlayer() {
        LocalPlayer player = Helpers.getPlayer();
        if(player == null) {
            BlazeMap.LOGGER.warn("Ignoring request to center on player because LocalPlayer is null");
            return;
        }
        Vec3 pos = player.position();
        setCenter((int) pos.x, (int) pos.z);
    }

    public BlockPos fromBegin(int x, int y, int z) {
        return begin.offset(x, y, z);
    }

    @Override
    public void close() {
        mapTexture.close();
        RENDERERS.remove(this);
    }
}
