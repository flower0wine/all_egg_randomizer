package com.alleggrandomizer.core.data;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.SheepEntity;

/**
 * Shared TrackedData definitions for custom sheep effects.
 * 
 * This class centralizes all custom entity data to ensure consistent ID allocation
 * across server and client code.
 */
public final class SheepDataKeys {

    private SheepDataKeys() {
        // Utility class - no instantiation
    }

    /**
     * TrackedData for upside-down flip effect.
     * Uses DataTracker.registerData() to automatically allocate the next available ID.
     * This must be registered in SheepEntity's initDataTracker method via mixin.
     */
    public static final TrackedData<Boolean> FLIP_UPSIDE_DOWN = DataTracker.registerData(
            SheepEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN
    );
}
