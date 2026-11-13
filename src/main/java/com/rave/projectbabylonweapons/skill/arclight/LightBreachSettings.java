package com.rave.projectbabylonweapons.skill.arclight;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public record LightBreachSettings(float cost, int columns, double minRadius, double maxRadius,
                                  float columnRadius, float height, int fadeIn, int activeTicks,
                                  int fadeOut, int pulseInterval, float damageMultiplier) {
    public static LightBreachSettings defaults() {
        return new LightBreachSettings(150.0F, 6, 2.0D, 5.0D, 0.75F, 6.0F, 5, 40, 8, 10, 1.5F);
    }

    public static LightBreachSettings fromTag(CompoundTag tag) {
        var d = defaults();
        double min = number(tag, "light_breach_min_radius", d.minRadius, 0.5D, 16.0D);
        return new LightBreachSettings(
                (float) number(tag, "light_breach_cost", d.cost, 0.0D, 10000.0D),
                (int) number(tag, "light_breach_columns", d.columns, 1, 16), min,
                number(tag, "light_breach_max_radius", d.maxRadius, min, 16.0D),
                (float) number(tag, "light_breach_column_radius", d.columnRadius, 0.2D, 4.0D),
                (float) number(tag, "light_breach_height", d.height, 1.0D, 32.0D),
                (int) number(tag, "light_breach_fade_in_ticks", d.fadeIn, 1, 40),
                (int) number(tag, "light_breach_active_ticks", d.activeTicks, 1, 200),
                (int) number(tag, "light_breach_fade_out_ticks", d.fadeOut, 1, 40),
                (int) number(tag, "light_breach_pulse_interval", d.pulseInterval, 1, 200),
                (float) number(tag, "light_breach_damage_multiplier", d.damageMultiplier, 0, 100));
    }

    private static double number(CompoundTag tag, String key, double fallback, double min, double max) {
        double value = tag.contains(key, 99) ? tag.getDouble(key) : fallback;
        return Double.isFinite(value) ? Mth.clamp(value, min, max) : fallback;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("light_breach_cost", cost);
        tag.putInt("light_breach_columns", columns);
        tag.putDouble("light_breach_min_radius", minRadius);
        tag.putDouble("light_breach_max_radius", maxRadius);
        tag.putFloat("light_breach_column_radius", columnRadius);
        tag.putFloat("light_breach_height", height);
        tag.putInt("light_breach_fade_in_ticks", fadeIn);
        tag.putInt("light_breach_active_ticks", activeTicks);
        tag.putInt("light_breach_fade_out_ticks", fadeOut);
        tag.putInt("light_breach_pulse_interval", pulseInterval);
        tag.putFloat("light_breach_damage_multiplier", damageMultiplier);
        return tag;
    }

    public int totalTicks() { return fadeIn + activeTicks + fadeOut; }
}
