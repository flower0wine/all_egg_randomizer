package com.alleggrandomizer.core.entity;

import net.minecraft.entity.EntityType;

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

            // Lightning creeper
            case LIGHTNING_CREEPER:
                return new LightningCreeperConfigurator();

            default:
                return null;
        }
    }
}
