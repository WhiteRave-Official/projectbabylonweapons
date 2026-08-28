package com.rave.projectbabylonweapons.passive.dragonsteel;

import com.google.gson.JsonObject;
import com.rave.projectbabylonweapons.item.battleaxe.DragonsteelBattleAxeItem;
import com.rave.projectbabylonweapons.item.battlehammer.DragonsteelBattleHammerItem;
import com.rave.projectbabylonweapons.item.battlescythe.DragonsteelBattleScytheItem;
import com.rave.projectbabylonweapons.item.claws.DragonsteelClawsItem;
import com.rave.projectbabylonweapons.item.dagger.DragonsteelDaggerItem;
import com.rave.projectbabylonweapons.item.greatsword.DragonsteelGreatswordItem;
import com.rave.projectbabylonweapons.item.longsword.DragonsteelLongswordItem;
import com.rave.projectbabylonweapons.item.messer.DragonsteelMesserItem;
import com.rave.projectbabylonweapons.item.rapier.DragonsteelRapierItem;
import com.rave.projectbabylonweapons.item.shortsword.DragonsteelShortswordItem;
import com.rave.projectbabylonweapons.item.sickle.DragonsteelSickleItem;
import com.rave.projectbabylonweapons.item.spear.DragonsteelSpearItem;
import com.rave.projectbabylonweapons.item.staff.DragonsteelStaffItem;
import com.rave.projectbabylonweapons.item.tachi.DragonsteelTachiItem;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DragonsteelWyrmEchoBalance {
    private static final Profile DEFAULT = new Profile(5, 0.15F, 5.0F, 0.03F, 40, 4, 0.20F, 5.0F, 0.06F, 60, 0.85F, 0.55F, 5);

    private DragonsteelWyrmEchoBalance() {
    }

    public static Profile resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Profile override = WeaponPassivePatchManager.INSTANCE.resolveProfile(WeaponPassiveIds.DRAGONSTEEL_WYRM_ECHO, stack, DragonsteelWyrmEchoBalance::parseProfile);
        if (override != null) {
            return override;
        }

        Item item = stack.getItem();
        if (item instanceof DragonsteelBattleAxeItem
                || item instanceof DragonsteelBattleHammerItem
                || item instanceof DragonsteelBattleScytheItem
                || item instanceof DragonsteelClawsItem
                || item instanceof DragonsteelDaggerItem
                || item instanceof DragonsteelGreatswordItem
                || item instanceof DragonsteelLongswordItem
                || item instanceof DragonsteelRapierItem
                || item instanceof DragonsteelShortswordItem
                || item instanceof DragonsteelSickleItem
                || item instanceof DragonsteelSpearItem
                || item instanceof DragonsteelStaffItem
                || item instanceof DragonsteelMesserItem
                || item instanceof DragonsteelTachiItem) {
            return DEFAULT;
        }

        return null;
    }

    private static Profile parseProfile(JsonObject json) {
        return new Profile(
                GsonHelper.getAsInt(json, "normal_attack_interval", DEFAULT.normalAttackInterval()),
                GsonHelper.getAsFloat(json, "normal_damage_multiplier", DEFAULT.normalDamageMultiplier()),
                GsonHelper.getAsFloat(json, "normal_range_blocks", DEFAULT.normalRangeBlocks()),
                GsonHelper.getAsFloat(json, "normal_trail_damage_multiplier", DEFAULT.normalTrailDamageMultiplier()),
                GsonHelper.getAsInt(json, "normal_trail_lifetime_ticks", DEFAULT.normalTrailLifetimeTicks()),
                GsonHelper.getAsInt(json, "berserk_attack_interval", DEFAULT.berserkAttackInterval()),
                GsonHelper.getAsFloat(json, "berserk_damage_multiplier", DEFAULT.berserkDamageMultiplier()),
                GsonHelper.getAsFloat(json, "berserk_range_blocks", DEFAULT.berserkRangeBlocks()),
                GsonHelper.getAsFloat(json, "berserk_trail_damage_multiplier", DEFAULT.berserkTrailDamageMultiplier()),
                GsonHelper.getAsInt(json, "berserk_trail_lifetime_ticks", DEFAULT.berserkTrailLifetimeTicks()),
                GsonHelper.getAsFloat(json, "projectile_speed", DEFAULT.projectileSpeed()),
                GsonHelper.getAsFloat(json, "hit_radius", DEFAULT.hitRadius()),
                GsonHelper.getAsInt(json, "trail_damage_interval_ticks", DEFAULT.trailDamageIntervalTicks())
        );
    }

    public record Profile(
            int normalAttackInterval,
            float normalDamageMultiplier,
            float normalRangeBlocks,
            float normalTrailDamageMultiplier,
            int normalTrailLifetimeTicks,
            int berserkAttackInterval,
            float berserkDamageMultiplier,
            float berserkRangeBlocks,
            float berserkTrailDamageMultiplier,
            int berserkTrailLifetimeTicks,
            float projectileSpeed,
            float hitRadius,
            int trailDamageIntervalTicks
    ) {
        public int attackInterval(boolean berserk) {
            return Math.max(1, berserk ? this.berserkAttackInterval : this.normalAttackInterval);
        }

        public float damageMultiplier(boolean berserk) {
            return Math.max(0.0F, berserk ? this.berserkDamageMultiplier : this.normalDamageMultiplier);
        }


        public float trailDamageMultiplier(boolean berserk) {
            return Math.max(0.0F, berserk ? this.berserkTrailDamageMultiplier : this.normalTrailDamageMultiplier);
        }

        public int trailLifetimeTicks(boolean berserk) {
            return Math.max(1, berserk ? this.berserkTrailLifetimeTicks : this.normalTrailLifetimeTicks);
        }
        public float rangeBlocks(boolean berserk) {
            return Math.max(0.1F, berserk ? this.berserkRangeBlocks : this.normalRangeBlocks);
        }
    }
}

