package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.world.entity.effect.IceSpikeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public final class IceSpikeRenderer extends EntityRenderer<IceSpikeEntity> {
    public static final ResourceLocation SMALL_MODEL = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "entity/ice_spike_small");
    public static final ResourceLocation MEDIUM_MODEL = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "entity/ice_spike_medium");
    public static final ResourceLocation BIG_MODEL = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "entity/ice_spike_big");
    private static final ResourceLocation[] MODELS = {SMALL_MODEL, MEDIUM_MODEL, BIG_MODEL};
    private final BakedModel[] cachedModels = new BakedModel[3];
    private final Bounds[] cachedBounds = new Bounds[3];

    public IceSpikeRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0; }

    @Override
    public void render(IceSpikeEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        float extension = entity.getExtension(partialTick);
        if (extension <= 0) return;
        var minecraft = Minecraft.getInstance();
        int variant = entity.getVariant();
        BakedModel model = minecraft.getModelManager().getModel(MODELS[variant]);
        if (cachedModels[variant] != model) {
            cachedModels[variant] = model;
            cachedBounds[variant] = bounds(model);
        }
        Bounds bounds = cachedBounds[variant];
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(entity.getTiltX()));
        pose.mulPose(Axis.ZP.rotationDegrees(entity.getTiltZ()));
        float scale = entity.getSpikeScale();
        pose.scale(scale, scale, scale);
        pose.translate(-(bounds.minX + bounds.maxX) * .5F, -bounds.minY - (bounds.maxY - bounds.minY) * (1 - extension),
                -(bounds.minZ + bounds.maxZ) * .5F);
        for (var type : model.getRenderTypes(ItemStack.EMPTY, false)) {
            var consumer = ItemRenderer.getFoilBufferDirect(buffers, type, true, false);
            minecraft.getItemRenderer().renderModelLists(model, ItemStack.EMPTY, light, OverlayTexture.NO_OVERLAY, pose, consumer);
        }
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    private static Bounds bounds(BakedModel model) {
        Bounds bounds = new Bounds();
        RandomSource random = RandomSource.create(42);
        for (Direction direction : Direction.values()) {
            random.setSeed(42);
            include(bounds, model.getQuads(null, direction, random));
        }
        random.setSeed(42);
        include(bounds, model.getQuads(null, null, random));
        if (!Float.isFinite(bounds.minY)) {
            bounds.minX = bounds.minY = bounds.minZ = 0;
            bounds.maxX = bounds.maxY = bounds.maxZ = 1;
        }
        return bounds;
    }

    private static void include(Bounds bounds, List<BakedQuad> quads) {
        for (var quad : quads) {
            int[] vertices = quad.getVertices();
            int stride = vertices.length / 4;
            if (stride < 3) continue;
            for (int i = 0; i < 4; i++) {
                float x = Float.intBitsToFloat(vertices[i * stride]);
                float y = Float.intBitsToFloat(vertices[i * stride + 1]);
                float z = Float.intBitsToFloat(vertices[i * stride + 2]);
                bounds.minX = Math.min(bounds.minX, x); bounds.maxX = Math.max(bounds.maxX, x);
                bounds.minY = Math.min(bounds.minY, y); bounds.maxY = Math.max(bounds.maxY, y);
                bounds.minZ = Math.min(bounds.minZ, z); bounds.maxZ = Math.max(bounds.maxZ, z);
            }
        }
    }

    private static final class Bounds {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
    }

    @Override
    public ResourceLocation getTextureLocation(IceSpikeEntity entity) { return TextureAtlas.LOCATION_BLOCKS; }
}
