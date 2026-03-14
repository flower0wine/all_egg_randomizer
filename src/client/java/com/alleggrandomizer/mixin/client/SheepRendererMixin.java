package com.alleggrandomizer.mixin.client;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.data.SheepDataKeys;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for LivingEntityRenderer to support combined rainbow + upside-down sheep effect.
 * 
 * The rainbow effect is triggered by naming the sheep "jeb_" (vanilla).
 * The upside-down effect is triggered by reading custom TrackedData.
 * 
 * Since "jeb_" must be the complete name to trigger rainbow effect,
 * we use TrackedData to store the upside-down flag separately.
 * TrackedData automatically syncs from server to client.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class SheepRendererMixin {

    /**
     * Inject into the method that updates render state.
     * Check for TrackedData to enable upside-down rendering.
     */
    @Inject(
        method = "updateRenderState",
        at = @At("TAIL")
    )
    private void onUpdateRenderState(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        // Only process Sheep entities
        if (!(entity instanceof SheepEntity sheep)) {
            return;
        }
        
        // Check if the sheep has our flip upside-down TrackedData set
        try {
            Boolean flipUpsideDown = sheep.getDataTracker().get(SheepDataKeys.FLIP_UPSIDE_DOWN);
            if (flipUpsideDown != null && flipUpsideDown) {
                state.flipUpsideDown = true;
                AllEggRandomizer.LOGGER.debug("Applied upside-down effect to sheep via TrackedData");
            }
        } catch (Exception e) {
            AllEggRandomizer.LOGGER.trace("TrackedData not available yet: {}", e.getMessage());
        }
    }
}
