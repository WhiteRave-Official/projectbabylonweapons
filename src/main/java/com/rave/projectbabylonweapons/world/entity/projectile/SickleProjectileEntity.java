package com.rave.projectbabylonweapons.world.entity.projectile;

import javax.annotation.Nullable;

import com.rave.projectbabylonmaterials.combat.PreserveOriginalOwnerOnReflect;
import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.init.PBWSounds;
import com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowSkill;
import com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowChains;
import com.rave.projectbabylonweapons.event.SicklePullStartedEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import java.util.UUID;

import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

public class SickleProjectileEntity extends ThrowableItemProjectile implements PreserveOriginalOwnerOnReflect {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK =
            SynchedEntityData.defineId(SickleProjectileEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_TETHERED =
            SynchedEntityData.defineId(SickleProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(SickleProjectileEntity.class, EntityDataSerializers.INT);

    private static final double RETURN_BASE_SPEED = 1.4;
    private static final double RETURN_MAX_SPEED = 3.2;
    private static final double RETURN_DISTANCE_FACTOR = 0.35;
    private static final double RETURN_CATCH_DISTANCE = 1.0;

    @Nullable
    private LivingEntity tetherTarget;
    private int tetherTicksRemaining;
    private boolean returning;
    private int returningTicks;
    private double maxThrowDistance = SickleThrowSkill.MAX_CHAIN_DISTANCE;
    private double chainDistance = SickleThrowSkill.MAX_CHAIN_DISTANCE;
    private float projectileDamage = SickleThrowSkill.PROJECTILE_DAMAGE;

    public SickleProjectileEntity(EntityType<? extends SickleProjectileEntity> type, Level level) {
        super(type, level);
    }

    public SickleProjectileEntity(Level level, LivingEntity owner) {
        this(PBModEntities.SICKLE_PROJECTILE.get(), level);
        this.setOwner(owner);

    }

    public SickleProjectileEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(PBModEntities.SICKLE_PROJECTILE.get(), level);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ITEM_STACK, ItemStack.EMPTY);
        this.entityData.define(DATA_TETHERED, false);
        this.entityData.define(DATA_OWNER_ID, -1);
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        if (owner != null) {
            this.entityData.set(DATA_OWNER_ID, owner.getId());
        } else {
            this.entityData.set(DATA_OWNER_ID, -1);
        }
    }

    @Override
    @Nullable
    public Entity getOwner() {
        Entity owner = super.getOwner();
        if (owner != null) {
            return owner;
        }

        int ownerId = this.entityData.get(DATA_OWNER_ID);
        if (ownerId < 0) {
            return null;
        }

        return this.level().getEntity(ownerId);
    }

    @Override
    protected Item getDefaultItem() {
        ItemStack stack = this.entityData.get(DATA_ITEM_STACK);
        return stack.isEmpty() ? Items.AIR : stack.getItem();
    }

    @Override
    public ItemStack getItem() {
        ItemStack stack = this.entityData.get(DATA_ITEM_STACK);
        return stack.isEmpty() ? new ItemStack(Items.AIR) : stack;
    }

    public void setItemStack(ItemStack stack) {
        this.entityData.set(DATA_ITEM_STACK, stack.copy());
    }

    public void setMaxThrowDistance(double distance) {
        this.maxThrowDistance = Math.max(1, Math.min(distance, this.chainDistance));
    }

    public void setChainDistance(double distance) {
        if (Double.isFinite(distance)) this.chainDistance = Math.max(1, distance);
    }

    public double getMaxThrowDistance() {
        return this.maxThrowDistance;
    }

    public void setProjectileDamage(float damage) {
        if (Float.isFinite(damage)) this.projectileDamage = Math.max(0, damage);
    }

    public boolean isTethered() {
        return this.entityData.get(DATA_TETHERED);
    }

