package com.rave.projectbabylonweapons.gameasset;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;

import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.registry.entries.EpicFightParticles;




public class PBAnimations {
    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(ProjectBabylonWeapons.MODID, PBAnimations::buildAnimations);
        }

//SICKLE
    public static AnimationManager.AnimationAccessor<AttackAnimation> SICKLE_ONEHAND_AUTO_1;
    public static AnimationManager.AnimationAccessor<AttackAnimation> SICKLE_ONEHAND_AUTO_2;
    public static AnimationManager.AnimationAccessor<AttackAnimation> SICKLE_ONEHAND_AUTO_3;
    public static AnimationManager.AnimationAccessor<AttackAnimation> SICKLE_ONEHAND_AUTO_4;
    public static AnimationManager.AnimationAccessor<DashAttackAnimation> SICKLE_ONEHAND_DASH;
    public static AnimationManager.AnimationAccessor<AirSlashAnimation> SICKLE_ONEHAND_AIR_SLASH;

    public static AnimationManager.AnimationAccessor<StaticAnimation> SICKLE_IDLE;
    public static AnimationManager.AnimationAccessor<StaticAnimation> SICKLE_HOLD;
    public static AnimationManager.AnimationAccessor<MovementAnimation> SICKLE_WALK;
    public static AnimationManager.AnimationAccessor<MovementAnimation> SICKLE_RUN;

    public static AnimationManager.AnimationAccessor<AttackAnimation> SICKLE_DUAL_AUTO_1;
    public static AnimationManager.AnimationAccessor<AttackAnimation> SICKLE_DUAL_AUTO_2;
    public static AnimationManager.AnimationAccessor<AttackAnimation> SICKLE_DUAL_AUTO_3;
    public static AnimationManager.AnimationAccessor<AirSlashAnimation> SICKLE_DUAL_AIR_SLASH;

    public static AnimationManager.AnimationAccessor<StaticAnimation> SICKLE_DUAL_IDLE;
    public static AnimationManager.AnimationAccessor<StaticAnimation> SICKLE_DUAL_HOLD;
    public static AnimationManager.AnimationAccessor<MovementAnimation> SICKLE_DUAL_WALK;
    public static AnimationManager.AnimationAccessor<MovementAnimation> SICKLE_DUAL_RUN;

//BATTLEHAMMER
    public static AnimationManager.AnimationAccessor<StaticAnimation> HOLD_BATTLEHAMMER;
    public static AnimationManager.AnimationAccessor<DashAttackAnimation> BATTLEHAMMER_DASH;

//ARCLIGHT
    public static AnimationManager.AnimationAccessor<StaticAnimation> ARCLIGHT_IDLE;
    public static AnimationManager.AnimationAccessor<StaticAnimation> ARCLIGHT_WALK;
    public static AnimationManager.AnimationAccessor<StaticAnimation> ARCLIGHT_RUN;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_AUTO_1;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_AUTO_2;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_AUTO_3;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_AUTO_4;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_AUTO_4_OLD;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_AUTO_5_OLD;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_DASH;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_AIRSlASH;

    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_EX_AUTO_1;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_EX_AUTO_2;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_EX_AUTO_3;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_EX_AUTO_4;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_EX_AUTO_4_NEW;
    public static AnimationManager.AnimationAccessor<AttackAnimation> ARCLIGHT_EX_AUTO_5;
//WAND
    public static AnimationManager.AnimationAccessor<StaticAnimation> WAND_IDLE;
    public static AnimationManager.AnimationAccessor<MovementAnimation> WAND_WALK;
    public static AnimationManager.AnimationAccessor<MovementAnimation> WAND_RUN;

    public static AnimationManager.AnimationAccessor<AttackAnimation> WAND_AUTO_1;
    public static AnimationManager.AnimationAccessor<AttackAnimation> WAND_AUTO_2;
    public static AnimationManager.AnimationAccessor<AttackAnimation> WAND_AUTO_3;
    public static AnimationManager.AnimationAccessor<AttackAnimation> WAND_AUTO_4;
    public static AnimationManager.AnimationAccessor<AttackAnimation> WAND_AIRSlASH;
    public static AnimationManager.AnimationAccessor<AttackAnimation> WAND_DASH;

    public static AnimationManager.AnimationAccessor<StaticAnimation> BASTION_IDLE;
