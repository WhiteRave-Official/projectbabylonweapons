package com.rave.projectbabylonweapons.skill.weapon_innate;

import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.UUID;

public class ArclightAwakeningSkill extends WeaponInnateSkill {
    private static final UUID CONTACT_UUID = UUID.fromString("c28a0abe-5605-4ccc-a736-338aa1cc9e8f");
    public ArclightAwakeningSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
        super(builder);
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
        container.getExecutor().playAnimationSynchronized(PBAnimations.ARCLIGHT_AWAKENING, 0.0F);
    }

    @Override
    public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
        super.executeOnClient(container, args);
        container.activate();
    }

    @Override
    public void onRemoved(SkillContainer container) {
        if (!container.getExecutor().isLogicalClient()) {
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

        ArclightSwordItem.awaken(arclight);
        syncInventory(player);
        serverPatch.modifyLivingMotionByCurrentItem(false);
    }

    private static void resetForm(SkillContainer container) {
        if (!(container.getExecutor() instanceof ServerPlayerPatch serverPatch)
                || !(container.getExecutor().getOriginal() instanceof ServerPlayer player)) return;

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