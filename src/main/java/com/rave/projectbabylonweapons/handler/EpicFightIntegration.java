package com.rave.projectbabylonweapons.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class EpicFightIntegration {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final IdentifierProvider FEAR_LISTENER = IdentifierProvider.constant(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "fear_restriction"));

    private static final Map<UUID, Boolean> FEAR_STATES = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
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
            playerPatch.getEventListener().registerContextAwareEvent(
                    EpicFightEventHooks.Player.CAST_SKILL,
                    (SkillCastEvent event, yesman.epicfight.api.event.EventContext context) -> {
                        var skillContainer = event.getSkillContainer();
                        if (skillContainer != null && skillContainer.getSkill() != null) {
                            String skillName = skillContainer.getSkill().toString().toLowerCase();

                            if ((skillName.contains("dash") && !skillName.contains("basic")) ||
                                    skillName.contains("roll") ||
                                    skillName.contains("rush") ||
                                    skillName.contains("dodge") ||
                                    skillName.contains("step") ||
                                    skillName.contains("run")) {

                                event.cancel();
                            }
                        }
                    },
                    FEAR_LISTENER
            );
        } catch (Exception e) {
            LOGGER.error("[ProjectBabylonWeapons] Failed to add listeners: {}", e.getMessage(), e);
        }
    }

    private static void removeListeners(PlayerPatch<?> playerPatch) {
        try {
            playerPatch.getEventListener().removeListenersBelongTo(FEAR_LISTENER);
        } catch (Exception e) {
            // No-op: listeners may already be removed.
        }
    }
}
