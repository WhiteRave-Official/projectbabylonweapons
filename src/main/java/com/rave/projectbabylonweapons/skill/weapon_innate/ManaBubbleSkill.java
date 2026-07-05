package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import com.rave.projectbabylonweapons.handler.MagicMeleeWeaponHelper;
import com.rave.projectbabylonweapons.handler.StaffMagicArmorHelper;
import com.rave.projectbabylonweapons.item.MagicProjectileStaffWeapon;
import com.rave.projectbabylonweapons.world.entity.projectile.ManaBubbleProjectileEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.animation.AnimationEndEvent;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ManaBubbleSkill extends WeaponInnateSkill {
    private static final float DAMAGE_MULTIPLIER = 3.0F;
    private static final float SPEED_MULTIPLIER = 0.15F;
    private static final float RENDER_SCALE = 7.0F;
    private static final float DRAG_STRENGTH = 0.82F;
    private static final int MIN_LIFETIME = 40;

    public ManaBubbleSkill(WeaponInnateSkill.Builder<?> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);
        eventListener.registerEvent(
                EpicFightEventHooks.Animation.ATTACK_PHASE_END,
                event -> {
                    if (container.getExecutor().isLogicalClient()) {
                        return;
                    }

                    if (event.getAnimation() != PBAnimations.MANA_BUBBLE) {
                        return;
                    }

                    if (event.getPhaseOrder() != 0) {
                        return;
                    }

                    if (!(event.getEntityPatch() instanceof ServerPlayerPatch playerPatch)) {
                        return;
                    }

                    playerPatch.getOriginal().level().playSound(
                            null,
                            playerPatch.getOriginal().getX(),
                            playerPatch.getOriginal().getY(),
                            playerPatch.getOriginal().getZ(),
                            SoundEvents.ILLUSIONER_CAST_SPELL,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                    spawnManaBubble(playerPatch, playerPatch.getOriginal().getMainHandItem());
                    if (container.isActivated()) {
                        container.deactivate();
                    }
                },
                this
        );
        eventListener.registerEvent(
                EpicFightEventHooks.Animation.END,
                event -> onAnimationEnd(container, event),
                this
        );
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);
        container.activate();
        container.getExecutor().getOriginal().level().playSound(
                null,
                container.getExecutor().getOriginal().getX(),
                container.getExecutor().getOriginal().getY(),
                container.getExecutor().getOriginal().getZ(),
                SoundRegistry.HEARTSTOP_CAST.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
        container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
        container.getExecutor().playAnimationSynchronized(PBAnimations.MANA_BUBBLE, 0.0F);
    }

    @Override
    public void executeOnClient(SkillContainer container, CompoundTag args) {
        super.executeOnClient(container, args);
        container.activate();
    }

    @Override
    public void cancelOnServer(SkillContainer container, CompoundTag args) {
        container.deactivate();
        super.cancelOnServer(container, args);
    }

    @Override
    public void cancelOnClient(SkillContainer container, CompoundTag args) {
        container.deactivate();
        super.cancelOnClient(container, args);
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        if (!this.properties.isEmpty()) {
            PBAnimations.MANA_BUBBLE.get().phases[0].addProperties(this.properties.get(0).entrySet());
        }
        return this;
    }

    private static void onAnimationEnd(SkillContainer container, AnimationEndEvent event) {
        if (event.getAnimation() != PBAnimations.MANA_BUBBLE) {
            return;
        }

        if (container.isActivated()) {
            container.deactivate();
        }
    }

    private static void spawnManaBubble(ServerPlayerPatch playerPatch, ItemStack weaponStack) {
        if (!(weaponStack.getItem() instanceof MagicProjectileStaffWeapon weapon)) {
            return;
        }

        ManaBubbleProjectileEntity bubble = new ManaBubbleProjectileEntity(playerPatch.getOriginal().level());
        ManaBubbleProjectileEntity.VisualPreset visualPreset = ManaBubbleProjectileEntity.VisualPreset.BASIC;

        bubble.configureBubble(
                playerPatch.getOriginal(),
                weaponStack,
                weapon.getMagicDamageType(),
                MagicMeleeWeaponHelper.calculateRawMagicDamage(
                        playerPatch.getOriginal(),
                        weaponStack,
                        weapon,
                        1.0F,
                        DAMAGE_MULTIPLIER
                ),
                StaffMagicArmorHelper.resolveWeaponMagicArmorNegation(playerPatch.getOriginal(), weaponStack),
                StaffMagicArmorHelper.resolveWeaponImpact(playerPatch.getOriginal(), weaponStack),
                weapon.getMagicProjectileStunType(),
                Math.max(MIN_LIFETIME, weapon.getMagicProjectileLifetime()),
                weapon.getMagicProjectileTrailColor(),
                visualPreset,
                RENDER_SCALE,
                DRAG_STRENGTH
        );

        Vec3 look = playerPatch.getOriginal().getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 1.0E-6D) {
            forward = playerPatch.getOriginal().getForward();
        } else {
            forward = forward.normalize();
        }

        WeaponVisualEffectHelper.playManaBubbleBasicContact(playerPatch.getOriginal());

        double spawnX = playerPatch.getOriginal().getX() + forward.x * weapon.getMagicProjectileSpawnForwardOffset();
        double spawnY = playerPatch.getOriginal().getY() + playerPatch.getOriginal().getBbHeight() * 0.6D + weapon.getMagicProjectileSpawnVerticalOffset();
        double spawnZ = playerPatch.getOriginal().getZ() + forward.z * weapon.getMagicProjectileSpawnForwardOffset();
        bubble.setPos(spawnX, spawnY, spawnZ);
        bubble.shoot(forward.x, 0.0D, forward.z, weapon.getMagicProjectileSpeed() * SPEED_MULTIPLIER, 0.0F);
        playerPatch.getOriginal().level().addFreshEntity(bubble);
    }

    @OnlyIn(Dist.CLIENT)
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }
}
