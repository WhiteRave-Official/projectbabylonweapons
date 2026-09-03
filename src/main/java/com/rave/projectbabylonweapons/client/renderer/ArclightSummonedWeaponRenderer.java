package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.model.EmptyEntityModel;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class ArclightSummonedWeaponRenderer extends MobRenderer<ArclightSummonedWeaponEntity,
        EmptyEntityModel<ArclightSummonedWeaponEntity>> {
    public static final ResourceLocation MINI_MODEL =
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "item/arclight_mini_sword");
    public static final ResourceLocation SPEAR_MODEL =
            ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "item/arclight_spear");

    public ArclightSummonedWeaponRenderer(EntityRendererProvider.Context context) {
        super(context, new EmptyEntityModel<>(), 0.0F);
    }

    @Override
    public void render(ArclightSummonedWeaponEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = 0.0F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ArclightSummonedWeaponEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}