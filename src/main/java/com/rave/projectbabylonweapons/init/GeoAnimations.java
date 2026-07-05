package com.rave.projectbabylonweapons.init;

import software.bernie.geckolib.animatable.GeoItem;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

import com.rave.projectbabylonweapons.item.battlescythe.*;
import com.rave.projectbabylonweapons.item.battlehammer.*;
import com.rave.projectbabylonweapons.item.greatsword.*;

@EventBusSubscriber
public class GeoAnimations {
    @SubscribeEvent
    public static void animatedItems(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        String animation = "";
        ItemStack mainhandItem = player.getMainHandItem().copy();
        ItemStack offhandItem = player.getOffhandItem().copy();
        if (mainhandItem.getItem() instanceof GeoItem || offhandItem.getItem() instanceof GeoItem) {
            if (mainhandItem.getItem() instanceof IceBattleScytheItem animatable) {
                animation = mainhandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("geckoAnim");
                if (!animation.isEmpty()) {
                    CustomData.update(DataComponents.CUSTOM_DATA, player.getMainHandItem(), tag -> tag.putString("geckoAnim", ""));
                    if (player.level().isClientSide()) {
                        ((IceBattleScytheItem) player.getMainHandItem().getItem()).animationprocedure = animation;
                    }
                }
            }
            if (offhandItem.getItem() instanceof IceBattleScytheItem animatable) {
                animation = offhandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("geckoAnim");
                if (!animation.isEmpty()) {
                    CustomData.update(DataComponents.CUSTOM_DATA, player.getOffhandItem(), tag -> tag.putString("geckoAnim", ""));
                    if (player.level().isClientSide()) {
                        ((IceBattleScytheItem) player.getOffhandItem().getItem()).animationprocedure = animation;
                    }
                }
            }
            if (mainhandItem.getItem() instanceof NetheriteBattleScytheItem animatable) {
                animation = mainhandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("geckoAnim");
                if (!animation.isEmpty()) {
                    CustomData.update(DataComponents.CUSTOM_DATA, player.getMainHandItem(), tag -> tag.putString("geckoAnim", ""));
                    if (player.level().isClientSide()) {
                        ((NetheriteBattleScytheItem) player.getMainHandItem().getItem()).animationprocedure = animation;
                    }
                }
            }
            if (offhandItem.getItem() instanceof NetheriteBattleScytheItem animatable) {
                animation = offhandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("geckoAnim");
                if (!animation.isEmpty()) {
                    CustomData.update(DataComponents.CUSTOM_DATA, player.getOffhandItem(), tag -> tag.putString("geckoAnim", ""));
                    if (player.level().isClientSide()) {
                        ((NetheriteBattleScytheItem) player.getOffhandItem().getItem()).animationprocedure = animation;
                    }
                }
            }
            if (mainhandItem.getItem() instanceof IceGreatswordItem animatable) {
                animation = mainhandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("geckoAnim");
                if (!animation.isEmpty()) {
                    CustomData.update(DataComponents.CUSTOM_DATA, player.getMainHandItem(), tag -> tag.putString("geckoAnim", ""));
                    if (player.level().isClientSide()) {
                        ((IceGreatswordItem) player.getMainHandItem().getItem()).animationprocedure = animation;
                    }
                }
            }
            if (offhandItem.getItem() instanceof IceGreatswordItem animatable) {
                animation = offhandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("geckoAnim");
                if (!animation.isEmpty()) {
                    CustomData.update(DataComponents.CUSTOM_DATA, player.getOffhandItem(), tag -> tag.putString("geckoAnim", ""));
                    if (player.level().isClientSide()) {
                        ((IceGreatswordItem) player.getOffhandItem().getItem()).animationprocedure = animation;
                    }
                }
            }
            if (mainhandItem.getItem() instanceof IceBattleHammerItem animatable) {
                animation = mainhandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("geckoAnim");
                if (!animation.isEmpty()) {
                    CustomData.update(DataComponents.CUSTOM_DATA, player.getMainHandItem(), tag -> tag.putString("geckoAnim", ""));
                    if (player.level().isClientSide()) {
                        ((IceBattleHammerItem) player.getMainHandItem().getItem()).animationprocedure = animation;
                    }
                }
            }
            if (offhandItem.getItem() instanceof IceBattleHammerItem animatable) {
                animation = offhandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("geckoAnim");
                if (!animation.isEmpty()) {
                    CustomData.update(DataComponents.CUSTOM_DATA, player.getOffhandItem(), tag -> tag.putString("geckoAnim", ""));
                    if (player.level().isClientSide()) {
                        ((IceBattleHammerItem) player.getOffhandItem().getItem()).animationprocedure = animation;
                    }
                }
            }
        }
    }
}