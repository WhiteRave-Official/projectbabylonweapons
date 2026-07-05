package com.rave.projectbabylonweapons.passive.bastion;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.init.PBModItems;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class BastionRuleAuraBalance {
    private static final Profile DEFAULT = new Profile(20 * 15, 20 * 5, 20 * 6, 20, 8.0F);

    private BastionRuleAuraBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.BASTION_DRAGONSTEEL_RULE_AURA, stack, BastionRuleAuraBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.is(PBModItems.DRAGONSTEEL_BASTION_SHIELD.get()) ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsInt(json, "aura_duration_ticks"),
                GsonHelper.getAsInt(json, "provoke_duration_ticks"),
                GsonHelper.getAsInt(json, "crit_resistance_duration_ticks"),
                GsonHelper.getAsInt(json, "refresh_interval_ticks"),
                GsonHelper.getAsFloat(json, "radius_blocks")
        );
    }

    public record Profile(int auraDurationTicks, int provokeDurationTicks, int critResistanceDurationTicks, int refreshIntervalTicks, float radiusBlocks) {
    }
}