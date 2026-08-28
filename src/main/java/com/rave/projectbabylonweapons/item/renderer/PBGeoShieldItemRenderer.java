package com.rave.projectbabylonweapons.item.renderer;

import com.rave.projectbabylonweapons.item.model.PBGeoShieldItemModel;
import com.rave.projectbabylonweapons.item.shield.PBGeoShieldItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
public class PBGeoShieldItemRenderer extends PBCullingGeoItemRenderer<PBGeoShieldItem> {
    public PBGeoShieldItemRenderer() {
        super(new PBGeoShieldItemModel());
    }

    @Override
    public RenderType getRenderType(PBGeoShieldItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}