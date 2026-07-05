package com.rave.projectbabylonweapons.passive.diamond;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.battleaxe.DiamondBattleAxeItem;
import com.rave.projectbabylonweapons.item.battlehammer.DiamondBattleHammerItem;
import com.rave.projectbabylonweapons.item.battlescythe.DiamondBattleScytheItem;
import com.rave.projectbabylonweapons.item.claws.DiamondClawsItem;
import com.rave.projectbabylonweapons.item.dagger.DiamondDaggerItem;
import com.rave.projectbabylonweapons.item.greatsword.DiamondGreatswordItem;
import com.rave.projectbabylonweapons.item.longsword.DiamondLongswordItem;
import com.rave.projectbabylonweapons.item.shortsword.DiamondShortswordItem;
import com.rave.projectbabylonweapons.item.sickle.DiamondSickleItem;
import com.rave.projectbabylonweapons.item.spear.DiamondSpearItem;
import com.rave.projectbabylonweapons.item.staff.DiamondStaffItem;
import com.rave.projectbabylonweapons.item.tachi.DiamondTachiItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import com.rave.projectbabylonweapons.util.PBItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DiamondFangBalance {
    private static final Profile TOOLS = new Profile(0.15F);
    private static final Profile WEAPONS = new Profile(0.15F);

    private DiamondFangBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.DIAMOND_FANG, stack, DiamondFangBalance::parseProfile);
        if (override != null) {
            return override;
        }

        if (stack.is(PBItemTags.DIAMOND_TOOLS)) {
            return TOOLS;
        }

        Item item = stack.getItem();
        if (item instanceof DiamondBattleAxeItem
                || item instanceof DiamondBattleHammerItem
                || item instanceof DiamondBattleScytheItem
                || item instanceof DiamondClawsItem
                || item instanceof DiamondDaggerItem
                || item instanceof DiamondGreatswordItem
                || item instanceof DiamondLongswordItem
                || item instanceof DiamondShortswordItem
                || item instanceof DiamondSickleItem
                || item instanceof DiamondSpearItem
                || item instanceof DiamondStaffItem
                || item instanceof DiamondTachiItem) {
            return WEAPONS;
        }

        return null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(GsonHelper.getAsFloat(json, "ignore_defense_proc_chance"));
    }

    public record Profile(float ignoreDefenseProcChance) {
    }
}
