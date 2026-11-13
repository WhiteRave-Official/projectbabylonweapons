package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.event.SickleThrowParametersEvent;
import net.minecraftforge.common.MinecraftForge;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.handler.FearHandler;
import com.rave.projectbabylonweapons.network.PBNetworkManager;
import com.rave.projectbabylonweapons.network.SPSickleActiveSync;
import com.rave.projectbabylonweapons.world.entity.projectile.SickleProjectileEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID)
public class SickleThrowSkill extends WeaponInnateSkill implements ChargeableSkill {
    private static final UUID CHARGING_SLOW_UUID = UUID.fromString("f2fd8aa1-ec8a-4da4-a67f-b18620d4f6d2");
    private static final double CHARGING_SLOW_MULTIPLIER = -0.7D;
    public static final int TETHER_DURATION_TICKS = 7 * 20;
    public static final double MAX_CHAIN_DISTANCE = 10.0;
    public static final double MAX_TETHER_DISTANCE = 11.0;
    public static final double MIN_CHAIN_DISTANCE = 1.0;
    public static final float PROJECTILE_DAMAGE = 5.0f;

    private static final Map<UUID, LockedItem> LOCKED_ITEMS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> ACTIVE_PROJECTILES = new ConcurrentHashMap<>();


    private static final Map<UUID, Boolean> CHARGING_PLAYERS = new ConcurrentHashMap<>();

    private static final class LockedItem {
        private final int slot;
        private final ItemStack stack;

        private LockedItem(int slot, ItemStack stack) {
            this.slot = slot;
            this.stack = stack;
        }
    }

