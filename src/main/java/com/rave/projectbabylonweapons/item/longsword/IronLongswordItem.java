package com.rave.projectbabylonweapons.item.longsword;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class IronLongswordItem extends SwordItem {


    public static final int DURABILITY = 910;
    public static final int ATTACK_DAMAGE_MOD = 3;
    public static final float ATTACK_SPEED_MOD = -3.0F;

    public IronLongswordItem(Properties props) {

        super(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD, props.durability(DURABILITY));
    }
}
