package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;

import com.rave.projectbabylonweapons.item.MagicProjectileStaffWeapon;
import com.rave.projectbabylonweapons.skill.weapon_innate.DragonDescendSkill;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.function.Function;

public final class StaffProjectileAttackHelper {
    private StaffProjectileAttackHelper() {
    }

    public static void spawnProjectile(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon) {
        LivingEntity attacker = playerPatch.getOriginal();
        Vec3 forward = horizontalForward(attacker);
        spawnProjectileInternal(playerPatch, weaponStack, weapon, forward, weapon.getMagicProjectileSpawnForwardOffset(), 0.0D,
                weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier(), true, 1.0F, null);
    }

    public static void spawnProjectile(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon,
                                       Vec3 direction, double forwardOffset, double sideOffset, double verticalOffset,
                                       float damageMultiplier) {
        spawnProjectileInternal(playerPatch, weaponStack, weapon, direction, forwardOffset, sideOffset, verticalOffset, damageMultiplier, true, 1.0F, null);
    }

    public static void spawnProjectileWithoutPassives(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon,
                                                      Vec3 direction, double forwardOffset, double sideOffset, double verticalOffset,
                                                      float damageMultiplier) {
        spawnProjectileWithoutPassives(playerPatch, weaponStack, weapon, direction, forwardOffset, sideOffset, verticalOffset, damageMultiplier, 1.0F);
    }

    public static void spawnProjectileWithoutPassives(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon,
                                                      Vec3 direction, double forwardOffset, double sideOffset, double verticalOffset,
                                                      float damageMultiplier, float renderScale) {
        spawnProjectileInternal(playerPatch, weaponStack, weapon, direction, forwardOffset, sideOffset, verticalOffset, damageMultiplier, false, renderScale, null);
    }

    public static void spawnProjectileWithoutPassives(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon,
                                                      Vec3 direction, double forwardOffset, double sideOffset, double verticalOffset,
                                                      float damageMultiplier, float renderScale,
                                                      Function<Level, BasicSpellProjectileEntity> projectileFactory) {
        spawnProjectileInternal(playerPatch, weaponStack, weapon, direction, forwardOffset, sideOffset, verticalOffset, damageMultiplier, false, renderScale, projectileFactory);
    }

    private static void spawnProjectileInternal(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon,
                                                Vec3 direction, double forwardOffset, double sideOffset, double verticalOffset,
                                                float damageMultiplier, boolean allowPassiveEffects, float renderScale,
                                                Function<Level, BasicSpellProjectileEntity> projectileFactory) {
        LivingEntity attacker = playerPatch.getOriginal();
        BasicSpellProjectileEntity projectile = projectileFactory != null
                ? projectileFactory.apply(attacker.level())
                : weapon.createMagicProjectile(attacker.level());
        if (projectile == null) {
            return;
        }

        Vec3 baseForward = horizontalForward(attacker);
        Vec3 shootDirection = new Vec3(direction.x, 0.0D, direction.z);
        if (shootDirection.lengthSqr() < 1.0E-6D) {
            shootDirection = baseForward;
        } else {
            shootDirection = shootDirection.normalize();
        }

        float adjustedDamageMultiplier = BattleWandPassiveHooks.adjustProjectileDamageMultiplier(attacker, weaponStack, damageMultiplier);
        float rawMagicDamage = MagicMeleeWeaponHelper.calculateRawMagicDamage(attacker, weaponStack, weapon, 1.0F, adjustedDamageMultiplier);
        if (rawMagicDamage <= 0.0F) {
            return;
        }

        float armorNegation = StaffMagicArmorHelper.resolveWeaponMagicArmorNegation(attacker, weaponStack);
        float impact = StaffMagicArmorHelper.resolveWeaponImpact(attacker, weaponStack);

        Vec3 horizontalRight = new Vec3(-baseForward.z, 0.0D, baseForward.x);
        if (horizontalRight.lengthSqr() < 1.0E-6D) {
            horizontalRight = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            horizontalRight = horizontalRight.normalize();
        }

        Vec3 basePos = attacker.position().add(0.0D, attacker.getBbHeight() * 0.6D, 0.0D);
        Vec3 spawnPos = basePos
                .add(baseForward.scale(forwardOffset))
                .add(horizontalRight.scale(sideOffset))
                .add(0.0D, verticalOffset, 0.0D);

        projectile.configureMagicProjectile(
                attacker,
                weaponStack.copy(),
                weapon.getMagicDamageType(),
                rawMagicDamage,
                armorNegation,
                impact,
                weapon.getMagicProjectileStunType(),
                weapon.getMagicProjectileLifetime(),
                weapon.getMagicProjectileTrailColor()
        );
        projectile.setBasicWandAttack(allowPassiveEffects);
        BattleWandPassiveHooks.configureProjectile(projectile, allowPassiveEffects);
        projectile.setVisualScale(renderScale);
        projectile.setPiercing(DragonDescendSkill.isDragonDescendActive(attacker));
        projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        weapon.playMagicProjectileCastSound(attacker);
        projectile.shoot(shootDirection.x, shootDirection.y, shootDirection.z, weapon.getMagicProjectileSpeed(), weapon.getMagicProjectileInaccuracy());

        attacker.level().addFreshEntity(projectile);
        BattleWandPassiveHooks.afterProjectileSpawn(playerPatch, weaponStack, weapon, shootDirection, forwardOffset, verticalOffset, damageMultiplier, allowPassiveEffects);
    }

    private static Vec3 horizontalForward(LivingEntity attacker) {
        Vec3 look = attacker.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        if (flat.lengthSqr() < 1.0E-6D) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }
        return flat.normalize();
    }
}