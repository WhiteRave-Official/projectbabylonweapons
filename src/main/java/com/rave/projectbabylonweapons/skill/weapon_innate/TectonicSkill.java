package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.init.PBWSounds;
import com.rave.projectbabylonweapons.world.entity.effect.TectonicFallingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.SimpleWeaponInnateSkill;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TectonicSkill extends SimpleWeaponInnateSkill {

    private static final UUID TECTONIC_BEGIN_UUID = UUID.fromString("f306ba6f-4842-455e-b9ec-2ca8c6f79e5d");
    private static final int CONCUSSED_DURATION_TICKS = 7 * 20;
    private static final float KNOCKUP_VELOCITY = 0.90F;
    private static final float HORIZONTAL_PUSH = 0.24F;
    private static final float WAVE_DAMAGE_MULTIPLIER = 0.5F;
    private static final double WAVE_RANGE = 10.0D;
    private static final double WAVE_HALF_ANGLE_DEGREES = 38.0D;
    private static final double RING_THICKNESS = 1.0D;
    private static final int RING_DELAY_TICKS = 2;
    private static final int WAVE_START_DELAY_TICKS = 40;
    private static final float BLOCK_POPUP_INITIAL_VELOCITY = 0.32F;
    private static final Map<UUID, CastSnapshot> PENDING_CASTS = new HashMap<>();
    private static final Map<UUID, WaveState> ACTIVE_WAVES = new HashMap<>();

    private static class CastSnapshot {
        private final Vec3 origin;
        private final Vec3 forward;

        private CastSnapshot(Vec3 origin, Vec3 forward) {
            this.origin = origin;
            this.forward = forward;
        }
    }

    private static class WaveState {
        private final Vec3 origin;
        private final Vec3 forward;
        private final Vec3 right;
        private final ServerLevel level;
        private final long triggerGameTime;
        private int currentRing = 1;
        private long nextRingGameTime;
        private final Set<BlockPos> spawnedPositions = new HashSet<>();
        private final Set<UUID> hitTargets = new HashSet<>();

        private WaveState(ServerLevel level, Vec3 origin, Vec3 forward, long triggerGameTime) {
            this.level = level;
            this.origin = origin;
            this.forward = forward;
            this.right = new Vec3(-forward.z, 0.0D, forward.x).normalize();
            this.triggerGameTime = triggerGameTime;
            this.nextRingGameTime = triggerGameTime;
        }
    }

    public TectonicSkill(SimpleWeaponInnateSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
                EpicFightEventHooks.Animation.BEGIN,
                (event) -> {
                    if (container.getExecutor().isLogicalClient()) {
                        return;
                    }

                    if (event.getAnimation() != PBAnimations.TECTONIC) {
                        return;
                    }

                    LivingEntity caster = container.getExecutor().getOriginal();
                    captureCastSnapshot(caster);
                    queueWaveFromSnapshot(caster);
                },
                this
        );

    }

    @Override
    public void onRemoved(SkillContainer container) {
        PENDING_CASTS.remove(container.getExecutor().getOriginal().getUUID());
        super.onRemoved(container);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_WAVES.isEmpty()) {
            return;
        }

        var iterator = ACTIVE_WAVES.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            UUID casterId = entry.getKey();
            WaveState waveState = entry.getValue();
            ServerLevel serverLevel = waveState.level;

            Entity entity = serverLevel.getEntity(casterId);
            if (!(entity instanceof LivingEntity caster) || !caster.isAlive()) {
                iterator.remove();
                continue;
            }

            long gameTime = serverLevel.getGameTime();
            if (gameTime < waveState.triggerGameTime || gameTime < waveState.nextRingGameTime) {
                continue;
            }

            processWaveRing(caster, serverLevel, waveState, waveState.currentRing);
            waveState.currentRing++;
            waveState.nextRingGameTime = gameTime + RING_DELAY_TICKS;

            if (waveState.currentRing > (int) WAVE_RANGE) {
                iterator.remove();
            }
        }
    }

    private static void captureCastSnapshot(LivingEntity caster) {
        Vec3 forward = caster.getLookAngle();
        Vec3 flatForward = new Vec3(forward.x, 0.0D, forward.z);
        if (flatForward.lengthSqr() < 1.0E-6D) {
            return;
        }
        flatForward = flatForward.normalize();
        PENDING_CASTS.put(caster.getUUID(), new CastSnapshot(caster.position(), flatForward));
    }

    private static void queueWaveFromSnapshot(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        CastSnapshot snapshot = PENDING_CASTS.remove(caster.getUUID());
        Vec3 origin;
        Vec3 flatForward;

        if (snapshot != null) {
            origin = snapshot.origin;
            flatForward = snapshot.forward;
        } else {
            Vec3 forward = caster.getLookAngle();
            flatForward = new Vec3(forward.x, 0.0D, forward.z);
            if (flatForward.lengthSqr() < 1.0E-6D) {
                return;
            }
            flatForward = flatForward.normalize();
            origin = caster.position();
        }

        long triggerTime = serverLevel.getGameTime() + WAVE_START_DELAY_TICKS;
        ACTIVE_WAVES.put(caster.getUUID(), new WaveState(serverLevel, origin, flatForward, triggerTime));
        serverLevel.playSound(null, origin.x, origin.y, origin.z, PBWSounds.RUMBLING_GROUND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void processWaveRing(LivingEntity caster, ServerLevel serverLevel, WaveState waveState, int distanceRing) {
        DamageSource waveDamageSource = createWaveDamageSource(caster);
        float baseAttackDamage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float waveDamage = Math.max(1.0F, baseAttackDamage * WAVE_DAMAGE_MULTIPLIER);
        double minDistance = Math.max(0.0D, distanceRing - RING_THICKNESS);
        double maxDistance = distanceRing + RING_THICKNESS;

        AABB ringArea = new AABB(
                waveState.origin.x - maxDistance, waveState.origin.y - 2.0D, waveState.origin.z - maxDistance,
                waveState.origin.x + maxDistance, waveState.origin.y + 3.0D, waveState.origin.z + maxDistance
        );

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, ringArea, e -> e.isAlive() && e != caster)) {
            if (target.isAlliedTo(caster) || waveState.hitTargets.contains(target.getUUID())) {
                continue;
            }

            Vec3 toTarget = target.position().subtract(waveState.origin);
            Vec3 horizontalToTarget = new Vec3(toTarget.x, 0.0D, toTarget.z);
            double along = horizontalToTarget.dot(waveState.forward);
            if (along < minDistance || along > maxDistance) {
                continue;
            }

            if (!isInsideWaveCone(along, horizontalToTarget, waveState)) {
                continue;
            }

            int originalInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            try {
                target.hurt(waveDamageSource, waveDamage);
            } finally {
                target.invulnerableTime = originalInvulnerableTime;
            }

            Vec3 away = target.position().subtract(waveState.origin);
            Vec3 horizontalAway = new Vec3(away.x, 0.0D, away.z);
            Vec3 push = horizontalAway.lengthSqr() > 1.0E-6D
                    ? horizontalAway.normalize().scale(HORIZONTAL_PUSH)
                    : Vec3.ZERO;

            target.setDeltaMovement(push.x, KNOCKUP_VELOCITY, push.z);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(PBMEffects.CONCUSSED, CONCUSSED_DURATION_TICKS, 0, false, true, true));
            waveState.hitTargets.add(target.getUUID());
        }

        spawnWaveBlockRing(serverLevel, waveState, distanceRing);
    }

    private static DamageSource createWaveDamageSource(LivingEntity caster) {
        if (caster instanceof Player player) {
            return caster.damageSources().playerAttack(player);
        }
        return caster.damageSources().mobAttack(caster);
    }

    private static boolean isInsideWaveCone(double along, Vec3 horizontalToTarget, WaveState waveState) {
        if (along <= 0.05D || along > WAVE_RANGE) {
            return false;
        }

        double side = Math.abs(horizontalToTarget.dot(waveState.right));
        double maxSide = Math.tan(Math.toRadians(WAVE_HALF_ANGLE_DEGREES)) * along;
        return side <= maxSide;
    }

    private static void spawnWaveBlockRing(ServerLevel level, WaveState waveState, int distanceRing) {
        double width = Math.tan(Math.toRadians(WAVE_HALF_ANGLE_DEGREES)) * distanceRing;
        int segmentCount = Math.max(2, Mth.ceil(width * 2.0D));

        for (int i = -segmentCount; i <= segmentCount; i++) {
            double sideOffset = (i / (double) segmentCount) * width;
            Vec3 point = waveState.origin
                    .add(waveState.forward.scale(distanceRing))
                    .add(waveState.right.scale(sideOffset));
            double x = point.x;
            double z = point.z;

            BlockPos surfacePos = findSurfaceBlock(level, x, waveState.origin.y, z);
            if (surfacePos == null || !waveState.spawnedPositions.add(surfacePos)) {
                continue;
            }

            BlockState state = level.getBlockState(surfacePos);
            if (state.getRenderShape() == RenderShape.INVISIBLE) {
                continue;
            }

            TectonicFallingBlockEntity blockEntity = new TectonicFallingBlockEntity(level, state, BLOCK_POPUP_INITIAL_VELOCITY);
            blockEntity.setPos(surfacePos.getX() + 0.5D, surfacePos.getY() + 0.5D, surfacePos.getZ() + 0.5D);
            level.addFreshEntity(blockEntity);
        }
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

