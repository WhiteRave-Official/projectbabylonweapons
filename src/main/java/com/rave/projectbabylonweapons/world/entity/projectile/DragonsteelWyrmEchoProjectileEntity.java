package com.rave.projectbabylonweapons.world.entity.projectile;

import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.init.PBModEntities;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DragonsteelWyrmEchoProjectileEntity extends Projectile {
    private record TrailSegment(Vec3 position, long expireGameTime) {
    }

    private record ClientTrailSegment(Vec3 position, Vec3 forward, int expireTick) {
    }

    private static final String TAG_DAMAGE = "Damage";
    private static final String TAG_TRAIL_DAMAGE = "TrailDamage";
    private static final String TAG_RANGE = "Range";
    private static final String TAG_SPEED = "Speed";
    private static final String TAG_HIT_RADIUS = "HitRadius";
    private static final String TAG_TRAIL_LIFETIME = "TrailLifetime";
    private static final String TAG_TRAIL_INTERVAL = "TrailInterval";
    private static final String TAG_BERSERK = "Berserk";
    private static final String TAG_LINGERING = "Lingering";
    private static final String TAG_START_X = "StartX";
    private static final String TAG_START_Y = "StartY";
    private static final String TAG_START_Z = "StartZ";
    private static final String TAG_SOURCE_WEAPON = "SourceWeapon";
    private static final float INERTIA = 0.99F;
    private static final double TRAIL_DAMAGE_RADIUS = 0.9D;
    private static final double TRAIL_VERTICAL_RADIUS = 1.25D;
    private static final EntityDataAccessor<Boolean> DATA_BERSERK = SynchedEntityData.defineId(DragonsteelWyrmEchoProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_RANGE = SynchedEntityData.defineId(DragonsteelWyrmEchoProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_TRAIL_LIFETIME = SynchedEntityData.defineId(DragonsteelWyrmEchoProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LINGERING = SynchedEntityData.defineId(DragonsteelWyrmEchoProjectileEntity.class, EntityDataSerializers.BOOLEAN);

    private final Set<UUID> damagedTargets = new HashSet<>();
    private final List<TrailSegment> trailSegments = new ArrayList<>();
    private final List<ClientTrailSegment> clientTrailSegments = new ArrayList<>();
    private final Map<UUID, Long> trailDamageCooldowns = new HashMap<>();
    private float damage;
    private float trailDamage;
    private float rangeBlocks = 5.0F;
    private float speed = 0.85F;
    private float hitRadius = 0.55F;
    private int trailLifetimeTicks = 60;
    private int trailDamageIntervalTicks = 5;
    private Vec3 startPos = Vec3.ZERO;
    private ItemStack sourceWeapon = ItemStack.EMPTY;
    private boolean clientBurstSpawned;

    public DragonsteelWyrmEchoProjectileEntity(EntityType<? extends DragonsteelWyrmEchoProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public DragonsteelWyrmEchoProjectileEntity(Level level) {
        this(PBModEntities.DRAGONSTEEL_WYRM_ECHO_PROJECTILE.get(), level);
    }

    public DragonsteelWyrmEchoProjectileEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(PBModEntities.DRAGONSTEEL_WYRM_ECHO_PROJECTILE.get(), level);
    }

    public void configure(LivingEntity owner, ItemStack sourceWeapon, float damage, float trailDamage, float rangeBlocks,
                          float speed, float hitRadius, int trailLifetimeTicks, int trailDamageIntervalTicks, boolean berserk) {
        this.setOwner(owner);
        this.sourceWeapon = sourceWeapon.copy();
        this.damage = Math.max(0.0F, damage);
        this.trailDamage = Math.max(0.0F, trailDamage);
        this.rangeBlocks = Math.max(0.1F, rangeBlocks);
        this.speed = Math.max(0.05F, speed);
        this.hitRadius = Math.max(0.1F, hitRadius);
        this.trailLifetimeTicks = Math.max(1, trailLifetimeTicks);
        this.trailDamageIntervalTicks = Math.max(1, trailDamageIntervalTicks);
        this.entityData.set(DATA_BERSERK, berserk);
        this.entityData.set(DATA_RANGE, this.rangeBlocks);
        this.entityData.set(DATA_TRAIL_LIFETIME, this.trailLifetimeTicks);
        this.entityData.set(DATA_LINGERING, false);
        this.startPos = this.position();
        this.trailSegments.clear();
        this.clientTrailSegments.clear();
        this.trailDamageCooldowns.clear();
    }

    public boolean isBerserk() {
        return this.entityData.get(DATA_BERSERK);
    }

    public boolean isLingering() {
        return this.entityData.get(DATA_LINGERING);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_BERSERK, false);
        this.entityData.define(DATA_RANGE, 5.0F);
        this.entityData.define(DATA_TRAIL_LIFETIME, 60);
        this.entityData.define(DATA_LINGERING, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount == 1 && this.startPos == Vec3.ZERO) {
            this.startPos = this.position();
        }

        if (this.isLingering()) {
            this.setDeltaMovement(Vec3.ZERO);
            if (this.level().isClientSide) {
                this.processClientLingeringEffects();
                if (this.clientTrailSegments.isEmpty()) {
                    this.discard();
                }
            } else {
                this.processTrailDamage();
                if (this.trailSegments.isEmpty()) {
                    this.discard();
                }
            }
            return;
        }

        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6D) {
            if (this.level().isClientSide) {
                this.beginLingering();
            } else {
                this.beginLingering();
            }
            return;
        }

        if (this.level().isClientSide) {
            this.spawnFlightEffects(movement);
        } else {
            this.recordTrailSegment();
            this.affectEntitiesAlongPath(movement);
            this.processTrailDamage();
            if (this.isLingering()) {
                return;
            }
        }

        HitResult blockHit = this.level().clip(new ClipContext(this.position(), this.position().add(movement), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) blockHit);
            return;
        }

        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        this.updateRotation();
        this.setDeltaMovement(movement.scale(INERTIA));

        if (this.position().distanceTo(this.startPos) >= this.entityData.get(DATA_RANGE) || this.tickCount > 80) {
            if (this.level().isClientSide) {
                this.spawnClientBurst(this.position());
                this.beginLingering();
            } else {
                this.beginLingering();
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level().isClientSide && reason != RemovalReason.UNLOADED_TO_CHUNK) {
            this.spawnClientBurst(this.position());
        }
        super.remove(reason);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Entity hits are handled by sweeping so fast echoes do not miss targets between ticks.
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide) {
            this.spawnClientBurst(result.getLocation());
            this.beginLingering();
        }
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.beginLingering();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && !this.level().isClientSide) {
            this.beginLingering();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(TAG_DAMAGE, this.damage);
        tag.putFloat(TAG_TRAIL_DAMAGE, this.trailDamage);
        tag.putFloat(TAG_RANGE, this.rangeBlocks);
        tag.putFloat(TAG_SPEED, this.speed);
        tag.putFloat(TAG_HIT_RADIUS, this.hitRadius);
        tag.putInt(TAG_TRAIL_LIFETIME, this.trailLifetimeTicks);
        tag.putInt(TAG_TRAIL_INTERVAL, this.trailDamageIntervalTicks);
        tag.putBoolean(TAG_BERSERK, this.isBerserk());
        tag.putBoolean(TAG_LINGERING, this.isLingering());
        tag.putDouble(TAG_START_X, this.startPos.x);
        tag.putDouble(TAG_START_Y, this.startPos.y);
        tag.putDouble(TAG_START_Z, this.startPos.z);
        if (!this.sourceWeapon.isEmpty()) {
            tag.put(TAG_SOURCE_WEAPON, this.sourceWeapon.save(new CompoundTag()));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat(TAG_DAMAGE);
        this.trailDamage = tag.getFloat(TAG_TRAIL_DAMAGE);
        this.rangeBlocks = Math.max(0.1F, tag.getFloat(TAG_RANGE));
        this.speed = Math.max(0.05F, tag.getFloat(TAG_SPEED));
        this.hitRadius = Math.max(0.1F, tag.getFloat(TAG_HIT_RADIUS));
        this.trailLifetimeTicks = Math.max(1, tag.getInt(TAG_TRAIL_LIFETIME));
        this.trailDamageIntervalTicks = Math.max(1, tag.getInt(TAG_TRAIL_INTERVAL));
        this.entityData.set(DATA_BERSERK, tag.getBoolean(TAG_BERSERK));
        this.entityData.set(DATA_RANGE, this.rangeBlocks);
        this.entityData.set(DATA_TRAIL_LIFETIME, this.trailLifetimeTicks);
        this.entityData.set(DATA_LINGERING, tag.getBoolean(TAG_LINGERING));
        this.startPos = new Vec3(tag.getDouble(TAG_START_X), tag.getDouble(TAG_START_Y), tag.getDouble(TAG_START_Z));
        if (tag.contains(TAG_SOURCE_WEAPON)) {
            this.sourceWeapon = ItemStack.of(tag.getCompound(TAG_SOURCE_WEAPON));
        }
    }

    private void affectEntitiesAlongPath(Vec3 movement) {
        if (!(this.getOwner() instanceof LivingEntity owner) || this.damage <= 0.0F) {
            return;
        }

        AABB sweepBox = this.getBoundingBox().expandTowards(movement).inflate(this.hitRadius);
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, sweepBox, entity -> entity.isAlive() && this.canHitEntity(entity))) {
            if (target.isAlliedTo(owner) || !this.damagedTargets.add(target.getUUID())) {
                continue;
            }

            this.damageTarget(owner, target, this.damage);

            if (!this.isBerserk()) {
                this.beginLingering();
                return;
            }
        }
    }

    private void recordTrailSegment() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        this.trailSegments.add(new TrailSegment(this.position(), serverLevel.getGameTime() + this.trailLifetimeTicks));
    }

    private void processTrailDamage() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) || !(this.getOwner() instanceof LivingEntity owner)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        Iterator<TrailSegment> iterator = this.trailSegments.iterator();
        while (iterator.hasNext()) {
            TrailSegment segment = iterator.next();
            if (segment.expireGameTime() <= gameTime) {
                iterator.remove();
                continue;
            }

            AABB damageArea = new AABB(
                    segment.position().x - TRAIL_DAMAGE_RADIUS,
                    segment.position().y - 0.35D,
                    segment.position().z - TRAIL_DAMAGE_RADIUS,
                    segment.position().x + TRAIL_DAMAGE_RADIUS,
                    segment.position().y + TRAIL_VERTICAL_RADIUS,
                    segment.position().z + TRAIL_DAMAGE_RADIUS
            );

            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, damageArea, entity -> entity.isAlive() && entity != owner)) {
                if (target.isAlliedTo(owner)) {
                    continue;
                }

                long nextAllowedTick = this.trailDamageCooldowns.getOrDefault(target.getUUID(), 0L);
                if (gameTime < nextAllowedTick) {
                    continue;
                }

                this.trailDamageCooldowns.put(target.getUUID(), gameTime + this.trailDamageIntervalTicks);
                this.damageTarget(owner, target, this.trailDamage);
            }
        }
    }

    private void damageTarget(LivingEntity owner, LivingEntity target, float amount) {
        if (amount <= 0.0F) {
            return;
        }

        int originalInvulnerableTime = target.invulnerableTime;
        Vec3 originalMovement = target.getDeltaMovement();
        target.invulnerableTime = 0;
        try {
            target.hurt(this.createDamageSource(owner), amount);
        } finally {
            target.invulnerableTime = originalInvulnerableTime;
            target.setDeltaMovement(originalMovement);
            target.hurtMarked = true;
        }
    }

    private void beginLingering() {
        this.entityData.set(DATA_LINGERING, true);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void spawnFlightEffects(Vec3 movement) {
        this.recordClientTrailSegment(movement);
        int visualLifetime = this.entityData.get(DATA_TRAIL_LIFETIME);
        PhotonWeaponEffectHelper.spawnDragonsteelWyrmEchoFlight(this, movement, this.isBerserk(), visualLifetime);
        if (this.isBerserk()) {
            PhotonWeaponEffectHelper.spawnDragonsteelWyrmEchoFlight(this, movement.scale(1.2D), true, visualLifetime);
        }
    }

    private void recordClientTrailSegment(Vec3 movement) {
        if ((this.tickCount & 1) != 0 || movement.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 forward = movement.normalize();
        Vec3 center = this.position().subtract(forward.scale(0.9D)).add(0.0D, 0.05D, 0.0D);
        this.clientTrailSegments.add(new ClientTrailSegment(center, forward, this.tickCount + this.entityData.get(DATA_TRAIL_LIFETIME)));
    }

    private void processClientLingeringEffects() {
        Iterator<ClientTrailSegment> iterator = this.clientTrailSegments.iterator();
        while (iterator.hasNext()) {
            ClientTrailSegment segment = iterator.next();
            int remainingTicks = segment.expireTick() - this.tickCount;
            if (remainingTicks <= 0) {
                iterator.remove();
                continue;
            }

            if ((this.tickCount % 5) == 0) {
                PhotonWeaponEffectHelper.spawnDragonsteelWyrmEchoLingering(
                        this.level(),
                        segment.position(),
                        segment.forward(),
                        Math.min(14, remainingTicks)
                );
            }
        }
    }

    private void spawnClientBurst(Vec3 position) {
        if (this.clientBurstSpawned) {
            return;
        }
        this.clientBurstSpawned = true;
        PhotonWeaponEffectHelper.spawnDragonsteelWyrmEchoImpact(this, position, this.isBerserk());
    }

    private DamageSource createDamageSource(LivingEntity owner) {
        ResourceKey<DamageType> damageType = ISSDamageTypes.ENDER_MAGIC;
        return new DamageSource(
                this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageType),
                this,
                owner
        );
    }
}
