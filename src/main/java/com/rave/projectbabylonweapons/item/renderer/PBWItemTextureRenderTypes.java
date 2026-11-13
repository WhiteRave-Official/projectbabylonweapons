package com.rave.projectbabylonweapons.item.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class PBWItemTextureRenderTypes {
    private static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();

    private PBWItemTextureRenderTypes() { }

    static RenderType get(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, location -> isCutout(location)
                ? RenderType.entityCutoutNoCull(location) : RenderType.entityTranslucent(location));
    }

    static RenderType get(ResourceLocation texture, net.minecraft.client.renderer.MultiBufferSource source) {
        return source instanceof PBWItemAtlasBufferSource atlas ? atlas.renderType(texture) : get(texture);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static boolean isCutout(ResourceLocation texture) {
        var resources = Minecraft.getInstance().getResourceManager();
        if (resources.getResource(ResourceLocation.fromNamespaceAndPath(texture.getNamespace(),
                texture.getPath() + ".mcmeta")).isPresent()) return false;
        var resource = resources.getResource(texture);
        if (resource.isEmpty()) return false;
        try (var stream = new BufferedInputStream(resource.get().open())) {
            stream.mark(8);
            if (stream.readNBytes(8).length < 8) return false;
            stream.reset();
            try (var image = NativeImage.read(stream)) {
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        int alpha = image.getPixelRGBA(x, y) >>> 24;
                        if (alpha != 0 && alpha != 255) return false;
                    }
                }
                return true;
            }
        } catch (IOException | RuntimeException exception) {
            ProjectBabylonWeapons.LOGGER.debug("Unable to inspect weapon texture {}; retaining translucent rendering", texture, exception);
            return false;
        }
    }
}
