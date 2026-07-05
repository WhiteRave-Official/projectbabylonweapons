package com.rave.projectbabylonweapons.passive.smallshield;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.shield.NetheriteSmallShieldItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class NetheriteSmallShieldBalance {
    private static final Profile DEFAULT = new Profile(5, 10 * 20);

    private NetheriteSmallShieldBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.SMALL_SHIELD_NETHERITE_SULFUR_BRAND, stack, NetheriteSmallShieldBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof NetheriteSmallShieldItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(GsonHelper.getAsInt(json, "fire_duration_seconds"), GsonHelper.getAsInt(json, "brimstone_duration_ticks"));
    }

    public record Profile(int fireDurationSeconds, int brimstoneDurationTicks) {
    }
}
