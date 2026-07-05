package com.rave.projectbabylonweapons.item.tool;

import com.rave.projectbabylonweapons.passive.netherite.NetheriteBrimstonePassive;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class NetheriteAxeItem extends AxeItem {

    public NetheriteAxeItem(Properties props) {
        super(Tiers.NETHERITE, (props).attributes(AxeItem.createAttributes(Tiers.NETHERITE, 5.0F, -3.0F)));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        NetheriteBrimstonePassive.appendTooltip(tooltip);
    }
}
