package com.rave.projectbabylonweapons.tooltip;

import com.rave.projectbabylonweapons.passive.bastion.BastionCrushingBalance;
import com.rave.projectbabylonweapons.passive.bastion.BastionCurseBalance;
import com.rave.projectbabylonweapons.passive.bastion.BastionHeavensGiftBalance;
import com.rave.projectbabylonweapons.passive.bastion.BastionPassiveTooltips;
import com.rave.projectbabylonweapons.passive.bastion.BastionPermafrostBalance;
import com.rave.projectbabylonweapons.passive.bastion.BastionRuleAuraBalance;
import com.rave.projectbabylonweapons.passive.bastion.BastionWarSignalBalance;
import com.rave.projectbabylonweapons.passive.data.WeaponPassiveIds;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import com.rave.projectbabylonweapons.passive.diamond.DiamondFangBalance;
import com.rave.projectbabylonweapons.passive.diamond.DiamondFangPassive;
import com.rave.projectbabylonweapons.passive.ethereal.EtherealHolyBalance;
import com.rave.projectbabylonweapons.passive.ethereal.EtherealHolyPassive;
import com.rave.projectbabylonweapons.passive.golden.GoldenMagicBalance;
import com.rave.projectbabylonweapons.passive.golden.GoldenMagicPassive;
import com.rave.projectbabylonweapons.passive.ice.IceChillBalance;
import com.rave.projectbabylonweapons.passive.ice.IceChillPassive;
import com.rave.projectbabylonweapons.passive.netherite.NetheriteBrimstoneBalance;
import com.rave.projectbabylonweapons.passive.netherite.NetheriteBrimstonePassive;
import com.rave.projectbabylonweapons.passive.smallshield.DiamondSmallShieldBalance;
import com.rave.projectbabylonweapons.passive.smallshield.DiamondSmallShieldPassive;
import com.rave.projectbabylonweapons.passive.smallshield.DragonsteelSmallShieldBalance;
import com.rave.projectbabylonweapons.passive.smallshield.DragonsteelSmallShieldPassive;
import com.rave.projectbabylonweapons.passive.smallshield.EtherealSmallShieldBalance;
import com.rave.projectbabylonweapons.passive.smallshield.EtherealSmallShieldPassive;
import com.rave.projectbabylonweapons.passive.smallshield.GoldenSmallShieldBalance;
import com.rave.projectbabylonweapons.passive.smallshield.GoldenSmallShieldPassive;
import com.rave.projectbabylonweapons.passive.smallshield.IceSmallShieldBalance;
import com.rave.projectbabylonweapons.passive.smallshield.IceSmallShieldPassive;
import com.rave.projectbabylonweapons.passive.smallshield.NetheriteSmallShieldBalance;
import com.rave.projectbabylonweapons.passive.smallshield.NetheriteSmallShieldPassive;
import com.rave.projectbabylonweapons.passive.wand.DiamondRicochetBalance;
import com.rave.projectbabylonweapons.passive.wand.DiamondRicochetPassive;
import com.rave.projectbabylonweapons.passive.wand.DragonsteelDragonlordBalance;
import com.rave.projectbabylonweapons.passive.wand.DragonsteelDragonlordPassive;
import com.rave.projectbabylonweapons.passive.wand.EtherealSanctuaryBalance;
import com.rave.projectbabylonweapons.passive.wand.EtherealSanctuaryPassive;
import com.rave.projectbabylonweapons.passive.wand.GoldenBloodPactBalance;
import com.rave.projectbabylonweapons.passive.wand.GoldenBloodPactPassive;
import com.rave.projectbabylonweapons.passive.wand.IceFrostTouchBalance;
import com.rave.projectbabylonweapons.passive.wand.IceFrostTouchPassive;
import com.rave.projectbabylonweapons.passive.wand.NetheriteAshenBalance;
import com.rave.projectbabylonweapons.passive.wand.NetheriteAshenPassive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class WeaponPassiveTooltipResolver {
    private WeaponPassiveTooltipResolver() {
    }

    @Nullable
    public static WeaponPassiveTooltipData resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        if (DiamondRicochetBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.WAND_DIAMOND_RICOCHET, DiamondRicochetPassive.getTooltipData());
        }
        if (GoldenBloodPactBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.WAND_GOLDEN_BLOOD_PACT, GoldenBloodPactPassive.getTooltipData());
        }
        if (NetheriteAshenBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.WAND_NETHERITE_ASHEN, NetheriteAshenPassive.getTooltipData());
        }
        if (EtherealSanctuaryBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.WAND_ETHEREAL_SANCTUARY, EtherealSanctuaryPassive.getTooltipData());
        }
        if (IceFrostTouchBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.WAND_ICE_FROST_TOUCH, IceFrostTouchPassive.getTooltipData());
        }
        if (DragonsteelDragonlordBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.WAND_DRAGONSTEEL_DRAGONLORD, DragonsteelDragonlordPassive.getTooltipData());
        }
        if (DiamondFangBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.DIAMOND_FANG, DiamondFangPassive.getTooltipData());
        }
        if (IceChillBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.ICE_CHILL, IceChillPassive.getTooltipData());
        }
        if (GoldenMagicBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.GOLDEN_MAGIC, GoldenMagicPassive.getTooltipData());
        }
        if (EtherealHolyBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.ETHEREAL_HOLY, EtherealHolyPassive.getTooltipData());
        }
        if (NetheriteBrimstoneBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.NETHERITE_BRIMSTONE, NetheriteBrimstonePassive.getTooltipData());
        }
        if (DiamondSmallShieldBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.SMALL_SHIELD_DIAMOND_WOLF_GRIP, DiamondSmallShieldPassive.getTooltipData());
        }
        if (GoldenSmallShieldBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.SMALL_SHIELD_GOLDEN_WITHERING, GoldenSmallShieldPassive.getTooltipData());
        }
        if (IceSmallShieldBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.SMALL_SHIELD_ICE_PRISON, IceSmallShieldPassive.getTooltipData());
        }
        if (NetheriteSmallShieldBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.SMALL_SHIELD_NETHERITE_SULFUR_BRAND, NetheriteSmallShieldPassive.getTooltipData());
        }
        if (EtherealSmallShieldBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.SMALL_SHIELD_ETHEREAL_PURIFICATION, EtherealSmallShieldPassive.getTooltipData());
        }
        if (DragonsteelSmallShieldBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.SMALL_SHIELD_DRAGONSTEEL_RETRIBUTION, DragonsteelSmallShieldPassive.getTooltipData());
        }
        if (BastionCrushingBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.BASTION_DIAMOND_CRUSHING, BastionPassiveTooltips.diamond());
        }
        if (BastionCurseBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.BASTION_GOLDEN_CURSE, BastionPassiveTooltips.golden());
        }
        if (BastionPermafrostBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.BASTION_ICE_PERMAFROST, BastionPassiveTooltips.ice());
        }
        if (BastionWarSignalBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.BASTION_NETHERITE_WAR_SIGNAL, BastionPassiveTooltips.netherite());
        }
        if (BastionHeavensGiftBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.BASTION_ETHEREAL_HEAVENS_GIFT, BastionPassiveTooltips.ethereal());
        }
        if (BastionRuleAuraBalance.resolve(stack) != null) {
            return resolveTooltip(stack, WeaponPassiveIds.BASTION_DRAGONSTEEL_RULE_AURA, BastionPassiveTooltips.dragonsteel());
        }
        return null;
    }

    private static WeaponPassiveTooltipData resolveTooltip(ItemStack stack, ResourceLocation passiveId, WeaponPassiveTooltipData fallback) {
        WeaponPassiveTooltipData patchTooltip = WeaponPassivePatchManager.INSTANCE.resolveTooltipData(passiveId, stack);
        return patchTooltip != null ? patchTooltip : fallback;
    }
}
