package com.rave.projectbabylonweapons.passive.wand;

import com.rave.projectbabylonmaterials.ProjectBabylonMaterials;
import com.rave.projectbabylonmaterials.tooltip.TooltipFrameStyle;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.tooltip.WeaponPassiveTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class GoldenBloodPactPassive {
    private static final ResourceLocation MAX_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "golden_blood_pact_health");
    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "golden_blood_pact_speed");
    private static final Map<UUID, PactState> ACTIVE_STATES = new ConcurrentHashMap<>();
    private static final WeaponPassiveTooltipData TOOLTIP = new WeaponPassiveTooltipData(
            Component.translatable("tooltip.project_babylon_weapons.passive.wand_golden.name"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/frame/material/gold_material_frame.png"),
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonMaterials.MODID, "textures/gui/tooltip/icon/material/gold_material_icon.png"),
            List.of(
                    Component.translatable("tooltip.project_babylon_weapons.passive.wand_golden.line1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.project_babylon_weapons.passive.wand_golden.line2").withStyle(ChatFormatting.GRAY)
            ),
            TooltipFrameStyle.material("golden")
    );

    private GoldenBloodPactPassive() {
    }

    public static void onBattleWandAttack(LivingEntity attacker, ItemStack weaponStack) {
        GoldenBloodPactBalance.Profile profile = GoldenBloodPactBalance.resolve(weaponStack);
        if (profile == null || attacker.level().isClientSide) {
            return;
        }

        long gameTime = attacker.level().getGameTime();
        PactState state = ACTIVE_STATES.computeIfAbsent(attacker.getUUID(), ignored -> new PactState());
        state.stackCount = Math.min(maxStacks(profile), state.stackCount + 1);
        state.expiresAt = gameTime + profile.durationTicks();
        applyState(attacker, state, profile);
    }

    public static float getProjectileDamageMultiplier(LivingEntity attacker, ItemStack weaponStack) {
        GoldenBloodPactBalance.Profile profile = GoldenBloodPactBalance.resolve(weaponStack);
        if (profile == null) {
            return 1.0F;
        }

        PactState state = ACTIVE_STATES.get(attacker.getUUID());
        if (state == null || attacker.level().getGameTime() > state.expiresAt) {
            return 1.0F;
        }

        return (float) (1.0D + (state.stackCount * profile.stackPercent()));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        PactState state = ACTIVE_STATES.get(event.getEntity().getUUID());
        if (state == null) {
            return;
        }

        if (event.getEntity().level().getGameTime() > state.expiresAt) {
            clearState(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearState(event.getEntity());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearState(player);
        }
    }

    public static WeaponPassiveTooltipData getTooltipData() {
        return TOOLTIP;
    }

    private static int maxStacks(GoldenBloodPactBalance.Profile profile) {
        return Math.max(1, (int) Math.round(profile.maxPercent() / profile.stackPercent()));
    }

    private static void applyState(LivingEntity attacker, PactState state, GoldenBloodPactBalance.Profile profile) {
        double totalPercent = Math.min(profile.maxPercent(), state.stackCount * profile.stackPercent());
        AttributeInstance maxHealth = attacker.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
            maxHealth.addTransientModifier(new AttributeModifier(MAX_HEALTH_MODIFIER_ID, -totalPercent, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if (attacker.getHealth() > attacker.getMaxHealth()) {
                attacker.setHealth(attacker.getMaxHealth());
            }
        }

        AttributeInstance attackSpeed = attacker.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED_MODIFIER_ID, totalPercent, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void clearState(LivingEntity attacker) {
        ACTIVE_STATES.remove(attacker.getUUID());

        AttributeInstance maxHealth = attacker.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
        }

        AttributeInstance attackSpeed = attacker.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        }
    }

    private static final class PactState {
        private int stackCount;
        private long expiresAt;
    }
}
