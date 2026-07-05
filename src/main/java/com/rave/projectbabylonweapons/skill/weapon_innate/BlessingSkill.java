package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import com.rave.projectbabylonweapons.world.entity.effect.HolyMagicalSealEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.animation.AnimationEndEvent;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;

import java.util.List;

public class BlessingSkill extends WeaponInnateSkill {
    private static final double ALLY_RADIUS = 15.0D;

    public BlessingSkill(WeaponInnateSkill.Builder<?> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);
        eventListener.registerEvent(
                EpicFightEventHooks.Animation.BEGIN,
                event -> {
                    if (container.getExecutor().isLogicalClient()) {
                        return;
                    }

                    if (event.getAnimation() != PBAnimations.BLESSING) {
                        return;
                    }

                    WeaponVisualEffectHelper.startBlessingCast(container.getExecutor().getOriginal());
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
        WeaponVisualEffectHelper.stopBlessingCast(container.getExecutor().getOriginal());
        super.onRemoved(container);
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        if (this.isActivated(container)) {
            this.cancelOnServer(container, args);
        } else {
            super.executeOnServer(container, args);
            container.activate();
            container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
            if (container.getExecutor().getOriginal() instanceof ServerPlayer serverPlayer) {
                spawnSeal(serverPlayer, serverPlayer, container.getRemainDuration());
            }
            container.getExecutor().playAnimationSynchronized(PBAnimations.BLESSING, 0.0F);
        }
    }

    @Override
    public void executeOnClient(SkillContainer container, CompoundTag args) {
        super.executeOnClient(container, args);
        container.activate();
    }

    @Override
    public void cancelOnServer(SkillContainer container, CompoundTag args) {
        WeaponVisualEffectHelper.stopBlessingCast(container.getExecutor().getOriginal());
        container.deactivate();
        super.cancelOnServer(container, args);
        container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
    }

    @Override
    public void cancelOnClient(SkillContainer container, CompoundTag args) {
        container.deactivate();
        super.cancelOnClient(container, args);
    }

    private static void onAnimationEnd(SkillContainer container, AnimationEndEvent event) {
        if (event.getAnimation() != PBAnimations.BLESSING) {
            return;
        }

        WeaponVisualEffectHelper.burstBlessingCast(container.getExecutor().getOriginal());

        if (!(container.getExecutor().getOriginal() instanceof ServerPlayer caster)) {
            return;
        }

        int remainingDuration = container.getRemainDuration();
        ServerLevel serverLevel = caster.serverLevel();
        AABB area = caster.getBoundingBox().inflate(ALLY_RADIUS, 4.0D, ALLY_RADIUS);
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, area,
                player -> player.isAlive() && !player.isSpectator() && player != caster);

        for (ServerPlayer ally : nearbyPlayers) {
            spawnSeal(caster, ally, remainingDuration);
        }
    }

    private static void spawnSeal(ServerPlayer caster, ServerPlayer target, int durationTicks) {
        ServerLevel serverLevel = caster.serverLevel();
        discardExistingSeals(serverLevel, target);

        HolyMagicalSealEntity seal = new HolyMagicalSealEntity(serverLevel);
        seal.configure(caster, target, durationTicks);
        seal.setPos(target.getX(), target.getY() + 0.02D, target.getZ());
        serverLevel.addFreshEntity(seal);
    }

    private static void discardExistingSeals(ServerLevel serverLevel, ServerPlayer target) {
        AABB searchBox = target.getBoundingBox().inflate(32.0D);
        for (HolyMagicalSealEntity existingSeal : serverLevel.getEntitiesOfClass(HolyMagicalSealEntity.class, searchBox,
                seal -> seal.tracks(target))) {
            existingSeal.clearEffectsAndDiscard();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }
}
