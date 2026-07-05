package com.rave.projectbabylonweapons.passive.smallshield;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.shield.EtherealSmallShieldItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class EtherealSmallShieldBalance {
    private static final Profile DEFAULT = new Profile(0.10F, 0.05F, 10 * 20);

    private EtherealSmallShieldBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.SMALL_SHIELD_ETHEREAL_PURIFICATION, stack, EtherealSmallShieldBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof EtherealSmallShieldItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(GsonHelper.getAsFloat(json, "weapon_charge_restore_per_buff"), GsonHelper.getAsFloat(json, "absorption_percent_per_buff"), GsonHelper.getAsInt(json, "absorption_duration_ticks"));
    }

    public record Profile(float weaponChargeRestorePerBuff, float absorptionPercentPerBuff, int absorptionDurationTicks) {
    }
}
