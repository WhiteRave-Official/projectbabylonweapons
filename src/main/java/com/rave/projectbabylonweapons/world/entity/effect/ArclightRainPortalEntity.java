package com.rave.projectbabylonweapons.world.entity.effect;

import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.world.entity.projectile.ArclightMiniProjectileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class ArclightRainPortalEntity extends Entity {
    public static final int STATE_WAITING = 0;
    public static final int STATE_ACTIVE = 1;
    public static final int STATE_CLOSED = 2;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(ArclightRainPortalEntity.class, EntityDataSerializers.INT);

    private static final Vec3 SHOT_DIRECTION = new Vec3(0.0D, -1.0D, 0.0D);
    private static final int WAITING_LIFETIME = 100;
    private static final int REOPEN_DELAY_MIN = 5;
    private static final int REOPEN_DELAY_VARIANCE = 4;
    private static final int OPENING_DELAY = 7;
    private static final int MAX_LIFETIME = 180;

    private UUID ownerUuid;
    private Vec3 areaCenter = Vec3.ZERO;
    private Vec3 areaForward = new Vec3(0.0D, 0.0D, 1.0D);
    private float damage;
    private int remainingShots;
    private int actionDelay;
    private int stateTicks;

    public ArclightRainPortalEntity(EntityType<? extends ArclightRainPortalEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public ArclightRainPortalEntity(Level level) {
        this(PBModEntities.ARCLIGHT_RAIN_PORTAL.get(), level);
    }

    public void configure(LivingEntity owner, Vec3 center, Vec3 forward, float damage, int shots) {
        Vec3 flatForward = new Vec3(forward.x, 0.0D, forward.z);
        this.ownerUuid = owner.getUUID();
        this.areaCenter = center;
        this.areaForward = flatForward.lengthSqr() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : flatForward.normalize();
        this.damage = Math.max(0.0F, damage);
        this.remainingShots = Math.max(1, shots);
        this.entityData.set(DATA_STATE, STATE_WAITING);
        this.relocateWithinArea();
    }

    public void activate(int delayTicks) {
        if (!this.level().isClientSide && this.getPortalState() == STATE_WAITING) {
            this.actionDelay = Math.max(0, delayTicks);
            this.stateTicks = 0;
            this.entityData.set(DATA_STATE, STATE_ACTIVE);
        }
    }

    public int getPortalState() {
        return this.entityData.get(DATA_STATE);
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) {
            if (this.getPortalState() != STATE_CLOSED && this.tickCount % 3 == 0) {
                PhotonWeaponEffectHelper.spawnArclightMiniPortal(this, SHOT_DIRECTION);
            }
            return;
        }

        if (this.tickCount > MAX_LIFETIME || !this.hasValidOwner()) {
            this.discard();
            return;
        }

        switch (this.getPortalState()) {
            case STATE_WAITING -> {
                if (++this.stateTicks > WAITING_LIFETIME) {
                    this.discard();
                }
            }
            case STATE_ACTIVE -> this.tickActive();
            case STATE_CLOSED -> this.tickClosed();
            default -> this.discard();
        }
    }

    private void tickActive() {
        if (this.actionDelay > 0) {
            this.actionDelay--;
            return;
        }

        this.fireSword();
        this.remainingShots--;
        this.level().broadcastEntityEvent(this, (byte) 4);
        if (this.remainingShots <= 0) {
            this.discard();
            return;
        }

        this.entityData.set(DATA_STATE, STATE_CLOSED);
        this.actionDelay = REOPEN_DELAY_MIN + this.random.nextInt(REOPEN_DELAY_VARIANCE);
    }

    private void tickClosed() {
        if (this.actionDelay-- > 0) {
            return;
        }

        this.relocateWithinArea();
        this.entityData.set(DATA_STATE, STATE_ACTIVE);
        this.actionDelay = OPENING_DELAY;
    }

    private void fireSword() {
        if (!(this.level() instanceof ServerLevel level) || !(level.getEntity(this.ownerUuid) instanceof LivingEntity owner)) {
            return;
        }

        ArclightMiniProjectileEntity sword = new ArclightMiniProjectileEntity(level);
        sword.setPos(this.position());
        sword.configure(owner, SHOT_DIRECTION, this.damage, false);
        level.addFreshEntity(sword);
        sword.queueLaunch(0);
    }

    private boolean hasValidOwner() {
        return this.level() instanceof ServerLevel level
                && this.ownerUuid != null
                && level.getEntity(this.ownerUuid) instanceof LivingEntity owner
                && owner.isAlive();
    }

    private void relocateWithinArea() {
        Vec3 right = new Vec3(-this.areaForward.z, 0.0D, this.areaForward.x);
        double sideOffset = (this.random.nextDouble() - 0.5D) * 12.0D;
        double depthOffset = (this.random.nextDouble() - 0.5D) * 12.0D;
        double heightOffset = (this.random.nextDouble() - 0.5D) * 3.0D;
        Vec3 position = this.areaCenter
                .add(right.scale(sideOffset))
                .add(this.areaForward.scale(depthOffset))
                .add(0.0D, heightOffset, 0.0D);
        this.setPos(position);
    }

    @Override
    public void handleEntityEvent(byte eventId) {
        if (eventId == 4) {
            PhotonWeaponEffectHelper.spawnArclightMiniDissolve(this, this.position());
            return;
        }
        super.handleEntityEvent(eventId);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_STATE, STATE_WAITING);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUuid = tag.getUUID("Owner");
        }
        this.areaCenter = new Vec3(tag.getDouble("AreaX"), tag.getDouble("AreaY"), tag.getDouble("AreaZ"));
        this.areaForward = new Vec3(tag.getDouble("ForwardX"), 0.0D, tag.getDouble("ForwardZ"));
        this.damage = tag.getFloat("Damage");
        this.remainingShots = tag.getInt("RemainingShots");
        this.actionDelay = tag.getInt("ActionDelay");
        this.stateTicks = tag.getInt("StateTicks");
        this.entityData.set(DATA_STATE, tag.getInt("PortalState"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.putUUID("Owner", this.ownerUuid);
        }
        tag.putDouble("AreaX", this.areaCenter.x);
        tag.putDouble("AreaY", this.areaCenter.y);
        tag.putDouble("AreaZ", this.areaCenter.z);
        tag.putDouble("ForwardX", this.areaForward.x);
        tag.putDouble("ForwardZ", this.areaForward.z);
        tag.putFloat("Damage", this.damage);
        tag.putInt("RemainingShots", this.remainingShots);
        tag.putInt("ActionDelay", this.actionDelay);
        tag.putInt("StateTicks", this.stateTicks);
        tag.putInt("PortalState", this.getPortalState());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
