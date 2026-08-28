package com.rave.projectbabylonweapons.client.culling;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtils;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class PBWGeoCubeOcclusionCullingHelper {
    private static final float EPSILON = 0.001F;
    private static final Map<GeoCube, Bounds> BOUNDS_CACHE = new IdentityHashMap<>();

    private PBWGeoCubeOcclusionCullingHelper() {
    }

    public static boolean isQuadOccludedBySiblingCube(GeoCube currentCube, GeoQuad quad, List<GeoCube> siblingCubes) {
        if (!isSafeVolumeCube(currentCube) || quad == null || siblingCubes == null || siblingCubes.size() <= 1) {
            return false;
        }

        Vector3f[] quadVertices = transformQuad(currentCube, quad);
        for (GeoCube siblingCube : siblingCubes) {
            if (siblingCube == currentCube || !isSafeVolumeCube(siblingCube)) {
                continue;
            }

            Bounds siblingBounds = BOUNDS_CACHE.computeIfAbsent(siblingCube, Bounds::of);
            if (siblingBounds.containsAll(quadVertices)) {
                return true;
            }
        }

        return false;
    }

    private static Vector3f[] transformQuad(GeoCube cube, GeoQuad quad) {
        PoseStack poseStack = new PoseStack();
        RenderUtils.translateToPivotPoint(poseStack, cube);
        RenderUtils.rotateMatrixAroundCube(poseStack, cube);
        RenderUtils.translateAwayFromPivotPoint(poseStack, cube);

        GeoVertex[] vertices = quad.vertices();
        Vector3f[] transformedVertices = new Vector3f[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            Vector4f transformed = new Vector4f(vertices[i].position(), 1.0F);
            transformed.mul(poseStack.last().pose());
            transformedVertices[i] = new Vector3f(transformed.x(), transformed.y(), transformed.z());
        }
        return transformedVertices;
    }

    private static boolean isSafeVolumeCube(GeoCube cube) {
        if (cube == null || cube.size() == null || cube.rotation() == null) {
            return false;
        }

        return cube.size().x > EPSILON
                && cube.size().y > EPSILON
                && cube.size().z > EPSILON
                && Math.abs(cube.rotation().x) <= EPSILON
                && Math.abs(cube.rotation().y) <= EPSILON
                && Math.abs(cube.rotation().z) <= EPSILON;
    }

    private record Bounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        private static Bounds of(GeoCube cube) {
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            float maxZ = Float.NEGATIVE_INFINITY;

            for (GeoQuad quad : cube.quads()) {
                for (Vector3f vertex : transformQuad(cube, quad)) {
                    minX = Math.min(minX, vertex.x());
                    minY = Math.min(minY, vertex.y());
                    minZ = Math.min(minZ, vertex.z());
                    maxX = Math.max(maxX, vertex.x());
                    maxY = Math.max(maxY, vertex.y());
                    maxZ = Math.max(maxZ, vertex.z());
                }
            }

            return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
        }

        private boolean containsAll(Vector3f[] vertices) {
            for (Vector3f vertex : vertices) {
                if (!contains(vertex)) {
                    return false;
                }
            }
            return true;
        }

        private boolean contains(Vector3f vertex) {
            return vertex.x() >= this.minX - EPSILON && vertex.x() <= this.maxX + EPSILON
                    && vertex.y() >= this.minY - EPSILON && vertex.y() <= this.maxY + EPSILON
                    && vertex.z() >= this.minZ - EPSILON && vertex.z() <= this.maxZ + EPSILON;
        }
    }
}
