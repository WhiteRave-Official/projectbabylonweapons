package com.rave.projectbabylonweapons.mixin;

import com.rave.projectbabylonweapons.event.ShieldDisableAttemptEvent;
import com.rave.projectbabylonweapons.event.ShieldDisabledEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerShieldDisableMixin {
    @Unique private LivingEntity projectBabylonWeapons$shieldAttacker;
    @Unique private ShieldDisableAttemptEvent projectBabylonWeapons$attempt;
    @Unique private boolean projectBabylonWeapons$alreadyDisabled;
    @Inject(method = "blockUsingShield", at = @At("HEAD"))
    private void projectBabylonWeapons$attacker(LivingEntity attacker, CallbackInfo ci) {
        projectBabylonWeapons$shieldAttacker = attacker;
    }
    @Inject(method = "blockUsingShield", at = @At("RETURN"))
    private void projectBabylonWeapons$clearAttacker(LivingEntity attacker, CallbackInfo ci) {
        projectBabylonWeapons$shieldAttacker = null;
    }
    @Inject(method = "disableShield", at = @At("HEAD"), cancellable = true)
    private void projectBabylonWeapons$attempt(boolean sprinting, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        if (player.level().isClientSide()) return;
        projectBabylonWeapons$attempt = new ShieldDisableAttemptEvent(player, projectBabylonWeapons$shieldAttacker, player.getUseItem().copy());
        projectBabylonWeapons$alreadyDisabled = player.getCooldowns().isOnCooldown(projectBabylonWeapons$attempt.shield().getItem());
        MinecraftForge.EVENT_BUS.post(projectBabylonWeapons$attempt);
        if (!projectBabylonWeapons$attempt.allowed()) { projectBabylonWeapons$attempt = null; ci.cancel(); }
    }
    @ModifyArg(method = "disableShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"), index = 1)
    private int projectBabylonWeapons$duration(int duration) {
        return projectBabylonWeapons$attempt == null ? duration : projectBabylonWeapons$attempt.duration();
    }
    @Inject(method = "disableShield", at = @At("RETURN"))
    private void projectBabylonWeapons$disabled(boolean sprinting, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        var attempt = projectBabylonWeapons$attempt;
        projectBabylonWeapons$attempt = null;
        if (attempt != null && !projectBabylonWeapons$alreadyDisabled && !player.isUsingItem()
                && player.getCooldowns().isOnCooldown(attempt.shield().getItem()))
            MinecraftForge.EVENT_BUS.post(new ShieldDisabledEvent(attempt));
    }
}
