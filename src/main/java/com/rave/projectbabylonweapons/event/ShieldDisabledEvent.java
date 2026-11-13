package com.rave.projectbabylonweapons.event;

import net.minecraftforge.eventbus.api.Event;

public final class ShieldDisabledEvent extends Event {
    private final ShieldDisableAttemptEvent attempt;
    public ShieldDisabledEvent(ShieldDisableAttemptEvent attempt) { this.attempt = attempt; }
    public ShieldDisableAttemptEvent attempt() { return attempt; }
}
