package com.rave.projectbabylonweapons.world.capabilities.item;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class ArclightWeaponCapability extends WeaponCapability {
    protected ArclightWeaponCapability(CapabilityItem.Builder builder) {
        super(builder);
    }

    public static WeaponCapability.Builder builder() {
        WeaponCapability.Builder builder = WeaponCapability.builder();
        builder.constructor(ArclightWeaponCapability::new);
        return builder;
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> getGuardMotion(
            GuardSkill skill,
            GuardSkill.BlockType blockType,
            PlayerPatch<?> playerpatch
    ) {
        if (blockType != GuardSkill.BlockType.GUARD) {
            return null;
        }

        return this.weaponCategory == PBWeaponCategories.EVERGATE
                || this.getStyle(playerpatch) == PBArclightStyles.EVERGATE
                ? PBAnimations.EVERGATE_GUARD_HIT
                : PBAnimations.ARCLIGHT_GUARD_HIT;
    }
}