package com.rave.projectbabylonweapons.passive.smallshield;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.shield.DiamondSmallShieldItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class DiamondSmallShieldBalance {
    private static final Profile DEFAULT = new Profile(5, 8 * 20, 0.15F, 10 * 20, 14.0D, 1.25D);

    private DiamondSmallShieldBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.SMALL_SHIELD_DIAMOND_WOLF_GRIP, stack, DiamondSmallShieldBalance::parseProfile);
        if (override != null) {
            return override;
        }

        if (stack.getItem() instanceof DiamondSmallShieldItem) {
            return DEFAULT;
        }

        return null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsInt(json, "max_shards"),
                GsonHelper.getAsInt(json, "shard_lifetime_ticks"),
                GsonHelper.getAsFloat(json, "damage_multiplier"),
                GsonHelper.getAsInt(json, "weapon_chip_duration_ticks"),
                GsonHelper.getAsDouble(json, "launch_distance"),
                GsonHelper.getAsDouble(json, "launch_speed")
        );
    }

    public record Profile(int maxShards, int shardLifetimeTicks, float damageMultiplier,
                          int weaponChipDurationTicks, double launchDistance, double launchSpeed) {
    }
}