package com.rave.projectbabylonweapons.client;

import com.rave.projectbabylonmaterials.client.photon.PBMPhotonEffectHelper;
import com.rave.projectbabylonweapons.passive.bastion.BastionPermafrostBalance;
import com.rave.projectbabylonweapons.passive.bastion.BastionRuleAuraBalance;
import com.rave.projectbabylonweapons.world.entity.effect.FireStormEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class PhotonWeaponEffectHelper {
    private PhotonWeaponEffectHelper() {
    }

    public static void startDragonDescendCast(Entity entity) {
        PBMPhotonEffectHelper.startDragonDescendCast(entity);
    }

    public static void burstDragonDescendCast(Entity entity) {
        PBMPhotonEffectHelper.burstDragonDescendCast(entity);
    }

    public static void stopDragonDescendCast(Entity entity) {
        PBMPhotonEffectHelper.stopDragonDescendCast(entity);
    }

    public static void startGlacierCast(Entity entity) {
        PBMPhotonEffectHelper.startGlacierCast(entity);
    }

    public static void stopGlacierCast(Entity entity) {
        PBMPhotonEffectHelper.stopGlacierCast(entity);
    }

    public static void startFireStormCast(Entity entity) {
        PBMPhotonEffectHelper.startFireStormCast(entity);
    }

    public static void burstFireStormCast(Entity entity) {
        PBMPhotonEffectHelper.burstFireStormCast(entity);
    }

    public static void stopFireStormCast(Entity entity) {
        PBMPhotonEffectHelper.stopFireStormCast(entity);
    }

    public static void spawnFireStorm(FireStormEntity entity) {
        PBMPhotonEffectHelper.spawnFireStorm(entity, entity.getVisualProgress(), entity.getVisualHeight(), entity.getVisualRadius(), entity.tickCount);
    }

    public static void startBlessingCast(Entity entity) {
        PBMPhotonEffectHelper.startBlessingCast(entity);
    }

    public static void burstBlessingCast(Entity entity) {
        PBMPhotonEffectHelper.burstBlessingCast(entity);
    }

    public static void stopBlessingCast(Entity entity) {
        PBMPhotonEffectHelper.stopBlessingCast(entity);
    }

    public static void spawnGlacierContactWave(Entity entity) {
        PBMPhotonEffectHelper.spawnGlacierContactWave(entity);
    }

    public static void spawnDragonDescendFlight(Entity projectile, Vec3 movement) {
        PBMPhotonEffectHelper.spawnDragonDescendFlight(projectile, movement);
    }

    public static void spawnEnderProjectileFlight(Entity projectile, Vec3 movement) {
        PBMPhotonEffectHelper.spawnEnderProjectileFlight(projectile, movement);
    }

    public static void spawnEnderProjectileImpact(Entity projectile, Vec3 hitPos) {
        PBMPhotonEffectHelper.spawnEnderProjectileImpact(projectile, hitPos);
    }

    public static void spawnManaBubbleBasicContact(Entity entity) {
        PBMPhotonEffectHelper.spawnEnderProjectileImpact(entity, entity.position().add(0.0D, entity.getBbHeight() * 0.6D, 0.0D));
    }

    public static void spawnManaBubbleBasicFlight(Entity projectile, Vec3 movement) {
        PBMPhotonEffectHelper.spawnEnderProjectileFlight(projectile, movement);
    }

    public static void spawnManaBubbleBasicImpact(Entity projectile, Vec3 hitPos) {
        PBMPhotonEffectHelper.spawnEnderProjectileImpact(projectile, hitPos);
    }

    public static void spawnHolyProjectileFlight(Entity projectile, Vec3 movement) {
        PBMPhotonEffectHelper.spawnHolyProjectileFlight(projectile, movement);
    }

    public static void spawnHolyProjectileImpact(Entity projectile, Vec3 hitPos) {
        PBMPhotonEffectHelper.spawnHolyProjectileImpact(projectile, hitPos);
    }

    public static void spawnIceProjectileFlight(Entity projectile, Vec3 movement) {
        PBMPhotonEffectHelper.spawnIceProjectileFlight(projectile, movement);
    }

    public static void spawnIceProjectileImpact(Entity projectile, Vec3 hitPos) {
        PBMPhotonEffectHelper.spawnIceProjectileImpact(projectile, hitPos);
    }

    public static void spawnFireProjectileFlight(Entity projectile, Vec3 movement) {
        PBMPhotonEffectHelper.spawnFireProjectileFlight(projectile, movement);
    }

    public static void spawnFireProjectileImpact(Entity projectile, Vec3 hitPos) {
        PBMPhotonEffectHelper.spawnFireProjectileImpact(projectile, hitPos);
    }


    public static void spawnBrimstoneBlast(Entity entity) {
        PBMPhotonEffectHelper.spawnFireProjectileImpact(entity, entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D));
    }
    public static void spawnGoldenProjectileFlight(Entity projectile, Vec3 movement) {
        PBMPhotonEffectHelper.spawnGoldenProjectileFlight(projectile, movement);
    }

    public static void spawnGoldenProjectileImpact(Entity projectile, Vec3 hitPos) {
        PBMPhotonEffectHelper.spawnGoldenProjectileImpact(projectile, hitPos);
    }

    public static void spawnDiamondProjectileFlight(Entity projectile, Vec3 movement) {
        PBMPhotonEffectHelper.spawnDiamondProjectileFlight(projectile, movement);
    }

    public static void spawnDiamondProjectileImpact(Entity projectile, Vec3 hitPos) {
        PBMPhotonEffectHelper.spawnDiamondProjectileImpact(projectile, hitPos);
    }

    public static void spawnDiamondShardOrbitTrail(Entity entity, Vec3 movement) {
        PBMPhotonEffectHelper.spawnDiamondProjectileFlight(entity, movement.scale(1.15D));
    }

    public static void spawnDiamondShardFlightTrail(Entity entity, Vec3 movement) {
        PBMPhotonEffectHelper.spawnDiamondProjectileFlight(entity, movement);
    }

    public static void spawnDiamondShardBurst(Entity entity) {
        PBMPhotonEffectHelper.spawnDiamondProjectileImpact(entity, entity.position().add(0.0D, 0.08D, 0.0D));
    }

    public static void spawnDragonFuryChargeTrail(Entity entity, Vec3 movement) {
        PBMPhotonEffectHelper.spawnEnderProjectileFlight(entity, movement.scale(1.05D));
    }

    public static void spawnDragonFuryChargeBurst(Entity entity) {
        PBMPhotonEffectHelper.spawnEnderProjectileImpact(entity, entity.position().add(0.0D, 0.08D, 0.0D));
    }

    public static void spawnBlessingHealPulse(Entity entity) {
        PBMPhotonEffectHelper.spawnBlessingHealPulse(entity);
    }

    public static void spawnBlessingAbsorptionPulse(Entity entity) {
        PBMPhotonEffectHelper.spawnBlessingAbsorptionPulse(entity);
    }

    public static void startBastionFrostAura(Entity entity) {
        PBMPhotonEffectHelper.startBastionFrostAura(entity, resolveBastionAuraRadius(entity, true));
    }

    public static void stopBastionFrostAura(Entity entity) {
        PBMPhotonEffectHelper.stopBastionFrostAura(entity);
    }

    public static void startBastionRuleAura(Entity entity) {
        PBMPhotonEffectHelper.startBastionRuleAura(entity, resolveBastionAuraRadius(entity, false));
    }

    public static void stopBastionRuleAura(Entity entity) {
        PBMPhotonEffectHelper.stopBastionRuleAura(entity);
    }

    private static float resolveBastionAuraRadius(Entity entity, boolean frost) {
        if (entity instanceof LivingEntity livingEntity) {
            if (frost) {
                BastionPermafrostBalance.Profile profile = BastionPermafrostBalance.resolve(livingEntity.getOffhandItem());
                if (profile != null) {
                    return profile.radiusBlocks();
                }
            } else {
                BastionRuleAuraBalance.Profile profile = BastionRuleAuraBalance.resolve(livingEntity.getOffhandItem());
                if (profile != null) {
                    return profile.radiusBlocks();
                }
            }
        }

        return 8.0F;
    }

    public static void spawnAbsorptionShield(LivingEntity entity, float progress, int tick, float absorptionAmount) {
        PBMPhotonEffectHelper.spawnAbsorptionShield(entity, progress, tick, absorptionAmount);
    }
}
