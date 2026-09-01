package com.rave.projectbabylonweapons.passive.special;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class ArclightConduitBalance {
    private static final Profile DEFAULT = new Profile(0.25D, 15.0F, 0.15F, 0.10F, 0.50F, 60);

    private ArclightConduitBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (!(stack.getItem() instanceof ArclightSwordItem) || ArclightSwordItem.isEvergate(stack)) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(
                WeaponPassiveIds.ARCLIGHT_CONDUIT, stack, ArclightConduitBalance::parseProfile);
        return override != null ? override : DEFAULT;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsDouble(json, "mana_regeneration_bonus"),
                GsonHelper.getAsFloat(json, "mana_cost"),
                GsonHelper.getAsFloat(json, "damage_bonus"),
                GsonHelper.getAsFloat(json, "holy_damage_percent"),
                GsonHelper.getAsFloat(json, "barrier_conversion_percent"),
                GsonHelper.getAsInt(json, "barrier_duration_ticks")
        );
    }

    public record Profile(double manaRegenerationBonus, float manaCost, float damageBonus,
                          float holyDamagePercent, float barrierConversionPercent,
                          int barrierDurationTicks) {
    }
}