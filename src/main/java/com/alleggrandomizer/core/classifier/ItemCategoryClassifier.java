package com.alleggrandomizer.core.classifier;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

/**
 * Classifies items into categories: WEAPON, TOOL, EQUIPMENT, or OTHER.
 * Uses DataComponentTypes to detect item types (Minecraft 1.21.5+).
 * 
 * - WEAPON: Items with WEAPON component (swords, bows, crossbows, tridents, etc.)
 * - TOOL: Items with TOOL component (pickaxes, axes, shovels, etc.)
 * - EQUIPMENT: Items with EQUIPPABLE component (armor, elytra, etc.)
 * - OTHER: All other items
 */
public class ItemCategoryClassifier {

    /**
     * Item category enumeration.
     */
    public enum ItemCategory {
        /**
         * Weapons: swords, bows, crossbows, tridents, shields, etc.
         */
        WEAPON,
        
        /**
         * Tools: pickaxes, axes, shovels, hoes, etc.
         */
        TOOL,
        
        /**
         * Equipment: armor, elytra, horse armor, etc.
         */
        EQUIPMENT,
        
        /**
         * All other items that are neither weapons, tools, nor equipment.
         */
        OTHER
    }

    /**
     * Determines the category of an item.
     * Uses DataComponentTypes to check item components.
     * 
     * @param itemStack the item stack to classify
     * @return the item category (WEAPON, TOOL, EQUIPMENT, or OTHER)
     */
    public static ItemCategory classify(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemCategory.OTHER;
        }

        // Check WEAPON component first (highest priority)
        if (itemStack.contains(DataComponentTypes.WEAPON)) {
            return ItemCategory.WEAPON;
        }
        
        // Check TOOL component
        if (itemStack.contains(DataComponentTypes.TOOL)) {
            return ItemCategory.TOOL;
        }

        // Check EQUIPPABLE component
        if (itemStack.contains(DataComponentTypes.EQUIPPABLE)) {
            return ItemCategory.EQUIPMENT;
        }

        return ItemCategory.OTHER;
    }

    /**
     * Checks if the item should give only single quantity.
     * (Weapons, tools, and equipment give 1)
     * 
     * @param itemStack the item stack to check
     * @return true if quantity should be 1
     */
    public static boolean isSingleQuantity(ItemStack itemStack) {
        ItemCategory category = classify(itemStack);
        return category == ItemCategory.WEAPON || 
               category == ItemCategory.TOOL || 
               category == ItemCategory.EQUIPMENT;
    }
}
