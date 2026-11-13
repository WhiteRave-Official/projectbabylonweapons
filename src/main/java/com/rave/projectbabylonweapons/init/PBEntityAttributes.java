package com.rave.projectbabylonweapons.init;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.mob.BiterEntity;
import com.rave.projectbabylonweapons.world.entity.mob.ThornEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PBEntityAttributes {
    private PBEntityAttributes() { }
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent event) {
        event.put(PBModEntities.BITER.get(), BiterEntity.createAttributes().build());
        event.put(PBModEntities.THORN.get(), ThornEntity.createAttributes().build());
    }
}
