package com.alleggrandomizer.core.classifier;

import net.minecraft.item.*;

/**
 * Classifier for special item types that require single quantity.
 * Detects buckets, harnesses, boats, shulker boxes, and minecarts.
 */
public class SpecialItemClassifier {

    /**
     * Special item type enumeration.
     */
    public enum SpecialItemType {
        /**
         * Buckets: water bucket, milk bucket, lava bucket, etc.
         */
        BUCKET,
        
        /**
         * Harnesses: used for horse breeding/riding.
         */
        HARNESS,
        
        /**
         * Boats: any boat item.
         */
        BOAT,
        
        /**
         * Shulker boxes: any shulker box variant.
         */
        SHULKER_BOX,
        
        /**
         * Minecarts: any minecart variant.
         */
        MINECART,
        
        /**
         * Player head: player skull item.
         */
        PLAYER_HEAD
    }

    /**
     * Checks if the given item stack is a special item type that requires single quantity.
     * This does NOT check weapon/tool/equipment - only special items like buckets, harnesses, etc.
     *
     * @param itemStack the item stack to check
     * @return true if it's a special item type (bucket, harness, boat, shulker box, minecart, player head)
     */
    public static boolean isSpecialItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        
        // Check special item types
        return isBucket(itemStack) 
            || isHarness(itemStack)
            || isAnyBoatItem(itemStack)
            || isShulkerBox(itemStack)
            || isMinecart(itemStack)
            || isPlayerHead(itemStack);
    }

    /**
     * Checks if the given item stack should have quantity of 1.
     * This includes weapons, tools, equipment, and special items (buckets, harnesses, etc.)
     *
     * @param itemStack the item stack to check
     * @return true if quantity should be 1
     */
    public static boolean isSingleQuantity(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        
        // Check special item types
        return isBucket(itemStack) 
            || isHarness(itemStack)
            || isAnyBoatItem(itemStack)
            || isShulkerBox(itemStack)
            || isMinecart(itemStack)
            || isPlayerHead(itemStack);
    }

    /**
     * Checks if the given item stack is a bucket.
     * Detects bucket items by checking if the item is a BucketItem.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a bucket
     */
    public static boolean isBucket(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        Item item = itemStack.getItem();
        // Check for bucket items - they have "bucket" in their translation key
        // and are typically used for carrying liquids
        return item == Items.WATER_BUCKET 
            || item == Items.LAVA_BUCKET 
            || item == Items.MILK_BUCKET
            || item == Items.POWDER_SNOW_BUCKET
            || item == Items.BUCKET;
    }

    /**
     * Checks if the given item stack is a harness.
     * In Minecraft 1.21+, harness is used for riding happy ghasts.
     * Checks for all colored harness variants.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a harness
     */
    public static boolean isHarness(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        // Check for harness items directly
        Item item = itemStack.getItem();
        return item == Items.WHITE_HARNESS 
            || item == Items.ORANGE_HARNESS
            || item == Items.MAGENTA_HARNESS
            || item == Items.LIGHT_BLUE_HARNESS
            || item == Items.YELLOW_HARNESS
            || item == Items.LIME_HARNESS
            || item == Items.PINK_HARNESS
            || item == Items.GRAY_HARNESS
            || item == Items.LIGHT_GRAY_HARNESS
            || item == Items.CYAN_HARNESS
            || item == Items.PURPLE_HARNESS
            || item == Items.BLUE_HARNESS
            || item == Items.BROWN_HARNESS
            || item == Items.GREEN_HARNESS
            || item == Items.RED_HARNESS
            || item == Items.BLACK_HARNESS;
    }

    /**
     * Checks if the given item stack is any boat item.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a boat
     */
    public static boolean isAnyBoatItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.getItem() instanceof BoatItem;
    }

    /**
     * Checks if the given item stack is a shulker box.
     * Detects all shulker box variants.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a shulker box
     */
    public static boolean isShulkerBox(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        Item item = itemStack.getItem();
        // Check for shulker box items
        return item == Items.SHULKER_BOX
            || item == Items.WHITE_SHULKER_BOX
            || item == Items.ORANGE_SHULKER_BOX
            || item == Items.MAGENTA_SHULKER_BOX
            || item == Items.LIGHT_BLUE_SHULKER_BOX
            || item == Items.YELLOW_SHULKER_BOX
            || item == Items.LIME_SHULKER_BOX
            || item == Items.PINK_SHULKER_BOX
            || item == Items.GRAY_SHULKER_BOX
            || item == Items.LIGHT_GRAY_SHULKER_BOX
            || item == Items.CYAN_SHULKER_BOX
            || item == Items.PURPLE_SHULKER_BOX
            || item == Items.BLUE_SHULKER_BOX
            || item == Items.BROWN_SHULKER_BOX
            || item == Items.GREEN_SHULKER_BOX
            || item == Items.RED_SHULKER_BOX
            || item == Items.BLACK_SHULKER_BOX;
    }

    /**
     * Checks if the given item stack is a minecart.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a minecart
     */
    public static boolean isMinecart(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.getItem() instanceof MinecartItem;
    }

    /**
     * Checks if the given item stack is a player head.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a player head
     */
    public static boolean isPlayerHead(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.getItem() == Items.PLAYER_HEAD;
    }

    /**
     * Gets the special item type if the item is a special type.
     *
     * @param itemStack the item stack to check
     * @return the special item type, or null if not special
     */
    public static SpecialItemType getSpecialItemType(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return null;
        }
        
        if (isBucket(itemStack)) {
            return SpecialItemType.BUCKET;
        }
        if (isHarness(itemStack)) {
            return SpecialItemType.HARNESS;
        }
        if (isAnyBoatItem(itemStack)) {
            return SpecialItemType.BOAT;
        }
        if (isShulkerBox(itemStack)) {
            return SpecialItemType.SHULKER_BOX;
        }
        if (isMinecart(itemStack)) {
            return SpecialItemType.MINECART;
        }
        if (isPlayerHead(itemStack)) {
            return SpecialItemType.PLAYER_HEAD;
        }
        
        return null;
    }
}
