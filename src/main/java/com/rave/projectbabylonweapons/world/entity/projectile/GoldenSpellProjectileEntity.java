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
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class GoldenSpellProjectileEntity extends BasicSpellProjectileEntity {
    private static final RawAnimation GOLDEN_LOOP_ANIMATION = RawAnimation.begin().thenLoop("animation.golden_spell_projectile.idle");
    private static final int IMPACT_GLOW_COUNT = 22;

    public GoldenSpellProjectileEntity(EntityType<? extends GoldenSpellProjectileEntity> type, Level level) {
        super(type, level);
    }

    public GoldenSpellProjectileEntity(Level level) {
        this(PBModEntities.GOLDEN_SPELL_PROJECTILE.get(), level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "loop_controller", 0, state -> {
            return PlayState.CONTINUE;
        }));
    }

    @Override
    protected void spawnClientParticles() {
        PhotonWeaponEffectHelper.spawnGoldenProjectileFlight(this, this.getDeltaMovement());
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.level().isClientSide && result.getType() != HitResult.Type.MISS) {
            PhotonWeaponEffectHelper.spawnGoldenProjectileImpact(this, result.getLocation());
        }
        super.onHit(result);
    }

    @Override
    protected void spawnImpactParticles(Vec3 hitPos) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ParticleTypes.WAX_ON,
                hitPos.x, hitPos.y, hitPos.z,
                IMPACT_GLOW_COUNT,
                0.3D, 0.3D, 0.3D,
                0.04D);
    }
}