package com.rave.projectbabylonweapons.world.entity.effect;

import com.rave.projectbabylonweapons.init.PBModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public final class IceSpikeEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(IceSpikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(IceSpikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> WAIT_TIME = SynchedEntityData.defineId(IceSpikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TILT_X = SynchedEntityData.defineId(IceSpikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TILT_Z = SynchedEntityData.defineId(IceSpikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> REST_TIME = SynchedEntityData.defineId(IceSpikeEntity.class, EntityDataSerializers.INT);

    public IceSpikeEntity(EntityType<? extends IceSpikeEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public IceSpikeEntity(Level level) { this(PBModEntities.ICE_SPIKE.get(), level); }
    public int getVariant() { return entityData.get(VARIANT); }
    public void setVariant(int variant) { entityData.set(VARIANT, Mth.clamp(variant, 0, 2)); }
    public float getSpikeScale() { return entityData.get(SCALE); }
    public void setSpikeScale(float scale) { entityData.set(SCALE, Float.isFinite(scale) ? Mth.clamp(scale, .5F, 2) : 1); }
    public int getWaitTime() { return entityData.get(WAIT_TIME); }
    public void setWaitTime(int ticks) { entityData.set(WAIT_TIME, Mth.clamp(ticks, 0, IceSpikeAnimation.MAX_WAIT_TIME)); }
    public float getTiltX() { return entityData.get(TILT_X); }
    public float getTiltZ() { return entityData.get(TILT_Z); }
    public void setTilt(float x, float z) {
        entityData.set(TILT_X, Float.isFinite(x) ? Mth.clamp(x, -IceSpikeAnimation.MAX_TILT, IceSpikeAnimation.MAX_TILT) : 0);
        entityData.set(TILT_Z, Float.isFinite(z) ? Mth.clamp(z, -IceSpikeAnimation.MAX_TILT, IceSpikeAnimation.MAX_TILT) : 0);
    }
    public int getRestTime() { return entityData.get(REST_TIME); }
    public void setRestTime(int ticks) { entityData.set(REST_TIME, Mth.clamp(ticks, 0, 1200)); }
    public float getExtension(float partialTick) { return IceSpikeAnimation.extension(tickCount + partialTick - getWaitTime(), getRestTime()); }

    @Override
    public AABB getBoundingBoxForCulling() { return getBoundingBox().inflate(2, 2, 2); }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0, 0, 0);
        if (!level().isClientSide() && tickCount >= getWaitTime() + IceSpikeAnimation.lifetime(getRestTime())) discard();
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(VARIANT, 0);
        entityData.define(SCALE, 1.0F);
        entityData.define(WAIT_TIME, 0);
        entityData.define(TILT_X, 0.0F);
        entityData.define(TILT_Z, 0.0F);
        entityData.define(REST_TIME, IceSpikeAnimation.REST_TIME);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setVariant(tag.getInt("Variant"));
        setSpikeScale(tag.contains("Scale") ? tag.getFloat("Scale") : 1);
        setWaitTime(tag.getInt("WaitTime"));
        setTilt(tag.getFloat("TiltX"), tag.getFloat("TiltZ"));
        setRestTime(tag.contains("RestTime") ? tag.getInt("RestTime") : IceSpikeAnimation.REST_TIME);
        tickCount = Mth.clamp(tag.getInt("Age"), 0, getWaitTime() + IceSpikeAnimation.lifetime(getRestTime()));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Variant", getVariant());
        tag.putFloat("Scale", getSpikeScale());
        tag.putInt("WaitTime", getWaitTime());
        tag.putFloat("TiltX", getTiltX());
        tag.putFloat("TiltZ", getTiltZ());
        tag.putInt("Age", tickCount);
        tag.putInt("RestTime", getRestTime());
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(getVariant());
        buffer.writeFloat(getSpikeScale());
        buffer.writeVarInt(getWaitTime());
        buffer.writeFloat(getTiltX());
        buffer.writeFloat(getTiltZ());
        buffer.writeVarInt(tickCount);
        buffer.writeVarInt(getRestTime());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        setVariant(buffer.readVarInt());
        setSpikeScale(buffer.readFloat());
        setWaitTime(buffer.readVarInt());
        setTilt(buffer.readFloat(), buffer.readFloat());
        int age = buffer.readVarInt();
        setRestTime(buffer.readVarInt());
        tickCount = Mth.clamp(age, 0, getWaitTime() + IceSpikeAnimation.lifetime(getRestTime()));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
