package com.rave.projectbabylonweapons.world.entity.summon;

public enum ArclightSummonedWeaponType {
    SWORD(ArclightSummonCollider.SWORD),
    SPEAR(ArclightSummonCollider.SPEAR);

    private final ArclightSummonCollider collider;

    ArclightSummonedWeaponType(ArclightSummonCollider collider) {
        this.collider = collider;
    }

    public ArclightSummonCollider collider() {
        return this.collider;
    }

    public static ArclightSummonedWeaponType byId(int id) {
        return id == SPEAR.ordinal() ? SPEAR : SWORD;
    }
}