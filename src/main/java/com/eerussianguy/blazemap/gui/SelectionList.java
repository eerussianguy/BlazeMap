package com.eerussianguy.blazemap.gui;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import com.eerussianguy.blazemap.util.Helpers;
import com.eerussianguy.blazemap.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

public class SelectionList<T> implements Widget, GuiEventListener, NarratableEntry {
    private final int x, y, w, h, rh, iw, ih, bx, by;
    private final EntryRenderer<T> renderer;
    private Consumer<T> responder = w -> {};
    private List<T> items = Collections.EMPTY_LIST;

    private int selected = -1, begin = 0;
    private int maxDisplay, maxBegin;
    private float scroll = 0F;

    public SelectionList(int x, int y, int w, int h, int rh, EntryRenderer<T> renderer) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.rh = rh;
        this.iw = w - 5;
        this.ih = h - 2;
        this.bx = x + 1;
        this.by = y + 1;
        this.renderer = renderer;
    }

    public SelectionList<T> setItems(List<T> items) {
        this.items = items;
        this.maxDisplay = Math.min(ih / rh, items.size());
        this.selected = -1;
        this.maxBegin = items.size() - maxDisplay;
        this.begin = Math.min(begin, maxBegin);
        if(items.size() <= maxDisplay) {
            scroll = 0F;
        }
        else {
            scroll = ((float) maxDisplay) / ((float) items.size()) * rh;
        }
        responder.accept(null);
        return this;
    }

    public SelectionList<T> setResponder(Consumer<T> responder) {
        this.responder = Objects.requireNonNull(responder);
        return this;
    }

    public T getSelected() {
        if(selected < 0 || selected >= items.size()) return null;
        return items.get(selected);
    }

    public SelectionList<T> setSelected(T item) {
        int previous = selected;
        selected = items.indexOf(item);
        if(selected != previous) {
            responder.accept(getSelected());
        }
        return this;
    }

    @Override
    public void render(PoseStack stack, int mx, int my, float p) {
        stack.pushPose();
        stack.translate(x, y, 0);

        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        RenderHelper.drawFrame(buffers.getBuffer(RenderType.text(BlazeGui.SLOT)), stack, w, h, 1);

        stack.pushPose();
        stack.translate(1 + iw, 1, 0);
        RenderHelper.fillRect(buffers, stack.last().pose(), 3, ih, 0x80000000);
        if(scroll > 0F) {
            stack.translate(0, begin * scroll, 1);
            RenderHelper.fillRect(buffers, stack.last().pose(), 3, ((float) maxDisplay) * scroll, 0xFFC6C6C6);
        }
        stack.popPose();
        buffers.endBatch();
        stack.popPose();

        stack.pushPose();
        stack.translate(bx, by, 0);
        for(int i = 0; i < maxDisplay; i++) {
            int idx = begin + i;
            T item = items.get(idx);
            stack.pushPose();
            if(idx == selected) {
                RenderHelper.fillRect(stack.last().pose(), iw, rh, 0x40000000);
            }
            renderer.render(stack, item);
            stack.popPose();
            stack.translate(0, rh, 0);
        }
        stack.popPose();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double d) {
        if(!isMouseOver(x, y)) return false;
        begin = Helpers.clamp(0, (int) (begin - d), maxBegin);
        return true;
    }

    @Override
    public boolean mouseClicked(double x, double y, int b) {
        if(!isMouseOver(x, y)) return false;
        int slot = (int) ((y - by) / rh);
        if(slot > maxDisplay - 1) return false;
        selected = begin + slot;
        responder.accept(items.get(selected));
        return true;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        return x >= bx && x <= bx + iw && y >= by && y <= by + ih;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {}

    @FunctionalInterface
    public interface EntryRenderer<T> {
        void render(PoseStack stack, T item);
    }
}
