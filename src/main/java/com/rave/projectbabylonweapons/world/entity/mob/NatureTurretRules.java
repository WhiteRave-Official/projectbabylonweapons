package com.rave.projectbabylonweapons.world.entity.mob;

public final class NatureTurretRules {
    public static final int SPAWN_TICKS = 50, BITER_INTERVAL = 30, THORN_INTERVAL = 24;
    public static final int BITER_DEATH_TICKS = 40, THORN_DEATH_TICKS = 65, POISON_TICKS = 160;
    public static final float BITER_DAMAGE = 5, BITER_HEALTH = 15, THORN_DAMAGE = 2, THORN_HEALTH = 10, POISON_CHANCE = .15F;
    public static final double BITER_RANGE = 3, THORN_RANGE = 10, PROJECTILE_SPEED = 1.25;
    private NatureTurretRules() { }
    public static int interval(double speed, int fallback) {
        return Double.isFinite(speed) && speed > 0 ? Math.max(2, (int)Math.round(20 / speed)) : fallback;
    }
    public static int contact(int duration, boolean thorn) {
        return Math.max(1, Math.min(duration - 1, (int)Math.ceil(duration * (thorn ? 1.0833 / 2.375 : 1.25 / 2.5833))));
    }
    public static double animationSpeed(int duration, boolean thorn) { return (thorn ? 47.5 : 51.666) / Math.max(1, duration); }
}
