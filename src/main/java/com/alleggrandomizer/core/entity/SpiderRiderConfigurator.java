package com.alleggrandomizer.core.entity;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.enchant.RandomEnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for skeleton riders on spiders.
 * Can equip either an iron sword or bow with random enchantments.
 */
public class SpiderRiderConfigurator implements EntityConfigurator {

    public enum WeaponType {
        IRON_SWORD,
        BOW
    }

    private final WeaponType weaponType;

    public SpiderRiderConfigurator(WeaponType weaponType) {
        this.weaponType = weaponType;
    }

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!(entity instanceof SkeletonEntity skeleton)) {
            return false;
        }

        // Create spider mount
        Entity spider = EntityType.SPIDER.create(world, SpawnReason.TRIGGERED);
        if (spider == null) {
            AllEggRandomizer.LOGGER.warn("Failed to create spider mount");
            return false;
        }

        // Position the spider at the skeleton's location
        spider.refreshPositionAndAngles(
            skeleton.getX(),
            skeleton.getY(),
            skeleton.getZ(),
            skeleton.getYaw(),
            skeleton.getPitch()
        );

        // Spawn the mount first
        world.spawnEntity(spider);

        // Make skeleton ride the spider
        skeleton.startRiding(spider);

        // Equip the skeleton with weapon
        equipSkeleton(skeleton, world);

        AllEggRandomizer.LOGGER.debug("Created skeleton on spider with {}",
            weaponType);

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof SkeletonEntity;
    }

    private void equipSkeleton(SkeletonEntity skeleton, ServerWorld world) {
        ItemStack weapon;

        switch (weaponType) {
            case IRON_SWORD:
                weapon = RandomEnchantmentHelper.createRandomEnchantedSword(world);
                break;
            case BOW:
                weapon = RandomEnchantmentHelper.createRandomEnchantedBow(world);
                break;
            default:
                weapon = new ItemStack(Items.BOW);
        }

        skeleton.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, weapon);
    }
}
