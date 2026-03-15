package com.alleggrandomizer.core.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.util.DyeColor;

/**
 * Factory for creating entity configurators for special entity types.
 */
public class SpecialEntityConfiguratorFactory {

    /**
     * Creates a configurator for the given special entity type.
     */
    public static EntityConfigurator create(SpecialEntityType type) {
        switch (type) {
            // Passive animals - 30% chance to be baby
            case DONKEY:
                return new PassiveAnimalConfigurator(0.3);
            case SHEEP:
                return new PassiveAnimalConfigurator(0.3);
            case TADPOLE:
                return new PassiveAnimalConfigurator(0.3);
            case TROPICAL_FISH:
                return new PassiveAnimalConfigurator(0.3);

            // Golems
            case IRON_GOLEM:
                return new IronGolemConfigurator();
            case SNOW_GOLEM:
                return new SnowGolemConfigurator();
            case COPPER_GOLEM:
                return new CopperGolemConfigurator();

            // Undead
            case SKELETON:
                return new SkeletonConfigurator();
            case STRAY:
                return new StrayConfigurator();

            // Camel riders - zombie camel doesn't exist as separate type, using regular camel
            case ZOMBIE_ON_CAMEL_WITH_SPEAR:
                return new CamelRiderConfigurator(EntityType.CAMEL, true, true);
            case BABY_ZOMBIE_ON_CAMEL_WITH_SPEAR:
                return new CamelRiderConfigurator(EntityType.CAMEL, false, true);
            case ZOMBIE_ON_ZOMBIE_CAMEL_WITH_SPEAR:
                return new CamelRiderConfigurator(EntityType.CAMEL, true, true);
            case BABY_ZOMBIE_ON_ZOMBIE_CAMEL_WITH_SPEAR:
                return new CamelRiderConfigurator(EntityType.CAMEL, false, true);

            // Skeleton horse riders
            case SKELETON_ON_SKELETON_HORSE_WITH_SWORD:
                return new SkeletonHorseRiderConfigurator(
                    SkeletonHorseRiderConfigurator.WeaponType.IRON_SWORD);
            case SKELETON_ON_SKELETON_HORSE_WITH_BOW:
                return new SkeletonHorseRiderConfigurator(
                    SkeletonHorseRiderConfigurator.WeaponType.BOW);

            // Spider knight riders
            case SKELETON_ON_SPIDER_WITH_SWORD:
                return new SpiderRiderConfigurator(
                    SpiderRiderConfigurator.WeaponType.IRON_SWORD);
            case SKELETON_ON_SPIDER_WITH_BOW:
                return new SpiderRiderConfigurator(
                    SpiderRiderConfigurator.WeaponType.BOW);

            // Wolf variants - each with specific coat variant and random collar color
            case WOLF_DEFAULT:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.DEFAULT, DyeColor.RED, false);
            case WOLF_ASHEN:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.ASHEN, DyeColor.RED, false);
            case WOLF_BLACK:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.BLACK, DyeColor.RED, false);
            case WOLF_CHESTNUT:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.CHESTNUT, DyeColor.RED, false);
            case WOLF_PALE:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.PALE, DyeColor.RED, false);
            case WOLF_RUSTY:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.RUSTY, DyeColor.RED, false);
            case WOLF_SNOWY:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.SNOWY, DyeColor.RED, false);
            case WOLF_SPOTTED:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.SPOTTED, DyeColor.RED, false);
            case WOLF_STRIPED:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.STRIPED, DyeColor.RED, false);
            case WOLF_WOODS:
                return new WolfConfigurator(WolfConfigurator.WolfCoatVariant.WOODS, DyeColor.RED, false);
            case WOLF_RANDOM:
                // Random variant, random collar color - handled in configure method
                return new WolfConfigurator(null, null, false);

            // Fox (basic - no variant configurator needed for basic spawn)
            case RED_FOX:
                return new PassiveAnimalConfigurator(0.3);

            // Lightning creeper
            case LIGHTNING_CREEPER:
                return new LightningCreeperConfigurator();

            default:
                return null;
        }
    }
}
