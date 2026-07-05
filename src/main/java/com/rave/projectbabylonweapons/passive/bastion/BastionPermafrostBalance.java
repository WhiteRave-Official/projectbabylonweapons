package com.rave.projectbabylonweapons.passive.bastion;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.init.PBModItems;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class BastionPermafrostBalance {
    private static final Profile DEFAULT = new Profile(20 * 8, 20 * 15, 20 * 3, 20, 8.0F);

    private BastionPermafrostBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.BASTION_ICE_PERMAFROST, stack, BastionPermafrostBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.is(PBModItems.ICE_BASTION_SHIELD.get()) ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsInt(json, "aura_duration_ticks"),
                GsonHelper.getAsInt(json, "chill_duration_ticks"),
                GsonHelper.getAsInt(json, "magical_resistance_duration_ticks"),
                GsonHelper.getAsInt(json, "refresh_interval_ticks"),
                GsonHelper.getAsFloat(json, "radius_blocks")
        );
    }

    public record Profile(int auraDurationTicks, int chillDurationTicks, int magicalResistanceDurationTicks, int refreshIntervalTicks, float radiusBlocks) {
    }
}