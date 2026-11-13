package com.rave.projectbabylonweapons.mixin;

import com.rave.projectbabylonweapons.item.shield.BastionShield;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = GuardSkill.class, remap = false)
public abstract class BastionGuardCooldownMixin {
    @Inject(method = "isExecutableState", at = @At("RETURN"), cancellable = true)
    private void projectBabylonWeapons$cooldown(PlayerPatch<?> patch, CallbackInfoReturnable<Boolean> cir) {
        var player = patch.getOriginal();
        var offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof BastionShield && player.getCooldowns().isOnCooldown(offhand.getItem())) cir.setReturnValue(false);
    }
}
