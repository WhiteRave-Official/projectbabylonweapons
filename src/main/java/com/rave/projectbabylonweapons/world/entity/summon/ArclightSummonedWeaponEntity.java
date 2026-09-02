package com.rave.projectbabylonweapons.world.entity.summon;

import com.rave.projectbabylonweapons.client.PhotonWeaponEffectHelper;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.gameasset.PBSkills;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import com.rave.projectbabylonweapons.summon.arclight.ArclightSummonAttackController;
import com.rave.projectbabylonweapons.summon.arclight.ArclightSummonManager;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ArclightSummonedWeaponEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(
            ArclightSummonedWeaponEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_WEAPON_TYPE = SynchedEntityData.defineId(
            ArclightSummonedWeaponEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(
            ArclightSummonedWeaponEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(
            ArclightSummonedWeaponEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID = SynchedEntityData.defineId(
            ArclightSummonedWeaponEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FORMATION_INDEX = SynchedEntityData.defineId(
            ArclightSummonedWeaponEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFETIME = SynchedEntityData.defineId(
            ArclightSummonedWeaponEntity.class, EntityDataSerializers.INT);

    private UUID ownerUuid;
    private int stateTicks;
    private int cooldownTicks;
    private int lifetimeTicks;
    private int nextTargetSearchTick;
    private AttackType lastAttack = AttackType.NONE;
    private Vec3 attackOrigin = Vec3.ZERO;
    private Vec3 attackTarget = Vec3.ZERO;
    private final Set<Integer> hitEntityIds = new HashSet<>();
    private Balance balance = Balance.defaults();
    private int previousClientState = -1;
    private boolean clientDissolved;

    public ArclightSummonedWeaponEntity(EntityType<? extends ArclightSummonedWeaponEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_OWNER_ID, -1);
        this.entityData.define(DATA_WEAPON_TYPE, ArclightSummonedWeaponType.SWORD.ordinal());
        this.entityData.define(DATA_STATE, ArclightSummonedWeaponState.ORBIT.ordinal());
        this.entityData.define(DATA_ATTACK, AttackType.NONE.ordinal());
        this.entityData.define(DATA_TARGET_ID, -1);
        this.entityData.define(DATA_FORMATION_INDEX, 0);
        this.entityData.define(DATA_LIFETIME, 1200);
    }

    public void configure(ServerPlayer owner, ArclightSummonedWeaponType type, int formationIndex, Balance balance) {
        this.ownerUuid = owner.getUUID();
        this.entityData.set(DATA_OWNER_ID, owner.getId());
        this.entityData.set(DATA_WEAPON_TYPE, type.ordinal());
        this.entityData.set(DATA_FORMATION_INDEX, formationIndex);
        this.balance = balance;
        this.lifetimeTicks = balance.lifetimeTicks();
        this.entityData.set(DATA_LIFETIME, this.lifetimeTicks);
        this.nextTargetSearchTick = formationIndex * Math.max(1, balance.targetSearchInterval() / 2);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.tickClientVisuals();
            return;
        }

        if (!ArclightSummonManager.claim(this)) {
            this.discard();
            return;
        }

        ServerPlayer owner = this.resolveOwner();
        if (owner == null || !owner.isAlive() || owner.isSpectator()
                || !(owner.getMainHandItem().getItem() instanceof ArclightSwordItem)
                || ArclightSwordItem.isEvergate(owner.getMainHandItem())
                || !hasAwakeningSkill(owner)) {
            this.dismiss(true);
            return;
        }

        if (--this.lifetimeTicks <= 0) {
            this.dismiss(true);
            return;
        }
        if ((this.lifetimeTicks % 20) == 0) {
            this.entityData.set(DATA_LIFETIME, this.lifetimeTicks);
        }
        ArclightSummonAttackController.tick(this, owner);
    }

    private void tickClientVisuals() {
        int state = this.entityData.get(DATA_STATE);
        if (state != this.previousClientState) {
            Vec3 direction = this.getLookDirection();
            if (state == ArclightSummonedWeaponState.ATTACK.ordinal()) {
                if (this.isSpear()) {
                    PhotonWeaponEffectHelper.spawnArclightSpearLaunch(this, direction);
                } else {
                    PhotonWeaponEffectHelper.spawnArclightMiniLaunch(this, direction);
                }
            } else if (state == ArclightSummonedWeaponState.RETURN.ordinal()) {
                if (this.isSpear()) {
                    PhotonWeaponEffectHelper.spawnArclightSpearImpact(this, this.position(), direction);
                } else {
                    PhotonWeaponEffectHelper.spawnArclightMiniImpact(this, this.position(), direction);
                }
            }
            this.previousClientState = state;
        }

        if (state == ArclightSummonedWeaponState.ATTACK.ordinal() && (this.tickCount & 1) == 0) {
            if (this.isSpear()) {
                PhotonWeaponEffectHelper.spawnArclightSpearFlight(this, this.getDeltaMovement());
            } else {
                PhotonWeaponEffectHelper.spawnArclightMiniFlight(this, this.getDeltaMovement());
            }
        } else if (state == ArclightSummonedWeaponState.ORBIT.ordinal() && this.tickCount % 12 == 0) {
            PhotonWeaponEffectHelper.spawnArclightMiniFlight(this, this.getLookDirection().scale(0.02D));
        }
    }

    private static boolean hasAwakeningSkill(ServerPlayer owner) {
        ServerPlayerPatch patch = EpicFightCapabilities.getEntityPatch(owner, ServerPlayerPatch.class);
        return patch != null && patch.getSkill(SkillSlots.WEAPON_INNATE).getSkill() == PBSkills.ARCLIGHT_AWAKENING;
    }

    @Nullable
    public ServerPlayer resolveOwner() {
        if (!(this.level() instanceof ServerLevel level)) {
            return null;
        }
        if (this.ownerUuid != null) {
            return level.getServer().getPlayerList().getPlayer(this.ownerUuid);
        }
        Entity entity = level.getEntity(this.entityData.get(DATA_OWNER_ID));
        return entity instanceof ServerPlayer player ? player : null;
    }

    public Vec3 getOrbitPosition(LivingEntity owner) {
        return this.getIdlePosition(owner, null);
    }

    public Vec3 getIdlePosition(LivingEntity owner, @Nullable LivingEntity target) {
        LivingEntity anchor = target == null ? owner : target;
        boolean nearTarget = target != null;
        double phase = this.isSpear() ? Math.PI * 0.5D : this.getFormationIndex() * Math.PI;
        double speed = nearTarget ? 0.055D : 0.035D;
        double angle = this.tickCount * speed + phase;
        double radius = nearTarget
                ? (this.isSpear() ? 2.7D : 2.15D)
                : (this.isSpear() ? 1.8D : 1.45D);
        double baseY = nearTarget
                ? Math.max(1.0D, anchor.getBbHeight() * 0.65D)
                : (this.isSpear() ? 1.9D : 1.4D);
        double bob = Math.sin(this.tickCount * 0.09D + phase) * 0.22D;
        return anchor.position().add(
                Math.cos(angle) * radius,
                baseY + bob,
                Math.sin(angle) * radius
        );
    }

    public void moveToward(Vec3 destination, double factor, double maxSpeed) {
        this.moveToward(destination, factor, maxSpeed, null);
    }

    public void moveToward(Vec3 destination, double factor, double maxSpeed, @Nullable Vec3 lookAt) {
        Vec3 movement = destination.subtract(this.position()).scale(factor);
        if (movement.length() > maxSpeed) {
            movement = movement.normalize().scale(maxSpeed);
        }
        this.setDeltaMovement(movement);
        this.setPos(this.position().add(movement));
        Vec3 facing = lookAt == null ? movement : lookAt.subtract(this.position());
        if (facing.lengthSqr() > 1.0E-6D) {
            this.faceDirection(facing);
        }
    }

    public void setPositionAndMovement(Vec3 position, Vec3 previousPosition) {
        Vec3 movement = position.subtract(previousPosition);
        this.setDeltaMovement(movement);
        this.setPos(position);
        if (movement.lengthSqr() > 1.0E-6D) {
            this.faceDirection(movement);
        }
    }

    private void faceDirection(Vec3 direction) {
        Vec3 normal = direction.normalize();
        this.setYRot((float) (Mth.atan2(normal.x, normal.z) * Mth.RAD_TO_DEG));
        this.setXRot((float) (Mth.atan2(normal.y, normal.horizontalDistance()) * Mth.RAD_TO_DEG));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public Vec3 getLookDirection() {
        return Vec3.directionFromRotation(this.getXRot(), this.getYRot());
    }

    public void damageAlongSweep(LivingEntity owner, Vec3 previousPosition, Vec3 currentPosition) {
        Vec3 direction = currentPosition.subtract(previousPosition);
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = this.getLookDirection();
        } else {
            direction = direction.normalize();
        }
        ArclightSummonCollider collider = this.getWeaponType().collider();
        Vec3 start = previousPosition.subtract(direction.scale(collider.halfLength()));
        Vec3 end = currentPosition.add(direction.scale(collider.halfLength()));
        AABB swept = new AABB(start, end).inflate(
                collider.halfWidth(), collider.halfHeight(), collider.halfWidth());
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, swept,
                candidate -> candidate != owner && candidate.isAlive()
                        && !owner.isAlliedTo(candidate) && !candidate.isAlliedTo(owner)
                        && !this.hitEntityIds.contains(candidate.getId()))) {
            this.hitEntityIds.add(target.getId());
            this.damageTarget(owner, target);
        }
    }

    private void damageTarget(LivingEntity owner, LivingEntity target) {
        float baseDamage = (float) owner.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        float multiplier = switch (this.getAttackType()) {
            case DASH -> this.balance.swordDashDamage();
            case SPIN -> this.balance.spearSpinDamage();
            default -> this.isSpear() ? this.balance.spearNormalDamage() : this.balance.swordNormalDamage();
        };
        DamageSource source = new DamageSource(
                this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ISSDamageTypes.HOLY_MAGIC), this, owner);
        target.hurt(source, baseDamage * multiplier);
    }

    public void dismiss(boolean dissolve) {
        if (this.level().isClientSide) {
            return;
        }
        if (dissolve) {
            this.level().broadcastEntityEvent(this, (byte) 60);
        }
        ArclightSummonManager.forget(this);
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte eventId) {
        if (eventId == 60) {
            this.spawnDissolve();
            return;
        }
        super.handleEntityEvent(eventId);
    }

    private void spawnDissolve() {
        if (this.clientDissolved) {
            return;
        }
        this.clientDissolved = true;
        if (this.isSpear()) {
            PhotonWeaponEffectHelper.spawnArclightSpearDissolve(this, this.position());
        } else {
            PhotonWeaponEffectHelper.spawnArclightMiniDissolve(this, this.position());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level().isClientSide && reason != RemovalReason.UNLOADED_TO_CHUNK) {
            this.spawnDissolve();
        }
        super.remove(reason);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUuid != null) tag.putUUID("Owner", this.ownerUuid);
        tag.putInt("WeaponType", this.entityData.get(DATA_WEAPON_TYPE));
        tag.putInt("CombatState", this.entityData.get(DATA_STATE));
        tag.putInt("AttackType", this.entityData.get(DATA_ATTACK));
        tag.putInt("TargetId", this.entityData.get(DATA_TARGET_ID));
        tag.putInt("FormationIndex", this.entityData.get(DATA_FORMATION_INDEX));
        tag.putInt("Lifetime", this.lifetimeTicks);
        tag.putInt("StateTicks", this.stateTicks);
        tag.putInt("CooldownTicks", this.cooldownTicks);
        tag.putInt("LastAttack", this.lastAttack.ordinal());
        tag.put("Balance", this.balance.save());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) this.ownerUuid = tag.getUUID("Owner");
        this.entityData.set(DATA_WEAPON_TYPE, tag.getInt("WeaponType"));
        this.entityData.set(DATA_STATE, tag.getInt("CombatState"));
        this.entityData.set(DATA_ATTACK, tag.getInt("AttackType"));
        this.entityData.set(DATA_TARGET_ID, tag.getInt("TargetId"));
        this.entityData.set(DATA_FORMATION_INDEX, tag.getInt("FormationIndex"));
        this.lifetimeTicks = tag.getInt("Lifetime");
        this.entityData.set(DATA_LIFETIME, this.lifetimeTicks);
        this.stateTicks = tag.getInt("StateTicks");
        this.cooldownTicks = tag.getInt("CooldownTicks");
        this.lastAttack = AttackType.byId(tag.getInt("LastAttack"));
        if (tag.contains("Balance")) this.balance = Balance.load(tag.getCompound("Balance"));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public ArclightSummonedWeaponType getWeaponType() { return ArclightSummonedWeaponType.byId(this.entityData.get(DATA_WEAPON_TYPE)); }
    public boolean isSpear() { return this.getWeaponType() == ArclightSummonedWeaponType.SPEAR; }
    public ArclightSummonedWeaponState getCombatState() { return ArclightSummonedWeaponState.byId(this.entityData.get(DATA_STATE)); }
    public void setCombatState(ArclightSummonedWeaponState state) { this.entityData.set(DATA_STATE, state.ordinal()); this.stateTicks = 0; }
    public AttackType getAttackType() { return AttackType.byId(this.entityData.get(DATA_ATTACK)); }
    public void setAttackType(AttackType attack) { this.entityData.set(DATA_ATTACK, attack.ordinal()); }
    public LivingEntity getTarget() { Entity e = this.level().getEntity(this.entityData.get(DATA_TARGET_ID)); return e instanceof LivingEntity living ? living : null; }
    public void setTarget(@Nullable LivingEntity target) { this.entityData.set(DATA_TARGET_ID, target == null ? -1 : target.getId()); }
    public int getFormationIndex() { return this.entityData.get(DATA_FORMATION_INDEX); }
    public UUID getOwnerUuid() { return this.ownerUuid; }
    public int getStateTicks() { return this.stateTicks; }
    public void incrementStateTicks() { this.stateTicks++; }
    public int getCooldownTicks() { return this.cooldownTicks; }
    public void setCooldownTicks(int ticks) { this.cooldownTicks = Math.max(0, ticks); }
    public void decrementCooldown() { if (this.cooldownTicks > 0) this.cooldownTicks--; }
    public int getNextTargetSearchTick() { return this.nextTargetSearchTick; }
    public void setNextTargetSearchTick(int tick) { this.nextTargetSearchTick = tick; }
    public AttackType getLastAttack() { return this.lastAttack; }
    public void setLastAttack(AttackType attack) { this.lastAttack = attack; }
    public Vec3 getAttackOrigin() { return this.attackOrigin; }
    public void setAttackOrigin(Vec3 origin) { this.attackOrigin = origin; }
    public Vec3 getAttackTarget() { return this.attackTarget; }
    public void setAttackTarget(Vec3 target) { this.attackTarget = target; }
    public void clearHitEntities() { this.hitEntityIds.clear(); }
    public Balance getBalance() { return this.balance; }
    public int nextRandomInt(int bound) { return this.random.nextInt(bound); }

    public enum AttackType {
        NONE, STAB, VERTICAL, HORIZONTAL, DASH, SPIN;

        public static AttackType byId(int id) {
            AttackType[] values = values();
            return id >= 0 && id < values.length ? values[id] : NONE;
        }
    }

    public record Balance(int lifetimeTicks, double targetRadius, int targetSearchInterval,
                          int windupTicks, int attackTicks, int returnTicks,
                          int swordCooldownTicks, int spearCooldownTicks,
                          float swordNormalDamage, float swordDashDamage,
                          float spearNormalDamage, float spearSpinDamage) {
        public static Balance defaults() {
            return new Balance(1200, 8.0D, 9, 12, 10, 16, 30, 40,
                    0.15F, 0.20F, 0.25F, 0.20F);
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Lifetime", this.lifetimeTicks);
            tag.putDouble("TargetRadius", this.targetRadius);
            tag.putInt("SearchInterval", this.targetSearchInterval);
            tag.putInt("Windup", this.windupTicks);
            tag.putInt("Attack", this.attackTicks);
            tag.putInt("Return", this.returnTicks);
            tag.putInt("SwordCooldown", this.swordCooldownTicks);
            tag.putInt("SpearCooldown", this.spearCooldownTicks);
            tag.putFloat("SwordNormalDamage", this.swordNormalDamage);
            tag.putFloat("SwordDashDamage", this.swordDashDamage);
            tag.putFloat("SpearNormalDamage", this.spearNormalDamage);
            tag.putFloat("SpearSpinDamage", this.spearSpinDamage);
            return tag;
        }

        public static Balance load(CompoundTag tag) {
            Balance defaults = defaults();
            return new Balance(tag.contains("Lifetime") ? tag.getInt("Lifetime") : defaults.lifetimeTicks,
                    tag.contains("TargetRadius") ? tag.getDouble("TargetRadius") : defaults.targetRadius,
                    tag.contains("SearchInterval") ? tag.getInt("SearchInterval") : defaults.targetSearchInterval,
                    tag.contains("Windup") ? tag.getInt("Windup") : defaults.windupTicks,
                    tag.contains("Attack") ? tag.getInt("Attack") : defaults.attackTicks,
                    tag.contains("Return") ? tag.getInt("Return") : defaults.returnTicks,
                    tag.contains("SwordCooldown") ? tag.getInt("SwordCooldown") : defaults.swordCooldownTicks,
                    tag.contains("SpearCooldown") ? tag.getInt("SpearCooldown") : defaults.spearCooldownTicks,
                    tag.contains("SwordNormalDamage") ? tag.getFloat("SwordNormalDamage") : defaults.swordNormalDamage,
                    tag.contains("SwordDashDamage") ? tag.getFloat("SwordDashDamage") : defaults.swordDashDamage,
                    tag.contains("SpearNormalDamage") ? tag.getFloat("SpearNormalDamage") : defaults.spearNormalDamage,
                    tag.contains("SpearSpinDamage") ? tag.getFloat("SpearSpinDamage") : defaults.spearSpinDamage);
        }
    }
}