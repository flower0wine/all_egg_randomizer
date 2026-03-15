package com.alleggrandomizer.core.entity;

import net.minecraft.entity.EntityType;

/**
 * Enumeration of special entity types that can be spawned from eggs.
 * These represent specific entity combinations not available in the standard registry.
 */
public enum SpecialEntityType {
    // Basic entities
    DONKEY(EntityType.DONKEY, "驴", true),
    SHEEP(EntityType.SHEEP, "羊", true),
    SKELETON(EntityType.SKELETON, "骷髅", false),
    TADPOLE(EntityType.TADPOLE, "蝌蚪", false),
    TROPICAL_FISH(EntityType.TROPICAL_FISH, "热带鱼", false),
    IRON_GOLEM(EntityType.IRON_GOLEM, "铁傀儡", false),
    SNOW_GOLEM(EntityType.SNOW_GOLEM, "雪傀儡", false),
    COPPER_GOLEM(EntityType.COPPER_GOLEM, "铜傀儡", false),
    STRAY(EntityType.STRAY, "流浪者", false),

    // Camel variants with zombie riders
    ZOMBIE_ON_CAMEL_WITH_SPEAR(EntityType.ZOMBIE, "骆驼僵尸骑士（拿长矛）", false),
    BABY_ZOMBIE_ON_CAMEL_WITH_SPEAR(EntityType.ZOMBIE, "骆驼小僵尸骑士（拿长矛）", false),
    ZOMBIE_ON_ZOMBIE_CAMEL_WITH_SPEAR(EntityType.ZOMBIE, "僵尸骆驼僵尸骑士（拿长矛）", false),
    BABY_ZOMBIE_ON_ZOMBIE_CAMEL_WITH_SPEAR(EntityType.ZOMBIE, "僵尸骆驼小僵尸骑士（拿长矛）", false),

    // Skeleton horse variants
    SKELETON_ON_SKELETON_HORSE_WITH_SWORD(EntityType.SKELETON, "骷髅马上面骑着骷髅（拿铁剑）", false),
    SKELETON_ON_SKELETON_HORSE_WITH_BOW(EntityType.SKELETON, "骷髅马上面骑着骷髅（拿弓）", false),

    // Spider knight variants
    SKELETON_ON_SPIDER_WITH_SWORD(EntityType.SKELETON, "蜘蛛骑士（拿铁剑）", false),
    SKELETON_ON_SPIDER_WITH_BOW(EntityType.SKELETON, "蜘蛛骑士（拿弓）", false),

    // Wolf variants (10 coat patterns + 1 random)
    WOLF_DEFAULT(EntityType.WOLF, "默认狼", true),
    WOLF_ASHEN(EntityType.WOLF, "灰白狼", true),
    WOLF_BLACK(EntityType.WOLF, "黑色狼", true),
    WOLF_CHESTNUT(EntityType.WOLF, "栗色狼", true),
    WOLF_PALE(EntityType.WOLF, "淡色狼", true),
    WOLF_RUSTY(EntityType.WOLF, "锈色狼", true),
    WOLF_SNOWY(EntityType.WOLF, "雪白狼", true),
    WOLF_SPOTTED(EntityType.WOLF, "斑点狼", true),
    WOLF_STRIPED(EntityType.WOLF, "条纹狼", true),
    WOLF_WOODS(EntityType.WOLF, "森林狼", true),
    WOLF_RANDOM(EntityType.WOLF, "随机狼", true),

    // Fox (basic - variant setting requires more complex API in 1.21.11)
    RED_FOX(EntityType.FOX, "狐狸", true),

    // Lightning creeper (special - created by lightning strike)
    LIGHTNING_CREEPER(EntityType.CREEPER, "闪电苦力怕", false);

    private final EntityType<?> baseEntityType;
    private final String displayName;
    private final boolean isPassiveAnimal;

    SpecialEntityType(EntityType<?> baseEntityType, String displayName, boolean isPassiveAnimal) {
        this.baseEntityType = baseEntityType;
        this.displayName = displayName;
        this.isPassiveAnimal = isPassiveAnimal;
    }

    public EntityType<?> getBaseEntityType() {
        return baseEntityType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPassiveAnimal() {
        return isPassiveAnimal;
    }

    /**
     * Gets the configurator for this special entity type.
     */
    public EntityConfigurator getConfigurator() {
        return SpecialEntityConfiguratorFactory.create(this);
    }

    /**
     * Get all special entity types that should be included in the random pool.
     */
    public static SpecialEntityType[] getRandomPool() {
        return values();
    }

    /**
     * Check if the entity type is available in the current game version.
     */
    public static boolean isAvailable(SpecialEntityType type) {
        return type != null && type.getBaseEntityType() != null;
    }
}
