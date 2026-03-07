package com.alleggrandomizer.gui;

import com.alleggrandomizer.config.CategoryConfig;
import com.alleggrandomizer.config.CategoryType;
import com.alleggrandomizer.config.ConfigManager;
import com.alleggrandomizer.config.GlobalSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Main configuration screen for the All Egg Randomizer mod.
 * Provides a simple, intuitive GUI for adjusting category settings.
 * 
 * Implementation for SPEC-05: UI Configuration Panel System
 */
public class EggConfigScreen extends Screen {

    private static final int PADDING = 20;
    private static final int SECTION_GAP = 30;
    private static final int BOTTOM_BUTTON_MARGIN = 40;
    private static final int TITLE_HEIGHT = 30;
    private static final int LABEL_HEIGHT = 20;
    
    private static final int CATEGORY_BUTTON_WIDTH = 150;
    private static final int CATEGORY_BUTTON_HEIGHT = 28;
    private static final int BUTTON_SPACING = 4;
    
    // Minimum panel dimensions
    private static final int MIN_EDIT_PANEL_WIDTH = 300;
    private static final int MIN_GLOBAL_HEIGHT = 70;
    
    private final Screen parent;
    private final ConfigManager configManager;
    private CategoryType selectedCategory;
    
    // Category selection buttons
    private final List<CategoryButton> categoryButtons = new ArrayList<>();
    
    // Edit panel
    private EditPanelWidget editPanel;
    
    // Global settings
    private GlobalSettingsWidget globalSettings;
    
    // Action buttons
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;
    
    // Layout cache
    private int leftPanelWidth;
    private int contentTopY;
    
    public EggConfigScreen(Screen parent) {
        super(Text.of("All Egg Randomizer 配置"));
        this.parent = parent;
        this.configManager = ConfigManager.getInstance();
        this.selectedCategory = CategoryType.ENTITY;
    }

    @Override
    protected void init() {
        super.init();
        
        // Calculate layout bounds
        calculateLayout();
        
        // Create category selection buttons
        createCategoryButtons();
        
        // Create edit panel
        createEditPanel();
        
        // Create global settings
        createGlobalSettings();
        
        // Create action buttons
        createActionButtons();
        
        // Load initial category
        selectCategory(selectedCategory);
    }
    
    /**
     * Calculate layout dimensions based on screen size.
     */
    private void calculateLayout() {
        // Left panel width for category buttons
        leftPanelWidth = CATEGORY_BUTTON_WIDTH + PADDING * 2;
        
        // Content starts below title and section labels
        contentTopY = TITLE_HEIGHT + LABEL_HEIGHT + 10;
    }

    /**
     * Create category selection buttons on the left side.
     */
    private void createCategoryButtons() {
        categoryButtons.clear();
        
        int buttonX = PADDING;
        int buttonY = contentTopY;
        
        // Check if we have enough vertical space
        int totalButtonHeight = CategoryType.values().length * (CATEGORY_BUTTON_HEIGHT + BUTTON_SPACING) - BUTTON_SPACING;
        int maxButtonY = this.height - BOTTOM_BUTTON_MARGIN - MIN_GLOBAL_HEIGHT - 50;
        
        if (buttonY + totalButtonHeight > maxButtonY) {
            // Reduce button height to fit
            buttonY = maxButtonY - totalButtonHeight;
        }
        
        for (CategoryType type : CategoryType.values()) {
            CategoryButton button = new CategoryButton(
                buttonX,
                buttonY,
                CATEGORY_BUTTON_WIDTH,
                CATEGORY_BUTTON_HEIGHT,
                type,
                btn -> selectCategory(type)
            );
            categoryButtons.add(button);
            this.addDrawableChild(button);
            buttonY += CATEGORY_BUTTON_HEIGHT + BUTTON_SPACING;
        }
    }

    /**
     * Create edit panel on the right side.
     */
    private void createEditPanel() {
        int editPanelX = leftPanelWidth + SECTION_GAP;
        int editPanelY = contentTopY;
        
        // Calculate available width
        int availableWidth = this.width - editPanelX - PADDING;
        
        // Ensure minimum width
        editPanelWidth = Math.max(availableWidth, MIN_EDIT_PANEL_WIDTH);
        
        // Clamp to screen bounds
        if (editPanelX + editPanelWidth > this.width) {
            editPanelWidth = this.width - editPanelX - PADDING;
        }
        
        // Calculate height - leave space for global settings and buttons
        int bottomReserve = BOTTOM_BUTTON_MARGIN + MIN_GLOBAL_HEIGHT + 30;
        int editPanelHeight = this.height - editPanelY - bottomReserve;
        editPanelHeight = Math.max(editPanelHeight, 200); // Minimum height
        
        this.editPanel = new EditPanelWidget(
            editPanelX,
            editPanelY,
            editPanelWidth,
            editPanelHeight,
            this.textRenderer
        );
        this.addDrawableChild(editPanel);
    }
    
