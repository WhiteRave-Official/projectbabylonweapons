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

public class HolySpellProjectileEntity extends BasicSpellProjectileEntity {
    private static final RawAnimation HOLY_LOOP_ANIMATION = RawAnimation.begin().thenLoop("animation.holy_spell_projectile.idle");
    private static final int IMPACT_END_ROD_COUNT = 26;

    public HolySpellProjectileEntity(EntityType<? extends HolySpellProjectileEntity> type, Level level) {
        super(type, level);
    }

    public HolySpellProjectileEntity(Level level) {
        this(PBModEntities.HOLY_SPELL_PROJECTILE.get(), level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "loop_controller", 0, state -> {
            return PlayState.CONTINUE;
        }));
    }

    @Override
    protected void spawnClientParticles() {
        PhotonWeaponEffectHelper.spawnHolyProjectileFlight(this, this.getDeltaMovement());
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.level().isClientSide && result.getType() != HitResult.Type.MISS) {
            PhotonWeaponEffectHelper.spawnHolyProjectileImpact(this, result.getLocation());
        }
        super.onHit(result);
    }

    @Override
    protected void spawnImpactParticles(Vec3 hitPos) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ParticleTypes.END_ROD,
                hitPos.x, hitPos.y, hitPos.z,
                IMPACT_END_ROD_COUNT,
                0.32D, 0.32D, 0.32D,
                0.02D);
    }
}
