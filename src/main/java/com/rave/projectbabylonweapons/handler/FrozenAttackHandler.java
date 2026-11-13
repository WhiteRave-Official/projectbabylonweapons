package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonmaterials.init.PBMEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class FrozenAttackHandler {
    private static final UUID FROST_SKILL_LISTENER = UUID.fromString("f1e2d3c4-b5a6-7890-1234-56789abcdef0");
    private static final Map<UUID, Boolean> FROST_STATES = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Player player = event.player;
        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (playerPatch == null) return;

        UUID playerId = player.getUUID();
        boolean hasFrost = player.hasEffect(PBMEffects.FROZEN.get());
        Boolean previousFrost = FROST_STATES.get(playerId);

        if (previousFrost == null || previousFrost != hasFrost) {
            if (hasFrost) {
                addFrostListeners(playerPatch);
            } else {
                removeFrostListeners(playerPatch);
            }
            FROST_STATES.put(playerId, hasFrost);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == PBMEffects.FROZEN.get()) {
            removeListenersForEntity(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance().getEffect() == PBMEffects.FROZEN.get()) {
            removeListenersForEntity(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        removeListenersForEntity(event.getEntity());
        FROST_STATES.remove(event.getEntity().getUUID());
    }

    private static void removeListenersForEntity(LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (playerPatch != null) {
            removeFrostListeners(playerPatch);
        }
        FROST_STATES.remove(player.getUUID());
    }

    private static void addFrostListeners(PlayerPatch<?> playerPatch) {
        try {
            playerPatch.getEventListener().addEventListener(
                    PlayerEventListener.EventType.SKILL_CAST_EVENT,
                    FROST_SKILL_LISTENER,
                    (SkillCastEvent event) -> event.setCanceled(true)
            );
        } catch (Exception e) {

        }
    }

    private static void removeFrostListeners(PlayerPatch<?> playerPatch) {
        try {
            playerPatch.getEventListener().removeListener(
                    PlayerEventListener.EventType.SKILL_CAST_EVENT,
                    FROST_SKILL_LISTENER
            );
        } catch (Exception e) {

        }
    }
}
