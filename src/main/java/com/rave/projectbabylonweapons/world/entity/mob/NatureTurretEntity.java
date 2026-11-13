package com.rave.projectbabylonweapons.world.entity.mob;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.Optional;
import java.util.UUID;

public abstract class NatureTurretEntity extends Mob implements GeoEntity, IMagicSummon, OwnableEntity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(NatureTurretEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Long> BORN_AT = SynchedEntityData.defineId(NatureTurretEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> ATTACK_AT = SynchedEntityData.defineId(NatureTurretEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> ATTACK_TICKS = SynchedEntityData.defineId(NatureTurretEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation SPAWN = RawAnimation.begin().thenPlayAndHold("spawn");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlayAndHold("attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ResourceLocation model, texture, animations;
    private final boolean thorn;
    private UUID attackTarget;
    private boolean delivered = true;
    private long nextAttack;
    private long lastAnimationAttack = -1;
    private String lastAnimation = "";

    protected NatureTurretEntity(EntityType<? extends NatureTurretEntity> type, Level level, String asset, boolean thorn) {
        super(type, level);
        this.thorn = thorn;
        model = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/" + asset + ".geo.json");
        texture = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/" + asset + ".png");
        animations = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/" + asset + ".animation.json");
        entityData.set(BORN_AT, level.getGameTime());
        setPersistenceRequired();
    }
    public ResourceLocation getModelResource() { return model; }
    public ResourceLocation getTextureResource() { return texture; }
    public ResourceLocation getAnimationResource() { return animations; }
    @Override public UUID getOwnerUUID() {
        UUID owner = entityData.get(OWNER).orElse(null);
        Entity tracked = owner == null ? SummonManager.getOwner(this) : null;
        return tracked == null ? owner : tracked.getUUID();
    }
    @Override public LivingEntity getOwner() {
        UUID id = getOwnerUUID();
        if (id == null) return null;
        Entity owner = level() instanceof ServerLevel server ? server.getEntity(id) : level().getPlayerByUUID(id);
        return owner instanceof LivingEntity living ? living : null;
    }
    @Override public Entity getSummoner() { return getOwner(); }
    public void setSummoner(LivingEntity owner) {
        if (owner == this) return;
        entityData.set(OWNER, Optional.ofNullable(owner == null ? null : owner.getUUID()));
        if (!level().isClientSide()) {
            if (owner == null) SummonManager.removeSummon(this);
            else SummonManager.setOwner(this, owner);
        }
        setTarget(null);
    }
    @Override public boolean isAlliedTo(Entity other) {
        LivingEntity owner = getOwner();
        return super.isAlliedTo(other) || owner != null && (other == owner || DamageSources.isFriendlyFireBetween(owner, other));
    }
    public boolean isEnemy(LivingEntity target) {
        return target != null && NatureTurretCombat.enemy(this, getOwner(), getOwnerUUID() != null, target);
    }
    protected abstract void attack(LivingEntity target);
    protected boolean inRange(LivingEntity target) {
        double range = getAttributeValue(Attributes.FOLLOW_RANGE);
        return isEnemy(target) && distanceToSqr(target) <= range * range && getSensing().hasLineOfSight(target);
    }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();
        long now = level().getGameTime();
        if (!isAlive() || now - entityData.get(BORN_AT) < NatureTurretRules.SPAWN_TICKS) return;
        if (!delivered) {
            long start = entityData.get(ATTACK_AT);
            int duration = entityData.get(ATTACK_TICKS);
            if (now >= start + duration) delivered = true;
            else if (now >= start + NatureTurretRules.contact(duration, thorn)) {
                delivered = true;
                Entity pending = attackTarget == null ? null : ((ServerLevel)level()).getEntity(attackTarget);
                if (pending instanceof LivingEntity living && inRange(living)) attack(living);
            }
        }
        LivingEntity target = getTarget();
        if (target != null && !inRange(target)) { setTarget(null); target = null; }
        if (tickCount % 10 == 0) {
            double range = getAttributeValue(Attributes.FOLLOW_RANGE), nearest = range * range;
            LivingEntity selected = target;
            if (target != null) nearest = distanceToSqr(target);
            for (var candidate : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(range))) {
                double distance = distanceToSqr(candidate);
                if (distance <= nearest && inRange(candidate)) { nearest = distance; selected = candidate; }
            }
            setTarget(selected);
            target = getTarget();
        }
        if (target == null || !inRange(target)) return;
        Vec3 direction = target.getBoundingBox().getCenter().subtract(position());
        float yaw = (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
        setYRot(yaw); setYBodyRot(yaw); setYHeadRot(yaw);
        if (now < nextAttack) return;
        int duration = NatureTurretRules.interval(getAttributeValue(Attributes.ATTACK_SPEED), thorn ? NatureTurretRules.THORN_INTERVAL : NatureTurretRules.BITER_INTERVAL);
        entityData.set(ATTACK_AT, now); entityData.set(ATTACK_TICKS, duration);
        attackTarget = target.getUUID();
        delivered = false;
        nextAttack = now + duration;
    }
    @Override public void travel(Vec3 input) {
        setDeltaMovement(0, getDeltaMovement().y, 0);
        super.travel(Vec3.ZERO);
        setDeltaMovement(0, getDeltaMovement().y, 0);
    }
    @Override public boolean isPushable() { return false; }
    @Override public void push(double x, double y, double z) { }
    @Override public void knockback(double strength, double x, double z) { }
    @Override public boolean hurt(DamageSource source, float damage) {
        return !shouldIgnoreDamage(source) && super.hurt(source, damage);
    }
    @Override protected void tickDeath() {
        deathTime++;
        if (!level().isClientSide() && deathTime >= (thorn ? NatureTurretRules.THORN_DEATH_TICKS : NatureTurretRules.BITER_DEATH_TICKS)) remove(RemovalReason.KILLED);
    }
    @Override public void onUnSummon() { if (!level().isClientSide() && isAlive()) hurt(damageSources().genericKill(), Float.MAX_VALUE); }
    @Override public void onRemovedFromWorld() {
        if (!level().isClientSide() && getRemovalReason() != null && getRemovalReason().shouldDestroy()) {
            SummonManager.removeSummon(this);
            SummonManager.stopTrackingExpiration(this);
        }
        super.onRemovedFromWorld();
    }
    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER, Optional.empty()); entityData.define(BORN_AT, 0L);
        entityData.define(ATTACK_AT, -1L); entityData.define(ATTACK_TICKS, NatureTurretRules.BITER_INTERVAL);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getOwnerUUID() != null) tag.putUUID("Owner", getOwnerUUID());
        tag.putLong("PlantBornAt", entityData.get(BORN_AT));
        tag.putLong("PlantAttackAt", entityData.get(ATTACK_AT));
        tag.putInt("PlantAttackTicks", entityData.get(ATTACK_TICKS));
        tag.putLong("PlantNextAttack", nextAttack);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(OWNER, tag.hasUUID("Owner") ? Optional.of(tag.getUUID("Owner")) : Optional.empty());
        if (tag.contains("PlantBornAt")) entityData.set(BORN_AT, tag.getLong("PlantBornAt"));
        entityData.set(ATTACK_AT, tag.contains("PlantAttackAt") ? tag.getLong("PlantAttackAt") : -1L);
        entityData.set(ATTACK_TICKS, Math.max(2, tag.getInt("PlantAttackTicks")));
        nextAttack = tag.getLong("PlantNextAttack");
        delivered = true;
    }
    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().inflate(1, 1.5, 1); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 0, state -> {
            long attackAt = entityData.get(ATTACK_AT), now = level().getGameTime();
            int duration = entityData.get(ATTACK_TICKS);
            String mode = isDeadOrDying() ? "death" : now - entityData.get(BORN_AT) < NatureTurretRules.SPAWN_TICKS ? "spawn"
                    : attackAt >= 0 && now - attackAt < duration ? "attack" : "idle";
            if (!mode.equals(lastAnimation) || mode.equals("attack") && attackAt != lastAnimationAttack) {
                lastAnimation = mode; lastAnimationAttack = attackAt;
                state.getController().forceAnimationReset();
            }
            state.getController().setAnimationSpeed(mode.equals("attack") ? NatureTurretRules.animationSpeed(duration, thorn) : 1);
            state.getController().setAnimation(mode.equals("death") ? DEATH : mode.equals("spawn") ? SPAWN : mode.equals("attack") ? ATTACK : IDLE);
            return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
