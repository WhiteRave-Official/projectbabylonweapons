package com.rave.projectbabylonweapons.passive.netherite;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.battleaxe.NetheriteBattleAxeItem;
import com.rave.projectbabylonweapons.item.battlehammer.NetheriteBattleHammerItem;
import com.rave.projectbabylonweapons.item.battlescythe.NetheriteBattleScytheItem;
import com.rave.projectbabylonweapons.item.claws.NetheriteClawsItem;
import com.rave.projectbabylonweapons.item.dagger.NetheriteDaggerItem;
import com.rave.projectbabylonweapons.item.greatsword.NetheriteGreatswordItem;
import com.rave.projectbabylonweapons.item.longsword.NetheriteLongswordItem;
import com.rave.projectbabylonweapons.item.shortsword.NetheriteShortswordItem;
import com.rave.projectbabylonweapons.item.sickle.NetheriteSickleItem;
import com.rave.projectbabylonweapons.item.spear.NetheriteSpearItem;
import com.rave.projectbabylonweapons.item.staff.NetheriteStaffItem;
import com.rave.projectbabylonweapons.item.tachi.NetheriteTachiItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import com.rave.projectbabylonweapons.util.PBItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class NetheriteBrimstoneBalance {
    private static final Profile TOOLS = new Profile(0.30F, 0.35F, 20 * 6, 0.35F, 20 * 6, 0.30F, 0.40F, 2.0F);
    private static final Profile TACHI = new Profile(0.30F, 0.30F, 20 * 5, 0.30F, 20 * 5, 0.30F, 0.40F, 1.5F);
    private static final Profile STAFF = new Profile(0.20F, 0.20F, 20 * 5, 0.20F, 20 * 5, 0.30F, 0.30F, 3.0F);
    private static final Profile DAGGER = new Profile(0.20F, 0.25F, 20 * 5, 0.25F, 20 * 5, 0.20F, 0.30F, 1.5F);
    private static final Profile CLAWS = new Profile(0.20F, 0.25F, 20 * 5, 0.25F, 20 * 5, 0.25F, 0.30F, 1.5F);
    private static final Profile BATTLE_SCYTHE = new Profile(0.25F, 0.25F, 20 * 5, 0.25F, 20 * 5, 0.30F, 0.30F, 2.5F);
    private static final Profile SICKLE = new Profile(0.30F, 0.30F, 20 * 6, 0.30F, 20 * 6, 0.30F, 0.30F, 1.5F);
    private static final Profile SHORTSWORD = new Profile(0.30F, 0.35F, 20 * 6, 0.35F, 20 * 6, 0.30F, 0.40F, 2.0F);
    private static final Profile SPEAR = new Profile(0.30F, 0.30F, 20 * 6, 0.30F, 20 * 6, 0.30F, 0.40F, 2.0F);
    private static final Profile LONGSWORD = new Profile(0.30F, 0.30F, 20 * 7, 0.30F, 20 * 7, 0.35F, 0.40F, 2.5F);
    private static final Profile BATTLE_HAMMER = new Profile(0.40F, 0.40F, 20 * 8, 0.40F, 20 * 8, 0.50F, 0.50F, 3.0F);
    private static final Profile BATTLE_AXE = new Profile(0.35F, 0.35F, 20 * 8, 0.35F, 20 * 8, 0.35F, 0.40F, 2.8F);
    private static final Profile GREATSWORD = new Profile(0.35F, 0.35F, 20 * 8, 0.35F, 20 * 8, 0.35F, 0.50F, 3.0F);

    private NetheriteBrimstoneBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.NETHERITE_BRIMSTONE, stack, NetheriteBrimstoneBalance::parseProfile);
        if (override != null) {
            return override;
        }

        if (stack.is(PBItemTags.NETHERITE_TOOLS)) {
            return TOOLS;
        }

        Item item = stack.getItem();
        if (item instanceof NetheriteTachiItem) {
            return TACHI;
        }
        if (item instanceof NetheriteStaffItem) {
            return STAFF;
        }
        if (item instanceof NetheriteDaggerItem) {
            return DAGGER;
        }
        if (item instanceof NetheriteClawsItem) {
            return CLAWS;
        }
        if (item instanceof NetheriteBattleScytheItem) {
            return BATTLE_SCYTHE;
        }
        if (item instanceof NetheriteSickleItem) {
            return SICKLE;
        }
        if (item instanceof NetheriteShortswordItem) {
            return SHORTSWORD;
        }
        if (item instanceof NetheriteSpearItem) {
            return SPEAR;
        }
        if (item instanceof NetheriteLongswordItem) {
            return LONGSWORD;
        }
        if (item instanceof NetheriteBattleHammerItem) {
            return BATTLE_HAMMER;
        }
        if (item instanceof NetheriteBattleAxeItem) {
            return BATTLE_AXE;
        }
        if (item instanceof NetheriteGreatswordItem) {
            return GREATSWORD;
        }

        return null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsFloat(json, "ignite_proc_chance"),
                GsonHelper.getAsFloat(json, "brimstone_flames_proc_chance"),
                GsonHelper.getAsInt(json, "brimstone_flames_duration_ticks"),
                GsonHelper.getAsFloat(json, "brimstone_fire_proc_chance"),
                GsonHelper.getAsInt(json, "brimstone_fire_duration_ticks"),
                GsonHelper.getAsFloat(json, "brimstone_blast_proc_chance"),
                GsonHelper.getAsFloat(json, "brimstone_blast_damage_multiplier"),
                GsonHelper.getAsFloat(json, "brimstone_blast_radius_blocks")
        );
    }

    public record Profile(
            float igniteProcChance,
            float brimstoneFlamesProcChance,
            int brimstoneFlamesDurationTicks,
            float brimstoneFireProcChance,
            int brimstoneFireDurationTicks,
            float brimstoneBlastProcChance,
            float brimstoneBlastDamageMultiplier,
            float brimstoneBlastRadiusBlocks
    ) {
    }
}
