package com.rave.projectbabylonweapons.passive.wand;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.wand.IceBattleWandItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class IceFrostTouchBalance {
    public static final int CHILL_I_AMPLIFIER = 0;
    public static final int CHILL_II_AMPLIFIER = 1;
    public static final int CHILL_III_AMPLIFIER = 2;

    private static final Profile DEFAULT = new Profile(0.33F, 0.25F, 0.20F, 0.15F, 20 * 6, 20 * 9, 20 * 12, 20 * 2, 1.5F);

    private IceFrostTouchBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.WAND_ICE_FROST_TOUCH, stack, IceFrostTouchBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof IceBattleWandItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsFloat(json, "chill_i_proc_chance"),
                GsonHelper.getAsFloat(json, "chill_ii_proc_chance"),
                GsonHelper.getAsFloat(json, "chill_iii_proc_chance"),
                GsonHelper.getAsFloat(json, "frozen_proc_chance"),
                GsonHelper.getAsInt(json, "chill_i_duration_ticks"),
                GsonHelper.getAsInt(json, "chill_ii_duration_ticks"),
                GsonHelper.getAsInt(json, "chill_iii_duration_ticks"),
                GsonHelper.getAsInt(json, "frozen_duration_ticks"),
                GsonHelper.getAsFloat(json, "freeze_radius_blocks")
        );
    }

    public record Profile(
            float chillIProcChance,
            float chillIIProcChance,
            float chillIIIProcChance,
            float frozenProcChance,
            int chillIDurationTicks,
            int chillIIDurationTicks,
            int chillIIIDurationTicks,
            int frozenDurationTicks,
            float freezeRadiusBlocks
    ) {
    }
}
