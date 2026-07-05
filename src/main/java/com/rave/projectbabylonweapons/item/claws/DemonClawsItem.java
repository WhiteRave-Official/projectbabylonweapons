package com.rave.projectbabylonweapons.item.claws;

import com.rave.projectbabylonmaterials.init.PBMEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class DemonClawsItem extends SwordItem {

    // РС‚РѕРі: СѓСЂРѕРЅ 4.0, СЃРєРѕСЂРѕСЃС‚СЊ 3.4, РїСЂРѕС‡РЅРѕСЃС‚СЊ 3460.
    public static final int   DURABILITY = 12000;
    public static final int   ATTACK_DAMAGE_MOD = 3;     // +3.0 в†’ 1.0 Р±Р°Р·РѕРІРѕРµ = 4.0
    public static final float ATTACK_SPEED_MOD = -3.0F; // 4.0 - 0.6 = 3.4

    public DemonClawsItem(Properties props) {
        // Р’РђР–РќРћ: Р±РµР· stacksTo(...). РўРѕР»СЊРєРѕ durability(...)
        super(Tiers.WOOD, props.durability(DURABILITY).attributes(SwordItem.createAttributes(Tiers.WOOD, ATTACK_DAMAGE_MOD, ATTACK_SPEED_MOD)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            if (player.getRandom().nextFloat() < 0.05F) { // 5%
                target.addEffect(new MobEffectInstance(PBMEffects.BLEED_DEBUFF, 20 * 20, 0));
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}

