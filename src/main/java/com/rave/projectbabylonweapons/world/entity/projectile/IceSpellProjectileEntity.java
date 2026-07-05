package com.rave.projectbabylonweapons.world.entity.projectile;

import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.init.PBModEntities;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class IceSpellProjectileEntity extends BasicSpellProjectileEntity {
    private static final RawAnimation ICE_LOOP_ANIMATION = RawAnimation.begin().thenLoop("animation.ice_spell_projectile.idle");
    private static final int IMPACT_SNOWFLAKE_COUNT = 28;
    private static final int IMPACT_SNOW_DUST_COUNT = 16;

    public IceSpellProjectileEntity(EntityType<? extends IceSpellProjectileEntity> type, Level level) {
        super(type, level);
    }

    public IceSpellProjectileEntity(Level level) {
        this(PBModEntities.ICE_SPELL_PROJECTILE.get(), level);
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.level().isClientSide && result.getType() != HitResult.Type.MISS) {
            PhotonWeaponEffectHelper.spawnIceProjectileImpact(this, result.getLocation());
        }
        super.onHit(result);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "loop_controller", 0, state -> {
            return PlayState.CONTINUE;
        }));
    }

    @Override
    protected void spawnClientParticles() {
        PhotonWeaponEffectHelper.spawnIceProjectileFlight(this, this.getDeltaMovement());
    }

    protected void spawnImpactParticles(Vec3 hitPos) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ParticleHelper.SNOWFLAKE,
                hitPos.x, hitPos.y, hitPos.z,
                IMPACT_SNOWFLAKE_COUNT,
                0.35D, 0.35D, 0.35D,
                0.05D);
        serverLevel.sendParticles(ParticleHelper.SNOW_DUST,
                hitPos.x, hitPos.y, hitPos.z,
                IMPACT_SNOW_DUST_COUNT,
                0.25D, 0.25D, 0.25D,
                0.03D);
    }
}

