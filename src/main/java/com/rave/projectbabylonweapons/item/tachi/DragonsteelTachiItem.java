package com.rave.projectbabylonweapons.item.tachi;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class DragonsteelTachiItem extends SwordItem {

    // Итог: урон 4.0, скорость 3.4, прочность 3460.
    public static final int DURABILITY = 3410;
    public static final int ATTACK_DAMAGE_MOD = 3;     // +3.0 → 1.0 базовое = 4.0
    public static final float ATTACK_SPEED_MOD = -3.0F; // 4.0 - 0.6 = 3.4

    public DragonsteelTachiItem(Properties props) {
        // ВАЖНО: без stacksTo(...). Только durability(...)
        super(Tiers.WOOD, (props.durability(DURABILITY)).attributes(SwordItem.createAttributes(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD)));
    }
}