//SKILLS
    public static AnimationManager.AnimationAccessor<StaticAnimation> SICKLE_READY;
    public static AnimationManager.AnimationAccessor<ActionAnimation> SICKLE_THROW;
    public static AnimationManager.AnimationAccessor<DashAttackAnimation> SICKLE_PULLING;
    public static AnimationManager.AnimationAccessor<AttackAnimation> SICKLE_HOOKING;
    public static AnimationManager.AnimationAccessor<DashAttackAnimation> UPPERCUT;
    public static AnimationManager.AnimationAccessor<AttackAnimation> TECTONIC;
    public static AnimationManager.AnimationAccessor<AttackAnimation> THE_HARVEST;
    public static AnimationManager.AnimationAccessor<AttackAnimation> BEAST_ROAR;
    public static AnimationManager.AnimationAccessor<AttackAnimation> GLACIER;
    public static AnimationManager.AnimationAccessor<AttackAnimation> MANA_BUBBLE;
    public static AnimationManager.AnimationAccessor<AttackAnimation> DRAGON_DESCEND;
    public static AnimationManager.AnimationAccessor<AttackAnimation> BLESSING;
    public static AnimationManager.AnimationAccessor<AttackAnimation> FIRE_STORM;
    public static AnimationManager.AnimationAccessor<AttackAnimation> STERN_SLAM;

    public static AnimationManager.AnimationAccessor<AttackAnimation> REBIRTH;


    public static void buildAnimations(AnimationManager.AnimationBuilder builder) {

//SICKLE
        SICKLE_DUAL_IDLE = builder.nextAccessor("biped/living/sickle_dual_idle", (accessor) -> new StaticAnimation(true, accessor, Armatures.BIPED));
        SICKLE_DUAL_HOLD = builder.nextAccessor("biped/living/sickle_dual_hold", (accessor) -> new StaticAnimation(true, accessor, Armatures.BIPED));
        SICKLE_DUAL_WALK = builder.nextAccessor("biped/living/sickle_dual_walk", (accessor) -> new MovementAnimation(0.1F, true, accessor, Armatures.BIPED));
        SICKLE_DUAL_RUN = builder.nextAccessor("biped/living/sickle_dual_run", (accessor) -> new MovementAnimation(0.1F, true, accessor, Armatures.BIPED));


        SICKLE_ONEHAND_AUTO_1 = builder.nextAccessor("biped/combat/sickle_onehand_auto_1", (accessor) ->  new AttackAnimation(0.15F,  0.15F,  0.1F,  0.4F, 7.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.4F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.3F));
        SICKLE_ONEHAND_AUTO_2 = builder.nextAccessor("biped/combat/sickle_onehand_auto_2", (accessor) ->  new AttackAnimation(0.15F,  0.15F,  0.1F,  0.25F, 7.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.45F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.3F));
        SICKLE_ONEHAND_AUTO_3 = builder.nextAccessor("biped/combat/sickle_onehand_auto_3", (accessor) ->  new AttackAnimation(0.15F,  0.15F,  0.1F,  0.35F, 4.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.4F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.3F));
        SICKLE_ONEHAND_AUTO_4 = builder.nextAccessor("biped/combat/sickle_onehand_auto_4", (accessor) ->  new AttackAnimation(0.15F,  0.15F,  0.1F,  0.35F, 4.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.45F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.3F));


        SICKLE_ONEHAND_DASH = builder.nextAccessor("biped/combat/sickle_onehand_dash", (accessor) -> (DashAttackAnimation)(new DashAttackAnimation(0.12F, 0.1F, 0.25F, 0.4F, 0.65F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED)).addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.3F));
        SICKLE_ONEHAND_AIR_SLASH = builder.nextAccessor("biped/combat/sickle_onehand_airslash", (accessor) -> new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.5F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED));


        SICKLE_DUAL_AUTO_1 = builder.nextAccessor("biped/combat/sickle_dual_auto_1", (accessor) ->  new AttackAnimation(0.15F,  0.15F,  0.07F,  0.3F, 0.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.3F)).newTimePair(0.0F, 0.45F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F));
        SICKLE_DUAL_AUTO_2 = builder.nextAccessor("biped/combat/sickle_dual_auto_2", (accessor) ->  new AttackAnimation(0.15F,  0.15F,  0.2F,  0.3F, 0.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.45F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.3F));
        SICKLE_DUAL_AUTO_3 = builder.nextAccessor("biped/combat/sickle_dual_auto_3", (accessor) ->  new AttackAnimation(0.15F,  0.1F,  0.04F,  0.2F, 0.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolL, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.45F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.8F));
        SICKLE_DUAL_AIR_SLASH = builder.nextAccessor("biped/combat/sickle_dual_airslash", (accessor) -> (AirSlashAnimation)(new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.5F, ColliderPreset.DUAL_SWORD_AIR_SLASH, ((HumanoidArmature)Armatures.BIPED.get()).torso, accessor, Armatures.BIPED)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F)).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.45F));

