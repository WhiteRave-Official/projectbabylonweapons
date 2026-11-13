package com.rave.projectbabylonweapons.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public final class HarvestHealingEvent extends Event {
    private final LivingEntity owner;
    private float ratio;

    public HarvestHealingEvent(LivingEntity owner, float ratio) {
        this.owner = owner;
        this.ratio = ratio;
    }

    public LivingEntity owner() { return owner; }
    public float ratio() { return ratio; }
    public void setRatio(float value) { if (Float.isFinite(value)) ratio = Math.max(0, value); }
}
