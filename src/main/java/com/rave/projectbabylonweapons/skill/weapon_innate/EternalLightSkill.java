package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.world.entity.projectile.ArclightMiniProjectileEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EternalLightSkill extends WeaponInnateSkill {
    private static final UUID AUTO_CONTACT_UUID = UUID.fromString("81f16175-fc23-4a70-b164-91e231c33eb2");
    private static final int AUTO_COST = 1;
    private static final int AIRSLASH_COST = 2;
    private static final int DASH_COST = 5;
    private static final float MINI_DAMAGE_MULTIPLIER = 0.15F;
    private static final float SPEAR_DAMAGE_MULTIPLIER = 1.0F;
    private static final int AUTO_SEQUENCE_WINDOW_TICKS = 60;
    private static final Map<UUID, List<UUID>> PENDING_MINI_PROJECTILES = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> PENDING_SPEAR_PROJECTILES = new ConcurrentHashMap<>();
    private static final Map<UUID, AutoSequence> AUTO_SEQUENCES = new ConcurrentHashMap<>();

    public EternalLightSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        container.getExecutor().getEventListener().addEventListener(
                EventType.ATTACK_PHASE_END_EVENT,
                AUTO_CONTACT_UUID,
                event -> {
                    if (container.getExecutor().isLogicalClient()) {
                        return;
                    }
                    if (event.getAnimation() == PBAnimations.EVERGATE_EXTRA_AUTO_1
                            && event.getPhaseOrder() >= 2) {
                        launchPendingProjectiles(event.getPlayerPatch().getOriginal());
                    } else if (event.getAnimation() == PBAnimations.EVERGATE_EXTRA_AUTO_2
                            && event.getPhaseOrder() >= 1) {
                        launchPendingSpear(event.getPlayerPatch().getOriginal());
                    }
                }
        );
    }

    public boolean canExecute(SkillContainer container) {
        return super.canExecute(container)
                && ArclightSwordItem.isEvergate(((Player) container.getExecutor().getOriginal()).getMainHandItem());
    }

    public boolean isExecutableState(PlayerPatch<?> executor) {
        return !((Player) executor.getOriginal()).isSpectator() && executor.getEntityState().canUseSkill();
    }

    @OnlyIn(Dist.CLIENT)
    public FriendlyByteBuf gatherArguments(SkillContainer container, ControlEngine controlEngine) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        LocalPlayer player = (LocalPlayer) controlEngine.getPlayerPatch().getOriginal();
        AttackType type = !player.onGround() ? AttackType.AIRSLASH : (player.isSprinting() ? AttackType.DASH : AttackType.AUTO);
        buffer.writeByte(type.ordinal());
        return buffer;
    }

    public boolean resourcePredicate(PlayerPatch<?> playerPatch, SkillCastEvent event) {
        AttackType type = readAttackType(event.getArguments());
        SkillContainer container = playerPatch.getSkill(SkillSlots.WEAPON_INNATE);
        if (container.getSkill() == this && container.getStack() >= type.cost) {
            if (!playerPatch.isLogicalClient()) {
                Skill.setSkillStackSynchronize(container, container.getStack() - type.cost);
            }
            return true;
        }
        return false;
    }

    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        super.executeOnServer(container, args);
        AttackType type = readAttackType(args);
        switch (type) {
            case DASH -> container.getExecutor().playAnimationSynchronized(PBAnimations.EVERGATE_EXTRA_DASH, 0.0F);
            case AIRSLASH -> container.getExecutor().playAnimationSynchronized(PBAnimations.EVERGATE_EXTRA_AIRSLASH, 0.0F);
            case AUTO -> executeAutoSequence(container);
        }
    }

    public void onRemoved(SkillContainer container) {
        container.getExecutor().getEventListener().removeListener(EventType.ATTACK_PHASE_END_EVENT, AUTO_CONTACT_UUID);
        if (!container.getExecutor().isLogicalClient()) {
            clearPendingProjectiles(container.getExecutor().getOriginal());
            clearPendingSpear(container.getExecutor().getOriginal());
            AUTO_SEQUENCES.remove(container.getExecutor().getOriginal().getUUID());
            ArclightAwakeningSkill.resetForm(container);
        }
        super.onRemoved(container);
    }

    private static void executeAutoSequence(SkillContainer container) {
        LivingEntity caster = container.getExecutor().getOriginal();
        long gameTime = caster.level().getGameTime();
        AutoSequence sequence = AUTO_SEQUENCES.get(caster.getUUID());
        boolean continueSequence = sequence != null
                && gameTime - sequence.lastUseTick() <= AUTO_SEQUENCE_WINDOW_TICKS;

        if (continueSequence) {
            spawnSpear(caster);
            container.getExecutor().playAnimationSynchronized(PBAnimations.EVERGATE_EXTRA_AUTO_2, 0.0F);
            AUTO_SEQUENCES.remove(caster.getUUID());
        } else {
            clearPendingSpear(caster);
            spawnPortals(caster);
            container.getExecutor().playAnimationSynchronized(PBAnimations.EVERGATE_EXTRA_AUTO_1, 0.0F);
            AUTO_SEQUENCES.put(caster.getUUID(), new AutoSequence(gameTime));
        }
    }

    private static void spawnSpear(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        clearPendingSpear(caster);
        Vec3 forward = flatForward(caster);
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 spawnPosition = caster.position()
                .subtract(right.scale(1.75D))
                .subtract(forward.scale(1.0D))
                .add(0.0D, 1.35D, 0.0D);
        float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * SPEAR_DAMAGE_MULTIPLIER;

        ArclightMiniProjectileEntity spear = new ArclightMiniProjectileEntity(
                com.rave.projectbabylonweapons.init.PBModEntities.ARCLIGHT_SPEAR_PROJECTILE.get(), level);
        spear.setPos(spawnPosition);
        spear.configure(caster, forward, damage);
        level.addFreshEntity(spear);
        PENDING_SPEAR_PROJECTILES.put(caster.getUUID(), spear.getUUID());
    }

    private static void launchPendingSpear(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        UUID projectileId = PENDING_SPEAR_PROJECTILES.remove(caster.getUUID());
        if (projectileId != null
                && level.getEntity(projectileId) instanceof ArclightMiniProjectileEntity spear) {
            spear.queueLaunch(0);
        }
    }

    private static void clearPendingSpear(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        UUID projectileId = PENDING_SPEAR_PROJECTILES.remove(caster.getUUID());
        if (projectileId != null
                && level.getEntity(projectileId) instanceof ArclightMiniProjectileEntity spear
                && spear.getState() <= ArclightMiniProjectileEntity.STATE_QUEUED) {
            spear.discard();
        }
    }
    private static void spawnPortals(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        clearPendingProjectiles(caster);
        Vec3 forward = flatForward(caster);
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        int count = 3 + caster.getRandom().nextInt(6);
        float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * MINI_DAMAGE_MULTIPLIER;
        List<UUID> projectiles = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            double centeredIndex = i - (count - 1) * 0.5D;
            double sideOffset = centeredIndex * 0.9D + (caster.getRandom().nextDouble() - 0.5D) * 0.36D;
            double rearOffset = 2.5D + caster.getRandom().nextDouble();
            double heightOffset = 0.45D + caster.getRandom().nextDouble() * 1.75D;
            Vec3 spawnPosition = caster.position()
                    .subtract(forward.scale(rearOffset))
                    .add(right.scale(sideOffset))
                    .add(0.0D, heightOffset, 0.0D);
            Vec3 direction = forward
                    .add(right.scale((caster.getRandom().nextDouble() - 0.5D) * 0.16D))
                    .add(0.0D, (caster.getRandom().nextDouble() - 0.5D) * 0.12D, 0.0D)
                    .normalize();

            ArclightMiniProjectileEntity projectile = new ArclightMiniProjectileEntity(level);
            projectile.setPos(spawnPosition);
            projectile.configure(caster, direction, damage);
            level.addFreshEntity(projectile);
            projectiles.add(projectile.getUUID());
        }

        PENDING_MINI_PROJECTILES.put(caster.getUUID(), projectiles);
    }

    private static void launchPendingProjectiles(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        List<UUID> projectileIds = PENDING_MINI_PROJECTILES.remove(caster.getUUID());
        if (projectileIds == null) {
            return;
        }

        int delay = 0;
        for (UUID projectileId : projectileIds) {
            if (level.getEntity(projectileId) instanceof ArclightMiniProjectileEntity projectile) {
                projectile.queueLaunch(delay);
                delay += 1 + caster.getRandom().nextInt(2);
            }
        }
    }

    private static void clearPendingProjectiles(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        List<UUID> projectileIds = PENDING_MINI_PROJECTILES.remove(caster.getUUID());
        if (projectileIds == null) {
            return;
        }

        for (UUID projectileId : projectileIds) {
            if (level.getEntity(projectileId) instanceof ArclightMiniProjectileEntity projectile
                    && projectile.getState() <= ArclightMiniProjectileEntity.STATE_QUEUED) {
                projectile.discard();
            }
        }
    }

    private static Vec3 flatForward(LivingEntity caster) {
        Vec3 look = caster.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        return flat.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
    }

    private static AttackType readAttackType(FriendlyByteBuf args) {
        if (args != null && args.isReadable()) {
            int ordinal = args.getUnsignedByte(args.readerIndex());
            return ordinal < AttackType.values().length ? AttackType.values()[ordinal] : AttackType.AUTO;
        }
        return AttackType.AUTO;
    }

    @OnlyIn(Dist.CLIENT)
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }

    private record AutoSequence(long lastUseTick) {
    }
    private enum AttackType {
        AUTO(AUTO_COST),
        DASH(DASH_COST),
        AIRSLASH(AIRSLASH_COST);

        private final int cost;

        AttackType(int cost) {
            this.cost = cost;
        }
    }
}