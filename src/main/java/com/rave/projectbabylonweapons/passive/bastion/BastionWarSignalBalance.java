package com.rave.projectbabylonweapons.passive.bastion;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.init.PBModItems;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class BastionWarSignalBalance {
    private static final Profile DEFAULT = new Profile(20 * 20);

    private BastionWarSignalBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.BASTION_NETHERITE_WAR_SIGNAL, stack, BastionWarSignalBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.is(PBModItems.NETHERITE_BASTION_SHIELD.get()) ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(GsonHelper.getAsInt(json, "ash_memory_duration_ticks"));
    }

    public record Profile(int ashMemoryDurationTicks) {
    }
}