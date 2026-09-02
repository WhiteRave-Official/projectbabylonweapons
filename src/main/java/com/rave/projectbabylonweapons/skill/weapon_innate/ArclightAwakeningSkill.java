package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.gameasset.PBSkills;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import com.rave.projectbabylonweapons.init.PBWSounds;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.passive.special.ArclightFormPassiveHandler;
import com.rave.projectbabylonweapons.summon.arclight.ArclightSummonManager;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArclightAwakeningSkill extends WeaponInnateSkill {
    private static final UUID CONTACT_UUID = UUID.fromString("c28a0abe-5605-4ccc-a736-338aa1cc9e8f");
    private static final Set<UUID> PENDING_SUMMON_CASTS = ConcurrentHashMap.newKeySet();
    private static final ResourceLocation SUMMON_ICON = ResourceLocation.fromNamespaceAndPath(
            ProjectBabylonWeapons.MODID, "textures/gui/skills/weapon_innate/summon.png");
    private static final float AWAKENING_COST = 300.0F;
    private static final int SUMMON_ICON_TEXTURE_SIZE = 32;
    private static final float SUMMON_ICON_SCALE = 0.5F;
    private static final float SUMMON_ICON_OFFSET_X = 21.0F;
    private static final float SUMMON_ICON_OFFSET_Y = -7.0F;

    private float summonCost = 150.0F;
    private float awakeningBarrier = 200.0F;
    private int awakeningProtectionTicks = 280;
    private int summonLifetimeTicks = 1200;
    private double summonTargetRadius = 8.0D;
    private int summonTargetSearchInterval = 9;
    private int summonWindupTicks = 12;
    private int summonAttackTicks = 10;
    private int summonReturnTicks = 16;
    private int summonSwordCooldownTicks = 30;
    private int summonSpearCooldownTicks = 40;
    private float summonSwordNormalDamage = 0.15F;
    private float summonSwordDashDamage = 0.20F;
    private float summonSpearNormalDamage = 0.25F;
    private float summonSpearSpinDamage = 0.20F;

    public ArclightAwakeningSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
    }

    @Override
    public void setParams(CompoundTag parameters) {
        super.setParams(parameters);
        this.summonCost = getFloat(parameters, "summon_cost", 150.0F);
        this.awakeningBarrier = getFloat(parameters, "awakening_barrier", 200.0F);
        this.awakeningProtectionTicks = getInt(parameters, "awakening_protection_ticks", 280);
        this.summonLifetimeTicks = getInt(parameters, "summon_lifetime_ticks", 1200);
        this.summonTargetRadius = getDouble(parameters, "summon_target_radius", 8.0D);
        this.summonTargetSearchInterval = getInt(parameters, "summon_target_search_interval", 9);
        this.summonWindupTicks = getInt(parameters, "summon_windup_ticks", 12);
        this.summonAttackTicks = getInt(parameters, "summon_attack_ticks", 10);
        this.summonReturnTicks = getInt(parameters, "summon_return_ticks", 16);
        this.summonSwordCooldownTicks = getInt(parameters, "summon_sword_cooldown_ticks", 30);
        this.summonSpearCooldownTicks = getInt(parameters, "summon_spear_cooldown_ticks", 40);
        this.summonSwordNormalDamage = getFloat(parameters, "summon_sword_normal_damage", 0.15F);
        this.summonSwordDashDamage = getFloat(parameters, "summon_sword_dash_damage", 0.20F);
        this.summonSpearNormalDamage = getFloat(parameters, "summon_spear_normal_damage", 0.25F);
        this.summonSpearSpinDamage = getFloat(parameters, "summon_spear_spin_damage", 0.20F);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        container.getExecutor().getEventListener().addEventListener(
                EventType.ATTACK_PHASE_END_EVENT,
                CONTACT_UUID,
                event -> {
                    if (event.getAnimation() == PBAnimations.SUMMON) {
                        UUID playerId = event.getPlayerPatch().getOriginal().getUUID();
                        ProjectBabylonWeapons.LOGGER.info("[ArclightSummonDebug] phase_contact player={} phase={} pending={}",
                                event.getPlayerPatch().getOriginal().getName().getString(),
                                event.getPhaseOrder(), PENDING_SUMMON_CASTS.contains(playerId));
                        if (PENDING_SUMMON_CASTS.remove(playerId)) {
                            summonAtContact(event.getPlayerPatch());
                        }
                        return;
                    }
                    if (event.getPhaseOrder() != 0) {
                        return;
                    }
                    if (event.getAnimation() == PBAnimations.ARCLIGHT_AWAKENING) {
                        awakenAtContact(event.getPlayerPatch());
                    }
                });
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        return super.canExecute(container)
                && !ArclightSwordItem.isEvergate(container.getExecutor().getOriginal().getMainHandItem());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public FriendlyByteBuf gatherArguments(SkillContainer container, ControlEngine controlEngine) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(Screen.hasShiftDown() ? Action.SUMMON.ordinal() : Action.AWAKENING.ordinal());
        return buffer;
    }

    @Override
    public boolean resourcePredicate(PlayerPatch<?> playerPatch, SkillCastEvent event) {
        SkillContainer container = playerPatch.getSkill(SkillSlots.WEAPON_INNATE);
        if (container.getSkill() != this) {
            return false;
        }
        Action action = readAction(event.getArguments());
        float cost = action == Action.SUMMON ? this.summonCost : AWAKENING_COST;
        if (getSharedEnergy(container) + 1.0E-4F < cost) {
            return false;
        }
        if (action == Action.AWAKENING && !playerPatch.isLogicalClient()) {
            consumeSharedEnergy(container, cost);
        }
        return true;
    }

    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        super.executeOnServer(container, args);
        Action action = readAction(args);
        if (action == Action.SUMMON) {
            PENDING_SUMMON_CASTS.add(container.getExecutor().getOriginal().getUUID());
            ProjectBabylonWeapons.LOGGER.info("[ArclightSummonDebug] execute player={} energy={}",
                    container.getExecutor().getOriginal().getName().getString(), getSharedEnergy(container));
            container.getExecutor().playAnimationSynchronized(PBAnimations.SUMMON, 0.0F);
            return;
        }

        container.activate();
        if (!(container.getExecutor().getOriginal() instanceof ServerPlayer player)) {
            return;
        }
        ArclightFormPassiveHandler.grantBarrier(player, this.awakeningBarrier,
                this.awakeningProtectionTicks, this.awakeningBarrier);
        player.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(),
                this.awakeningProtectionTicks, 0, false, true, true));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                PBWSounds.ARCLIGHT_AWAKENING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        container.getExecutor().playAnimationSynchronized(PBAnimations.ARCLIGHT_AWAKENING, 0.0F);
        WeaponVisualEffectHelper.startArclightAwakening(player);
    }

    @Override
    public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
        super.executeOnClient(container, args);
        if (readAction(args) == Action.AWAKENING) {
            container.activate();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics,
                          float x, float y, float partialTick) {
        super.drawOnGui(gui, container, guiGraphics, x, y, partialTick);
        float iconX = x + SUMMON_ICON_OFFSET_X;
        float iconY = y + SUMMON_ICON_OFFSET_Y;
        float fill = container.getExecutor().getOriginal().getAbilities().instabuild
                ? 1.0F
                : Math.min(1.0F, getSharedEnergy(container) / Math.max(1.0F, this.summonCost));
        int filledPixels = Mth.ceil(SUMMON_ICON_TEXTURE_SIZE * fill);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(iconX, iconY, 0.0F);
        guiGraphics.pose().scale(SUMMON_ICON_SCALE, SUMMON_ICON_SCALE, 1.0F);

        RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 0.8F);
        guiGraphics.blit(SUMMON_ICON, 0, 0, 0.0F, 0.0F,
                SUMMON_ICON_TEXTURE_SIZE, SUMMON_ICON_TEXTURE_SIZE,
                SUMMON_ICON_TEXTURE_SIZE, SUMMON_ICON_TEXTURE_SIZE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (filledPixels > 0) {
            int sourceY = SUMMON_ICON_TEXTURE_SIZE - filledPixels;
            guiGraphics.blit(SUMMON_ICON, 0, sourceY, 0.0F, sourceY,
                    SUMMON_ICON_TEXTURE_SIZE, filledPixels,
                    SUMMON_ICON_TEXTURE_SIZE, SUMMON_ICON_TEXTURE_SIZE);
        }
        guiGraphics.pose().popPose();
    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecutor().getEventListener().removeListener(EventType.ATTACK_PHASE_END_EVENT, CONTACT_UUID);
        PENDING_SUMMON_CASTS.remove(container.getExecutor().getOriginal().getUUID());
        if (!container.getExecutor().isLogicalClient()
                && container.getExecutor().getOriginal() instanceof ServerPlayer player) {
            ArclightSummonManager.dismiss(player, true);
            if (!ArclightSwordItem.isEvergate(player.getMainHandItem())) {
                resetForm(container);
            }
        }
        super.onRemoved(container);
    }

    @Override
    public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
        resetForm(container);
        container.deactivate();
        super.cancelOnServer(container, args);
    }

    @Override
    public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
        container.deactivate();
        super.cancelOnClient(container, args);
    }

    public static void awakenAtContact(LivingEntityPatch<?> entityPatch) {
        if (!(entityPatch instanceof ServerPlayerPatch serverPatch)
                || !(entityPatch.getOriginal() instanceof ServerPlayer player)) return;

        ItemStack arclight = player.getMainHandItem();
        if (!(arclight.getItem() instanceof ArclightSwordItem) || ArclightSwordItem.isEvergate(arclight)) return;
        ArclightSummonManager.dismiss(player, true);
        WeaponVisualEffectHelper.burstArclightAwakening(player);
        ArclightSwordItem.awaken(arclight, player.level().getGameTime() + PBSkills.ARCLIGHT_AWAKENING.getMaxDuration());
        syncInventory(player);
        serverPatch.modifyLivingMotionByCurrentItem(false);

        player.server.tell(new net.minecraft.server.TickTask(player.server.getTickCount() + 1, () -> {
            ItemStack current = player.getMainHandItem();
            if (current.getItem() instanceof ArclightSwordItem && ArclightSwordItem.isEvergate(current)) {
                EpicFightCapabilities.getItemStackCapability(current).changeWeaponInnateSkill(serverPatch, current);
            }
        }));
    }

    private void summonAtContact(PlayerPatch<?> playerPatch) {
        if (!(playerPatch instanceof ServerPlayerPatch)
                || !(playerPatch.getOriginal() instanceof ServerPlayer player)) {
            return;
        }
        SkillContainer container = playerPatch.getSkill(SkillSlots.WEAPON_INNATE);
        float energy = getSharedEnergy(container);
        boolean creative = player.getAbilities().instabuild;
        if (container.getSkill() != this || (!creative && energy + 1.0E-4F < this.summonCost)) {
            ProjectBabylonWeapons.LOGGER.info("[ArclightSummonDebug] contact_rejected player={} skillMatches={} creative={} energy={} cost={}",
                    player.getName().getString(), container.getSkill() == this, creative, energy, this.summonCost);
            return;
        }
        ProjectBabylonWeapons.LOGGER.info("[ArclightSummonDebug] contact_accepted player={} creative={} energy={} cost={}",
                player.getName().getString(), creative, energy, this.summonCost);
        if (!creative) {
            consumeSharedEnergy(container, this.summonCost);
        }
        ArclightSummonManager.replaceSummon(player, this.createSummonBalance());
    }

    private ArclightSummonedWeaponEntity.Balance createSummonBalance() {
        return new ArclightSummonedWeaponEntity.Balance(
                this.summonLifetimeTicks, this.summonTargetRadius, this.summonTargetSearchInterval,
                this.summonWindupTicks, this.summonAttackTicks, this.summonReturnTicks,
                this.summonSwordCooldownTicks, this.summonSpearCooldownTicks,
                this.summonSwordNormalDamage, this.summonSwordDashDamage,
                this.summonSpearNormalDamage, this.summonSpearSpinDamage);
    }

    static void resetForm(SkillContainer container) {
        if (!(container.getExecutor() instanceof ServerPlayerPatch serverPatch)
                || !(container.getExecutor().getOriginal() instanceof ServerPlayer player)) return;

        WeaponVisualEffectHelper.stopArclightAwakening(player);
        boolean changed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof ArclightSwordItem) {
                changed |= ArclightSwordItem.reset(stack);
            }
        }
        if (changed) {
            syncInventory(player);
            serverPatch.modifyLivingMotionByCurrentItem(false);
        }
    }

    private static float getSharedEnergy(SkillContainer container) {
        return container.getResource() + container.getStack() * container.getMaxResource();
    }

    private static void consumeSharedEnergy(SkillContainer container, float amount) {
        float maxResource = Math.max(1.0F, container.getMaxResource());
        float remaining = Math.max(0.0F, getSharedEnergy(container) - amount);
        int stack = (int) (remaining / maxResource);
        float resource = remaining - stack * maxResource;
        Skill.setSkillStackSynchronize(container, stack);
        Skill.setSkillConsumptionSynchronize(container, resource);
    }

    private static Action readAction(FriendlyByteBuf args) {
        if (args != null && args.isReadable()) {
            int ordinal = args.getUnsignedByte(args.readerIndex());
            return ordinal == Action.SUMMON.ordinal() ? Action.SUMMON : Action.AWAKENING;
        }
        return Action.AWAKENING;
    }

    private static float getFloat(CompoundTag tag, String key, float fallback) {
        return tag.contains(key) ? tag.getFloat(key) : fallback;
    }

    private static int getInt(CompoundTag tag, String key, int fallback) {
        return tag.contains(key) ? tag.getInt(key) : fallback;
    }

    private static double getDouble(CompoundTag tag, String key, double fallback) {
        return tag.contains(key) ? tag.getDouble(key) : fallback;
    }

    private static void syncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
    }

    @OnlyIn(Dist.CLIENT)
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }

    private enum Action {
        AWAKENING,
        SUMMON
    }
}