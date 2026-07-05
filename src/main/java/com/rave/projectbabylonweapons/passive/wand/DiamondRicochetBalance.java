package com.rave.projectbabylonweapons.passive.wand;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.wand.DiamondBattleWandItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class DiamondRicochetBalance {
    private static final Profile DEFAULT = new Profile(0.5F, 2);

    private DiamondRicochetBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.WAND_DIAMOND_RICOCHET, stack, DiamondRicochetBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof DiamondBattleWandItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsFloat(json, "ricochet_proc_chance"),
                GsonHelper.getAsInt(json, "max_ricochets")
        );
    }

    public record Profile(float ricochetProcChance, int maxRicochets) {
    }
}
