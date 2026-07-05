package com.rave.projectbabylonweapons.item.tool;

import com.rave.projectbabylonweapons.item.material.PBToolTiers;
import com.rave.projectbabylonweapons.passive.ice.IceChillPassive;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class IceAxeItem extends AxeItem {

    public IceAxeItem(Properties props) {
        super(PBToolTiers.ICE, (props).attributes(AxeItem.createAttributes(PBToolTiers.ICE, 5.0F, -3.0F)));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        IceChillPassive.appendTooltip(tooltip);
    }
}


