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
 * Populates chest and trap chest items with random contents.
 * Handles the logic for filling chests with random item types,
 * respecting quantity rules for weapons/equipment vs other items.
 * Items are randomly distributed across the inventory slots to create a messy appearance.
 * 
 * - Chest: 1-4 different item types with quantity 1-8
 * - Trap chest: 2-6 different item types with quantity 1-8
 */
public class ChestItemPopulator {

    private static final Random RANDOM = new Random();
    
    // Chest settings
    private static final int CHEST_MIN_ITEM_TYPES = 1;
    private static final int CHEST_MAX_ITEM_TYPES = 4;
    
    // Trap chest settings
    private static final int TRAP_CHEST_MIN_ITEM_TYPES = 2;
    private static final int TRAP_CHEST_MAX_ITEM_TYPES = 6;
    
    // Quantity settings (both use 1-8)
    private static final int MIN_QUANTITY_NORMAL = 1;
    private static final int MAX_QUANTITY_NORMAL = 8;
    private static final int QUANTITY_SINGLE = 1;
    
    // Inventory sizes
    private static final int CHEST_INVENTORY_SIZE = 27;
    private static final int TRAP_CHEST_INVENTORY_SIZE = 27;

    /**
     * Chest type enumeration.
     */
    public enum ChestType {
        CHEST,
        TRAP_CHEST
    }

    /**
     * Checks if the given item stack is a chest.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a chest
     */
    public static boolean isChest(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return SpecialItemClassifier.isChest(itemStack);
    }

    /**
     * Checks if the given item stack is a trap chest.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a trap chest
     */
    public static boolean isTrapChest(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return SpecialItemClassifier.isTrapChest(itemStack);
    }

    /**
     * Determines the chest type from the item stack.
     *
     * @param itemStack the item stack to check
     * @return the chest type, or null if not a chest
     */
    public static ChestType getChestType(ItemStack itemStack) {
        if (isChest(itemStack)) {
            return ChestType.CHEST;
        }
        if (isTrapChest(itemStack)) {
            return ChestType.TRAP_CHEST;
        }
        return null;
    }

    /**
     * Populates a chest with random items.
     * Adds 1-4 different item types with quantity 1-8.
     *
     * @param chest the chest item stack to populate
     * @return the populated chest, or the original chest if population fails
     */
    public static ItemStack populateChest(ItemStack chest) {
        return populateChestInternal(chest, ChestType.CHEST);
    }

    /**
     * Populates a trap chest with random items.
     * Adds 2-6 different item types with quantity 1-8.
     *
     * @param trapChest the trap chest item stack to populate
     * @return the populated trap chest, or the original trap chest if population fails
     */
    public static ItemStack populateTrapChest(ItemStack trapChest) {
        return populateChestInternal(trapChest, ChestType.TRAP_CHEST);
    }

