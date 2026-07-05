package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class MagicMeleeWeaponHandler {
    private static final ThreadLocal<Set<UUID>> PROCESSING_ENTITIES = ThreadLocal.withInitial(HashSet::new);

    private MagicMeleeWeaponHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker.level().isClientSide) {
            return;
        }

        if (event.getSource().getDirectEntity() != null && event.getSource().getDirectEntity() != attacker) {
            return;
        }

        ItemStack weaponStack = attacker.getMainHandItem();
        if (!(weaponStack.getItem() instanceof MagicMeleeWeapon magicWeapon)) {
            return;
        }

        LivingEntity target = event.getEntity();
        UUID targetUUID = target.getUUID();
        if (PROCESSING_ENTITIES.get().contains(targetUUID)) {
            return;
        }

        float originalDamage = event.getAmount();
        float rawMagicDamage = MagicMeleeWeaponHelper.calculateRawMagicDamage(attacker, weaponStack, magicWeapon, originalDamage);
        if (rawMagicDamage <= 0.0F) {
            return;
        }

        float magicArmorNegation = StaffMagicArmorHelper.resolveMagicArmorNegation(event.getSource());
        float schoolResistMultiplier = magicWeapon.getSchoolResistMultiplier(target);
        float adjustedMagicDamage = StaffMagicArmorHelper.applyAdjustedMagicDamage(target, rawMagicDamage, schoolResistMultiplier, magicArmorNegation);
        if (adjustedMagicDamage <= 0.0F) {
            return;
        }

        DamageSource magicSource = MagicMeleeWeaponHelper.createMagicDamageSource(attacker, weaponStack, magicWeapon, event.getSource());

        PROCESSING_ENTITIES.get().add(targetUUID);
        try {
            event.setCanceled(true);
            if (!target.isAlive()) {
                return;
            }

            int originalInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            try {
                target.hurt(magicSource, adjustedMagicDamage);
            } finally {
                target.invulnerableTime = originalInvulnerableTime;
            }
        } catch (Exception exception) {
            event.setCanceled(false);
            event.setAmount(originalDamage);
        } finally {
            PROCESSING_ENTITIES.get().remove(targetUUID);
        }
    }
}

