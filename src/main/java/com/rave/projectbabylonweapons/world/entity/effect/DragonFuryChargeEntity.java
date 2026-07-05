package com.rave.projectbabylonweapons.world.entity.effect;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class DragonFuryChargeEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(DragonFuryChargeEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_ORBIT_SLOT = SynchedEntityData.defineId(DragonFuryChargeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ORBIT_TOTAL = SynchedEntityData.defineId(DragonFuryChargeEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation LOOP_ANIMATION = RawAnimation.begin().thenLoop("animation.ender_spell_projectile.idle");
    private static final double ORBIT_RADIUS = 1.2D;
    private static final double ORBIT_HEIGHT = 1.15D;
    private static final double ORBIT_SPEED = 0.16D;
    private static final double TRAIL_MIN_MOVEMENT_SQR = 1.0E-4D;
    private static final int ORBIT_TRAIL_INTERVAL_TICKS = 2;
    private static final float VISUAL_SCALE = 0.65F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private Vec3 lastClientTrailPos;

    public DragonFuryChargeEntity(EntityType<? extends DragonFuryChargeEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void initializeOrbit(Player owner, int slot, int total) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(owner.getUUID()));
        this.setOrbitData(slot, total);
        Vec3 orbitPosition = this.computeOrbitPosition(owner, owner.tickCount);
        this.setPos(orbitPosition);
        this.refreshFacing(owner.position().add(0.0D, ORBIT_HEIGHT, 0.0D));
    }

    public void setOrbitData(int slot, int total) {
        this.entityData.set(DATA_ORBIT_SLOT, Math.max(0, slot));
        this.entityData.set(DATA_ORBIT_TOTAL, Math.max(1, total));
    }

    public int getOrbitSlot() {
        return this.entityData.get(DATA_ORBIT_SLOT);
    }

    public int getOrbitTotal() {
        return this.entityData.get(DATA_ORBIT_TOTAL);
    }

    public float getVisualScale() {
        return VISUAL_SCALE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_ORBIT_SLOT, 0);
        builder.define(DATA_ORBIT_TOTAL, 1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("owner_uuid")) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID("owner_uuid")));
        }
        this.entityData.set(DATA_ORBIT_SLOT, tag.getInt("orbit_slot"));
        this.entityData.set(DATA_ORBIT_TOTAL, Math.max(1, tag.getInt("orbit_total")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.entityData.get(DATA_OWNER_UUID).ifPresent(uuid -> tag.putUUID("owner_uuid", uuid));
        tag.putInt("orbit_slot", this.getOrbitSlot());
        tag.putInt("orbit_total", this.getOrbitTotal());
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
            WeaponVisualEffectHelper.playDragonFuryChargeSpawn(this);
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

        if ((this.tickCount % ORBIT_TRAIL_INTERVAL_TICKS) != 0) {
            this.lastClientTrailPos = currentPos;
            return;
        }

        Vec3 movement = currentPos.subtract(this.lastClientTrailPos);
        if (movement.lengthSqr() >= TRAIL_MIN_MOVEMENT_SQR) {
            PhotonWeaponEffectHelper.spawnDragonFuryChargeTrail(this, movement);
        }

        this.lastClientTrailPos = currentPos;
    }

    public void discardWithEffects() {
        if (!this.level().isClientSide) {
            WeaponVisualEffectHelper.playDragonFuryChargeDespawn(this);
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
        controllers.add(new AnimationController<>(this, "loop_controller", 0, state -> {
            state.getController().setAnimation(LOOP_ANIMATION);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}