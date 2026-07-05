package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.passive.bastion.BastionPassiveHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class SternSlamSkill extends WeaponInnateSkill {
    private static final int BUFF_DURATION_TICKS = 20 * 20;
    private static final double BUFF_RADIUS = 8.0D;
    private static final double BUFF_VERTICAL = 3.0D;

    public SternSlamSkill(WeaponInnateSkill.Builder<?> builder) {
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

                    if (event.getAnimation() != PBAnimations.STERN_SLAM || event.getPhaseOrder() != 0) {
                        return;
                    }

                    if (!(event.getEntityPatch() instanceof ServerPlayerPatch playerPatch)) {
                        return;
                    }

                    LivingEntity caster = playerPatch.getOriginal();
                    applyPhysicalResistanceBuff(caster);
                    BastionPassiveHandler.handleSternSlamContact(caster, caster.getOffhandItem());
                },
                this
        );
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        if (this.isActivated(container)) {
            return;
        }

        super.executeOnServer(container, args);
        container.activate();
        container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
        container.getExecutor().playAnimationSynchronized(PBAnimations.STERN_SLAM, 0.0F);
    }

    @Override
    public void executeOnClient(SkillContainer container, CompoundTag args) {
        super.executeOnClient(container, args);
        if (!this.isActivated(container)) {
            container.activate();
        }
    }

    @Override
    public void cancelOnServer(SkillContainer container, CompoundTag args) {
        container.deactivate();
        super.cancelOnServer(container, args);
        container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
    }

    @Override
    public void cancelOnClient(SkillContainer container, CompoundTag args) {
        container.deactivate();
        super.cancelOnClient(container, args);
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        if (!this.properties.isEmpty()) {
            PBAnimations.STERN_SLAM.get().phases[0].addProperties(this.properties.get(0).entrySet());
        }
        return this;
    }

    private static void applyPhysicalResistanceBuff(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        MobEffectInstance buff = new MobEffectInstance(PBMEffects.PHYSICAL_RESISTANCE, BUFF_DURATION_TICKS, 0, false, true, true);
        caster.addEffect(new MobEffectInstance(buff));

        if (!(caster instanceof ServerPlayer serverPlayer)) {
            return;
        }

        AABB area = serverPlayer.getBoundingBox().inflate(BUFF_RADIUS, BUFF_VERTICAL, BUFF_RADIUS);
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, area,
                target -> target.isAlive() && !target.isSpectator() && target != serverPlayer);

        for (ServerPlayer target : nearbyPlayers) {
            target.addEffect(new MobEffectInstance(buff));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }
}
