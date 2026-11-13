package com.rave.projectbabylonweapons.item.claws;

import com.rave.projectbabylonmaterials.effect.Bleeding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class DemonClawsItem extends SwordItem {


    public static final int   DURABILITY = 12000;
    public static final int   ATTACK_DAMAGE_MOD = 3;
    public static final float ATTACK_SPEED_MOD = -3.0F;

    public DemonClawsItem(Properties props) {

        super(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD, props.durability(DURABILITY));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            Bleeding.tryApply(target, attacker, 0.05F, 20 * 20, 0);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}

