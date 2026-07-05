package com.rave.projectbabylonweapons.world.entity.projectile;


import com.rave.projectbabylonweapons.handler.BattleWandPassiveHooks;
import com.rave.projectbabylonweapons.handler.MagicMeleeWeaponHelper;
import com.rave.projectbabylonweapons.handler.StaffMagicArmorHelper;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.init.PBModParticles;
import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import yesman.epicfight.world.damagesource.StunType;

public class BasicSpellProjectileEntity extends Projectile implements GeoEntity {
    private static final RawAnimation LOOP_ANIMATION = RawAnimation.begin().thenLoop("animation.basic_spell_projectile.idle");
    private static final float INERTIA = 0.99F;
    private static final int DEFAULT_TRAIL_COLOR = 0xB970FF;
    private static final int IMPACT_DUST_LARGE_COUNT = 20;
    private static final int IMPACT_DUST_SMALL_COUNT = 12;
    private static final String TAG_RAW_MAGIC_DAMAGE = "RawMagicDamage";
    private static final String TAG_MAGIC_ARMOR_NEGATION = "MagicArmorNegation";
    private static final String TAG_IMPACT = "Impact";
    private static final String TAG_DAMAGE_TYPE = "MagicDamageType";
    private static final String TAG_STUN_TYPE = "StunType";
    private static final String TAG_LIFETIME = "Lifetime";
    private static final String TAG_SOURCE_WEAPON = "SourceWeapon";
    private static final String TAG_TRAIL_COLOR = "TrailColor";
    private static final String TAG_PIERCING = "Piercing";
    private static final String TAG_REMAINING_RICOCHETS = "RemainingRicochets";
    private static final String TAG_VISUAL_SCALE = "VisualScale";
    private static final String TAG_BASIC_WAND_ATTACK = "BasicWandAttack";
    private static final String TAG_HOMING_ENABLED = "HomingEnabled";
    private static final EntityDataAccessor<Integer> DATA_TRAIL_COLOR = SynchedEntityData.defineId(BasicSpellProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_VISUAL_SCALE = SynchedEntityData.defineId(BasicSpellProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final double HOMING_RANGE = 14.0D;
    private static final double HOMING_TARGET_VERTICAL_OFFSET = 0.45D;
    private static final float HOMING_TURN_RATE = 0.18F;
    private static final double MAX_RENDER_DISTANCE = 72.0D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private float rawMagicDamage;
    private float magicArmorNegation;
    private float impact;
    private String magicDamageTypeId = "";
    private String stunTypeName = "";
    private int maxLifetime = 200;
    private ItemStack sourceWeapon = ItemStack.EMPTY;
    private boolean trailSpawnedClient;
    private boolean piercing;
    private int remainingRicochets;
    private boolean basicWandAttack;
    private boolean homingEnabled;

    public BasicSpellProjectileEntity(EntityType<? extends BasicSpellProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public BasicSpellProjectileEntity(Level level) {
        this(PBModEntities.BASIC_SPELL_PROJECTILE.get(), level);
    }

    public void configureMagicProjectile(LivingEntity owner, ItemStack sourceWeapon, ResourceKey<DamageType> magicDamageType,
                                         float rawMagicDamage, float magicArmorNegation, float impact,
                                         StunType stunType, int maxLifetime, int trailColor) {
        this.setOwner(owner);
        this.sourceWeapon = sourceWeapon.copy();
        this.rawMagicDamage = rawMagicDamage;
        this.magicArmorNegation = magicArmorNegation;
        this.impact = impact;
        this.magicDamageTypeId = magicDamageType.location().toString();
        this.stunTypeName = stunType != null ? stunType.name() : "";
        this.maxLifetime = Math.max(1, maxLifetime);
        this.setTrailColor(trailColor);
    }

    public ItemStack getSourceWeapon() {
        return this.sourceWeapon.copy();
    }

    public float getRawMagicDamage() {
        return this.rawMagicDamage;
    }

    public float getMagicArmorNegationValue() {
        return this.magicArmorNegation;
    }

    public float getImpactValue() {
        return this.impact;
    }

    public ResourceKey<DamageType> getMagicDamageTypeKey() {
        return this.resolveMagicDamageType();
    }

    public StunType getProjectileStunType() {
        return this.resolveStunType();
    }

    public float getVisualScale() {
        return this.entityData.get(DATA_VISUAL_SCALE);
    }

    public void setVisualScale(float visualScale) {
        this.entityData.set(DATA_VISUAL_SCALE, Math.max(0.1F, visualScale));
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public boolean isPiercing() {
        return this.piercing;
    }

    public int getRemainingRicochets() {
        return this.remainingRicochets;
    }

    public void setRemainingRicochets(int remainingRicochets) {
        this.remainingRicochets = Math.max(0, remainingRicochets);
    }

    public void multiplyRawMagicDamage(float multiplier) {
        if (multiplier <= 0.0F) {
            return;
        }

        this.rawMagicDamage *= multiplier;
    }

    public boolean isBasicWandAttack() {
        return this.basicWandAttack;
    }

    public void setBasicWandAttack(boolean basicWandAttack) {
        this.basicWandAttack = basicWandAttack;
    }

    public boolean isHomingEnabled() {
        return this.homingEnabled;
    }

    public void setHomingEnabled(boolean homingEnabled) {
        this.homingEnabled = homingEnabled;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TRAIL_COLOR, DEFAULT_TRAIL_COLOR);
        builder.define(DATA_VISUAL_SCALE, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.spawnClientTrailOnce();
            this.spawnClientParticles();
        } else {
            this.applyHoming();
        }

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        Vec3 movement = this.getDeltaMovement();
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
        super.onHitEntity(result);

        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target && this.getOwner() instanceof LivingEntity owner) {
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
            if (adjustedMagicDamage > 0.0F) {
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

                BattleWandPassiveHooks.onProjectileHitEntity(this, target, owner, adjustedMagicDamage, magicDamageSource);
            }
        }

        if (!this.level().isClientSide && !this.piercing) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide && result.getType() == HitResult.Type.BLOCK
                && BattleWandPassiveHooks.tryHandleBlockHit(this, (BlockHitResult) result)) {
            return;
        }

        super.onHit(result);
        if (!this.level().isClientSide && result.getType() == HitResult.Type.BLOCK) {
            this.discard();
        }
        if (!this.level().isClientSide && result.getType() == HitResult.Type.ENTITY && !this.piercing) {
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
        tag.putBoolean(TAG_PIERCING, this.piercing);
        tag.putInt(TAG_REMAINING_RICOCHETS, this.remainingRicochets);
        tag.putFloat(TAG_VISUAL_SCALE, this.getVisualScale());
        tag.putBoolean(TAG_BASIC_WAND_ATTACK, this.basicWandAttack);
        tag.putBoolean(TAG_HOMING_ENABLED, this.homingEnabled);
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
        this.piercing = tag.getBoolean(TAG_PIERCING);
        this.remainingRicochets = tag.getInt(TAG_REMAINING_RICOCHETS);
        this.basicWandAttack = tag.getBoolean(TAG_BASIC_WAND_ATTACK);
        this.homingEnabled = tag.getBoolean(TAG_HOMING_ENABLED);
        if (tag.contains(TAG_VISUAL_SCALE)) {
            this.setVisualScale(tag.getFloat(TAG_VISUAL_SCALE));
        }
        if (tag.contains(TAG_TRAIL_COLOR)) {
            this.setTrailColor(tag.getInt(TAG_TRAIL_COLOR));
        }
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

    private void spawnClientTrailOnce() {
        if (this.trailSpawnedClient || !this.isAlive()) {
            return;
        }

        this.level().addParticle(PBModParticles.BASIC_SPELL_PROJECTILE_TRAIL.get(), this.getId(), 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        this.trailSpawnedClient = true;
    }

    protected void spawnClientParticles() {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-5D) {
            return;
        }

        Vec3 normalized = movement.normalize();
        Vec3 center = this.position().subtract(normalized.scale(0.35D));
        Vec3 right = new Vec3(-normalized.z, 0.0D, normalized.x);
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        Vec3 up = normalized.cross(right);
        if (up.lengthSqr() < 1.0E-6D) {
            up = new Vec3(0.0D, 1.0D, 0.0D);
        } else {
            up = up.normalize();
        }

        float angle = this.tickCount * 0.6F;
        Vec3 spiralOffset = right.scale(Math.cos(angle) * 0.12D).add(up.scale(Math.sin(angle) * 0.12D));
        Vec3 oppositeOffset = spiralOffset.scale(-1.0D);
        Vector3f color = new Vector3f(this.getTrailRed(), this.getTrailGreen(), this.getTrailBlue());

        this.level().addParticle(new DustParticleOptions(color, 1.0F), center.x + spiralOffset.x, center.y + spiralOffset.y, center.z + spiralOffset.z, 0.0D, 0.0D, 0.0D);
        this.level().addParticle(new DustParticleOptions(color, 0.75F), center.x + oppositeOffset.x, center.y + oppositeOffset.y, center.z + oppositeOffset.z, 0.0D, 0.0D, 0.0D);
    }

    protected void spawnImpactParticles(Vec3 hitPos) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vector3f color = new Vector3f(this.getTrailRed(), this.getTrailGreen(), this.getTrailBlue());
        serverLevel.sendParticles(new DustParticleOptions(color, 1.25F),
                hitPos.x, hitPos.y, hitPos.z,
                IMPACT_DUST_LARGE_COUNT,
                0.35D, 0.35D, 0.35D,
                0.05D);
        serverLevel.sendParticles(new DustParticleOptions(color, 0.85F),
                hitPos.x, hitPos.y, hitPos.z,
                IMPACT_DUST_SMALL_COUNT,
                0.2D, 0.2D, 0.2D,
                0.02D);
    }

    private void applyHoming() {
        if (!this.homingEnabled) {
            return;
        }

        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6D) {
            return;
        }

        LivingEntity target = this.findHomingTarget();
        if (target == null) {
            return;
        }

        Vec3 currentDirection = movement.normalize();
        Vec3 desiredDirection = target.position()
                .add(0.0D, target.getBbHeight() * HOMING_TARGET_VERTICAL_OFFSET, 0.0D)
                .subtract(this.position())
                .normalize();

        if (desiredDirection.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 adjustedDirection = currentDirection.lerp(desiredDirection, HOMING_TURN_RATE).normalize();
        this.setDeltaMovement(adjustedDirection.scale(movement.length()));
    }

    private LivingEntity findHomingTarget() {
        Entity ownerEntity = this.getOwner();
        LivingEntity owner = ownerEntity instanceof LivingEntity livingOwner ? livingOwner : null;
        Vec3 currentDirection = this.getDeltaMovement().normalize();
        LivingEntity closestTarget = null;
        double closestDistanceSq = HOMING_RANGE * HOMING_RANGE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(HOMING_RANGE))) {
            if (!candidate.isAlive() || candidate == owner || !this.canHitEntity(candidate)) {
                continue;
            }

            if (owner != null && candidate.isAlliedTo(owner)) {
                continue;
            }

            Vec3 toCandidate = candidate.position()
                    .add(0.0D, candidate.getBbHeight() * HOMING_TARGET_VERTICAL_OFFSET, 0.0D)
                    .subtract(this.position());
            double distanceSq = toCandidate.lengthSqr();
            if (distanceSq < 1.0E-6D || distanceSq > closestDistanceSq) {
                continue;
            }

            Vec3 directionToCandidate = toCandidate.normalize();
            if (currentDirection.dot(directionToCandidate) <= 0.15D) {
                continue;
            }

            closestDistanceSq = distanceSq;
            closestTarget = candidate;
        }

        return closestTarget;
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
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}




