package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.gameasset.PBSkills;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import com.rave.projectbabylonweapons.passive.special.ArclightFormPassiveHandler;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.world.entity.effect.ArclightRainPortalEntity;
import com.rave.projectbabylonweapons.world.entity.projectile.ArclightMiniProjectileEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
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

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EternalLightSkill extends WeaponInnateSkill {
    private static final UUID AUTO_CONTACT_UUID = UUID.fromString("81f16175-fc23-4a70-b164-91e231c33eb2");
    private static final int AUTO_COST = 1;
    private static final int AIRSLASH_COST = 2;
    private static final int DASH_COST = 5;
    private static final float MINI_DAMAGE_MULTIPLIER = 0.15F;
    private static final float SPEAR_DAMAGE_MULTIPLIER = 1.0F;
    private static final int AUTO_SEQUENCE_WINDOW_TICKS = 60;
    private static final Map<UUID, List<UUID>> PENDING_MINI_PROJECTILES = new ConcurrentHashMap<>();
    private static final Map<UUID, List<UUID>> PENDING_SPEAR_PROJECTILES = new ConcurrentHashMap<>();
    private static final Map<UUID, AutoSequence> AUTO_SEQUENCES = new ConcurrentHashMap<>();
    private static final Map<UUID, List<UUID>> PENDING_RAIN_PORTALS = new ConcurrentHashMap<>();
    private static final Map<UUID, DamageBonusState> DAMAGE_BONUSES = new ConcurrentHashMap<>();

    private float damageBonusPerCharge = 0.015F;
    private int damageBonusDurationTicks = 100;
    private int expirationWarningTicks = 40;
    private float barrierPerRemainingCharge = 0.015F;
    private int expirationBarrierDurationTicks = 200;

    public EternalLightSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
    }

    @Override
    public void setParams(CompoundTag parameters) {
        super.setParams(parameters);
        if (parameters.contains("damage_bonus_per_charge")) {
            this.damageBonusPerCharge = parameters.getFloat("damage_bonus_per_charge");
        }
        if (parameters.contains("damage_bonus_duration_ticks")) {
            this.damageBonusDurationTicks = parameters.getInt("damage_bonus_duration_ticks");
        }
        if (parameters.contains("expiration_warning_ticks")) {
            this.expirationWarningTicks = parameters.getInt("expiration_warning_ticks");
        }
        if (parameters.contains("barrier_per_remaining_charge")) {
            this.barrierPerRemainingCharge = parameters.getFloat("barrier_per_remaining_charge");
        }
        if (parameters.contains("expiration_barrier_duration_ticks")) {
            this.expirationBarrierDurationTicks = parameters.getInt("expiration_barrier_duration_ticks");
        }
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
                            && event.getPhaseOrder() == 1) {
                        launchPendingProjectiles(event.getPlayerPatch().getOriginal());
                    } else if (event.getAnimation() == PBAnimations.EVERGATE_EXTRA_AUTO_2
                            && event.getPhaseOrder() == 0) {
                        launchPendingSpear(event.getPlayerPatch().getOriginal());
                    } else if (event.getAnimation() == PBAnimations.EVERGATE_EXTRA_AUTO_3
                            && event.getPhaseOrder() == 0) {
                        activateRainPortals(event.getPlayerPatch().getOriginal());
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
                addDamageBonus(playerPatch.getOriginal(), type.cost);
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
            clearRainPortals(container.getExecutor().getOriginal());
            AUTO_SEQUENCES.remove(container.getExecutor().getOriginal().getUUID());
            DAMAGE_BONUSES.remove(container.getExecutor().getOriginal().getUUID());
            ArclightAwakeningSkill.resetForm(container);
        }
        super.onRemoved(container);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)
                || !(PBSkills.ETERNAL_LIGHT instanceof EternalLightSkill skill)) {
            return;
        }

        long gameTime = player.level().getGameTime();
        DamageBonusState bonus = DAMAGE_BONUSES.get(player.getUUID());
        if (bonus != null && gameTime >= bonus.expiresAt()) {
            DAMAGE_BONUSES.remove(player.getUUID());
        }

        ItemStack weapon = player.getMainHandItem();
        if (!ArclightSwordItem.isEvergate(weapon)) {
            return;
        }

        long expiresAt = ArclightSwordItem.getFormExpiresAt(weapon);
        if (expiresAt <= 0L) {
            expiresAt = gameTime + PBSkills.ARCLIGHT_AWAKENING.getMaxDuration();
            ArclightSwordItem.awaken(weapon, expiresAt);
            syncInventory(player);
        }

        long remaining = expiresAt - gameTime;
        if (remaining <= skill.expirationWarningTicks && !ArclightSwordItem.hasExpirationWarning(weapon)) {
            ArclightSwordItem.markExpirationWarning(weapon);
            WeaponVisualEffectHelper.playEvergateExpirationWarning(player);
            syncInventory(player);
        }
        if (remaining <= 0L) {
            skill.expireForm(player, weapon);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide
                || !(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || event.getAmount() <= 0.0F) {
            return;
        }

        DamageBonusState bonus = DAMAGE_BONUSES.get(attacker.getUUID());
        long gameTime = attacker.level().getGameTime();
        if (bonus == null || gameTime >= bonus.expiresAt()) {
            DAMAGE_BONUSES.remove(attacker.getUUID());
            return;
        }
        event.setAmount(event.getAmount() * (1.0F + bonus.spentCharges() * current().damageBonusPerCharge));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DAMAGE_BONUSES.remove(event.getEntity().getUUID());
    }

    private void addDamageBonus(LivingEntity player, int spentCharges) {
        long expiresAt = player.level().getGameTime() + this.damageBonusDurationTicks;
        DAMAGE_BONUSES.compute(player.getUUID(), (id, current) -> new DamageBonusState(
                (current == null ? 0 : current.spentCharges()) + spentCharges,
                expiresAt
        ));
    }

    private void expireForm(ServerPlayer player, ItemStack weapon) {
        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        SkillContainer container = playerPatch == null ? null : playerPatch.getSkill(SkillSlots.WEAPON_INNATE);
        int remainingCharges = container != null && container.getSkill() == this ? container.getStack() : 0;
        if (remainingCharges > 0) {
            ArclightFormPassiveHandler.grantBarrier(
                    player,
                    player.getMaxHealth() * this.barrierPerRemainingCharge * remainingCharges,
                    this.expirationBarrierDurationTicks
            );
            Skill.setSkillStackSynchronize(container, 0);
        }

        WeaponVisualEffectHelper.burstArclightAwakening(player);
        DAMAGE_BONUSES.remove(player.getUUID());
        clearPendingProjectiles(player);
        clearPendingSpear(player);
        clearRainPortals(player);
        AUTO_SEQUENCES.remove(player.getUUID());
        ArclightSwordItem.reset(weapon);
        syncInventory(player);

        if (playerPatch != null) {
            playerPatch.modifyLivingMotionByCurrentItem(false);
            player.server.tell(new net.minecraft.server.TickTask(player.server.getTickCount() + 1, () -> {
                ItemStack currentWeapon = player.getMainHandItem();
                if (currentWeapon.getItem() instanceof ArclightSwordItem && !ArclightSwordItem.isEvergate(currentWeapon)) {
                    EpicFightCapabilities.getItemStackCapability(currentWeapon)
                            .changeWeaponInnateSkill((yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch) playerPatch, currentWeapon);
                }
            }));
        }
    }

    private static EternalLightSkill current() {
        return (EternalLightSkill) PBSkills.ETERNAL_LIGHT;
    }

    private static void syncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics,
                          float x, float y, float partialTick) {
        super.drawOnGui(gui, container, guiGraphics, x, y, partialTick);
        ItemStack weapon = container.getExecutor().getOriginal().getMainHandItem();
        long expiresAt = ArclightSwordItem.getFormExpiresAt(weapon);
        if (expiresAt <= 0L) {
            return;
        }

        long remainingTicks = Math.max(0L, expiresAt - container.getExecutor().getOriginal().level().getGameTime());
        String timer = String.format("%.1f", remainingTicks / 20.0F);
        int textX = Math.round(x + 16.0F - gui.getFont().width(timer) * 0.5F);
        int textY = Math.round(y - 10.0F);
        guiGraphics.drawString(gui.getFont(), timer, textX, textY, 0xFFF4D88A, true);
    }

    private static void executeAutoSequence(SkillContainer container) {
        LivingEntity caster = container.getExecutor().getOriginal();
        long gameTime = caster.level().getGameTime();
        AutoSequence sequence = AUTO_SEQUENCES.get(caster.getUUID());
        boolean continueSequence = sequence != null
                && gameTime - sequence.lastUseTick() <= AUTO_SEQUENCE_WINDOW_TICKS;

        if (!continueSequence) {
            clearPendingSpear(caster);
            spawnPortals(caster);
            container.getExecutor().playAnimationSynchronized(PBAnimations.EVERGATE_EXTRA_AUTO_1, 0.0F);
            AUTO_SEQUENCES.put(caster.getUUID(), new AutoSequence(gameTime, 1));
            return;
        }

        if (sequence.step() == 1) {
            spawnSpear(caster);
            container.getExecutor().playAnimationSynchronized(PBAnimations.EVERGATE_EXTRA_AUTO_2, 0.0F);
            AUTO_SEQUENCES.put(caster.getUUID(), new AutoSequence(gameTime, 2));
            return;
        }

        spawnRainPortals(caster);
        container.getExecutor().playAnimationSynchronized(PBAnimations.EVERGATE_EXTRA_AUTO_3, 0.0F);
        AUTO_SEQUENCES.remove(caster.getUUID());
    }

    private static void spawnRainPortals(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        clearRainPortals(caster);
        Vec3 forward = flatForward(caster);
        Vec3 areaCenter = caster.position().add(forward.scale(8.0D)).add(0.0D, 7.0D, 0.0D);
        float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * MINI_DAMAGE_MULTIPLIER;
        int portalCount = 6 + caster.getRandom().nextInt(4);
        List<UUID> portals = new ArrayList<>(portalCount);

        for (int i = 0; i < portalCount; i++) {
            ArclightRainPortalEntity portal = new ArclightRainPortalEntity(level);
            portal.configure(caster, areaCenter, forward, damage, 2 + caster.getRandom().nextInt(3));
            level.addFreshEntity(portal);
            portals.add(portal.getUUID());
        }

        PENDING_RAIN_PORTALS.put(caster.getUUID(), portals);
    }

    private static void activateRainPortals(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        List<UUID> portalIds = PENDING_RAIN_PORTALS.get(caster.getUUID());
        if (portalIds == null) {
            return;
        }

        List<ArclightRainPortalEntity> portals = new ArrayList<>(portalIds.size());
        for (UUID portalId : portalIds) {
            if (level.getEntity(portalId) instanceof ArclightRainPortalEntity portal) {
                portals.add(portal);
            }
        }
        portals.sort((left, right) -> Double.compare(right.getY(), left.getY()));
        for (int i = 0; i < portals.size(); i++) {
            portals.get(i).activate(i * 2);
        }
    }

    private static void clearRainPortals(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        List<UUID> portalIds = PENDING_RAIN_PORTALS.remove(caster.getUUID());
        if (portalIds == null) {
            return;
        }

        for (UUID portalId : portalIds) {
            if (level.getEntity(portalId) instanceof ArclightRainPortalEntity portal) {
                portal.discard();
            }
        }
    }
    private static void spawnSpear(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        clearPendingSpear(caster);
        Vec3 forward = flatForward(caster);
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * SPEAR_DAMAGE_MULTIPLIER;
        List<UUID> spears = new ArrayList<>(2);

        for (double sideOffset : new double[]{-1.25D, 1.25D}) {
            Vec3 spawnPosition = caster.position()
                    .add(right.scale(sideOffset))
                    .subtract(forward.scale(1.0D))
                    .add(0.0D, 1.35D, 0.0D);
            ArclightMiniProjectileEntity spear = new ArclightMiniProjectileEntity(
                    com.rave.projectbabylonweapons.init.PBModEntities.ARCLIGHT_SPEAR_PROJECTILE.get(), level);
            spear.setPos(spawnPosition);
            spear.configure(caster, forward, damage);
            level.addFreshEntity(spear);
            spears.add(spear.getUUID());
        }

        PENDING_SPEAR_PROJECTILES.put(caster.getUUID(), spears);
    }

    private static void launchPendingSpear(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        List<UUID> projectileIds = PENDING_SPEAR_PROJECTILES.remove(caster.getUUID());
        if (projectileIds == null) {
            return;
        }

        for (UUID projectileId : projectileIds) {
            if (level.getEntity(projectileId) instanceof ArclightMiniProjectileEntity spear) {
                spear.queueLaunch(0);
            }
        }
    }

    private static void clearPendingSpear(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }

        List<UUID> projectileIds = PENDING_SPEAR_PROJECTILES.remove(caster.getUUID());
        if (projectileIds == null) {
            return;
        }

        for (UUID projectileId : projectileIds) {
            if (level.getEntity(projectileId) instanceof ArclightMiniProjectileEntity spear
                    && spear.getState() <= ArclightMiniProjectileEntity.STATE_QUEUED) {
                spear.discard();
            }
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

    private record AutoSequence(long lastUseTick, int step) {
    }

    private record DamageBonusState(int spentCharges, long expiresAt) {
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