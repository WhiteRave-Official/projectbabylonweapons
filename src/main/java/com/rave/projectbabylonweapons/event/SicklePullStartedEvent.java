package com.rave.projectbabylonweapons.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public final class SicklePullStartedEvent extends Event {
    private final LivingEntity owner;
    private final LivingEntity target;
    private final boolean movingOwner;

    public SicklePullStartedEvent(LivingEntity owner, LivingEntity target, boolean movingOwner) {
        this.owner = owner;
        this.target = target;
        this.movingOwner = movingOwner;
    }

    public LivingEntity owner() { return owner; }
    public LivingEntity target() { return target; }
    public boolean movingOwner() { return movingOwner; }
}
