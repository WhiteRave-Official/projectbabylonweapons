package com.rave.projectbabylonweapons.passive.bastion;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.init.PBModItems;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class BastionCrushingBalance {
    private static final Profile DEFAULT = new Profile(20 * 10, 0.9F, 0.24F, 4);

    private BastionCrushingBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.BASTION_DIAMOND_CRUSHING, stack, BastionCrushingBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.is(PBModItems.DIAMOND_BASTION_SHIELD.get()) ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsInt(json, "weakness_duration_ticks"),
                GsonHelper.getAsFloat(json, "knockup_velocity"),
                GsonHelper.getAsFloat(json, "horizontal_push"),
                GsonHelper.getAsInt(json, "block_ring_count")
        );
    }

    public record Profile(int weaknessDurationTicks, float knockupVelocity, float horizontalPush, int blockRingCount) {
    }
}