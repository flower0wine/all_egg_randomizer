package com.alleggrandomizer.core.item;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.classifier.ItemCategoryClassifier;
import com.alleggrandomizer.core.classifier.SpecialItemClassifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Populates shulker box items with random contents.
 * Handles the logic for filling shulker boxes with random item types,
 * respecting quantity rules for weapons/equipment vs other items.
 */
public class ShulkerBoxItemPopulator {

    private static final Random RANDOM = new Random();
    private static final int MIN_ITEM_TYPES = 3;
    private static final int MAX_ITEM_TYPES = 9;
    private static final int MIN_QUANTITY_NORMAL = 1;
    private static final int MAX_QUANTITY_NORMAL = 16;
    private static final int QUANTITY_SINGLE = 1;
    private static final int INVENTORY_SIZE = 27;

    /**
     * Checks if the given item stack is a shulker box.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a shulker box
     */
    public static boolean isShulkerBox(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return SpecialItemClassifier.isShulkerBox(itemStack);
    }

    /**
     * Populates a shulker box with random items.
     * Adds 3-9 different item types with appropriate quantities.
     *
     * @param shulkerBox the shulker box item stack to populate
     * @return the populated shulker box, or the original shulker box if population fails
     */
    public static ItemStack populateShulkerBox(ItemStack shulkerBox) {
        if (!isShulkerBox(shulkerBox)) {
            AllEggRandomizer.LOGGER.warn("Attempted to populate non-shulker box item: {}", shulkerBox.getItem());
            return shulkerBox;
        }

        // Determine how many different item types to add
        int itemTypeCount = RANDOM.nextInt(MAX_ITEM_TYPES - MIN_ITEM_TYPES + 1) + MIN_ITEM_TYPES;

        // Get all valid items from registry (exclude shulker boxes and air)
        List<Item> availableItems = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }
            // Skip shulker boxes to avoid nested containers
            ItemStack testStack = item.getDefaultStack();
            if (SpecialItemClassifier.isShulkerBox(testStack)) {
                continue;
            }
            // Skip bundles to avoid nested bundles
            if (BundleItemPopulator.isBundle(testStack)) {
                continue;
            }
            availableItems.add(item);
        }

        if (availableItems.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No valid items found to populate shulker box");
            return shulkerBox;
        }

        // Create inventory with 27 slots
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

        for (int i = 0; i < itemTypeCount && i < INVENTORY_SIZE && !availableItems.isEmpty(); i++) {
            // Select a random item
            int index = RANDOM.nextInt(availableItems.size());
            Item selectedItem = availableItems.remove(index);

            // Create item stack and determine quantity
            ItemStack itemStack = new ItemStack(selectedItem);
            int quantity = determineQuantity(itemStack);

            itemStack.setCount(quantity);
            inventory.set(i, itemStack);

            AllEggRandomizer.LOGGER.debug("Added to shulker box: {} x{} (category: {})",
                selectedItem, quantity, ItemCategoryClassifier.classify(itemStack));
        }

        // Set shulker box contents using ContainerComponent
        try {
            ContainerComponent containerComponent = ContainerComponent.fromStacks(inventory);
            shulkerBox.set(DataComponentTypes.CONTAINER, containerComponent);

            AllEggRandomizer.LOGGER.info("Populated shulker box with {} item types", itemTypeCount);
        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Failed to populate shulker box contents", e);
            return shulkerBox;
        }

        return shulkerBox;
    }

    /**
     * Determines the quantity for an item based on its category.
     * Weapons/equipment/special items get 1, other items get 1-16.
     *
     * @param itemStack the item stack to determine quantity for
     * @return the quantity (1 for special items, 1-16 for others)
     */
    public static int determineQuantity(ItemStack itemStack) {
        if (ItemCategoryClassifier.isSingleQuantity(itemStack)) {
            return QUANTITY_SINGLE;
        } else {
            return RANDOM.nextInt(MAX_QUANTITY_NORMAL - MIN_QUANTITY_NORMAL + 1) + MIN_QUANTITY_NORMAL;
        }
    }
}
