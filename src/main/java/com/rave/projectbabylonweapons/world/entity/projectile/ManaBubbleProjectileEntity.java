package com.rave.projectbabylonweapons.world.entity.projectile;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.handler.MagicMeleeWeaponHelper;
import com.rave.projectbabylonweapons.handler.StaffMagicArmorHelper;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
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
import yesman.epicfight.world.damagesource.StunType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ManaBubbleProjectileEntity extends Projectile implements GeoEntity {
    public enum VisualPreset {
        BASIC(
                ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "geo/basic_spell_projectile.geo.json"),
                ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "textures/entity/projectile/basic_spell_projectile.png"),
                ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "animations/projectile_loop_animation.geo.json"),
                "animation.basic_spell_projectile.idle"
        );
        private final ResourceLocation modelResource;
        private final ResourceLocation textureResource;
        private final ResourceLocation animationResource;
        private final String animationName;
        VisualPreset(ResourceLocation modelResource, ResourceLocation textureResource, ResourceLocation animationResource,
                     String animationName) {
            this.modelResource = modelResource;
            this.textureResource = textureResource;
            this.animationResource = animationResource;
            this.animationName = animationName;
        }
        public ResourceLocation modelResource() {
            return this.modelResource;
        }
        public ResourceLocation textureResource() {
            return this.textureResource;
        }
        public ResourceLocation animationResource() {
            return this.animationResource;
        }
        public String animationName() {
            return this.animationName;
        }
        public static VisualPreset fromOrdinal(int ordinal) {
            return BASIC;
        }
    }
    private static final float INERTIA = 0.99F;
    private static final int DEFAULT_TRAIL_COLOR = 0xB970FF;
    private static final float DEFAULT_RENDER_SCALE = 10.0F;
    private static final double EFFECT_RADIUS_PER_SCALE = 0.23D;
    private static final String TAG_RAW_MAGIC_DAMAGE = "RawMagicDamage";
    private static final String TAG_MAGIC_ARMOR_NEGATION = "MagicArmorNegation";
    private static final String TAG_IMPACT = "Impact";
    private static final String TAG_DAMAGE_TYPE = "MagicDamageType";
    private static final String TAG_STUN_TYPE = "StunType";
    private static final String TAG_LIFETIME = "Lifetime";
    private static final String TAG_SOURCE_WEAPON = "SourceWeapon";
    private static final String TAG_TRAIL_COLOR = "TrailColor";
    private static final String TAG_RENDER_SCALE = "RenderScale";
    private static final String TAG_DRAG_STRENGTH = "DragStrength";
    private static final String TAG_VISUAL_PRESET = "VisualPreset";
    private static final EntityDataAccessor<Integer> DATA_TRAIL_COLOR = SynchedEntityData.defineId(ManaBubbleProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_RENDER_SCALE = SynchedEntityData.defineId(ManaBubbleProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_VISUAL_PRESET = SynchedEntityData.defineId(ManaBubbleProjectileEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Set<UUID> damagedTargets = new HashSet<>();

    private float rawMagicDamage;
    private float magicArmorNegation;
    private float impact;
    private String magicDamageTypeId = "";
    private String stunTypeName = "";
    private int maxLifetime = 90;
    private float dragStrength = 0.9F;
    private ItemStack sourceWeapon = ItemStack.EMPTY;

    public ManaBubbleProjectileEntity(EntityType<? extends ManaBubbleProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public ManaBubbleProjectileEntity(Level level) {
        this(PBModEntities.MANA_BUBBLE_PROJECTILE.get(), level);
    }

    public void configureBubble(LivingEntity owner, ItemStack sourceWeapon, ResourceKey<DamageType> magicDamageType,
                                float rawMagicDamage, float magicArmorNegation, float impact, StunType stunType,
                                int maxLifetime, int trailColor, VisualPreset visualPreset, float renderScale,
                                float dragStrength) {
        this.setOwner(owner);
        this.sourceWeapon = sourceWeapon.copy();
        this.rawMagicDamage = rawMagicDamage;
        this.magicArmorNegation = magicArmorNegation;
        this.impact = impact;
        this.magicDamageTypeId = magicDamageType.location().toString();
        this.stunTypeName = stunType != null ? stunType.name() : "";
        this.maxLifetime = Math.max(1, maxLifetime);
        this.setTrailColor(trailColor);
        this.setVisualPreset(visualPreset);
        this.setRenderScale(renderScale);
        this.dragStrength = Math.max(0.0F, dragStrength);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TRAIL_COLOR, DEFAULT_TRAIL_COLOR);
        builder.define(DATA_RENDER_SCALE, DEFAULT_RENDER_SCALE);
        builder.define(DATA_VISUAL_PRESET, VisualPreset.BASIC.ordinal());
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6D) {
            if (this.tickCount > this.maxLifetime) {
                this.discard();
            }
            return;
        }

        if (this.level().isClientSide) {
            this.spawnClientParticles();
        } else {
            this.affectEntitiesAlongPath(movement);
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
        // Entity contacts are handled by affectEntitiesAlongPath so the bubble can keep travelling.
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide) {
            PhotonWeaponEffectHelper.spawnManaBubbleBasicImpact(this, result.getLocation());
        }
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && !this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(TAG_RAW_MAGIC_DAMAGE, this.rawMagicDamage);
        tag.putFloat(TAG_MAGIC_ARMOR_NEGATION, this.magicArmorNegation);
        tag.putFloat(TAG_IMPACT, this.impact);
        tag.putString(TAG_DAMAGE_TYPE, this.magicDamageTypeId);
        tag.putString(TAG_STUN_TYPE, this.stunTypeName);
        tag.putInt(TAG_LIFETIME, this.maxLifetime);
        tag.putInt(TAG_TRAIL_COLOR, this.getTrailColor());
        tag.putFloat(TAG_RENDER_SCALE, this.getRenderScale());
        tag.putFloat(TAG_DRAG_STRENGTH, this.dragStrength);
        tag.putInt(TAG_VISUAL_PRESET, this.getVisualPreset().ordinal());
        if (!this.sourceWeapon.isEmpty()) {
            tag.put(TAG_SOURCE_WEAPON, this.sourceWeapon.save(this.registryAccess()));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.rawMagicDamage = tag.getFloat(TAG_RAW_MAGIC_DAMAGE);
        this.magicArmorNegation = tag.getFloat(TAG_MAGIC_ARMOR_NEGATION);
        this.impact = tag.getFloat(TAG_IMPACT);
        this.magicDamageTypeId = tag.getString(TAG_DAMAGE_TYPE);
        this.stunTypeName = tag.getString(TAG_STUN_TYPE);
        this.maxLifetime = Math.max(1, tag.getInt(TAG_LIFETIME));
        this.setTrailColor(tag.contains(TAG_TRAIL_COLOR) ? tag.getInt(TAG_TRAIL_COLOR) : DEFAULT_TRAIL_COLOR);
        this.setRenderScale(tag.contains(TAG_RENDER_SCALE) ? tag.getFloat(TAG_RENDER_SCALE) : DEFAULT_RENDER_SCALE);
        this.dragStrength = tag.contains(TAG_DRAG_STRENGTH) ? Math.max(0.0F, tag.getFloat(TAG_DRAG_STRENGTH)) : 0.9F;
        this.setVisualPreset(VisualPreset.fromOrdinal(tag.getInt(TAG_VISUAL_PRESET)));
        if (tag.contains(TAG_SOURCE_WEAPON)) {
            this.sourceWeapon = ItemStack.parse(this.registryAccess(), tag.getCompound(TAG_SOURCE_WEAPON)).orElse(ItemStack.EMPTY);
        }
    }

    public int getTrailColor() {
        return this.entityData.get(DATA_TRAIL_COLOR);
    }

    public void setTrailColor(int trailColor) {
        this.entityData.set(DATA_TRAIL_COLOR, trailColor);
    }

    public float getTrailRed() {
        return ((this.getTrailColor() >> 16) & 0xFF) / 255.0F;
    }

    public float getTrailGreen() {
        return ((this.getTrailColor() >> 8) & 0xFF) / 255.0F;
    }

    public float getTrailBlue() {
        return (this.getTrailColor() & 0xFF) / 255.0F;
    }

    public float getRenderScale() {
        return this.entityData.get(DATA_RENDER_SCALE);
    }

    public void setRenderScale(float renderScale) {
        this.entityData.set(DATA_RENDER_SCALE, Math.max(1.0F, renderScale));
    }

    public VisualPreset getVisualPreset() {
        return VisualPreset.fromOrdinal(this.entityData.get(DATA_VISUAL_PRESET));
    }

    public void setVisualPreset(VisualPreset visualPreset) {
        this.entityData.set(DATA_VISUAL_PRESET, visualPreset.ordinal());
    }

    private void spawnClientParticles() {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-5D) {
            return;
        }

        PhotonWeaponEffectHelper.spawnManaBubbleBasicFlight(this, movement);
    }

    private void affectEntitiesAlongPath(Vec3 movement) {
        if (!(this.getOwner() instanceof LivingEntity owner)) {
            return;
        }

        double effectRadius = this.getEffectRadius();
        AABB sweepBox = this.getBoundingBox().expandTowards(movement).inflate(effectRadius);
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, sweepBox, entity -> entity.isAlive() && this.canHitEntity(entity))) {
            this.dragTarget(target, movement);
            if (this.damagedTargets.add(target.getUUID())) {
                this.damageTarget(owner, target);
            }
        }
    }

    private void dragTarget(LivingEntity target, Vec3 movement) {
        Vec3 dragVector = movement.scale(this.dragStrength);
        target.move(MoverType.SELF, dragVector);
        target.setDeltaMovement(target.getDeltaMovement().scale(0.35D).add(dragVector));
        target.hasImpulse = true;
        target.hurtMarked = true;
        target.fallDistance = 0.0F;
    }

    private void damageTarget(LivingEntity owner, LivingEntity target) {
        float schoolResistMultiplier = 1.0F;
        if (this.sourceWeapon.getItem() instanceof MagicMeleeWeapon magicWeapon) {
            schoolResistMultiplier = magicWeapon.getSchoolResistMultiplier(target);
        }

        float adjustedMagicDamage = StaffMagicArmorHelper.applyAdjustedMagicDamage(
                target,
                this.rawMagicDamage,
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

        int originalInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        try {
            target.hurt(magicDamageSource, adjustedMagicDamage);
        } finally {
            target.invulnerableTime = originalInvulnerableTime;
        }
    }

    private double getEffectRadius() {
        return Math.max(0.8D, this.getRenderScale() * EFFECT_RADIUS_PER_SCALE);
    }

    private ResourceKey<DamageType> resolveMagicDamageType() {
        ResourceLocation damageTypeLocation = this.magicDamageTypeId.isEmpty()
                ? ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "evocation_magic")
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
        controllers.add(new AnimationController<>(this, "loop_controller", 0, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenLoop(this.getVisualPreset().animationName()));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

