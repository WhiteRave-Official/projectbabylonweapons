package com.rave.projectbabylonweapons.event;

import com.rave.projectbabylonweapons.init.PBModEffects;
import com.rave.projectbabylonweapons.item.shortsword.IceShortswordItem;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IceShortswordChillEvent {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker.level().isClientSide) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (!(weapon.getItem() instanceof IceShortswordItem)) {
            return;
        }

        LivingEntity target = event.getEntity();
        MobEffect chilledEffect = MobEffectRegistry.CHILLED.get();
        MobEffectInstance chilledInstance = target.getEffect(chilledEffect);

        if (chilledInstance == null) {
            if (rollChance(attacker, IceShortswordItem.CHILL_I_PROC_CHANCE)) {
                applyChill(target, chilledEffect, IceShortswordItem.CHILL_I_DURATION_TICKS, IceShortswordItem.CHILL_I_AMPLIFIER);
            }
            return;
        }

        int amplifier = chilledInstance.getAmplifier();

        if (amplifier <= IceShortswordItem.CHILL_I_AMPLIFIER) {
            if (rollChance(attacker, IceShortswordItem.CHILL_II_PROC_CHANCE)) {
                applyChill(target, chilledEffect, IceShortswordItem.CHILL_II_DURATION_TICKS, IceShortswordItem.CHILL_II_AMPLIFIER);
            }
            return;
        }

        if (amplifier == IceShortswordItem.CHILL_II_AMPLIFIER) {
            if (rollChance(attacker, IceShortswordItem.CHILL_III_PROC_CHANCE)) {
                applyChill(target, chilledEffect, IceShortswordItem.CHILL_III_DURATION_TICKS, IceShortswordItem.CHILL_III_AMPLIFIER);
            }
            return;
        }

        if (rollChance(attacker, IceShortswordItem.FROZEN_FROM_CHILL_III_PROC_CHANCE)) {
            target.removeEffect(chilledEffect);
            target.addEffect(new MobEffectInstance(PBModEffects.FROZEN.get(), IceShortswordItem.FROZEN_DURATION_TICKS));
        }
    }

    private static void applyChill(LivingEntity target, MobEffect chilledEffect, int durationTicks, int amplifier) {
        target.addEffect(new MobEffectInstance(chilledEffect, durationTicks, amplifier));
    }

    private static boolean rollChance(LivingEntity attacker, float chance) {
        return attacker.getRandom().nextFloat() < chance;
    }
}
