package com.rave.projectbabylonweapons.event;

import com.rave.projectbabylonweapons.item.shield.BastionShield;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public final class ShieldDisableAttemptEvent extends Event {
    private final Player defender;
    private final LivingEntity attacker;
    private final ItemStack shield;
    private boolean allowed;
    private int duration = 100;
    public ShieldDisableAttemptEvent(Player defender, LivingEntity attacker, ItemStack shield) {
        this.defender = defender; this.attacker = attacker; this.shield = shield;
        allowed = !(shield.getItem() instanceof BastionShield);
    }
    public Player defender() { return defender; }
    public LivingEntity attacker() { return attacker; }
    public ItemStack shield() { return shield; }
    public boolean allowed() { return allowed; }
    public void setAllowed(boolean value) { allowed = value; }
    public int duration() { return duration; }
    public void addDuration(int ticks) { duration = Math.max(1, duration + ticks); }
}
