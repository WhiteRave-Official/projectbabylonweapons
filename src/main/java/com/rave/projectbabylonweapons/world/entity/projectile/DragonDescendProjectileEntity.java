package com.rave.projectbabylonweapons.world.entity.projectile;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.handler.MagicMeleeWeaponHelper;
import com.rave.projectbabylonweapons.handler.StaffMagicArmorHelper;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DragonDescendProjectileEntity extends Projectile implements GeoEntity {
    public enum AnimationMode {
        DIVE,
        FLIGHT;

        public static AnimationMode fromOrdinal(int ordinal) {
            AnimationMode[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DIVE;
        }
    }

    private record TrailSegment(Vec3 position, long expireGameTime) {
    }

    private static final RawAnimation FLIGHT_ANIMATION = RawAnimation.begin().thenLoop("animation.ender_spell_projectile.idle");
    private static final float INERTIA = 0.985F;
    private static final float DEFAULT_RENDER_SCALE = 5.0F;
    private static final int DEFAULT_TRAIL_COLOR = 0x7B4DFF;
    private static final int PREP_LIFETIME = 60;
    private static final int TRAIL_SEGMENT_LIFETIME = 60;
    private static final int TRAIL_DAMAGE_INTERVAL = 8;
    private static final int ROAR_INTERVAL_TICKS = 24;
    private static final double DIRECT_HIT_RADIUS = 1.35D;
    private static final double TRAIL_DAMAGE_RADIUS = 1.5D;
    private static final double TRAIL_VERTICAL_RADIUS = 1.8D;
    private static final String TAG_RAW_MAGIC_DAMAGE = "RawMagicDamage";
    private static final String TAG_TRAIL_MAGIC_DAMAGE = "TrailMagicDamage";
    private static final String TAG_MAGIC_ARMOR_NEGATION = "MagicArmorNegation";
    private static final String TAG_IMPACT = "Impact";
    private static final String TAG_DAMAGE_TYPE = "MagicDamageType";
    private static final String TAG_STUN_TYPE = "StunType";
    private static final String TAG_LIFETIME = "Lifetime";
    private static final String TAG_SOURCE_WEAPON = "SourceWeapon";
    private static final String TAG_TRAIL_COLOR = "TrailColor";
    private static final String TAG_RENDER_SCALE = "RenderScale";
    private static final String TAG_ANIMATION_MODE = "AnimationMode";
    private static final String TAG_NEXT_ROAR_TICK = "NextRoarTick";
    private static final EntityDataAccessor<Integer> DATA_TRAIL_COLOR = SynchedEntityData.defineId(DragonDescendProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_RENDER_SCALE = SynchedEntityData.defineId(DragonDescendProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_ANIMATION_MODE = SynchedEntityData.defineId(DragonDescendProjectileEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Set<UUID> damagedTargets = new HashSet<>();
    private final List<TrailSegment> trailSegments = new ArrayList<>();
    private final Map<UUID, Long> trailDamageCooldowns = new HashMap<>();

    private float rawMagicDamage;
    private float trailMagicDamage;
    private float magicArmorNegation;
    private float impact;
    private String magicDamageTypeId = "";
    private String stunTypeName = "";
    private int maxLifetime = 70;
    private long nextRoarGameTime;
    private ItemStack sourceWeapon = ItemStack.EMPTY;

    public DragonDescendProjectileEntity(EntityType<? extends DragonDescendProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public DragonDescendProjectileEntity(Level level) {
        this(PBModEntities.DRAGON_DESCEND_PROJECTILE.get(), level);
    }


    public void configureProjectile(LivingEntity owner, ItemStack sourceWeapon, ResourceKey<DamageType> magicDamageType,
                                    float rawMagicDamage, float trailMagicDamage, float magicArmorNegation,
                                    float impact, StunType stunType, int maxLifetime, int trailColor, float renderScale) {
        this.setOwner(owner);
        this.sourceWeapon = sourceWeapon.copy();
        this.rawMagicDamage = rawMagicDamage;
        this.trailMagicDamage = trailMagicDamage;
        this.magicArmorNegation = magicArmorNegation;
        this.impact = impact;
        this.magicDamageTypeId = magicDamageType.location().toString();
        this.stunTypeName = stunType != null ? stunType.name() : "";
        this.maxLifetime = Math.max(1, maxLifetime);
        this.setTrailColor(trailColor);
        this.setRenderScale(renderScale);
        this.setAnimationMode(AnimationMode.DIVE);
        this.setDeltaMovement(Vec3.ZERO);
        this.nextRoarGameTime = 0L;
        this.damagedTargets.clear();
        this.trailSegments.clear();
        this.trailDamageCooldowns.clear();
    }

    public void launch(Vec3 spawnPos, Vec3 direction, float speed) {
        Vec3 flatDirection = new Vec3(direction.x, 0.0D, direction.z);
        if (flatDirection.lengthSqr() < 1.0E-6D) {
            flatDirection = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            flatDirection = flatDirection.normalize();
        }

        this.setAnimationMode(AnimationMode.FLIGHT);
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        this.shoot(flatDirection.x, 0.0D, flatDirection.z, speed, 0.0F);
        this.trailSegments.clear();
        this.trailDamageCooldowns.clear();
        if (this.level() instanceof ServerLevel serverLevel) {
            this.nextRoarGameTime = serverLevel.getGameTime();
        }
    }

    public boolean isLaunched() {
        return this.getAnimationMode() == AnimationMode.FLIGHT;
    }

    public AnimationMode getAnimationMode() {
        return AnimationMode.fromOrdinal(this.entityData.get(DATA_ANIMATION_MODE));
    }

    public void setAnimationMode(AnimationMode animationMode) {
        this.entityData.set(DATA_ANIMATION_MODE, animationMode.ordinal());
    }

    public int getTrailColor() {
        return this.entityData.get(DATA_TRAIL_COLOR);
    }

    public void setTrailColor(int trailColor) {
        this.entityData.set(DATA_TRAIL_COLOR, trailColor);
    }

    public float getRenderScale() {
        return this.entityData.get(DATA_RENDER_SCALE);
    }

    public void setRenderScale(float renderScale) {
        this.entityData.set(DATA_RENDER_SCALE, Math.max(1.0F, renderScale));
    }

    public ResourceLocation getModelResource() {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/ender_spell_projectile.geo.json");
    }

    public ResourceLocation getTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/ender_spell_projectile.png");
    }

    public ResourceLocation getAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/ender_projectile_loop.animation.json");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TRAIL_COLOR, DEFAULT_TRAIL_COLOR);
        builder.define(DATA_RENDER_SCALE, DEFAULT_RENDER_SCALE);
        builder.define(DATA_ANIMATION_MODE, AnimationMode.DIVE.ordinal());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getAnimationMode() == AnimationMode.DIVE) {
            this.setDeltaMovement(Vec3.ZERO);
            if (this.level().isClientSide) {
                this.spawnDiveParticles();
            }
            if (this.tickCount > PREP_LIFETIME) {
                this.discard();
            }
            return;
        }

        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6D) {
            if (this.tickCount > this.maxLifetime) {
                this.discard();
            }
            return;
        }

        if (this.level().isClientSide) {
            this.spawnFlightParticles();
        } else {
            this.playFlightRoar();
            this.affectEntitiesAlongPath(movement);
            this.recordTrailSegment();
            this.processTrailDamage();
        }

        HitResult blockHit = this.level().clip(new ClipContext(this.position(), this.position().add(movement), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) blockHit);
            return;
        }

        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        this.updateRotation();
        this.setDeltaMovement(movement.scale(INERTIA));

        if (this.tickCount > this.maxLifetime) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Direct contact is handled by sweeping along the movement vector to preserve the projectile flight.
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.spawnImpactParticles(result.getLocation());
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && !this.level().isClientSide) {
            this.spawnImpactParticles(result.getLocation());
            this.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(TAG_RAW_MAGIC_DAMAGE, this.rawMagicDamage);
        tag.putFloat(TAG_TRAIL_MAGIC_DAMAGE, this.trailMagicDamage);
        tag.putFloat(TAG_MAGIC_ARMOR_NEGATION, this.magicArmorNegation);
        tag.putFloat(TAG_IMPACT, this.impact);
        tag.putString(TAG_DAMAGE_TYPE, this.magicDamageTypeId);
        tag.putString(TAG_STUN_TYPE, this.stunTypeName);
        tag.putInt(TAG_LIFETIME, this.maxLifetime);
        tag.putInt(TAG_TRAIL_COLOR, this.getTrailColor());
        tag.putFloat(TAG_RENDER_SCALE, this.getRenderScale());
        tag.putInt(TAG_ANIMATION_MODE, this.getAnimationMode().ordinal());
        tag.putLong(TAG_NEXT_ROAR_TICK, this.nextRoarGameTime);
        if (!this.sourceWeapon.isEmpty()) {
            tag.put(TAG_SOURCE_WEAPON, this.sourceWeapon.save(this.registryAccess()));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.rawMagicDamage = tag.getFloat(TAG_RAW_MAGIC_DAMAGE);
        this.trailMagicDamage = tag.getFloat(TAG_TRAIL_MAGIC_DAMAGE);
        this.magicArmorNegation = tag.getFloat(TAG_MAGIC_ARMOR_NEGATION);
        this.impact = tag.getFloat(TAG_IMPACT);
        this.magicDamageTypeId = tag.getString(TAG_DAMAGE_TYPE);
        this.stunTypeName = tag.getString(TAG_STUN_TYPE);
        this.maxLifetime = Math.max(1, tag.getInt(TAG_LIFETIME));
        this.setTrailColor(tag.contains(TAG_TRAIL_COLOR) ? tag.getInt(TAG_TRAIL_COLOR) : DEFAULT_TRAIL_COLOR);
        this.setRenderScale(tag.contains(TAG_RENDER_SCALE) ? tag.getFloat(TAG_RENDER_SCALE) : DEFAULT_RENDER_SCALE);
        this.setAnimationMode(AnimationMode.fromOrdinal(tag.getInt(TAG_ANIMATION_MODE)));
        this.nextRoarGameTime = tag.getLong(TAG_NEXT_ROAR_TICK);
        if (tag.contains(TAG_SOURCE_WEAPON)) {
            this.sourceWeapon = ItemStack.parse(this.registryAccess(), tag.getCompound(TAG_SOURCE_WEAPON)).orElse(ItemStack.EMPTY);
        }
    }

    private void affectEntitiesAlongPath(Vec3 movement) {
        if (!(this.getOwner() instanceof LivingEntity owner)) {
            return;
        }

        AABB sweepBox = this.getBoundingBox().expandTowards(movement).inflate(DIRECT_HIT_RADIUS);
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, sweepBox, entity -> entity.isAlive() && this.canHitEntity(entity))) {
            if (target.isAlliedTo(owner)) {
                continue;
            }

            if (this.damagedTargets.add(target.getUUID())) {
                this.damageTarget(owner, target, this.rawMagicDamage);
            }
        }
    }

    private void recordTrailSegment() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 trailPos = this.position();
        long expireTime = serverLevel.getGameTime() + TRAIL_SEGMENT_LIFETIME;
        this.trailSegments.add(new TrailSegment(trailPos, expireTime));
    }

    private void processTrailDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !(this.getOwner() instanceof LivingEntity owner)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        Iterator<TrailSegment> iterator = this.trailSegments.iterator();
        while (iterator.hasNext()) {
            TrailSegment segment = iterator.next();
            long remainingTicks = segment.expireGameTime() - gameTime;
            if (remainingTicks <= 0L) {
                iterator.remove();
                continue;
            }

            AABB damageArea = new AABB(
                    segment.position().x - TRAIL_DAMAGE_RADIUS,
                    segment.position().y - 0.6D,
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

                this.trailDamageCooldowns.put(target.getUUID(), gameTime + TRAIL_DAMAGE_INTERVAL);
                this.damageTarget(owner, target, this.trailMagicDamage);
            }
        }
    }

    private void damageTarget(LivingEntity owner, LivingEntity target, float baseDamage) {
        if (baseDamage <= 0.0F) {
            return;
        }

        float schoolResistMultiplier = 1.0F;
        if (this.sourceWeapon.getItem() instanceof MagicMeleeWeapon magicWeapon) {
            schoolResistMultiplier = magicWeapon.getSchoolResistMultiplier(target);
        }

        float adjustedMagicDamage = StaffMagicArmorHelper.applyAdjustedMagicDamage(
                target,
                baseDamage,
                schoolResistMultiplier,
                this.magicArmorNegation
        );
        if (adjustedMagicDamage <= 0.0F) {
            return;
        }

        DamageSource magicDamageSource = MagicMeleeWeaponHelper.createMagicProjectileDamageSource(
                owner,
                this,
                this.sourceWeapon,
                this.resolveMagicDamageType(),
                this.magicArmorNegation,
                this.impact,
                this.resolveStunType()
        );
        if (magicDamageSource instanceof EpicFightDamageSource epicFightDamageSource) {
            epicFightDamageSource.addRuntimeTag(EpicFightDamageTypeTags.WEAPON_INNATE);
        }

        int originalInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        try {
            target.hurt(magicDamageSource, adjustedMagicDamage);
        } finally {
            target.invulnerableTime = originalInvulnerableTime;
        }
    }

    private void playFlightRoar() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        if (gameTime < this.nextRoarGameTime) {
            return;
        }

        serverLevel.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.PLAYERS,
                1.1F,
                1.0F
        );
        this.nextRoarGameTime = gameTime + ROAR_INTERVAL_TICKS;
    }

    private void spawnDiveParticles() {
        if ((this.tickCount & 1) != 0) {
            return;
        }

        this.level().addParticle(ParticleTypes.PORTAL,
                this.getX(),
                this.getY() + 0.4D,
                this.getZ(),
                0.0D, 0.03D, 0.0D);
        this.level().addParticle(ParticleTypes.DRAGON_BREATH,
                this.getX(),
                this.getY() + 0.2D,
                this.getZ(),
                0.0D, 0.01D, 0.0D);
    }

    private void spawnFlightParticles() {
        PhotonWeaponEffectHelper.spawnDragonDescendFlight(this, this.getDeltaMovement());
    }

    private void spawnImpactParticles(Vec3 hitPos) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ParticleTypes.PORTAL,
                hitPos.x, hitPos.y, hitPos.z,
                34,
                0.45D, 0.35D, 0.45D,
                0.08D);
        serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                hitPos.x, hitPos.y, hitPos.z,
                24,
                0.28D, 0.2D, 0.28D,
                0.03D);
    }

    private ResourceKey<DamageType> resolveMagicDamageType() {
        ResourceLocation damageTypeLocation = this.magicDamageTypeId.isEmpty()
                ? ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "ender_magic")
                : ResourceLocation.parse(this.magicDamageTypeId);
        return ResourceKey.create(Registries.DAMAGE_TYPE, damageTypeLocation);
    }

    private StunType resolveStunType() {
        if (this.stunTypeName == null || this.stunTypeName.isEmpty()) {
            return null;
        }

        try {
            return StunType.valueOf(this.stunTypeName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "dragon_controller", 0, state -> {
            if (this.getAnimationMode() == AnimationMode.DIVE) {
                state.getController().forceAnimationReset();
                return PlayState.STOP;
            }

            state.getController().setAnimation(FLIGHT_ANIMATION);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
