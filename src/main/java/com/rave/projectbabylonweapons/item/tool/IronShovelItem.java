package com.rave.projectbabylonweapons.item.tool;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;

public class IronShovelItem extends ShovelItem {

    public IronShovelItem(Properties props) {
        super(Tiers.IRON, (props).attributes(ShovelItem.createAttributes(Tiers.IRON, 1.5F, -3.0F)));
    }
}
