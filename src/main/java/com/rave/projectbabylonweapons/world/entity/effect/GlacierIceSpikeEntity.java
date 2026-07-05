package com.rave.projectbabylonweapons.world.entity.effect;

import com.rave.projectbabylonweapons.init.PBModEntities;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GlacierIceSpikeEntity extends Entity implements GeoEntity {
    public static final int RISE_TIME = 6;
    public static final int REST_TIME = 14;
    public static final int LOWER_TIME = 10;
    private static final float EMERGE_SOUND_VOLUME_MULTIPLIER = 0.625F;
    private static final String TAG_WAIT_TIME = "WaitTime";
    private static final String TAG_RISE_HEIGHT = "RiseHeight";
    private static final String TAG_SPIKE_SCALE = "SpikeScale";
    private static final String TAG_MIRRORED = "Mirrored";
    private static final EntityDataAccessor<Integer> DATA_WAIT_TIME = SynchedEntityData.defineId(GlacierIceSpikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_RISE_HEIGHT = SynchedEntityData.defineId(GlacierIceSpikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SPIKE_SCALE = SynchedEntityData.defineId(GlacierIceSpikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_MIRRORED = SynchedEntityData.defineId(GlacierIceSpikeEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean initializedBaseY;
    private double hiddenBaseY;
    private boolean emergeSoundPlayed;

    public GlacierIceSpikeEntity(EntityType<? extends GlacierIceSpikeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public GlacierIceSpikeEntity(Level level) {
        this(PBModEntities.GLACIER_ICE_SPIKE.get(), level);
    }

    public int getWaitTime() {
        return this.entityData.get(DATA_WAIT_TIME);
    }

    public void setWaitTime(int waitTime) {
        this.entityData.set(DATA_WAIT_TIME, Math.max(0, waitTime));
    }

    public float getRiseHeight() {
        return this.entityData.get(DATA_RISE_HEIGHT);
    }

    public void setRiseHeight(float riseHeight) {
        this.entityData.set(DATA_RISE_HEIGHT, Math.max(0.1F, riseHeight));
    }

    public float getSpikeScale() {
        return this.entityData.get(DATA_SPIKE_SCALE);
    }

    public void setSpikeScale(float spikeScale) {
        this.entityData.set(DATA_SPIKE_SCALE, Math.max(0.1F, spikeScale));
    }

    public boolean isMirrored() {
        return this.entityData.get(DATA_MIRRORED);
    }

    public void setMirrored(boolean mirrored) {
        this.entityData.set(DATA_MIRRORED, mirrored);
    }

    public float getPositionOffset(float partialTicks) {
        float age = this.tickCount + partialTicks;
        int waitTime = this.getWaitTime();
        if (age < waitTime) {
            return -1.0F;
        }

        if (age < waitTime + RISE_TIME) {
            float progress = (age - waitTime) / (float) RISE_TIME;
            return progress - 1.0F;
        }

        if (age < waitTime + RISE_TIME + REST_TIME) {
            return 0.0F;
        }

        float lowerProgress = (age - (waitTime + RISE_TIME + REST_TIME)) / (float) LOWER_TIME;
        if (lowerProgress < 1.0F) {
            return -lowerProgress;
        }

        return -1.0F;
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);

        if (!this.initializedBaseY) {
            this.hiddenBaseY = this.getY();
            this.initializedBaseY = true;
        }

        float positionOffset = this.getPositionOffset(0.0F);
        this.setPos(this.getX(), this.hiddenBaseY + this.getRiseHeight() * (positionOffset + 1.0F), this.getZ());

        if (!this.level().isClientSide && !this.emergeSoundPlayed && this.tickCount >= this.getWaitTime()) {
            this.emergeSoundPlayed = true;
            this.level().playSound(null, this.blockPosition(), SoundRegistry.ICE_SPIKE_EMERGE.get(), SoundSource.NEUTRAL,
                    EMERGE_SOUND_VOLUME_MULTIPLIER * this.getSpikeScale(), Mth.randomBetweenInclusive(this.random, 6, 12) * 0.1F);
        }

        if (this.tickCount > this.getWaitTime() + RISE_TIME + REST_TIME + LOWER_TIME) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_WAIT_TIME, 0);
        builder.define(DATA_RISE_HEIGHT, 1.4F);
        builder.define(DATA_SPIKE_SCALE, 1.0F);
        builder.define(DATA_MIRRORED, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setWaitTime(tag.getInt(TAG_WAIT_TIME));
        this.setRiseHeight(tag.getFloat(TAG_RISE_HEIGHT));
        this.setSpikeScale(tag.contains(TAG_SPIKE_SCALE) ? tag.getFloat(TAG_SPIKE_SCALE) : 1.0F);
        this.setMirrored(tag.getBoolean(TAG_MIRRORED));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(TAG_WAIT_TIME, this.getWaitTime());
        tag.putFloat(TAG_RISE_HEIGHT, this.getRiseHeight());
        tag.putFloat(TAG_SPIKE_SCALE, this.getSpikeScale());
        tag.putBoolean(TAG_MIRRORED, this.isMirrored());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}