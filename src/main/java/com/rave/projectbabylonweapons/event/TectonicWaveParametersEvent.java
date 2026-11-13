package com.rave.projectbabylonweapons.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public final class TectonicWaveParametersEvent extends Event {
    private final LivingEntity owner;
    private float damageMultiplier = 1;
    private int rangePerTarget;
    private int maxExtensions;

    public TectonicWaveParametersEvent(LivingEntity owner) { this.owner = owner; }
    public LivingEntity owner() { return owner; }
    public float damageMultiplier() { return damageMultiplier; }
    public int rangePerTarget() { return rangePerTarget; }
    public int maxExtensions() { return maxExtensions; }
    public void multiplyDamage(float multiplier) {
        if (Float.isFinite(multiplier) && multiplier > 0) damageMultiplier *= multiplier;
    }
    public void extendOnHit(int blocks, int limit) {
        rangePerTarget = Math.max(0, Math.min(16, blocks));
        maxExtensions = Math.max(0, Math.min(16, limit));
    }
    public static TectonicWaveParametersEvent resolve(LivingEntity owner) {
        var event = new TectonicWaveParametersEvent(owner);
        if (!owner.level().isClientSide()) MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}
