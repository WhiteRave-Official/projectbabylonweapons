package com.rave.projectbabylonweapons.item.tool;

import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import com.rave.projectbabylonweapons.passive.diamond.DiamondFangPassive;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tiers;

public class DiamondHoeItem extends HoeItem {

    public DiamondHoeItem(Properties props) {
        super(Tiers.DIAMOND, (props).attributes(HoeItem.createAttributes(Tiers.DIAMOND, -3, 0.0F)));
    }
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        DiamondFangPassive.appendTooltip(tooltip);
    }
}
