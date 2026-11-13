package com.rave.projectbabylonweapons.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public final class HarvestHealedEvent extends Event {
    private final LivingEntity owner;
    private final float amount;

    public HarvestHealedEvent(LivingEntity owner, float amount) {
        this.owner = owner;
        this.amount = amount;
    }

    public LivingEntity owner() { return owner; }
    public float amount() { return amount; }
}
