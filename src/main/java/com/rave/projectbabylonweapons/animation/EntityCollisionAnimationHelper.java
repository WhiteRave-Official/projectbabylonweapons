package com.rave.projectbabylonweapons.animation;

import com.rave.projectbabylonweapons.gameasset.PBAnimationProperties;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public final class EntityCollisionAnimationHelper {
    private EntityCollisionAnimationHelper() {
    }

    public static boolean ignoresEntityCollision(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(livingEntity, LivingEntityPatch.class);
        if (patch == null) {
            return false;
        }

        AnimationPlayer player = patch.getAnimator().getPlayerFor(null);
        if (player == null || player.getRealAnimation() == null || player.getRealAnimation().get() == null) {
            return false;
        }

        return player.getRealAnimation().get()
                .getProperty(PBAnimationProperties.IGNORE_ENTITY_COLLISION)
                .orElse(false);
    }
}
