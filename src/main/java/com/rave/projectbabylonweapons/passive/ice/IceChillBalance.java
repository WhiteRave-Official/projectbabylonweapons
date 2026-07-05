package com.rave.projectbabylonweapons.passive.ice;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.battleaxe.IceBattleAxeItem;
import com.rave.projectbabylonweapons.item.battlehammer.IceBattleHammerItem;
import com.rave.projectbabylonweapons.item.battlescythe.IceBattleScytheItem;
import com.rave.projectbabylonweapons.item.claws.IceClawsItem;
import com.rave.projectbabylonweapons.item.dagger.IceDaggerItem;
import com.rave.projectbabylonweapons.item.greatsword.IceGreatswordItem;
import com.rave.projectbabylonweapons.item.longsword.IceLongswordItem;
import com.rave.projectbabylonweapons.item.rapier.IceRapierItem;
import com.rave.projectbabylonweapons.item.shortsword.IceShortswordItem;
import com.rave.projectbabylonweapons.item.sickle.IceSickleItem;
import com.rave.projectbabylonweapons.item.spear.IceSpearItem;
import com.rave.projectbabylonweapons.item.staff.IceStaffItem;
import com.rave.projectbabylonweapons.item.tachi.IceTachiItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import com.rave.projectbabylonweapons.util.PBItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class IceChillBalance {
    public static final int CHILL_I_AMPLIFIER = 0;
    public static final int CHILL_II_AMPLIFIER = 1;
    public static final int CHILL_III_AMPLIFIER = 2;

    private static final Profile TOOLS = new Profile(0.30F, 0.30F, 0.30F, 0.30F, 20 * 6, 20 * 6, 20 * 6, 20 * 2);
    private static final Profile TACHI = new Profile(0.30F, 0.30F, 0.30F, 0.30F, 20 * 6, 20 * 6, 20 * 6, 20);
    private static final Profile STAFF = new Profile(0.20F, 0.20F, 0.20F, 0.20F, 20 * 5, 20 * 5, 20 * 5, 20 * 3);
    private static final Profile DAGGER = new Profile(0.20F, 0.20F, 0.20F, 0.20F, 20 * 5, 20 * 5, 20 * 5, 20);
    private static final Profile CLAWS = new Profile(0.20F, 0.20F, 0.20F, 0.20F, 20 * 5, 20 * 5, 20 * 5, 20);
    private static final Profile BATTLE_SCYTHE = new Profile(0.25F, 0.25F, 0.25F, 0.25F, 20 * 5, 20 * 5, 20 * 5, 20 * 2);
    private static final Profile SICKLE = new Profile(0.25F, 0.25F, 0.25F, 0.25F, 20 * 5, 20 * 5, 20 * 5, 20);
    private static final Profile SHORTSWORD = new Profile(0.30F, 0.30F, 0.30F, 0.30F, 20 * 6, 20 * 6, 20 * 6, 20 * 2);
    private static final Profile SPEAR = new Profile(0.30F, 0.30F, 0.30F, 0.30F, 20 * 6, 20 * 6, 20 * 6, 20 * 2);
    private static final Profile RAPIER = new Profile(0.40F, 0.40F, 0.40F, 0.40F, 20 * 6, 20 * 6, 20 * 6, 20 * 2);
    private static final Profile LONGSWORD = new Profile(0.33F, 0.33F, 0.33F, 0.33F, 20 * 7, 20 * 7, 20 * 7, 20 * 2);
    private static final Profile BATTLE_HAMMER = new Profile(0.33F, 0.33F, 0.33F, 0.33F, 20 * 8, 20 * 8, 20 * 8, 20 * 4);
    private static final Profile BATTLE_AXE = new Profile(0.35F, 0.35F, 0.35F, 0.35F, 20 * 8, 20 * 8, 20 * 8, 20 * 3);
    private static final Profile GREATSWORD = new Profile(0.35F, 0.35F, 0.35F, 0.35F, 20 * 8, 20 * 8, 20 * 8, 20 * 3);

    private IceChillBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.ICE_CHILL, stack, IceChillBalance::parseProfile);
        if (override != null) {
            return override;
        }

        if (stack.is(PBItemTags.ICE_TOOLS)) {
            return TOOLS;
        }

        Item item = stack.getItem();

        if (item instanceof IceTachiItem) {
            return TACHI;
        }
        if (item instanceof IceStaffItem) {
            return STAFF;
        }
        if (item instanceof IceDaggerItem) {
            return DAGGER;
        }
        if (item instanceof IceClawsItem) {
            return CLAWS;
        }
        if (item instanceof IceBattleScytheItem) {
            return BATTLE_SCYTHE;
        }
        if (item instanceof IceSickleItem) {
            return SICKLE;
        }
        if (item instanceof IceShortswordItem) {
            return SHORTSWORD;
        }
        if (item instanceof IceSpearItem) {
            return SPEAR;
        }
        if (item instanceof IceRapierItem) {
            return RAPIER;
        }
        if (item instanceof IceLongswordItem) {
            return LONGSWORD;
        }
        if (item instanceof IceBattleHammerItem) {
            return BATTLE_HAMMER;
        }
        if (item instanceof IceBattleAxeItem) {
            return BATTLE_AXE;
        }
        if (item instanceof IceGreatswordItem) {
            return GREATSWORD;
        }

        return null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsFloat(json, "chill_i_proc_chance"),
                GsonHelper.getAsFloat(json, "chill_ii_proc_chance"),
                GsonHelper.getAsFloat(json, "chill_iii_proc_chance"),
                GsonHelper.getAsFloat(json, "frozen_from_chill_iii_proc_chance"),
                GsonHelper.getAsInt(json, "chill_i_duration_ticks"),
                GsonHelper.getAsInt(json, "chill_ii_duration_ticks"),
                GsonHelper.getAsInt(json, "chill_iii_duration_ticks"),
                GsonHelper.getAsInt(json, "frozen_duration_ticks")
        );
    }

    public record Profile(
            float chillIProcChance,
            float chillIIProcChance,
            float chillIIIProcChance,
            float frozenFromChillIIIProcChance,
            int chillIDurationTicks,
            int chillIIDurationTicks,
            int chillIIIDurationTicks,
            int frozenDurationTicks
    ) {
    }
}
