package com.rave.projectbabylonweapons.world.entity.projectile;

import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.init.PBModParticles;
import com.rave.projectbabylonweapons.init.PBWSounds;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import java.util.HashSet;
import java.util.Set;

public class ArclightMiniProjectileEntity extends Projectile {
    public static final int STATE_WAITING = 0;
    public static final int STATE_QUEUED = 1;
    public static final int STATE_FLYING = 2;
    public static final int STATE_EMBEDDED = 3;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(ArclightMiniProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LAUNCH_DELAY =
            SynchedEntityData.defineId(ArclightMiniProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DIRECTION_X =
            SynchedEntityData.defineId(ArclightMiniProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DIRECTION_Y =
            SynchedEntityData.defineId(ArclightMiniProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DIRECTION_Z =
            SynchedEntityData.defineId(ArclightMiniProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_PORTAL_VISUAL =
            SynchedEntityData.defineId(ArclightMiniProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_EXPLOSIVE =
            SynchedEntityData.defineId(ArclightMiniProjectileEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float SPEED = 2.35F;
    private static final int WAITING_LIFETIME = 60;
    private static final int FLYING_LIFETIME = 50;
    private static final int BLOCK_COLLISION_GRACE_TICKS = 2;
    private static final int EMBEDDED_LIFETIME = 20;
    private static final int RAIN_EMBEDDED_LIFETIME = 10;
    private static final double EXPLOSION_RADIUS = 2.0D;

    private float damage;
    private float explosionDamage;
    private boolean areaDamage;
    private int stateTicks;
    private int previousClientState = -1;
    private boolean clientDissolveSpawned;
    private boolean clientTrailSpawned;
    private final Set<Integer> piercedEntityIds = new HashSet<>();

    public ArclightMiniProjectileEntity(EntityType<? extends ArclightMiniProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public ArclightMiniProjectileEntity(Level level) {
        this(PBModEntities.ARCLIGHT_MINI_PROJECTILE.get(), level);
    }

    public ArclightMiniProjectileEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(PBModEntities.ARCLIGHT_MINI_PROJECTILE.get(), level);
    }

    public void configure(LivingEntity owner, Vec3 direction, float damage) {
        this.configure(owner, direction, damage, true, false);
    }

    public void configure(LivingEntity owner, Vec3 direction, float damage, boolean portalVisual) {
        this.configure(owner, direction, damage, portalVisual, false);
    }

    public void configure(LivingEntity owner, Vec3 direction, float damage, boolean portalVisual, boolean areaDamage) {
        this.configure(owner, direction, damage, portalVisual, areaDamage, 0.0F);
    }

    public void configure(LivingEntity owner, Vec3 direction, float damage, boolean portalVisual,
                          boolean areaDamage, float explosionDamage) {
        Vec3 normalized = direction.lengthSqr() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : direction.normalize();
        this.setOwner(owner);
        this.piercedEntityIds.clear();
        this.damage = Math.max(0.0F, damage);
        this.areaDamage = areaDamage;
        this.explosionDamage = Math.max(0.0F, explosionDamage);
        this.entityData.set(DATA_EXPLOSIVE, this.explosionDamage > 0.0F);
        this.setDirection(normalized);
        this.entityData.set(DATA_STATE, STATE_WAITING);
        this.entityData.set(DATA_LAUNCH_DELAY, 0);
        this.entityData.set(DATA_PORTAL_VISUAL, portalVisual);
        this.setDeltaMovement(Vec3.ZERO);
        this.updateRotationFromDirection(normalized);
    }

    public void queueLaunch(int delayTicks) {
        if (!this.level().isClientSide && this.getState() == STATE_WAITING) {
            this.entityData.set(DATA_LAUNCH_DELAY, Math.max(0, delayTicks));
            this.entityData.set(DATA_STATE, STATE_QUEUED);
            this.stateTicks = 0;
        }
    }

    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    public Vec3 getFlightDirection() {
        Vec3 direction = new Vec3(
                this.entityData.get(DATA_DIRECTION_X),
                this.entityData.get(DATA_DIRECTION_Y),
                this.entityData.get(DATA_DIRECTION_Z)
        );
        return direction.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : direction.normalize();
    }

    @Override
    protected AABB makeBoundingBox() {
        double halfWidth = this.getBbWidth() * 0.5D;
        double halfHeight = this.getBbHeight() * 0.5D;
        return new AABB(
                this.getX() - halfWidth,
                this.getY() - halfHeight,
                this.getZ() - halfWidth,
                this.getX() + halfWidth,
                this.getY() + halfHeight,
                this.getZ() + halfWidth
        );
    }
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_STATE, STATE_WAITING);
        this.entityData.define(DATA_LAUNCH_DELAY, 0);
        this.entityData.define(DATA_DIRECTION_X, 0.0F);
        this.entityData.define(DATA_DIRECTION_Y, 0.0F);
        this.entityData.define(DATA_DIRECTION_Z, 1.0F);
        this.entityData.define(DATA_PORTAL_VISUAL, true);
        this.entityData.define(DATA_EXPLOSIVE, false);
    }

    @Override
    public void tick() {
        super.tick();
        int state = this.getState();

        if (this.level().isClientSide) {
            this.processClientStateChange(state);
        }

        switch (state) {
            case STATE_WAITING -> tickWaiting();
            case STATE_QUEUED -> tickQueued();
            case STATE_FLYING -> tickFlying();
            case STATE_EMBEDDED -> tickEmbedded();
            default -> discard();
        }
    }

    private void tickWaiting() {
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level().isClientSide) {
            if (this.entityData.get(DATA_PORTAL_VISUAL) && (this.tickCount % 3) == 0) {
                this.spawnPortalVisual();
            }
        } else if (++this.stateTicks > WAITING_LIFETIME) {
            this.discard();
        }
    }

    private void tickQueued() {
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level().isClientSide) {
            if (this.entityData.get(DATA_PORTAL_VISUAL) && (this.tickCount % 3) == 0) {
                this.spawnPortalVisual();
            }
            return;
        }

        int delay = this.entityData.get(DATA_LAUNCH_DELAY);
        if (delay > 0) {
            this.entityData.set(DATA_LAUNCH_DELAY, delay - 1);
        } else {
            beginFlight();
        }
    }

    private void beginFlight() {
        Vec3 direction = this.getFlightDirection();
        this.entityData.set(DATA_STATE, STATE_FLYING);
        this.stateTicks = 0;
        this.setDeltaMovement(direction.scale(SPEED));
        this.updateRotationFromDirection(direction);
        float pitch = 0.92F + this.random.nextFloat() * 0.16F;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                this.isSpear() ? PBWSounds.ARCLIGHT_SPEAR_SHOOT.get() : PBWSounds.ARCLIGHT_SWORD_SHOOT.get(),
                SoundSource.PLAYERS, 0.5F, pitch);
    }

    private void tickFlying() {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6D) {
            movement = this.getFlightDirection().scale(SPEED);
            this.setDeltaMovement(movement);
        }

        if (this.level().isClientSide) {
            this.spawnClientTrailOnce();
            this.spawnFlightVisual(movement);
        } else if (this.stateTicks >= BLOCK_COLLISION_GRACE_TICKS) {
            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onHit(hitResult);
                return;
            }
        }

        this.setPos(this.position().add(movement));
        this.updateRotationFromDirection(movement);
        if (!this.level().isClientSide && ++this.stateTicks > FLYING_LIFETIME) {
            this.discard();
        }
    }

