package com.rave.projectbabylonweapons.item.tool;

import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import com.rave.projectbabylonweapons.passive.diamond.DiamondFangPassive;

import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;

public class DiamondPickaxeItem extends PickaxeItem {

    public DiamondPickaxeItem(Properties props) {
        super(Tiers.DIAMOND, (props).attributes(PickaxeItem.createAttributes(Tiers.DIAMOND, 1, -2.8F)));
    }
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        DiamondFangPassive.appendTooltip(tooltip);
    }
}
