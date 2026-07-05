package com.rave.projectbabylonweapons.item.tool;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tiers;

public class IronHoeItem extends HoeItem {

    public IronHoeItem(Properties props) {
        super(Tiers.IRON, (props).attributes(HoeItem.createAttributes(Tiers.IRON, -2, -1.0F)));
    }
}
