package com.rave.projectbabylonweapons.client;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ArclightAwakeningClientState {
    private static final Set<Integer> ACTIVE_ENTITIES = ConcurrentHashMap.newKeySet();
    private static final Set<Integer> EXPIRING_ENTITIES = ConcurrentHashMap.newKeySet();

    private ArclightAwakeningClientState() {
    }

    public static void start(Entity entity) {
        if (entity != null) {
            ACTIVE_ENTITIES.add(entity.getId());
        }
    }

    public static void stop(Entity entity) {
        if (entity != null) {
            ACTIVE_ENTITIES.remove(entity.getId());
        }
    }

    public static void startExpiration(Entity entity) {
        if (entity != null) {
            EXPIRING_ENTITIES.add(entity.getId());
        }
    }

    public static void stopExpiration(Entity entity) {
        if (entity != null) {
            EXPIRING_ENTITIES.remove(entity.getId());
        }
    }

    public static boolean isExpirationActive(LivingEntityPatch<?> entityPatch) {
        return entityPatch != null && EXPIRING_ENTITIES.contains(entityPatch.getOriginal().getId());
    }

    public static float getProgress(LivingEntityPatch<?> entityPatch, float partialTicks) {
        if (entityPatch == null) {
            return 0.0F;
        }

        int entityId = entityPatch.getOriginal().getId();
        boolean active = ACTIVE_ENTITIES.contains(entityId);
        AnimationPlayer player = entityPatch.getAnimator().getPlayerFor(null);
        boolean animationMatches = player != null && player.getRealAnimation() == PBAnimations.ARCLIGHT_AWAKENING;
        if (!active || !animationMatches) {
            return 0.0F;
        }

        float elapsed = Mth.lerp(partialTicks, player.getPrevElapsedTime(), player.getElapsedTime());
        float contact = PBAnimations.ARCLIGHT_AWAKENING.get().phases[0].contact;
        float linear = Mth.clamp(elapsed / Math.max(contact, 0.001F), 0.0F, 1.0F);
        return linear * linear * (3.0F - 2.0F * linear);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE_ENTITIES.clear();
        EXPIRING_ENTITIES.clear();
    }
}