//BATTLEHAMMER
        BATTLEHAMMER_DASH = builder.nextAccessor("biped/combat/battlehammer_dash", (accessor) -> (DashAttackAnimation)(new DashAttackAnimation(0.1F, 0.1F, 0.25F, 0.6F, 1.25F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED)).addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F));
        HOLD_BATTLEHAMMER = builder.nextAccessor("biped/living/hold_battlehammer", (accessor) -> new StaticAnimation(true, accessor, Armatures.BIPED));

//ARCLIGHT
        ARCLIGHT_IDLE = builder.nextAccessor("biped/living/arclight_idle", (accessor) -> new StaticAnimation(true, accessor, Armatures.BIPED));
        ARCLIGHT_WALK = builder.nextAccessor("biped/living/arclight_walk", (accessor) -> new StaticAnimation(true, accessor, Armatures.BIPED));
        ARCLIGHT_RUN = builder.nextAccessor("biped/living/arclight_run", (accessor) -> new StaticAnimation(true, accessor, Armatures.BIPED));

        ARCLIGHT_AUTO_1 = builder.nextAccessor("biped/combat/arclight_auto_1", (accessor) ->  new AttackAnimation(0.25F,  0.25F,  0.1F,  0.4F, 0.7F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.75F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.8F));
        ARCLIGHT_AUTO_2 = builder.nextAccessor("biped/combat/arclight_auto_2", (accessor) ->  new AttackAnimation(0.20F,  0.25F,  0.1F,  0.4F, 0.7F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.6F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.8F));
        ARCLIGHT_AUTO_3 = builder.nextAccessor("biped/combat/arclight_auto_3", (accessor) ->  new AttackAnimation(0.35F,  0.25F,  0.2F,  0.40F, 1.0F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.7F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F));
        ARCLIGHT_AUTO_4 = builder.nextAccessor("biped/combat/arclight_auto_4", (accessor) -> (AttackAnimation)(new AttackAnimation(0.25F, accessor, Armatures.BIPED, new AttackAnimation.Phase[]{
                new AttackAnimation.Phase(0.0F, 1.10F, 1.30F, 1.50F, 1.50F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null),
                new AttackAnimation.Phase(1.50F, 1.60F, 1.70F, 3.2F, 3.2F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null)
        })).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F)).addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.EVISCERATE).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.5F));

        ARCLIGHT_DASH = builder.nextAccessor("biped/combat/arclight_dash", (accessor) -> (AttackAnimation)(new AttackAnimation(0.7F, accessor, Armatures.BIPED, new AttackAnimation.Phase[]{
                new AttackAnimation.Phase(0.0F, 1.10F, 1.30F, 1.50F, 1.50F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null),
                new AttackAnimation.Phase(1.50F, 1.60F, 1.70F, 3.2F, 3.2F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null)
        })).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F)).addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.EVISCERATE).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.9F));

        ARCLIGHT_AIRSlASH = builder.nextAccessor("biped/combat/arclight_airslash", (accessor) ->  new AttackAnimation(0.25F,  0.28F,  0.1F,  0.9F, 1.7F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.75F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F));

        ARCLIGHT_AUTO_4_OLD = builder.nextAccessor("biped/combat/arclight_auto_4_old", (accessor) ->  new AttackAnimation(0.40F,  0.25F,  0.2F,  0.40F, 1.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.8F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.1F));
        ARCLIGHT_AUTO_5_OLD = builder.nextAccessor("biped/combat/arclight_auto_5_old", (accessor) ->  new AttackAnimation(0.40F,  0.25F,  0.2F,  0.40F, 1.0F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.8F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.1F));

        ARCLIGHT_EX_AUTO_1 = builder.nextAccessor("biped/combat/arclight_ex_auto_1", (accessor) ->  new AttackAnimation(0.25F,  0.25F,  0.1F,  0.4F, 7.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.75F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.9F));
        ARCLIGHT_EX_AUTO_2 = builder.nextAccessor("biped/combat/arclight_ex_auto_2", (accessor) ->  new AttackAnimation(0.20F,  0.25F,  0.1F,  0.4F, 7.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.77F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.9F));
        ARCLIGHT_EX_AUTO_3 = builder.nextAccessor("biped/combat/arclight_ex_auto_3", (accessor) ->  new AttackAnimation(0.35F,  0.25F,  0.2F,  0.40F, 4.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.9F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.9F));
        ARCLIGHT_EX_AUTO_4 = builder.nextAccessor("biped/combat/arclight_ex_auto_4", (accessor) ->  new AttackAnimation(0.40F,  0.25F,  0.2F,  0.40F, 4.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.8F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.9F));
        ARCLIGHT_EX_AUTO_4_NEW = builder.nextAccessor("biped/combat/arclight_ex_auto_4_new", (accessor) ->  new AttackAnimation(0.40F,  0.25F,  0.2F,  0.40F, 4.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.8F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.9F));
        ARCLIGHT_EX_AUTO_5 = builder.nextAccessor("biped/combat/arclight_ex_auto_5", (accessor) ->  new AttackAnimation(0.25F,  0.25F,  0.1F,  0.4F, 7.667F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.75F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.9F));
