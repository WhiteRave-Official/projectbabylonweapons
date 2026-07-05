package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.network.PBNetworkManager;
import com.rave.projectbabylonweapons.network.SPFrozenVisualSync;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class FrozenEffectHandler {

    private static final ResourceLocation FROZEN_MOVEMENT_ID = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "frozen_movement");
    private static final int VISUAL_SYNC_HEARTBEAT_TICKS = 20;
    private static final int FROZEN_SNOWFLAKE_COUNT = 36;
    private static final int FROZEN_SNOW_DUST_COUNT = 20;
    private static final float HIGH_HP_DURATION_MULTIPLIER = 0.25F;
    private static final float HIGH_HP_THRESHOLD = 60.0F;
    private static final Map<UUID, FrozenRotation> FROZEN_ROTATIONS = new ConcurrentHashMap<>();
    private static final Set<UUID> SYNCED_FROZEN_VISUALS = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> HIGH_HP_DURATION_ADJUSTED = ConcurrentHashMap.newKeySet();


    private static class FrozenRotation {
        float yRot;
        float yHeadRot;
        float yBodyRot;

        FrozenRotation(LivingEntity entity) {
            this.yRot = entity.getYRot();
            this.yHeadRot = entity.getYHeadRot();
            this.yBodyRot = entity.yBodyRot;
        }

        void applyTo(LivingEntity entity) {
            entity.setYRot(this.yRot);
            entity.yRotO = this.yRot;
            entity.setYHeadRot(this.yHeadRot);
            entity.yHeadRotO = this.yHeadRot;
            entity.setYBodyRot(this.yBodyRot);
            entity.yBodyRotO = this.yBodyRot;
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        UUID entityId = entity.getUUID();
        boolean hasFrozen = entity.hasEffect(PBMEffects.FROZEN);

        if (!entity.level().isClientSide) {
            boolean syncedFrozen = SYNCED_FROZEN_VISUALS.contains(entityId);

            if (hasFrozen && !syncedFrozen) {
                PBNetworkManager.sendToTrackingAndSelf(entity, new SPFrozenVisualSync(entity.getId(), true));
                SYNCED_FROZEN_VISUALS.add(entityId);
            } else if (!hasFrozen && syncedFrozen) {
                PBNetworkManager.sendToTrackingAndSelf(entity, new SPFrozenVisualSync(entity.getId(), false));
                SYNCED_FROZEN_VISUALS.remove(entityId);
            } else if ((hasFrozen || syncedFrozen) && entity.tickCount % VISUAL_SYNC_HEARTBEAT_TICKS == 0) {
                PBNetworkManager.sendToTrackingAndSelf(entity, new SPFrozenVisualSync(entity.getId(), hasFrozen));
                if (hasFrozen) {
                    SYNCED_FROZEN_VISUALS.add(entityId);
                } else {
                    SYNCED_FROZEN_VISUALS.remove(entityId);
                }
            }
        }

        if (!entity.level().isClientSide) {
            if (hasFrozen) {
                adjustFrozenDurationIfNeeded(entity);
            } else {
                HIGH_HP_DURATION_ADJUSTED.remove(entityId);
            }
        }

        if (!entity.isAlive()) {
            if (!entity.level().isClientSide && SYNCED_FROZEN_VISUALS.remove(entityId)) {
                PBNetworkManager.sendToTrackingAndSelf(entity, new SPFrozenVisualSync(entity.getId(), false));
            }
            clearFrozenState(entity);
            return;
        }

        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

        if (hasFrozen) {


            if (patch != null) {
                Animator animator = patch.getAnimator();
                animator.setSoftPause(true);


                patch.cancelItemUse();
            }


            entity.setDeltaMovement(Vec3.ZERO);
            entity.xxa = 0.0F;
            entity.yya = 0.0F;
            entity.zza = 0.0F;

            if (entity.getDeltaMovement().y > 0) {
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            }


            if (!entity.level().isClientSide) {
                AttributeInstance moveSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                if (moveSpeed != null && moveSpeed.getModifier(FROZEN_MOVEMENT_ID) == null) {
                    moveSpeed.addTransientModifier(new AttributeModifier(
                            FROZEN_MOVEMENT_ID,
                            -1.0,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    ));
                }


                if (entity instanceof Mob mob) {
                    mob.setNoAi(true);
                }
            }


            FROZEN_ROTATIONS.computeIfAbsent(entityId, k -> new FrozenRotation(entity));
            FROZEN_ROTATIONS.get(entityId).applyTo(entity);

        } else {

            if (patch != null) {
                Animator animator = patch.getAnimator();
                animator.setSoftPause(false);
            }


            if (!entity.level().isClientSide) {
                AttributeInstance moveSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                if (moveSpeed != null) {
                    moveSpeed.removeModifier(FROZEN_MOVEMENT_ID);
                }

                if (entity instanceof Mob mob) {
                    mob.setNoAi(false);
                }
            }

            FROZEN_ROTATIONS.remove(entityId);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == PBMEffects.FROZEN) {
            if (!event.getEntity().level().isClientSide) {
                PBNetworkManager.sendToTrackingAndSelf(event.getEntity(), new SPFrozenVisualSync(event.getEntity().getId(), false));
                SYNCED_FROZEN_VISUALS.remove(event.getEntity().getUUID());
            }
            HIGH_HP_DURATION_ADJUSTED.remove(event.getEntity().getUUID());
            clearFrozenState(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide) {
            PBNetworkManager.sendToTrackingAndSelf(event.getEntity(), new SPFrozenVisualSync(event.getEntity().getId(), false));
            SYNCED_FROZEN_VISUALS.remove(event.getEntity().getUUID());
        }
        HIGH_HP_DURATION_ADJUSTED.remove(event.getEntity().getUUID());
        clearFrozenState(event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || !entity.hasEffect(PBMEffects.FROZEN)) {
            return;
        }

        if (event.getNewDamage() < entity.getHealth()) {
            return;
        }

        PBNetworkManager.sendToTrackingAndSelf(entity, new SPFrozenVisualSync(entity.getId(), false));
        SYNCED_FROZEN_VISUALS.remove(entity.getUUID());
        HIGH_HP_DURATION_ADJUSTED.remove(entity.getUUID());
        entity.removeEffect(PBMEffects.FROZEN);
        clearFrozenState(entity);
    }



    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() == PBMEffects.FROZEN) {
            LivingEntity entity = event.getEntity();

            if (!entity.level().isClientSide) {
                if (adjustFrozenDurationIfNeeded(entity)) {
                    return;
                }
            }

            if (!entity.level().isClientSide) {
                PBNetworkManager.sendToTrackingAndSelf(entity, new SPFrozenVisualSync(entity.getId(), true));
                SYNCED_FROZEN_VISUALS.add(entity.getUUID());
                spawnFrozenParticles(entity);
                entity.level().playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        com.rave.projectbabylonweapons.init.PBWSounds.GOT_FROZEN.get(),
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(event.getTarget() instanceof LivingEntity living)) {
            return;
        }

        if (living.hasEffect(PBMEffects.FROZEN)) {
            PBNetworkManager.sendToPlayer(player, new SPFrozenVisualSync(living.getId(), true));
            SYNCED_FROZEN_VISUALS.add(living.getUUID());
        }
    }

    @SubscribeEvent
    public static void onStopTracking(PlayerEvent.StopTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getTarget() instanceof LivingEntity living) {
            PBNetworkManager.sendToPlayer(player, new SPFrozenVisualSync(living.getId(), false));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.hasEffect(PBMEffects.FROZEN)) {
            PBNetworkManager.sendToPlayer(player, new SPFrozenVisualSync(player.getId(), true));
            SYNCED_FROZEN_VISUALS.add(player.getUUID());
        }
    }

    private static void clearFrozenState(LivingEntity entity) {
        SYNCED_FROZEN_VISUALS.remove(entity.getUUID());
        HIGH_HP_DURATION_ADJUSTED.remove(entity.getUUID());

        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

        if (patch != null) {
            patch.getAnimator().setSoftPause(false);
            if (entity.level().isClientSide) {
                patch.updateMotion(true);
            }
        }

        if (!entity.level().isClientSide) {
            AttributeInstance moveSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeed != null) {
                moveSpeed.removeModifier(FROZEN_MOVEMENT_ID);
            }

            if (entity instanceof Mob mob) {
                mob.setNoAi(false);
            }
        }

        FROZEN_ROTATIONS.remove(entity.getUUID());
    }

    private static void spawnFrozenParticles(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double centerY = entity.getY() + entity.getBbHeight() * 0.55D;
        double spreadX = Math.max(0.25D, entity.getBbWidth() * 0.55D);
        double spreadY = Math.max(0.20D, entity.getBbHeight() * 0.30D);
        double spreadZ = Math.max(0.25D, entity.getBbWidth() * 0.55D);

        serverLevel.sendParticles(ParticleHelper.SNOWFLAKE, entity.getX(), centerY, entity.getZ(),
                FROZEN_SNOWFLAKE_COUNT, spreadX, spreadY, spreadZ, 0.04D);
        serverLevel.sendParticles(ParticleHelper.SNOW_DUST, entity.getX(), centerY, entity.getZ(),
                FROZEN_SNOW_DUST_COUNT, spreadX, spreadY * 0.75D, spreadZ, 0.02D);
    }

    public static int getAdjustedFrozenDuration(LivingEntity entity, int durationTicks) {
        if (entity.getMaxHealth() >= HIGH_HP_THRESHOLD) {
            int reduced = Math.round(durationTicks * HIGH_HP_DURATION_MULTIPLIER);
            return Math.max(1, reduced);
        }
        return durationTicks;
    }

    private static boolean adjustFrozenDurationIfNeeded(LivingEntity entity) {
        if (entity.getMaxHealth() < HIGH_HP_THRESHOLD) {
            return false;
        }

        UUID entityId = entity.getUUID();
        if (HIGH_HP_DURATION_ADJUSTED.contains(entityId)) {
            return false;
        }

        MobEffectInstance current = entity.getEffect(PBMEffects.FROZEN);
        if (current == null) {
            return false;
        }

        int originalDuration = current.getDuration();
        int adjustedDuration = Math.max(1, Math.round(originalDuration * HIGH_HP_DURATION_MULTIPLIER));
        if (adjustedDuration >= originalDuration) {
            return false;
        }

        entity.removeEffect(PBMEffects.FROZEN);
        entity.addEffect(new MobEffectInstance(PBMEffects.FROZEN, adjustedDuration));
        HIGH_HP_DURATION_ADJUSTED.add(entityId);
        return true;
    }
}

