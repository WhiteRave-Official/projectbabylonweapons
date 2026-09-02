package com.rave.projectbabylonweapons.world.entity.summon;

public enum ArclightSummonedWeaponState {
    ORBIT,
    WINDUP,
    ATTACK,
    RETURN,
    COOLDOWN;

    public static ArclightSummonedWeaponState byId(int id) {
        ArclightSummonedWeaponState[] values = values();
        return id >= 0 && id < values.length ? values[id] : ORBIT;
    }
}