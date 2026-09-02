package com.rave.projectbabylonweapons.world.entity.summon;

public record ArclightSummonCollider(double halfWidth, double halfHeight, double halfLength) {
    public static final ArclightSummonCollider SWORD = new ArclightSummonCollider(0.4D, 0.4D, 1.0D);
    public static final ArclightSummonCollider SPEAR = new ArclightSummonCollider(0.4D, 0.4D, 2.0D);
}