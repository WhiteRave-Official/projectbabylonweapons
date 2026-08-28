package com.rave.projectbabylonweapons.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rave.projectbabylonweapons.client.culling.PBWGeoCubeOcclusionCullingHelper;
import net.minecraft.world.item.Item;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

public class PBCullingGeoItemRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {
    public PBCullingGeoItemRenderer(GeoModel<T> model) {
        super(model);
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.isHidden()) {
            return;
        }

        for (GeoCube cube : bone.getCubes()) {
            poseStack.pushPose();
            RenderUtils.translateToPivotPoint(poseStack, cube);
            RenderUtils.rotateMatrixAroundCube(poseStack, cube);
            RenderUtils.translateAwayFromPivotPoint(poseStack, cube);
            Matrix3f normalisedPoseState = poseStack.last().normal();
            Matrix4f poseState = poseStack.last().pose();

            for (GeoQuad quad : cube.quads()) {
                if (quad == null) {
                    continue;
                }
                if (PBWGeoCubeOcclusionCullingHelper.isQuadOccludedBySiblingCube(cube, quad, bone.getCubes())) {
                    continue;
                }

                Vector3f normal = normalisedPoseState.transform(new Vector3f(quad.normal()));
                RenderUtils.fixInvertedFlatCube(cube, normal);
                this.createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            }

            poseStack.popPose();
        }
    }
}