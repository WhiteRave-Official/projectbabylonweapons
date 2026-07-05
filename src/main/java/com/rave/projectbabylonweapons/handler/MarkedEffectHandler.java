package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonmaterials.init.PBMEffects;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Bus.GAME)
public class MarkedEffectHandler {
    private static final float MARKED_DAMAGE_MULTIPLIER = 1.15f;

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity().hasEffect(PBMEffects.MARKED)) {
            event.setAmount(event.getAmount() * MARKED_DAMAGE_MULTIPLIER);
        }
    }
}
