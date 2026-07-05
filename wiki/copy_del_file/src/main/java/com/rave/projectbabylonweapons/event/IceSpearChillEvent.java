package com.rave.projectbabylonweapons.event;

import com.rave.projectbabylonweapons.init.PBModEffects;

import com.rave.projectbabylonweapons.item.spear.IceSpearItem;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IceSpearChillEvent {

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
        if (!(weapon.getItem() instanceof IceSpearItem)) {
            return;
        }

        LivingEntity target = event.getEntity();
        MobEffect chilledEffect = MobEffectRegistry.CHILLED.get();
        MobEffectInstance chilledInstance = target.getEffect(chilledEffect);

        if (chilledInstance == null) {
            if (rollChance(attacker, IceSpearItem.CHILL_I_PROC_CHANCE)) {
                applyChill(target, chilledEffect, IceSpearItem.CHILL_I_DURATION_TICKS, IceSpearItem.CHILL_I_AMPLIFIER);
            }
            return;
        }

        int amplifier = chilledInstance.getAmplifier();

        if (amplifier <= IceSpearItem.CHILL_I_AMPLIFIER) {
            if (rollChance(attacker, IceSpearItem.CHILL_II_PROC_CHANCE)) {
                applyChill(target, chilledEffect, IceSpearItem.CHILL_II_DURATION_TICKS, IceSpearItem.CHILL_II_AMPLIFIER);
            }
            return;
        }

        if (amplifier == IceSpearItem.CHILL_II_AMPLIFIER) {
            if (rollChance(attacker, IceSpearItem.CHILL_III_PROC_CHANCE)) {
                applyChill(target, chilledEffect, IceSpearItem.CHILL_III_DURATION_TICKS, IceSpearItem.CHILL_III_AMPLIFIER);
            }
            return;
        }

        if (rollChance(attacker, IceSpearItem.FROZEN_FROM_CHILL_III_PROC_CHANCE)) {
            target.removeEffect(chilledEffect);
            target.addEffect(new MobEffectInstance(PBModEffects.FROZEN.get(), IceSpearItem.FROZEN_DURATION_TICKS));
        }
    }

    private static void applyChill(LivingEntity target, MobEffect chilledEffect, int durationTicks, int amplifier) {
        target.addEffect(new MobEffectInstance(chilledEffect, durationTicks, amplifier));
    }

    private static boolean rollChance(LivingEntity attacker, float chance) {
        return attacker.getRandom().nextFloat() < chance;
    }
}
