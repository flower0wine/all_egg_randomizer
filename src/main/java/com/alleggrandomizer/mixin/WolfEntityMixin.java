package com.alleggrandomizer.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Mixin for WolfEntity to expose private methods and fields.
 */
@Mixin(WolfEntity.class)
public interface WolfEntityMixin {

    @Invoker("setVariant")
    void invokeSetVariant(RegistryEntry<net.minecraft.entity.passive.WolfVariant> variant);

    @Invoker("setCollarColor")
    void invokeSetCollarColor(DyeColor color);

    @Accessor("VARIANT")
    static TrackedData<RegistryEntry<net.minecraft.entity.passive.WolfVariant>> getVariantTracker() {
        throw new AssertionError();
    }

    @Accessor("COLLAR_COLOR")
    static TrackedData<Integer> getCollarColorTracker() {
        throw new AssertionError();
    }
}
