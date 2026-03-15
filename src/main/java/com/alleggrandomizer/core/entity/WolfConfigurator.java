package com.alleggrandomizer.core.entity;

import com.alleggrandomizer.mixin.WolfEntityMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.entity.passive.WolfVariant;

/**
 * Configurator for Wolf entities.
 * Handles wolf variant (coat pattern) and collar color customization.
 */
public class WolfConfigurator implements EntityConfigurator {

    /**
     * Wolf coat variants available in Minecraft 1.21.11.
     */
    public enum WolfCoatVariant {
        DEFAULT("默认", net.minecraft.entity.passive.WolfVariants.DEFAULT),
        ASHEN("灰白", net.minecraft.entity.passive.WolfVariants.ASHEN),
        BLACK("黑色", net.minecraft.entity.passive.WolfVariants.BLACK),
        CHESTNUT("栗色", net.minecraft.entity.passive.WolfVariants.CHESTNUT),
        PALE("淡色", net.minecraft.entity.passive.WolfVariants.PALE),
        RUSTY("锈色", net.minecraft.entity.passive.WolfVariants.RUSTY),
        SNOWY("雪白", net.minecraft.entity.passive.WolfVariants.SNOWY),
        SPOTTED("斑点", net.minecraft.entity.passive.WolfVariants.SPOTTED),
        STRIPED("条纹", net.minecraft.entity.passive.WolfVariants.STRIPED),
        WOODS("森林", net.minecraft.entity.passive.WolfVariants.WOODS);

        private final String displayName;
        private final net.minecraft.registry.RegistryKey<WolfVariant> registryKey;

        WolfCoatVariant(String displayName, net.minecraft.registry.RegistryKey<WolfVariant> registryKey) {
            this.displayName = displayName;
            this.registryKey = registryKey;
        }

        public String getDisplayName() {
            return displayName;
        }

        public net.minecraft.registry.RegistryKey<WolfVariant> getRegistryKey() {
            return registryKey;
        }
    }

    private final WolfCoatVariant coatVariant;
    private final DyeColor collarColor;
    private final boolean isBaby;

    /**
     * Create a wolf configurator with specified variant and color.
     *
     * @param coatVariant the coat variant (pattern), or null for random
     * @param collarColor the collar color (DyeColor), or null for random
     * @param isBaby whether the wolf should be a baby
     */
    public WolfConfigurator(WolfCoatVariant coatVariant, DyeColor collarColor, boolean isBaby) {
        this.coatVariant = coatVariant;  // Can be null for random
        this.collarColor = collarColor;  // Can be null for random
        this.isBaby = isBaby;
    }

    /**
     * Create a random wolf configurator.
     *
     * @param world the server world to get registry access from
     */
    public WolfConfigurator(ServerWorld world) {
        // Randomly select coat variant
        WolfCoatVariant[] variants = WolfCoatVariant.values();
        this.coatVariant = variants[world.random.nextInt(variants.length)];

        // Randomly select collar color (all 16 colors)
        DyeColor[] colors = DyeColor.values();
        this.collarColor = colors[world.random.nextInt(colors.length)];

        // 30% chance to be baby
        this.isBaby = world.random.nextDouble() < 0.3;
    }

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }

        WolfEntity wolf = (WolfEntity) entity;
        WolfEntityMixin wolfMixin = (WolfEntityMixin) wolf;

        // Resolve variant (random if null)
        WolfCoatVariant resolvedVariant = this.coatVariant;
        if (resolvedVariant == null) {
            WolfCoatVariant[] variants = WolfCoatVariant.values();
            resolvedVariant = variants[world.random.nextInt(variants.length)];
        }

        // Resolve collar color (random if null)
        DyeColor resolvedColor = this.collarColor;
        if (resolvedColor == null) {
            DyeColor[] colors = DyeColor.values();
            resolvedColor = colors[world.random.nextInt(colors.length)];
        }

        // Set coat variant using mixin invoker
        var registryAccess = world.getRegistryManager();
        var wolfVariantRegistry = registryAccess.getOrThrow(RegistryKeys.WOLF_VARIANT);
        
        wolfVariantRegistry.getOptional(resolvedVariant.getRegistryKey()).ifPresent(variantHolder -> {
            wolfMixin.invokeSetVariant(variantHolder);
        });

        // Set collar color using mixin invoker
        wolfMixin.invokeSetCollarColor(resolvedColor);

        // Set baby status if applicable
        if (isBaby) {
            wolf.setBaby(true);
        }

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof WolfEntity;
    }

    public WolfCoatVariant getCoatVariant() {
        return coatVariant;
    }

    public DyeColor getCollarColor() {
        return collarColor;
    }

    public boolean isBaby() {
        return isBaby;
    }
}
