package com.rave.projectbabylonweapons.client;

import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.culling.PBWBakedItemModelCullingHandler;
import com.rave.projectbabylonweapons.client.culling.PBWGeoCubeOcclusionCullingHelper;
import com.rave.projectbabylonweapons.client.renderer.item.ArclightAwakeningItemRenderer;
import com.rave.projectbabylonweapons.item.renderer.PBWRigidItemMesh;
import com.rave.projectbabylonweapons.item.renderer.PBWItemTextureAtlas;
import com.rave.projectbabylonweapons.item.renderer.PBWItemTextureRenderTypes;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PBWClientRenderCacheReloadListener {
    private PBWClientRenderCacheReloadListener() { }

    @SubscribeEvent
    public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resources -> {
            PBWRigidItemMesh.clearCache();
            PBWItemTextureAtlas.clearCache();
            PBWItemTextureRenderTypes.clearCache();
            PBWGeoCubeOcclusionCullingHelper.clearCache();
            PBWBakedItemModelCullingHandler.clearCache();
            ArclightAwakeningItemRenderer.clearCache();
        });
    }
}
