package com.alleggrandomizer.core.enchant;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for applying random enchantments to items.
 * Uses DataComponentTypes API introduced in Minecraft 1.21.5+.
 */
public class RandomEnchantmentHelper {

    private static final Random RANDOM = new Random();
    
    // Configuration for enchantment levels
    private static final int MIN_ENCHANTMENT_LEVEL = 1;
    private static final int MAX_WEAPON_ENCHANTMENT_LEVEL = 5;
    private static final int MAX_BOW_ENCHANTMENT_LEVEL = 5;
    
    // Chance to add additional enchantments
    private static final double SECONDARY_ENCHANTMENT_CHANCE = 0.3;
    private static final double TERTIARY_ENCHANTMENT_CHANCE = 0.2;

    /**
     * Creates an ItemStack with random weapon enchantments.
     * Used for skeleton riders with swords.
     * 
     * @param world ServerWorld to get the registry from
     * @return ItemStack with random enchantments applied
     */
    public static ItemStack createRandomEnchantedSword(ServerWorld world) {
        ItemStack swordStack = new ItemStack(Items.IRON_SWORD);
        
        MinecraftServer server = world.getServer();
        if (server == null) {
            AllEggRandomizer.LOGGER.warn("Cannot apply enchantments: server is null");
            return swordStack;
        }
        
        Registry<Enchantment> enchantmentRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        
        // Get weapon-compatible enchantments
        List<RegistryEntry<Enchantment>> weaponEnchantments = getWeaponCompatibleEnchantments(enchantmentRegistry);
        
        if (weaponEnchantments.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No weapon enchantments found in registry");
            return swordStack;
        }
        
        // Apply random enchantments
        applyRandomEnchantments(swordStack, weaponEnchantments, MAX_WEAPON_ENCHANTMENT_LEVEL);
        
        AllEggRandomizer.LOGGER.info("Created enchanted iron sword with {} enchantments", 
            getEnchantmentCount(swordStack));
        return swordStack;
    }

    /**
     * Creates an ItemStack with random bow enchantments.
     * Used for skeleton riders with bows.
     * 
     * @param world ServerWorld to get the registry from
     * @return ItemStack with random enchantments applied
     */
    public static ItemStack createRandomEnchantedBow(ServerWorld world) {
        ItemStack bowStack = new ItemStack(Items.BOW);
        
        MinecraftServer server = world.getServer();
        if (server == null) {
            AllEggRandomizer.LOGGER.warn("Cannot apply enchantments: server is null");
            return bowStack;
        }
        
        Registry<Enchantment> enchantmentRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        
        // Get bow-compatible enchantments
        List<RegistryEntry<Enchantment>> bowEnchantments = getBowCompatibleEnchantments(enchantmentRegistry);
        
        if (bowEnchantments.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No bow enchantments found in registry");
            return bowStack;
        }
        
        // Apply random enchantments
        applyRandomEnchantments(bowStack, bowEnchantments, MAX_BOW_ENCHANTMENT_LEVEL);
        
        AllEggRandomizer.LOGGER.info("Created enchanted bow with {} enchantments", 
            getEnchantmentCount(bowStack));
        return bowStack;
    }

    /**
     * Gets all weapon-compatible enchantments from the registry.
     * Includes: sharpness, smite, bane of arthropods, knockback, fire aspect, looting,
     * sweeping edge, unbreaking, mending
     */
    private static List<RegistryEntry<Enchantment>> getWeaponCompatibleEnchantments(
            Registry<Enchantment> enchantmentRegistry) {
        List<RegistryEntry<Enchantment>> compatible = new ArrayList<>();
        
        // Weapon-specific enchantments
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.SHARPNESS, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.SMITE, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.BANE_OF_ARTHROPODS, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.KNOCKBACK, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.FIRE_ASPECT, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.LOOTING, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.SWEEPING_EDGE, compatible);
        
