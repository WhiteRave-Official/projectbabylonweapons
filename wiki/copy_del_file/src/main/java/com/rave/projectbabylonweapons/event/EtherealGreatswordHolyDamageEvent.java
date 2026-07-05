package com.rave.projectbabylonweapons.event;


import com.rave.projectbabylonweapons.item.greatsword.EtherealGreatswordItem;
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
public class EtherealGreatswordHolyDamageEvent {

    private static final ThreadLocal<Set<UUID>> PROCESSING_ENTITIES = ThreadLocal.withInitial(HashSet::new);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {

        UUID targetUUID = event.getEntity().getUUID();
        if (PROCESSING_ENTITIES.get().contains(targetUUID)) {
            return;
        }


        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }


        ItemStack weapon = attacker.getMainHandItem();
        if (!(weapon.getItem() instanceof EtherealGreatswordItem)) {
            return;
        }


        float originalDamage = event.getAmount();


        event.setAmount(originalDamage * EtherealGreatswordItem.PHYSICAL_DAMAGE_PERCENT);


        float holyDamage = originalDamage * EtherealGreatswordItem.HOLY_DAMAGE_PERCENT;

        PROCESSING_ENTITIES.get().add(targetUUID);

        try {
            LivingEntity target = event.getEntity();


            DamageSource holySource = new DamageSource(
                    attacker.level().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(ISSDamageTypes.HOLY_MAGIC),
                    attacker
            );
            if (target.isAlive()) {
                int originalInvulnerableTime = target.invulnerableTime;
                target.invulnerableTime = 0;

                target.hurt(holySource, holyDamage);

                target.invulnerableTime = originalInvulnerableTime;
            }

        } catch (Exception e) {

            event.setAmount(originalDamage);
        } finally {
            PROCESSING_ENTITIES.get().remove(targetUUID);
        }
    }
}
