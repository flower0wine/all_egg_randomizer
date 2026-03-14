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

/**
 * Handles egg hit events and spawns random outputs based on configuration.
 * Uses registries to dynamically load all available entities and items with equal weights.
 */
public class EggHitHandler {

    private static final WeightedRandomSystem randomSystem = new WeightedRandomSystem();
    private static final EntityConfigurationManager entityConfigManager =
        new EntityConfigurationManager();
    
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

        // Select category
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
     *
     * @return map of entity registry entries to weights (equal weight for all)
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

            // Give equal weight to all valid entities
            entityWeights.put(Registries.ENTITY_TYPE.getEntry(type), 1.0);
        }

        return entityWeights;
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
        Map<RegistryEntry<EntityType<?>>, Double> entityWeights = buildEntityWeightsFromRegistry();
        
        // Add special entities to the pool with equal weight
        addSpecialEntitiesToPool(entityWeights);

        AllEggRandomizer.LOGGER.debug("Total entity types available (including special): {}", entityWeights.size());

        if (entityWeights.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No valid entities found in registry");
            return;
        }

        // Select entity from unified pool
        RegistryEntry<EntityType<?>> selectedType = randomSystem.selectEntity(config, entityWeights);

        if (selectedType != null) {
            // Check if this is a special entity
            SpecialEntityType specialType = findSpecialEntityByType(selectedType.value());
            
            if (specialType != null) {
                spawnSpecialEntity(world, pos, specialType);
            } else {
                spawnRegistryEntity(world, pos, config, selectedType);
            }
        }
    }
    
    /**
     * Adds special entities to the entity weight pool with equal weight.
     */
    private static void addSpecialEntitiesToPool(Map<RegistryEntry<EntityType<?>>, Double> entityWeights) {
        for (SpecialEntityType specialType : SpecialEntityType.getRandomPool()) {
            if (SpecialEntityType.isAvailable(specialType)) {
                EntityType<?> entityType = specialType.getBaseEntityType();
                // Get registry entry for the entity type
                RegistryEntry<EntityType<?>> entry = Registries.ENTITY_TYPE.getEntry(entityType);
                if (entry != null) {
                    entityWeights.put(entry, 1.0);
                }
            }
        }
    }
    
    /**
     * Finds a SpecialEntityType by its base EntityType.
     */
    private static SpecialEntityType findSpecialEntityByType(EntityType<?> entityType) {
        for (SpecialEntityType type : SpecialEntityType.getRandomPool()) {
            if (type.getBaseEntityType() == entityType) {
                return type;
            }
        }
        return null;
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

