package com.rave.projectbabylonweapons.world.entity.effect;

public final class IceSpikeAnimation {
    public static final int RISE_TIME = GlacierIceSpikeEntity.RISE_TIME;
    public static final int REST_TIME = GlacierIceSpikeEntity.REST_TIME;
    public static final int LOWER_TIME = GlacierIceSpikeEntity.LOWER_TIME;
    public static final int LIFETIME = RISE_TIME + REST_TIME + LOWER_TIME;
    public static final int MAX_WAIT_TIME = 200;
    public static final float MAX_TILT = 30;
    private IceSpikeAnimation() { }

    public static float spawnScale(int variant, float random) {
        float base = variant == 2 ? 1.5F : variant == 1 ? 1 : .6F;
        float unit = Float.isFinite(random) ? Math.max(0, Math.min(1, random)) : 0;
        return base + .1F + .2F * unit;
    }

    public static float extension(float age) {
        return extension(age, REST_TIME);
    }

    public static int lifetime(int restTime) { return RISE_TIME + Math.max(0, restTime) + LOWER_TIME; }

    public static float extension(float age, int restTime) {
        int rest = Math.max(0, restTime);
        if (!Float.isFinite(age) || age <= 0 || age >= lifetime(rest)) return 0;
        if (age < RISE_TIME) return age / RISE_TIME;
        if (age <= RISE_TIME + rest) return 1;
        return Math.max(0, 1 - (age - RISE_TIME - rest) / LOWER_TIME);
    }
}
