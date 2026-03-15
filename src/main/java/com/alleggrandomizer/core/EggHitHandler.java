package com.alleggrandomizer.core;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.config.CategoryType;
import com.alleggrandomizer.config.ModConfig;
import com.alleggrandomizer.config.WorldConfigData;
import com.alleggrandomizer.config.WorldConfigManager;
import com.alleggrandomizer.core.entity.AgeConfigurator;
import com.alleggrandomizer.core.entity.EntityConfigurationManager;
import com.alleggrandomizer.core.entity.EntityConfigurator;
import com.alleggrandomizer.core.entity.SpecialEntityType;
import com.alleggrandomizer.core.entity.ZombieEquipmentConfigurator;
import com.alleggrandomizer.core.entity.ZombieMountConfigurator;
import com.alleggrandomizer.random.WeightedRandom;
import com.alleggrandomizer.random.WeightedRandomSystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handles egg hit events and spawns random outputs based on configuration.
 * Uses registries to dynamically load all available entities and items with equal weights.
 */
public class EggHitHandler {

    /**
     * Default weight for most entities.
     */
    private static final double DEFAULT_ENTITY_WEIGHT = 1.0;

    /**
     * Reduced weight for boss entities (Ender Dragon and Wither).
     * These are extremely powerful mobs that should rarely spawn from eggs.
     */
    private static final double BOSS_ENTITY_WEIGHT = 0.01;

    /**
     * Set of entity types that should have reduced spawn weight.
     * These are boss-level entities that shouldn't spawn frequently.
     */
    private static final Set<EntityType<?>> REDUCED_WEIGHT_ENTITIES = Set.of(
        EntityType.ENDER_DRAGON,
        EntityType.WITHER
    );

    private static final WeightedRandomSystem randomSystem = new WeightedRandomSystem();
    private static final EntityConfigurationManager entityConfigManager =
        new EntityConfigurationManager();
    
    /**
     * Unified entity choice - wraps either a registry entity or a special entity.
     * This allows both regular and special entities to be in the same pool.
     */
    public static class EntityChoice {
        public final RegistryEntry<EntityType<?>> registryEntry;  // For regular entities
        public final SpecialEntityType specialType;              // For special entities
        
        private EntityChoice(RegistryEntry<EntityType<?>> registryEntry, SpecialEntityType specialType) {
            this.registryEntry = registryEntry;
            this.specialType = specialType;
        }
        
        /**
         * Create a regular entity choice from registry entry.
         */
        public static EntityChoice fromRegistry(RegistryEntry<EntityType<?>> entry) {
            return new EntityChoice(entry, null);
        }
        
        /**
         * Create a special entity choice.
         */
        public static EntityChoice fromSpecial(SpecialEntityType type) {
            return new EntityChoice(null, type);
        }
        
        /**
         * Check if this is a special entity.
         */
        public boolean isSpecial() {
            return specialType != null;
        }
    }
    
    // Removed: separate special entity chance - now they are in the unified pool
    private static final Random RANDOM = Random.create();

    static {
        // Register entity configurators
        entityConfigManager.registerConfigurator(new AgeConfigurator());
        entityConfigManager.registerConfigurator(new ZombieEquipmentConfigurator());
        entityConfigManager.registerConfigurator(new ZombieMountConfigurator());
    }

