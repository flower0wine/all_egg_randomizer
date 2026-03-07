package com.alleggrandomizer.mixin;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.config.ConfigManager;
import com.alleggrandomizer.config.ModConfig;
import com.alleggrandomizer.core.EggHitHandler;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EggEntity.class)
public class ThrownEggEntityMixin {
    
    @Inject(method = "onCollision", at = @At("HEAD"), cancellable = true)
    private void onEggHit(HitResult hitResult, CallbackInfo ci) {
        EggEntity egg = (EggEntity) (Object) this;
        
        // Only process on server side
        if (egg.getEntityWorld().isClient()) {
            return;
        }
        
        try {
            ModConfig config = ConfigManager.getInstance().getConfig();
            if (config == null) {
                AllEggRandomizer.LOGGER.warn("Config not loaded, using vanilla behavior");
                return;
            }
            
            // Check if any category is enabled
            boolean anyEnabled = config.getCategories().values().stream()
                .anyMatch(cat -> cat.isEnabled());
            
            if (!anyEnabled) {
                AllEggRandomizer.LOGGER.debug("No categories enabled, using vanilla behavior");
                return;
            }
            
            // Handle the egg hit event with our custom logic
            EggHitHandler.handleEggHit(egg, hitResult, config);
            
            // Send particle effect (same as vanilla)
            egg.getEntityWorld().sendEntityStatus(egg, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
            
            // Discard the egg entity
            egg.discard();
            
            // Cancel vanilla behavior since we handled it
            ci.cancel();
            
        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error processing egg hit: {}", e.getMessage(), e);
            // Let vanilla behavior run on error
        }
    }
}
