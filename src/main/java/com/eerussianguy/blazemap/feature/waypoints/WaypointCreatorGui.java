package com.eerussianguy.blazemap.feature.waypoints;

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
import com.eerussianguy.blazemap.gui.BlazeGui;
import com.eerussianguy.blazemap.gui.NumericWrapper;
import com.eerussianguy.blazemap.gui.SelectionList;
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
    private final NumericWrapper nx, ny, nz;

    private final Waypoint waypoint;
    private ResourceLocation icon = BlazeMapReferences.Icons.WAYPOINT;
    private RenderType iconRender;
    private String name;
    private int x, y, z;
    private int color;

    protected WaypointCreatorGui(Waypoint waypoint) {
        super(Helpers.translate("blazemap.gui.waypoint_editor.title"), 228, 182);
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
            randomColor();
        }
        else {
            name = waypoint.getLabel();
            BlockPos pos = waypoint.getPosition();
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
            color = waypoint.getColor();
        }

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
            waypoint.setLabel(name);
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
        color = Colors.randomBrightColor();
    }

    @Override
    protected void init() {
        super.init();

        EditBox fname = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 12, top + 25, 126, 12, this.title));
        EditBox fx = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 12, top + 40, 40, 12, this.title));
        EditBox fy = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 55, top + 40, 40, 12, this.title));
        EditBox fz = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 98, top + 40, 40, 12, this.title));

        addRenderableWidget(new SelectionList<>(left + 12, top + 55, 126, 92, 18, this::renderIcon))
            .setResponder(this::onSelect)
            .setItems(BlazeMapReferences.Icons.ALL_WAYPOINTS)
            .setSelected(icon);

        addRenderableWidget(new Button(left + 12, top + 150, 126, 20, Helpers.translate("blazemap.gui.waypoint_editor.random"), b -> randomColor()));
        save = addRenderableWidget(new Button(left + 150, top + 150, 66, 20, Helpers.translate("blazemap.gui.waypoint_editor.save"), b -> createWaypoint()));

        fname.setValue(name);
        fx.setValue(String.valueOf(x));
        fy.setValue(String.valueOf(y));
        fz.setValue(String.valueOf(z));

        fname.setResponder(n -> {
            name = n;
            save.active = n != null && !n.equals("");
        });
        nx.setSubject(fx);
        ny.setSubject(fy);
        nz.setSubject(fz);
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
        renderSlot(stack, buffers, 150, 25, 66, 66);
        stack.pushPose();
        stack.translate(167, 42, 0);
        RenderHelper.drawQuad(buffers.getBuffer(iconRender), stack.last().pose(), 32, 32, color);
        stack.popPose();
    }
}