//WAND
        WAND_IDLE = builder.nextAccessor("biped/living/wand_idle", (accessor) -> new StaticAnimation(true, accessor, Armatures.BIPED));
        WAND_WALK = builder.nextAccessor("biped/living/wand_walk", (accessor) -> new MovementAnimation(true, accessor, Armatures.BIPED));
        WAND_RUN = builder.nextAccessor("biped/living/wand_run", (accessor) -> new MovementAnimation(true, accessor, Armatures.BIPED));

        WAND_AUTO_1 = builder.nextAccessor("biped/combat/wand_auto_1", (accessor) ->  new AttackAnimation(0.15F,  0.15F,  0.1F,  0.4F, 2.0F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 1.5F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.7F));
        WAND_AUTO_2 = builder.nextAccessor("biped/combat/wand_auto_2", (accessor) ->  new AttackAnimation(0.15F,  0.15F,  0.1F,  0.4F, 2.0F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 1.0F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.9F));
        WAND_AUTO_3 = builder.nextAccessor("biped/combat/wand_auto_3", (accessor) -> (AttackAnimation)(new AttackAnimation(0.1F, accessor, Armatures.BIPED, new AttackAnimation.Phase[]{
                new AttackAnimation.Phase(0.0F, 0.25F, 0.50F, 0.50F, 0.50F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolL, (Collider)null),
                new AttackAnimation.Phase(0.50F, 0.60F, 0.9F, 1.0F, 1.0F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolL, (Collider)null),
                new AttackAnimation.Phase(1.0F, 1.10F, 1.2F, 1.30F, 1.30F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null),
                new AttackAnimation.Phase(1.30F, 1.40F, 1.60F, 1.9F, 1.9F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null)
        })).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F)).addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.EVISCERATE).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F));

        WAND_AUTO_4 = builder.nextAccessor("biped/combat/wand_auto_4", (accessor) -> (AttackAnimation)(new AttackAnimation(0.15F, accessor, Armatures.BIPED, new AttackAnimation.Phase[]{
                new AttackAnimation.Phase(0.0F, 0.15F, 0.40F, 0.8F, 0.8F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null),
                new AttackAnimation.Phase(0.8F, 0.9F, 1.4F, 2.0F, 2.0F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null)
        })).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F));

        WAND_DASH = builder.nextAccessor("biped/combat/wand_dash", (accessor) ->  new AttackAnimation(0.15F,  0.15F,  0.1F,  0.4F, 2.0F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 2.0F).addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.8F));
        WAND_AIRSlASH = builder.nextAccessor("biped/combat/wand_airslash", (accessor) ->  new AttackAnimation(0.25F,  0.28F,  0.1F,  0.7F, 1.7F, (Collider)null, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 0.75F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.9F).addEvents(new AnimationEvent[]{AnimationEvent.InTimeEvent.create(0.4f, PBReusableEvents.MEDIUM_GROUNDSLAM, AnimationEvent.Side.CLIENT).params(new Vec3f(0.0f, -0.24f, -2.0f))}));

        BASTION_IDLE = builder.nextAccessor("biped/living/bastion_shield_idle", (accessor) -> new StaticAnimation(true, accessor, Armatures.BIPED));
