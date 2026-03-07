package com.alleggrandomizer.gui;

import com.alleggrandomizer.config.GlobalSettings;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Widget for global settings like max spawn count.
 */
public class GlobalSettingsWidget extends ClickableWidget {

    // Layout constants
    private static final int CONTENT_PADDING = 12;
    private static final int LABEL_TO_WIDGET_GAP = 5;
    private static final int SLIDER_HEIGHT = 20;
    
    private final TextRenderer textRenderer;
    private final List<ClickableWidget> children = new ArrayList<>();
    
    private int maxSpawnPerThrow;
    private SliderWidget maxSpawnSlider;

    public GlobalSettingsWidget(int x, int y, int width, int height, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());
        this.textRenderer = textRenderer;
        this.maxSpawnPerThrow = 5; // Default
        initializeWidgets();
    }

    private void initializeWidgets() {
        // Create slider with default value
        this.maxSpawnSlider = createSlider(maxSpawnPerThrow);
        children.add(maxSpawnSlider);
        updateWidgetPositions();
    }
    
    /**
     * Create a new slider with specified initial value.
     */
    private SliderWidget createSlider(int initialValue) {
        return new SliderWidget(
            0, 0, 200, SLIDER_HEIGHT,
            Text.empty(),
            (initialValue - 1) / 99.0
        ) {
            @Override
            protected void updateMessage() {
                maxSpawnPerThrow = (int) (this.value * 99) + 1;
                this.setMessage(net.minecraft.text.Text.of(String.valueOf(maxSpawnPerThrow)));
            }
            
            @Override
            protected void applyValue() {
                maxSpawnPerThrow = (int) (this.value * 99) + 1;
            }
        };
    }
    
    /**
     * Update widget positions based on current dimensions.
     */
    private void updateWidgetPositions() {
        int contentX = this.getX() + CONTENT_PADDING;
        int contentY = this.getY() + 30; // Below title
        
        // Calculate slider width based on available space
        int sliderMaxWidth = 200;
        int availableWidth = this.width - CONTENT_PADDING * 2;
        int sliderWidth = Math.min(sliderMaxWidth, availableWidth - 120); // Leave space for value display
        
        maxSpawnSlider.setWidth(Math.max(120, sliderWidth));
        maxSpawnSlider.setPosition(contentX, contentY);
    }

    /**
     * Load global settings into the widget.
     */
    public void loadSettings(GlobalSettings settings) {
        this.maxSpawnPerThrow = settings.getMaxSpawnPerThrow();
        // Recreate slider with correct initial value (setValue is protected)
        children.clear();
        this.maxSpawnSlider = createSlider(this.maxSpawnPerThrow);
        children.add(maxSpawnSlider);
        updateWidgetPositions();
    }

    /**
     * Save current values to global settings.
     */
    public void saveToConfig(GlobalSettings settings) {
        settings.setMaxSpawnPerThrow(maxSpawnPerThrow);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw panel background
        context.fill(this.getX(), this.getY(), this.getX() + width, this.getY() + height, 0x80000000);
        
        // Draw border manually
        int x = this.getX();
        int y = this.getY();
        context.fill(x, y, x + width, y + 1, 0xFF404040); // Top
        context.fill(x, y + height - 1, x + width, y + height, 0xFF404040); // Bottom
        context.fill(x, y, x + 1, y + height, 0xFF404040); // Left
        context.fill(x + width - 1, y, x + width, y + height, 0xFF404040); // Right
        
        // Update widget positions
        updateWidgetPositions();
        
        int contentX = this.getX() + CONTENT_PADDING;
        int contentY = this.getY() + CONTENT_PADDING;
        
        // Title
        context.drawTextWithShadow(
            textRenderer,
            net.minecraft.text.Text.of("§e全局设置"),
            contentX,
            contentY,
            0xFFFFFF
        );
        
        // Max spawn label
        int labelY = contentY + 25;
        context.drawTextWithShadow(
            textRenderer,
            net.minecraft.text.Text.of("最大生成数量:"),
            contentX,
            labelY,
            0xAAAAAA
        );
        
        // Current value display - position next to slider
        int valueX = contentX + maxSpawnSlider.getWidth() + 15;
        int valueY = contentY + 30 + LABEL_TO_WIDGET_GAP;
        context.drawTextWithShadow(
            textRenderer,
            net.minecraft.text.Text.of("当前值: §e" + maxSpawnPerThrow),
            valueX,
            valueY,
            0xFFFFFF
        );
        
        // Render child widgets
        for (ClickableWidget child : children) {
            child.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        updateWidgetPositions();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        updateWidgetPositions();
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        // Add narration for accessibility
    }
}
