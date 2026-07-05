package com.rave.projectbabylonweapons.skill.weapon_innate;

import java.util.List;

import com.google.common.collect.Lists;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.registry.entries.EpicFightAttributes;

public class BeastRoarSkill extends WeaponInnateSkill {
    private static final ResourceLocation STAMINA_REGEN_ID = ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "beast_roar_stamina_regen");
    private static final int ROAR_BUFF_DURATION_TICKS = 38 * 20;

    public BeastRoarSkill(WeaponInnateSkill.Builder<?> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);
        eventListener.registerEvent(EpicFightEventHooks.Entity.MODIFY_ATTACK_SPEED, (event) -> {
            if (container.isActivated()) {
                float attackSpeed = event.getAttackSpeed();
                event.setAttackSpeed(attackSpeed * 1.18F);
            }
        }, this);
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        if (!container.getExecutor().isLogicalClient()) {
            removeStaminaRegenModifier(container);
            removeStunImmunity(container);
        }
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        if (this.isActivated(container)) {
            this.cancelOnServer(container, args);
        } else {
            super.executeOnServer(container, args);
            container.activate();
            container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
            container.getExecutor().playAnimationSynchronized(PBAnimations.BEAST_ROAR, 0.0F);
            applyStaminaRegenModifier(container);
            applyStunImmunity(container);
        }
    }

    @Override
    public void cancelOnServer(SkillContainer container, CompoundTag args) {
        container.deactivate();
        super.cancelOnServer(container, args);
        container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
        removeStaminaRegenModifier(container);
        removeStunImmunity(container);
    }

    @Override
    public void executeOnClient(SkillContainer container, CompoundTag args) {
        super.executeOnClient(container, args);
        container.activate();
    }

    @Override
    public void cancelOnClient(SkillContainer container, CompoundTag args) {
        super.cancelOnClient(container, args);
        container.deactivate();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Component> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerCap) {
        List<Component> list = Lists.newArrayList();
        list.add(Component.translatable(this.getTranslationKey()).withStyle(ChatFormatting.WHITE)
                .append(Component.literal(String.format("[%.0f]", this.consumption)).withStyle(ChatFormatting.AQUA)));
        list.add(Component.translatable(this.getTranslationKey() + ".tooltip").withStyle(ChatFormatting.DARK_GRAY));


        int firstPhaseProperty = getPhasePropertyIndex(0);
        int secondPhaseProperty = getPhasePropertyIndex(1);
        int thirdPhaseProperty = getPhasePropertyIndex(2);
        int fourthPhaseProperty = getPhasePropertyIndex(3);

        if (firstPhaseProperty >= 0) {
            this.generateTooltipforPhase(list, itemstack, cap, playerCap, this.properties.get(firstPhaseProperty), "First Hit:");
        }
        if (secondPhaseProperty >= 0) {
            this.generateTooltipforPhase(list, itemstack, cap, playerCap, this.properties.get(secondPhaseProperty), "Second Hit:");
        }
        if (thirdPhaseProperty >= 0) {
            this.generateTooltipforPhase(list, itemstack, cap, playerCap, this.properties.get(thirdPhaseProperty), "Third Hit:");
        }
        if (fourthPhaseProperty >= 0) {
            this.generateTooltipforPhase(list, itemstack, cap, playerCap, this.properties.get(fourthPhaseProperty), "Fourth Hit:");
        }

        return list;
    }


    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        int firstPhaseProperty = getPhasePropertyIndex(0);
        int secondPhaseProperty = getPhasePropertyIndex(1);
        int thirdPhaseProperty = getPhasePropertyIndex(2);
        int fourthPhaseProperty = getPhasePropertyIndex(3);

        if (firstPhaseProperty >= 0) {
            PBAnimations.BEAST_ROAR.get().phases[0].addProperties(this.properties.get(firstPhaseProperty).entrySet());
        }
        if (secondPhaseProperty >= 0) {
            PBAnimations.BEAST_ROAR.get().phases[1].addProperties(this.properties.get(secondPhaseProperty).entrySet());
        }
        if (thirdPhaseProperty >= 0) {
            PBAnimations.BEAST_ROAR.get().phases[2].addProperties(this.properties.get(thirdPhaseProperty).entrySet());
        }
        if (fourthPhaseProperty >= 0 && PBAnimations.BEAST_ROAR.get().phases.length > 3) {
            PBAnimations.BEAST_ROAR.get().phases[3].addProperties(this.properties.get(fourthPhaseProperty).entrySet());
        }
        return this;
    }

    private int getPhasePropertyIndex(int phaseIndex) {
        if (this.properties.isEmpty()) {
            return -1;
        }
        return Math.min(phaseIndex, this.properties.size() - 1);
    }

    private void applyStaminaRegenModifier(SkillContainer container) {
        Player player = (Player) container.getExecutor().getOriginal();
        AttributeInstance instance = player.getAttribute(EpicFightAttributes.STAMINA_REGEN);
        if (instance == null || instance.getModifier(STAMINA_REGEN_ID) != null) {
            return;
        }
        AttributeModifier modifier = new AttributeModifier(
                STAMINA_REGEN_ID,
                0.30D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        instance.addTransientModifier(modifier);
    }

    private void removeStaminaRegenModifier(SkillContainer container) {
        Player player = (Player) container.getExecutor().getOriginal();
        AttributeInstance instance = player.getAttribute(EpicFightAttributes.STAMINA_REGEN);
        if (instance != null) {
            instance.removeModifier(STAMINA_REGEN_ID);
        }
    }

    private void applyStunImmunity(SkillContainer container) {
        Player player = (Player) container.getExecutor().getOriginal();
        player.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY, ROAR_BUFF_DURATION_TICKS, 0, false, true, true));
    }

    private void removeStunImmunity(SkillContainer container) {
        Player player = (Player) container.getExecutor().getOriginal();
        player.removeEffect(EpicFightMobEffects.STUN_IMMUNITY);
    }
}
