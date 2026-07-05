package com.rave.projectbabylonweapons.handler;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.HashSet;
import java.util.Set;

/**
 * TEMPORARY DIAGNOSTIC — logs the CONTEXTUAL Epic Fight weapon-category of the shield in each hand,
 * as the pb_longsword styleProvider sees it (playerPatch.getHoldingItemCapability(hand)). Remove later.
 */
@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ShieldDebugHandler {
    private static final Set<String> LOGGED = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        boolean mainShield = player.getMainHandItem().getItem() instanceof ShieldItem;
        boolean offShield = player.getOffhandItem().getItem() instanceof ShieldItem;
        if (!mainShield && !offShield) return;

        PlayerPatch<?> patch = EpicFightCapabilities.getPlayerPatch(player);
        if (patch == null) return;

        ItemStack shieldStack = offShield ? player.getOffhandItem() : player.getMainHandItem();
        String key = shieldStack.getItem().getClass().getSimpleName() + (offShield ? ":OFF" : ":MAIN");
        if (!LOGGED.add(key)) return;

        var mainCat = patch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory();
        var offCat = patch.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory();
        ProjectBabylonWeapons.LOGGER.info("[SHIELD-DEBUG2] {} | contextual MAIN_HAND category={} | OFF_HAND category={}",
                key, String.valueOf(mainCat), String.valueOf(offCat));
    }
}
