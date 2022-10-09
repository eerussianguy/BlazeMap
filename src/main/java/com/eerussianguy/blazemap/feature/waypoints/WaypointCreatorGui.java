package com.eerussianguy.blazemap.feature.waypoints;

import java.awt.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import com.eerussianguy.blazemap.api.BlazeMapReferences;
import com.eerussianguy.blazemap.api.event.DimensionChangedEvent;
import com.eerussianguy.blazemap.api.markers.IMarkerStorage;
import com.eerussianguy.blazemap.api.markers.Waypoint;
import com.eerussianguy.blazemap.gui.*;
import com.eerussianguy.blazemap.util.Colors;
import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;

public class WaypointCreatorGui extends BlazeGui {
    private static IMarkerStorage<Waypoint> waypointStorage;

    public static void onDimensionChanged(DimensionChangedEvent event) {
        waypointStorage = event.waypoints;
    }

    public static void open() {
        open(null);
    }

    public static void open(Waypoint waypoint) {
        Minecraft.getInstance().setScreen(new WaypointCreatorGui(waypoint));
    }

    private Button save;
    private HueSlider slider;
    private SaturationBrightnessSelector sbs;
    private final NumericWrapper nx, ny, nz;

    private final Waypoint waypoint;
    private ResourceLocation icon = BlazeMapReferences.Icons.WAYPOINT;
    private RenderType iconRender;
    private String name;
    private int x, y, z;
    private int color;
    private float hue360, s, b;

    protected WaypointCreatorGui(Waypoint waypoint) {
        super(Helpers.translate("blazemap.gui.waypoint_editor.title"), 212, 202);
        this.waypoint = waypoint;

        nx = new NumericWrapper(() -> x, v -> x = v);
        ny = new NumericWrapper(() -> y, v -> y = v);
        nz = new NumericWrapper(() -> z, v -> z = v);

        if(waypoint == null) {
            name = "New Waypoint";
            Vec3 pos = Minecraft.getInstance().player.position();
            x = (int) pos.x;
            y = (int) pos.y;
            z = (int) pos.z;
            color = Colors.randomBrightColor();
        }
        else {
            name = waypoint.getName();
            BlockPos pos = waypoint.getPosition();
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
            color = waypoint.getColor();
            icon = waypoint.getIcon();
        }

        float[] hsb = new float[3];
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        Color.RGBtoHSB(r, g, b, hsb);
        this.hue360 = hsb[0] * 360;
        this.s = hsb[1];
        this.b = hsb[2];

        iconRender = RenderType.text(icon);
    }

    private void createWaypoint() {
        if(waypoint == null) {
            waypointStorage.add(new Waypoint(
                Helpers.identifier("waypoint-" + System.currentTimeMillis()),
                getMinecraft().level.dimension(),
                new BlockPos(x, y, z),
                name,
                icon,
                color
            ));
        }
        else {
            waypoint.setName(name);
            waypoint.setPosition(new BlockPos(x, y, z));
            waypoint.setColor(color);
            waypoint.setIcon(icon);
            // TODO: replace remove + add with a proper changed event
            waypointStorage.remove(waypoint);
            waypointStorage.add(waypoint);
        }
        onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
        if(waypoint != null) {
            WaypointManagerGui.open();
        }
    }

    private void randomColor() {
        hue360 = ((float) System.nanoTime() % 360);
        s = 1;
        b = 1;
        slider.setValue(hue360);
        sbs.setHue360(hue360);
        sbs.setSB(s, b);
    }

    @Override
    protected void init() {
        super.init();

        EditBox fname = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 12, top + 25, 126, 12, this.title));
        EditBox fx = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 12, top + 40, 40, 12, this.title));
        EditBox fy = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 55, top + 40, 40, 12, this.title));
        EditBox fz = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 98, top + 40, 40, 12, this.title));

        addRenderableWidget(new SelectionList<>(left + 12, top + 55, 126, 112, 18, this::renderIcon))
            .setResponder(this::onSelect)
            .setItems(BlazeMapReferences.Icons.ALL_WAYPOINTS)
            .setSelected(icon);

        addRenderableWidget(new Button(left + 12, top + 170, 126, 20, Helpers.translate("blazemap.gui.waypoint_editor.random"), b -> randomColor()));
        save = addRenderableWidget(new Button(left + 150, top + 170, 50, 20, Helpers.translate("blazemap.gui.waypoint_editor.save"), b -> createWaypoint()));

        fname.setValue(name);
        fx.setValue(String.valueOf(x));
        fy.setValue(String.valueOf(y));
        fz.setValue(String.valueOf(z));

        sbs = addRenderableWidget(new SaturationBrightnessSelector(left + 150, top + 87, 50, 50));
        sbs.setResponder((s, b) -> {
            this.s = s;
            this.b = b;
            updateColor();
        });
        sbs.setHue360(hue360);
        sbs.setSB(s, b);

        slider = addRenderableWidget(new HueSlider(left + 150, top + 140, 50, 20, null, null, 0, 360, 1, 6, 1, false));
        slider.setResponder(hue -> {
            sbs.setHue360(hue);
            this.hue360 = hue;
            updateColor();
        });
        slider.setValue(hue360);

        fname.setResponder(n -> {
            name = n;
            save.active = n != null && !n.equals("");
        });
        nx.setSubject(fx);
        ny.setSubject(fy);
        nz.setSubject(fz);
    }

    private void updateColor() {
        color = Color.HSBtoRGB(hue360 / 360, s, b);
    }

    private void onSelect(ResourceLocation icon) {
        if(icon == null) icon = BlazeMapReferences.Icons.WAYPOINT;
        this.icon = icon;
        this.iconRender = RenderType.text(icon);
    }

    private void renderIcon(PoseStack stack, ResourceLocation icon) {
        RenderHelper.drawTexturedQuad(icon, -1, stack, 2, 1, 16, 16);
        String[] path = icon.getPath().split("/");
        font.draw(stack, path[path.length - 1].split("\\.")[0], 20, 5, -1);
    }

    @Override
    protected void renderComponents(PoseStack stack, MultiBufferSource buffers) {
        renderSlot(stack, buffers, 150, 25, 50, 50);
        stack.pushPose();
        stack.translate(159, 34, 0);
        RenderHelper.drawQuad(buffers.getBuffer(iconRender), stack.last().pose(), 32, 32, color);
        stack.popPose();
    }
}
