package com.rave.projectbabylonweapons.event;

import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.skill.SkillContainer;

public final class SickleThrowParametersEvent extends Event {
    private final SkillContainer container;
    private double maxDistance;
    private int maxProjectiles;
    private float damage = com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowSkill.PROJECTILE_DAMAGE;

    public SickleThrowParametersEvent(SkillContainer container, double maxDistance, int maxProjectiles) {
        this.container = container;
        this.maxDistance = maxDistance;
        this.maxProjectiles = maxProjectiles;
    }

    public SkillContainer container() { return container; }
    public double maxDistance() { return maxDistance; }
    public int maxProjectiles() { return maxProjectiles; }
    public float damage() { return damage; }
    public void setMaxDistance(double value) { if (Double.isFinite(value)) maxDistance = Math.max(1, value); }
    public void setMaxProjectiles(int value) { maxProjectiles = Math.max(1, value); }
    public void setDamage(float value) { if (Float.isFinite(value)) damage = Math.max(0, value); }
}
