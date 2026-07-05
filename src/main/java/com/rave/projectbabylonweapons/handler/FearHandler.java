package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.world.entity.projectile.SickleProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public class FearHandler {
    private static final Set<ResourceLocation> BLOCKED_ANIMATIONS = new HashSet<>();
    private static final Set<String> BLOCKED_PATTERNS = new HashSet<>();
    private static final Set<UUID> SICKLE_MOVEMENT_LOCKS = ConcurrentHashMap.newKeySet();

    static {
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("epicfight:biped/combat/roll"));
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("epicfight:biped/combat/dodge"));
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("epicfight:biped/combat/step"));
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("epicfight:biped/combat/step_forward"));
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("epicfight:biped/combat/step_backward"));
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("epicfight:biped/living/run"));
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("epicfight:biped/skill/dash"));
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("epicfight:biped/skill/roll"));
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("epicfight:biped/skill/rush"));

        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("projectbabylonweapons:biped/living/sickle_dual_run"));
        BLOCKED_ANIMATIONS.add(ResourceLocation.parse("projectbabylonweapons:biped/living/sickle_run"));

        BLOCKED_PATTERNS.add("dash");
        BLOCKED_PATTERNS.add("roll");
        BLOCKED_PATTERNS.add("rush");
        BLOCKED_PATTERNS.add("dodge");
        BLOCKED_PATTERNS.add("step");
        BLOCKED_PATTERNS.add("run");
        BLOCKED_PATTERNS.add("charge");
        BLOCKED_PATTERNS.add("leap");
        BLOCKED_PATTERNS.add("sprint");
        BLOCKED_PATTERNS.add("evade");
    }

    public static boolean isFearActive(LivingEntity entity) {
        return entity instanceof Player && entity.hasEffect(PBMEffects.FEAR_DEBUFF);
    }

    public static boolean isSickleLocked(LivingEntity entity) {
        return entity instanceof Player player && SICKLE_MOVEMENT_LOCKS.contains(player.getUUID());
    }

    public static boolean isTemporarilyLocked(LivingEntity entity) {
        return isSickleLocked(entity);
    }

    public static boolean isMobilityRestricted(LivingEntity entity) {
        return isFearActive(entity) || isSickleLocked(entity);
    }

    public static void setSickleMovementLock(UUID playerId, boolean locked, String reason) {
        if (locked) {
            SICKLE_MOVEMENT_LOCKS.add(playerId);
        } else {
            SICKLE_MOVEMENT_LOCKS.remove(playerId);
        }
    }

    public static void setSickleMovementLock(UUID playerId, boolean locked) {
        setSickleMovementLock(playerId, locked, "unspecified");
    }

    public static void clearSickleMovementLock(UUID playerId, String reason) {
        setSickleMovementLock(playerId, false, reason);
    }

    public static void setTemporaryMovementLock(UUID playerId, boolean locked) {
        setSickleMovementLock(playerId, locked, "legacy_alias");
    }

    public static void clearTemporaryMovementLock(UUID playerId) {
        clearSickleMovementLock(playerId, "legacy_alias");
    }

    public static boolean isAnimationBlocked(ResourceLocation animId) {
        if (animId == null) {
            return false;
        }

        if (BLOCKED_ANIMATIONS.contains(animId)) {
            return true;
        }

        String path = animId.getPath().toLowerCase();
        for (String pattern : BLOCKED_PATTERNS) {
            if (path.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAnimationClassBlocked(Object animation) {
        if (animation == null) {
            return false;
        }

        String className = animation.getClass().getSimpleName().toLowerCase();
        if (className.contains("dashattack") ||
                className.contains("rush") ||
                className.contains("roll") ||
                className.contains("step") ||
                className.contains("charge") ||
                className.contains("evade")) {
            return true;
        }

        return false;
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity instanceof Player player) || !isMobilityRestricted(player)) {
            return;
        }


        if (player.isSprinting()) {
            player.setSprinting(false);
        }


        var motion = player.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontalSpeed > 0.15) {
            double scale = 0.15 / horizontalSpeed;
            player.setDeltaMovement(motion.x * scale, motion.y, motion.z * scale);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        if (!isSickleLocked(player)) {
            return;
        }

        boolean hasActiveSickleTether = player.level()
                .getEntitiesOfClass(SickleProjectileEntity.class, player.getBoundingBox().inflate(32.0D), SickleProjectileEntity::isTethered)
                .stream()
                .anyMatch(projectile -> projectile.isTetheredTo(player.getUUID()));

        if (!hasActiveSickleTether) {
            clearSickleMovementLock(player.getUUID(), "safeguard_no_active_tether");
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            clearSickleMovementLock(player.getUUID(), "player_death");
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearSickleMovementLock(event.getEntity().getUUID(), "player_logout");
    }
}

