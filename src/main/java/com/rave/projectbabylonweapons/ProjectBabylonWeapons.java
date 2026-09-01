package com.rave.projectbabylonweapons;

import com.mojang.logging.LogUtils;
import com.rave.projectbabylonweapons.config.PBConfig;
import com.rave.projectbabylonweapons.gameasset.PBAnimationProperties;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.PathPackResources;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;


@Mod(ProjectBabylonWeapons.MODID)
public class ProjectBabylonWeapons {
    public static final String MODID = "project_babylon_weapons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ProjectBabylonWeapons() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PBConfig.SPEC);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::addCreative);
        modBus.addListener(this::addPackFindersEvent);
        modBus.addListener(PBSkills::buildSkillEvent);
        modBus.addListener(PBAnimations::registerAnimations);
        modBus.addListener(PBAnimationProperties::applyArclightProperties);

        CreativeTabRegistry.TABS.register(modBus);
        PBModBlocks.register(modBus);
        PBModItems.ITEMS.register(modBus);
        PBModParticles.PARTICLES.register(modBus);
        PBWSounds.register(modBus);
        PBModEntities.ENTITIES.register(modBus);

        forgeBus.addListener(this::addReloadListeners);
        forgeBus.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Project Babylon common setup started");

            PBNetworkManager.register();
            LOGGER.info("Network packets registered");

            LOGGER.info("Fear animations registered");
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(WeaponPassivePatchManager.INSTANCE);
    }

    public void addPackFindersEvent(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            Path resourcePath = ModList.get().getModFileById(ProjectBabylonWeapons.MODID).getFile().findResource("packs/projectbabylonpack");
            if (!Files.exists(resourcePath)) {
                LOGGER.warn("Builtin resource pack path is missing: {}", resourcePath);
                return;
            }

            PathPackResources pack = new PathPackResources(ModList.get().getModFileById(ProjectBabylonWeapons.MODID).getFile().getFileName() + ":" + resourcePath, false, resourcePath);
            Pack.ResourcesSupplier resourcesSupplier = (string) -> pack;
            Pack.Info info = Pack.readPackInfo("projectbabylonpack", resourcesSupplier);

            if (info != null) {
                event.addRepositorySource((source) ->
                        source.accept(Pack.create("projectbabylonpack", Component.translatable("pack.projectbabylonpack.title"), false, resourcesSupplier, info, PackType.CLIENT_RESOURCES, Pack.Position.TOP, false, PackSource.BUILT_IN)));
            }
        }
    }
}


