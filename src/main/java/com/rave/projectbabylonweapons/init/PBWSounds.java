package com.rave.projectbabylonweapons.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PBWSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "project_babylon_weapons");

    public static final RegistryObject<SoundEvent> GOT_FROZEN = SOUND_EVENTS.register("got_frozen",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/got_frozen")
        )
    );

    public static final RegistryObject<SoundEvent> FIRE_BLAST = SOUND_EVENTS.register("fire_blast",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/fire_blast")
        )
    );

    public static final RegistryObject<SoundEvent> FIRE_STORM_START = SOUND_EVENTS.register("fire_storm_start",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "fire_storm_start")
        )
    );

    public static final RegistryObject<SoundEvent> FIRE_TORNADO = SOUND_EVENTS.register("fire_tornado",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "fire_tornado")
        )
    );

    public static final RegistryObject<SoundEvent> RUMBLING_GROUND = SOUND_EVENTS.register("rumbling_ground",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/rumbling_ground")
        )
    );

    public static final RegistryObject<SoundEvent> CHAIN_UP = SOUND_EVENTS.register("chain_up",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/chain_up")
        )
    );

    public static final RegistryObject<SoundEvent> CHAIN_DOWN = SOUND_EVENTS.register("chain_down",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/chain_down")
        )
    );

    public static final RegistryObject<SoundEvent> BLIZZARD = SOUND_EVENTS.register("blizzard",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/blizzard")
        )
    );

    public static final RegistryObject<SoundEvent> ARCLIGHT_AWAKENING = SOUND_EVENTS.register("arclight_awakening",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "arclight_awakening")
        )
    );

    public static final RegistryObject<SoundEvent> ARCLIGHT_SWORD_SHOOT = SOUND_EVENTS.register("arclight_sword_shoot",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "arclight_sword_shoot")
        )
    );

    public static final RegistryObject<SoundEvent> ARCLIGHT_SPEAR_SHOOT = SOUND_EVENTS.register("arclight_spear_shoot",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "arclight_spear_shoot")
        )
    );

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}

