package com.rave.projectbabylonweapons.client.culling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PBWBakedItemModelCullingHandler {
    private static final Map<BakedModel, BakedModel> RESOLVED_MODEL_CACHE = Collections.synchronizedMap(new IdentityHashMap<>());

    private PBWBakedItemModelCullingHandler() {
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll(PBWBakedItemModelCullingHandler::wrapProjectBabylonItemModel);
    }

    private static BakedModel wrapProjectBabylonItemModel(ResourceLocation id, BakedModel model) {
        if (model instanceof CulledBakedModel || !ProjectBabylonWeapons.MODID.equals(id.getNamespace()) || !isLikelyItemModel(id)) {
            return model;
        }
        return new CulledBakedModel(id, model);
    }

    private static boolean isLikelyItemModel(ResourceLocation id) {
        String path = id.getPath();
        return path.startsWith("item/") || !path.contains("/");
    }

    private static final class CulledBakedModel extends BakedModelWrapper<BakedModel> {
        private final Map<List<BakedQuad>, List<BakedQuad>> cache = Collections.synchronizedMap(new IdentityHashMap<>());
        private final ResourceLocation id;
        private final ItemOverrides wrappedOverrides;
        private volatile IdentityHashMap<BakedQuad, Boolean> simpleCulledQuads;
        private volatile IdentityHashMap<BakedQuad, Boolean> extendedCulledQuads;

        private CulledBakedModel(ResourceLocation id, BakedModel originalModel) {
            super(originalModel);
            this.id = id;
            this.wrappedOverrides = new CullingItemOverrides(id, originalModel.getOverrides());
        }

        @Override
        public ItemOverrides getOverrides() {
            return this.wrappedOverrides;
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
            List<BakedModel> passes = super.getRenderPasses(itemStack, fabulous);
            if (passes.isEmpty()) {
                return passes;
            }

            List<BakedModel> wrappedPasses = new ArrayList<>(passes.size());
            boolean changed = false;
            for (BakedModel pass : passes) {
                BakedModel wrapped = wrapResolvedModel(this.id, pass);
                wrappedPasses.add(wrapped);
                changed |= wrapped != pass;
            }
            return changed ? List.copyOf(wrappedPasses) : passes;
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
            BakedModel transformed = super.applyTransform(transformType, poseStack, applyLeftHandTransform);
            return wrapResolvedModel(this.id, transformed);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            List<BakedQuad> quads = super.getQuads(state, side, rand);
            return cull(quads, getSimpleCulledQuads(state));
        }

        @NotNull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
            List<BakedQuad> quads = super.getQuads(state, side, rand, extraData, renderType);
            return cull(quads, getExtendedCulledQuads(state, extraData, renderType));
        }

        private IdentityHashMap<BakedQuad, Boolean> getSimpleCulledQuads(@Nullable BlockState state) {
            IdentityHashMap<BakedQuad, Boolean> culled = this.simpleCulledQuads;
            if (culled != null) {
                return culled;
            }
            synchronized (this) {
                if (this.simpleCulledQuads == null) {
                    List<BakedQuad> allQuads = new ArrayList<>();
                    collectSimpleQuads(state, null, allQuads);
                    for (Direction direction : Direction.values()) {
                        collectSimpleQuads(state, direction, allQuads);
                    }
                    this.simpleCulledQuads = buildCulledSet(allQuads);
                }
                return this.simpleCulledQuads;
            }
        }

        private IdentityHashMap<BakedQuad, Boolean> getExtendedCulledQuads(@Nullable BlockState state, ModelData extraData, @Nullable RenderType renderType) {
            IdentityHashMap<BakedQuad, Boolean> culled = this.extendedCulledQuads;
            if (culled != null) {
                return culled;
            }
            synchronized (this) {
                if (this.extendedCulledQuads == null) {
                    List<BakedQuad> allQuads = new ArrayList<>();
                    collectExtendedQuads(state, null, extraData, renderType, allQuads);
                    for (Direction direction : Direction.values()) {
                        collectExtendedQuads(state, direction, extraData, renderType, allQuads);
                    }
                    this.extendedCulledQuads = buildCulledSet(allQuads);
                }
                return this.extendedCulledQuads;
            }
        }

        private void collectSimpleQuads(@Nullable BlockState state, @Nullable Direction side, List<BakedQuad> target) {
            target.addAll(super.getQuads(state, side, RandomSource.create(42L)));
        }

        private void collectExtendedQuads(@Nullable BlockState state, @Nullable Direction side, ModelData extraData, @Nullable RenderType renderType, List<BakedQuad> target) {
            target.addAll(super.getQuads(state, side, RandomSource.create(42L), extraData, renderType));
        }

        private List<BakedQuad> cull(List<BakedQuad> quads, IdentityHashMap<BakedQuad, Boolean> culledQuads) {
            if (quads.isEmpty() || culledQuads.isEmpty()) {
                return quads;
            }
            return this.cache.computeIfAbsent(quads, input -> filterCulledQuads(input, culledQuads));
        }
    }

    private static IdentityHashMap<BakedQuad, Boolean> buildCulledSet(List<BakedQuad> quads) {
        Map<FaceKey, FaceBucket> buckets = new java.util.HashMap<>();
        for (BakedQuad quad : quads) {
            FaceKey key = FaceKey.of(quad);
            if (key == null) {
                continue;
            }
            buckets.computeIfAbsent(key, ignored -> new FaceBucket()).add(quad);
        }

        IdentityHashMap<BakedQuad, Boolean> culled = new IdentityHashMap<>();
        for (FaceBucket bucket : buckets.values()) {
            if (bucket.hasOppositePair()) {
                for (BakedQuad quad : bucket.quads) {
                    culled.put(quad, Boolean.TRUE);
                }
            }
        }
        return culled;
    }

    private static List<BakedQuad> filterCulledQuads(List<BakedQuad> quads, IdentityHashMap<BakedQuad, Boolean> culledQuads) {
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            if (!culledQuads.containsKey(quad)) {
                result.add(quad);
            }
        }
        return result.size() == quads.size() ? quads : List.copyOf(result);
    }

    private static BakedModel wrapResolvedModel(ResourceLocation id, BakedModel resolved) {
        if (resolved == null || resolved instanceof CulledBakedModel) {
            return resolved;
        }
        synchronized (RESOLVED_MODEL_CACHE) {
            return RESOLVED_MODEL_CACHE.computeIfAbsent(resolved, ignored -> new CulledBakedModel(id, resolved));
        }
    }

    private static final class CullingItemOverrides extends ItemOverrides {
        private final ResourceLocation id;
        private final ItemOverrides delegate;

        private CullingItemOverrides(ResourceLocation id, ItemOverrides delegate) {
            this.id = id;
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            BakedModel resolved = this.delegate.resolve(model, stack, level, entity, seed);
            return wrapResolvedModel(this.id, resolved);
        }
    }

    private static final class FaceBucket {
        private final List<BakedQuad> quads = new ArrayList<>(2);
        private Direction firstDirection;
        private boolean hasOpposite;

        private void add(BakedQuad quad) {
            if (this.firstDirection == null) {
                this.firstDirection = quad.getDirection();
            } else if (this.firstDirection.getOpposite() == quad.getDirection()) {
                this.hasOpposite = true;
            }
            this.quads.add(quad);
        }

        private boolean hasOppositePair() {
            return this.hasOpposite;
        }
    }

    private record FaceKey(Direction.Axis axis, int plane, int minA, int maxA, int minB, int maxB) {
        private static final int STRIDE = 8;

        private static FaceKey of(BakedQuad quad) {
            Direction direction = quad.getDirection();
            if (direction == null) {
                return null;
            }

            int[] vertices = quad.getVertices();
            if (vertices.length < STRIDE * 4) {
                return null;
            }

            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            float maxZ = Float.NEGATIVE_INFINITY;

            for (int i = 0; i < 4; i++) {
                int offset = i * STRIDE;
                float x = Float.intBitsToFloat(vertices[offset]);
                float y = Float.intBitsToFloat(vertices[offset + 1]);
                float z = Float.intBitsToFloat(vertices[offset + 2]);
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                minZ = Math.min(minZ, z);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
                maxZ = Math.max(maxZ, z);
            }

            return switch (direction.getAxis()) {
                case X -> new FaceKey(Direction.Axis.X, quantize((minX + maxX) * 0.5F), quantize(minY), quantize(maxY), quantize(minZ), quantize(maxZ));
                case Y -> new FaceKey(Direction.Axis.Y, quantize((minY + maxY) * 0.5F), quantize(minX), quantize(maxX), quantize(minZ), quantize(maxZ));
                case Z -> new FaceKey(Direction.Axis.Z, quantize((minZ + maxZ) * 0.5F), quantize(minX), quantize(maxX), quantize(minY), quantize(maxY));
            };
        }

        private static int quantize(float value) {
            return Math.round(value * 10000.0F);
        }
    }
}