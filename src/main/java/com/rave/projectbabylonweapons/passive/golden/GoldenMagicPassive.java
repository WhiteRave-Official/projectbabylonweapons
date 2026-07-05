package com.rave.projectbabylonweapons.passive.golden;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.combat.MagicArmorCalculationHelper;
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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class GoldenMagicPassive {
    private static final ThreadLocal<Set<UUID>> PROCESSING_ENTITIES = ThreadLocal.withInitial(HashSet::new);
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.golden.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/gold_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/gold_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.golden.line1").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("golden")
    );

    private GoldenMagicPassive() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
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
        GoldenMagicBalance.Profile profile = GoldenMagicBalance.resolve(weapon);
        if (profile == null) {
            return;
        }

        LivingEntity target = event.getEntity();
        UUID targetUUID = target.getUUID();
        if (PROCESSING_ENTITIES.get().contains(targetUUID)) {
            return;
        }

        float bonusMagicDamage = event.getAmount() * profile.bonusMagicDamagePercent();
        if (bonusMagicDamage <= 0.0F) {
            return;
        }

        float adjustedBonusMagicDamage = MagicArmorCalculationHelper.applyAdjustedMagicDamage(target, bonusMagicDamage);
        if (adjustedBonusMagicDamage <= 0.0F) {
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
                target.hurt(magicSource, adjustedBonusMagicDamage);
            } finally {
                target.invulnerableTime = originalInvulnerableTime;
            }
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
