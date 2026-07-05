package com.rave.projectbabylonweapons.init;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.block.display.FrozenDebuffIceBlockDisplayItem;



import com.rave.projectbabylonweapons.item.claws.*;
import com.rave.projectbabylonweapons.item.longsword.*;
import com.rave.projectbabylonweapons.item.shortsword.*;
import com.rave.projectbabylonweapons.item.sickle.*;
import com.rave.projectbabylonweapons.item.greatsword.*;
import com.rave.projectbabylonweapons.item.battlescythe.*;
import com.rave.projectbabylonweapons.item.battleaxe.*;
import com.rave.projectbabylonweapons.item.battlehammer.*;
import com.rave.projectbabylonweapons.item.dagger.*;
import com.rave.projectbabylonweapons.item.material.PBToolTiers;
import com.rave.projectbabylonweapons.item.rapier.*;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.item.tachi.*;
import com.rave.projectbabylonweapons.item.spear.*;
import com.rave.projectbabylonweapons.item.staff.*;
import com.rave.projectbabylonweapons.item.tool.*;
import com.rave.projectbabylonweapons.item.wand.*;
import com.rave.projectbabylonweapons.item.shield.*;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PBModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ProjectBabylonWeapons.MODID);

    public static final DeferredHolder<Item, Item> FIRE_SPELL_PROJECTILE =
            ITEMS.register("fire_spell_projectile", () -> new Item(new Item.Properties()));

    //IRON
    public static final DeferredHolder<Item, Item> IRON_PICKAXE =
            ITEMS.register("iron_pickaxe", () -> new IronPickaxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_AXE =
            ITEMS.register("iron_axe", () -> new IronAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_SHOVEL =
            ITEMS.register("iron_shovel", () -> new IronShovelItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_HOE =
            ITEMS.register("iron_hoe", () -> new IronHoeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_BATTLEAXE =
            ITEMS.register("iron_battleaxe", () -> new IronBattleAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_BATTLEHAMMER =
            ITEMS.register("iron_battlehammer", () -> new IronBattleHammerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_BATTLESCYTHE =
            ITEMS.register("iron_battlescythe", () -> new IronBattleScytheItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_SPEAR =
            ITEMS.register("iron_spear", () -> new IronSpearItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_STAFF =
            ITEMS.register("iron_staff", () -> new IronStaffItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_SHORTSWORD =
            ITEMS.register("iron_shortsword", () -> new IronShortswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_LONGSWORD =
            ITEMS.register("iron_longsword", () -> new IronLongswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_GREATSWORD =
            ITEMS.register("iron_greatsword", () -> new IronGreatswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_DAGGER =
            ITEMS.register("iron_dagger", () -> new IronDaggerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_SICKLE =
            ITEMS.register("iron_sickle", () -> new IronSickleItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_CLAWS =
            ITEMS.register("iron_claws", () -> new IronClawsItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_TACHI =
            ITEMS.register("iron_tachi", () -> new IronTachiItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> IRON_BATTLE_WAND =
            ITEMS.register("iron_battle_wand", () -> new IronBattleWandItem());

    public static final DeferredHolder<Item, Item> IRON_SMALL_SHIELD =
            ITEMS.register("iron_small_shield", IronSmallShieldItem::new);

    public static final DeferredHolder<Item, Item> IRON_BASTION_SHIELD =
            ITEMS.register("iron_bastion_shield", IronBastionShieldItem::new);


    //GOLDEN
    public static final DeferredHolder<Item, Item> GOLDEN_PICKAXE =
            ITEMS.register("golden_pickaxe", () -> new GoldenPickaxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_AXE =
            ITEMS.register("golden_axe", () -> new GoldenAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_SHOVEL =
            ITEMS.register("golden_shovel", () -> new GoldenShovelItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_HOE =
            ITEMS.register("golden_hoe", () -> new GoldenHoeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_BATTLEAXE =
            ITEMS.register("golden_battleaxe", () -> new GoldenBattleAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_BATTLEHAMMER =
            ITEMS.register("golden_battlehammer", () -> new GoldenBattleHammerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_BATTLESCYTHE =
            ITEMS.register("golden_battlescythe", () -> new GoldenBattleScytheItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_SPEAR =
            ITEMS.register("golden_spear", () -> new GoldenSpearItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_STAFF =
            ITEMS.register("golden_staff", () -> new GoldenStaffItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_SHORTSWORD =
            ITEMS.register("golden_shortsword", () -> new GoldenShortswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_LONGSWORD =
            ITEMS.register("golden_longsword", () -> new GoldenLongswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_GREATSWORD =
            ITEMS.register("golden_greatsword", () -> new GoldenGreatswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_DAGGER =
            ITEMS.register("golden_dagger", () -> new GoldenDaggerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_SICKLE =
            ITEMS.register("golden_sickle", () -> new GoldenSickleItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_CLAWS =
            ITEMS.register("golden_claws", () -> new GoldenClawsItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_TACHI =
            ITEMS.register("golden_tachi", () -> new GoldenTachiItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GOLDEN_BATTLE_WAND =
            ITEMS.register("golden_battle_wand", () -> new GoldenBattleWandItem());

    public static final DeferredHolder<Item, Item> GOLDEN_SMALL_SHIELD =
            ITEMS.register("golden_small_shield", GoldenSmallShieldItem::new);

    public static final DeferredHolder<Item, Item> GOLDEN_BASTION_SHIELD =
            ITEMS.register("golden_bastion_shield", GoldenBastionShieldItem::new);


    //DIAMOND
    public static final DeferredHolder<Item, Item> DIAMOND_PICKAXE =
            ITEMS.register("diamond_pickaxe", () -> new DiamondPickaxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_AXE =
            ITEMS.register("diamond_axe", () -> new DiamondAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_SHOVEL =
            ITEMS.register("diamond_shovel", () -> new DiamondShovelItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_HOE =
            ITEMS.register("diamond_hoe", () -> new DiamondHoeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_BATTLEAXE =
            ITEMS.register("diamond_battleaxe", () -> new DiamondBattleAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_BATTLEHAMMER =
            ITEMS.register("diamond_battlehammer", () -> new DiamondBattleHammerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_BATTLESCYTHE =
            ITEMS.register("diamond_battlescythe", () -> new DiamondBattleScytheItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_SPEAR =
            ITEMS.register("diamond_spear", () -> new DiamondSpearItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_STAFF =
            ITEMS.register("diamond_staff", () -> new DiamondStaffItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_SHORTSWORD =
            ITEMS.register("diamond_shortsword", () -> new DiamondShortswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_LONGSWORD =
            ITEMS.register("diamond_longsword", () -> new DiamondLongswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_GREATSWORD =
            ITEMS.register("diamond_greatsword", () -> new DiamondGreatswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_DAGGER =
            ITEMS.register("diamond_dagger", () -> new DiamondDaggerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_SICKLE =
            ITEMS.register("diamond_sickle", () -> new DiamondSickleItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_CLAWS =
            ITEMS.register("diamond_claws", () -> new DiamondClawsItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_TACHI =
            ITEMS.register("diamond_tachi", () -> new DiamondTachiItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DIAMOND_BATTLE_WAND =
            ITEMS.register("diamond_battle_wand", () -> new DiamondBattleWandItem());

    public static final DeferredHolder<Item, Item> DIAMOND_BASTION_SHIELD =
            ITEMS.register("diamond_bastion_shield", DiamondBastionShieldItem::new);

    public static final DeferredHolder<Item, Item> DIAMOND_SMALL_SHIELD =
            ITEMS.register("diamond_small_shield", DiamondSmallShieldItem::new);



    //NETHERITE
    public static final DeferredHolder<Item, Item> NETHERITE_PICKAXE =
            ITEMS.register("netherite_pickaxe", () -> new NetheritePickaxeItem(new Item.Properties().fireResistant()));

    public static final DeferredHolder<Item, Item> NETHERITE_AXE =
            ITEMS.register("netherite_axe", () -> new NetheriteAxeItem(new Item.Properties().fireResistant()));

    public static final DeferredHolder<Item, Item> NETHERITE_SHOVEL =
            ITEMS.register("netherite_shovel", () -> new NetheriteShovelItem(new Item.Properties().fireResistant()));

    public static final DeferredHolder<Item, Item> NETHERITE_HOE =
            ITEMS.register("netherite_hoe", () -> new NetheriteHoeItem(new Item.Properties().fireResistant()));

    public static final DeferredHolder<Item, Item> NETHERITE_BATTLEAXE =
            ITEMS.register("netherite_battleaxe", () -> new NetheriteBattleAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_BATTLEHAMMER =
            ITEMS.register("netherite_battlehammer", () -> new NetheriteBattleHammerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_BATTLESCYTHE =
            ITEMS.register("netherite_battlescythe", () -> new NetheriteBattleScytheItem());

    public static final DeferredHolder<Item, Item> NETHERITE_SPEAR =
            ITEMS.register("netherite_spear", () -> new NetheriteSpearItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_STAFF =
            ITEMS.register("netherite_staff", () -> new NetheriteStaffItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_SHORTSWORD =
            ITEMS.register("netherite_shortsword", () -> new NetheriteShortswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_LONGSWORD =
            ITEMS.register("netherite_longsword", () -> new NetheriteLongswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_GREATSWORD =
            ITEMS.register("netherite_greatsword", () -> new NetheriteGreatswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_DAGGER =
            ITEMS.register("netherite_dagger", () -> new NetheriteDaggerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_SICKLE =
            ITEMS.register("netherite_sickle", () -> new NetheriteSickleItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_CLAWS =
            ITEMS.register("netherite_claws", () -> new NetheriteClawsItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_TACHI =
            ITEMS.register("netherite_tachi", () -> new NetheriteTachiItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> NETHERITE_BATTLE_WAND =
            ITEMS.register("netherite_battle_wand", () -> new NetheriteBattleWandItem());

    public static final DeferredHolder<Item, Item> NETHERITE_SMALL_SHIELD =
            ITEMS.register("netherite_small_shield", NetheriteSmallShieldItem::new);

    public static final DeferredHolder<Item, Item> NETHERITE_BASTION_SHIELD =
            ITEMS.register("netherite_bastion_shield", NetheriteBastionShieldItem::new);


    //ICE
    public static final DeferredHolder<Item, Item> ICE_PICKAXE =
            ITEMS.register("ice_pickaxe", () -> new IcePickaxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_AXE =
            ITEMS.register("ice_axe", () -> new IceAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_SHOVEL =
            ITEMS.register("ice_shovel", () -> new IceShovelItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_HOE =
            ITEMS.register("ice_hoe", () -> new IceHoeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_BATTLEAXE =
            ITEMS.register("ice_battleaxe", () -> new IceBattleAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_BATTLEHAMMER =
            ITEMS.register("ice_battlehammer", () -> new IceBattleHammerItem());

    public static final DeferredHolder<Item, Item> ICE_BATTLESCYTHE =
            ITEMS.register("ice_battlescythe", () -> new IceBattleScytheItem());

    public static final DeferredHolder<Item, Item> ICE_SPEAR =
            ITEMS.register("ice_spear", () -> new IceSpearItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_STAFF =
            ITEMS.register("ice_staff", () -> new IceStaffItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_SHORTSWORD =
            ITEMS.register("ice_shortsword", () -> new IceShortswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_LONGSWORD =
            ITEMS.register("ice_longsword", () -> new IceLongswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_GREATSWORD =
            ITEMS.register("ice_greatsword", () -> new IceGreatswordItem());

    public static final DeferredHolder<Item, Item> ICE_DAGGER =
            ITEMS.register("ice_dagger", () -> new IceDaggerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_SICKLE =
            ITEMS.register("ice_sickle", () -> new IceSickleItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_CLAWS =
            ITEMS.register("ice_claws", () -> new IceClawsItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_TACHI =
            ITEMS.register("ice_tachi", () -> new IceTachiItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_RAPIER =
            ITEMS.register("ice_rapier", () -> new IceRapierItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ICE_BATTLE_WAND =
            ITEMS.register("ice_battle_wand", () -> new IceBattleWandItem());

    public static final DeferredHolder<Item, Item> ICE_SMALL_SHIELD =
            ITEMS.register("ice_small_shield", IceSmallShieldItem::new);

    public static final DeferredHolder<Item, Item> ICE_BASTION_SHIELD =
            ITEMS.register("ice_bastion_shield", IceBastionShieldItem::new);

    //DRAGONSTEEL
    public static final DeferredHolder<Item, Item> DRAGONSTEEL_BATTLEAXE =
            ITEMS.register("dragonsteel_battleaxe", () -> new DragonsteelBattleAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_BATTLEHAMMER =
            ITEMS.register("dragonsteel_battlehammer", () -> new DragonsteelBattleHammerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_BATTLESCYTHE =
            ITEMS.register("dragonsteel_battlescythe", () -> new DragonsteelBattleScytheItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_SPEAR =
            ITEMS.register("dragonsteel_spear", () -> new DragonsteelSpearItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_STAFF =
            ITEMS.register("dragonsteel_staff", () -> new DragonsteelStaffItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_SHORTSWORD =
            ITEMS.register("dragonsteel_shortsword", () -> new DragonsteelShortswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_LONGSWORD =
            ITEMS.register("dragonsteel_longsword", () -> new DragonsteelLongswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_GREATSWORD =
            ITEMS.register("dragonsteel_greatsword", () -> new DragonsteelGreatswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_DAGGER =
            ITEMS.register("dragonsteel_dagger", () -> new DragonsteelDaggerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_SICKLE =
            ITEMS.register("dragonsteel_sickle", () -> new DragonsteelSickleItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_CLAWS =
            ITEMS.register("dragonsteel_claws", () -> new DragonsteelClawsItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_TACHI =
            ITEMS.register("dragonsteel_tachi", () -> new DragonsteelTachiItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_RAPIER =
            ITEMS.register("dragonsteel_rapier", () -> new DragonsteelRapierItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_BATTLE_WAND =
            ITEMS.register("dragonsteel_battle_wand", () -> new DragonsteelBattleWandItem());

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_SMALL_SHIELD =
            ITEMS.register("dragonsteel_small_shield", DragonsteelSmallShieldItem::new);

    public static final DeferredHolder<Item, Item> DRAGONSTEEL_BASTION_SHIELD =
            ITEMS.register("dragonsteel_bastion_shield", DragonsteelBastionShieldItem::new);


    //ETHEREAL
    public static final DeferredHolder<Item, Item> ETHEREAL_BATTLEAXE =
            ITEMS.register("ethereal_battleaxe", () -> new EtherealBattleAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_BATTLEHAMMER =
            ITEMS.register("ethereal_battlehammer", () -> new EtherealBattleHammerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_BATTLESCYTHE =
            ITEMS.register("ethereal_battlescythe", () -> new EtherealBattleScytheItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_SPEAR =
            ITEMS.register("ethereal_spear", () -> new EtherealSpearItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_STAFF =
            ITEMS.register("ethereal_staff", () -> new EtherealStaffItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_SHORTSWORD =
            ITEMS.register("ethereal_shortsword", () -> new EtherealShortswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_LONGSWORD =
            ITEMS.register("ethereal_longsword", () -> new EtherealLongswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_GREATSWORD =
            ITEMS.register("ethereal_greatsword", () -> new EtherealGreatswordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_DAGGER =
            ITEMS.register("ethereal_dagger", () -> new EtherealDaggerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_SICKLE =
            ITEMS.register("ethereal_sickle", () -> new EtherealSickleItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_CLAWS =
            ITEMS.register("ethereal_claws", () -> new EtherealClawsItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_TACHI =
            ITEMS.register("ethereal_tachi", () -> new EtherealTachiItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_RAPIER =
            ITEMS.register("ethereal_rapier", () -> new EtherealRapierItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ETHEREAL_BATTLE_WAND =
            ITEMS.register("ethereal_battle_wand", () -> new EtherealBattleWandItem());

    public static final DeferredHolder<Item, Item> ETHEREAL_SMALL_SHIELD =
            ITEMS.register("ethereal_small_shield", EtherealSmallShieldItem::new);

    public static final DeferredHolder<Item, Item> ETHEREAL_BASTION_SHIELD =
            ITEMS.register("ethereal_bastion_shield", EtherealBastionShieldItem::new);

    //DEMON
    public static final DeferredHolder<Item, Item> DEMON_BATTLEAXE =
            ITEMS.register("demon_battleaxe", () -> new DemonBattleAxeItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DEMON_CLAWS =
            ITEMS.register("demon_claws", () -> new DemonClawsItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DEMON_TACHI =
            ITEMS.register("demon_tachi", () -> new DemonTachiItem(new Item.Properties()));


    //SPECIAL
    public static final DeferredHolder<Item, Item> ARCLIGHT_SWORD =
            ITEMS.register("arclight_sword", () -> new ArclightSwordItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> FROZEN_DEBUFF_ICE_BLOCK =
            ITEMS.register("frozen_debuff_ice_block",
                    () -> new FrozenDebuffIceBlockDisplayItem(PBModBlocks.FROZEN_DEBUFF_ICE_BLOCK.get(), new Item.Properties()));

}