    public SickleThrowSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
    }

    @Override
    public void onRemoved(SkillContainer container) {
        removeChargingSlow(container);
        super.onRemoved(container);
    }

    @Override
    public int getAllowedMaxChargingTicks() {
        return 40;
    }

    @Override
    public int getMaxChargingTicks() {
        return 40;
    }

    @Override
    public int getMinChargingTicks() {
        return 6;
    }

    @Override
    public void startHolding(SkillContainer container) {

        UUID playerUUID = container.getExecutor().getOriginal().getUUID();
        CHARGING_PLAYERS.put(playerUUID, true);
        applyChargingSlow(container);

        AssetAccessor<? extends StaticAnimation> currentPlaying = container.getExecutor().getAnimator().getPlayerFor(null).getRealAnimation();

        if (currentPlaying.get().isMainFrameAnimation()) {
            container.getExecutor().stopPlaying(currentPlaying);
        }

        container.getExecutor().playAnimationSynchronized(PBAnimations.SICKLE_READY, 0.0F);
    }

    @Override
    public void resetHolding(SkillContainer container) {

        UUID playerUUID = container.getExecutor().getOriginal().getUUID();
        CHARGING_PLAYERS.remove(playerUUID);
        removeChargingSlow(container);

        if (container.getExecutor().isLogicalClient()) {
            container.getExecutor().getAnimator().stopPlaying(PBAnimations.SICKLE_READY);
        } else {
            container.getExecutor().stopPlaying(PBAnimations.SICKLE_READY);
        }
    }

    @Override
    public void onStopHolding(SkillContainer container, SPSkillExecutionFeedback feedback) {
        removeChargingSlow(container);

        if (container.getExecutor().isLogicalClient()) {
            return;
        }

        ServerPlayerPatch playerPatch = container.getServerExecutor();
        ServerPlayer player = (ServerPlayer) playerPatch.getOriginal();


        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.isEmpty()) {
            LockedItem locked = LOCKED_ITEMS.get(player.getUUID());
            if (locked != null) {
                held = locked.stack;
            } else {
                return;
            }
        }

        SickleThrowParametersEvent parameters = parameters(container);
        if (SickleThrowChains.count(player) >= parameters.maxProjectiles()) return;
        if (parameters.maxProjectiles() > 1) {
            releaseLock(player);
        } else {
            lockMainHand(player);
        }

        float chargeAmount = playerPatch.getAccumulatedChargeAmount();
        float chargeRatio = Math.min(chargeAmount / (float)getMaxChargingTicks(), 1.0f);
        double throwDistance = MIN_CHAIN_DISTANCE + (parameters.maxDistance() - MIN_CHAIN_DISTANCE) * chargeRatio;

        SickleProjectileEntity projectile = new SickleProjectileEntity(player.level(), player);
        projectile.setItemStack(held);
        projectile.setProjectileDamage(parameters.damage());
        projectile.setChainDistance(parameters.maxDistance());
        projectile.setMaxThrowDistance(throwDistance);

        Vec3 eyePos = player.getEyePosition(1.0f);
        projectile.setPos(eyePos.x, eyePos.y, eyePos.z);

        float pitch = player.getViewXRot(1.0f);
        float yaw = player.getViewYRot(1.0f);
        projectile.shootFromRotation(player, pitch, yaw, 0.0f, 1.2f, 1.0f);

        if (!player.level().addFreshEntity(projectile)) {
            if (SickleThrowChains.count(player) == 0) releaseLock(player);
            return;
        }
        SickleThrowChains.add(player, projectile);
        playerPatch.playAnimationSynchronized(PBAnimations.SICKLE_THROW, 0.0f);
    }

    @Override
    public void holdTick(SkillContainer container) {
        ChargeableSkill.super.holdTick(container);
    }

    private SickleThrowParametersEvent parameters(SkillContainer container) {
        var event = new SickleThrowParametersEvent(container, MAX_CHAIN_DISTANCE, getMaxStack());
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        return super.canExecute(container) && (!(container.getExecutor().getOriginal() instanceof ServerPlayer player)
                || SickleThrowChains.count(player) < parameters(container).maxProjectiles());
    }

    private static void applyChargingSlow(SkillContainer container) {
        if (container.getExecutor().isLogicalClient()) {
            return;
        }

        if (!(container.getExecutor().getOriginal() instanceof ServerPlayer player)) {
            return;
        }

        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null || movementSpeed.getModifier(CHARGING_SLOW_UUID) != null) {
            return;
        }

        movementSpeed.addTransientModifier(new AttributeModifier(
                CHARGING_SLOW_UUID,
                "GetOverHere charging slow",
                CHARGING_SLOW_MULTIPLIER,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        ));
    }

    private static void removeChargingSlow(SkillContainer container) {
        if (container.getExecutor().isLogicalClient()) {
            return;
        }

        if (!(container.getExecutor().getOriginal() instanceof ServerPlayer player)) {
            return;
        }

        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(CHARGING_SLOW_UUID);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }

    private static void lockMainHand(ServerPlayer player) {
        int slot = player.getInventory().selected;
        ItemStack stack = player.getInventory().getItem(slot);
        if (stack.isEmpty()) {
            return;
        }
        LOCKED_ITEMS.put(player.getUUID(), new LockedItem(slot, stack.copy()));
        player.getInventory().setItem(slot, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.getInventory().setChanged();
    }

    public static void releaseLock(ServerPlayer player) {
        LockedItem locked = LOCKED_ITEMS.remove(player.getUUID());
        if (locked == null) {
            return;
        }
        ItemStack slotStack = player.getInventory().getItem(locked.slot);
        if (slotStack.isEmpty()) {
            player.getInventory().setItem(locked.slot, locked.stack);
        } else if (!player.getInventory().add(locked.stack)) {
            player.drop(locked.stack, false);
        }
        player.getInventory().setChanged();
    }

    public static void clearActiveProjectile(ServerPlayer player) {
        ACTIVE_PROJECTILES.remove(player.getUUID());
        releaseLock(player);
        if (!player.level().isClientSide) {
            PBNetworkManager.sendToPlayer(player, new SPSickleActiveSync(player.getUUID(), -1));
        }
    }

    public static void setActiveProjectile(ServerPlayer player, int entityId) {
        ACTIVE_PROJECTILES.put(player.getUUID(), entityId);
        if (!player.level().isClientSide) {
            PBNetworkManager.sendToPlayer(player, new SPSickleActiveSync(player.getUUID(), entityId));
        }
    }

    public static void setTetherMovementLock(UUID playerUUID, boolean locked) {
        FearHandler.setSickleMovementLock(playerUUID, locked, "sickle_tether");
    }


    public static boolean isCharging(UUID playerUUID) {
        return CHARGING_PLAYERS.getOrDefault(playerUUID, false);
    }



    public static SickleProjectileEntity getActiveProjectilePublic(ServerPlayer player) {
        return getActiveProjectile(player);
    }

    private static boolean isTetherActive(PlayerPatch<?> playerPatch) {
        return ACTIVE_PROJECTILES.containsKey(playerPatch.getOriginal().getUUID());
    }

    private static SickleProjectileEntity getActiveProjectile(ServerPlayerPatch playerPatch) {
        return getActiveProjectile((ServerPlayer) playerPatch.getOriginal());
    }

    private static SickleProjectileEntity getActiveProjectile(ServerPlayer player) {
        return SickleThrowChains.anchor(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        UUID uuid = serverPlayer.getUUID();
        LockedItem locked = LOCKED_ITEMS.get(uuid);
        if (locked != null) {
            serverPlayer.getInventory().selected = locked.slot;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CHARGING_PLAYERS.remove(serverPlayer.getUUID());
        SickleThrowChains.discardAll(serverPlayer);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CHARGING_PLAYERS.remove(serverPlayer.getUUID());
        SickleThrowChains.discardAll(serverPlayer);
    }
    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CHARGING_PLAYERS.remove(player.getUUID());
            SickleThrowChains.discardAll(player);
        }
    }

    @SubscribeEvent
    public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        SickleThrowChains.reset();
        ACTIVE_PROJECTILES.clear();
        CHARGING_PLAYERS.clear();
        LOCKED_ITEMS.clear();
    }
}
