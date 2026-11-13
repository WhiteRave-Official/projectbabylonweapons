package com.rave.projectbabylonweapons.item.battleaxe;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tiers;

public class DragonsteelBattleAxeItem extends AxeItem {


    public static final int DURABILITY = 3460;
    public static final int ATTACK_DAMAGE_MOD = 3;
    public static final float ATTACK_SPEED_MOD = -3.0F;

    public DragonsteelBattleAxeItem(Properties props) {

        super(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD, props.durability(DURABILITY));
    }
}
