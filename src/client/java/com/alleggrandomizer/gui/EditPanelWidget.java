package com.alleggrandomizer.gui;

import com.alleggrandomizer.config.CategoryConfig;
import com.alleggrandomizer.config.CategoryType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit panel widget for modifying category settings.
 * Displays enable/disable toggle, weight slider, and category information.
 */
public class EditPanelWidget extends ClickableWidget {

    // Layout constants
    private static final int CONTENT_PADDING = 12;
    private static final int SECTION_SPACING = 25;
    private static final int LABEL_TO_WIDGET_GAP = 5;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SLIDER_HEIGHT = 20;
    private static final int RESET_BUTTON_WIDTH = 60;
    
    private final TextRenderer textRenderer;
    private final List<ClickableWidget> children = new ArrayList<>();
    
    private CategoryType currentCategory;
    private boolean enabled;
    private double weight;
    
    private ButtonWidget toggleButton;
    private WeightSliderWidget weightSlider;
    private ButtonWidget resetButton;
    
    public EditPanelWidget(int x, int y, int width, int height, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());
        this.textRenderer = textRenderer;
        initializeWidgets();
    }

    private void initializeWidgets() {
        // Widgets will be repositioned in repositionWidgets() which is called after dimensions are set
        // Create placeholder widgets first
        this.toggleButton = ButtonWidget.builder(
            net.minecraft.text.Text.of("启用"),
            button -> {
                enabled = !enabled;
                updateToggleButton();
            }
        )
        .dimensions(0, 0, 100, BUTTON_HEIGHT)
        .build();
        children.add(toggleButton);
        
        this.weightSlider = new WeightSliderWidget(
            0, 0, 200, SLIDER_HEIGHT, 1.0
        );
        children.add(weightSlider);
        
        this.resetButton = ButtonWidget.builder(
            net.minecraft.text.Text.of("重置"),
            button -> {
                weight = 1.0;
                weightSlider.setValue(weight);
            }
        )
        .dimensions(0, 0, RESET_BUTTON_WIDTH, BUTTON_HEIGHT)
        .build();
        children.add(resetButton);
    }
    
    /**
     * Reposition child widgets based on current panel dimensions.
     */
    private void repositionWidgets() {
        int contentX = this.getX() + CONTENT_PADDING;
        int contentWidth = this.width - CONTENT_PADDING * 2;
        
        // Toggle button - full width centered or left-aligned
        int toggleWidth = Math.min(120, contentWidth / 3);
        toggleButton.setDimensions(toggleWidth, BUTTON_HEIGHT);
        
        // Weight slider - use remaining width after reset button
        int sliderWidth = Math.max(150, contentWidth - RESET_BUTTON_WIDTH - 10);
        weightSlider.setWidth(sliderWidth);
        
        // Reset button position depends on slider
    }
    
    /**
     * Update widget positions based on current dimensions.
     */
    private void updateWidgetPositions() {
        int contentX = this.getX() + CONTENT_PADDING;
        int contentY = this.getY() + 40; // After category name header
        
        // Toggle button position
        toggleButton.setPosition(contentX, contentY);
        
        // Weight slider position
        weightSlider.setPosition(contentX, contentY + SECTION_SPACING + 20);
        
        // Reset button - right next to slider
        resetButton.setPosition(
            contentX + weightSlider.getWidth() + 10,
            contentY + SECTION_SPACING + 20
        );
    }

    /**
     * Load category data into the panel.
     */
    public void loadCategory(CategoryType category, CategoryConfig config) {
        this.currentCategory = category;
        this.enabled = config.isEnabled();
        this.weight = config.getWeight();
        
        updateToggleButton();
        if (weightSlider != null) {
            weightSlider.setValue(weight);
        }
        
        // Recalculate widget positions after loading
        updateWidgetPositions();
    }

    /**
     * Save current values to config.
     */
    public void saveToConfig(CategoryConfig config) {
        config.setEnabled(enabled);
        config.setWeight(weightSlider != null ? weightSlider.getValue() : weight);
    }

    private void updateToggleButton() {
        if (toggleButton != null) {
            if (enabled) {
                toggleButton.setMessage(net.minecraft.text.Text.of("§a✓ 已启用"));
            } else {
                toggleButton.setMessage(net.minecraft.text.Text.of("§7✗ 已禁用"));
            }
        }
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
        
        if (currentCategory == null) {
            return;
        }
        
        // Ensure widgets are properly positioned
        updateWidgetPositions();
        
        int contentX = this.getX() + CONTENT_PADDING;
        int contentY = this.getY() + CONTENT_PADDING;
        
        // Category name header
        String categoryName = currentCategory.getDisplayName() + " (" + currentCategory.name() + ")";
        context.drawTextWithShadow(
            textRenderer,
            net.minecraft.text.Text.of("当前选择: §e" + categoryName),
            contentX,
            contentY,
            0xFFFFFF
        );
        
        // Separator line
        context.fill(contentX, contentY + 18, this.getX() + width - CONTENT_PADDING, contentY + 19, 0xFF505050);
        
        // Enable/Disable section
        int toggleLabelY = contentY + 30;
        context.drawTextWithShadow(
            textRenderer,
            net.minecraft.text.Text.of("状态:"),
            contentX,
            toggleLabelY,
            0xAAAAAA
        );
        
        // Weight section
        int weightLabelY = toggleLabelY + SECTION_SPACING + BUTTON_HEIGHT;
        context.drawTextWithShadow(
            textRenderer,
            net.minecraft.text.Text.of("权重:"),
            contentX,
            weightLabelY,
            0xAAAAAA
        );
        
        // Weight value display
        double currentWeight = weightSlider != null ? weightSlider.getValue() : weight;
        int weightValueY = weightLabelY + LABEL_TO_WIDGET_GAP + SLIDER_HEIGHT + 5;
        context.drawTextWithShadow(
            textRenderer,
            net.minecraft.text.Text.of(String.format("当前值: §e%.1f", currentWeight)),
            contentX,
            weightValueY,
            0xFFFFFF
        );
        
        // Category description
        int descY = weightValueY + 25;
        if (descY + 30 < this.getY() + height) {
            context.drawTextWithShadow(
                textRenderer,
                net.minecraft.text.Text.of("§7" + currentCategory.getDescription()),
                contentX,
                descY,
                0x888888
            );
        }
        
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

    public boolean mouseClickedCustom(double mouseX, double mouseY, int button) {
        for (ClickableWidget child : children) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        // Add narration for accessibility
    }
}
