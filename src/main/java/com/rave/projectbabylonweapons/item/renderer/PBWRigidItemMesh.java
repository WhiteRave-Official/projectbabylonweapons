package com.rave.projectbabylonweapons.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rave.projectbabylonweapons.client.culling.PBWGeoCubeOcclusionCullingHelper;
import java.util.IdentityHashMap;
import java.util.Map;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtils;

public final class PBWRigidItemMesh {
    private static final Map<GeoBone, PBWRigidItemMesh> CACHE = new IdentityHashMap<>();
    private final float[] vertices;
    private final float[] normals;
    private final byte[] flatCorrections;

    private PBWRigidItemMesh(float[] vertices, float[] normals, byte[] flatCorrections) {
        this.vertices = vertices;
        this.normals = normals;
        this.flatCorrections = flatCorrections;
    }

    static PBWRigidItemMesh get(GeoBone bone) {
        PBWRigidItemMesh mesh = CACHE.get(bone);
        if (mesh == null) {
            mesh = bake(bone);
            CACHE.put(bone, mesh);
        }
        return mesh;
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static PBWRigidItemMesh bake(GeoBone bone) {
        int faces = 0;
        for (GeoCube cube : bone.getCubes()) {
            faces += PBWGeoCubeOcclusionCullingHelper.visibleQuads(bone, cube).length;
        }
        float[] vertices = new float[faces * 20];
        float[] normals = new float[faces * 3];
        byte[] corrections = new byte[faces];
        PoseStack poses = new PoseStack();
        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();
        int offset = 0;
        int face = 0;
        for (GeoCube cube : bone.getCubes()) {
            GeoQuad[] quads = PBWGeoCubeOcclusionCullingHelper.visibleQuads(bone, cube);
            if (quads.length == 0) continue;
            poses.pushPose();
            RenderUtils.translateToPivotPoint(poses, cube);
            RenderUtils.rotateMatrixAroundCube(poses, cube);
            RenderUtils.translateAwayFromPivotPoint(poses, cube);
            var pose = poses.last();
            var size = cube.size();
            byte correction = (byte) ((size.y == 0 || size.z == 0 ? 1 : 0)
                    | (size.x == 0 || size.z == 0 ? 2 : 0)
                    | (size.x == 0 || size.y == 0 ? 4 : 0));
            for (GeoQuad quad : quads) {
                pose.normal().transform(normal.set(quad.normal()));
                normals[face * 3] = normal.x;
                normals[face * 3 + 1] = normal.y;
                normals[face * 3 + 2] = normal.z;
                corrections[face++] = correction;
                for (GeoVertex vertex : quad.vertices()) {
                    pose.pose().transformPosition(position.set(vertex.position()));
                    vertices[offset++] = position.x;
                    vertices[offset++] = position.y;
                    vertices[offset++] = position.z;
                    vertices[offset++] = vertex.texU();
                    vertices[offset++] = vertex.texV();
                }
            }
            poses.popPose();
        }
        return new PBWRigidItemMesh(vertices, normals, corrections);
    }

    void render(PoseStack poses, VertexConsumer buffer, int light, int overlay,
                float red, float green, float blue, float alpha) {
        var pose = poses.last().pose();
        var normal = poses.last().normal();
        int offset = 0;
        for (int face = 0; face < flatCorrections.length; face++) {
            float nx = normals[face * 3];
            float ny = normals[face * 3 + 1];
            float nz = normals[face * 3 + 2];
            float xNormal = normal.m00() * nx + normal.m10() * ny + normal.m20() * nz;
            float yNormal = normal.m01() * nx + normal.m11() * ny + normal.m21() * nz;
            float zNormal = normal.m02() * nx + normal.m12() * ny + normal.m22() * nz;
            byte correction = flatCorrections[face];
            if (xNormal < 0 && (correction & 1) != 0) xNormal = -xNormal;
            if (yNormal < 0 && (correction & 2) != 0) yNormal = -yNormal;
            if (zNormal < 0 && (correction & 4) != 0) zNormal = -zNormal;
            for (int vertex = 0; vertex < 4; vertex++) {
                float x = vertices[offset++];
                float y = vertices[offset++];
                float z = vertices[offset++];
                float u = vertices[offset++];
                float v = vertices[offset++];
                buffer.vertex(pose.m00() * x + pose.m10() * y + pose.m20() * z + pose.m30(),
                        pose.m01() * x + pose.m11() * y + pose.m21() * z + pose.m31(),
                        pose.m02() * x + pose.m12() * y + pose.m22() * z + pose.m32(),
                        red, green, blue, alpha, u, v, overlay, light, xNormal, yNormal, zNormal);
            }
        }
    }
}
