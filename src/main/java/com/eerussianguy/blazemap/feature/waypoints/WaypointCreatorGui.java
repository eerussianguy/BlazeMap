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
        Minecraft.getInstance().setScreen(new WaypointCreatorGui());
    }

    private EditBox fname;
    private Button save;
    private final NumericWrapper nx, ny, nz;

    private ResourceLocation icon = BlazeMapReferences.Icons.WAYPOINT;
    private RenderType iconRender;
    private String name;
    private int x, y, z;
    private int color;

    protected WaypointCreatorGui() {
        super(Helpers.translate("blazemap.gui.waypoint_creator.title"), 228, 135);

        nx = new NumericWrapper(() -> x, v -> x = v);
        ny = new NumericWrapper(() -> y, v -> y = v);
        nz = new NumericWrapper(() -> z, v -> z = v);

        name = "New Waypoint";
        Vec3 pos = Minecraft.getInstance().player.position();
        x = (int) pos.x;
        y = (int) pos.y;
        z = (int) pos.z;
        randomColor();
        iconRender = RenderType.text(icon);
    }

    private void createWaypoint(){
        waypointStorage.add(new Waypoint(
            Helpers.identifier("waypoint-" + System.currentTimeMillis()),
            getMinecraft().level.dimension(),
            new BlockPos(x, y, z),
            "Test",
            icon,
            color
        ));
        onClose();
    }

    private void randomColor(){
        color = Colors.randomBrightColor();
    }

    @Override
    protected void init() {
        super.init();

        fname = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left+12, top+25, 126, 12, this.title));
        EditBox fx = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 12, top + 40, 40, 12, this.title));
        EditBox fy = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 55, top + 40, 40, 12, this.title));
        EditBox fz = addRenderableWidget(new EditBox(Minecraft.getInstance().font, left + 98, top + 40, 40, 12, this.title));
        addRenderableWidget(new Button(left+12, top+103, 126, 20, Helpers.translate("blazemap.gui.waypoint_creator.random"), b -> randomColor()));
        save = addRenderableWidget(new Button(left+150, top+103, 66, 20, Helpers.translate("blazemap.gui.waypoint_creator.save"), b -> createWaypoint()));

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

    @Override
    protected void renderComponents(PoseStack stack, MultiBufferSource buffers) {
        renderSlot(stack, buffers, 150, 25, 66, 66);
        stack.pushPose();
        stack.translate(167, 42, 0);
        RenderHelper.drawQuad(buffers.getBuffer(iconRender), stack.last().pose(), 32, 32, color);
        stack.popPose();
    }
}
