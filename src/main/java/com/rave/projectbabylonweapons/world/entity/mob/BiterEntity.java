package com.rave.projectbabylonweapons.world.entity.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public final class BiterEntity extends NatureTurretEntity {
    public BiterEntity(EntityType<? extends BiterEntity> type, Level level) { super(type, level, "biter", false); }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, NatureTurretRules.BITER_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, NatureTurretRules.BITER_DAMAGE).add(Attributes.ATTACK_SPEED, 20.0 / NatureTurretRules.BITER_INTERVAL)
                .add(Attributes.FOLLOW_RANGE, NatureTurretRules.BITER_RANGE).add(Attributes.MOVEMENT_SPEED, 0).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }
    @Override protected void attack(LivingEntity target) {
        NatureTurretCombat.hurt(this, getOwner(), getOwnerUUID() != null, target, (float)getAttributeValue(Attributes.ATTACK_DAMAGE));
    }
}
