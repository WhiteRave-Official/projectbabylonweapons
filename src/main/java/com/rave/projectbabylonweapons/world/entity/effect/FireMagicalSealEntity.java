package com.rave.projectbabylonweapons.world.entity.effect;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.init.PBModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class FireMagicalSealEntity extends Entity implements GeoEntity {
    private static final RawAnimation ACTIVE_ANIMATION = RawAnimation.begin()
            .thenPlay("spawn_animation")
            .thenLoop("loop_animation");
    private static final RawAnimation DESPAWN_ANIMATION = RawAnimation.begin().thenPlay("despawn_animation");
    private static final int DEFAULT_ACTIVE_DURATION_TICKS = 60;
    private static final int DESPAWN_DURATION_TICKS = 60;
    private static final double Y_OFFSET = 0.02D;
    private static final String TAG_TARGET_UUID = "TargetUuid";
    private static final String TAG_EXPIRES_AT = "ExpiresAt";
    private static final String TAG_DESPAWNING = "Despawning";
    private static final EntityDataAccessor<Boolean> DATA_DESPAWNING = SynchedEntityData.defineId(FireMagicalSealEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID targetUuid;
    private long expiresAt;

    public FireMagicalSealEntity(EntityType<? extends FireMagicalSealEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public FireMagicalSealEntity(Level level) {
        this(PBModEntities.FIRE_MAGICAL_SEAL.get(), level);
    }

    public void configure(ServerPlayer target) {
        this.configure(target, DEFAULT_ACTIVE_DURATION_TICKS);
    }

    public void configure(ServerPlayer target, int activeDurationTicks) {
        this.targetUuid = target.getUUID();
        long gameTime = target.level().getGameTime();
        int boundedDuration = Mth.clamp(activeDurationTicks, 1, 20 * 60 * 10);
        this.expiresAt = gameTime + boundedDuration;
        this.setDespawning(false);
        this.setPos(target.getX(), target.getY() + Y_OFFSET, target.getZ());
        this.startRiding(target, true);
    }

    public boolean tracks(ServerPlayer target) {
        return target != null && target.getUUID().equals(this.targetUuid);
    }

    public UUID getTargetUuid() {
        return this.targetUuid;
    }

    public boolean isDespawning() {
        return this.entityData.get(DATA_DESPAWNING);
    }

    public void setDespawning(boolean despawning) {
        this.entityData.set(DATA_DESPAWNING, despawning);
    }

    public void beginDespawnNow() {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.beginDespawn(serverLevel.getGameTime());
        } else {
            this.setDespawning(true);
        }
    }

    public void clearAndDiscard() {
        this.discard();
    }

    public ResourceLocation getModelResource() {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/fire_magical_seal.geo.json");
    }

    public ResourceLocation getTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/fire_magical_seal.png");
    }

    public ResourceLocation getAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/fire_magical_seal_animations.animation.json");
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        ServerPlayer target = this.resolveTarget(serverLevel);
        if (target == null || !target.isAlive() || target.isRemoved()) {
            this.beginDespawn(gameTime);
        } else if (this.getVehicle() != target || !this.isPassenger()) {
            this.beginDespawn(gameTime);
        } else {
            if (!this.isDespawning() && gameTime >= this.expiresAt) {
                this.beginDespawn(gameTime);
            }
        }

        if (this.isDespawning() && gameTime >= this.expiresAt + DESPAWN_DURATION_TICKS) {
            this.discard();
        }
    }
    private void beginDespawn(long gameTime) {
        if (this.isDespawning()) {
            return;
        }

        this.setDespawning(true);
        this.expiresAt = gameTime;
    }

    private ServerPlayer resolveTarget(ServerLevel level) {
        Entity entity = this.targetUuid == null ? null : level.getEntity(this.targetUuid);
        return entity instanceof ServerPlayer player ? player : null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DESPAWNING, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(TAG_TARGET_UUID)) {
            this.targetUuid = tag.getUUID(TAG_TARGET_UUID);
        }
        this.expiresAt = tag.getLong(TAG_EXPIRES_AT);
        this.setDespawning(tag.getBoolean(TAG_DESPAWNING));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.targetUuid != null) {
            tag.putUUID(TAG_TARGET_UUID, this.targetUuid);
        }
        tag.putLong(TAG_EXPIRES_AT, this.expiresAt);
        tag.putBoolean(TAG_DESPAWNING, this.isDespawning());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "seal_controller", 0, state -> {
            state.getController().setAnimation(this.isDespawning() ? DESPAWN_ANIMATION : ACTIVE_ANIMATION);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}









