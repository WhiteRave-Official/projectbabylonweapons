package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.item.MagicProjectileStaffWeapon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public final class StaffProjectilePatternHelper {
    private static final double SIDE_BY_SIDE_OFFSET = 0.35D;
    private static final double ARROW_SIDE_OFFSET = 0.30D;
    private static final double ARROW_ANGLE_DEGREES = 18.0D;

    private StaffProjectilePatternHelper() {
    }

    public static void fireForward(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon) {
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon);
    }

    public static void fireForwardBackward(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon) {
        Vec3 forward = forward(playerPatch);
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, forward, weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, forward.scale(-1.0D), weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
    }

    public static void firePlus(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon) {
        Vec3 forward = forward(playerPatch);
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, forward, weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, 180.0D), weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, 90.0D), weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, -90.0D), weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
    }

    public static void fireCross(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon) {
        Vec3 forward = forward(playerPatch);
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, 45.0D), weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, -45.0D), weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, 135.0D), weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, -135.0D), weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
    }

    public static void fireAdjacentDoubleForward(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon) {
        Vec3 forward = forward(playerPatch);
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, forward, weapon.getMagicProjectileSpawnForwardOffset(), -SIDE_BY_SIDE_OFFSET, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, forward, weapon.getMagicProjectileSpawnForwardOffset(), SIDE_BY_SIDE_OFFSET, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
    }

    public static void fireCircle(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon, int count) {
        Vec3 forward = forward(playerPatch);
        for (int i = 0; i < count; i++) {
            double angle = (360.0D / count) * i;
            StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, angle), weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        }
    }

    public static void fireArrowFormation(ServerPlayerPatch playerPatch, ItemStack weaponStack, MagicProjectileStaffWeapon weapon) {
        Vec3 forward = forward(playerPatch);
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, forward, weapon.getMagicProjectileSpawnForwardOffset(), 0.0D, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, ARROW_ANGLE_DEGREES), weapon.getMagicProjectileSpawnForwardOffset(), -ARROW_SIDE_OFFSET, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
        StaffProjectileAttackHelper.spawnProjectile(playerPatch, weaponStack, weapon, rotateY(forward, -ARROW_ANGLE_DEGREES), weapon.getMagicProjectileSpawnForwardOffset(), ARROW_SIDE_OFFSET, weapon.getMagicProjectileSpawnVerticalOffset(), weapon.getProjectileMagicDamageMultiplier());
    }

    private static Vec3 forward(ServerPlayerPatch playerPatch) {
        return playerPatch.getOriginal().getLookAngle().normalize();
    }

    private static Vec3 rotateY(Vec3 vector, double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = vector.x * cos - vector.z * sin;
        double z = vector.x * sin + vector.z * cos;
        return new Vec3(x, vector.y, z).normalize();
    }
}
