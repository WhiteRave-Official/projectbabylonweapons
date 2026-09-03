package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.client.model.EmptyEntityModel;
import com.rave.projectbabylonweapons.summon.arclight.epicfight.ArclightSummonedWeaponPatch;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponEntity;
import com.rave.projectbabylonweapons.world.entity.summon.ArclightSummonedWeaponState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.api.utils.math.MathUtils;
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
        BakedModel model = minecraft.getModelManager().getModel(
                entity.isSpear() ? ArclightSummonedWeaponRenderer.SPEAR_MODEL
                        : ArclightSummonedWeaponRenderer.MINI_MODEL);
        ItemStack renderStack = ItemStack.EMPTY;
        Armature armature = patch.getArmature();

        poseStack.pushPose();
        this.mulPoseStack(poseStack, armature, entity, patch, partialTick);
        this.setArmaturePose(patch, armature, partialTick);
        OpenMatrix4f[] poseMatrices = armature.getPoseMatrices();
        OpenMatrix4f weaponPose = poseMatrices[armature.searchJointByName("Weapon").getId()];
        MathUtils.mulStack(poseStack, weaponPose);
        // Baked item models point along +Y, while the animated weapon and collider point along -Z.
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        // Roaming weapons hover flat; attack animations roll the blade onto its cutting edge.
        if (entity.getCombatState() == ArclightSummonedWeaponState.ATTACK) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        if (entity.markRenderDebugTick()) {
            var animationPlayer = patch.getAnimator().getPlayerFor(null);
            ProjectBabylonWeapons.LOGGER.info("[ArclightWeaponDebug] render side=client id={} type={} state={} attack={} position={} animation={} elapsed={} weaponPose={}",
                    entity.getId(), entity.getWeaponType(), entity.getCombatState(), entity.getAttackType(),
                    entity.position(),
                    animationPlayer == null ? "none" : animationPlayer.getRealAnimation(),
                    animationPlayer == null ? -1.0F : animationPlayer.getElapsedTime(),
                    weaponPose);
        }

        float formation = Mth.clamp((entity.tickCount + partialTick) / FORMATION_TICKS, 0.0F, 1.0F);
        float modelScale = entity.isSpear() ? SPEAR_MODEL_SCALE : MINI_MODEL_SCALE;
        float formationScale = modelScale * Mth.lerp(formation, 0.35F, 1.0F);
        renderBaseModel(minecraft.getItemRenderer(), model, renderStack, poseStack, bufferSource, formationScale);

        if (formation < 1.0F) {
            renderFormationGlow(model, renderStack, poseStack, bufferSource,
                    formationScale * GLOW_SCALE, Mth.sin(formation * Mth.PI));
        }
        poseStack.popPose();
    }

    private static void renderBaseModel(ItemRenderer itemRenderer, BakedModel model, ItemStack stack,
                                        PoseStack poseStack, MultiBufferSource bufferSource, float scale) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        for (RenderType renderType : model.getRenderTypes(stack, false)) {
            VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(bufferSource, renderType, true, false);
            itemRenderer.renderModelLists(model, stack, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, consumer);
        }
        poseStack.popPose();
    }

    private static void renderFormationGlow(BakedModel model, ItemStack stack, PoseStack poseStack,
                                             MultiBufferSource bufferSource, float scale, float alpha) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        for (BakedModel pass : model.getRenderPasses(stack, true)) {
            EntitySnapshot.renderModelLists(pass, stack, FULL_BRIGHT, 0, 1.0F, poseStack,
                    bufferSource.getBuffer(EpicFightRenderTypes.itemAfterimageStencil()),
                    Mesh.DrawingFunction.POSITION_TEX);
        }
        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(EpicFightRenderTypes.itemAfterimageStencil());
        }
        for (BakedModel pass : model.getRenderPasses(stack, true)) {
            EntitySnapshot.renderModelLists(pass, stack, FULL_BRIGHT, 0, alpha, poseStack,
                    bufferSource.getBuffer(EpicFightRenderTypes.itemAfterimageWhite()),
                    Mesh.DrawingFunction.POSITION_TEX_COLOR_LIGHTMAP);
        }
        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(EpicFightRenderTypes.itemAfterimageWhite());
        }
        poseStack.popPose();
    }

    @Override
    public AssetAccessor<HumanoidMesh> getDefaultMesh() {
        // The baked weapon is rendered manually; this satisfies the patched renderer contract only.
        return Meshes.BIPED;
    }
}
