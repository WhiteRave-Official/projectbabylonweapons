package com.rave.projectbabylonweapons.world.entity.projectile;

import com.rave.projectbabylonweapons.world.entity.mob.NatureTurretCombat;
import com.rave.projectbabylonweapons.world.entity.mob.NatureTurretEntity;
import com.rave.projectbabylonweapons.world.entity.mob.NatureTurretRules;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import io.redspace.ironsspellbooks.entity.spells.ShieldPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class ThornProjectileEntity extends AbstractMagicProjectile implements GeoEntity {
    private static final RawAnimation SPIN = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Set<UUID> struck = new HashSet<>();
    private UUID summoner;
    private double travelled;
    public ThornProjectileEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setDamage(NatureTurretRules.THORN_DAMAGE);
        setInfinitePiercing();
    }
    public void configure(NatureTurretEntity turret, Vec3 position, float damage) {
        setOwner(turret); setPos(position); setDamage(damage);
        summoner = turret.getOwnerUUID();
    }
    private LivingEntity summoner() {
        Entity owner = summoner == null || !(level() instanceof ServerLevel server) ? null : server.getEntity(summoner);
        return owner instanceof LivingEntity living ? living : null;
    }
    @Override public void handleHitDetection() {
        double remaining = Math.max(0, NatureTurretRules.THORN_RANGE - travelled);
        Vec3 velocity = getDeltaMovement();
        if (velocity.length() > remaining) setDeltaMovement(velocity.normalize().scale(remaining));
        if (level().isClientSide()) return;
        for (int i = 0; i < 16 && !isRemoved(); i++) {
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() == HitResult.Type.MISS || ForgeEventFactory.onProjectileImpact(this, hit)) return;
            onHit(hit);
            if (hit.getType() == HitResult.Type.BLOCK) return;
        }
    }
    @Override protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || struck.contains(entity.getUUID())) return false;
        return entity instanceof LivingEntity living ? NatureTurretCombat.enemy(this, summoner(), summoner != null, living)
                : entity instanceof AbstractShieldEntity || entity instanceof ShieldPart;
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if (level().isClientSide() || !struck.add(hit.getEntity().getUUID())) return;
        super.onHitEntity(hit);
        if (isRemoved() || !(hit.getEntity() instanceof LivingEntity target)) return;
        LivingEntity owner = summoner();
        if (NatureTurretCombat.hurt(this, owner, summoner != null, target, getDamage()) && random.nextFloat() < NatureTurretRules.POISON_CHANCE)
            target.addEffect(new MobEffectInstance(MobEffects.POISON, NatureTurretRules.POISON_TICKS), owner == null ? getOwner() : owner);
    }
    @Override protected void onHitBlock(BlockHitResult hit) { if (!level().isClientSide()) discard(); }
    @Override public void travel() {
        if (isRemoved()) return;
        Vec3 from = position();
        super.travel();
        travelled += position().distanceTo(from);
        if (!level().isClientSide() && (travelled >= NatureTurretRules.THORN_RANGE - .000001 || tickCount >= 80)) discard();
    }
    @Override public void tick() {
        if (!level().isClientSide() && !level().hasChunkAt(blockPosition())) { discard(); return; }
        super.tick();
    }
    @Override protected void handleEntityHoming() { }
    @Override protected void handleCursorHoming() { }
    @Override public float getSpeed() { return (float)NatureTurretRules.PROJECTILE_SPEED; }
    @Override public void trailParticles() { }
    @Override public void impactParticles(double x, double y, double z) { }
    @Override public Optional<Supplier<SoundEvent>> getImpactSound() { return Optional.empty(); }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (summoner != null) tag.putUUID("PlantSummoner", summoner);
        tag.putDouble("PlantTravelled", travelled);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        summoner = tag.hasUUID("PlantSummoner") ? tag.getUUID("PlantSummoner") : null;
        travelled = tag.getDouble("PlantTravelled");
    }
    @Override public void writeSpawnData(FriendlyByteBuf buffer) { super.writeSpawnData(buffer); buffer.writeDouble(travelled); }
    @Override public void readSpawnData(FriendlyByteBuf buffer) { super.readSpawnData(buffer); travelled = buffer.readDouble(); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "flight", 0, state -> {
            state.getController().setAnimation(SPIN);
            return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
