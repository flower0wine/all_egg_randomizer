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
 * Configurator that gives zombies spears with 100% chance.
 * Used for special zombie riders (camel riders, etc.)
 */
public class ZombieSpearConfigurator implements EntityConfigurator {

    private static final Random RANDOM = new Random();

    // All spear types available in Minecraft 1.21.11
    private static final List<Item> SPEAR_ITEMS = new ArrayList<>();

    static {
        // Initialize spear items list
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
        return giveRandomSpear(zombie);
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof ZombieEntity;
    }

    /**
     * Gives a random spear to the zombie.
     * Used by CamelRiderConfigurator.
     */
    public static boolean giveRandomSpear(ZombieEntity zombie) {
        if (SPEAR_ITEMS.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No spear items available");
            return false;
        }

        Item spear = SPEAR_ITEMS.get(RANDOM.nextInt(SPEAR_ITEMS.size()));
        ItemStack spearStack = new ItemStack(spear);

        zombie.equipStack(EquipmentSlot.MAINHAND, spearStack);
        zombie.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.085f);

        AllEggRandomizer.LOGGER.debug("Equipped {} with {}",
            zombie.getType().getName().getString(),
            spear.getName().getString());

        return true;
    }
}