    public boolean isTetheredTo(UUID playerId) {
        return this.isTethered()
                && this.tetherTarget instanceof ServerPlayer targetPlayer
                && targetPlayer.getUUID().equals(playerId);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        if (this.entityData.get(DATA_OWNER_ID) != owner.getId()) {
            this.entityData.set(DATA_OWNER_ID, owner.getId());
        }


        if (this.isTethered()) {
            if (this.tetherTarget == null || !this.tetherTarget.isAlive()) {
                endTether();
                return;
            }

            this.tetherTicksRemaining--;
            if (this.tetherTicksRemaining <= 0) {
                endTether();
                return;
            }


            Vec3 targetPos = this.tetherTarget.position().add(0.0, this.tetherTarget.getBbHeight() * 0.6, 0.0);
            this.setPos(targetPos.x, targetPos.y, targetPos.z);
            this.setDeltaMovement(Vec3.ZERO);


            Vec3 ownerPos = owner.position();
            Vec3 toTarget = targetPos.subtract(ownerPos);
            double tetherDistance = toTarget.length();
            if (tetherDistance > this.chainDistance + 1) {
                endTether();
                return;
            }
            if (tetherDistance > this.chainDistance) {
                Vec3 clamped = ownerPos.add(toTarget.normalize().scale(this.chainDistance));
                Vec3 pull = clamped.subtract(this.tetherTarget.position());


                Vec3 pullXZ = new Vec3(pull.x, 0.0, pull.z);
                double pullLength = pullXZ.length();
                if (pullLength > 0.001) {
                    Vec3 pullVelocity = pullXZ.normalize().scale(Math.min(0.35, pullLength));
                    Vec3 current = this.tetherTarget.getDeltaMovement();
                    this.tetherTarget.setDeltaMovement(current.x + pullVelocity.x, current.y, current.z + pullVelocity.z);
                }


                double pullY = pull.y;
                if (Math.abs(pullY) > 0.001) {
                    Vec3 current = this.tetherTarget.getDeltaMovement();
                    double cappedY = Mth.clamp(pullY, -0.15, 0.15);
                    this.tetherTarget.setDeltaMovement(current.x, current.y + cappedY, current.z);
                }
            }
            return;
        }


        if (this.returning) {
            this.returningTicks++;
            Vec3 ownerEye = owner.getEyePosition(1.0f);
            Vec3 toOwner = ownerEye.subtract(this.position());
            double length = toOwner.length();

            if (length < RETURN_CATCH_DISTANCE || (this.returningTicks > 20 && length < 2.0)) {
                this.discard();
                return;
            }

            double speed = Mth.clamp(RETURN_BASE_SPEED + length * RETURN_DISTANCE_FACTOR, RETURN_BASE_SPEED, RETURN_MAX_SPEED);
            Vec3 velocity = toOwner.normalize().scale(speed).add(owner.getDeltaMovement());
            this.setDeltaMovement(velocity);
            return;
        }


        double distance = this.distanceTo(owner);
        if (distance >= this.maxThrowDistance) {
            startReturning();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide || this.isTethered() || this.returning) {
            return;
        }

        Entity hit = result.getEntity();
        if (!(hit instanceof LivingEntity target) || hit == this.getOwner()) {
            return;
        }

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;


        DamageSource baseSource = this.damageSources().thrown(this, owner);
        if (target.isDamageSourceBlocked(baseSource)) {
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
            startReturning();
            return;
        }

        EpicFightDamageSource epicSource = EpicFightDamageSources.fromVanillaDamageSource(baseSource)
                .addRuntimeTag(EpicFightDamageTypeTags.WEAPON_INNATE)
                .setUsedItem(this.getItem());
        target.hurt(epicSource, this.projectileDamage);


        target.addEffect(new MobEffectInstance(PBMEffects.MARKED.get(), SickleThrowSkill.TETHER_DURATION_TICKS, 0, false, true, true), owner);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SickleThrowSkill.TETHER_DURATION_TICKS, 0, false, true, true), owner);
        target.addEffect(new MobEffectInstance(PBMEffects.CHAINED.get(), SickleThrowSkill.TETHER_DURATION_TICKS, 0, false, true, true), owner);


        this.tetherTarget = target;
        this.tetherTicksRemaining = SickleThrowSkill.TETHER_DURATION_TICKS;
        this.entityData.set(DATA_TETHERED, true);
        SickleThrowChains.bind(this, target);
        this.setDeltaMovement(Vec3.ZERO);
        playSickleFeedbackOnOwnerAndTarget(PBWSounds.CHAIN_UP.get(), owner, target);


        if (target instanceof ServerPlayer targetPlayer) {
            SickleThrowSkill.setTetherMovementLock(targetPlayer.getUUID(), true);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide || this.isTethered() || this.returning) {
            return;
        }

        if (result.getType() == HitResult.Type.BLOCK) {
            startReturning();
        }
    }

    public void pullOwnerToTarget() {
        if (this.level().isClientSide || this.tetherTarget == null) {
            return;
        }

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
        if (owner == null) {
            return;
        }

        Vec3 dir = this.tetherTarget.position().subtract(owner.position());
        if (dir.length() > 0.01) {
            owner.setDeltaMovement(dir.normalize().scale(1.2));
            owner.hurtMarked = true;
            owner.hasImpulse = true;
            MinecraftForge.EVENT_BUS.post(new SicklePullStartedEvent(owner, this.tetherTarget, true));
        }

        endTether();
    }

    public void pullTargetToOwner() {
        if (this.level().isClientSide || this.tetherTarget == null) {
            return;
        }

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
        if (owner == null) {
            return;
        }

        Vec3 dir = owner.position().subtract(this.tetherTarget.position());
        if (dir.length() > 0.01) {
            this.tetherTarget.setDeltaMovement(dir.normalize().scale(2.2));
            this.tetherTarget.hurtMarked = true;
            this.tetherTarget.hasImpulse = true;
            MinecraftForge.EVENT_BUS.post(new SicklePullStartedEvent(owner, this.tetherTarget, false));
        }

        endTether();
    }

    private void endTether() {
        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
        playSickleFeedbackOnOwnerAndTarget(PBWSounds.CHAIN_DOWN.get(), owner, this.tetherTarget);
        boolean lastChain = this.tetherTarget != null && SickleThrowChains.unbind(this, this.tetherTarget);
        if (lastChain) clearChainedEffect(this.tetherTarget);


        if (lastChain && this.tetherTarget instanceof ServerPlayer targetPlayer) {
            SickleThrowSkill.setTetherMovementLock(targetPlayer.getUUID(), false);
        }
        this.tetherTarget = null;
        this.entityData.set(DATA_TETHERED, false);
        startReturning();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && this.isTethered()) {
            boolean lastChain = this.tetherTarget != null && SickleThrowChains.unbind(this, this.tetherTarget);
            if (lastChain) clearChainedEffect(this.tetherTarget);
            if (lastChain && this.tetherTarget instanceof ServerPlayer targetPlayer) {
                SickleThrowSkill.setTetherMovementLock(targetPlayer.getUUID(), false);
            }
            this.tetherTarget = null;
            this.entityData.set(DATA_TETHERED, false);
        }
        Entity removalOwner = this.getOwner();
        super.remove(reason);
        if (!this.level().isClientSide && removalOwner instanceof ServerPlayer serverPlayer) {
            SickleThrowChains.removed(serverPlayer, this);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public float getPickRadius() {
        return 0.3f;
    }

    @Override
    protected float getGravity() {
        return 0.0f;
    }

    private void startReturning() {
        if (this.returning) {
            return;
        }
        this.returning = true;
        this.returningTicks = 0;
    }

    private static void clearChainedEffect(@Nullable LivingEntity target) {
        if (target != null) {
            target.removeEffect(PBMEffects.CHAINED.get());
        }
    }

    private void playSickleFeedbackOnOwnerAndTarget(SoundEvent sound, @Nullable LivingEntity owner, @Nullable LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (owner != null) {
            serverLevel.playSound(null, owner.getX(), owner.getY(), owner.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (target != null && (owner == null || target.getId() != owner.getId())) {
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}

