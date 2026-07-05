package com.rave.projectbabylonweapons.passive.smallshield;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.shield.IceSmallShieldItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class IceSmallShieldBalance {
    private static final Profile DEFAULT = new Profile(3 * 20, 6 * 20);

    private IceSmallShieldBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.SMALL_SHIELD_ICE_PRISON, stack, IceSmallShieldBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof IceSmallShieldItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(GsonHelper.getAsInt(json, "frozen_duration_ticks"), GsonHelper.getAsInt(json, "chill_duration_ticks"));
    }

    public record Profile(int frozenDurationTicks, int chillDurationTicks) {
    }
}
