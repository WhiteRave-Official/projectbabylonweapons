package com.rave.projectbabylonweapons.passive.wand;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.wand.DragonsteelBattleWandItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class DragonsteelDragonlordBalance {
    private static final Profile DEFAULT = new Profile(0.5F, 0.35D);

    private DragonsteelDragonlordBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.WAND_DRAGONSTEEL_DRAGONLORD, stack, DragonsteelDragonlordBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof DragonsteelBattleWandItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsFloat(json, "mini_projectile_damage_multiplier"),
                GsonHelper.getAsDouble(json, "mini_projectile_side_offset")
        );
    }

    public record Profile(float miniProjectileDamageMultiplier, double miniProjectileSideOffset) {
    }
}
