package com.rave.projectbabylonweapons.passive.wand;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.wand.NetheriteBattleWandItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class NetheriteAshenBalance {
    private static final Profile DEFAULT = new Profile(0.5F, 0.25F, 5, 2.0F);

    private NetheriteAshenBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.WAND_NETHERITE_ASHEN, stack, NetheriteAshenBalance::parseProfile);
        if (override != null) {
            return override;
        }

        return stack.getItem() instanceof NetheriteBattleWandItem ? DEFAULT : null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsFloat(json, "aoe_damage_multiplier"),
                GsonHelper.getAsFloat(json, "ignite_proc_chance"),
                GsonHelper.getAsInt(json, "ignite_seconds"),
                GsonHelper.getAsFloat(json, "radius_blocks")
        );
    }

    public record Profile(float aoeDamageMultiplier, float igniteProcChance, int igniteSeconds, float radiusBlocks) {
    }
}
