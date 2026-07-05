package com.rave.projectbabylonweapons;

import com.mojang.logging.LogUtils;
import com.rave.projectbabylonweapons.config.PBConfig;
import com.rave.projectbabylonweapons.gameasset.PBAnimations;
import com.rave.projectbabylonweapons.gameasset.PBSkills;
import com.rave.projectbabylonweapons.init.CreativeTabRegistry;
import com.rave.projectbabylonweapons.init.PBModBlocks;
import com.rave.projectbabylonweapons.init.PBModEntities;
import com.rave.projectbabylonweapons.init.PBModItems;
import com.rave.projectbabylonweapons.init.PBModParticles;
import com.rave.projectbabylonweapons.init.PBWSounds;
import com.rave.projectbabylonweapons.network.PBNetworkManager;
import com.rave.projectbabylonweapons.passive.data.WeaponPassivePatchManager;
import com.rave.projectbabylonweapons.world.capabilities.item.PBWeaponCapabilityPresets;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(ProjectBabylonWeapons.MODID)
public class ProjectBabylonWeapons {
    public static final String MODID = "project_babylon_weapons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ProjectBabylonWeapons(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, PBConfig.SPEC);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::addCreative);
        modBus.addListener(this::addPackFindersEvent);
        modBus.addListener(PBNetworkManager::register);
        modBus.addListener(PBAnimations::registerAnimations);

        CreativeTabRegistry.TABS.register(modBus);
        PBModBlocks.register(modBus);
        PBModItems.ITEMS.register(modBus);
        PBModParticles.PARTICLES.register(modBus);
        PBWSounds.register(modBus);
        PBModEntities.ENTITIES.register(modBus);
        PBSkills.REGISTRY.register(modBus);

        PBWeaponCapabilityPresets.register();

        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);

        // Skill event handlers registered via addListener instead of @EventBusSubscriber:
        // the bus's annotation scan reflects over the whole class hierarchy (checkSupertypes ->
        // getDeclaredMethods), and Epic Fight's Skill superclasses declare client-only methods
        // (GuiGraphics in signatures), which crashes class-loading on a dedicated server.
        NeoForge.EVENT_BUS.addListener(com.rave.projectbabylonweapons.skill.weapon_innate.GlacierSkill::onServerTick);
        NeoForge.EVENT_BUS.addListener(com.rave.projectbabylonweapons.skill.weapon_innate.TectonicSkill::onServerTick);
        NeoForge.EVENT_BUS.addListener(com.rave.projectbabylonweapons.skill.weapon_innate.UppercutSkill::onServerTick);
        NeoForge.EVENT_BUS.addListener(com.rave.projectbabylonweapons.skill.weapon_innate.RebirthSkill::onLivingAttack);
        NeoForge.EVENT_BUS.addListener(com.rave.projectbabylonweapons.skill.weapon_innate.RebirthSkill::onLivingHurt);
        NeoForge.EVENT_BUS.addListener(com.rave.projectbabylonweapons.skill.weapon_innate.RebirthSkill::onLivingTick);
        NeoForge.EVENT_BUS.addListener(com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowSkill::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowSkill::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowSkill::onPlayerDeath);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Project Babylon common setup started");
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(WeaponPassivePatchManager.INSTANCE);
    }

    public void addPackFindersEvent(AddPackFindersEvent event) {
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(MODID, "resourcepacks/projectbabylonpack"),
                PackType.CLIENT_RESOURCES,
                Component.translatable("pack.projectbabylonpack.title"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP);
    }
}
