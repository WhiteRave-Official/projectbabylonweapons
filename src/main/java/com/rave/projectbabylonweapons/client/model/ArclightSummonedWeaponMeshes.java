package com.rave.projectbabylonweapons.client.model;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SingleGroupVertexBuilder;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.VanillaModelTransformer.VanillaMeshPartDefinition;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.math.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rigid Epic Fight meshes built from the item models used by summoned weapons.
 * Every vertex is bound exclusively to the Weapon joint.
 */
public final class ArclightSummonedWeaponMeshes {
    private static final int WEAPON_JOINT_ID = 2;
    private static final MeshPartDefinition BODY = VanillaMeshPartDefinition.of("weapon");

    private static MeshCache sword;
    private static MeshCache spear;

    private ArclightSummonedWeaponMeshes() {
    }

    public static SkinnedMesh sword(BakedModel source) {
        sword = getOrBake(sword, source);
        return sword.mesh();
    }

    public static SkinnedMesh spear(BakedModel source) {
        spear = getOrBake(spear, source);
        return spear.mesh();
    }

    private static MeshCache getOrBake(MeshCache cache, BakedModel source) {
        if (cache != null && cache.source() == source) {
            return cache;
        }
        if (cache != null) {
            cache.mesh().destroy();
        }

        SkinnedMesh mesh = bake(source);
        mesh.initialize();
        return new MeshCache(source, mesh);
    }

    private static SkinnedMesh bake(BakedModel model) {
        List<SingleGroupVertexBuilder> vertices = new ArrayList<>();
        IntList indices = new IntArrayList();

        appendQuads(model, null, vertices, indices);
        for (Direction direction : Direction.values()) {
            appendQuads(model, direction, vertices, indices);
        }

        Map<MeshPartDefinition, IntList> parts = new HashMap<>();
        parts.put(BODY, indices);
        return SingleGroupVertexBuilder.loadVertexInformation(vertices, parts);
    }

    private static void appendQuads(BakedModel model, Direction direction,
                                    List<SingleGroupVertexBuilder> vertices, IntList indices) {
        RandomSource random = RandomSource.create(42L);
        for (BakedQuad quad : model.getQuads(null, direction, random)) {
            int base = vertices.size();
            int[] data = quad.getVertices();

            for (int vertex = 0; vertex < 4; vertex++) {
                int offset = vertex * 8;
                float x = Float.intBitsToFloat(data[offset]) - 0.5F;
                float y = Float.intBitsToFloat(data[offset + 1]) - 0.5F;
                float z = Float.intBitsToFloat(data[offset + 2]) - 0.5F;
                float u = Float.intBitsToFloat(data[offset + 4]);
                float v = Float.intBitsToFloat(data[offset + 5]);

                Vec3f normal = decodeNormal(data[offset + 7], quad.getDirection());
                vertices.add(new SingleGroupVertexBuilder()
                        // Item-space correction is baked once. Runtime orientation comes from animation bones.
                        .setPosition(new Vec3f(-x, -z, -y))
                        .setNormal(new Vec3f(-normal.x, -normal.z, -normal.y))
                        .setTextureCoordinate(new Vec2f(u, v))
                        .setEffectiveJointIDs(new Vec3f(WEAPON_JOINT_ID, 0.0F, 0.0F))
                        .setEffectiveJointWeights(new Vec3f(1.0F, 0.0F, 0.0F))
                        .setEffectiveJointNumber(1));
            }

            addTriangle(indices, base, base + 1, base + 2);
            addTriangle(indices, base, base + 2, base + 3);
        }
    }

    private static Vec3f decodeNormal(int packed, Direction fallback) {
        float x = (byte) (packed & 0xFF) / 127.0F;
        float y = (byte) ((packed >>> 8) & 0xFF) / 127.0F;
        float z = (byte) ((packed >>> 16) & 0xFF) / 127.0F;
        if (x == 0.0F && y == 0.0F && z == 0.0F) {
            x = fallback.getStepX();
            y = fallback.getStepY();
            z = fallback.getStepZ();
        }
        return new Vec3f(x, y, z);
    }

    private static void addTriangle(IntList indices, int first, int second, int third) {
        addVertex(indices, first);
        addVertex(indices, second);
        addVertex(indices, third);
    }

    private static void addVertex(IntList indices, int index) {
        indices.add(index);
        indices.add(index);
        indices.add(index);
    }

    private record MeshCache(BakedModel source, SkinnedMesh mesh) {
    }
}

