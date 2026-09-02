package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.gameasset.PBSkills;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.handler.WeaponVisualEffectHelper;
import com.rave.projectbabylonweapons.init.PBWSounds;
import com.rave.projectbabylonweapons.passive.special.ArclightFormPassiveHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.effect.EpicFightMobEffects;

import java.util.UUID;

public class ArclightAwakeningSkill extends WeaponInnateSkill {
    private static final UUID CONTACT_UUID = UUID.fromString("c28a0abe-5605-4ccc-a736-338aa1cc9e8f");
    private float awakeningBarrier = 200.0F;
    private int awakeningProtectionTicks = 280;
    public ArclightAwakeningSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
    }

    @Override
    public void setParams(CompoundTag parameters) {
        super.setParams(parameters);
        if (parameters.contains("awakening_barrier")) {
            this.awakeningBarrier = parameters.getFloat("awakening_barrier");
        }
        if (parameters.contains("awakening_protection_ticks")) {
            this.awakeningProtectionTicks = parameters.getInt("awakening_protection_ticks");
        }
    }
    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        container.getExecutor().getEventListener().addEventListener(
                EventType.ATTACK_PHASE_END_EVENT,
                CONTACT_UUID,
                event -> {
                    if (event.getAnimation() == PBAnimations.ARCLIGHT_AWAKENING
                            && event.getPhaseOrder() == 0) {
                        awakenAtContact(event.getPlayerPatch());
                    }
                });
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        return super.canExecute(container)
                && !ArclightSwordItem.isEvergate(container.getExecutor().getOriginal().getMainHandItem());
    }

    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        super.executeOnServer(container, args);
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
        container.activate();
    }

    @Override
    public void onRemoved(SkillContainer container) {
        if (!container.getExecutor().isLogicalClient()
                && !ArclightSwordItem.isEvergate(container.getExecutor().getOriginal().getMainHandItem())) {
            resetForm(container);
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
        WeaponVisualEffectHelper.burstArclightAwakening(player);
        ArclightSwordItem.awaken(arclight, player.level().getGameTime() + PBSkills.ARCLIGHT_AWAKENING.getMaxDuration());
        syncInventory(player);
        serverPatch.modifyLivingMotionByCurrentItem(false);

        // Replacing the innate skill removes/adds Epic Fight listeners. Queue it so the
        // current ATTACK_PHASE_END_EVENT iteration can finish without being mutated.
        player.server.tell(new net.minecraft.server.TickTask(player.server.getTickCount() + 1, () -> {
            ItemStack current = player.getMainHandItem();
            if (current.getItem() instanceof ArclightSwordItem && ArclightSwordItem.isEvergate(current)) {
                EpicFightCapabilities.getItemStackCapability(current).changeWeaponInnateSkill(serverPatch, current);
            }
        }));
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

    private static void syncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
    }

    @OnlyIn(Dist.CLIENT)
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }
}