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
        PLAYER_HEAD,
        
        /**
         * Bow: ranged weapon.
         */
        BOW,
        
        /**
         * Crossbow: ranged weapon.
         */
        CROSSBOW,
        
        /**
         * Fishing rod: used for fishing.
         */
        FISHING_ROD,
        
        /**
         * Flint and steel: used to create fire.
         */
        FLINT_AND_STEEL,
        
        /**
         * Music discs: all music disc variants.
         */
        MUSIC_DISC,
        
        /**
         * Chest: standard chest block item.
         */
        CHEST,
        
        /**
         * Trap chest: trapped chest block item.
         */
        TRAP_CHEST
    }

    /**
     * Checks if the given item stack is a special item type that requires single quantity.
     * This does NOT check weapon/tool/equipment - only special items like buckets, harnesses, etc.
     *
     * @param itemStack the item stack to check
     * @return true if it's a special item type (bucket, harness, boat, shulker box, minecart, player head, bow, crossbow, fishing rod, flint and steel, music disc, chest, trap chest)
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
            || isPlayerHead(itemStack)
            || isBow(itemStack)
            || isCrossbow(itemStack)
            || isFishingRod(itemStack)
            || isFlintAndSteel(itemStack)
            || isMusicDisc(itemStack)
            || isChest(itemStack)
            || isTrapChest(itemStack);
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
            || isPlayerHead(itemStack)
            || isBow(itemStack)
            || isCrossbow(itemStack)
            || isFishingRod(itemStack)
            || isFlintAndSteel(itemStack)
            || isMusicDisc(itemStack)
            || isChest(itemStack)
            || isTrapChest(itemStack);
    }

    /**
     * Checks if the given item stack is a bucket.
     * Uses instanceof to detect all bucket types including any future bucket variants.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a bucket
     */
    public static boolean isBucket(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        // Use instanceof to detect all bucket types (water, lava, milk, powder snow, etc.)
        return itemStack.getItem() instanceof BucketItem;
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
     * Checks if the given item stack is a bow.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a bow
     */
    public static boolean isBow(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.getItem() == Items.BOW;
    }

    /**
     * Checks if the given item stack is a crossbow.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a crossbow
     */
    public static boolean isCrossbow(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.getItem() == Items.CROSSBOW;
    }

    /**
     * Checks if the given item stack is a fishing rod.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a fishing rod
     */
    public static boolean isFishingRod(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.getItem() == Items.FISHING_ROD;
    }

    /**
     * Checks if the given item stack is flint and steel.
     *
     * @param itemStack the item stack to check
     * @return true if the item is flint and steel
     */
    public static boolean isFlintAndSteel(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.getItem() == Items.FLINT_AND_STEEL;
    }

    /**
     * Checks if the given item stack is a music disc.
     * Detects all music disc variants.
     *
     * @param itemStack the item stack to check
     * @return true if the item is a music disc
     */
    public static boolean isMusicDisc(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        Item item = itemStack.getItem();
        // Check for all music disc variants
        return item == Items.MUSIC_DISC_5
            || item == Items.MUSIC_DISC_11
            || item == Items.MUSIC_DISC_13
            || item == Items.MUSIC_DISC_BLOCKS
            || item == Items.MUSIC_DISC_CAT
            || item == Items.MUSIC_DISC_CHIRP
            || item == Items.MUSIC_DISC_CREATOR
            || item == Items.MUSIC_DISC_CREATOR_MUSIC_BOX
            || item == Items.MUSIC_DISC_FAR
            || item == Items.MUSIC_DISC_MALL
            || item == Items.MUSIC_DISC_MELLOHI
            || item == Items.MUSIC_DISC_PIGSTEP
            || item == Items.MUSIC_DISC_RELIC
            || item == Items.MUSIC_DISC_STAL
            || item == Items.MUSIC_DISC_STRAD
            || item == Items.MUSIC_DISC_WAIT
            || item == Items.MUSIC_DISC_WARD;
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
        return itemStack.getItem() == Items.CHEST;
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
        return itemStack.getItem() == Items.TRAPPED_CHEST;
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
        if (isBow(itemStack)) {
            return SpecialItemType.BOW;
        }
        if (isCrossbow(itemStack)) {
            return SpecialItemType.CROSSBOW;
        }
        if (isFishingRod(itemStack)) {
            return SpecialItemType.FISHING_ROD;
        }
        if (isFlintAndSteel(itemStack)) {
            return SpecialItemType.FLINT_AND_STEEL;
        }
        if (isMusicDisc(itemStack)) {
            return SpecialItemType.MUSIC_DISC;
        }
        if (isChest(itemStack)) {
            return SpecialItemType.CHEST;
        }
        if (isTrapChest(itemStack)) {
            return SpecialItemType.TRAP_CHEST;
        }
        
        return null;
    }
}
