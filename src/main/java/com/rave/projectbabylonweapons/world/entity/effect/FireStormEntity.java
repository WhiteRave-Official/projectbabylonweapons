package com.rave.projectbabylonweapons.world.entity.effect;

import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.client.sound.FireStormSoundController;
import com.rave.projectbabylonweapons.handler.MagicMeleeWeaponHelper;
import com.rave.projectbabylonweapons.handler.StaffMagicArmorHelper;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerEntity;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class FireStormEntity extends Projectile {
    private static final double MOVE_SPEED = 0.12D;
    private static final double SEEK_STRENGTH = 0.18D;
    private static final double SEEK_RANGE = 14.0D;
    private static final double PULL_RADIUS = 3.4D;
    private static final double DAMAGE_RADIUS = 1.45D;
    private static final double STEER_IGNORE_HORIZONTAL_DISTANCE = 1.75D;
    private static final double STEER_IGNORE_VERTICAL_DISTANCE = 1.5D;
    private static final double VERTICAL_RADIUS = 2.6D;
    private static final int DAMAGE_INTERVAL_TICKS = 20;
    private static final int BURN_DURATION_SECONDS = 8;
    private static final float BURN_CHANCE = 0.25F;
    private static final int VISUAL_GROWTH_TICKS = 20;
    private static final String TAG_OWNER_UUID = "OwnerUuid";
    private static final String TAG_SOURCE_WEAPON = "SourceWeapon";
    private static final String TAG_DAMAGE_TYPE = "MagicDamageType";
    private static final String TAG_RAW_MAGIC_DAMAGE = "RawMagicDamage";
    private static final String TAG_MAGIC_ARMOR_NEGATION = "MagicArmorNegation";
    private static final String TAG_IMPACT = "Impact";
    private static final String TAG_LIFETIME = "Lifetime";
    private static final String TAG_DIRECTION_X = "DirectionX";
    private static final String TAG_DIRECTION_Z = "DirectionZ";

    private final Map<UUID, Long> damageCooldowns = new HashMap<>();
    private UUID ownerUuid;
    private ResourceKey<DamageType> magicDamageType = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "fire_magic"));
    private float rawMagicDamage;
    private float magicArmorNegation;
    private float impact;
    private int maxLifetime = 160;
    private ItemStack sourceWeapon = ItemStack.EMPTY;
    private Vec3 travelDirection = new Vec3(0.0D, 0.0D, 1.0D);

    public FireStormEntity(EntityType<? extends FireStormEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public FireStormEntity(Level level) {
        this(PBModEntities.FIRE_STORM.get(), level);
    }

    public void configure(LivingEntity owner, ItemStack sourceWeapon, ResourceKey<DamageType> magicDamageType,
                          float rawMagicDamage, float magicArmorNegation, float impact, int maxLifetime, Vec3 initialDirection) {
        this.setOwner(owner);
        this.ownerUuid = owner.getUUID();
        this.sourceWeapon = sourceWeapon.copy();
        this.magicDamageType = magicDamageType;
        this.rawMagicDamage = rawMagicDamage;
        this.magicArmorNegation = magicArmorNegation;
        this.impact = impact;
        this.maxLifetime = Math.max(1, maxLifetime);
        this.travelDirection = flatten(initialDirection);
        if (this.travelDirection.lengthSqr() < 1.0E-6D) {
            this.travelDirection = new Vec3(0.0D, 0.0D, 1.0D);
        }
    }

    public float getVisualProgress() {
        return Mth.clamp(this.tickCount / (float) VISUAL_GROWTH_TICKS, 0.0F, 1.0F);
    }

    public float getVisualHeight() {
        return Mth.lerp(this.getVisualProgress(), 2.0F, 4.0F);
    }

    public float getVisualRadius() {
        return Mth.lerp(this.getVisualProgress(), 0.65F, 1.4F);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            FireStormSoundController.ensurePlaying(this);
            PhotonWeaponEffectHelper.spawnFireStorm(this);
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity owner = this.resolveOwner(serverLevel);
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        this.updateMovement(owner);
        this.affectTargets(serverLevel, owner);
        this.cleanupCooldowns(serverLevel.getGameTime());

        if (this.tickCount >= this.maxLifetime) {
            this.discard();
        }
    }
    private void updateMovement(LivingEntity owner) {
        Vec3 desiredDirection = this.travelDirection;
        LivingEntity nearestTarget = this.findNearestTarget(owner);
        if (nearestTarget != null) {
            Vec3 toTarget = flatten(nearestTarget.position().subtract(this.position()));
            if (toTarget.lengthSqr() > 1.0E-6D) {
                desiredDirection = toTarget.normalize();
            }
        }

        Vec3 currentDirection = this.travelDirection.lengthSqr() > 1.0E-6D ? this.travelDirection : desiredDirection;
        Vec3 blended = currentDirection.scale(1.0D - SEEK_STRENGTH).add(desiredDirection.scale(SEEK_STRENGTH));
        this.travelDirection = blended.lengthSqr() > 1.0E-6D ? blended.normalize() : currentDirection.normalize();

        Vec3 movement = this.travelDirection.scale(MOVE_SPEED);
        this.setDeltaMovement(movement);
        this.setPos(this.getX() + movement.x, this.getY(), this.getZ() + movement.z);
        float yaw = (float) (Mth.atan2(this.travelDirection.z, this.travelDirection.x) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(yaw);
        this.yRotO = yaw;
    }

    private void affectTargets(ServerLevel level, LivingEntity owner) {
        long gameTime = level.getGameTime();
        AABB area = new AABB(
                this.getX() - PULL_RADIUS,
                this.getY() - 0.5D,
                this.getZ() - PULL_RADIUS,
                this.getX() + PULL_RADIUS,
                this.getY() + VERTICAL_RADIUS,
                this.getZ() + PULL_RADIUS
        );

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity != owner)) {
            if (target.isAlliedTo(owner)) {
                continue;
            }

            Vec3 horizontalToCenter = new Vec3(this.getX() - target.getX(), 0.0D, this.getZ() - target.getZ());
            double horizontalDistance = horizontalToCenter.length();
            if (horizontalDistance < 1.0E-4D) {
                horizontalDistance = 1.0E-4D;
            }

            if (horizontalDistance <= PULL_RADIUS) {
                double pullStrength = 0.03D + (1.0D - Math.min(horizontalDistance / PULL_RADIUS, 1.0D)) * 0.045D;
                Vec3 pull = horizontalToCenter.normalize().scale(pullStrength);
                Vec3 updatedVelocity = target.getDeltaMovement().add(pull.x, 0.018D, pull.z);
                target.setDeltaMovement(updatedVelocity.x, Math.max(updatedVelocity.y, 0.018D), updatedVelocity.z);
                target.hurtMarked = true;
            }

            if (horizontalDistance <= DAMAGE_RADIUS && Math.abs(target.getY() - this.getY()) <= VERTICAL_RADIUS) {
                long nextAllowedTick = this.damageCooldowns.getOrDefault(target.getUUID(), 0L);
                if (gameTime < nextAllowedTick) {
                    continue;
                }

                this.damageCooldowns.put(target.getUUID(), gameTime + DAMAGE_INTERVAL_TICKS);
                this.damageTarget(owner, target);
                if (level.random.nextFloat() < BURN_CHANCE) {
                    target.igniteForSeconds(BURN_DURATION_SECONDS);
                }
            }
        }
    }

    private void damageTarget(LivingEntity owner, LivingEntity target) {
        if (this.rawMagicDamage <= 0.0F) {
            return;
        }

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

        DamageSource damageSource = MagicMeleeWeaponHelper.createMagicProjectileDamageSource(
                owner,
                this,
                this.sourceWeapon,
                this.magicDamageType,
                this.magicArmorNegation,
                this.impact,
                StunType.SHORT
        );
        if (damageSource instanceof EpicFightDamageSource epicFightDamageSource) {
            epicFightDamageSource.addRuntimeTag(EpicFightDamageTypeTags.WEAPON_INNATE);
        }

        int originalInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        try {
            target.hurt(damageSource, adjustedMagicDamage);
        } finally {
            target.invulnerableTime = originalInvulnerableTime;
        }
    }

    private LivingEntity findNearestTarget(LivingEntity owner) {
        AABB searchArea = new AABB(
                this.getX() - SEEK_RANGE,
                this.getY() - 2.0D,
                this.getZ() - SEEK_RANGE,
                this.getX() + SEEK_RANGE,
                this.getY() + 3.0D,
                this.getZ() + SEEK_RANGE
        );

        LivingEntity bestTarget = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, searchArea, entity -> entity.isAlive() && entity != owner)) {
            Vec3 horizontalOffset = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
            double horizontalDistance = horizontalOffset.length();
            double distance = target.distanceToSqr(this);
            double yDelta = target.getY() - this.getY();
            boolean allied = target.isAlliedTo(owner);
            boolean capturedByStorm = horizontalDistance <= STEER_IGNORE_HORIZONTAL_DISTANCE
                    || Math.abs(yDelta) >= STEER_IGNORE_VERTICAL_DISTANCE;

            if (allied || capturedByStorm) {
                continue;
            }

            if (distance < bestDistance) {
                bestDistance = distance;
                bestTarget = target;
            }
        }

        return bestTarget;
    }

    private void cleanupCooldowns(long gameTime) {
        Iterator<Map.Entry<UUID, Long>> iterator = this.damageCooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= gameTime) {
                iterator.remove();
            }
        }
    }

    private LivingEntity resolveOwner(ServerLevel level) {
        Entity directOwner = this.getOwner();
        if (directOwner instanceof LivingEntity livingOwner) {
            return livingOwner;
        }

        Entity uuidOwner = this.ownerUuid == null ? null : level.getEntity(this.ownerUuid);
        return uuidOwner instanceof LivingEntity livingOwner ? livingOwner : null;
    }

    private static Vec3 flatten(Vec3 vector) {
        Vec3 flat = new Vec3(vector.x, 0.0D, vector.z);
        if (flat.lengthSqr() < 1.0E-6D) {
            return Vec3.ZERO;
        }
        return flat.normalize();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(TAG_OWNER_UUID)) {
            this.ownerUuid = tag.getUUID(TAG_OWNER_UUID);
        }
        if (tag.contains(TAG_SOURCE_WEAPON)) {
            this.sourceWeapon = ItemStack.parse(this.registryAccess(), tag.getCompound(TAG_SOURCE_WEAPON)).orElse(ItemStack.EMPTY);
        }
        if (tag.contains(TAG_DAMAGE_TYPE)) {
            this.magicDamageType = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse(tag.getString(TAG_DAMAGE_TYPE)));
        }
        this.rawMagicDamage = tag.getFloat(TAG_RAW_MAGIC_DAMAGE);
        this.magicArmorNegation = tag.getFloat(TAG_MAGIC_ARMOR_NEGATION);
        this.impact = tag.getFloat(TAG_IMPACT);
        this.maxLifetime = Math.max(1, tag.getInt(TAG_LIFETIME));
        this.travelDirection = flatten(new Vec3(tag.getDouble(TAG_DIRECTION_X), 0.0D, tag.getDouble(TAG_DIRECTION_Z)));
        if (this.travelDirection.lengthSqr() < 1.0E-6D) {
            this.travelDirection = new Vec3(0.0D, 0.0D, 1.0D);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.putUUID(TAG_OWNER_UUID, this.ownerUuid);
        }
        if (!this.sourceWeapon.isEmpty()) {
            tag.put(TAG_SOURCE_WEAPON, this.sourceWeapon.save(this.registryAccess()));
        }
        tag.putString(TAG_DAMAGE_TYPE, this.magicDamageType.location().toString());
        tag.putFloat(TAG_RAW_MAGIC_DAMAGE, this.rawMagicDamage);
        tag.putFloat(TAG_MAGIC_ARMOR_NEGATION, this.magicArmorNegation);
        tag.putFloat(TAG_IMPACT, this.impact);
        tag.putInt(TAG_LIFETIME, this.maxLifetime);
        tag.putDouble(TAG_DIRECTION_X, this.travelDirection.x);
        tag.putDouble(TAG_DIRECTION_Z, this.travelDirection.z);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }
}








