package com.alleggrandomizer.mixin;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.data.SheepDataKeys;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for SheepEntity to add custom TrackedData for the flip upside-down effect.
 * 
 * This allows syncing the flip state from server to client via entity data tracker.
 * The flip state is stored as a boolean that automatically syncs to all nearby clients.
 */
@Mixin(SheepEntity.class)
public class SheepEntityMixin {

    /**
     * Add custom TrackedData at the start of initDataTracker.
     */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onInitDataTrackerTail(DataTracker.Builder builder, CallbackInfo ci) {
        // Add our custom flip upside-down data
        builder.add(SheepDataKeys.FLIP_UPSIDE_DOWN, false);
        AllEggRandomizer.LOGGER.debug("Registered custom TrackedData for sheep flip effect: {}", SheepDataKeys.FLIP_UPSIDE_DOWN.id());
    }
}
