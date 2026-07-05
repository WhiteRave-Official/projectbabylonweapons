package com.rave.projectbabylonweapons.item.tool;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tiers;

public class IronAxeItem extends AxeItem {

    public IronAxeItem(Properties props) {
        super(Tiers.IRON, (props).attributes(AxeItem.createAttributes(Tiers.IRON, 6.0F, -3.1F)));
    }
}
