package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.animation.AnimationEndEvent;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RebirthSkill extends WeaponInnateSkill {
    private static final Set<UUID> ACTIVE_REBIRTH = ConcurrentHashMap.newKeySet();

    public RebirthSkill(WeaponInnateSkill.Builder<?> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);
        eventListener.registerEvent(
                EpicFightEventHooks.Animation.END,
                event -> onAnimationEnd(container, event),
                this
        );
    }

    @Override
    public void onRemoved(SkillContainer container) {
        clearRebirth(container.getExecutor().getOriginal());
        super.onRemoved(container);
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);
        container.activate();
        startRebirth(container.getExecutor().getOriginal());
    }

    @Override
    public void executeOnClient(SkillContainer container, CompoundTag args) {
        super.executeOnClient(container, args);
        container.activate();
    }

    @Override
    public void cancelOnServer(SkillContainer container, CompoundTag args) {
        clearRebirth(container.getExecutor().getOriginal());
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
            PBAnimations.REBIRTH.get().phases[0].addProperties(this.properties.get(0).entrySet());
        }
        return this;
    }

    public static boolean triggerPassiveRebirth(LivingEntity entity) {
        return startRebirth(entity);
    }

    private static boolean startRebirth(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide() || !(entity.level() instanceof ServerLevel)) {
            return false;
        }

        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
        if (patch == null) {
            return false;
        }

        ACTIVE_REBIRTH.add(entity.getUUID());
        patch.playAnimationSynchronized(PBAnimations.REBIRTH, 0.0F);
        return true;
    }

    private static void onAnimationEnd(SkillContainer container, AnimationEndEvent event) {
        if (event.getAnimation() != PBAnimations.REBIRTH.get()) {
            return;
        }

        clearRebirth(container.getExecutor().getOriginal());
        if (container.isActivated()) {
            container.deactivate();
        }
    }

    private static void clearRebirth(LivingEntity entity) {
        if (entity != null) {
            ACTIVE_REBIRTH.remove(entity.getUUID());
        }
    }

    private static boolean isRebirthActive(LivingEntity entity) {
        if (entity == null || !ACTIVE_REBIRTH.contains(entity.getUUID())) {
            return false;
        }

        if (!entity.isAlive() || !(entity.level() instanceof ServerLevel)) {
            clearRebirth(entity);
            return false;
        }

        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
        if (patch == null) {
            clearRebirth(entity);
            return false;
        }

        var currentAnimation = patch.getAnimator().getPlayerFor(null).getRealAnimation();
        if (currentAnimation == null || currentAnimation != PBAnimations.REBIRTH) {
            clearRebirth(entity);
            return false;
        }

        return true;
    }

    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.isCanceled()) {
            return;
        }

        if (!isRebirthActive(entity)) {
            return;
        }

        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        event.setCanceled(true);
    }

    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        if (!isRebirthActive(entity)) {
            return;
        }

        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        event.setNewDamage(0.0F);
    }

    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        if (entity.level().isClientSide() || !ACTIVE_REBIRTH.contains(entity.getUUID())) {
            return;
        }

        isRebirthActive(entity);
    }

    @OnlyIn(Dist.CLIENT)
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }
}
