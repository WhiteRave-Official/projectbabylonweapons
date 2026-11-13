package com.rave.projectbabylonweapons.handler;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yesman.epicfight.api.animation.types.DashAttackAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.AnimationBeginEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class EpicFightIntegration {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final UUID FEAR_SKILL_LISTENER = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID FEAR_ANIMATION_LISTENER = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f1234567890a");

    private static final Map<UUID, Boolean> FEAR_STATES = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (playerPatch == null) {
            return;
        }

        UUID playerId = player.getUUID();
        boolean restricted = FearHandler.isMobilityRestricted(player);
        Boolean previousFear = FEAR_STATES.get(playerId);

        if (previousFear == null || previousFear != restricted) {
            if (restricted) {
                addListeners(playerPatch);
            } else {
                removeListeners(playerPatch);
            }
            FEAR_STATES.put(playerId, restricted);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        FEAR_STATES.remove(playerId);
    }

    private static void addListeners(PlayerPatch<?> playerPatch) {
        try {
            playerPatch.getEventListener().addEventListener(
                    PlayerEventListener.EventType.SKILL_CAST_EVENT,
                    FEAR_SKILL_LISTENER,
                    (SkillCastEvent event) -> {
                        var skillContainer = event.getSkillContainer();
                        if (skillContainer != null && skillContainer.getSkill() != null) {
                            String skillName = skillContainer.getSkill().toString().toLowerCase();

                            if ((skillName.contains("dash") && !skillName.contains("basic")) ||
                                    skillName.contains("roll") ||
                                    skillName.contains("rush") ||
                                    skillName.contains("dodge") ||
                                    skillName.contains("step") ||
                                    skillName.contains("run")) {

                                event.setCanceled(true);
                            }
                        }
                    }
            );

            playerPatch.getEventListener().addEventListener(
                    PlayerEventListener.EventType.ANIMATION_BEGIN_EVENT,
                    FEAR_ANIMATION_LISTENER,
                    (AnimationBeginEvent event) -> {
                        try {
                            var animation = event.getAnimation();
                            if (animation == null) {
                                return;
                            }

                            if (FearHandler.isAnimationClassBlocked(animation)) {
                                event.setCanceled(true);
                                return;
                            }

                            try {
                                var animId = animation.getRegistryName();
                                if (animId != null && FearHandler.isAnimationBlocked(animId)) {
                                    event.setCanceled(true);
                                }
                            } catch (NullPointerException npe) {
                                checkAnimationByOtherMeans(event, animation);
                            }
                        } catch (Exception e) {
                            LOGGER.error("[Fear] Failed to check animation: {}", e.getMessage(), e);
                        }
                    }
            );

        } catch (Exception e) {
            LOGGER.error("[ProjectBabylonWeapons] Failed to add listeners: {}", e.getMessage(), e);
        }
    }

    private static void checkAnimationByOtherMeans(AnimationBeginEvent event, Object animation) {
        String animString = animation.toString().toLowerCase();

        if ((animString.contains("dash") && !animString.contains("basic")) ||
                animString.contains("rush") ||
                animString.contains("roll") ||
                animString.contains("step") ||
                animString.contains("run") ||
                animString.contains("evade")) {

            event.setCanceled(true);
        }

        if (animation instanceof DashAttackAnimation) {
            event.setCanceled(true);
        }
    }

    private static void removeListeners(PlayerPatch<?> playerPatch) {
        try {
            playerPatch.getEventListener().removeListener(
                    PlayerEventListener.EventType.SKILL_CAST_EVENT,
                    FEAR_SKILL_LISTENER
            );
            playerPatch.getEventListener().removeListener(
                    PlayerEventListener.EventType.ANIMATION_BEGIN_EVENT,
                    FEAR_ANIMATION_LISTENER
            );
        } catch (Exception e) {

        }
    }
}
