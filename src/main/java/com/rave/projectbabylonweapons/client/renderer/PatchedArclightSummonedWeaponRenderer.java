package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.model.ArclightSummonedWeaponMeshes;
import com.rave.projectbabylonweapons.client.model.EmptyEntityModel;
import com.rave.projectbabylonweapons.summon.arclight.epicfight.ArclightSummonedWeaponPatch;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;

public class PatchedArclightSummonedWeaponRenderer extends PatchedLivingEntityRenderer<
        ArclightSummonedWeaponEntity,
        ArclightSummonedWeaponPatch,
        EmptyEntityModel<ArclightSummonedWeaponEntity>,
        ArclightSummonedWeaponRenderer,
        HumanoidMesh> {
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final float MINI_MODEL_SCALE = 0.8F;
    private static final float SPEAR_MODEL_SCALE = 1.15F;
    private static final float FORMATION_TICKS = 12.0F;
    private static final float GLOW_SCALE = 1.08F;

    public PatchedArclightSummonedWeaponRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
        super(context, entityType);
    }

    @Override
    public void render(ArclightSummonedWeaponEntity entity, ArclightSummonedWeaponPatch patch,
                       ArclightSummonedWeaponRenderer renderer, MultiBufferSource bufferSource,
                       PoseStack poseStack, int packedLight, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel sourceModel = minecraft.getModelManager().getModel(
                entity.isSpear() ? ArclightSummonedWeaponRenderer.SPEAR_MODEL
                        : ArclightSummonedWeaponRenderer.MINI_MODEL);
        SkinnedMesh mesh = entity.isSpear()
                ? ArclightSummonedWeaponMeshes.spear(sourceModel)
                : ArclightSummonedWeaponMeshes.sword(sourceModel);
        Armature armature = patch.getArmature();

        poseStack.pushPose();
        this.mulPoseStack(poseStack, armature, entity, patch, partialTick);
        this.setArmaturePose(patch, armature, partialTick);
        OpenMatrix4f[] poseMatrices = armature.getPoseMatrices();

        if (entity.markRenderDebugTick()) {
            var animationPlayer = patch.getAnimator().getPlayerFor(null);
            ProjectBabylonWeapons.LOGGER.info("[ArclightWeaponDebug] render side=client id={} type={} state={} attack={} position={} animation={} elapsed={} weaponPose={}",
                    entity.getId(), entity.getWeaponType(), entity.getCombatState(), entity.getAttackType(),
                    entity.position(),
                    animationPlayer == null ? "none" : animationPlayer.getRealAnimation(),
                    animationPlayer == null ? -1.0F : animationPlayer.getElapsedTime(),
                    poseMatrices[armature.searchJointByName("Weapon").getId()]);
        }

        float formation = Mth.clamp((entity.tickCount + partialTick) / FORMATION_TICKS, 0.0F, 1.0F);
        float modelScale = entity.isSpear() ? SPEAR_MODEL_SCALE : MINI_MODEL_SCALE;
        float formationScale = modelScale * Mth.lerp(formation, 0.35F, 1.0F);

        poseStack.pushPose();
        poseStack.scale(formationScale, formationScale, formationScale);
        renderBaseMesh(mesh, armature, poseMatrices, poseStack, bufferSource);
        poseStack.popPose();

        if (formation < 1.0F) {
            renderFormationGlow(mesh, armature, poseMatrices, poseStack, bufferSource,
                    formationScale * GLOW_SCALE, Mth.sin(formation * Mth.PI));
        }
        poseStack.popPose();
    }

    private static void renderBaseMesh(SkinnedMesh mesh, Armature armature, OpenMatrix4f[] poses,
                                       PoseStack poseStack, MultiBufferSource bufferSource) {
        mesh.drawPosed(poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)),
                Mesh.DrawingFunction.NEW_ENTITY, FULL_BRIGHT,
                1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);
    }

    private static void renderFormationGlow(SkinnedMesh mesh, Armature armature, OpenMatrix4f[] poses,
                                             PoseStack poseStack, MultiBufferSource bufferSource,
                                             float scale, float alpha) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        mesh.drawPosed(poseStack, bufferSource.getBuffer(EpicFightRenderTypes.itemAfterimageStencil()),
                Mesh.DrawingFunction.POSITION_TEX, FULL_BRIGHT,
                1.0F, 1.0F, 1.0F, 1.0F, 0, armature, poses);
        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(EpicFightRenderTypes.itemAfterimageStencil());
        }
        mesh.drawPosed(poseStack, bufferSource.getBuffer(EpicFightRenderTypes.itemAfterimageWhite()),
                Mesh.DrawingFunction.POSITION_TEX_COLOR_LIGHTMAP, FULL_BRIGHT,
                1.0F, 1.0F, 1.0F, alpha, 0, armature, poses);
        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(EpicFightRenderTypes.itemAfterimageWhite());
        }
        poseStack.popPose();
    }

    @Override
    public AssetAccessor<HumanoidMesh> getDefaultMesh() {
        // Rendering uses the dedicated rigid weapon mesh above.
        return Meshes.BIPED;
    }
}
