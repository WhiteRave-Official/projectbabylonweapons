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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.SimpleWeaponInnateSkill;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UppercutSkill extends SimpleWeaponInnateSkill {

    private static final UUID DAMAGE_UUID = UUID.fromString("d3a1f2c4-9b7e-4a11-8c3d-1f2e3a4b5c6d");
    private static final int FEAR_DURATION_TICKS = 3 * 20;
    private static final float KNOCKUP_VELOCITY = 0.70F;
    private static final int KNOCKUP_DELAY_TICKS = 1;
    private static final int PENDING_TIMEOUT_TICKS = 20;
    private static final Map<UUID, PendingKnockup> PENDING_KNOCKUPS = new HashMap<>();

    private static class PendingKnockup {
        private final ResourceKey<Level> dimension;
        private final long applyAtGameTime;
        private final long expireAtGameTime;
        private final UUID ownerId;

        private PendingKnockup(ResourceKey<Level> dimension, long applyAtGameTime, UUID ownerId) {
            this.dimension = dimension;
            this.applyAtGameTime = applyAtGameTime;
            this.expireAtGameTime = applyAtGameTime + PENDING_TIMEOUT_TICKS;
            this.ownerId = ownerId;
        }
    }

    public UppercutSkill(SimpleWeaponInnateSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getExecutor().getEventListener().addEventListener(
                EventType.DEAL_DAMAGE_EVENT_HURT,
                DAMAGE_UUID,
                (event) -> {
                    var eventAnim = event.getDamageSource().getAnimation();
                    if (eventAnim == null) {
                        return;
                    }

                    if (!eventAnim.toString().equals(PBAnimations.UPPERCUT.registryName().toString())) {
                        return;
                    }

                    LivingEntity target = event.getTarget();
                    if (target == null || !target.isAlive()) {
                        return;
                    }

                    queueKnockupAndFear(target, container.getExecutor().getOriginal());
                }
        );
    }

    private static void queueKnockupAndFear(LivingEntity target, LivingEntity owner) {
        if (target.level().isClientSide() || !(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long applyAt = serverLevel.getGameTime() + KNOCKUP_DELAY_TICKS;
        PENDING_KNOCKUPS.put(target.getUUID(), new PendingKnockup(serverLevel.dimension(), applyAt, owner.getUUID()));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING_KNOCKUPS.isEmpty()) {
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
                Entity owner = server.getPlayerList().getPlayer(pending.ownerId);
                if (owner == null) owner = level.getEntity(pending.ownerId);
                applyKnockupAndFear(target, owner);
            }

            if (gameTime >= pending.applyAtGameTime || gameTime > pending.expireAtGameTime || entity == null) {
                iterator.remove();
            }
        }
    }

    private static void applyKnockupAndFear(LivingEntity target, Entity owner) {
        target.setOnGround(false);
        target.setDeltaMovement(target.getDeltaMovement().x, KNOCKUP_VELOCITY, target.getDeltaMovement().z);
        target.hurtMarked = true;

        target.addEffect(new MobEffectInstance(
                PBMEffects.FEAR_DEBUFF.get(),
                FEAR_DURATION_TICKS,
                0,
                false,
                true,
                true
        ), owner);
    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecutor().getEventListener().removeListener(EventType.DEAL_DAMAGE_EVENT_HURT, DAMAGE_UUID);
        super.onRemoved(container);
    }
}

