package com.rave.projectbabylonweapons.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PBCullingGeoItemRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {
    public PBCullingGeoItemRenderer(GeoModel<T> model) {
        super(model);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses, MultiBufferSource source,
                             int light, int overlay) {
        MultiBufferSource target = source;
        if (context != ItemDisplayContext.GUI && getRenderLayers().isEmpty()) {
            @SuppressWarnings("unchecked")
            T item = (T) stack.getItem();
            var texture = getTextureLocation(item);
            if (PBWItemTextureRenderTypes.get(texture) == net.minecraft.client.renderer.RenderType.entityCutoutNoCull(texture)
                    && PBWItemTextureAtlas.supports(getGeoModel().getBakedModel(getGeoModel().getModelResource(item, this)))) {
                var region = PBWItemTextureAtlas.resolve(texture);
                if (!region.texture().equals(texture)) target = new PBWItemAtlasBufferSource(source, texture, region);
            }
        }
        super.renderByItem(stack, context, poses, target, light, overlay);
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.isHidden()) {
            return;
        }

        PBWRigidItemMesh.get(bone).render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}