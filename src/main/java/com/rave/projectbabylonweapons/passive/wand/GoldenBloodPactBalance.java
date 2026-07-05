package com.rave.projectbabylonweapons.passive.wand;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.wand.GoldenBattleWandItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class GoldenBloodPactBalance {
    private static final Profile DEFAULT = new Profile(0.02D, 0.20D, 20 * 8);

    private GoldenBloodPactBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.WAND_GOLDEN_BLOOD_PACT, stack, GoldenBloodPactBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof GoldenBattleWandItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsDouble(json, "stack_percent"),
                GsonHelper.getAsDouble(json, "max_percent"),
                GsonHelper.getAsInt(json, "duration_ticks")
        );
    }

    public record Profile(double stackPercent, double maxPercent, int durationTicks) {
    }
}
