package com.rave.projectbabylonweapons.passive.wand;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.wand.EtherealBattleWandItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class EtherealSanctuaryBalance {
    private static final Profile DEFAULT = new Profile(0.15F, 15.0F, 0.10F);

    private EtherealSanctuaryBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.WAND_ETHEREAL_SANCTUARY, stack, EtherealSanctuaryBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof EtherealBattleWandItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsFloat(json, "proc_chance"),
                GsonHelper.getAsFloat(json, "ally_range_blocks"),
                GsonHelper.getAsFloat(json, "heal_percent")
        );
    }

    public record Profile(float procChance, float allyRangeBlocks, float healPercent) {
    }
}
