package com.rave.projectbabylonweapons.world.entity.projectile;

import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.init.PBModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;

public class EnderSpellProjectileEntity extends BasicSpellProjectileEntity {
    private static final int IMPACT_PORTAL_COUNT = 28;
    private static final int IMPACT_DRAGON_BREATH_COUNT = 18;

    public EnderSpellProjectileEntity(EntityType<? extends EnderSpellProjectileEntity> type, Level level) {
        super(type, level);
    }

    public EnderSpellProjectileEntity(Level level) {
        this(PBModEntities.ENDER_SPELL_PROJECTILE.get(), level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    protected void spawnClientParticles() {
        PhotonWeaponEffectHelper.spawnEnderProjectileFlight(this, this.getDeltaMovement());
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.level().isClientSide && result.getType() != HitResult.Type.MISS) {
            PhotonWeaponEffectHelper.spawnEnderProjectileImpact(this, result.getLocation());
        }
        super.onHit(result);
    }

    @Override
    protected void spawnImpactParticles(Vec3 hitPos) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ParticleTypes.PORTAL,
                hitPos.x, hitPos.y, hitPos.z,
                IMPACT_PORTAL_COUNT,
                0.35D, 0.35D, 0.35D,
                0.05D);
        serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                hitPos.x, hitPos.y, hitPos.z,
                IMPACT_DRAGON_BREATH_COUNT,
                0.22D, 0.22D, 0.22D,
                0.02D);
    }
}