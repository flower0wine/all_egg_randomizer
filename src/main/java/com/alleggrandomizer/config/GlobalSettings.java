package com.alleggrandomizer.config;

/**
 * Global settings that apply to the entire mod behavior.
 */
public class GlobalSettings {

    private int cooldown = 0;
    private int maxSpawnPerThrow = 5;

    public GlobalSettings() {
    }

    public GlobalSettings(int cooldown, int maxSpawnPerThrow) {
        this.cooldown = cooldown;
        this.maxSpawnPerThrow = maxSpawnPerThrow;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

    public int getMaxSpawnPerThrow() {
        return maxSpawnPerThrow;
    }

    public void setMaxSpawnPerThrow(int maxSpawnPerThrow) {
        this.maxSpawnPerThrow = Math.max(1, Math.min(maxSpawnPerThrow, 100));
    }

    /**
     * Validate and fix invalid values.
     */
    public void validate() {
        if (cooldown < 0) {
            cooldown = 0;
        }
        if (maxSpawnPerThrow < 1) {
            maxSpawnPerThrow = 1;
        } else if (maxSpawnPerThrow > 100) {
            maxSpawnPerThrow = 100;
        }
    }
}