    // Add instance variable for edit panel dimensions
    private int editPanelWidth;

    /**
     * Create global settings widget at the bottom.
     */
    private void createGlobalSettings() {
        // Position below edit panel
        int globalY = this.height - BOTTOM_BUTTON_MARGIN - MIN_GLOBAL_HEIGHT;
        int globalWidth = this.width - PADDING * 2;
        
        // Ensure it doesn't exceed screen bounds
        globalWidth = Math.min(globalWidth, this.width - PADDING * 2);
        
        // Clamp Y position
        globalY = Math.min(globalY, this.height - BOTTOM_BUTTON_MARGIN - 30);
        
        this.globalSettings = new GlobalSettingsWidget(
            PADDING,
            globalY,
            globalWidth,
            MIN_GLOBAL_HEIGHT,
            this.textRenderer
        );
        this.addDrawableChild(globalSettings);
    }

    /**
     * Create save/cancel buttons at the bottom center.
     */
    private void createActionButtons() {
        int bottomY = this.height - 25;
        int buttonWidth = 100;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        
        this.saveButton = ButtonWidget.builder(
            Text.of("保存"),
            button -> {
                saveConfiguration();
                this.close();
            }
        )
        .dimensions(centerX - buttonWidth - 10, bottomY, buttonWidth, buttonHeight)
        .build();
        this.addDrawableChild(saveButton);
        
        this.cancelButton = ButtonWidget.builder(
            Text.of("取消"),
            button -> this.close()
        )
        .dimensions(centerX + 10, bottomY, buttonWidth, buttonHeight)
        .build();
        this.addDrawableChild(cancelButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render simple background without blur
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        // Render title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            this.width / 2,
            10,
            0xFFFFFF
        );
        
        // Render section labels
        context.drawTextWithShadow(
            this.textRenderer,
            Text.of("分类列表"),
            PADDING,
            TITLE_HEIGHT,
            0xAAAAAA
        );
        
        context.drawTextWithShadow(
            this.textRenderer,
            Text.of("编辑详情"),
            leftPanelWidth + SECTION_GAP,
            TITLE_HEIGHT,
            0xAAAAAA
        );
        
        // Render all widgets
        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Select a category and update the edit panel.
     */
    private void selectCategory(CategoryType category) {
        this.selectedCategory = category;
        
        // Update button states
        for (CategoryButton button : categoryButtons) {
            button.setSelected(button.getCategory() == category);
        }
        
        // Load category data into edit panel
        CategoryConfig config = configManager.getCategoryConfig(category);
        if (config != null && editPanel != null) {
            editPanel.loadCategory(category, config);
        }
    }

    /**
     * Save all configuration changes.
     */
    private void saveConfiguration() {
        // Save current category from edit panel
        if (editPanel != null) {
            CategoryConfig config = configManager.getCategoryConfig(selectedCategory);
            if (config != null) {
                editPanel.saveToConfig(config);
            }
        }
        
        // Save global settings
        if (globalSettings != null) {
            GlobalSettings settings = configManager.getGlobalSettings();
            if (settings != null) {
                globalSettings.saveToConfig(settings);
            }
        }
        
        // Persist to file
        configManager.saveConfig();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /**
     * Custom button for category selection with status indicator.
     */
    private class CategoryButton extends ButtonWidget {
        private final CategoryType category;
        private boolean selected;

        public CategoryButton(int x, int y, int width, int height, CategoryType category, PressAction onPress) {
            super(x, y, width, height, net.minecraft.text.Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
            this.category = category;
            this.selected = false;
            updateMessage();
        }

        public CategoryType getCategory() {
            return category;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            updateMessage();
        }

        private void updateMessage() {
            CategoryConfig config = configManager.getCategoryConfig(category);
            boolean enabled = config != null && config.isEnabled();
            
            String indicator = enabled ? "§a✓" : "§7✗";
            String name = category.getDisplayName();
            
            if (selected) {
                this.setMessage(net.minecraft.text.Text.of("§e▶ " + indicator + " " + name));
            } else {
                this.setMessage(net.minecraft.text.Text.of(indicator + " " + name));
            }
        }

        @Override
        protected void drawIcon(DrawContext context, int x, int y, float delta) {
            // No icon needed
        }
    }
}
