package com.alleggrandomizer.core.item;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.classifier.ItemCategoryClassifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Populates bundle items with random contents.
 * Handles the logic for filling bundles with 2-10 different item types,
 * respecting quantity rules for weapons/equipment vs other items.
 */
public class BundleItemPopulator {

    private static final Random RANDOM = new Random();
    private static final int MIN_ITEM_TYPES = 2;
    private static final int MAX_ITEM_TYPES = 10;
    private static final int MIN_QUANTITY_NORMAL = 2;
    private static final int MAX_QUANTITY_NORMAL = 5;
    private static final int QUANTITY_SINGLE = 1;

    /**
     * Checks if the given item stack is a bundle.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a bundle
     */
    public static boolean isBundle(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.isIn(ItemTags.BUNDLES);
    }

    /**
     * Populates a bundle with random items.
     * Adds 2-10 different item types with appropriate quantities.
     *
     * @param bundle the bundle item stack to populate
     * @return the populated bundle, or the original bundle if population fails
     */
    public static ItemStack populateBundle(ItemStack bundle) {
        if (!isBundle(bundle)) {
            AllEggRandomizer.LOGGER.warn("Attempted to populate non-bundle item: {}", bundle.getItem());
            return bundle;
        }

        // Determine how many different item types to add
        int itemTypeCount = RANDOM.nextInt(MAX_ITEM_TYPES - MIN_ITEM_TYPES + 1) + MIN_ITEM_TYPES;

        // Get all valid items from registry
        List<Item> availableItems = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            if (item != Items.AIR && !item.getDefaultStack().isIn(ItemTags.BUNDLES)) {
                availableItems.add(item);
            }
        }

        if (availableItems.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No valid items found to populate bundle");
            return bundle;
        }

        // Build bundle contents
        List<ItemStack> contents = new ArrayList<>();
        List<Item> selectedItems = new ArrayList<>();

        for (int i = 0; i < itemTypeCount && !availableItems.isEmpty(); i++) {
            // Select a random item
            int index = RANDOM.nextInt(availableItems.size());
            Item selectedItem = availableItems.remove(index);
            selectedItems.add(selectedItem);

            // Create item stack and determine quantity
            ItemStack itemStack = new ItemStack(selectedItem);
            int quantity = determineQuantity(itemStack);

            itemStack.setCount(quantity);
            contents.add(itemStack);

            AllEggRandomizer.LOGGER.debug("Added to bundle: {} x{} (category: {})",
                selectedItem, quantity, ItemCategoryClassifier.classify(itemStack));
        }

        // Set bundle contents
        try {
            BundleContentsComponent bundleContents = new BundleContentsComponent(contents);
            bundle.set(DataComponentTypes.BUNDLE_CONTENTS, bundleContents);

            AllEggRandomizer.LOGGER.info("Populated bundle with {} item types", itemTypeCount);
        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Failed to populate bundle contents", e);
            return bundle;
        }

        return bundle;
    }

    /**
     * Determines the quantity for an item based on its category.
     * Weapons/equipment get 1, other items get 2-5.
     *
     * @param itemStack the item stack to determine quantity for
     * @return the quantity (1 for weapons/equipment, 2-5 for others)
     */
    private static int determineQuantity(ItemStack itemStack) {
        if (ItemCategoryClassifier.isSingleQuantity(itemStack)) {
            return QUANTITY_SINGLE;
        } else {
            return RANDOM.nextInt(MAX_QUANTITY_NORMAL - MIN_QUANTITY_NORMAL + 1) + MIN_QUANTITY_NORMAL;
        }
    }
}
