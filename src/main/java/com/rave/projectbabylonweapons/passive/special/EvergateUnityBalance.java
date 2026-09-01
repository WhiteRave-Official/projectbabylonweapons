package com.rave.projectbabylonweapons.passive.special;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class EvergateUnityBalance {
    private static final Profile DEFAULT = new Profile(0.08D, 0.15F, 20);

    private EvergateUnityBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (!(stack.getItem() instanceof ArclightSwordItem) || !ArclightSwordItem.isEvergate(stack)) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(
                WeaponPassiveIds.EVERGATE_UNITY, stack, EvergateUnityBalance::parseProfile);
        return override != null ? override : DEFAULT;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsDouble(json, "movement_speed_bonus"),
                GsonHelper.getAsFloat(json, "holy_damage_percent"),
                GsonHelper.getAsInt(json, "stun_immunity_duration_ticks")
        );
    }

    public record Profile(double movementSpeedBonus, float holyDamagePercent, int stunImmunityDurationTicks) {
    }
}