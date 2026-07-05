package com.rave.projectbabylonweapons.item.tool;

import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;

public class IronPickaxeItem extends PickaxeItem {

    public IronPickaxeItem(Properties props) {
        super(Tiers.IRON, (props).attributes(PickaxeItem.createAttributes(Tiers.IRON, 1, -2.8F)));
    }
}
