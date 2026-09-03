package com.rave.projectbabylonweapons.world.entity.summon;

public enum ArclightSummonedWeaponType {
    SWORD,
    SPEAR;

    public static ArclightSummonedWeaponType byId(int id) {
        ArclightSummonedWeaponType[] values = values();
        return id >= 0 && id < values.length ? values[id] : SWORD;
    }
}