package com.rave.projectbabylonweapons.passive.bastion;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.init.PBModItems;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class BastionHeavensGiftBalance {
    private static final Profile DEFAULT = new Profile(20 * 15, 20 * 10, 20 * 2, 8.0F, 0.05F);

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
                return new Profile(
                GsonHelper.getAsInt(json, "holy_sigil_duration_ticks"),
                GsonHelper.getAsInt(json, "aura_duration_ticks", DEFAULT.auraDurationTicks()),
                GsonHelper.getAsInt(json, "heal_interval_ticks", DEFAULT.healIntervalTicks()),
                GsonHelper.getAsFloat(json, "radius_blocks", DEFAULT.radiusBlocks()),
                GsonHelper.getAsFloat(json, "heal_max_health_percent", DEFAULT.healMaxHealthPercent())
        );
    }

    public record Profile(int holySigilDurationTicks, int auraDurationTicks, int healIntervalTicks, float radiusBlocks, float healMaxHealthPercent) {
    }
}