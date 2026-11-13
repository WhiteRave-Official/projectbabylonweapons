package com.rave.projectbabylonweapons.world.entity.effect;

import com.rave.projectbabylonweapons.client.LightBreachClientEffects;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.skill.arclight.LightBreachCast;
import com.rave.projectbabylonweapons.skill.arclight.LightBreachSettings;
import com.rave.projectbabylonweapons.skill.arclight.LightBreachTiming;
import com.rave.projectbabylonweapons.skill.arclight.LightBreachSounds;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public final class LightBreachEntity extends Entity {
    private static final EntityDataAccessor<CompoundTag> DATA_LAYOUT =
            SynchedEntityData.defineId(LightBreachEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Long> DATA_ACTIVE_SINCE =
            SynchedEntityData.defineId(LightBreachEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> DATA_CANCEL_SINCE =
            SynchedEntityData.defineId(LightBreachEntity.class, EntityDataSerializers.LONG);
    private static final int MAX_PREPARATION_TICKS = 200;

    public record Column(Vec3 base, float height) {}

    private UUID ownerUuid;
    private int ownerId;
    private long preparedAt;
    private float pulseDamage;
    private LightBreachSettings settings = LightBreachSettings.defaults();
    private List<Column> columns = List.of();
    private AABB searchBounds;
    private CompoundTag cachedLayout;

    public LightBreachEntity(EntityType<? extends LightBreachEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public LightBreachEntity(Level level) {
        this(PBModEntities.LIGHT_BREACH.get(), level);
    }

    public boolean configure(ServerPlayer owner, LightBreachSettings settings) {
        this.ownerUuid = owner.getUUID();
        this.ownerId = owner.getId();
        this.settings = settings;
        this.preparedAt = level().getGameTime();
        setPos(owner.position());
        this.columns = placeColumns(owner);
        if (columns.isEmpty()) return false;
        this.searchBounds = calculateBounds();

        CompoundTag layout = settings.toTag();
        layout.putInt("OwnerId", ownerId);
        layout.putLong("PreparedAt", preparedAt);
        ListTag positions = new ListTag();
        for (Column column : columns) {
            CompoundTag position = new CompoundTag();
            position.putDouble("X", column.base.x);
            position.putDouble("Y", column.base.y);
            position.putDouble("Z", column.base.z);
            position.putFloat("Height", column.height);
            positions.add(position);
        }
        layout.put("Columns", positions);
        entityData.set(DATA_LAYOUT, layout);
        return true;
    }

    private List<Column> placeColumns(ServerPlayer owner) {
        List<Column> result = new ArrayList<>();
        double rotation = random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < settings.columns(); i++) {
            for (int attempt = 0; attempt < 4; attempt++) {
                double angle = rotation + (i + random.nextDouble() * 0.35) * Math.PI * 2 / settings.columns();
                double radius = settings.minRadius() + random.nextDouble() * (settings.maxRadius() - settings.minRadius());
                Vec3 start = owner.position().add(Math.cos(angle) * radius, 0.5D, Math.sin(angle) * radius);
                if (!level().hasChunkAt(BlockPos.containing(start))) continue;
                var ground = level().clip(new ClipContext(start, start.add(0, -6, 0),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (ground.getType() != HitResult.Type.BLOCK || ground.getDirection() != Direction.UP) continue;
                Vec3 base = ground.getLocation().add(0, 0.025D, 0);
                var ceiling = level().clip(new ClipContext(base, base.add(0, settings.height(), 0),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                float height = (float) Math.max(0, ceiling.getLocation().y - base.y);
                if (height >= 0.75F) {
                    result.add(new Column(base, height));
                    break;
                }
            }
        }
        return List.copyOf(result);
    }

    private AABB calculateBounds() {
        AABB bounds = null;
        double r = settings.columnRadius();
        for (Column column : columns) {
            Vec3 p = column.base;
            AABB box = new AABB(p.x - r, p.y, p.z - r, p.x + r, p.y + column.height, p.z + r);
            bounds = bounds == null ? box : bounds.minmax(box);
        }
        return bounds;
    }

    public void activate(ServerPlayer owner) {
        if (!(level() instanceof ServerLevel server) || getActiveSince() >= 0 || getCancelSince() >= 0) return;
        this.pulseDamage = Math.max(0, (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE))
                * settings.damageMultiplier();
        entityData.set(DATA_ACTIVE_SINCE, level().getGameTime());
        for (Column column : columns) LightBreachSounds.beam(server, column.base(), columns.size());
    }

    public void cancelPreparation() {
        if (getActiveSince() < 0 && getCancelSince() < 0) {
            entityData.set(DATA_CANCEL_SINCE, level().getGameTime());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            readClientLayout();
            LightBreachClientEffects.tick(this);
            return;
        }
        if (!(level() instanceof ServerLevel server) || ownerUuid == null
                || !(server.getEntity(ownerUuid) instanceof ServerPlayer owner) || !owner.isAlive()) {
            discard();
            return;
        }
        if (getCancelSince() >= 0) {
            if (level().getGameTime() - getCancelSince() >= settings.fadeOut()) discard();
            return;
        }
        if (getActiveSince() < 0) {
            if (level().getGameTime() - preparedAt > MAX_PREPARATION_TICKS
                    || !(owner.getMainHandItem().getItem() instanceof ArclightSwordItem)
                    || ArclightSwordItem.isEvergate(owner.getMainHandItem())) {
                LightBreachCast.forget(ownerUuid, this);
                cancelPreparation();
            }
            return;
        }
        int age = (int) (level().getGameTime() - getActiveSince());
        if (age >= settings.totalTicks()) {
            discard();
        } else if (LightBreachTiming.isPulse(age, settings.fadeIn(), settings.activeTicks(), settings.pulseInterval())) {
            damageTargets(owner);
        }
    }

    private void damageTargets(ServerPlayer owner) {
        if (pulseDamage <= 0 || searchBounds == null) return;
        DamageSource source = new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ISSDamageTypes.HOLY_MAGIC), this, owner);

        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, searchBounds,
                entity -> entity.isAlive() && !entity.isSpectator() && entity != owner
                        && !owner.isAlliedTo(entity) && !entity.isAlliedTo(owner)
                        && (!(entity instanceof ServerPlayer player) || owner.canHarmPlayer(player)))) {
            AABB box = target.getBoundingBox();
            for (Column column : columns) {
                Vec3 p = column.base;
                if (LightBreachTiming.intersectsColumn(p.x, p.y, p.z, settings.columnRadius(), column.height,
                        box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
                    target.hurt(source, pulseDamage);
                    break;
                }
            }
        }
    }

    private void readClientLayout() {
        CompoundTag layout = entityData.get(DATA_LAYOUT);
        if (layout == cachedLayout) return;
        cachedLayout = layout;
        settings = LightBreachSettings.fromTag(layout);
        ownerId = layout.getInt("OwnerId");
        preparedAt = layout.getLong("PreparedAt");
        ListTag list = layout.getList("Columns", 10);
        List<Column> decoded = new ArrayList<>();
        for (int i = 0; i < Math.min(16, list.size()); i++) {
            CompoundTag p = list.getCompound(i);
            decoded.add(new Column(new Vec3(p.getDouble("X"), p.getDouble("Y"), p.getDouble("Z")),
                    p.getFloat("Height")));
        }
        columns = List.copyOf(decoded);
    }

    public List<Column> getColumns() { return columns; }
    public LightBreachSettings getSettings() { return settings; }
    public int getOwnerId() { return ownerId; }
    public long getPreparedAt() { return preparedAt; }
    public long getActiveSince() { return entityData.get(DATA_ACTIVE_SINCE); }
    public long getCancelSince() { return entityData.get(DATA_CANCEL_SINCE); }

    @Override
    public void remove(RemovalReason reason) {
        if (level().isClientSide) LightBreachClientEffects.remove(this);
        else LightBreachCast.forget(ownerUuid, this);
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_LAYOUT, new CompoundTag());
        entityData.define(DATA_ACTIVE_SINCE, -1L);
        entityData.define(DATA_CANCEL_SINCE, -1L);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) { discard(); }
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override
    public boolean isPickable() { return false; }
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
