package com.rave.projectbabylonweapons.world.entity.effect;

import com.rave.projectbabylonweapons.init.PBModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;

public class TectonicFallingBlockEntity extends Entity {
    public static final float GRAVITY = 0.1F;

    private static final EntityDataAccessor<BlockState> BLOCK_STATE =
            SynchedEntityData.defineId(TectonicFallingBlockEntity.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Float> ANIM_VY =
            SynchedEntityData.defineId(TectonicFallingBlockEntity.class, EntityDataSerializers.FLOAT);

    public float animY = 0.0F;
    public float prevAnimY = 0.0F;

    public TectonicFallingBlockEntity(EntityType<? extends TectonicFallingBlockEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setBlock(Blocks.DIRT.defaultBlockState());
        this.setAnimVY(0.32F);
    }

    public TectonicFallingBlockEntity(Level level, BlockState blockState, float initialVelocityY) {
        this(PBModEntities.TECTONIC_FALLING_BLOCK.get(), level);
        this.setBlock(blockState);
        this.setAnimVY(initialVelocityY);
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(0.0D, 0.0D, 0.0D);

        this.prevAnimY = this.animY;
        this.animY += this.getAnimVY();
        this.setAnimVY(this.getAnimVY() - GRAVITY);

        if (this.animY < -0.5F || this.tickCount > 30) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BLOCK_STATE, Blocks.DIRT.defaultBlockState());
        builder.define(ANIM_VY, 0.32F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        Tag blockStateTag = compound.get("block");
        if (blockStateTag instanceof CompoundTag blockCompound) {
            BlockState blockState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), blockCompound);
            this.setBlock(blockState);
        }
        this.animY = compound.getFloat("animY");
        this.prevAnimY = compound.getFloat("prevAnimY");
        this.setAnimVY(compound.getFloat("vy"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("block", NbtUtils.writeBlockState(this.getBlock()));
        compound.putFloat("animY", this.animY);
        compound.putFloat("prevAnimY", this.prevAnimY);
        compound.putFloat("vy", this.getAnimVY());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public BlockState getBlock() {
        return this.entityData.get(BLOCK_STATE);
    }

    public void setBlock(BlockState block) {
        this.entityData.set(BLOCK_STATE, block);
    }

    public float getAnimVY() {
        return this.entityData.get(ANIM_VY);
    }

    public void setAnimVY(float vy) {
        this.entityData.set(ANIM_VY, vy);
    }
}
