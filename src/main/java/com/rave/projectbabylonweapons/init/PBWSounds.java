package com.rave.projectbabylonweapons.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PBWSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(Registries.SOUND_EVENT, "project_babylon_weapons");

    public static final DeferredHolder<SoundEvent, SoundEvent> GOT_FROZEN = SOUND_EVENTS.register("got_frozen",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/got_frozen")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_BLAST = SOUND_EVENTS.register("fire_blast",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/fire_blast")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_STORM_START = SOUND_EVENTS.register("fire_storm_start",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "fire_storm_start")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_TORNADO = SOUND_EVENTS.register("fire_tornado",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "fire_tornado")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> RUMBLING_GROUND = SOUND_EVENTS.register("rumbling_ground",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/rumbling_ground")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> CHAIN_UP = SOUND_EVENTS.register("chain_up",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/chain_up")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> CHAIN_DOWN = SOUND_EVENTS.register("chain_down",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/chain_down")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> BLIZZARD = SOUND_EVENTS.register("blizzard",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("project_babylon_weapons", "sounds/blizzard")
        )
    );

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}

