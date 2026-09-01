package com.rave.projectbabylonweapons.item.special;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class ArclightSwordItem extends SwordItem {
    public static final String EVERGATE_FORM_TAG = "ProjectBabylonEvergateForm";
    public static final String EVERGATE_EXPIRES_AT_TAG = "ProjectBabylonEvergateExpiresAt";
    public static final String EVERGATE_WARNING_TAG = "ProjectBabylonEvergateWarning";

    public static final int DURABILITY = 1658;
    public static final int ATTACK_DAMAGE_MOD = 3;
    public static final float ATTACK_SPEED_MOD = -3.0F;

    public ArclightSwordItem(Properties props) {
        super(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD, props.durability(DURABILITY));
    }

    public static boolean isEvergate(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean(EVERGATE_FORM_TAG);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(isEvergate(stack)
                ? "item.project_babylon_weapons.evergate"
                : this.getDescriptionId(stack));
    }

    public static void awaken(ItemStack stack, long expiresAt) {
        stack.getOrCreateTag().putBoolean(EVERGATE_FORM_TAG, true);
        stack.getOrCreateTag().putLong(EVERGATE_EXPIRES_AT_TAG, expiresAt);
        stack.getOrCreateTag().putBoolean(EVERGATE_WARNING_TAG, false);
    }

    public static long getFormExpiresAt(ItemStack stack) {
        return isEvergate(stack) ? stack.getOrCreateTag().getLong(EVERGATE_EXPIRES_AT_TAG) : 0L;
    }

    public static boolean hasExpirationWarning(ItemStack stack) {
        return isEvergate(stack) && stack.getOrCreateTag().getBoolean(EVERGATE_WARNING_TAG);
    }

    public static void markExpirationWarning(ItemStack stack) {
        if (isEvergate(stack)) {
            stack.getOrCreateTag().putBoolean(EVERGATE_WARNING_TAG, true);
        }
    }

    public static boolean reset(ItemStack stack) {
        if (!isEvergate(stack)) return false;
        stack.getTag().remove(EVERGATE_FORM_TAG);
        stack.getTag().remove(EVERGATE_EXPIRES_AT_TAG);
        stack.getTag().remove(EVERGATE_WARNING_TAG);
        if (stack.getTag().isEmpty()) stack.setTag(null);
        return true;
    }
}