    /**
     * Handle an egg hit event.
     */
    public static void handleEggHit(EggEntity egg, HitResult hitResult) {
        if (!(egg.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        // Get world-specific configuration
        WorldConfigData worldConfig = WorldConfigManager.getWorldConfig(serverWorld.getServer());
        if (worldConfig == null) {
            AllEggRandomizer.LOGGER.warn("Cannot handle egg hit: world config is null");
            return;
        }
        
        ModConfig config = worldConfig.getConfig();
        if (config == null) {
            AllEggRandomizer.LOGGER.warn("Cannot handle egg hit: mod config is null");
            return;
        }

        Vec3d pos = hitResult.getPos();

        // Select category using ThreadLocalRandom for high-quality randomness
        // ThreadLocalRandom combines nanoTime + threadId + sequence for non-deterministic results
        CategoryType selectedCategory = randomSystem.selectCategory(config);

        if (selectedCategory == null) {
            AllEggRandomizer.LOGGER.debug("No category selected");
            return;
        }

        AllEggRandomizer.LOGGER.info("Selected category: {} at position ({}, {}, {})",
            selectedCategory, pos.x, pos.y, pos.z);

        // Generate output based on category
        switch (selectedCategory) {
            case ENTITY -> spawnEntity(serverWorld, pos, config);
            case ITEM -> spawnItem(serverWorld, pos, config);
            case EFFECT -> applyEffect(serverWorld, pos, config);
            case EVENT -> triggerEvent(serverWorld, pos, config, egg);
        }
    }

    /**
     * Build a registry-based entity weight map with equal weights.
     * Uses the entity type registry to get all available entities.
     * Boss entities (Ender Dragon, Wither) have significantly reduced weight.
     *
     * @return map of entity registry entries to weights
     */
    private static Map<RegistryEntry<EntityType<?>>, Double> buildEntityWeightsFromRegistry() {
        Map<RegistryEntry<EntityType<?>>, Double> entityWeights = new HashMap<>();

        // Iterate over all entity types in the registry
        for (EntityType<?> type : Registries.ENTITY_TYPE) {
            // Skip entities that cannot spawn naturally or are excluded
            if (type == EntityType.PLAYER || type == EntityType.LIGHTNING_BOLT) {
                continue;
            }

            // Skip entities that don't have spawn eggs (not spawnable)
            // Use spawnGroup to determine if entity can be spawned
            SpawnGroup spawnGroup = type.getSpawnGroup();
            if (spawnGroup == SpawnGroup.MISC) {
                continue;
            }

            // Determine weight based on entity type
            double weight = isReducedWeightEntity(type) ? BOSS_ENTITY_WEIGHT : DEFAULT_ENTITY_WEIGHT;
            entityWeights.put(Registries.ENTITY_TYPE.getEntry(type), weight);
        }

        return entityWeights;
    }

    /**
     * Check if an entity type should have reduced spawn weight.
     *
     * @param entityType the entity type to check
     * @return true if the entity should have reduced weight
     */
    private static boolean isReducedWeightEntity(EntityType<?> entityType) {
        return REDUCED_WEIGHT_ENTITIES.contains(entityType);
    }

    /**
     * Build a registry-based item weight map with equal weights.
     * Uses the item registry to get all available items.
     *
     * @return map of items to weights (equal weight for all)
     */
    private static Map<Item, Double> buildItemWeightsFromRegistry() {
        Map<Item, Double> itemWeights = new HashMap<>();

        // Iterate over all items in the registry
        for (Item item : Registries.ITEM) {
            // Skip air
            if (item == Items.AIR) {
                continue;
            }

            // Give equal weight to all valid items
            itemWeights.put(item, 1.0);
        }

        return itemWeights;
    }

    private static void spawnEntity(ServerWorld world, Vec3d pos, ModConfig config) {
        // Build unified entity pool with both registry entities and special entities
        // All entities (except Ender Dragon and Wither) have equal weight
        Map<EntityChoice, Double> entityPool = buildUnifiedEntityPool();

        AllEggRandomizer.LOGGER.debug("Total entity pool size: {}", entityPool.size());

        if (entityPool.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No valid entities found in registry");
            return;
        }

        // Select from unified pool using WeightedRandom - all entities have equal probability
        WeightedRandom<EntityChoice> weightedRandom = new WeightedRandom<>();
        EntityChoice choice = weightedRandom.selectFromMap(entityPool);
        
        if (choice == null) {
            AllEggRandomizer.LOGGER.warn("Failed to select entity from pool");
            return;
        }
        
        if (choice.isSpecial()) {
            spawnSpecialEntity(world, pos, choice.specialType);
        } else {
            spawnRegistryEntity(world, pos, config, choice.registryEntry);
        }
    }
    
    /**
     * Build a unified pool containing both regular registry entities and special entities.
     * All entities (except Ender Dragon and Wither) have equal weight.
     */
    private static Map<EntityChoice, Double> buildUnifiedEntityPool() {
        Map<EntityChoice, Double> pool = new HashMap<>();
        
        // Add all registry entities with equal weight
        for (EntityType<?> type : Registries.ENTITY_TYPE) {
            // Skip entities that cannot spawn or are excluded
            if (type == EntityType.PLAYER || type == EntityType.LIGHTNING_BOLT) {
                continue;
            }
            
            SpawnGroup spawnGroup = type.getSpawnGroup();
            if (spawnGroup == SpawnGroup.MISC) {
                continue;
            }
            
            // Apply reduced weight for boss entities (Ender Dragon, Wither)
            double weight = isReducedWeightEntity(type) ? BOSS_ENTITY_WEIGHT : DEFAULT_ENTITY_WEIGHT;
            
            RegistryEntry<EntityType<?>> entry = Registries.ENTITY_TYPE.getEntry(type);
            if (entry != null) {
                pool.put(EntityChoice.fromRegistry(entry), weight);
            }
        }
        
        // Add special entities with equal weight (they don't exist in registry)
        for (SpecialEntityType specialType : SpecialEntityType.getRandomPool()) {
            if (!SpecialEntityType.isAvailable(specialType)) {
                continue;
            }
            
            // All special entities get equal weight (1.0)
            pool.put(EntityChoice.fromSpecial(specialType), DEFAULT_ENTITY_WEIGHT);
        }
        
        return pool;
    }

    /**
     * Spawns a special entity with predefined configuration.
     */
    private static void spawnSpecialEntity(ServerWorld world, Vec3d pos, SpecialEntityType type) {
        EntityType<?> entityType = type.getBaseEntityType();
        var entity = entityType.create(world, SpawnReason.TRIGGERED);
        
        if (entity != null) {
            entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
            
            // Apply special entity configurator
            EntityConfigurator configurator = type.getConfigurator();
            if (configurator != null) {
                configurator.configure(entity, world);
            }
            
            world.spawnEntity(entity);
            AllEggRandomizer.LOGGER.info("Spawned special entity: {} ({}) at ({}, {}, {})",
                type.name(), type.getDisplayName(), pos.x, pos.y, pos.z);
        }
    }

    /**
     * Spawns an entity from the standard registry.
     */
    private static void spawnRegistryEntity(ServerWorld world, Vec3d pos, ModConfig config, RegistryEntry<EntityType<?>> selectedType) {
        var entity = selectedType.value().create(world, SpawnReason.TRIGGERED);
        if (entity != null) {
            entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);

            // Apply entity configurations (age, equipment, etc.)
            entityConfigManager.configureEntity(entity, world);

            world.spawnEntity(entity);
            AllEggRandomizer.LOGGER.info("Spawned entity: {} at ({}, {}, {})",
                selectedType.value(), pos.x, pos.y, pos.z);
        }
    }

    private static void spawnItem(ServerWorld world, Vec3d pos, ModConfig config) {
        // Build item weights from registry (equal weight for each)
        Map<Item, Double> itemWeights = buildItemWeightsFromRegistry();

        AllEggRandomizer.LOGGER.debug("Total items available: {}", itemWeights.size());

        if (itemWeights.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No valid items found in registry");
            return;
        }

        ItemStack selectedItem = randomSystem.selectItem(config, itemWeights);

        if (selectedItem != null) {
            var itemEntity = new net.minecraft.entity.ItemEntity(
                world, pos.x, pos.y, pos.z, selectedItem.copy()
            );
            world.spawnEntity(itemEntity);
            AllEggRandomizer.LOGGER.info("Spawned item: {} at ({}, {}, {})",
                selectedItem.getItem(), pos.x, pos.y, pos.z);
        }
    }

    private static void applyEffect(ServerWorld world, Vec3d pos, ModConfig config) {
        com.alleggrandomizer.core.generator.EffectGenerator.generate(world, pos, config);
    }

    private static void triggerEvent(ServerWorld world, Vec3d pos, ModConfig config, EggEntity egg) {
        com.alleggrandomizer.core.generator.EventGenerator.generate(world, pos, config, egg);
    }
}

