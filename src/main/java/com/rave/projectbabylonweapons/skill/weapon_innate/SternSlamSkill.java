package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonmaterials.init.PBMEffects;
import com.rave.projectbabylonweapons.event.SternSlamParametersEvent;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.passive.bastion.BastionPassiveHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.List;
import java.util.UUID;

public class SternSlamSkill extends WeaponInnateSkill {
    private static final UUID CONTACT_UUID = UUID.fromString("f6b71011-2d0c-4c2c-b5f2-b9e1f2ff3fc2");
    private static final int BUFF_DURATION_TICKS = 20 * 20;
    private static final double BUFF_RADIUS = 8.0D;
    private static final double BUFF_VERTICAL = 3.0D;

    public SternSlamSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        container.getExecutor().getEventListener().addEventListener(
                EventType.ATTACK_PHASE_END_EVENT,
                CONTACT_UUID,
                event -> {
                    if (container.getExecutor().isLogicalClient()) {
                        return;
                    }

                    if (event.getAnimation() != PBAnimations.STERN_SLAM || event.getPhaseOrder() != 0) {
                        return;
                    }

                    ServerPlayerPatch playerPatch = event.getPlayerPatch();
                    if (playerPatch == null) {
                        return;
                    }

                    LivingEntity caster = playerPatch.getOriginal();
                    applyPhysicalResistanceBuff(caster);
                    BastionPassiveHandler.handleSternSlamContact(caster, caster.getOffhandItem());
                }
        );
    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecutor().getEventListener().removeListener(EventType.ATTACK_PHASE_END_EVENT, CONTACT_UUID);
        super.onRemoved(container);
    }

    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        if (this.isActivated(container)) {
            return;
        }

        super.executeOnServer(container, args);
        container.activate();
        container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
        container.getExecutor().playAnimationSynchronized(PBAnimations.STERN_SLAM, 0.0F);
    }

    @Override
    public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
        super.executeOnClient(container, args);
        if (!this.isActivated(container)) {
            container.activate();
        }
    }

    @Override
    public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
        container.deactivate();
        super.cancelOnServer(container, args);
        container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
    }

    @Override
    public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
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

        MobEffectInstance buff = new MobEffectInstance(PBMEffects.PHYSICAL_RESISTANCE.get(), SternSlamParametersEvent.duration(caster, BUFF_DURATION_TICKS), 0, false, true, true);
        caster.addEffect(new MobEffectInstance(buff));

        if (!(caster instanceof ServerPlayer serverPlayer)) {
            return;
        }

        AABB area = serverPlayer.getBoundingBox().inflate(SternSlamParametersEvent.radius(caster, BUFF_RADIUS), BUFF_VERTICAL, BUFF_RADIUS);
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