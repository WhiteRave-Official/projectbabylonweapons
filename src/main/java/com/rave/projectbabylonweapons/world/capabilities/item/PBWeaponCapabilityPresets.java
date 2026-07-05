package com.rave.projectbabylonweapons.world.capabilities.item;

import java.util.function.Function;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.gameasset.PBColliderPresets;
import com.rave.projectbabylonweapons.gameasset.PBSkills;
import com.rave.projectbabylonweapons.item.shield.BastionShield;
import net.corruptdog.cdm.gameasset.CDSkills;
import net.corruptdog.cdm.gameasset.CorruptAnimations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class PBWeaponCapabilityPresets {

    public static final Function<Item, CapabilityItem.Builder<?>> SICKLE = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(PBWeaponCategories.PB_SICKLE)
                .styleProvider((playerpatch) -> {
                    if (playerpatch.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == PBWeaponCategories.PB_SICKLE) {
                        return CapabilityItem.Styles.TWO_HAND;
                    }
                    return CapabilityItem.Styles.ONE_HAND;
                })
                .collider(PBColliderPresets.SICKLE)
                .canBePlacedOffhand(true)
                .weaponCombinationPredicator((entitypatch) ->
                        EpicFightCapabilities.getItemStackCapability(((LivingEntity) entitypatch.getOriginal()).getOffhandItem()).getWeaponCategory() == PBWeaponCategories.PB_SICKLE
                );

        if (item instanceof TieredItem tieredItem) {
            if (tieredItem.getTier() == Tiers.WOOD) {
                builder.hitSound(EpicFightSounds.BLUNT_HIT.get())
                        .hitParticle(EpicFightParticles.HIT_BLUNT.get());
            } else {
                builder.hitSound(EpicFightSounds.BLADE_HIT.get())
                        .hitParticle(EpicFightParticles.HIT_BLADE.get());
            }
        } else {
            builder.hitSound(EpicFightSounds.BLADE_HIT.get())
                    .hitParticle(EpicFightParticles.HIT_BLADE.get());
        }

        builder
                .newStyleCombo(CapabilityItem.Styles.ONE_HAND, new AnimationManager.AnimationAccessor[]{
                        PBAnimations.SICKLE_ONEHAND_AUTO_1,
                        PBAnimations.SICKLE_ONEHAND_AUTO_2,
                        PBAnimations.SICKLE_ONEHAND_AUTO_3,
                        PBAnimations.SICKLE_ONEHAND_AUTO_4,
                        PBAnimations.SICKLE_ONEHAND_DASH,
                        PBAnimations.SICKLE_ONEHAND_AIR_SLASH
                })
                .newStyleCombo(CapabilityItem.Styles.TWO_HAND, new AnimationManager.AnimationAccessor[]{
                        PBAnimations.SICKLE_DUAL_AUTO_1,
                        PBAnimations.SICKLE_DUAL_AUTO_2,
                        PBAnimations.SICKLE_DUAL_AUTO_3,
                        Animations.SWORD_DUAL_DASH,
                        PBAnimations.SICKLE_DUAL_AIR_SLASH
                })
                .newStyleCombo(CapabilityItem.Styles.MOUNT, new AnimationManager.AnimationAccessor[]{
                        Animations.SWORD_MOUNT_ATTACK
                })
                .innateSkill(CapabilityItem.Styles.ONE_HAND, (itemstack) -> PBSkills.SICKLE_THROW.get())
                .innateSkill(CapabilityItem.Styles.TWO_HAND, (itemstack) -> PBSkills.THE_HARVEST.get())
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.IDLE, PBAnimations.SICKLE_IDLE)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.KNEEL, PBAnimations.SICKLE_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.WALK, PBAnimations.SICKLE_WALK)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.CHASE, PBAnimations.SICKLE_WALK)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.RUN, PBAnimations.SICKLE_RUN)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.SNEAK, PBAnimations.SICKLE_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.SWIM, PBAnimations.SICKLE_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.FLOAT, PBAnimations.SICKLE_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.FALL, PBAnimations.SICKLE_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.IDLE, PBAnimations.SICKLE_DUAL_IDLE)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.KNEEL, PBAnimations.SICKLE_DUAL_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.WALK, PBAnimations.SICKLE_DUAL_WALK)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.CHASE, PBAnimations.SICKLE_DUAL_RUN)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.RUN, PBAnimations.SICKLE_DUAL_RUN)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.SNEAK, PBAnimations.SICKLE_DUAL_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.SWIM, PBAnimations.SICKLE_DUAL_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.FLOAT, PBAnimations.SICKLE_DUAL_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.FALL, PBAnimations.SICKLE_DUAL_HOLD)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.BLOCK, Animations.SWORD_DUAL_GUARD);

        return builder;
    };

    public static final Function<Item, CapabilityItem.Builder<?>> ARCLIGHT = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(PBWeaponCategories.ARCLIGHT)
                .styleProvider((playerpatch) -> CapabilityItem.Styles.TWO_HAND)
                .collider(PBColliderPresets.SICKLE)
                .canBePlacedOffhand(true)
                .weaponCombinationPredicator((entitypatch) ->
                        EpicFightCapabilities.getItemStackCapability(((LivingEntity) entitypatch.getOriginal()).getOffhandItem()).getWeaponCategory() == PBWeaponCategories.ARCLIGHT
                );

        if (item instanceof TieredItem tieredItem) {
            if (tieredItem.getTier() == Tiers.WOOD) {
                builder.hitSound(EpicFightSounds.BLUNT_HIT.get())
                        .hitParticle(EpicFightParticles.HIT_BLUNT.get());
            } else {
                builder.hitSound(EpicFightSounds.BLADE_HIT.get())
                        .hitParticle(EpicFightParticles.HIT_BLADE.get());
            }
        } else {
            builder.hitSound(EpicFightSounds.BLADE_HIT.get())
                    .hitParticle(EpicFightParticles.HIT_BLADE.get());
        }

        builder
                .newStyleCombo(CapabilityItem.Styles.TWO_HAND, new AnimationManager.AnimationAccessor[]{
                        PBAnimations.ARCLIGHT_AUTO_1,
                        PBAnimations.ARCLIGHT_AUTO_2,
                        PBAnimations.ARCLIGHT_AUTO_3,
                        PBAnimations.ARCLIGHT_AUTO_4,
                        PBAnimations.ARCLIGHT_DASH,
                        PBAnimations.ARCLIGHT_AIRSlASH
                })
                .newStyleCombo(CapabilityItem.Styles.MOUNT, new AnimationManager.AnimationAccessor[]{
                        Animations.SWORD_MOUNT_ATTACK
                })
                .innateSkill(CapabilityItem.Styles.TWO_HAND, (itemstack) -> PBSkills.THE_HARVEST.get())
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.IDLE, PBAnimations.ARCLIGHT_IDLE)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_KNEEL)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.WALK, PBAnimations.ARCLIGHT_WALK)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.RUN, PBAnimations.ARCLIGHT_RUN)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_SNEAK)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.SWIM, PBAnimations.ARCLIGHT_IDLE)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.FLOAT, PBAnimations.ARCLIGHT_IDLE)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.FALL, PBAnimations.ARCLIGHT_IDLE);

        return builder;
    };

    public static final Function<Item, CapabilityItem.Builder<?>> PB_WAND = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(PBWeaponCategories.PB_WAND)
                .styleProvider((playerpatch) -> CapabilityItem.Styles.TWO_HAND)
                .collider(PBColliderPresets.SICKLE)
                .canBePlacedOffhand(true)
                .weaponCombinationPredicator((entitypatch) ->
                        EpicFightCapabilities.getItemStackCapability(((LivingEntity) entitypatch.getOriginal()).getOffhandItem()).getWeaponCategory() == PBWeaponCategories.ARCLIGHT
                );

        if (item instanceof TieredItem tieredItem) {
            if (tieredItem.getTier() == Tiers.WOOD) {
                builder.hitSound(EpicFightSounds.BLUNT_HIT.get())
                        .hitParticle(EpicFightParticles.HIT_BLUNT.get());
            } else {
                builder.hitSound(EpicFightSounds.BLADE_HIT.get())
                        .hitParticle(EpicFightParticles.HIT_BLADE.get());
            }
        } else {
            builder.hitSound(EpicFightSounds.BLADE_HIT.get())
                    .hitParticle(EpicFightParticles.HIT_BLADE.get());
        }

        builder
                .newStyleCombo(CapabilityItem.Styles.TWO_HAND, new AnimationManager.AnimationAccessor[]{
                        PBAnimations.WAND_AUTO_1,
                        PBAnimations.WAND_AUTO_2,
                        PBAnimations.WAND_AUTO_3,
                        PBAnimations.WAND_AUTO_4,
                        PBAnimations.WAND_DASH,
                        PBAnimations.WAND_AIRSlASH
                })
                .newStyleCombo(CapabilityItem.Styles.MOUNT, new AnimationManager.AnimationAccessor[]{
                        Animations.SWORD_MOUNT_ATTACK
                })
                .innateSkill(CapabilityItem.Styles.TWO_HAND, (itemstack) -> PBSkills.MANA_BUBBLE.get())
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.IDLE, PBAnimations.WAND_IDLE)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_KNEEL)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.WALK, PBAnimations.WAND_WALK)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.RUN, PBAnimations.WAND_RUN)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_SNEAK)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_SWIM)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_FLOAT)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_FALL);

        return builder;
    };

    public static final Function<Item, CapabilityItem.Builder<?>> PB_LONGSWORD = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(CapabilityItem.WeaponCategories.LONGSWORD)
                .styleProvider((playerpatch) -> {
                    if (playerpatch.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == CapabilityItem.WeaponCategories.SHIELD) {
                        Item offhandItem = playerpatch.getOriginal().getOffhandItem().getItem();
                        return offhandItem instanceof BastionShield ? PBLongswordStyles.BASTION : CapabilityItem.Styles.ONE_HAND;
                    } else if (playerpatch instanceof PlayerPatch<?> tplayerpatch) {
                        return tplayerpatch.getSkill(SkillSlots.WEAPON_INNATE).isActivated() ? CapabilityItem.Styles.OCHS : CapabilityItem.Styles.TWO_HAND;
                    }

                    return CapabilityItem.Styles.TWO_HAND;
                })
                .collider(yesman.epicfight.gameasset.ColliderPreset.LONGSWORD)
                .canBePlacedOffhand(false)
                .newStyleCombo(CapabilityItem.Styles.ONE_HAND, CorruptAnimations.SWORD_ONEHAND_AUTO1, CorruptAnimations.SWORD_ONEHAND_AUTO2, CorruptAnimations.SWORD_ONEHAND_AUTO3, CorruptAnimations.SWORD_ONEHAND_AUTO4, CorruptAnimations.SWORD_ONEHAND_DASH, Animations.SWORD_AIR_SLASH)
                .newStyleCombo(CapabilityItem.Styles.TWO_HAND, CorruptAnimations.LONGSWORD_OLD_AUTO1, CorruptAnimations.LONGSWORD_OLD_AUTO2, CorruptAnimations.LONGSWORD_OLD_AUTO3, CorruptAnimations.LONGSWORD_OLD_AUTO4, CorruptAnimations.LONGSWORD_OLD_DASH, CorruptAnimations.LONGSWORD_OLD_AIRSLASH)
                .newStyleCombo(CapabilityItem.Styles.OCHS, Animations.LONGSWORD_LIECHTENAUER_AUTO1, Animations.LONGSWORD_LIECHTENAUER_AUTO2, Animations.LONGSWORD_LIECHTENAUER_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
                .newStyleCombo(PBLongswordStyles.BASTION, CorruptAnimations.SWORD_ONEHAND_AUTO1, CorruptAnimations.SWORD_ONEHAND_AUTO2, CorruptAnimations.SWORD_ONEHAND_AUTO3, CorruptAnimations.SWORD_ONEHAND_AUTO4, CorruptAnimations.SWORD_ONEHAND_DASH, Animations.SWORD_AIR_SLASH)
                .innateSkill(CapabilityItem.Styles.ONE_HAND, (itemstack) -> CDSkills.SHILEDSLASH)
                .innateSkill(CapabilityItem.Styles.TWO_HAND, (itemstack) -> CDSkills.GUARDPARRY)
                .innateSkill(CapabilityItem.Styles.OCHS, (itemstack) -> EpicFightSkills.LIECHTENAUER.get())
                .innateSkill(PBLongswordStyles.BASTION, (itemstack) -> PBSkills.STERN_SLAM.get())
                .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.IDLE, PBAnimations.BASTION_IDLE)
                .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
                .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.CHASE, Animations.BIPED_WALK_LONGSWORD)
                .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.RUN, Animations.BIPED_RUN_LONGSWORD)
                .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.SNEAK, Animations.BIPED_HOLD_LONGSWORD)
                .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.KNEEL, Animations.BIPED_HOLD_LONGSWORD)
                .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.JUMP, Animations.BIPED_HOLD_LONGSWORD)
                .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.SWIM, Animations.BIPED_HOLD_LONGSWORD)
                .livingMotionModifier(CapabilityItem.Styles.COMMON, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
                .livingMotionModifier(CapabilityItem.Styles.OCHS, LivingMotions.IDLE, Animations.BIPED_HOLD_LIECHTENAUER)
                .livingMotionModifier(CapabilityItem.Styles.OCHS, LivingMotions.WALK, Animations.BIPED_WALK_LIECHTENAUER)
                .livingMotionModifier(CapabilityItem.Styles.OCHS, LivingMotions.CHASE, Animations.BIPED_WALK_LIECHTENAUER)
                .livingMotionModifier(CapabilityItem.Styles.OCHS, LivingMotions.RUN, Animations.BIPED_HOLD_LIECHTENAUER)
                .livingMotionModifier(CapabilityItem.Styles.OCHS, LivingMotions.SNEAK, Animations.BIPED_HOLD_LIECHTENAUER)
                .livingMotionModifier(CapabilityItem.Styles.OCHS, LivingMotions.KNEEL, Animations.BIPED_HOLD_LIECHTENAUER)
                .livingMotionModifier(CapabilityItem.Styles.OCHS, LivingMotions.JUMP, Animations.BIPED_HOLD_LIECHTENAUER)
                .livingMotionModifier(CapabilityItem.Styles.OCHS, LivingMotions.SWIM, Animations.BIPED_HOLD_LIECHTENAUER)
                .livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
                .livingMotionModifier(CapabilityItem.Styles.OCHS, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
                .livingMotionModifier(PBLongswordStyles.BASTION, LivingMotions.BLOCK, Animations.SWORD_GUARD);

        if (item instanceof TieredItem tieredItem) {
            builder.hitSound(tieredItem.getTier() == Tiers.WOOD ? EpicFightSounds.BLUNT_HIT.get() : EpicFightSounds.BLADE_HIT.get());
            builder.hitParticle(tieredItem.getTier() == Tiers.WOOD ? EpicFightParticles.HIT_BLUNT.get() : EpicFightParticles.HIT_BLADE.get());
        }

        return builder;
    };

    public static void register() {
        EpicFightEventHooks.Registry.WEAPON_CAPABILITY_PRESET.registerEvent(PBWeaponCapabilityPresets::registerWeaponPresets);
    }

    public static void registerWeaponPresets(WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(
                ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "pb_sickle"),
                SICKLE);

        event.getTypeEntry().put(
                ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "arclight"),
                ARCLIGHT);

        event.getTypeEntry().put(
                ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "pb_wand"),
                PB_WAND);

        event.getTypeEntry().put(
                ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "pb_longsword"),
                PB_LONGSWORD);
    }
}





