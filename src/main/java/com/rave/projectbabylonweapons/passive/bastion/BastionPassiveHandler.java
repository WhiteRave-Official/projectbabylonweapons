package com.rave.projectbabylonweapons.passive.bastion;

import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import com.rave.projectbabylonweapons.world.entity.effect.GlacierIceSpikeEntity;
import com.rave.projectbabylonweapons.world.entity.effect.TectonicFallingBlockEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionPassiveHandler {
    private static final double AREA_RADIUS = 8.0D;
    private static final double AREA_VERTICAL = 3.0D;
    private static final int BLOCK_SCAN_VERTICAL = 6;
    private static final int PERMAFROST_INNER_SPIKE_COUNT = 8;
    private static final int PERMAFROST_OUTER_SPIKE_COUNT = 12;
    private static final double PERMAFROST_OUTER_SPIKE_RADIUS_MULTIPLIER = 0.55D;
    private static final double PERMAFROST_INNER_SPIKE_RADIUS_MULTIPLIER = 0.32D;
    private static final double PERMAFROST_SPIKE_MIN_RADIUS = 2.5D;
    private static final double PERMAFROST_SPIKE_DAMAGE_RADIUS = 1.15D;
    private static final float PERMAFROST_SPIKE_DAMAGE_MULTIPLIER = 0.8F;
    private static final Map<UUID, FrostAuraState> ACTIVE_FROST_AURAS = new HashMap<>();
    private static final Map<UUID, RuleAuraState> ACTIVE_RULE_AURAS = new HashMap<>();
    private static final List<PendingPermafrostSpikeBurst> PENDING_PERMAFROST_SPIKE_BURSTS = new ArrayList<>();

    private BastionPassiveHandler() {
    }

    public static void handleSternSlamContact(LivingEntity caster, ItemStack shieldStack) {
        if (!(caster.level() instanceof ServerLevel serverLevel) || shieldStack.isEmpty()) {
            return;
        }

        BastionCrushingBalance.Profile crushing = BastionCrushingBalance.resolve(shieldStack);
        if (crushing != null) {
            applyCrushing(serverLevel, caster, crushing);
            return;
        }

        BastionCurseBalance.Profile curse = BastionCurseBalance.resolve(shieldStack);
        if (curse != null) {
            applyEnemiesEffect(caster, new MobEffectInstance(PBMEffects.FEAR_DEBUFF, curse.fearDurationTicks(), 0, false, true, true));
            return;
        }

        BastionPermafrostBalance.Profile permafrost = BastionPermafrostBalance.resolve(shieldStack);
        if (permafrost != null) {
            spawnPermafrostSpikeBurst(serverLevel, caster, permafrost);
            startFrostAura(serverLevel, caster, permafrost);
            return;
        }

        BastionWarSignalBalance.Profile warSignal = BastionWarSignalBalance.resolve(shieldStack);
        if (warSignal != null) {
            applyAlliedPlayersEffect(caster, new MobEffectInstance(PBMEffects.ASH_MEMORY, warSignal.ashMemoryDurationTicks(), 0, false, true, true));
            return;
        }

        BastionHeavensGiftBalance.Profile heavensGift = BastionHeavensGiftBalance.resolve(shieldStack);
        if (heavensGift != null) {
            applyAlliedPlayersEffect(caster, new MobEffectInstance(PBMEffects.HOLY_SIGIL, heavensGift.holySigilDurationTicks(), 0, false, true, true));
            return;
        }

        BastionRuleAuraBalance.Profile ruleAura = BastionRuleAuraBalance.resolve(shieldStack);
        if (ruleAura != null) {
            startRuleAura(serverLevel, caster, ruleAura);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickPendingPermafrostSpikeBursts();
        tickFrostAuras();
        tickRuleAuras();
    }

    private static void tickPendingPermafrostSpikeBursts() {
        Iterator<PendingPermafrostSpikeBurst> iterator = PENDING_PERMAFROST_SPIKE_BURSTS.iterator();
        while (iterator.hasNext()) {
            PendingPermafrostSpikeBurst burst = iterator.next();
            if (burst.level.getGameTime() < burst.triggerGameTime) {
                continue;
            }

            ServerPlayer caster = burst.level.getServer().getPlayerList().getPlayer(burst.casterId);
            if (caster != null && caster.isAlive()) {
                damagePermafrostSpikeBurst(burst.level, caster, burst.spikePos, burst.damage, burst.hitTargets);
            }
            iterator.remove();
        }
    }

    private static void tickFrostAuras() {
        var iterator = ACTIVE_FROST_AURAS.entrySet().iterator();
        while (iterator.hasNext()) {
            FrostAuraState state = iterator.next().getValue();
            ServerPlayer caster = state.level.getServer().getPlayerList().getPlayer(state.casterId);
            if (caster == null || !caster.isAlive()) {
                WeaponVisualEffectHelper.stopBastionFrostAura(state.anchor);
                iterator.remove();
                continue;
            }

            if (state.level.getGameTime() >= state.endGameTime) {
                WeaponVisualEffectHelper.stopBastionFrostAura(caster);
                iterator.remove();
                continue;
            }

            if (state.level.getGameTime() < state.nextRefreshGameTime) {
                continue;
            }

            state.nextRefreshGameTime = state.level.getGameTime() + state.profile.refreshIntervalTicks();
            Holder<MobEffect> chilledEffect = MobEffectRegistry.CHILLED;
            MobEffectInstance chilled = new MobEffectInstance(chilledEffect, state.profile.chillDurationTicks(), 0, false, true, true);
            MobEffectInstance magicalResistance = new MobEffectInstance(PBMEffects.MAGICAL_RESISTANCE, state.profile.magicalResistanceDurationTicks(), 0, false, true, true);
            for (LivingEntity target : getNearbyEnemies(caster, state.profile.radiusBlocks(), AREA_VERTICAL)) {
                target.addEffect(new MobEffectInstance(chilled));
            }
            applyAlliedPlayersEffect(caster, magicalResistance, state.profile.radiusBlocks());
        }
    }

    private static void tickRuleAuras() {
        var iterator = ACTIVE_RULE_AURAS.entrySet().iterator();
        while (iterator.hasNext()) {
            RuleAuraState state = iterator.next().getValue();
            ServerPlayer caster = state.level.getServer().getPlayerList().getPlayer(state.casterId);
            if (caster == null || !caster.isAlive()) {
                WeaponVisualEffectHelper.stopBastionRuleAura(state.anchor);
                iterator.remove();
                continue;
            }

            if (state.level.getGameTime() >= state.endGameTime) {
                WeaponVisualEffectHelper.stopBastionRuleAura(caster);
                iterator.remove();
                continue;
            }

            if (state.level.getGameTime() < state.nextRefreshGameTime) {
                continue;
            }

            state.nextRefreshGameTime = state.level.getGameTime() + state.profile.refreshIntervalTicks();
            MobEffectInstance provoke = new MobEffectInstance(PBMEffects.PROVOKE_DEBUFF, state.profile.provokeDurationTicks(), 0, false, true, true);
            MobEffectInstance critResistance = new MobEffectInstance(PBMEffects.CRIT_RESISTANCE, state.profile.critResistanceDurationTicks(), 0, false, true, true);
            for (LivingEntity target : getNearbyEnemies(caster, state.profile.radiusBlocks(), AREA_VERTICAL)) {
                target.setLastHurtByMob(caster);
                target.addEffect(new MobEffectInstance(provoke));
            }
            applyAlliedPlayersEffect(caster, critResistance, state.profile.radiusBlocks());
        }
    }

    private static void applyCrushing(ServerLevel level, LivingEntity caster, BastionCrushingBalance.Profile profile) {
        MobEffectInstance weakness = new MobEffectInstance(MobEffects.WEAKNESS, profile.weaknessDurationTicks(), 0, false, true, true);
        Vec3 origin = caster.position();
        for (LivingEntity target : getNearbyEnemies(caster, AREA_RADIUS, AREA_VERTICAL)) {
            Vec3 away = target.position().subtract(origin);
            Vec3 horizontalAway = new Vec3(away.x, 0.0D, away.z);
            Vec3 push = horizontalAway.lengthSqr() > 1.0E-6D ? horizontalAway.normalize().scale(profile.horizontalPush()) : Vec3.ZERO;
            target.setDeltaMovement(push.x, profile.knockupVelocity(), push.z);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(weakness));
        }

        spawnCrushingBlocks(level, caster, profile.blockRingCount());
    }

    private static void spawnCrushingBlocks(ServerLevel level, LivingEntity caster, int ringCount) {
        Set<BlockPos> spawned = new HashSet<>();
        Vec3 origin = caster.position();
        int rings = Math.max(1, ringCount);
        for (int ring = 1; ring <= rings; ring++) {
            double radius = (AREA_RADIUS / rings) * ring;
            int segments = Math.max(10, Mth.ceil(radius * 4.0D));
            for (int i = 0; i < segments; i++) {
                double angle = (Math.PI * 2.0D * i) / segments;
                double x = origin.x + (Math.cos(angle) * radius);
                double z = origin.z + (Math.sin(angle) * radius);
                BlockPos surfacePos = findSurfaceBlock(level, x, origin.y, z);
                if (surfacePos == null || !spawned.add(surfacePos)) {
                    continue;
                }

                BlockState state = level.getBlockState(surfacePos);
                if (state.getRenderShape() == RenderShape.INVISIBLE) {
                    continue;
                }

                TectonicFallingBlockEntity blockEntity = new TectonicFallingBlockEntity(level, state, 0.28F + (ring * 0.03F));
                blockEntity.setPos(surfacePos.getX() + 0.5D, surfacePos.getY() + 0.5D, surfacePos.getZ() + 0.5D);
                level.addFreshEntity(blockEntity);
            }
        }
    }

    private static void spawnPermafrostSpikeBurst(ServerLevel level, LivingEntity caster, BastionPermafrostBalance.Profile profile) {
        Vec3 origin = caster.position();
        double outerRingRadius = Math.max(PERMAFROST_SPIKE_MIN_RADIUS, profile.radiusBlocks() * PERMAFROST_OUTER_SPIKE_RADIUS_MULTIPLIER);
        double innerRingRadius = Math.max(1.35D, profile.radiusBlocks() * PERMAFROST_INNER_SPIKE_RADIUS_MULTIPLIER);
        if (innerRingRadius >= outerRingRadius - 0.35D) {
            innerRingRadius = Math.max(1.35D, outerRingRadius - 0.8D);
        }

        float spikeDamage = Math.max(1.0F, (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * PERMAFROST_SPIKE_DAMAGE_MULTIPLIER);
        Set<UUID> hitTargets = new HashSet<>();

        spawnPermafrostSpikeRing(level, caster, origin, outerRingRadius, PERMAFROST_OUTER_SPIKE_COUNT, 3.0F, spikeDamage, hitTargets, false);
        spawnPermafrostSpikeRing(level, caster, origin, innerRingRadius, PERMAFROST_INNER_SPIKE_COUNT, 2.0F, spikeDamage, hitTargets, true);
    }

    private static void spawnPermafrostSpikeRing(ServerLevel level, LivingEntity caster, Vec3 origin, double ringRadius, int spikeCount,
                                                 float spikeScale, float spikeDamage, Set<UUID> hitTargets, boolean angleOffset) {
        double startAngle = angleOffset ? (Math.PI / spikeCount) : 0.0D;
        for (int i = 0; i < spikeCount; i++) {
            double angle = startAngle + ((Math.PI * 2.0D * i) / spikeCount);
            double x = origin.x + (Math.cos(angle) * ringRadius);
            double z = origin.z + (Math.sin(angle) * ringRadius);
            BlockPos surfacePos = findSurfaceBlock(level, x, origin.y, z);
            if (surfacePos == null) {
                continue;
            }

            Vec3 spikePos = new Vec3(surfacePos.getX() + 0.5D, surfacePos.getY() + 1.02D, surfacePos.getZ() + 0.5D);
            GlacierIceSpikeEntity spikeEntity = new GlacierIceSpikeEntity(level);
            spikeEntity.setWaitTime(0);
            spikeEntity.setSpikeScale(spikeScale);
            spikeEntity.setMirrored((i & 1) != 0);
            spikeEntity.setRiseHeight(Math.max(1.0F, spikeScale * 0.55F));
            spikeEntity.setPos(spikePos.x, spikePos.y - spikeEntity.getRiseHeight(), spikePos.z);
            spikeEntity.setYRot(computeOutwardYaw(origin, spikePos));
            spikeEntity.yRotO = spikeEntity.getYRot();
            spikeEntity.setXRot(-10.0F);
            spikeEntity.xRotO = spikeEntity.getXRot();
            level.addFreshEntity(spikeEntity);
            PENDING_PERMAFROST_SPIKE_BURSTS.add(new PendingPermafrostSpikeBurst(level, caster.getUUID(), level.getGameTime() + GlacierIceSpikeEntity.RISE_TIME, spikePos, spikeDamage, hitTargets));
        }
    }

    private static void damagePermafrostSpikeBurst(ServerLevel level, LivingEntity caster, Vec3 spikePos, float damage, Set<UUID> hitTargets) {
        if (damage <= 0.0F) {
            return;
        }

        AABB area = new AABB(
                spikePos.x - PERMAFROST_SPIKE_DAMAGE_RADIUS, spikePos.y - 1.0D, spikePos.z - PERMAFROST_SPIKE_DAMAGE_RADIUS,
                spikePos.x + PERMAFROST_SPIKE_DAMAGE_RADIUS, spikePos.y + 2.5D, spikePos.z + PERMAFROST_SPIKE_DAMAGE_RADIUS
        );
        DamageSource damageSource = createMeleeDamageSource(caster);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity != caster)) {
            if (target.isAlliedTo(caster) || hitTargets.contains(target.getUUID())) {
                continue;
            }

            int originalInvulnerableTime = target.invulnerableTime;
            boolean damaged;
            target.invulnerableTime = 0;
            try {
                damaged = target.hurt(damageSource, damage);
            } finally {
                target.invulnerableTime = originalInvulnerableTime;
            }

            if (damaged) {
                hitTargets.add(target.getUUID());
            }
        }
    }

    private static DamageSource createMeleeDamageSource(LivingEntity caster) {
        if (caster instanceof Player player) {
            return caster.damageSources().playerAttack(player);
        }
        return caster.damageSources().mobAttack(caster);
    }

    private static float computeOutwardYaw(Vec3 origin, Vec3 spikePos) {
        Vec3 delta = spikePos.subtract(origin);
        return (float) (Mth.atan2(delta.z, delta.x) * (180.0D / Math.PI)) - 90.0F;
    }

    private static void startFrostAura(ServerLevel level, LivingEntity caster, BastionPermafrostBalance.Profile profile) {
        if (!(caster instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ACTIVE_FROST_AURAS.remove(serverPlayer.getUUID());
        WeaponVisualEffectHelper.stopBastionFrostAura(serverPlayer);
        ACTIVE_FROST_AURAS.put(serverPlayer.getUUID(), new FrostAuraState(serverPlayer.getUUID(), level, level.getGameTime() + profile.auraDurationTicks(), level.getGameTime(), profile, serverPlayer));
        WeaponVisualEffectHelper.startBastionFrostAura(serverPlayer);
    }

    private static void startRuleAura(ServerLevel level, LivingEntity caster, BastionRuleAuraBalance.Profile profile) {
        if (!(caster instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ACTIVE_RULE_AURAS.remove(serverPlayer.getUUID());
        WeaponVisualEffectHelper.stopBastionRuleAura(serverPlayer);
        ACTIVE_RULE_AURAS.put(serverPlayer.getUUID(), new RuleAuraState(serverPlayer.getUUID(), level, level.getGameTime() + profile.auraDurationTicks(), level.getGameTime(), profile, serverPlayer));
        WeaponVisualEffectHelper.startBastionRuleAura(serverPlayer);
    }

    private static void applyAlliedPlayersEffect(LivingEntity caster, MobEffectInstance effect) {
        applyAlliedPlayersEffect(caster, effect, AREA_RADIUS);
    }

    private static void applyAlliedPlayersEffect(LivingEntity caster, MobEffectInstance effect, double radius) {
        if (!(caster.level() instanceof ServerLevel serverLevel) || !(caster instanceof ServerPlayer serverPlayer)) {
            return;
        }

        serverPlayer.addEffect(new MobEffectInstance(effect));
        AABB area = serverPlayer.getBoundingBox().inflate(radius, AREA_VERTICAL, radius);
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, area, target -> target.isAlive() && !target.isSpectator() && target != serverPlayer);
        for (ServerPlayer target : nearbyPlayers) {
            target.addEffect(new MobEffectInstance(effect));
        }
    }

    private static void applyEnemiesEffect(LivingEntity caster, MobEffectInstance effect) {
        for (LivingEntity target : getNearbyEnemies(caster, AREA_RADIUS, AREA_VERTICAL)) {
            target.addEffect(new MobEffectInstance(effect));
        }
    }

    private static List<LivingEntity> getNearbyEnemies(LivingEntity caster, double radius, double vertical) {
        AABB area = caster.getBoundingBox().inflate(radius, vertical, radius);
        return caster.level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity != caster && !entity.isAlliedTo(caster) && !(entity instanceof Player player && player.isSpectator()));
    }

    private static BlockPos findSurfaceBlock(ServerLevel level, double x, double y, double z) {
        int baseY = Mth.floor(y);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(Mth.floor(x), baseY + 1, Mth.floor(z));
        for (int dy = 0; dy < BLOCK_SCAN_VERTICAL; dy++) {
            mutable.setY(baseY + 1 - dy);
            BlockState state = level.getBlockState(mutable);
            if (!state.isAir() && state.getRenderShape() != RenderShape.INVISIBLE) {
                return mutable.immutable();
            }
        }
        return null;
    }

    private static final class FrostAuraState {
        private final UUID casterId;
        private final ServerLevel level;
        private final long endGameTime;
        private long nextRefreshGameTime;
        private final BastionPermafrostBalance.Profile profile;
        private final LivingEntity anchor;

        private FrostAuraState(UUID casterId, ServerLevel level, long endGameTime, long nextRefreshGameTime, BastionPermafrostBalance.Profile profile, LivingEntity anchor) {
            this.casterId = casterId;
            this.level = level;
            this.endGameTime = endGameTime;
            this.nextRefreshGameTime = nextRefreshGameTime;
            this.profile = profile;
            this.anchor = anchor;
        }
    }

    private static final class RuleAuraState {
        private final UUID casterId;
        private final ServerLevel level;
        private final long endGameTime;
        private long nextRefreshGameTime;
        private final BastionRuleAuraBalance.Profile profile;
        private final LivingEntity anchor;

        private RuleAuraState(UUID casterId, ServerLevel level, long endGameTime, long nextRefreshGameTime, BastionRuleAuraBalance.Profile profile, LivingEntity anchor) {
            this.casterId = casterId;
            this.level = level;
            this.endGameTime = endGameTime;
            this.nextRefreshGameTime = nextRefreshGameTime;
            this.profile = profile;
            this.anchor = anchor;
        }
    }

    private static final class PendingPermafrostSpikeBurst {
        private final ServerLevel level;
        private final UUID casterId;
        private final long triggerGameTime;
        private final Vec3 spikePos;
        private final float damage;
        private final Set<UUID> hitTargets;

        private PendingPermafrostSpikeBurst(ServerLevel level, UUID casterId, long triggerGameTime, Vec3 spikePos, float damage, Set<UUID> hitTargets) {
            this.level = level;
            this.casterId = casterId;
            this.triggerGameTime = triggerGameTime;
            this.spikePos = spikePos;
            this.damage = damage;
            this.hitTargets = hitTargets;
        }
    }
}