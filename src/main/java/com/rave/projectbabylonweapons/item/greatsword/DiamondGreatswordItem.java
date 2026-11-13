package com.rave.projectbabylonweapons.item.greatsword;

import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import com.rave.projectbabylonweapons.passive.diamond.DiamondFangPassive;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class DiamondGreatswordItem extends SwordItem {


    public static final int DURABILITY = 1758;
    public static final int ATTACK_DAMAGE_MOD = 3;
    public static final float ATTACK_SPEED_MOD = -3.0F;

    public DiamondGreatswordItem(Properties props) {

        super(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD, props.durability(DURABILITY));
    }
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        DiamondFangPassive.appendTooltip(tooltip);
    }
}
