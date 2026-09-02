package com.rave.projectbabylonweapons.summon.arclight;

import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponState;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class ArclightSummonAttackController {
    private static final double TELEPORT_DISTANCE_SQR = 32.0D * 32.0D;

    private ArclightSummonAttackController() {
    }

    public static void tick(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        if (weapon.distanceToSqr(owner) > TELEPORT_DISTANCE_SQR) {
            weapon.setPos(weapon.getIdlePosition(owner, null));
            weapon.setCombatState(ArclightSummonedWeaponState.ORBIT);
            weapon.setTarget(null);
        }

        weapon.decrementCooldown();
        switch (weapon.getCombatState()) {
            case ORBIT -> tickOrbit(weapon, owner);
            case WINDUP -> tickWindup(weapon, owner);
            case ATTACK -> tickAttack(weapon, owner);
            case RETURN -> tickRecovery(weapon, owner);
            case COOLDOWN -> tickCooldown(weapon, owner);
        }
        weapon.incrementStateTicks();
    }

    private static void tickOrbit(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        LivingEntity target = validTarget(weapon, owner);
        if (target == null) {
            weapon.moveToward(weapon.getIdlePosition(owner, null), 0.14D, 0.45D);
        } else {
            weapon.setDeltaMovement(weapon.getDeltaMovement().scale(0.8D));
        }
        if (weapon.getCooldownTicks() > 0 || weapon.tickCount < weapon.getNextTargetSearchTick()) {
            return;
        }

        if (target == null) {
            target = ArclightSummonTargeting.findTarget(owner, weapon.getBalance().targetRadius());
            weapon.setTarget(target);
        }
        weapon.setNextTargetSearchTick(weapon.tickCount + weapon.getBalance().targetSearchInterval());
        if (target != null) {
            beginWindup(weapon, owner, target);
        }
    }

    private static LivingEntity validTarget(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        LivingEntity target = weapon.getTarget();
        if (!ArclightSummonTargeting.isValid(owner, target, weapon.getBalance().targetRadius() * 1.25D)) {
            weapon.setTarget(null);
            return null;
        }
        return target;
    }

    private static void beginWindup(ArclightSummonedWeaponEntity weapon, LivingEntity owner, LivingEntity target) {
        ArclightSummonedWeaponEntity.AttackType attack = ArclightSummonAttackSelector.select(weapon, owner, target);
        weapon.setAttackType(attack);
        weapon.setLastAttack(attack);
        weapon.setAttackOrigin(weapon.position());
        weapon.setAttackTarget(target.getBoundingBox().getCenter());
        weapon.clearHitEntities();
        weapon.setCombatState(ArclightSummonedWeaponState.WINDUP);
    }

    private static void tickWindup(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        Vec3 center = weapon.getAttackTarget();
        Vec3 forward = horizontalDirection(weapon.getAttackOrigin(), center);
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        double sideSign = weapon.getFormationIndex() == 0 ? -1.0D : 1.0D;
        double lengthScale = weapon.isSpear() ? 1.25D : 1.0D;
        Vec3 windup = switch (weapon.getAttackType()) {
            case STAB -> center.subtract(forward.scale(2.5D * lengthScale)).add(0.0D, 0.15D, 0.0D);
            case VERTICAL -> center.subtract(forward.scale(0.65D))
                    .add(0.0D, weapon.isSpear() ? 3.6D : 3.0D, 0.0D);
            case HORIZONTAL -> center.add(side.scale(sideSign * 2.45D * lengthScale))
                    .add(0.0D, 0.45D, 0.0D);
            case DASH -> center.subtract(forward.scale(3.8D * lengthScale)).add(0.0D, 0.25D, 0.0D);
            case SPIN -> center.add(side.scale(sideSign * 2.8D)).add(0.0D, 0.5D, 0.0D);
            default -> weapon.getIdlePosition(owner, null);
        };

        weapon.moveToward(windup, 0.42D, weapon.isSpear() ? 1.25D : 1.05D, center);
        int windupDuration = weapon.getBalance().windupTicks() + (weapon.isSpear() ? 3 : 0);
        if (weapon.getStateTicks() + 1 >= windupDuration) {
            weapon.setAttackOrigin(weapon.position());
            weapon.setCombatState(ArclightSummonedWeaponState.ATTACK);
        }
    }

    private static void tickAttack(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        Vec3 targetPosition = weapon.getAttackTarget();
        int duration = Math.max(1, weapon.getBalance().attackTicks()
                + (weapon.isSpear() ? 2 : 0)
                + (weapon.getAttackType() == ArclightSummonedWeaponEntity.AttackType.SPIN ? 6 : 0));
        float progress = Mth.clamp((weapon.getStateTicks() + 1.0F) / duration, 0.0F, 1.0F);
        Vec3 previous = weapon.position();
        Vec3 next = attackPosition(weapon, targetPosition, progress);
        weapon.setPositionAndMovement(next, previous);
        weapon.damageAlongSweep(owner, previous, next);

        if (progress >= 1.0F) {
            weapon.setCombatState(ArclightSummonedWeaponState.RETURN);
        }
    }

    private static Vec3 attackPosition(ArclightSummonedWeaponEntity weapon, Vec3 target, float progress) {
        Vec3 origin = weapon.getAttackOrigin();
        Vec3 toTarget = target.subtract(origin);
        Vec3 direction = toTarget.lengthSqr() < 1.0E-6D ? weapon.getLookDirection() : toTarget.normalize();
        return switch (weapon.getAttackType()) {
            case STAB -> lerp(origin, target.add(direction.scale(weapon.isSpear() ? 1.6D : 1.0D)), smoothStep(progress));
            case VERTICAL -> {
                Vec3 end = target.add(direction.scale(0.75D)).add(0.0D, -0.8D, 0.0D);
                yield lerp(origin, end, smoothStep(progress));
            }
            case HORIZONTAL -> horizontalArc(weapon, origin, target, progress, Math.PI);
            case DASH -> lerp(origin, target.add(direction.scale(weapon.isSpear() ? 3.0D : 2.3D)), smoothStep(progress));
            case SPIN -> horizontalArc(weapon, origin, target, progress, Math.PI * 1.35D);
            default -> target;
        };
    }

    private static Vec3 horizontalArc(ArclightSummonedWeaponEntity weapon, Vec3 origin, Vec3 target,
                                      float progress, double arc) {
        Vec3 radial = origin.subtract(target).multiply(1.0D, 0.0D, 1.0D);
        if (radial.lengthSqr() < 1.0E-6D) {
            radial = new Vec3(1.0D, 0.0D, 0.0D);
        }
        double radius = Math.max(weapon.isSpear() ? 2.5D : 1.8D, radial.length());
        radial = radial.normalize().scale(radius);
        double sideSign = weapon.getFormationIndex() == 0 ? -1.0D : 1.0D;
        Vec3 rotated = rotateY(radial, arc * progress * sideSign);
        double y = Mth.lerp(progress, origin.y, target.y + 0.35D);
        return new Vec3(target.x + rotated.x, y, target.z + rotated.z);
    }

    private static void tickRecovery(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        Vec3 previous = weapon.position();
        Vec3 movement = weapon.getDeltaMovement().scale(0.78D);
        weapon.setPositionAndMovement(previous.add(movement), previous);
        if (weapon.getStateTicks() + 1 >= weapon.getBalance().returnTicks()
                || movement.lengthSqr() < 2.5E-4D) {
            int cooldown = weapon.getWeaponType() == ArclightSummonedWeaponType.SPEAR
                    ? weapon.getBalance().spearCooldownTicks()
                    : weapon.getBalance().swordCooldownTicks();
            weapon.setDeltaMovement(Vec3.ZERO);
            weapon.setCooldownTicks(cooldown);
            weapon.setAttackType(ArclightSummonedWeaponEntity.AttackType.NONE);
            weapon.setCombatState(ArclightSummonedWeaponState.COOLDOWN);
        }
    }

    private static void tickCooldown(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        LivingEntity target = validTarget(weapon, owner);
        if (target == null) {
            weapon.moveToward(weapon.getIdlePosition(owner, null), 0.1D, 0.35D);
        } else {
            weapon.setDeltaMovement(Vec3.ZERO);
        }
        if (weapon.getCooldownTicks() <= 0) {
            weapon.setCombatState(ArclightSummonedWeaponState.ORBIT);
        }
    }

    private static void abortAttack(ArclightSummonedWeaponEntity weapon) {
        weapon.setTarget(null);
        weapon.setAttackType(ArclightSummonedWeaponEntity.AttackType.NONE);
        weapon.setCombatState(ArclightSummonedWeaponState.RETURN);
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : direction.normalize();
    }

    private static Vec3 rotateY(Vec3 vector, double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        return new Vec3(vector.x * cos - vector.z * sin, vector.y,
                vector.x * sin + vector.z * cos);
    }

    private static Vec3 lerp(Vec3 start, Vec3 end, float progress) {
        return start.add(end.subtract(start).scale(progress));
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }
}