package com.rave.projectbabylonweapons.passive.ice;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IceChillPassive {
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.ice.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/ice_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/ice_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.ice.line1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.ice.line2").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("ice")
    );

    private IceChillPassive() {
    }

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
        IceChillBalance.Profile profile = IceChillBalance.resolve(weapon);
        if (profile == null) {
            return;
        }

        LivingEntity target = event.getEntity();
        MobEffect chilledEffect = MobEffectRegistry.CHILLED.get();
        MobEffectInstance chilledInstance = target.getEffect(chilledEffect);

        if (chilledInstance == null) {
            if (rollChance(attacker, profile.chillIProcChance())) {
                applyChill(target, attacker, chilledEffect, profile.chillIDurationTicks(), IceChillBalance.CHILL_I_AMPLIFIER);
            }
            return;
        }

        int amplifier = chilledInstance.getAmplifier();

        if (amplifier <= IceChillBalance.CHILL_I_AMPLIFIER) {
            if (rollChance(attacker, profile.chillIIProcChance())) {
                applyChill(target, attacker, chilledEffect, profile.chillIIDurationTicks(), IceChillBalance.CHILL_II_AMPLIFIER);
            }
            return;
        }

        if (amplifier == IceChillBalance.CHILL_II_AMPLIFIER) {
            if (rollChance(attacker, profile.chillIIIProcChance())) {
                applyChill(target, attacker, chilledEffect, profile.chillIIIDurationTicks(), IceChillBalance.CHILL_III_AMPLIFIER);
            }
            return;
        }

        if (rollChance(attacker, profile.frozenFromChillIIIProcChance())) {
            target.removeEffect(chilledEffect);
            target.addEffect(new MobEffectInstance(PBMEffects.FROZEN.get(), profile.frozenDurationTicks()), attacker);
        }
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }

    public static void appendTooltip(List<Component> tooltip) {
        TOOLTIP.appendTooltip(tooltip);
    }

    private static void applyChill(LivingEntity target, LivingEntity owner, MobEffect chilledEffect, int durationTicks, int amplifier) {
        target.addEffect(new MobEffectInstance(chilledEffect, durationTicks, amplifier), owner);
    }

    private static boolean rollChance(LivingEntity attacker, float chance) {
        return attacker.getRandom().nextFloat() < chance;
    }
}