    private void tickEmbedded() {
        this.setDeltaMovement(Vec3.ZERO);
        int lifetime = this.isRainProjectile() ? RAIN_EMBEDDED_LIFETIME : EMBEDDED_LIFETIME;
        if (!this.level().isClientSide && ++this.stateTicks > lifetime) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!super.canHitEntity(target) || target == this.getOwner()
                || this.piercedEntityIds.contains(target.getId())) {
            return false;
        }
        return !(this.getOwner() instanceof LivingEntity owner) || !target.isAlliedTo(owner);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide || !(this.getOwner() instanceof LivingEntity owner)) {
            return;
        }

        if (this.areaDamage) {
            this.damageArea(result.getLocation(), owner);
        } else {
            this.damageTarget(result.getEntity(), owner);
        }
        boolean exploded = this.damageExplosion(result.getLocation(), owner);
        this.level().broadcastEntityEvent(this, exploded ? (byte) 5 : (byte) 4);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide) {
            return;
        }

        if (this.getOwner() instanceof LivingEntity owner) {
            if (this.areaDamage) {
                this.damageArea(result.getLocation(), owner);
            }
            if (this.damageExplosion(result.getLocation(), owner)) {
                this.level().broadcastEntityEvent(this, (byte) 5);
            }
        }

        Vec3 direction = this.getFlightDirection();
        Vec3 embeddedPosition = result.getLocation().subtract(direction.scale(0.08D));
        this.setPos(embeddedPosition);
        this.setDeltaMovement(Vec3.ZERO);
        this.entityData.set(DATA_STATE, STATE_EMBEDDED);
        this.stateTicks = 0;
    }

    private void damageArea(Vec3 center, LivingEntity owner) {
        AABB area = new AABB(
                center.x - 1.0D, center.y - 1.0D, center.z - 1.0D,
                center.x + 1.0D, center.y + 1.0D, center.z + 1.0D
        );
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, this::canHitEntity)) {
            this.damageTarget(target, owner);
        }
    }

    private boolean damageExplosion(Vec3 center, LivingEntity owner) {
        if (this.explosionDamage <= 0.0F) {
            return false;
        }
        AABB area = new AABB(
                center.x - EXPLOSION_RADIUS, center.y - EXPLOSION_RADIUS, center.z - EXPLOSION_RADIUS,
                center.x + EXPLOSION_RADIUS, center.y + EXPLOSION_RADIUS, center.z + EXPLOSION_RADIUS
        );
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area,
                candidate -> super.canHitEntity(candidate) && candidate != owner && !candidate.isAlliedTo(owner))) {
            this.damageTarget(target, owner, this.explosionDamage, false);
        }
        this.explosionDamage = 0.0F;
        this.entityData.set(DATA_EXPLOSIVE, false);
        return true;
    }

    private void damageTarget(Entity target, LivingEntity owner) {
        this.damageTarget(target, owner, this.damage, true);
    }

    private void damageTarget(Entity target, LivingEntity owner, float amount, boolean markPierced) {
        if (markPierced) {
            this.piercedEntityIds.add(target.getId());
        }
        Vec3 originalMovement = target.getDeltaMovement();
        int originalInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        try {
            DamageSource holySource = new DamageSource(
                    this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(ISSDamageTypes.HOLY_MAGIC),
                    this,
                    owner
            );
            target.hurt(holySource, amount);
        } finally {
            target.invulnerableTime = originalInvulnerableTime;
            target.setDeltaMovement(originalMovement);
            target.hurtMarked = true;
        }
    }
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.damage);
        tag.putFloat("ExplosionDamage", this.explosionDamage);
        tag.putBoolean("AreaDamage", this.areaDamage);
        tag.putInt("State", this.getState());
        tag.putInt("LaunchDelay", this.entityData.get(DATA_LAUNCH_DELAY));
        Vec3 direction = this.getFlightDirection();
        tag.putDouble("DirectionX", direction.x);
        tag.putDouble("DirectionY", direction.y);
        tag.putDouble("DirectionZ", direction.z);
        tag.putInt("StateTicks", this.stateTicks);
        tag.putBoolean("PortalVisual", this.entityData.get(DATA_PORTAL_VISUAL));
        tag.putBoolean("Explosive", this.entityData.get(DATA_EXPLOSIVE));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("Damage");
        this.explosionDamage = tag.getFloat("ExplosionDamage");
        this.areaDamage = tag.getBoolean("AreaDamage");
        this.entityData.set(DATA_STATE, tag.getInt("State"));
        this.entityData.set(DATA_LAUNCH_DELAY, tag.getInt("LaunchDelay"));
        this.setDirection(new Vec3(tag.getDouble("DirectionX"), tag.getDouble("DirectionY"), tag.getDouble("DirectionZ")));
        this.stateTicks = tag.getInt("StateTicks");
        this.entityData.set(DATA_PORTAL_VISUAL, !tag.contains("PortalVisual") || tag.getBoolean("PortalVisual"));
        this.entityData.set(DATA_EXPLOSIVE, tag.getBoolean("Explosive") || this.explosionDamage > 0.0F);
    }

    @Override
    public void handleEntityEvent(byte eventId) {
        if (eventId == 4 || eventId == 5) {
            if (eventId == 5) {
                PhotonWeaponEffectHelper.spawnArclightRainExplosion(this, this.position());
            }
            this.spawnImpactVisual(this.position());
            return;
        }
        super.handleEntityEvent(eventId);
    }
    @Override
    public void remove(RemovalReason reason) {
        if (this.level().isClientSide && !this.clientDissolveSpawned && reason != RemovalReason.UNLOADED_TO_CHUNK) {
            this.clientDissolveSpawned = true;
            if (this.getState() == STATE_FLYING) {
                this.spawnImpactVisual(this.position());
            } else {
                this.spawnDissolveVisual(this.position());
            }
        }
        super.remove(reason);
    }

    private void processClientStateChange(int state) {
        if (state == this.previousClientState) {
            return;
        }

        if (state == STATE_FLYING) {
            this.spawnLaunchVisual();
        } else if (state == STATE_EMBEDDED) {
            this.spawnImpactVisual(this.position());
        }
        this.previousClientState = state;
    }

    public boolean isSpear() {
        return this.getType() == PBModEntities.ARCLIGHT_SPEAR_PROJECTILE.get();
    }

    public boolean isRainProjectile() {
        return !this.entityData.get(DATA_PORTAL_VISUAL);
    }

    private void spawnPortalVisual() {
        if (this.isSpear()) {
            PhotonWeaponEffectHelper.spawnArclightSpearPortal(this, this.getFlightDirection());
        } else {
            PhotonWeaponEffectHelper.spawnArclightMiniPortal(this, this.getFlightDirection());
        }
    }

    private void spawnLaunchVisual() {
        if (this.isSpear()) {
            PhotonWeaponEffectHelper.spawnArclightSpearLaunch(this, this.getFlightDirection());
        } else if (this.isRainProjectile()) {
            PhotonWeaponEffectHelper.spawnArclightRainLaunch(this, this.getFlightDirection());
        } else {
            PhotonWeaponEffectHelper.spawnArclightMiniLaunch(this, this.getFlightDirection());
        }
    }

    private void spawnFlightVisual(Vec3 movement) {
        if (this.isSpear()) {
            PhotonWeaponEffectHelper.spawnArclightSpearFlight(this, movement);
        } else if (this.isRainProjectile()) {
            PhotonWeaponEffectHelper.spawnArclightRainFlight(this, movement);
        } else {
            PhotonWeaponEffectHelper.spawnArclightMiniFlight(this, movement);
        }
    }

    private void spawnImpactVisual(Vec3 position) {
        if (this.isSpear()) {
            PhotonWeaponEffectHelper.spawnArclightSpearImpact(this, position, this.getFlightDirection());
        } else if (this.isRainProjectile()) {
            PhotonWeaponEffectHelper.spawnArclightRainImpact(this, position, this.getFlightDirection());
        } else {
            PhotonWeaponEffectHelper.spawnArclightMiniImpact(this, position, this.getFlightDirection());
        }
    }

    private void spawnDissolveVisual(Vec3 position) {
        if (this.isSpear()) {
            PhotonWeaponEffectHelper.spawnArclightSpearDissolve(this, position);
        } else if (this.isRainProjectile()) {
            PhotonWeaponEffectHelper.spawnArclightRainDissolve(this, position);
        } else {
            PhotonWeaponEffectHelper.spawnArclightMiniDissolve(this, position);
        }
    }
    private void spawnClientTrailOnce() {
        if (this.clientTrailSpawned || !this.isAlive()) {
            return;
        }
        this.level().addParticle(PBModParticles.ARCLIGHT_PROJECTILE_TRAIL.get(),
                this.getId(), 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        this.clientTrailSpawned = true;
    }

    private void setDirection(Vec3 direction) {
        Vec3 normalized = direction.lengthSqr() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : direction.normalize();
        this.entityData.set(DATA_DIRECTION_X, (float) normalized.x);
        this.entityData.set(DATA_DIRECTION_Y, (float) normalized.y);
        this.entityData.set(DATA_DIRECTION_Z, (float) normalized.z);
    }

    private void updateRotationFromDirection(Vec3 direction) {
        Vec3 normalized = direction.normalize();
        this.setYRot((float) (Mth.atan2(normalized.x, normalized.z) * Mth.RAD_TO_DEG));
        this.setXRot((float) (Mth.atan2(normalized.y, normalized.horizontalDistance()) * Mth.RAD_TO_DEG));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
}
