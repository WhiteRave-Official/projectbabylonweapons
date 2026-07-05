package com.rave.projectbabylonweapons.world.entity.effect;

import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class DiamondShardEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(DiamondShardEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_ORBIT_SLOT = SynchedEntityData.defineId(DiamondShardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ORBIT_TOTAL = SynchedEntityData.defineId(DiamondShardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LAUNCHED = SynchedEntityData.defineId(DiamondShardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(DiamondShardEntity.class, EntityDataSerializers.FLOAT);

    private static final double ORBIT_RADIUS = 1.2D;
    private static final double ORBIT_HEIGHT = 1.15D;
    private static final double ORBIT_SPEED = 0.18D;
    private static final int DEFAULT_ORBIT_LIFETIME_TICKS = 8 * 20;
    private static final int DEFAULT_WEAPON_CHIP_DURATION_TICKS = 10 * 20;
    private static final int PROJECTILE_LIFETIME_TICKS = 30;
    private static final double HIT_RADIUS = 0.45D;
    private static final int ORBIT_TRAIL_INTERVAL_TICKS = 2;
    private static final double TRAIL_MIN_MOVEMENT_SQR = 1.0E-4D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int spawnTick;
    private int orbitLifetimeTicks = DEFAULT_ORBIT_LIFETIME_TICKS;
    private int weaponChipDurationTicks = DEFAULT_WEAPON_CHIP_DURATION_TICKS;
    private int launchedTick = -1;
    @Nullable
    private Vec3 lastClientTrailPos;

    public DiamondShardEntity(EntityType<? extends DiamondShardEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void initializeOrbit(Player owner, int slot, int total) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(owner.getUUID()));
        this.setOrbitData(slot, total);
        this.spawnTick = owner.tickCount;
        this.setDamageAmount((float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.15F);
        Vec3 orbitPosition = this.computeOrbitPosition(owner, owner.tickCount);
        this.setPos(orbitPosition);
        this.refreshFacing(owner.position().add(0.0D, ORBIT_HEIGHT, 0.0D));
    }

    public void setOrbitData(int slot, int total) {
        this.entityData.set(DATA_ORBIT_SLOT, Math.max(0, slot));
        this.entityData.set(DATA_ORBIT_TOTAL, Math.max(1, total));
    }

    public void setLifetimeTicks(int lifetimeTicks) {
        this.orbitLifetimeTicks = Math.max(1, lifetimeTicks);
    }

    public void setWeaponChipDurationTicks(int durationTicks) {
        this.weaponChipDurationTicks = Math.max(1, durationTicks);
    }

    public int getOrbitSlot() {
        return this.entityData.get(DATA_ORBIT_SLOT);
    }

    public int getOrbitTotal() {
        return this.entityData.get(DATA_ORBIT_TOTAL);
    }

    public boolean isLaunched() {
        return this.entityData.get(DATA_LAUNCHED);
    }

    public float getDamageAmount() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public void setDamageAmount(float damage) {
        this.entityData.set(DATA_DAMAGE, Math.max(0.0F, damage));
    }

    public void launch(Vec3 targetPoint, double launchSpeed) {
        Player owner = this.getOwnerPlayer();
        if (owner == null) {
            return;
        }

        Vec3 direction = targetPoint.subtract(this.position());
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = owner.getLookAngle();
        }
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        }

        this.entityData.set(DATA_LAUNCHED, true);
        this.launchedTick = this.tickCount;
        Vec3 velocity = direction.normalize().scale(Math.max(0.05D, launchSpeed));
        this.setDeltaMovement(velocity);
        this.refreshFacing(this.position().add(velocity));
        this.lastClientTrailPos = this.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_ORBIT_SLOT, 0);
        builder.define(DATA_ORBIT_TOTAL, 1);
        builder.define(DATA_LAUNCHED, false);
        builder.define(DATA_DAMAGE, 0.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("owner_uuid")) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID("owner_uuid")));
        }
        this.entityData.set(DATA_ORBIT_SLOT, tag.getInt("orbit_slot"));
        this.entityData.set(DATA_ORBIT_TOTAL, Math.max(1, tag.getInt("orbit_total")));
        this.entityData.set(DATA_LAUNCHED, tag.getBoolean("launched"));
        this.entityData.set(DATA_DAMAGE, tag.getFloat("damage"));
        this.spawnTick = tag.getInt("spawn_tick");
        this.orbitLifetimeTicks = tag.contains("orbit_lifetime_ticks") ? Math.max(1, tag.getInt("orbit_lifetime_ticks")) : DEFAULT_ORBIT_LIFETIME_TICKS;
        this.weaponChipDurationTicks = tag.contains("weapon_chip_duration_ticks") ? Math.max(1, tag.getInt("weapon_chip_duration_ticks")) : DEFAULT_WEAPON_CHIP_DURATION_TICKS;
        this.launchedTick = tag.contains("launched_tick") ? tag.getInt("launched_tick") : -1;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.entityData.get(DATA_OWNER_UUID).ifPresent(uuid -> tag.putUUID("owner_uuid", uuid));
        tag.putInt("orbit_slot", this.getOrbitSlot());
        tag.putInt("orbit_total", this.getOrbitTotal());
        tag.putBoolean("launched", this.isLaunched());
        tag.putFloat("damage", this.getDamageAmount());
        tag.putInt("spawn_tick", this.spawnTick);
        tag.putInt("orbit_lifetime_ticks", this.orbitLifetimeTicks);
        tag.putInt("weapon_chip_duration_ticks", this.weaponChipDurationTicks);
        tag.putInt("launched_tick", this.launchedTick);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            this.tickClient();
            return;
        }

        Player owner = this.getOwnerPlayer();
        if (owner == null || !owner.isAlive()) {
            this.discardWithEffects();
            return;
        }

        if (this.tickCount == 1) {
            WeaponVisualEffectHelper.playDiamondShardSpawn(this);
        }

        if (this.isLaunched()) {
            this.tickLaunched(owner);
            return;
        }

        if ((owner.tickCount - this.spawnTick) >= this.orbitLifetimeTicks) {
            this.discardWithEffects();
            return;
        }

        Vec3 orbitPosition = this.computeOrbitPosition(owner, owner.tickCount);
        this.setPos(orbitPosition);
        this.setDeltaMovement(Vec3.ZERO);
        this.refreshFacing(owner.position().add(0.0D, ORBIT_HEIGHT, 0.0D));
    }

    private void tickClient() {
        Vec3 currentPos = this.position();
        if (this.lastClientTrailPos == null) {
            this.lastClientTrailPos = currentPos;
            return;
        }

        Vec3 movement = currentPos.subtract(this.lastClientTrailPos);
        if (this.isLaunched()) {
            if (movement.lengthSqr() < TRAIL_MIN_MOVEMENT_SQR) {
                movement = this.getDeltaMovement();
            }
            if (movement.lengthSqr() >= TRAIL_MIN_MOVEMENT_SQR) {
                PhotonWeaponEffectHelper.spawnDiamondShardFlightTrail(this, movement);
            }
        } else {
            if ((this.tickCount % ORBIT_TRAIL_INTERVAL_TICKS) != 0) {
                this.lastClientTrailPos = currentPos;
                return;
            }
            if (movement.lengthSqr() >= TRAIL_MIN_MOVEMENT_SQR) {
                PhotonWeaponEffectHelper.spawnDiamondShardOrbitTrail(this, movement);
            }
        }

        this.lastClientTrailPos = currentPos;
    }

    private void tickLaunched(Player owner) {
        Vec3 movement = this.getDeltaMovement();
        Vec3 nextPosition = this.position().add(movement);
        this.setPos(nextPosition);
        this.refreshFacing(nextPosition.add(movement));

        EntityHitResult entityHitResult = this.findHitTarget(owner);
        if (entityHitResult != null && entityHitResult.getEntity() instanceof LivingEntity target) {
            this.hitTarget(owner, target);
            return;
        }

        if (!this.level().noCollision(this, this.getBoundingBox())) {
            this.discardWithEffects();
            return;
        }

        if (this.launchedTick >= 0 && (this.tickCount - this.launchedTick) >= PROJECTILE_LIFETIME_TICKS) {
            this.discardWithEffects();
        }
    }

    @Nullable
    private EntityHitResult findHitTarget(Player owner) {
        AABB searchBox = this.getBoundingBox().inflate(HIT_RADIUS);
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity.isAlive() && entity != owner && !entity.isAlliedTo(owner))) {
            return new EntityHitResult(candidate);
        }
        return null;
    }

    private void hitTarget(Player owner, LivingEntity target) {
        DamageSource damageSource = owner.damageSources().playerAttack(owner);
        int originalInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        try {
            target.hurt(damageSource, this.getDamageAmount());
        } finally {
            target.invulnerableTime = originalInvulnerableTime;
        }
        target.addEffect(new MobEffectInstance(PBMEffects.WEAPON_CHIP, this.weaponChipDurationTicks, 0, false, true, true));
        this.discardWithEffects();
    }

    public void discardWithEffects() {
        if (!this.level().isClientSide) {
            WeaponVisualEffectHelper.playDiamondShardDespawn(this);
        }
        this.discard();
    }

    @Nullable
    public UUID getOwnerUuid() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    @Nullable
    private Player getOwnerPlayer() {
        UUID ownerUuid = this.getOwnerUuid();
        if (ownerUuid == null) {
            return null;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            ServerPlayer serverPlayer = serverLevel.getServer().getPlayerList().getPlayer(ownerUuid);
            if (serverPlayer != null) {
                return serverPlayer;
            }
        }

        for (Player player : this.level().players()) {
            if (ownerUuid.equals(player.getUUID())) {
                return player;
            }
        }

        return null;
    }

    private Vec3 computeOrbitPosition(Player owner, int ageTicks) {
        double angle = (ageTicks * ORBIT_SPEED) + (this.getOrbitSlot() * ((Math.PI * 2.0D) / this.getOrbitTotal()));
        double xOffset = Math.cos(angle) * ORBIT_RADIUS;
        double zOffset = Math.sin(angle) * ORBIT_RADIUS;
        return owner.position().add(xOffset, ORBIT_HEIGHT, zOffset);
    }

    private void refreshFacing(Vec3 lookTarget) {
        Vec3 delta = lookTarget.subtract(this.position());
        if (delta.lengthSqr() <= 1.0E-6D) {
            return;
        }

        Vec3 normalized = delta.normalize();
        float yaw = (float) (Mth.atan2(normalized.z, normalized.x) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(normalized.y, Math.sqrt(normalized.x * normalized.x + normalized.z * normalized.z)) * (180.0D / Math.PI)));
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.setXRot(pitch);
        this.xRotO = pitch;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

