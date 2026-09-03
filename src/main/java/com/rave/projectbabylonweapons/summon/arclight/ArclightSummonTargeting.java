package com.rave.projectbabylonweapons.summon.arclight;

import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

public final class ArclightSummonTargeting {
    private ArclightSummonTargeting() {
    }

    public static LivingEntity findTarget(LivingEntity owner, double radius) {
        AABB area = owner.getBoundingBox().inflate(radius, radius * 0.5D, radius);
        return owner.level().getEntitiesOfClass(LivingEntity.class, area,
                        candidate -> isValid(owner, candidate, radius))
                .stream()
                .min(Comparator.comparingDouble(owner::distanceToSqr))
                .orElse(null);
    }

    public static boolean isValid(LivingEntity owner, LivingEntity target, double radius) {
        if (target == null || target == owner || target instanceof ArclightSummonedWeaponEntity
                || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        if (target instanceof Player player && player.isSpectator()) {
            return false;
        }
        if (owner.isAlliedTo(target) || target.isAlliedTo(owner)) {
            return false;
        }
        return owner.distanceToSqr(target) <= radius * radius;
    }

    public static int countNearbyEnemies(LivingEntity owner, LivingEntity target, double radius) {
        return owner.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(radius),
                candidate -> isValid(owner, candidate, Double.MAX_VALUE)).size();
    }
}