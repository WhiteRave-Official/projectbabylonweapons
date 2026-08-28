package com.rave.projectbabylonweapons.item.renderer;

import com.rave.projectbabylonweapons.item.model.EtherealStaffItemModel;
import com.rave.projectbabylonweapons.item.staff.EtherealStaffItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
public class EtherealStaffItemRenderer extends PBCullingGeoItemRenderer<EtherealStaffItem> {
    public EtherealStaffItemRenderer() {
        super(new EtherealStaffItemModel());
    }

    @Override
    public RenderType getRenderType(EtherealStaffItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}
