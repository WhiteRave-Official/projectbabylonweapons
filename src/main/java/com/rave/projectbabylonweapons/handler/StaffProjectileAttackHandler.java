package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.item.MagicProjectileStaffWeapon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.api.event.types.animation.AttackPhaseEndEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class StaffProjectileAttackHandler {
    private static final IdentifierProvider STAFF_PROJECTILE_PHASE_LISTENER = IdentifierProvider.constant(
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "staff_projectile_phase"));
    private static final Map<UUID, Integer> REGISTERED_PATCH_IDENTITIES = new ConcurrentHashMap<>();

    private StaffProjectileAttackHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), PlayerPatch.class);
        if (playerPatch == null) {
            return;
        }

        int patchIdentity = System.identityHashCode(playerPatch.getEventListener());
        Integer previousIdentity = REGISTERED_PATCH_IDENTITIES.put(event.getEntity().getUUID(), patchIdentity);
        if (previousIdentity == null || previousIdentity != patchIdentity) {
            playerPatch.getEventListener().registerEvent(
                    EpicFightEventHooks.Animation.ATTACK_PHASE_END,
                    StaffProjectileAttackHandler::onAttackPhaseEnd,
                    STAFF_PROJECTILE_PHASE_LISTENER
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        REGISTERED_PATCH_IDENTITIES.remove(event.getEntity().getUUID());

        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), PlayerPatch.class);
        if (playerPatch != null) {
            playerPatch.getEventListener().removeListenersBelongTo(STAFF_PROJECTILE_PHASE_LISTENER);
        }
    }

    private static void onAttackPhaseEnd(AttackPhaseEndEvent event) {
        if (!(event.getEntityPatch() instanceof ServerPlayerPatch playerPatch) || playerPatch.getOriginal() == null) {
            return;
        }

        Player player = playerPatch.getOriginal();
        if (player.level().isClientSide) {
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof MagicProjectileStaffWeapon weapon)) {
            return;
        }

        if (!weapon.shouldFireMagicProjectile(event.getAnimation(), event.getPhase(), event.getPhaseOrder())) {
            return;
        }

        BattleWandPassiveHooks.onAttackPhaseEnd(playerPatch, player.getMainHandItem());
        weapon.fireMagicProjectiles(playerPatch, player.getMainHandItem(), event);
    }
}
