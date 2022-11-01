package com.eerussianguy.blazemap.gui;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;

public class HueSlider extends ForgeSlider {
    private Consumer<Float> responder;

    public HueSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize, precision, drawString);
    }

    public HueSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, drawString);
    }

    public void setResponder(Consumer<Float> responder) {
        this.responder = responder;
    }

    @Override
    protected void applyValue() {
        if(this.responder != null) {
            responder.accept((float) this.value * 360);
        }
    }
}
