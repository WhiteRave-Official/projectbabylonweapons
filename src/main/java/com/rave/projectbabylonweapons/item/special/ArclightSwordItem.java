package com.rave.projectbabylonweapons.item.special;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class ArclightSwordItem extends SwordItem {
    public static final String EVERGATE_FORM_TAG = "ProjectBabylonEvergateForm";

    public static final int DURABILITY = 1658;
    public static final int ATTACK_DAMAGE_MOD = 3;
    public static final float ATTACK_SPEED_MOD = -3.0F;

    public ArclightSwordItem(Properties props) {
        super(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD, props.durability(DURABILITY));
    }

    public static boolean isEvergate(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean(EVERGATE_FORM_TAG);
    }

    public static void awaken(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(EVERGATE_FORM_TAG, true);
    }

    public static boolean reset(ItemStack stack) {
        if (!isEvergate(stack)) return false;
        stack.getTag().remove(EVERGATE_FORM_TAG);
        if (stack.getTag().isEmpty()) stack.setTag(null);
        return true;
    }
}