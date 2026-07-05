package com.rave.projectbabylonweapons.gameasset;

import java.util.Set;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.skill.weapon_innate.*;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.weaponinnate.SimpleWeaponInnateSkill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

public class PBSkills {
    public static final DeferredRegister<Skill> REGISTRY =
            DeferredRegister.create(EpicFightRegistries.Keys.SKILL, ProjectBabylonWeapons.MODID);

    public static final DeferredHolder<Skill, WeaponInnateSkill> BEAST_ROAR = REGISTRY.register("beast_roar", key ->
            WeaponInnateSkill.createWeaponInnateBuilder(BeastRoarSkill::new)
                    .setActivateType(Skill.ActivateType.DURATION)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(3.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.7F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.2F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.6F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.5F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.0F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(5.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.4F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(0.9F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, WeaponInnateSkill> SICKLE_THROW = REGISTRY.register("sickle_throw", key ->
            WeaponInnateSkill.createWeaponInnateBuilder(SickleThrowSkill::new)
                    .setActivateType(Skill.ActivateType.HELD)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, SimpleWeaponInnateSkill> THE_HARVEST = REGISTRY.register("the_harvest", key ->
            SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder(TheHarvestSkill::new)
                    .setAnimations(PBAnimations.THE_HARVEST)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.4F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, WeaponInnateSkill> REBIRTH = REGISTRY.register("rebirth", key ->
            WeaponInnateSkill.createWeaponInnateBuilder(RebirthSkill::new)
                    .setActivateType(Skill.ActivateType.HELD)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, WeaponInnateSkill> MANA_BUBBLE = REGISTRY.register("mana_bubble", key ->
            WeaponInnateSkill.createWeaponInnateBuilder(ManaBubbleSkill::new)
                    .setActivateType(Skill.ActivateType.HELD)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, SimpleWeaponInnateSkill> GLACIER = REGISTRY.register("glacier", key ->
            SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder(GlacierSkill::new)
                    .setAnimations(PBAnimations.GLACIER)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, WeaponInnateSkill> DRAGON_DESCEND = REGISTRY.register("dragon_descend", key ->
            WeaponInnateSkill.createWeaponInnateBuilder(DragonDescendSkill::new)
                    .setActivateType(Skill.ActivateType.DURATION)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, WeaponInnateSkill> STERN_SLAM = REGISTRY.register("stern_slam", key ->
            WeaponInnateSkill.createWeaponInnateBuilder(SternSlamSkill::new)
                    .setActivateType(Skill.ActivateType.DURATION)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, SimpleWeaponInnateSkill> FIRE_STORM = REGISTRY.register("fire_storm", key ->
            SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder(FireStormSkill::new)
                    .setAnimations(PBAnimations.FIRE_STORM)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, WeaponInnateSkill> BLESSING = REGISTRY.register("blessing", key ->
            WeaponInnateSkill.createWeaponInnateBuilder(BlessingSkill::new)
                    .setActivateType(Skill.ActivateType.DURATION)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.0F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, SimpleWeaponInnateSkill> UPPERCUT = REGISTRY.register("uppercut", key ->
            SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder(UppercutSkill::new)
                    .setAnimations(PBAnimations.UPPERCUT)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(10.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.SHORT)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));

    public static final DeferredHolder<Skill, SimpleWeaponInnateSkill> TECTONIC = REGISTRY.register("tectonic", key ->
            SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder(TectonicSkill::new)
                    .setAnimations(PBAnimations.TECTONIC)
                    .newProperty()
                        .addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(3.0F))
                        .addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
                        .addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F))
                        .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(2.0F))
                        .addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
                    .build(key));
}
