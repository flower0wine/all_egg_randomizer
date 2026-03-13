package com.alleggrandomizer.core.entity;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Configures zombie equipment, giving them spears with a 30% chance.
 * Supports all spear tiers available in Minecraft 1.21.11.
 */
public class ZombieEquipmentConfigurator implements EntityConfigurator {
    
    private static final double SPEAR_CHANCE = 0.3;
    private static final Random RANDOM = new Random();
    
    // All spear types available in Minecraft 1.21.11
    private static final List<Item> SPEAR_ITEMS = new ArrayList<>();
    
    static {
        // Initialize spear items list
        // Using reflection-safe approach to handle potential missing items
        addSpearIfExists(Items.WOODEN_SPEAR);
        addSpearIfExists(Items.STONE_SPEAR);
        addSpearIfExists(Items.COPPER_SPEAR);
        addSpearIfExists(Items.IRON_SPEAR);
        addSpearIfExists(Items.GOLDEN_SPEAR);
        addSpearIfExists(Items.DIAMOND_SPEAR);
        addSpearIfExists(Items.NETHERITE_SPEAR);
    }
    
    private static void addSpearIfExists(Item item) {
        if (item != null && item != Items.AIR) {
            SPEAR_ITEMS.add(item);
        }
    }
    
    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }
        
        ZombieEntity zombie = (ZombieEntity) entity;
        
        // 30% chance to equip a spear
        if (RANDOM.nextDouble() < SPEAR_CHANCE && !SPEAR_ITEMS.isEmpty()) {
            Item spear = SPEAR_ITEMS.get(RANDOM.nextInt(SPEAR_ITEMS.size()));
            ItemStack spearStack = new ItemStack(spear);
            
            zombie.equipStack(EquipmentSlot.MAINHAND, spearStack);
            zombie.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.085f); // Standard drop chance
            
            AllEggRandomizer.LOGGER.debug("Equipped {} with {}", 
                zombie.getType().getName().getString(), 
                spear.getName().getString());
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof ZombieEntity;
    }
}
