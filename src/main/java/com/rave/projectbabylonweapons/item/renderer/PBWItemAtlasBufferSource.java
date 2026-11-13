package com.rave.projectbabylonweapons.item.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

final class PBWItemAtlasBufferSource implements MultiBufferSource {
    private final MultiBufferSource source;
    private final ResourceLocation original;
    private final PBWItemTextureAtlas.Region region;
    private final RenderType renderType;

    PBWItemAtlasBufferSource(MultiBufferSource source, ResourceLocation original, PBWItemTextureAtlas.Region region) {
        this.source = source;
        this.original = original;
        this.region = region;
        renderType = RenderType.entityCutoutNoCull(region.texture());
    }

    RenderType renderType(ResourceLocation texture) {
        return original.equals(texture) ? renderType : PBWItemTextureRenderTypes.get(texture);
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        VertexConsumer buffer = source.getBuffer(type);
        return type == renderType ? new AtlasConsumer(buffer, region) : buffer;
    }

    private record AtlasConsumer(VertexConsumer delegate, PBWItemTextureAtlas.Region region) implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(region.u() + u * region.width(), region.v() + v * region.height());
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(red, green, blue, alpha);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }

        @Override
        public void vertex(float x, float y, float z, float red, float green, float blue, float alpha,
                           float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
            delegate.vertex(x, y, z, red, green, blue, alpha, region.u() + u * region.width(),
                    region.v() + v * region.height(), overlay, light, normalX, normalY, normalZ);
        }
    }
}
