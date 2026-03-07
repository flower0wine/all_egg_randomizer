package com.alleggrandomizer.gui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * Custom slider widget for adjusting category weight.
 * Range: 0.0 - 10.0 with 0.1 step precision.
 */
public class WeightSliderWidget extends SliderWidget {
    
    private static final double MIN_WEIGHT = 0.0;
    private static final double MAX_WEIGHT = 10.0;
    
    private double weightValue;

    public WeightSliderWidget(int x, int y, int width, int height, double initialValue) {
        super(x, y, width, height, Text.empty(), normalizeValue(initialValue));
        this.weightValue = clampValue(initialValue);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.weightValue = denormalizeValue(this.value);
        this.setMessage(net.minecraft.text.Text.of(String.format("%.1f", weightValue)));
    }

    @Override
    protected void applyValue() {
        this.weightValue = denormalizeValue(this.value);
    }

    /**
     * Get the current weight value.
     */
    public double getValue() {
        return weightValue;
    }

    /**
     * Set the weight value programmatically.
     */
    public void setValue(double value) {
        this.weightValue = clampValue(value);
        this.value = normalizeValue(this.weightValue);
        updateMessage();
    }

    /**
     * Normalize weight value to slider range [0.0, 1.0].
     */
    private static double normalizeValue(double weight) {
        return clampValue(weight) / MAX_WEIGHT;
    }

    /**
     * Denormalize slider value to weight range [0.0, 10.0].
     */
    private static double denormalizeValue(double sliderValue) {
        return Math.round(sliderValue * MAX_WEIGHT * 10.0) / 10.0; // Round to 1 decimal
    }

    /**
     * Clamp value to valid weight range.
     */
    private static double clampValue(double value) {
        return Math.max(MIN_WEIGHT, Math.min(MAX_WEIGHT, value));
    }
}
