package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonmaterials.init.PBMEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.SimpleWeaponInnateSkill;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class UppercutSkill extends SimpleWeaponInnateSkill {

    private static final int FEAR_DURATION_TICKS = 3 * 20;
    private static final float KNOCKUP_VELOCITY = 0.70F;
    private static final int KNOCKUP_DELAY_TICKS = 1;
    private static final int PENDING_TIMEOUT_TICKS = 20;
    private static final Map<UUID, PendingKnockup> PENDING_KNOCKUPS = new HashMap<>();

    private static class PendingKnockup {
        private final ResourceKey<Level> dimension;
        private final long applyAtGameTime;
        private final long expireAtGameTime;

        private PendingKnockup(ResourceKey<Level> dimension, long applyAtGameTime) {
            this.dimension = dimension;
            this.applyAtGameTime = applyAtGameTime;
            this.expireAtGameTime = applyAtGameTime + PENDING_TIMEOUT_TICKS;
        }
    }

    public UppercutSkill(SimpleWeaponInnateSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
                EpicFightEventHooks.Entity.DELIVER_DAMAGE_POST,
                (event) -> {
                    if (event.getDamageSource().getAnimation() != PBAnimations.UPPERCUT) {
                        return;
                    }

                    LivingEntity target = event.getTarget();
                    if (target == null || !target.isAlive()) {
                        return;
                    }

                    queueKnockupAndFear(target);
                },
                this
        );
    }

    private static void queueKnockupAndFear(LivingEntity target) {
        if (target.level().isClientSide() || !(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long applyAt = serverLevel.getGameTime() + KNOCKUP_DELAY_TICKS;
        PENDING_KNOCKUPS.put(target.getUUID(), new PendingKnockup(serverLevel.dimension(), applyAt));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING_KNOCKUPS.isEmpty()) {
            return;
        }

        var server = event.getServer();
        Iterator<Map.Entry<UUID, PendingKnockup>> iterator = PENDING_KNOCKUPS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingKnockup> entry = iterator.next();
            UUID targetId = entry.getKey();
            PendingKnockup pending = entry.getValue();

            ServerLevel level = server.getLevel(pending.dimension);
            if (level == null) {
                iterator.remove();
                continue;
            }

            long gameTime = level.getGameTime();
            if (gameTime < pending.applyAtGameTime) {
                continue;
            }

            Entity entity = level.getEntity(targetId);
            if (entity instanceof LivingEntity target && target.isAlive()) {
                applyKnockupAndFear(target);
            }

            if (gameTime >= pending.applyAtGameTime || gameTime > pending.expireAtGameTime || entity == null) {
                iterator.remove();
            }
        }
    }

    private static void applyKnockupAndFear(LivingEntity target) {
        target.setOnGround(false);
        target.setDeltaMovement(target.getDeltaMovement().x, KNOCKUP_VELOCITY, target.getDeltaMovement().z);
        target.hurtMarked = true;

        target.addEffect(new MobEffectInstance(
                PBMEffects.FEAR_DEBUFF,
                FEAR_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
    }
}
