package com.rave.projectbabylonweapons.client.renderer.item;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rave.projectbabylonweapons.client.ArclightAwakeningClientState;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import com.rave.projectbabylonweapons.skill.weapon_innate.EternalLightSkill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ArclightAwakeningItemRenderer extends RenderItemBase {
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final float GLOW_SCALE = 1.02F;
    private static final float MIN_VISIBLE_PROGRESS = 0.01F;
    private static final Map<BakedModel, ModelPivot> PIVOT_CACHE =
            Collections.synchronizedMap(new IdentityHashMap<>());

    public ArclightAwakeningItemRenderer(JsonElement jsonElement) {
        super(jsonElement);
    }

    @Override
    public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entityPatch, InteractionHand hand,
                                 OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack,
                                 int packedLight, float partialTicks) {
        super.renderItemInHand(stack, entityPatch, hand, poses, buffer, poseStack, packedLight, partialTicks);

        boolean evergate = ArclightSwordItem.isEvergate(stack);
        float progress = 0.0F;
        if (hand == InteractionHand.MAIN_HAND) {
            if (evergate && ArclightAwakeningClientState.isExpirationActive(entityPatch)) {
                long expiresAt = ArclightSwordItem.getFormExpiresAt(stack);
                float gameTime = entityPatch.getOriginal().level().getGameTime() + partialTicks;
                float warningDuration = Math.max(1, EternalLightSkill.getExpirationWarningDurationTicks());
                float linear = Mth.clamp(1.0F - (expiresAt - gameTime) / warningDuration, 0.0F, 1.0F);
                progress = linear * linear * (3.0F - 2.0F * linear);
            } else if (!evergate) {
                progress = ArclightAwakeningClientState.getProgress(entityPatch, partialTicks);
            }
        }
        if (progress <= MIN_VISIBLE_PROGRESS) {
            return;
        }

        ItemStack arclightGlow = stack.copy();
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(
                arclightGlow,
                entityPatch.getOriginal().level(),
                entityPatch.getOriginal(),
                entityPatch.getOriginal().getId() + ItemDisplayContext.THIRD_PERSON_RIGHT_HAND.ordinal()
        );
        if (model.isCustomRenderer()) {
            return;
        }

        poseStack.pushPose();
        MathUtils.mulStack(poseStack, getCorrectionMatrix(entityPatch, hand, poses));
        model = ForgeHooksClient.handleCameraTransforms(
                poseStack,
                model,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                false
        );

        ModelPivot pivot = getModelPivot(model);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        poseStack.translate(pivot.x, pivot.y, pivot.z);
        poseStack.scale(GLOW_SCALE, GLOW_SCALE, GLOW_SCALE);
        poseStack.translate(-pivot.x, -pivot.y, -pivot.z);

        float overlayAlpha = Mth.clamp(
                (progress - MIN_VISIBLE_PROGRESS) / (1.0F - MIN_VISIBLE_PROGRESS),
                0.0F,
                1.0F
        );

        for (BakedModel pass : model.getRenderPasses(arclightGlow, true)) {
            EntitySnapshot.renderModelLists(
                    pass, arclightGlow, FULL_BRIGHT, 0, 1.0F, poseStack,
                    buffer.getBuffer(EpicFightRenderTypes.itemAfterimageStencil()),
                    Mesh.DrawingFunction.POSITION_TEX
            );
        }
        if (buffer instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(EpicFightRenderTypes.itemAfterimageStencil());
        }
        for (BakedModel pass : model.getRenderPasses(arclightGlow, true)) {
            EntitySnapshot.renderModelLists(
                    pass, arclightGlow, FULL_BRIGHT, 0, overlayAlpha, poseStack,
                    buffer.getBuffer(EpicFightRenderTypes.itemAfterimageWhite()),
                    Mesh.DrawingFunction.POSITION_TEX_COLOR_LIGHTMAP
            );
        }
        if (buffer instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(EpicFightRenderTypes.itemAfterimageWhite());
        }
        poseStack.popPose();
    }

    private static ModelPivot getModelPivot(BakedModel model) {
        synchronized (PIVOT_CACHE) {
            return PIVOT_CACHE.computeIfAbsent(model, ArclightAwakeningItemRenderer::calculateModelPivot);
        }
    }

    private static ModelPivot calculateModelPivot(BakedModel model) {
        Bounds bounds = new Bounds();
        RandomSource random = RandomSource.create();

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            includeQuads(bounds, model.getQuads(null, direction, random));
        }
        random.setSeed(42L);
        includeQuads(bounds, model.getQuads(null, null, random));

        return bounds.hasVertices
                ? new ModelPivot(
                        (bounds.minX + bounds.maxX) * 0.5F,
                        (bounds.minY + bounds.maxY) * 0.5F,
                        (bounds.minZ + bounds.maxZ) * 0.5F
                )
                : new ModelPivot(0.5F, 0.5F, 0.5F);
    }

    private static void includeQuads(Bounds bounds, List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices();
            int stride = vertices.length / 4;
            if (stride < 3) {
                continue;
            }

            for (int vertex = 0; vertex < 4; vertex++) {
                int offset = vertex * stride;
                bounds.include(
                        Float.intBitsToFloat(vertices[offset]),
                        Float.intBitsToFloat(vertices[offset + 1]),
                        Float.intBitsToFloat(vertices[offset + 2])
                );
            }
        }
    }

    private record ModelPivot(float x, float y, float z) {
    }

    private static final class Bounds {
        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float minZ = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private float maxZ = Float.NEGATIVE_INFINITY;
        private boolean hasVertices;

        private void include(float x, float y, float z) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
            hasVertices = true;
        }
    }
}