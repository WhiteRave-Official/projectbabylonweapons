package com.rave.projectbabylonweapons.summon.arclight.epicfight;

import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;

public class ArclightSummonedWeaponPatch extends MobPatch<ArclightSummonedWeaponEntity> {
    public ArclightSummonedWeaponPatch() {
        super(Factions.NEUTRAL);
    }

    @Override
    protected void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, ArclightSummonAnimations.IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, ArclightSummonAnimations.IDLE);
        animator.addLivingAnimation(LivingMotions.DEATH, ArclightSummonAnimations.IDLE);
    }

    @Override
    public boolean overrideRender() {
        return true;
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        this.currentLivingMotion = LivingMotions.IDLE;
        this.currentCompositeMotion = LivingMotions.IDLE;
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        return null;
    }

    @Override
    public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return AttackResult.missed(0.0F);
        }
        float damage = this.original.damageTargetFromEpicFight(livingTarget);
        return damage > 0.0F ? AttackResult.success(damage) : AttackResult.missed(0.0F);
    }

    public void playAttack(ArclightSummonedWeaponEntity.AttackType attack) {
        if (!this.isLogicalClient()) {
            this.playAnimationSynchronized(ArclightSummonAnimations.forAttack(attack, this.original.isSpear()), 0.0F);
        }
    }
}
