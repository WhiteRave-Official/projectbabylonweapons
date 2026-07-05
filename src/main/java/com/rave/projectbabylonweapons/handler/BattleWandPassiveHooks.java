package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.item.MagicProjectileStaffWeapon;
import com.rave.projectbabylonweapons.passive.wand.DiamondRicochetPassive;
import com.rave.projectbabylonweapons.passive.wand.DragonsteelDragonlordPassive;
import com.rave.projectbabylonweapons.passive.wand.EtherealSanctuaryPassive;
import com.rave.projectbabylonweapons.passive.wand.GoldenBloodPactPassive;
import com.rave.projectbabylonweapons.passive.wand.IceFrostTouchPassive;
import com.rave.projectbabylonweapons.passive.wand.NetheriteAshenPassive;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public final class BattleWandPassiveHooks {
    private BattleWandPassiveHooks() {
    }

    public static void onAttackPhaseEnd(ServerPlayerPatch playerPatch, ItemStack weaponStack) {
        GoldenBloodPactPassive.onBattleWandAttack(playerPatch.getOriginal(), weaponStack);
    }

    public static float adjustProjectileDamageMultiplier(LivingEntity attacker, ItemStack weaponStack, float damageMultiplier) {
        return damageMultiplier * GoldenBloodPactPassive.getProjectileDamageMultiplier(attacker, weaponStack);
    }

    public static void configureProjectile(BasicSpellProjectileEntity projectile, boolean allowPassiveEffects) {
        if (!allowPassiveEffects) {
            return;
        }

        DiamondRicochetPassive.configureProjectile(projectile);
    }

    public static void afterProjectileSpawn(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon,
                                            Vec3 direction, double forwardOffset, double verticalOffset,
                                            float damageMultiplier, boolean allowPassiveEffects) {
        if (!allowPassiveEffects) {
            return;
        }

        DragonsteelDragonlordPassive.spawnMiniProjectiles(playerPatch, weaponStack, weapon, direction, forwardOffset, verticalOffset, damageMultiplier);
    }

    public static void onProjectileHitEntity(BasicSpellProjectileEntity projectile, LivingEntity target, LivingEntity owner,
                                             float adjustedMagicDamage, DamageSource damageSource) {
        NetheriteAshenPassive.onProjectileHit(projectile, target, owner, damageSource);
        EtherealSanctuaryPassive.onProjectileHit(projectile, owner);
        IceFrostTouchPassive.onProjectileHit(projectile, target, owner);
    }

    public static boolean tryHandleBlockHit(BasicSpellProjectileEntity projectile, BlockHitResult result) {
        return DiamondRicochetPassive.tryRicochet(projectile, result);
    }
}
