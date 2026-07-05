package com.rave.projectbabylonweapons.passive.ethereal;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class EtherealHolyPassive {
    private static final ThreadLocal<Set<UUID>> PROCESSING_ENTITIES = ThreadLocal.withInitial(HashSet::new);
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.ethereal.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/ethereal_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/ethereal_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.ethereal.line1").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("ethereal")
    );

    private EtherealHolyPassive() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        UUID targetUUID = event.getEntity().getUUID();
        if (PROCESSING_ENTITIES.get().contains(targetUUID)) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        EtherealHolyBalance.Profile profile = EtherealHolyBalance.resolve(weapon);
        if (profile == null) {
            return;
        }

        float originalDamage = event.getAmount();
        event.setAmount(originalDamage * profile.physicalDamagePercent());

        float holyDamage = originalDamage * profile.holyDamagePercent();
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
                try {
                    target.hurt(holySource, holyDamage);
                } finally {
                    target.invulnerableTime = originalInvulnerableTime;
                }
            }
        } catch (Exception e) {
            event.setAmount(originalDamage);
        } finally {
            PROCESSING_ENTITIES.get().remove(targetUUID);
        }
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }

    public static void appendTooltip(List<Component> tooltip) {
        TOOLTIP.appendTooltip(tooltip);
    }
}
