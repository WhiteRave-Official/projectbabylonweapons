package com.rave.projectbabylonweapons.world.entity.mob;

import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.world.entity.projectile.ThornProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ThornEntity extends NatureTurretEntity {
    public ThornEntity(EntityType<? extends ThornEntity> type, Level level) { super(type, level, "thorn", true); }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, NatureTurretRules.THORN_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, NatureTurretRules.THORN_DAMAGE).add(Attributes.ATTACK_SPEED, 20.0 / NatureTurretRules.THORN_INTERVAL)
                .add(Attributes.FOLLOW_RANGE, NatureTurretRules.THORN_RANGE).add(Attributes.MOVEMENT_SPEED, 0).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }
    @Override protected void attack(LivingEntity target) {
        Vec3 from = getEyePosition().add(getLookAngle().scale(.55));
        var projectile = new ThornProjectileEntity(PBModEntities.THORN_PROJECTILE.get(), level());
        projectile.configure(this, from, (float)getAttributeValue(Attributes.ATTACK_DAMAGE));
        projectile.shoot(target.getBoundingBox().getCenter().subtract(from).normalize());
        level().addFreshEntity(projectile);
    }
}
