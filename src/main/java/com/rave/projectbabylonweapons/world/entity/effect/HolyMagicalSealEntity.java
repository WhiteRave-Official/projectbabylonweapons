package com.rave.projectbabylonweapons.world.entity.effect;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
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

public class HolyMagicalSealEntity extends Entity implements GeoEntity {
    private static final RawAnimation ACTIVE_ANIMATION = RawAnimation.begin()
            .thenPlay("spawn_animation")
            .thenLoop("loop_animation");
    private static final RawAnimation DESPAWN_ANIMATION = RawAnimation.begin().thenPlay("despawn_animation");
    private static final int DEFAULT_ACTIVE_DURATION_TICKS = 16 * 20;
    private static final int DESPAWN_DURATION_TICKS = 3 * 20;
    private static final int HEAL_INTERVAL_TICKS = 2 * 20;
    private static final int ABSORPTION_INTERVAL_TICKS = 8 * 20;
    private static final int ABSORPTION_DURATION_TICKS = 8 * 20;
    private static final float HEAL_PERCENT = 0.05F;
    private static final float ABSORPTION_PERCENT = 0.35F;
    private static final double Y_OFFSET = 0.02D;
    private static final String TAG_TARGET_UUID = "TargetUuid";
    private static final String TAG_CASTER_UUID = "CasterUuid";
    private static final String TAG_EXPIRES_AT = "ExpiresAt";
    private static final String TAG_NEXT_HEAL_AT = "NextHealAt";
    private static final String TAG_NEXT_ABSORPTION_AT = "NextAbsorptionAt";
    private static final String TAG_ABSORPTION_EXPIRES_AT = "AbsorptionExpiresAt";
    private static final String TAG_GRANTED_ABSORPTION = "GrantedAbsorption";
    private static final String TAG_DESPAWNING = "Despawning";
    private static final EntityDataAccessor<Boolean> DATA_DESPAWNING = SynchedEntityData.defineId(HolyMagicalSealEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID targetUuid;
    private UUID casterUuid;
    private long expiresAt;
    private long nextHealAt;
    private long nextAbsorptionAt;
    private long absorptionExpiresAt;
    private float grantedAbsorption;

    public HolyMagicalSealEntity(EntityType<? extends HolyMagicalSealEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public HolyMagicalSealEntity(Level level) {
        this(PBModEntities.HOLY_MAGICAL_SEAL.get(), level);
    }

    public void configure(ServerPlayer caster, ServerPlayer target) {
        this.configure(caster, target, DEFAULT_ACTIVE_DURATION_TICKS);
    }

    public void configure(ServerPlayer caster, ServerPlayer target, int activeDurationTicks) {
        this.casterUuid = caster.getUUID();
        this.targetUuid = target.getUUID();
        long gameTime = caster.level().getGameTime();
        int boundedDuration = Mth.clamp(activeDurationTicks, 1, 20 * 60 * 10);
        this.expiresAt = gameTime + boundedDuration;
        this.nextHealAt = gameTime;
        this.nextAbsorptionAt = gameTime;
        this.absorptionExpiresAt = -1L;
        this.grantedAbsorption = 0.0F;
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

    public void clearEffectsAndDiscard() {
        if (this.level() instanceof ServerLevel serverLevel) {
            ServerPlayer target = this.resolveTarget(serverLevel);
            if (target != null) {
                this.clearGrantedAbsorption(target);
            }
        }
        this.discard();
    }

    public boolean isDespawning() {
        return this.entityData.get(DATA_DESPAWNING);
    }

    public void setDespawning(boolean despawning) {
        this.entityData.set(DATA_DESPAWNING, despawning);
    }

    public ResourceLocation getModelResource() {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/holy_magical_seal.geo.json");
    }

    public ResourceLocation getTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/holy_magical_seal.png");
    }

    public ResourceLocation getAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/holy_magical_seal_animations.animation.json");
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerPlayer target = this.resolveTarget(serverLevel);
        long gameTime = serverLevel.getGameTime();

        if (target == null || !target.isAlive() || target.isRemoved()) {
            this.beginDespawn(gameTime, null);
        } else if (this.getVehicle() != target || !this.isPassenger()) {
            this.beginDespawn(gameTime, target);
        } else {
            if (!this.isDespawning()) {
                if (gameTime >= this.nextHealAt) {
                    this.healTarget(target);
                    this.nextHealAt = gameTime + HEAL_INTERVAL_TICKS;
                }
                if (gameTime >= this.nextAbsorptionAt) {
                    this.pulseAbsorption(target, gameTime);
                    this.nextAbsorptionAt = gameTime + ABSORPTION_INTERVAL_TICKS;
                }
                if (gameTime >= this.expiresAt) {
                    this.beginDespawn(gameTime, target);
                }
            }
        }

        if (target != null && this.absorptionExpiresAt > 0L && gameTime >= this.absorptionExpiresAt) {
            this.clearGrantedAbsorption(target);
            this.absorptionExpiresAt = -1L;
        }

        if (this.isDespawning() && gameTime >= this.expiresAt + DESPAWN_DURATION_TICKS) {
            if (target != null) {
                this.clearGrantedAbsorption(target);
            }
            this.discard();
        }
    }
    private void pulseTarget(ServerPlayer target, long gameTime) {
        float maxHealth = target.getMaxHealth();
        if (maxHealth <= 0.0F) {
            return;
        }

        if (target.getHealth() < maxHealth) {
            float healthBefore = target.getHealth();
            target.heal(maxHealth * HEAL_PERCENT);
            if (target.getHealth() > healthBefore) {
                WeaponVisualEffectHelper.playBlessingHealPulse(target);
            }
            return;
        }

        this.applyAbsorption(target, gameTime, maxHealth * ABSORPTION_PERCENT);
    }

    private void healTarget(ServerPlayer target) {
        float maxHealth = target.getMaxHealth();
        if (maxHealth <= 0.0F || target.getHealth() >= maxHealth) {
            return;
        }

        float healthBefore = target.getHealth();
        target.heal(maxHealth * HEAL_PERCENT);
        if (target.getHealth() > healthBefore) {
            WeaponVisualEffectHelper.playBlessingHealPulse(target);
        }
    }

    private void pulseAbsorption(ServerPlayer target, long gameTime) {
        float maxHealth = target.getMaxHealth();
        if (maxHealth <= 0.0F || target.getHealth() < maxHealth) {
            return;
        }

        this.applyAbsorption(target, gameTime, maxHealth * ABSORPTION_PERCENT);
        WeaponVisualEffectHelper.playBlessingAbsorptionPulse(target);
    }

    private void applyAbsorption(ServerPlayer target, long gameTime, float desiredAbsorption) {
        float currentAbsorption = target.getAbsorptionAmount();
        float ownedContributionPresent = Math.min(currentAbsorption, this.grantedAbsorption);
        float baseAbsorption = Math.max(0.0F, currentAbsorption - ownedContributionPresent);
        float desiredContribution = Math.max(0.0F, desiredAbsorption - baseAbsorption);
        target.setAbsorptionAmount(baseAbsorption + desiredContribution);
        this.grantedAbsorption = desiredContribution;
        this.absorptionExpiresAt = gameTime + ABSORPTION_DURATION_TICKS;
    }

    private void clearGrantedAbsorption(ServerPlayer target) {
        if (this.grantedAbsorption <= 0.0F) {
            return;
        }

        float currentAbsorption = target.getAbsorptionAmount();
        float ownedContributionPresent = Math.min(currentAbsorption, this.grantedAbsorption);
        target.setAbsorptionAmount(Math.max(0.0F, currentAbsorption - ownedContributionPresent));
        this.grantedAbsorption = 0.0F;
    }

    private void beginDespawn(long gameTime, ServerPlayer target) {
        if (this.isDespawning()) {
            return;
        }

        this.setDespawning(true);
        this.expiresAt = gameTime;
        if (target != null) {
            this.clearGrantedAbsorption(target);
            this.absorptionExpiresAt = -1L;
        }
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
        if (tag.hasUUID(TAG_CASTER_UUID)) {
            this.casterUuid = tag.getUUID(TAG_CASTER_UUID);
        }
        this.expiresAt = tag.getLong(TAG_EXPIRES_AT);
        this.nextHealAt = tag.getLong(TAG_NEXT_HEAL_AT);
        this.nextAbsorptionAt = tag.getLong(TAG_NEXT_ABSORPTION_AT);
        this.absorptionExpiresAt = tag.getLong(TAG_ABSORPTION_EXPIRES_AT);
        this.grantedAbsorption = tag.getFloat(TAG_GRANTED_ABSORPTION);
        this.setDespawning(tag.getBoolean(TAG_DESPAWNING));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.targetUuid != null) {
            tag.putUUID(TAG_TARGET_UUID, this.targetUuid);
        }
        if (this.casterUuid != null) {
            tag.putUUID(TAG_CASTER_UUID, this.casterUuid);
        }
        tag.putLong(TAG_EXPIRES_AT, this.expiresAt);
        tag.putLong(TAG_NEXT_HEAL_AT, this.nextHealAt);
        tag.putLong(TAG_NEXT_ABSORPTION_AT, this.nextAbsorptionAt);
        tag.putLong(TAG_ABSORPTION_EXPIRES_AT, this.absorptionExpiresAt);
        tag.putFloat(TAG_GRANTED_ABSORPTION, this.grantedAbsorption);
        tag.putBoolean(TAG_DESPAWNING, this.isDespawning());
    }

    @Override
    public void remove(RemovalReason removalReason) {
        if (this.level() instanceof ServerLevel serverLevel) {
            ServerPlayer target = this.resolveTarget(serverLevel);
            if (target != null) {
                this.clearGrantedAbsorption(target);
            }
        }
        super.remove(removalReason);
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









