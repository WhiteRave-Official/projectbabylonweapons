package com.rave.projectbabylonweapons.block.renderer;

import com.rave.projectbabylonweapons.block.display.FrozenDebuffIceBlockDisplayItem;
import com.rave.projectbabylonweapons.block.model.FrozenDebuffIceBlockDisplayModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

public class FrozenDebuffIceBlockDisplayItemRenderer extends GeoItemRenderer<FrozenDebuffIceBlockDisplayItem> {
    public FrozenDebuffIceBlockDisplayItemRenderer() {
        super(new FrozenDebuffIceBlockDisplayModel());
    }

    @Override
    public RenderType getRenderType(FrozenDebuffIceBlockDisplayItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
