package com.rave.projectbabylonweapons.init;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectBabylonWeapons.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CLAWS = TABS.register("weapons", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(PBModItems.ARCLIGHT_SWORD.get()))
                    .title(Component.translatable("creativetab.projectbabylonweapons.weapons"))
                    .displayItems((params, output) -> {

                        //IRON
                        output.accept(PBModItems.IRON_CLAWS.get());
                        output.accept(PBModItems.IRON_DAGGER.get());
                        output.accept(PBModItems.IRON_SICKLE.get());

                        output.accept(PBModItems.IRON_SHORTSWORD.get());
                        output.accept(PBModItems.IRON_LONGSWORD.get());
                        output.accept(PBModItems.IRON_GREATSWORD.get());
                        output.accept(PBModItems.IRON_BATTLEAXE.get());
                        output.accept(PBModItems.IRON_BATTLEHAMMER.get());
                        output.accept(PBModItems.IRON_SPEAR.get());
                        output.accept(PBModItems.IRON_BATTLESCYTHE.get());
                        output.accept(PBModItems.IRON_STAFF.get());
                        output.accept(PBModItems.IRON_TACHI.get());
                        output.accept(PBModItems.IRON_SHOVEL.get());
                        output.accept(PBModItems.IRON_AXE.get());
                        output.accept(PBModItems.IRON_PICKAXE.get());
                        output.accept(PBModItems.IRON_HOE.get());
                        output.accept(PBModItems.IRON_BATTLE_WAND.get());
                        output.accept(PBModItems.IRON_SMALL_SHIELD.get());
                        output.accept(PBModItems.IRON_BASTION_SHIELD.get());

                        //GOLDEN
                        output.accept(PBModItems.GOLDEN_CLAWS.get());
                        output.accept(PBModItems.GOLDEN_DAGGER.get());
                        output.accept(PBModItems.GOLDEN_SICKLE.get());

                        output.accept(PBModItems.GOLDEN_SHORTSWORD.get());
                        output.accept(PBModItems.GOLDEN_LONGSWORD.get());
                        output.accept(PBModItems.GOLDEN_GREATSWORD.get());
                        output.accept(PBModItems.GOLDEN_BATTLEAXE.get());
                        output.accept(PBModItems.GOLDEN_BATTLEHAMMER.get());
                        output.accept(PBModItems.GOLDEN_SPEAR.get());
                        output.accept(PBModItems.GOLDEN_BATTLESCYTHE.get());
                        output.accept(PBModItems.GOLDEN_STAFF.get());
                        output.accept(PBModItems.GOLDEN_TACHI.get());

                        output.accept(PBModItems.GOLDEN_SHOVEL.get());
                        output.accept(PBModItems.GOLDEN_AXE.get());
                        output.accept(PBModItems.GOLDEN_PICKAXE.get());
                        output.accept(PBModItems.GOLDEN_HOE.get());
                        output.accept(PBModItems.GOLDEN_BATTLE_WAND.get());
                        output.accept(PBModItems.GOLDEN_SMALL_SHIELD.get());
                        output.accept(PBModItems.GOLDEN_BASTION_SHIELD.get());

                        //DIAMOND
                        output.accept(PBModItems.DIAMOND_CLAWS.get());
                        output.accept(PBModItems.DIAMOND_DAGGER.get());
                        output.accept(PBModItems.DIAMOND_SICKLE.get());

                        output.accept(PBModItems.DIAMOND_SHORTSWORD.get());
                        output.accept(PBModItems.DIAMOND_LONGSWORD.get());
                        output.accept(PBModItems.DIAMOND_GREATSWORD.get());
                        output.accept(PBModItems.DIAMOND_BATTLEAXE.get());
                        output.accept(PBModItems.DIAMOND_BATTLEHAMMER.get());
                        output.accept(PBModItems.DIAMOND_SPEAR.get());
                        output.accept(PBModItems.DIAMOND_BATTLESCYTHE.get());
                        output.accept(PBModItems.DIAMOND_STAFF.get());
                        output.accept(PBModItems.DIAMOND_TACHI.get());

                        output.accept(PBModItems.DIAMOND_SHOVEL.get());
                        output.accept(PBModItems.DIAMOND_AXE.get());
                        output.accept(PBModItems.DIAMOND_PICKAXE.get());
                        output.accept(PBModItems.DIAMOND_HOE.get());
                        output.accept(PBModItems.DIAMOND_BATTLE_WAND.get());
                        output.accept(PBModItems.DIAMOND_BASTION_SHIELD.get());
                        output.accept(PBModItems.DIAMOND_SMALL_SHIELD.get());


                        //NETHERITE
                        output.accept(PBModItems.NETHERITE_CLAWS.get());
                        output.accept(PBModItems.NETHERITE_DAGGER.get());
                        output.accept(PBModItems.NETHERITE_SICKLE.get());

                        output.accept(PBModItems.NETHERITE_SHORTSWORD.get());
                        output.accept(PBModItems.NETHERITE_LONGSWORD.get());
                        output.accept(PBModItems.NETHERITE_GREATSWORD.get());
                        output.accept(PBModItems.NETHERITE_BATTLEAXE.get());
                        output.accept(PBModItems.NETHERITE_BATTLEHAMMER.get());
                        output.accept(PBModItems.NETHERITE_SPEAR.get());
                        output.accept(PBModItems.NETHERITE_BATTLESCYTHE.get());
                        output.accept(PBModItems.NETHERITE_STAFF.get());
                        output.accept(PBModItems.NETHERITE_TACHI.get());

                        output.accept(PBModItems.NETHERITE_SHOVEL.get());
                        output.accept(PBModItems.NETHERITE_AXE.get());
                        output.accept(PBModItems.NETHERITE_PICKAXE.get());
                        output.accept(PBModItems.NETHERITE_HOE.get());
                        output.accept(PBModItems.NETHERITE_BATTLE_WAND.get());
                        output.accept(PBModItems.NETHERITE_SMALL_SHIELD.get());
                        output.accept(PBModItems.NETHERITE_BASTION_SHIELD.get());

                        //ICE
                        output.accept(PBModItems.ICE_CLAWS.get());
                        output.accept(PBModItems.ICE_DAGGER.get());
                        output.accept(PBModItems.ICE_SICKLE.get());

                        output.accept(PBModItems.ICE_SHORTSWORD.get());
                        output.accept(PBModItems.ICE_LONGSWORD.get());
                        output.accept(PBModItems.ICE_GREATSWORD.get());
                        output.accept(PBModItems.ICE_BATTLEAXE.get());
                        output.accept(PBModItems.ICE_BATTLEHAMMER.get());
                        output.accept(PBModItems.ICE_SPEAR.get());
                        output.accept(PBModItems.ICE_BATTLESCYTHE.get());
                        output.accept(PBModItems.ICE_STAFF.get());
                        output.accept(PBModItems.ICE_TACHI.get());
                        output.accept(PBModItems.ICE_RAPIER.get());
                        output.accept(PBModItems.ICE_BATTLE_WAND.get());
                        output.accept(PBModItems.ICE_SMALL_SHIELD.get());
                        output.accept(PBModItems.ICE_BASTION_SHIELD.get());

                        output.accept(PBModItems.ICE_SHOVEL.get());
                        output.accept(PBModItems.ICE_AXE.get());
                        output.accept(PBModItems.ICE_PICKAXE.get());
                        output.accept(PBModItems.ICE_HOE.get());


                        //DRAGONSTEEL
                        output.accept(PBModItems.DRAGONSTEEL_CLAWS.get());
                        output.accept(PBModItems.DRAGONSTEEL_DAGGER.get());
                        output.accept(PBModItems.DRAGONSTEEL_SICKLE.get());

                        output.accept(PBModItems.DRAGONSTEEL_SHORTSWORD.get());
                        output.accept(PBModItems.DRAGONSTEEL_LONGSWORD.get());
                        output.accept(PBModItems.DRAGONSTEEL_GREATSWORD.get());
                        output.accept(PBModItems.DRAGONSTEEL_BATTLEAXE.get());
                        output.accept(PBModItems.DRAGONSTEEL_BATTLEHAMMER.get());
                        output.accept(PBModItems.DRAGONSTEEL_SPEAR.get());
                        output.accept(PBModItems.DRAGONSTEEL_BATTLESCYTHE.get());
                        output.accept(PBModItems.DRAGONSTEEL_STAFF.get());
                        output.accept(PBModItems.DRAGONSTEEL_TACHI.get());
                        output.accept(PBModItems.DRAGONSTEEL_RAPIER.get());
                        output.accept(PBModItems.DRAGONSTEEL_BATTLE_WAND.get());
                        output.accept(PBModItems.DRAGONSTEEL_SMALL_SHIELD.get());
                        output.accept(PBModItems.DRAGONSTEEL_BASTION_SHIELD.get());


                        //ETHEREAL
                        output.accept(PBModItems.ETHEREAL_CLAWS.get());
                        output.accept(PBModItems.ETHEREAL_DAGGER.get());
                        output.accept(PBModItems.ETHEREAL_SICKLE.get());

                        output.accept(PBModItems.ETHEREAL_SHORTSWORD.get());
                        output.accept(PBModItems.ETHEREAL_LONGSWORD.get());
                        output.accept(PBModItems.ETHEREAL_GREATSWORD.get());
                        output.accept(PBModItems.ETHEREAL_BATTLEAXE.get());
                        output.accept(PBModItems.ETHEREAL_BATTLEHAMMER.get());
                        output.accept(PBModItems.ETHEREAL_SPEAR.get());
                        output.accept(PBModItems.ETHEREAL_BATTLESCYTHE.get());
                        output.accept(PBModItems.ETHEREAL_STAFF.get());
                        output.accept(PBModItems.ETHEREAL_TACHI.get());
                        output.accept(PBModItems.ETHEREAL_RAPIER.get());
                        output.accept(PBModItems.ETHEREAL_BATTLE_WAND.get());
                        output.accept(PBModItems.ETHEREAL_SMALL_SHIELD.get());
                        output.accept(PBModItems.ETHEREAL_BASTION_SHIELD.get());


                        //DEMON
                        output.accept(PBModItems.DEMON_CLAWS.get());
                        output.accept(PBModItems.DEMON_BATTLEAXE.get());
                        output.accept(PBModItems.DEMON_TACHI.get());


                        //SPECIAL
                        output.accept(PBModItems.ARCLIGHT_SWORD.get());



                    })
                    .build()
    );

    public static void register (IEventBus eventBus){
        TABS.register(eventBus);
    }
}
