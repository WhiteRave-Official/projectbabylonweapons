package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import com.rave.projectbabylonweapons.handler.MagicMeleeWeaponHelper;
import com.rave.projectbabylonweapons.handler.StaffMagicArmorHelper;
import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.init.PBWSounds;
import com.rave.projectbabylonweapons.world.entity.effect.GlacierIceSpikeEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.SimpleWeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GlacierSkill extends SimpleWeaponInnateSkill {
    private static final int CHILL_DURATION_TICKS = 15 * 20;
    private static final int FROZEN_DURATION_TICKS = 4 * 20;
    private static final int OUTER_SPIKE_COUNT = 10;
    private static final int INNER_SPIKE_COUNT = 6;
    private static final double SPIKE_DAMAGE_RADIUS = 1.15D;
    private static final double OUTER_FRONT_RADIUS = 4.5D;
    private static final double OUTER_BACK_RADIUS = 2.5D;
    private static final double OUTER_SIDE_RADIUS = 3.65D;
    private static final double INNER_FRONT_RADIUS = 2.5D;
    private static final double INNER_BACK_RADIUS = 1.5D;
    private static final double INNER_SIDE_RADIUS = 2.05D;
    private static final float INNER_BACK_SCALE = 3.5F;
    private static final float INNER_FRONT_SCALE = 1.0F;
    private static final float OUTER_BACK_SCALE = 6.0F;
    private static final float OUTER_FRONT_SCALE = 2.0F;
    private static final double OPENING_HALF_ANGLE_DEGREES = 32.0D;
    private static final double BACK_HALF_ANGLE_DEGREES = 18.0D;
    private static final int OUTER_WAIT_STEP = 2;
    private static final int INNER_WAIT_STEP = 3;
    private static final int INNER_WAIT_START = 1;
    private static final int PARTICLE_WAVE_STEPS = 8;
    private static final int PARTICLE_WAVE_INTERVAL = 1;
    private static final int WAVE_SNOWFLAKE_COUNT = 26;
    private static final int WAVE_SNOW_DUST_COUNT = 18;
    private static final Map<UUID, ActiveGlacierCast> ACTIVE_CASTS = new HashMap<>();

    private record RingSpec(int spikeCount, double frontRadius, double backRadius, double sideRadius, double minDistance,
                            int waitStart, int waitStep, float backScale, float frontScale) {

    }

    private record PendingSpikeBurst(long triggerGameTime, Vec3 spikePos, GlacierIceSpikeEntity spikeEntity) {
    }

    private static class ActiveGlacierCast {
        private final ServerLevel level;
        private final UUID casterId;
        private final ItemStack weaponStack;
        private final MagicMeleeWeapon magicWeapon;
        private final Vec3 origin;
        private final Vec3 forward;
        private final Vec3 right;
        private final List<PendingSpikeBurst> pendingBursts = new ArrayList<>();
        private final Set<UUID> hitTargets = new HashSet<>();
        private final long startGameTime;
        private long nextWaveGameTime;
        private int waveStep;

        private ActiveGlacierCast(ServerLevel level, UUID casterId, ItemStack weaponStack, MagicMeleeWeapon magicWeapon,
                                  Vec3 origin, Vec3 forward, Vec3 right, long startGameTime) {
            this.level = level;
            this.casterId = casterId;
            this.weaponStack = weaponStack;
            this.magicWeapon = magicWeapon;
            this.origin = origin;
            this.forward = forward;
            this.right = right;
            this.startGameTime = startGameTime;
            this.nextWaveGameTime = startGameTime;
            this.waveStep = 0;
        }
    }

    public GlacierSkill(SimpleWeaponInnateSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
                EpicFightEventHooks.Animation.BEGIN,
                event -> {
                    if (container.getExecutor().isLogicalClient()) {
                        return;
                    }

                    if (event.getAnimation() != PBAnimations.GLACIER) {
                        return;
                    }

                    LivingEntity caster = container.getExecutor().getOriginal();
                    WeaponVisualEffectHelper.startGlacierCast(caster);
                    caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), PBWSounds.BLIZZARD.get(), SoundSource.PLAYERS, 1.25F, 1.0F);
                },
                this
        );

        eventListener.registerEvent(
                EpicFightEventHooks.Animation.ATTACK_PHASE_END,
                event -> {
                    if (container.getExecutor().isLogicalClient()) {
                        return;
                    }

                    if (event.getAnimation() != PBAnimations.GLACIER) {
                        return;
                    }

                    if (event.getPhaseOrder() != 0) {
                        return;
                    }

                    if (!(event.getEntityPatch() instanceof ServerPlayerPatch playerPatch)) {
                        return;
                    }

                    WeaponVisualEffectHelper.stopGlacierCast(playerPatch.getOriginal());
                    WeaponVisualEffectHelper.playGlacierContactWave(playerPatch.getOriginal());
                    spawnGlacier(playerPatch, playerPatch.getOriginal().getMainHandItem());
                },
                this
        );

        eventListener.registerEvent(
                EpicFightEventHooks.Animation.END,
                event -> {
                    if (event.getAnimation() != PBAnimations.GLACIER) {
                        return;
                    }

                    WeaponVisualEffectHelper.stopGlacierCast(container.getExecutor().getOriginal());
                },
                this
        );
    }

    @Override
    public void onRemoved(SkillContainer container) {
        WeaponVisualEffectHelper.stopGlacierCast(container.getExecutor().getOriginal());
        super.onRemoved(container);
    }
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_CASTS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, ActiveGlacierCast>> iterator = ACTIVE_CASTS.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveGlacierCast cast = iterator.next().getValue();
            Entity entity = cast.level.getEntity(cast.casterId);
            if (!(entity instanceof LivingEntity caster) || !caster.isAlive()) {
                iterator.remove();
                continue;
            }

            long gameTime = cast.level.getGameTime();
            processPendingBursts(cast, caster, gameTime);
            processParticleWave(cast, gameTime);

            if (cast.pendingBursts.isEmpty() && cast.waveStep >= PARTICLE_WAVE_STEPS) {
                iterator.remove();
            }
        }
    }

    private static void spawnGlacier(ServerPlayerPatch playerPatch, ItemStack weaponStack) {
        LivingEntity caster = playerPatch.getOriginal();
        if (!(caster.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(weaponStack.getItem() instanceof MagicMeleeWeapon magicWeapon)) {
            return;
        }

        Vec3 forward = getFlatForward(caster);
        if (forward.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x).normalize();
        Vec3 origin = caster.position();
        long gameTime = serverLevel.getGameTime();
        ActiveGlacierCast cast = new ActiveGlacierCast(serverLevel, caster.getUUID(), weaponStack.copy(), magicWeapon, origin, forward, right, gameTime);

        scheduleOpenRing(cast, new RingSpec(OUTER_SPIKE_COUNT, OUTER_FRONT_RADIUS, OUTER_BACK_RADIUS, OUTER_SIDE_RADIUS, 4.5D, 0, OUTER_WAIT_STEP, OUTER_BACK_SCALE, OUTER_FRONT_SCALE));
        scheduleOpenRing(cast, new RingSpec(INNER_SPIKE_COUNT, INNER_FRONT_RADIUS, INNER_BACK_RADIUS, INNER_SIDE_RADIUS, 2.5D, INNER_WAIT_START, INNER_WAIT_STEP, INNER_BACK_SCALE, INNER_FRONT_SCALE));
        applyFrozenArea(serverLevel, caster, origin, forward, right, OUTER_FRONT_RADIUS, OUTER_BACK_RADIUS, OUTER_SIDE_RADIUS);
        ACTIVE_CASTS.put(caster.getUUID(), cast);
    }

    private static void processPendingBursts(ActiveGlacierCast cast, LivingEntity caster, long gameTime) {
        Iterator<PendingSpikeBurst> iterator = cast.pendingBursts.iterator();
        while (iterator.hasNext()) {
            PendingSpikeBurst burst = iterator.next();
            if (gameTime < burst.triggerGameTime()) {
                continue;
            }

            damageAndChillNearby(cast.level, caster, cast.weaponStack, cast.magicWeapon, burst.spikeEntity(), burst.spikePos(), cast.hitTargets);
            iterator.remove();
        }
    }

    private static void processParticleWave(ActiveGlacierCast cast, long gameTime) {
        if (cast.waveStep >= PARTICLE_WAVE_STEPS || gameTime < cast.nextWaveGameTime) {
            return;
        }

        float progress = (cast.waveStep + 1) / (float) PARTICLE_WAVE_STEPS;
        double radius = Mth.lerp(progress, 0.45D, OUTER_FRONT_RADIUS + 0.65D);
        double centerY = cast.origin.y + 0.12D;
        spawnParticleRing(cast.level, cast.origin, centerY, WAVE_SNOWFLAKE_COUNT, radius, 0.18D, 0.12D + progress * 0.08D, true);
        spawnParticleRing(cast.level, cast.origin, centerY, WAVE_SNOW_DUST_COUNT, radius * 0.9D, 0.08D, 0.09D + progress * 0.05D, false);
        cast.waveStep++;
        cast.nextWaveGameTime = gameTime + PARTICLE_WAVE_INTERVAL;
    }

    private static void scheduleOpenRing(ActiveGlacierCast cast, RingSpec ringSpec) {
        int sideCount = ringSpec.spikeCount() / 2;
        if (sideCount <= 0) {
            return;
        }

        double openingHalfAngle = Math.toRadians(OPENING_HALF_ANGLE_DEGREES);
        double backHalfAngle = Math.toRadians(BACK_HALF_ANGLE_DEGREES);

        for (int step = 0; step < sideCount; step++) {
            float progress = sideCount == 1 ? 1.0F : step / (float) (sideCount - 1);
            int waitTime = ringSpec.waitStart() + step * ringSpec.waitStep();
            float spikeScale = Mth.lerp(progress, ringSpec.backScale(), ringSpec.frontScale());

            double rightTheta = Mth.lerp(progress, Math.PI - backHalfAngle, openingHalfAngle);
            double leftTheta = Mth.lerp(progress, Math.PI + backHalfAngle, (Math.PI * 2.0D) - openingHalfAngle);

            spawnScheduledSpike(cast, ringSpec, rightTheta, waitTime, spikeScale, false);
            spawnScheduledSpike(cast, ringSpec, leftTheta, waitTime, spikeScale, true);
        }
    }

    private static void spawnScheduledSpike(ActiveGlacierCast cast, RingSpec ringSpec, double theta, int waitTime, float spikeScale, boolean mirrored) {
        double forwardRadius = Math.cos(theta) >= 0.0D ? ringSpec.frontRadius() : ringSpec.backRadius();
        Vec3 offset = cast.forward.scale(Math.cos(theta) * forwardRadius)
                .add(cast.right.scale(Math.sin(theta) * ringSpec.sideRadius()));
        double offsetLength = offset.length();
        if (offsetLength > 1.0E-6D && offsetLength < ringSpec.minDistance()) {
            offset = offset.normalize().scale(ringSpec.minDistance());
        }
        Vec3 targetPoint = cast.origin.add(offset);
        BlockPos surfacePos = findSurfaceBlock(cast.level, targetPoint.x, cast.origin.y, targetPoint.z);
        if (surfacePos == null) {
            return;
        }

        double targetY = surfacePos.getY() + 1.02D;
        Vec3 targetPos = new Vec3(surfacePos.getX() + 0.5D, targetY, surfacePos.getZ() + 0.5D);
        GlacierIceSpikeEntity spikeEntity = new GlacierIceSpikeEntity(cast.level);
        spikeEntity.setWaitTime(waitTime);
        spikeEntity.setSpikeScale(spikeScale);
        spikeEntity.setMirrored(mirrored);
        spikeEntity.setRiseHeight(Math.max(1.0F, spikeScale * 0.55F));
        spikeEntity.setPos(targetPos.x, targetPos.y - spikeEntity.getRiseHeight(), targetPos.z);
        spikeEntity.setYRot(computeOutwardYaw(cast.origin, targetPos));
        spikeEntity.yRotO = spikeEntity.getYRot();
        spikeEntity.setXRot(-10.0F);
        spikeEntity.xRotO = spikeEntity.getXRot();
        cast.level.addFreshEntity(spikeEntity);
        cast.pendingBursts.add(new PendingSpikeBurst(cast.startGameTime + waitTime + GlacierIceSpikeEntity.RISE_TIME, targetPos, spikeEntity));
    }

    private static void damageAndChillNearby(ServerLevel level, LivingEntity caster, ItemStack weaponStack, MagicMeleeWeapon magicWeapon,
                                             Entity directEntity, Vec3 spikePos, Set<UUID> hitTargets) {
        float weaponMagicArmorNegation = StaffMagicArmorHelper.resolveWeaponMagicArmorNegation(caster, weaponStack);
        float weaponImpact = StaffMagicArmorHelper.resolveWeaponImpact(caster, weaponStack);
        float rawMagicDamage = MagicMeleeWeaponHelper.calculateRawMagicDamage(caster, weaponStack, magicWeapon, 1.0F, 1.0F);
        if (rawMagicDamage <= 0.0F) {
            return;
        }

        AABB area = new AABB(
                spikePos.x - SPIKE_DAMAGE_RADIUS, spikePos.y - 1.0D, spikePos.z - SPIKE_DAMAGE_RADIUS,
                spikePos.x + SPIKE_DAMAGE_RADIUS, spikePos.y + 2.5D, spikePos.z + SPIKE_DAMAGE_RADIUS
        );

        Holder<MobEffect> chilledEffect = MobEffectRegistry.CHILLED;

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity != caster)) {
            if (target.isAlliedTo(caster) || hitTargets.contains(target.getUUID())) {
                continue;
            }

            float adjustedMagicDamage = StaffMagicArmorHelper.applyAdjustedMagicDamage(
                    target,
                    rawMagicDamage,
                    magicWeapon.getSchoolResistMultiplier(target),
                    weaponMagicArmorNegation
            );
            if (adjustedMagicDamage <= 0.0F) {
                continue;
            }

            DamageSource damageSource = MagicMeleeWeaponHelper.createMagicProjectileDamageSource(
                    caster,
                    directEntity,
                    weaponStack,
                    magicWeapon.getMagicDamageType(),
                    weaponMagicArmorNegation,
                    weaponImpact,
                    StunType.SHORT
            );

            if (damageSource instanceof EpicFightDamageSource epicFightDamageSource) {
                epicFightDamageSource.addRuntimeTag(EpicFightDamageTypeTags.WEAPON_INNATE);
            }

            int originalInvulnerableTime = target.invulnerableTime;
            boolean damaged;
            target.invulnerableTime = 0;
            try {
                damaged = target.hurt(damageSource, adjustedMagicDamage);
            } finally {
                target.invulnerableTime = originalInvulnerableTime;
            }

            if (!damaged) {
                continue;
            }

            target.addEffect(new MobEffectInstance(chilledEffect, CHILL_DURATION_TICKS, 0, false, true, true));
            hitTargets.add(target.getUUID());
        }
    }

    private static void applyFrozenArea(ServerLevel level, LivingEntity caster, Vec3 origin, Vec3 forward, Vec3 right,
                                        double frontRadius, double backRadius, double sideRadius) {
        double maxRadius = Math.max(frontRadius, Math.max(backRadius, sideRadius)) + 1.0D;
        AABB area = new AABB(
                origin.x - maxRadius, origin.y - 1.5D, origin.z - maxRadius,
                origin.x + maxRadius, origin.y + 2.5D, origin.z + maxRadius
        );

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity != caster)) {
            if (target.isAlliedTo(caster)) {
                continue;
            }

            Vec3 delta = target.position().subtract(origin);
            double along = delta.dot(forward);
            double side = delta.dot(right);
            double allowedForward = along >= 0.0D ? frontRadius : backRadius;
            double normalized = (along * along) / (allowedForward * allowedForward) + (side * side) / (sideRadius * sideRadius);
            if (normalized <= 1.0D) {
                target.addEffect(new MobEffectInstance(PBMEffects.FROZEN, FROZEN_DURATION_TICKS, 0, false, true, true));
            }
        }
    }

    private static void spawnParticleRing(ServerLevel level, Vec3 origin, double y, int count, double radius, double verticalJitter,
                                          double outwardSpeed, boolean snowflake) {
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0D * i) / count;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double spawnX = origin.x + cos * radius;
            double spawnY = y + ((i & 1) == 0 ? verticalJitter : 0.0D);
            double spawnZ = origin.z + sin * radius;
            double velocityX = cos * outwardSpeed;
            double velocityZ = sin * outwardSpeed;
            if (snowflake) {
                level.sendParticles(ParticleHelper.SNOWFLAKE, spawnX, spawnY, spawnZ, 1, velocityX, 0.02D, velocityZ, 0.0D);
            } else {
                level.sendParticles(ParticleHelper.SNOW_DUST, spawnX, spawnY, spawnZ, 1, velocityX, 0.01D, velocityZ, 0.0D);
            }
        }
    }

    private static Vec3 getFlatForward(LivingEntity caster) {
        Vec3 look = caster.getLookAngle();
        Vec3 flatForward = new Vec3(look.x, 0.0D, look.z);
        if (flatForward.lengthSqr() < 1.0E-6D) {
            return Vec3.ZERO;
        }
        return flatForward.normalize();
    }

    private static float computeOutwardYaw(Vec3 origin, Vec3 spikePos) {
        Vec3 delta = spikePos.subtract(origin);
        return (float) (Mth.atan2(delta.z, delta.x) * (180.0D / Math.PI)) - 90.0F;
    }

    private static BlockPos findSurfaceBlock(ServerLevel level, double x, double y, double z) {
        int baseY = Mth.floor(y);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(Mth.floor(x), baseY + 1, Mth.floor(z));

        for (int dy = 0; dy < 6; dy++) {
            mutable.setY(baseY + 1 - dy);
            BlockState state = level.getBlockState(mutable);
            if (!state.isAir() && state.getRenderShape() != RenderShape.INVISIBLE) {
                return mutable.immutable();
            }
        }

        return null;
    }
}










