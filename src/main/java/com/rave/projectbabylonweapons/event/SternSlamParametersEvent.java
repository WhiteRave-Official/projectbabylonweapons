package com.rave.projectbabylonweapons.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public final class SternSlamParametersEvent extends Event {
    private final LivingEntity owner;
    private double radiusBonus;
    private int durationBonus;

    public SternSlamParametersEvent(LivingEntity owner) { this.owner = owner; }
    public LivingEntity owner() { return owner; }
    public void addRadius(double amount) { radiusBonus += amount; }
    public void addDuration(int ticks) { durationBonus += ticks; }
    private static SternSlamParametersEvent resolve(LivingEntity owner) {
        var event = new SternSlamParametersEvent(owner);
        if (!owner.level().isClientSide()) MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
    public static double radius(LivingEntity owner, double base) { return base + resolve(owner).radiusBonus; }
    public static int duration(LivingEntity owner, int base) { return base < 0 ? base : base + resolve(owner).durationBonus; }
}
