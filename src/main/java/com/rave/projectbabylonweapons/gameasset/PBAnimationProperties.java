package com.rave.projectbabylonweapons.gameasset;

import com.mojang.serialization.Codec;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.List;

public final class PBAnimationProperties {
    public static final AnimationProperty.StaticAnimationProperty<Boolean> IGNORE_ENTITY_COLLISION =
            new AnimationProperty.StaticAnimationProperty<>("ignore_entity_collision", Codec.BOOL);

    private PBAnimationProperties() {
    }

    public static void applyArclightProperties(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> applyArclightProperties());
    }

    private static void applyArclightProperties() {
        List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> animations = List.of(
                PBAnimations.ARCLIGHT_AUTO_1,
                PBAnimations.ARCLIGHT_AUTO_2,
                PBAnimations.ARCLIGHT_AUTO_3,
                PBAnimations.ARCLIGHT_AUTO_4,
                PBAnimations.ARCLIGHT_DASH,
                PBAnimations.ARCLIGHT_AIRSlASH,
                PBAnimations.EVERGATE_AUTO_1,
                PBAnimations.EVERGATE_AUTO_2,
                PBAnimations.EVERGATE_AUTO_3,
                PBAnimations.EVERGATE_AUTO_4,
                PBAnimations.EVERGATE_AUTO_5,
                PBAnimations.EVERGATE_DASH,
                PBAnimations.EVERGATE_AIRSLASH,
                PBAnimations.EVERGATE_EXTRA_AUTO_1,
                PBAnimations.EVERGATE_EXTRA_AUTO_2,
                PBAnimations.EVERGATE_EXTRA_AUTO_3,
                PBAnimations.EVERGATE_EXTRA_AUTO_4,
                PBAnimations.EVERGATE_EXTRA_DASH,
                PBAnimations.EVERGATE_EXTRA_AIRSLASH
        );

        for (AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor : animations) {
            if (accessor != null && accessor.get() != null) {
                StaticAnimation animation = accessor.get();
                animation.addProperty(IGNORE_ENTITY_COLLISION, true);
                animation.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD);
                animation.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.RAW_COORD);
            }
        }
    }
}
