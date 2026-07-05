package com.rave.projectbabylonweapons.passive.golden;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.battleaxe.GoldenBattleAxeItem;
import com.rave.projectbabylonweapons.item.battlehammer.GoldenBattleHammerItem;
import com.rave.projectbabylonweapons.item.battlescythe.GoldenBattleScytheItem;
import com.rave.projectbabylonweapons.item.claws.GoldenClawsItem;
import com.rave.projectbabylonweapons.item.dagger.GoldenDaggerItem;
import com.rave.projectbabylonweapons.item.greatsword.GoldenGreatswordItem;
import com.rave.projectbabylonweapons.item.longsword.GoldenLongswordItem;
import com.rave.projectbabylonweapons.item.shortsword.GoldenShortswordItem;
import com.rave.projectbabylonweapons.item.sickle.GoldenSickleItem;
import com.rave.projectbabylonweapons.item.spear.GoldenSpearItem;
import com.rave.projectbabylonweapons.item.staff.GoldenStaffItem;
import com.rave.projectbabylonweapons.item.tachi.GoldenTachiItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import com.rave.projectbabylonweapons.util.PBItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class GoldenMagicBalance {
    private static final Profile TOOLS = new Profile(0.25F);
    private static final Profile WEAPONS = new Profile(0.25F);

    private GoldenMagicBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.GOLDEN_MAGIC, stack, GoldenMagicBalance::parseProfile);
        if (override != null) {
            return override;
        }

        if (stack.is(PBItemTags.GOLDEN_TOOLS)) {
            return TOOLS;
        }

        Item item = stack.getItem();
        if (item instanceof GoldenBattleAxeItem
                || item instanceof GoldenBattleHammerItem
                || item instanceof GoldenBattleScytheItem
                || item instanceof GoldenClawsItem
                || item instanceof GoldenDaggerItem
                || item instanceof GoldenGreatswordItem
                || item instanceof GoldenLongswordItem
                || item instanceof GoldenShortswordItem
                || item instanceof GoldenSickleItem
                || item instanceof GoldenSpearItem
                || item instanceof GoldenStaffItem
                || item instanceof GoldenTachiItem) {
            return WEAPONS;
        }

        return null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(GsonHelper.getAsFloat(json, "bonus_magic_damage_percent"));
    }

    public record Profile(float bonusMagicDamagePercent) {
    }
}
