package com.rave.projectbabylonweapons.passive.smallshield;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.shield.GoldenSmallShieldItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class GoldenSmallShieldBalance {
    private static final Profile DEFAULT = new Profile(10 * 20);

    private GoldenSmallShieldBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.SMALL_SHIELD_GOLDEN_WITHERING, stack, GoldenSmallShieldBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof GoldenSmallShieldItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(GsonHelper.getAsInt(json, "exhausted_duration_ticks"));
    }

    public record Profile(int exhaustedDurationTicks) {
    }
}
