package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonmaterials.init.PBMEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.SimpleWeaponInnateSkill;

public class TheHarvestSkill extends SimpleWeaponInnateSkill {

    private static final int MARKED_DURATION_TICKS = 12 * 20;

    public TheHarvestSkill(SimpleWeaponInnateSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
                EpicFightEventHooks.Entity.DELIVER_DAMAGE_POST,
                event -> {
                    if (event.getDamageSource().getAnimation() != PBAnimations.THE_HARVEST) {
                        return;
                    }

                    LivingEntity target = event.getTarget();
                    if (target == null || !target.isAlive()) {
                        return;
                    }

                    float healAmount = event.getModifiedDamage() * 0.18f;
                    if (healAmount > 0) {
                        container.getExecutor().getOriginal().heal(healAmount);
                    }

                    target.addEffect(new MobEffectInstance(
                            PBMEffects.MARKED,
                            MARKED_DURATION_TICKS,
                            0,
                            false,
                            true,
                            true
                    ));
                },
                this
        );
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
    }
}
