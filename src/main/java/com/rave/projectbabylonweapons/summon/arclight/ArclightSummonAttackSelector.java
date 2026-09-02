package com.rave.projectbabylonweapons.summon.arclight;

import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponType;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public final class ArclightSummonAttackSelector {
    private ArclightSummonAttackSelector() {
    }

    public static ArclightSummonedWeaponEntity.AttackType select(
            ArclightSummonedWeaponEntity weapon, LivingEntity owner, LivingEntity target) {
        double distance = weapon.distanceTo(target);
        int nearbyEnemies = ArclightSummonTargeting.countNearbyEnemies(owner, target, 2.5D);
        List<ArclightSummonedWeaponEntity.AttackType> candidates = new ArrayList<>();

        if (weapon.getWeaponType() == ArclightSummonedWeaponType.SPEAR && nearbyEnemies >= 2) {
            candidates.add(ArclightSummonedWeaponEntity.AttackType.SPIN);
        }
        if (nearbyEnemies >= 2) {
            candidates.add(ArclightSummonedWeaponEntity.AttackType.HORIZONTAL);
        }
        if (distance < 3.0D) {
            candidates.add(ArclightSummonedWeaponEntity.AttackType.HORIZONTAL);
            candidates.add(ArclightSummonedWeaponEntity.AttackType.VERTICAL);
        } else if (distance < 5.5D) {
            candidates.add(ArclightSummonedWeaponEntity.AttackType.STAB);
            candidates.add(ArclightSummonedWeaponEntity.AttackType.VERTICAL);
        } else {
            candidates.add(ArclightSummonedWeaponEntity.AttackType.DASH);
        }

        ArclightSummonedWeaponEntity.AttackType last = weapon.getLastAttack();
        candidates.removeIf(type -> type == last && candidates.size() > 1);
        return candidates.get(weapon.nextRandomInt(candidates.size()));
    }
}