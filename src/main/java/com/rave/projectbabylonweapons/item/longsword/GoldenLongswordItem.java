package com.rave.projectbabylonweapons.item.longsword;

import com.rave.projectbabylonweapons.passive.golden.GoldenMagicPassive;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Properties;

public class GoldenLongswordItem extends SwordItem {

    public static final int DURABILITY = 1450;
    public static final int ATTACK_DAMAGE_MOD = 3;
    public static final float ATTACK_SPEED_MOD = -3.0F;

    public GoldenLongswordItem(Properties props) {
        super(Tiers.WOOD, (props.durability(DURABILITY)).attributes(SwordItem.createAttributes(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD)));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        GoldenMagicPassive.appendTooltip(tooltip);
    }
}

