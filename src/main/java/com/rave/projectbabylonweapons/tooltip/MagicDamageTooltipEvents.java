package com.rave.projectbabylonweapons.tooltip;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.item.MagicMeleeWeapon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;
import java.util.Locale;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class MagicDamageTooltipEvents {
    private MagicDamageTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof MagicMeleeWeapon magicWeapon)) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        String attackDamageLabel = Component.translatable("attribute.name.generic.attack_damage").getString();
        LivingEntity holder = event.getEntity();
        float magicDamage = magicWeapon.getBaseMagicDamage(stack, holder);
        String magicDamageLabel = String.format(Locale.ROOT, " %.1f %s", magicDamage, magicWeapon.getMagicDamageTooltipLabel().getString());

        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            String text = line.getString();
            if (!text.contains(attackDamageLabel)) {
                continue;
            }

            tooltip.set(i, Component.literal(magicDamageLabel).setStyle(line.getStyle()));
            return;
        }
    }
}



