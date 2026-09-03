package com.rave.projectbabylonweapons.summon.arclight.epicfight;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.init.PBModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.forgeevent.EntityPatchRegistryEvent;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Armatures;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ArclightSummonEpicFightRegistry {
    public static final AssetAccessor<Armature> ARMATURE = Armatures.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "entity/arclight_summoned_weapon"),
            Armature::new
    );

    private ArclightSummonEpicFightRegistry() {
    }

    @SubscribeEvent
    public static void registerPatch(EntityPatchRegistryEvent event) {
        event.getTypeEntry().put(PBModEntities.ARCLIGHT_SUMMONED_WEAPON.get(),
                entity -> ArclightSummonedWeaponPatch::new);
        Armatures.registerEntityTypeArmature(PBModEntities.ARCLIGHT_SUMMONED_WEAPON.get(), ARMATURE);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(PBModEntities.ARCLIGHT_SUMMONED_WEAPON.get(),
                net.minecraft.world.entity.Mob.createMobAttributes().build());
    }
}