    /**
     * Internal method to populate chest items.
     *
     * @param chest the chest item stack to populate
     * @param chestType the type of chest
     * @return the populated chest
     */
    private static ItemStack populateChestInternal(ItemStack chest, ChestType chestType) {
        if (chest == null || chest.isEmpty()) {
            return chest;
        }

        // Determine settings based on chest type
        int minItemTypes, maxItemTypes, inventorySize;
        switch (chestType) {
            case TRAP_CHEST:
                minItemTypes = TRAP_CHEST_MIN_ITEM_TYPES;
                maxItemTypes = TRAP_CHEST_MAX_ITEM_TYPES;
                inventorySize = TRAP_CHEST_INVENTORY_SIZE;
                break;
            case CHEST:
            default:
                minItemTypes = CHEST_MIN_ITEM_TYPES;
                maxItemTypes = CHEST_MAX_ITEM_TYPES;
                inventorySize = CHEST_INVENTORY_SIZE;
                break;
        }

        // Determine how many different item types to add
        int itemTypeCount = RANDOM.nextInt(maxItemTypes - minItemTypes + 1) + minItemTypes;

        // Get all valid items from registry (exclude chests, shulker boxes and air)
        List<Item> availableItems = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }
            // Skip containers to avoid nested containers
            ItemStack testStack = item.getDefaultStack();
            if (SpecialItemClassifier.isShulkerBox(testStack) || 
                SpecialItemClassifier.isChest(testStack) ||
                SpecialItemClassifier.isTrapChest(testStack)) {
                continue;
            }
            // Skip bundles to avoid nested bundles
            if (BundleItemPopulator.isBundle(testStack)) {
                continue;
            }
            availableItems.add(item);
        }

        if (availableItems.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No valid items found to populate " + chestType);
            return chest;
        }

        // Create inventory with slots
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(inventorySize, ItemStack.EMPTY);

        // Create a list of occupied slots to track where we've placed items
        List<Integer> occupiedSlots = new ArrayList<>();

        for (int i = 0; i < itemTypeCount && !availableItems.isEmpty(); i++) {
            // Select a random item
            int index = RANDOM.nextInt(availableItems.size());
            Item selectedItem = availableItems.remove(index);

            // Create item stack and determine quantity
            ItemStack itemStack = new ItemStack(selectedItem);
            int totalQuantity = determineQuantity(itemStack);

            // Scatter the item stack into smaller stacks across random slots
            distributeItemStack(inventory, occupiedSlots, selectedItem, totalQuantity);

            AllEggRandomizer.LOGGER.debug("Added to {}: {} (total: {})", chestType, selectedItem, totalQuantity);
        }

        // Set chest contents using ContainerComponent
        try {
            ContainerComponent containerComponent = ContainerComponent.fromStacks(inventory);
            chest.set(DataComponentTypes.CONTAINER, containerComponent);

            AllEggRandomizer.LOGGER.info("Populated {} with {} item types scattered across {} slots", 
                chestType, itemTypeCount, occupiedSlots.size());
        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Failed to populate " + chestType + " contents", e);
            return chest;
        }

        return chest;
    }

    /**
     * Distributes an item stack across random inventory slots, breaking it up to create a messy appearance.
     * Items are split into smaller stacks and placed in random positions.
     *
     * @param inventory the inventory to place items in
     * @param occupiedSlots list of occupied slots to track
     * @param item the item to distribute
     * @param totalQuantity total quantity to distribute
     */
    private static void distributeItemStack(DefaultedList<ItemStack> inventory, List<Integer> occupiedSlots, 
            Item item, int totalQuantity) {
        
        // For single-quantity items (weapons, tools, equipment, special items), place them directly
        if (totalQuantity == 1) {
            int slot = getRandomEmptySlot(inventory, occupiedSlots);
            if (slot >= 0) {
                inventory.set(slot, new ItemStack(item, 1));
                occupiedSlots.add(slot);
            }
            return;
        }

        // For larger stacks, scatter them into smaller stacks (2-4 items each)
        // This creates a messy, scattered appearance
        int remainingQuantity = totalQuantity;
        
        while (remainingQuantity > 0) {
            // Determine stack size: split into smaller stacks of 2-4 items
            int stackSize;
            if (remainingQuantity >= 4) {
                stackSize = RANDOM.nextInt(3) + 2; // 2, 3, or 4
            } else if (remainingQuantity >= 2) {
                stackSize = RANDOM.nextInt(remainingQuantity) + 1; // 1 to remainingQuantity-1
            } else {
                stackSize = 1;
            }
            
            stackSize = Math.min(stackSize, remainingQuantity);
            
            // Find a random empty slot
            int slot = getRandomEmptySlot(inventory, occupiedSlots);
            if (slot >= 0) {
                inventory.set(slot, new ItemStack(item, stackSize));
                occupiedSlots.add(slot);
            } else {
                // No more empty slots
                break;
            }
            
            remainingQuantity -= stackSize;
        }
    }

    /**
     * Gets a random empty slot from the inventory.
     *
     * @param inventory the inventory to search
     * @param occupiedSlots list of occupied slot indices
     * @return a random empty slot index, or -1 if none available
     */
    private static int getRandomEmptySlot(DefaultedList<ItemStack> inventory, List<Integer> occupiedSlots) {
        // Get all empty slots
        List<Integer> emptySlots = new ArrayList<>();
        int size = inventory.size();
        for (int i = 0; i < size; i++) {
            if (!occupiedSlots.contains(i) && inventory.get(i).isEmpty()) {
                emptySlots.add(i);
            }
        }
        
        if (emptySlots.isEmpty()) {
            return -1;
        }
        
        // Return a random empty slot
        return emptySlots.get(RANDOM.nextInt(emptySlots.size()));
    }

    /**
     * Determines the quantity for an item based on its category.
     * Weapons/equipment/special items get 1, other items get 1-8.
     *
     * @param itemStack the item stack to determine quantity for
     * @return the quantity (1 for special items, 1-8 for others)
     */
    public static int determineQuantity(ItemStack itemStack) {
        if (ItemCategoryClassifier.isSingleQuantity(itemStack)) {
            return QUANTITY_SINGLE;
        } else {
            return RANDOM.nextInt(MAX_QUANTITY_NORMAL - MIN_QUANTITY_NORMAL + 1) + MIN_QUANTITY_NORMAL;
        }
    }
}
