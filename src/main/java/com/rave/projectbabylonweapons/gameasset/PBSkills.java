package com.rave.projectbabylonweapons.gameasset;

import java.util.Set;


import com.rave.projectbabylonweapons.skill.weapon_innate.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.weaponinnate.SimpleWeaponInnateSkill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

@EventBusSubscriber(
        modid = "project_babylon_weapons",
        bus = Bus.MOD
)
public class PBSkills {
    public static Skill BEAST_ROAR;
    public static Skill SICKLE_THROW;
    public static Skill THE_HARVEST;
    public static Skill UPPERCUT;
    public static Skill TECTONIC;
    public static Skill REBIRTH;
    public static Skill GLACIER;
    public static Skill MANA_BUBBLE;
    public static Skill DRAGON_DESCEND;
    public static Skill BLESSING;
    public static Skill FIRE_STORM;
    public static Skill STERN_SLAM;

    public static Skill ARCLIGHT_AWAKENING;

    @SubscribeEvent
    public static void buildSkillEvent(SkillBuildEvent build) { SkillBuildEvent.ModRegistryWorker modRegistry = build.createRegistryWorker("project_babylon_weapons");
        WeaponInnateSkill beast_roar = modRegistry.build("beast_roar", BeastRoarSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(Skill.ActivateType.DURATION));
        beast_roar.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(3.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.7F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.2F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        beast_roar.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.6F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.SHORT).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        beast_roar.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.5F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.SHORT).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        beast_roar.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(5.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.4F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(0.9F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.SHORT).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        BEAST_ROAR = beast_roar;


        WeaponInnateSkill sickle_throw = modRegistry.build("sickle_throw", SickleThrowSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(Skill.ActivateType.HELD));
        sickle_throw.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        SICKLE_THROW = sickle_throw;

        WeaponInnateSkill the_harvest = modRegistry.build("the_harvest", TheHarvestSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(PBAnimations.THE_HARVEST));
        the_harvest.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.4F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.SHORT).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        THE_HARVEST = the_harvest;

        WeaponInnateSkill rebirth = modRegistry.build("rebirth", RebirthSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(Skill.ActivateType.HELD));
        rebirth.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        REBIRTH = rebirth;

        WeaponInnateSkill arclight_awakening = modRegistry.build("arclight_awakening", ArclightAwakeningSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(Skill.ActivateType.DURATION_INFINITE));
        arclight_awakening.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        ARCLIGHT_AWAKENING = arclight_awakening;

        WeaponInnateSkill mana_bubble = modRegistry.build("mana_bubble", ManaBubbleSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(Skill.ActivateType.HELD));
        mana_bubble.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        MANA_BUBBLE = mana_bubble;

        WeaponInnateSkill glacier = modRegistry.build("glacier", GlacierSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(PBAnimations.GLACIER));
        glacier.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        GLACIER = glacier;

        WeaponInnateSkill dragon_descend = modRegistry.build("dragon_descend", DragonDescendSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(Skill.ActivateType.DURATION));
        dragon_descend.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        DRAGON_DESCEND = dragon_descend;

        WeaponInnateSkill stern_slam = modRegistry.build("stern_slam", SternSlamSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(Skill.ActivateType.DURATION));
        stern_slam.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        STERN_SLAM = stern_slam;

        WeaponInnateSkill fire_storm = modRegistry.build("fire_storm", FireStormSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(PBAnimations.FIRE_STORM));
        fire_storm.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        FIRE_STORM = fire_storm;

        WeaponInnateSkill blessing = modRegistry.build("blessing", BlessingSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(Skill.ActivateType.DURATION));
        blessing.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        BLESSING = blessing;

        WeaponInnateSkill uppercut = modRegistry.build("uppercut", UppercutSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(PBAnimations.UPPERCUT));
        uppercut.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(10.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.1F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.SHORT).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        UPPERCUT = uppercut;

        WeaponInnateSkill tectonic = modRegistry.build("tectonic", TectonicSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(PBAnimations.TECTONIC));
        tectonic.newProperty().addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(3.0F)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F)).addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(2.0F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
        TECTONIC = tectonic;
    }
}


