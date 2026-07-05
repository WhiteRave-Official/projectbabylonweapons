package com.rave.projectbabylonweapons.item.tool;

import com.rave.projectbabylonweapons.passive.netherite.NetheriteBrimstonePassive;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class NetheritePickaxeItem extends PickaxeItem {

    public NetheritePickaxeItem(Properties props) {
        super(Tiers.NETHERITE, (props).attributes(PickaxeItem.createAttributes(Tiers.NETHERITE, 1, -2.8F)));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        NetheriteBrimstonePassive.appendTooltip(tooltip);
    }
}
