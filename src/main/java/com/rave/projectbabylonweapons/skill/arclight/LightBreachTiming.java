package com.rave.projectbabylonweapons.skill.arclight;


public final class LightBreachTiming {
    private LightBreachTiming() {}

    public static boolean isPulse(int age, int fadeIn, int active, int interval) {
        int damageAge = age - fadeIn;
        return damageAge >= 0 && damageAge < active && damageAge % interval == 0;
    }

    public static float opacity(int age, int fadeIn, int active, int fadeOut) {
        if (age < 0 || age >= fadeIn + active + fadeOut) return 0.0F;
        if (age < fadeIn) return (float) age / Math.max(1, fadeIn);
        if (age < fadeIn + active) return 1.0F;
        return (float) (fadeIn + active + fadeOut - age) / Math.max(1, fadeOut);
    }

    public static boolean intersectsColumn(double x, double y, double z, double radius, double height,
                                            double minX, double minY, double minZ,
                                            double maxX, double maxY, double maxZ) {
        if (maxY <= y || minY >= y + height) return false;
        double dx = Math.max(minX, Math.min(x, maxX)) - x;
        double dz = Math.max(minZ, Math.min(z, maxZ)) - z;
        return dx * dx + dz * dz <= radius * radius;
    }
}
