package com.rave.projectbabylonweapons.event;


import com.rave.projectbabylonweapons.item.spear.GoldenSpearItem;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GoldenSpearMagicDamageEvent {

    private static final ThreadLocal<Set<UUID>> PROCESSING_ENTITIES = ThreadLocal.withInitial(HashSet::new);

    @SubscribeEvent(priority = EventPriority.LOWEST)
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
        if (!(weapon.getItem() instanceof GoldenSpearItem)) {
            return;
        }

        LivingEntity target = event.getEntity();
        UUID targetUUID = target.getUUID();
        if (PROCESSING_ENTITIES.get().contains(targetUUID)) {
            return;
        }

        float bonusMagicDamage = event.getAmount() * GoldenSpearItem.BONUS_MAGIC_DAMAGE_PERCENT;
        if (bonusMagicDamage <= 0.0F) {
            return;
        }

        DamageSource magicSource = new DamageSource(
                attacker.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ISSDamageTypes.EVOCATION_MAGIC),
                attacker
        );

        PROCESSING_ENTITIES.get().add(targetUUID);
        try {
            if (!target.isAlive()) {
                return;
            }

            int originalInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            try {
                target.hurt(magicSource, bonusMagicDamage);
            } finally {
                target.invulnerableTime = originalInvulnerableTime;
            }
        } finally {
            PROCESSING_ENTITIES.get().remove(targetUUID);
        }
    }
}
