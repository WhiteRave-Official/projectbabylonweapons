package com.rave.projectbabylonweapons.item.tool;

import com.rave.projectbabylonweapons.item.material.PBToolTiers;
import com.rave.projectbabylonweapons.passive.golden.GoldenMagicPassive;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GoldenPickaxeItem extends PickaxeItem {

    public GoldenPickaxeItem(Properties props) {
        super(PBToolTiers.GOLDEN_DURABLE, (props).attributes(PickaxeItem.createAttributes(PBToolTiers.GOLDEN_DURABLE, 1, -2.8F)));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        GoldenMagicPassive.appendTooltip(tooltip);
    }
}
