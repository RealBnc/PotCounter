package com.potcounter.gui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class CustomSliderWidget extends SliderWidget {
    private final String prefix;
    private final String suffix;
    private final double min;
    private final double max;
    private final boolean isInteger;
    private final Consumer<Double> onValueChange;

    public CustomSliderWidget(int x, int y, int width, int height, String prefix, String suffix, double min, double max, double current, Consumer<Double> onValueChange) {
        super(x, y, width, height, Text.empty(), (current - min) / (max - min));
        this.prefix = prefix;
        this.suffix = suffix;
        this.min = min;
        this.max = max;
        this.isInteger = (max - min > 2.0 && Math.floor(min) == min && Math.floor(max) == max);
        this.onValueChange = onValueChange;
        updateMessage();
    }

    public double getActualValue() {
        double raw = min + (value * (max - min));
        if (isInteger) {
            return Math.round(raw);
        }
        return Math.round(raw * 100.0) / 100.0;
    }

    @Override
    protected void updateMessage() {
        double val = getActualValue();
        String formatted = isInteger ? String.format("%.0f", val) : String.format("%.2f", val);
        setMessage(Text.literal(prefix + ": " + formatted + suffix));
    }

    @Override
    protected void applyValue() {
        if (onValueChange != null) {
            onValueChange.accept(getActualValue());
        }
    }
}
