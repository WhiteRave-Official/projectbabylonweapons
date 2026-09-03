package com.rave.projectbabylonweapons.summon.arclight;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponState;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import com.rave.projectbabylonweapons.summon.arclight.epicfight.ArclightSummonedWeaponPatch;

public final class ArclightSummonAttackController {
    private static final double TELEPORT_DISTANCE_SQR = 32.0D * 32.0D;

    private ArclightSummonAttackController() {
    }

    public static void tick(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        if (weapon.distanceToSqr(owner) > TELEPORT_DISTANCE_SQR) {
            ProjectBabylonWeapons.LOGGER.info("[ArclightWeaponDebug] teleport_to_owner side=server id={} distanceSqr={} position={} ownerPosition={}",
                    weapon.getId(), weapon.distanceToSqr(owner), weapon.position(), owner.position());
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
            case ROAMING -> tickRoaming(weapon, owner);
        }
        weapon.incrementStateTicks();
    }

    private static void tickOrbit(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        LivingEntity target = validTarget(weapon, owner);
        if (target == null) {
            followOwnerFreely(weapon, owner);
        } else {
            coast(weapon);
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
        ProjectBabylonWeapons.LOGGER.info("[ArclightWeaponDebug] attack_selected side=server id={} type={} attack={} targetId={} origin={} lockedTarget={}",
                weapon.getId(), weapon.getWeaponType(), attack, target.getId(),
                weapon.getAttackOrigin(), weapon.getAttackTarget());
    }

    private static void tickWindup(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        Vec3 center = weapon.getAttackTarget();
        Vec3 forward = horizontalDirection(weapon.getAttackOrigin(), center);
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        double lengthScale = weapon.isSpear() ? 1.25D : 1.0D;
        Vec3 windup = switch (weapon.getAttackType()) {
            case STAB -> center.subtract(forward.scale(2.5D * lengthScale)).add(0.0D, 0.15D, 0.0D);
            case VERTICAL -> center.subtract(forward.scale(0.45D))
                    .add(0.0D, weapon.isSpear() ? 3.0D : 2.5D, 0.0D);
            case HORIZONTAL -> center.add(side.scale(1.55D * lengthScale))
                    .add(0.0D, 0.45D, 0.0D);
            case DASH -> center.subtract(forward.scale(3.8D * lengthScale)).add(0.0D, 0.25D, 0.0D);
            case SPIN -> center.add(side.scale(2.15D)).add(0.0D, 0.5D, 0.0D);
            default -> weapon.getIdlePosition(owner, null);
        };

        weapon.moveToward(windup, 0.42D, weapon.isSpear() ? 1.25D : 1.05D, center);
        int windupDuration = weapon.getBalance().windupTicks();
        if (weapon.getStateTicks() + 1 >= windupDuration) {
            weapon.setAttackOrigin(weapon.position());
            weapon.setDeltaMovement(Vec3.ZERO);
            weapon.setCombatState(ArclightSummonedWeaponState.ATTACK);
            ArclightSummonedWeaponPatch patch = EpicFightCapabilities.getEntityPatch(
                    weapon, ArclightSummonedWeaponPatch.class);
            if (patch != null) {
                patch.playAttack(weapon.getAttackType());
            } else {
                weapon.finishAnimatedAttack();
            }
        }
    }

    private static void tickAttack(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        weapon.setDeltaMovement(Vec3.ZERO);
        // ON_END is authoritative; the limit only protects against a malformed animation asset.
        if (weapon.getStateTicks() > 80) {
            weapon.finishAnimatedAttack();
        }
    }

    private static void tickRecovery(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        moveRoaming(weapon, owner);

        if (weapon.getStateTicks() + 1 >= weapon.getBalance().returnTicks()) {
            int cooldown = weapon.getWeaponType() == ArclightSummonedWeaponType.SPEAR
                    ? weapon.getBalance().spearCooldownTicks()
                    : weapon.getBalance().swordCooldownTicks();
            weapon.setCooldownTicks(cooldown);
            weapon.setAttackType(ArclightSummonedWeaponEntity.AttackType.NONE);
            weapon.setCombatState(ArclightSummonedWeaponState.ROAMING);
        }
    }
    private static void tickCooldown(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        // Preserve old saved entities that may still load with the legacy cooldown state.
        weapon.setCombatState(ArclightSummonedWeaponState.ROAMING);
        tickRoaming(weapon, owner);
    }

    private static void tickRoaming(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        moveRoaming(weapon, owner);
        if (weapon.getCooldownTicks() <= 0) {
            weapon.setCombatState(ArclightSummonedWeaponState.ORBIT);
        }
    }

    private static void moveRoaming(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        LivingEntity target = validTarget(weapon, owner);
        if (target == null && weapon.tickCount >= weapon.getNextTargetSearchTick()) {
            target = ArclightSummonTargeting.findTarget(owner, weapon.getBalance().targetRadius());
            weapon.setTarget(target);
            weapon.setNextTargetSearchTick(weapon.tickCount + weapon.getBalance().targetSearchInterval());
        }

        Vec3 anchor = target == null
                ? owner.position().add(0.0D, owner.getBbHeight() * 0.7D, 0.0D)
                : target.getBoundingBox().getCenter();
        if (weapon.needsRoamingDestination(anchor)) {
            double angle = weapon.nextRandomDouble() * Math.PI * 2.0D;
            double radius = target == null
                    ? 1.6D + weapon.nextRandomDouble() * 1.4D
                    : 1.8D + weapon.nextRandomDouble() * 1.8D;
            double slotOffset = weapon.isSpear() ? 0.0D : weapon.getFormationIndex() * Math.PI;
            double height = target == null
                    ? -0.25D + weapon.nextRandomDouble() * 1.2D
                    : 0.15D + weapon.nextRandomDouble() * Math.max(1.0D, target.getBbHeight());
            weapon.setRoamingDestination(anchor.add(
                    Math.cos(angle + slotOffset) * radius,
                    height,
                    Math.sin(angle + slotOffset) * radius
            ), 60 + weapon.nextRandomInt(41));
        }

        weapon.moveToward(weapon.getRoamingDestination(), 0.018D,
                weapon.isSpear() ? 0.095D : 0.08D);
        weapon.levelHoverPitch();
        weapon.tickRoamingDestination();
    }
    private static void followOwnerFreely(ArclightSummonedWeaponEntity weapon, LivingEntity owner) {
        Vec3 anchor = owner.position().add(0.0D, owner.getBbHeight() * 0.7D, 0.0D);
        double followRadius = weapon.isSpear() ? 4.0D : 3.25D;
        if (weapon.position().distanceToSqr(anchor) > followRadius * followRadius) {
            weapon.moveToward(anchor, 0.055D, weapon.isSpear() ? 0.34D : 0.28D);
        } else {
            coast(weapon);
        }
    }

    private static void coast(ArclightSummonedWeaponEntity weapon) {
        weapon.setDeltaMovement(weapon.getDeltaMovement().scale(0.86D));
        weapon.levelHoverPitch();
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : direction.normalize();
    }

}
