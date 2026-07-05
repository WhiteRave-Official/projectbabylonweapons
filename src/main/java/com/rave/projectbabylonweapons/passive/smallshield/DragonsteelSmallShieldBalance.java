package com.rave.projectbabylonweapons.passive.smallshield;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.shield.DragonsteelSmallShieldItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class DragonsteelSmallShieldBalance {
    private static final Profile DEFAULT = new Profile(2, 1.0F, 30.0F);

    private DragonsteelSmallShieldBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.SMALL_SHIELD_DRAGONSTEEL_RETRIBUTION, stack, DragonsteelSmallShieldBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof DragonsteelSmallShieldItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(GsonHelper.getAsInt(json, "max_charges"), GsonHelper.getAsFloat(json, "damage_bonus_multiplier_per_charge"), GsonHelper.getAsFloat(json, "armor_negation_per_charge"));
    }

    public record Profile(int maxCharges, float damageBonusMultiplierPerCharge, float armorNegationPerCharge) {
    }
}
