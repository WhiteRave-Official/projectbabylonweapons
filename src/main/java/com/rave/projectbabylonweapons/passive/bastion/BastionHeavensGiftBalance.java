package com.rave.projectbabylonweapons.passive.bastion;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.init.PBModItems;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class BastionHeavensGiftBalance {
    private static final Profile DEFAULT = new Profile(20 * 15);

    private BastionHeavensGiftBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.BASTION_ETHEREAL_HEAVENS_GIFT, stack, BastionHeavensGiftBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.is(PBModItems.ETHEREAL_BASTION_SHIELD.get()) ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(GsonHelper.getAsInt(json, "holy_sigil_duration_ticks"));
    }

    public record Profile(int holySigilDurationTicks) {
    }
}