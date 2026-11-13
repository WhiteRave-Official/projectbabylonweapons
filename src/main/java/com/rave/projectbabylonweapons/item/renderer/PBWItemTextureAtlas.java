package com.rave.projectbabylonweapons.item.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class PBWItemTextureAtlas {
    private static final int PADDING = 2;
    private static final Map<ResourceLocation, Region> REGIONS = new HashMap<>();
    private static final Map<BakedGeoModel, Boolean> MODELS = new IdentityHashMap<>();
    private static final List<Page> PAGES = new ArrayList<>();

    record Region(ResourceLocation texture, float u, float v, float width, float height) { }

    private PBWItemTextureAtlas() { }

    static boolean supports(BakedGeoModel model) {
        return MODELS.computeIfAbsent(model, value -> value.topLevelBones().stream().allMatch(PBWItemTextureAtlas::supports));
    }

    private static boolean supports(GeoBone bone) {
        for (var cube : bone.getCubes()) {
            for (var quad : cube.quads()) {
                if (quad == null) continue;
                for (var vertex : quad.vertices()) {
                    if (!(vertex.texU() >= 0 && vertex.texU() <= 1 && vertex.texV() >= 0 && vertex.texV() <= 1)) return false;
                }
            }
        }
        for (var child : bone.getChildBones()) if (!supports(child)) return false;
        return true;
    }

    static Region resolve(ResourceLocation texture) {
        Region existing = REGIONS.get(texture);
        if (existing != null) return existing;
        RenderSystem.assertOnRenderThread();
        Region region = new Region(texture, 0, 0, 1, 1);
        if (PBWItemTextureRenderTypes.get(texture) == RenderType.entityCutoutNoCull(texture)) {
            var resource = Minecraft.getInstance().getResourceManager().getResource(texture);
            if (resource.isPresent()) {
                try (var stream = resource.get().open(); var image = NativeImage.read(stream)) {
                    int required = Math.max(image.getWidth(), image.getHeight()) + PADDING * 2;
                    int limit = RenderSystem.maxSupportedTextureSize();
                    if (required <= limit) {
                        Region packed = null;
                        for (Page page : PAGES) {
                            packed = page.add(image);
                            if (packed != null) break;
                        }
                        if (packed == null) {
                            int size = Math.min(1024, limit);
                            while (size < required) size = Math.min(size * 2, limit);
                            Page page = new Page(size, PAGES.size());
                            PAGES.add(page);
                            packed = page.add(image);
                        }
                        if (packed != null) region = packed;
                    }
                } catch (IOException | RuntimeException exception) {
                    ProjectBabylonWeapons.LOGGER.debug("Unable to atlas weapon texture {}; retaining original texture", texture, exception);
                }
            }
        }
        REGIONS.put(texture, region);
        return region;
    }

    public static void clearCache() {
        List<Page> pages = new ArrayList<>(PAGES);
        PAGES.clear();
        REGIONS.clear();
        MODELS.clear();
        if (pages.isEmpty()) return;
        Runnable release = () -> {
            var manager = Minecraft.getInstance().getTextureManager();
            for (Page page : pages) manager.release(page.location);
        };
        if (RenderSystem.isOnRenderThread()) release.run();
        else RenderSystem.recordRenderCall(release::run);
    }

    private static final class Page {
        private final int size;
        private final ResourceLocation location;
        private final DynamicTexture texture;
        private int x;
        private int y;
        private int rowHeight;

        private Page(int size, int index) {
            this.size = size;
            location = ResourceLocation.fromNamespaceAndPath(ProjectBabylonWeapons.MODID, "dynamic/weapon_atlas_" + index);
            texture = new DynamicTexture(size, size, true);
            texture.setFilter(false, false);
            Minecraft.getInstance().getTextureManager().register(location, texture);
        }

        private Region add(NativeImage image) {
            int width = image.getWidth();
            int height = image.getHeight();
            int paddedWidth = width + PADDING * 2;
            int paddedHeight = height + PADDING * 2;
            if (paddedWidth > size || paddedHeight > size) return null;
            int nextX = x;
            int nextY = y;
            int nextHeight = rowHeight;
            if (nextX + paddedWidth > size) {
                nextX = 0;
                nextY += nextHeight;
                nextHeight = 0;
            }
            if (nextY + paddedHeight > size) return null;
            NativeImage pixels = texture.getPixels();
            for (int py = -PADDING; py < height + PADDING; py++) {
                for (int px = -PADDING; px < width + PADDING; px++) {
                    pixels.setPixelRGBA(nextX + px + PADDING, nextY + py + PADDING,
                            image.getPixelRGBA(Math.max(0, Math.min(width - 1, px)), Math.max(0, Math.min(height - 1, py))));
                }
            }
            texture.upload();
            Region region = new Region(location, (nextX + PADDING) / (float) size,
                    (nextY + PADDING) / (float) size, width / (float) size, height / (float) size);
            x = nextX + paddedWidth;
            y = nextY;
            rowHeight = Math.max(nextHeight, paddedHeight);
            return region;
        }
    }
}