//SKILLS
        SICKLE_READY = builder.nextAccessor("biped/skill/sickle_ready", (accessor) -> new StaticAnimation(0.15F, false, accessor, Armatures.BIPED).addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CHARGING));
        SICKLE_THROW = builder.nextAccessor("biped/skill/sickle_throw", (accessor) -> new ActionAnimation(0.1F, accessor, Armatures.BIPED).addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true));
        SICKLE_PULLING = builder.nextAccessor("biped/skill/sickle_pulling", (accessor) -> (DashAttackAnimation)(new DashAttackAnimation(0.1F, 0.0F, 0.5F, 0.7F, 1.65F, PBColliderPresets.SICKLE, ((HumanoidArmature)Armatures.BIPED.get()).legR, accessor, Armatures.BIPED)).addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F));
        SICKLE_HOOKING = builder.nextAccessor("biped/skill/sickle_hooking",  (accessor) -> (AttackAnimation)(new AttackAnimation(0.1F, 0.0F, 0.2F, 0.5F, 1.65F, PBColliderPresets.SICKLE, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED)).addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F));


        THE_HARVEST = builder.nextAccessor("biped/skill/the_harvest", (accessor) -> (AttackAnimation)(new AttackAnimation(0.1F, accessor, Armatures.BIPED, new AttackAnimation.Phase[]{
                new AttackAnimation.Phase(0.0F, 1.9F, 2.3F, 2.35F, 2.35F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null),
                new AttackAnimation.Phase(2.35F, 2.40F, 2.45F, 2.50F, 2.50F, InteractionHand.OFF_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolL, (Collider)null),
                new AttackAnimation.Phase(2.50F, 2.55F, 2.60F, 2.65F, 2.65F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolR, (Collider)null),
                new AttackAnimation.Phase(2.65F, 2.70F, 2.75F, 4.2F, 4.2F, InteractionHand.OFF_HAND, ((HumanoidArmature)Armatures.BIPED.get()).toolL, (Collider)null)
        })).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F)).addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.EVISCERATE).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.6F));
        BEAST_ROAR = builder.nextAccessor("biped/skill/beast_roar", (accessor) -> (AttackAnimation)(new AttackAnimation(
                0.1F,
                accessor,
                Armatures.BIPED,
                new AttackAnimation.Phase(0.0F, 1.10F, 1.12F, 1.17F, 1.21F, 1.24F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, PBColliderPresets.BEAST_ROAR_FIRST),
                new AttackAnimation.Phase(1.24F, 1.26F, 1.27F, 1.32F, 1.35F, 1.38F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, PBColliderPresets.BEAST_ROAR_SECOND),
                new AttackAnimation.Phase(1.38F, 1.40F, 1.41F, 1.47F, 1.50F, 1.52F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, PBColliderPresets.BEAST_ROAR_THIRD),
                new AttackAnimation.Phase(1.52F, 1.54F, 1.55F, 1.62F, 1.64F, 1.6471F, InteractionHand.MAIN_HAND, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, PBColliderPresets.BEAST_ROAR_THIRD)
        ))
                .removeProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND)
                .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
                .addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
                .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false)
                .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.3F)
                .addEvents(new AnimationEvent[]{AnimationEvent.InTimeEvent.create(1.0f, PBReusableEvents.BIG_GROUNDSLAM, AnimationEvent.Side.CLIENT).params(new Vec3f(0.0f, -0.24f, -2.0f))}));
        UPPERCUT = builder.nextAccessor("biped/skill/uppercut", (accessor) -> (DashAttackAnimation)(new DashAttackAnimation(0.12F, 0.1F, 0.4F, 0.6F, 2.0F, PBColliderPresets.APPERCUT_SKILL, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED)).addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F));
        TECTONIC = builder.nextAccessor("biped/skill/tectonic", (accessor) -> (AttackAnimation)(new AttackAnimation(0.12F, 0.1F, 0.8F, 1.5F, 1.1F, PBColliderPresets.TECTONIC_SKILL, ((HumanoidArmature)Armatures.BIPED.get()).toolR, accessor, Armatures.BIPED)).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.5F).addEvents(new AnimationEvent[]{AnimationEvent.InTimeEvent.create(1.0f, PBReusableEvents.BIG_GROUNDSLAM, AnimationEvent.Side.CLIENT).params(new Vec3f(0.0f, -0.24f, -2.0f))}));

        GLACIER = builder.nextAccessor("biped/skill/glacier", (accessor) ->  new AttackAnimation(0.35F,  0.15F,  1.7F,  1.9F, 2.0F, PBColliderPresets.BEAST_ROAR_FIRST, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 1.5F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.1F));
        MANA_BUBBLE = builder.nextAccessor("biped/skill/mana_bubble", (accessor) ->  new AttackAnimation(0.35F,  0.15F,  1.2F,  1.8F, 2.0F, PBColliderPresets.BEAST_ROAR_FIRST, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 1.5F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.1F));
        FIRE_STORM = builder.nextAccessor("biped/skill/fire_storm", (accessor) ->  new AttackAnimation(0.35F,  0.15F,  1.2F,  1.8F, 2.0F, PBColliderPresets.BEAST_ROAR_FIRST, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 1.5F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.1F));
        DRAGON_DESCEND = builder.nextAccessor("biped/skill/dragon_descend", (accessor) ->  new AttackAnimation(0.35F,  0.15F,  1.2F,  1.8F, 2.0F, PBColliderPresets.BEAST_ROAR_FIRST, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 1.5F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.1F));
        BLESSING = builder.nextAccessor("biped/skill/blessing", (accessor) ->  new AttackAnimation(0.35F,  0.15F,  1.7F,  1.9F, 2.0F, PBColliderPresets.BEAST_ROAR_FIRST, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).newTimePair(0.0F, 1.5F).addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.1F));
        STERN_SLAM = builder.nextAccessor("biped/skill/stern_slam", (accessor) ->  new AttackAnimation(0.35F,  0.15F,  2.3F,  3.6F, 5.0F, PBColliderPresets.BEAST_ROAR_FIRST, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true).addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 0.5F).addEvents(new AnimationEvent[]{AnimationEvent.InTimeEvent.create(3.4f, PBReusableEvents.BIG_GROUNDSLAM, AnimationEvent.Side.CLIENT).params(new Vec3f(0.0f, -0.24f, -2.0f))}));
        REBIRTH = builder.nextAccessor("biped/skill/rebirth", (accessor) ->  new AttackAnimation(0.25F,  0.26F,  0.29F,  0.6F, 4.7F, PBColliderPresets.BEAST_ROAR_FIRST, ((HumanoidArmature)Armatures.BIPED.get()).rootJoint, accessor, Armatures.BIPED).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true).addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.7F).addEvents(new AnimationEvent[]{AnimationEvent.InTimeEvent.create(0.3f, PBReusableEvents.BIG_GROUNDSLAM, AnimationEvent.Side.CLIENT).params(new Vec3f(0.0f, -0.24f, -2.0f))}));
    }
}