        // Universal enchantments (work on weapons)
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.UNBREAKING, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.MENDING, compatible);
        
        return compatible;
    }

    /**
     * Gets all bow-compatible enchantments from the registry.
     * Includes: power, punch, flame, infinity, unbreaking, mending
     */
    private static List<RegistryEntry<Enchantment>> getBowCompatibleEnchantments(
            Registry<Enchantment> enchantmentRegistry) {
        List<RegistryEntry<Enchantment>> compatible = new ArrayList<>();
        
        // Bow-specific enchantments
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.POWER, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.PUNCH, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.FLAME, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.INFINITY, compatible);
        
        // Universal enchantments (work on bows)
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.UNBREAKING, compatible);
        addEnchantmentIfExists(enchantmentRegistry, Enchantments.MENDING, compatible);
        
        return compatible;
    }

    /**
     * Adds an enchantment to the list if it exists in the registry.
     */
    private static void addEnchantmentIfExists(
            Registry<Enchantment> registry, 
            RegistryKey<Enchantment> key,
            List<RegistryEntry<Enchantment>> list) {
        try {
            RegistryEntry<Enchantment> entry = registry.getOrThrow(key);
            list.add(entry);
        } catch (Exception e) {
            // Enchantment not found in registry, skip it
        }
    }

    /**
     * Applies random enchantments to an item stack.
     * Applies 1-3 random enchantments with random levels.
     */
    private static void applyRandomEnchantments(
            ItemStack stack, 
            List<RegistryEntry<Enchantment>> availableEnchantments,
            int maxLevel) {
        
        if (availableEnchantments.isEmpty()) {
            return;
        }
        
        // Always add at least one enchantment
        RegistryEntry<Enchantment> primary = availableEnchantments.get(
            RANDOM.nextInt(availableEnchantments.size()));
        int primaryLevel = RANDOM.nextInt(maxLevel) + MIN_ENCHANTMENT_LEVEL;
        
        stack.addEnchantment(primary, primaryLevel);
        
        // Possibly add secondary enchantment
        if (RANDOM.nextDouble() < SECONDARY_ENCHANTMENT_CHANCE && availableEnchantments.size() > 1) {
            RegistryEntry<Enchantment> secondary;
            do {
                secondary = availableEnchantments.get(RANDOM.nextInt(availableEnchantments.size()));
            } while (secondary.equals(primary));
            
            int secondaryLevel = RANDOM.nextInt(maxLevel) + MIN_ENCHANTMENT_LEVEL;
            stack.addEnchantment(secondary, secondaryLevel);
            
            // Possibly add tertiary enchantment
            if (RANDOM.nextDouble() < TERTIARY_ENCHANTMENT_CHANCE && availableEnchantments.size() > 2) {
                RegistryEntry<Enchantment> tertiary;
                do {
                    tertiary = availableEnchantments.get(RANDOM.nextInt(availableEnchantments.size()));
                } while (tertiary.equals(primary) || tertiary.equals(secondary));
                
                int tertiaryLevel = RANDOM.nextInt(maxLevel) + MIN_ENCHANTMENT_LEVEL;
                stack.addEnchantment(tertiary, tertiaryLevel);
            }
        }
    }

    /**
     * Gets the number of enchantments on an item stack.
     */
    private static int getEnchantmentCount(ItemStack stack) {
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null) {
            return 0;
        }
        return enchantments.getEnchantments().size();
    }

    /**
     * Applies a random weapon-compatible enchantment.
     */
    public static void applyRandomWeaponEnchantment(ItemStack stack, ServerWorld world) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        
        MinecraftServer server = world.getServer();
        if (server == null) {
            return;
        }
        
        Registry<Enchantment> enchantmentRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        List<RegistryEntry<Enchantment>> weaponEnchantments = getWeaponCompatibleEnchantments(enchantmentRegistry);
        
        if (!weaponEnchantments.isEmpty()) {
            RegistryEntry<Enchantment> enchantment = weaponEnchantments.get(
                RANDOM.nextInt(weaponEnchantments.size()));
            int level = RANDOM.nextInt(MAX_WEAPON_ENCHANTMENT_LEVEL) + MIN_ENCHANTMENT_LEVEL;
            stack.addEnchantment(enchantment, level);
        }
        
        AllEggRandomizer.LOGGER.debug("Applied weapon enchantment to {}", 
            stack.getItem().getName().getString());
    }

    /**
     * Applies a random bow-compatible enchantment.
     */
    public static void applyRandomBowEnchantment(ItemStack stack, ServerWorld world) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        
        MinecraftServer server = world.getServer();
        if (server == null) {
            return;
        }
        
        Registry<Enchantment> enchantmentRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        List<RegistryEntry<Enchantment>> bowEnchantments = getBowCompatibleEnchantments(enchantmentRegistry);
        
        if (!bowEnchantments.isEmpty()) {
            RegistryEntry<Enchantment> enchantment = bowEnchantments.get(
                RANDOM.nextInt(bowEnchantments.size()));
            int level = RANDOM.nextInt(MAX_BOW_ENCHANTMENT_LEVEL) + MIN_ENCHANTMENT_LEVEL;
            stack.addEnchantment(enchantment, level);
        }
        
        AllEggRandomizer.LOGGER.debug("Applied bow enchantment to {}", 
            stack.getItem().getName().getString());
    }
}
