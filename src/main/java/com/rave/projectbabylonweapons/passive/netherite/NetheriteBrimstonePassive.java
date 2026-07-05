package com.rave.projectbabylonweapons.passive.netherite;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.init.PBWSounds;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class NetheriteBrimstonePassive {
    private static final ThreadLocal<Set<UUID>> PROCESSING_ENTITIES = ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<Boolean> PROCESSING_BRIMSTONE_BLAST = ThreadLocal.withInitial(() -> false);
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.netherite.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/netherite_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/netherite_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.netherite.line1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.netherite.line2").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.netherite.line3").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("netherite")
    );

    private NetheriteBrimstonePassive() {
    }

    @SubscribeEvent
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

        if (PROCESSING_BRIMSTONE_BLAST.get()) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        NetheriteBrimstoneBalance.Profile profile = NetheriteBrimstoneBalance.resolve(weapon);
        if (profile == null) {
            return;
        }

        LivingEntity target = event.getEntity();
        UUID targetId = target.getUUID();
        if (PROCESSING_ENTITIES.get().contains(targetId)) {
            return;
        }

        if (target.hasEffect(PBMEffects.BRIMSTONE_FIRE)) {
            if (rollChance(attacker, profile.brimstoneBlastProcChance())) {
                triggerBrimstoneBlast(attacker, target, event.getSource(), profile);
                clearBrimstoneEffects(target);
            }
            return;
        }

        if (target.hasEffect(PBMEffects.BRIMSTONE_FLAMES)) {
            if (rollChance(attacker, profile.brimstoneFireProcChance())) {
                target.addEffect(new MobEffectInstance(
                        PBMEffects.BRIMSTONE_FIRE,
                        profile.brimstoneFireDurationTicks()
                ));
            }
            return;
        }

        if (target.isOnFire()) {
            if (rollChance(attacker, profile.brimstoneFlamesProcChance())) {
                target.addEffect(new MobEffectInstance(
                        PBMEffects.BRIMSTONE_FLAMES,
                        profile.brimstoneFlamesDurationTicks()
                ));
            }
            return;
        }

        if (rollChance(attacker, profile.igniteProcChance())) {
            target.igniteForSeconds(8);
        }
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }

    public static void appendTooltip(List<Component> tooltip) {
        TOOLTIP.appendTooltip(tooltip);
    }

    private static void triggerBrimstoneBlast(LivingEntity attacker, LivingEntity centerTarget, DamageSource source, NetheriteBrimstoneBalance.Profile profile) {
        double attackDamage = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float blastDamage = (float) (attackDamage * profile.brimstoneBlastDamageMultiplier());

        AABB area = centerTarget.getBoundingBox().inflate(
                profile.brimstoneBlastRadiusBlocks(),
                1.0D,
                profile.brimstoneBlastRadiusBlocks()
        );

        if (attacker.level() instanceof ServerLevel serverLevel) {
            PROCESSING_BRIMSTONE_BLAST.set(true);
            PROCESSING_ENTITIES.get().add(centerTarget.getUUID());
            try {
                for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity != attacker)) {
                    if (!victim.isAlive()) {
                        continue;
                    }

                    int originalInvulnerableTime = victim.invulnerableTime;
                    victim.invulnerableTime = 0;
                    try {
                        victim.hurt(source, blastDamage);
                    } finally {
                        victim.invulnerableTime = originalInvulnerableTime;
                    }
                }
            } finally {
                PROCESSING_ENTITIES.get().remove(centerTarget.getUUID());
                PROCESSING_BRIMSTONE_BLAST.set(false);
            }

            WeaponVisualEffectHelper.playBrimstoneBlast(centerTarget);
            serverLevel.playSound(
                    null,
                    centerTarget.getX(),
                    centerTarget.getY(),
                    centerTarget.getZ(),
                    PBWSounds.FIRE_BLAST.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }

    private static void clearBrimstoneEffects(LivingEntity target) {
        target.removeEffect(PBMEffects.BRIMSTONE_FIRE);
        target.removeEffect(PBMEffects.BRIMSTONE_FLAMES);
    }

    private static boolean rollChance(LivingEntity attacker, float chance) {
        return attacker.getRandom().nextFloat() < chance;
    }
}


