package com.rave.projectbabylonweapons.client.particle;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.physics.bezier.CubicBezierCurve;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.particle.EpicFightParticleRenderTypes;

import java.util.List;

public class BasicSpellProjectileTrailParticle extends TextureSheetParticle {
    private static final TrailInfo DEFAULT_TRAIL = TrailInfo.builder()
            .startPos(new Vec3(-0.18D, 0.0D, 0.45D))
            .endPos(new Vec3(0.18D, 0.0D, 0.45D))
            .interpolations(4)
            .lifetime(9)
            .updateInterval(1)
            .texture("epicfight:textures/particle/projectile_trail.png")
            .create();

    private final BasicSpellProjectileEntity owner;
    private final TrailInfo trailInfo;
    private final List<TrailEdge> trailEdges = Lists.newLinkedList();
    private float lastXRot;
    private float lastYRot;
    private boolean shouldRemove;

    protected BasicSpellProjectileTrailParticle(ClientLevel level, BasicSpellProjectileEntity owner) {
        super(level, owner.getX(), owner.getY(), owner.getZ());
        this.owner = owner;
        this.trailInfo = DEFAULT_TRAIL;
        this.hasPhysics = false;
        this.rCol = owner.getTrailRed();
        this.gCol = owner.getTrailGreen();
        this.bCol = owner.getTrailBlue();
        float size = (float) Math.max(this.trailInfo.start().length(), this.trailInfo.end().length()) * 2.0F;
        this.setSize(size, size);
    }

    @Override
    public void tick() {
        if (this.shouldRemove) {
            if (this.age >= this.lifetime) {
                this.remove();
            }
        } else if (!this.canContinue()) {
            this.shouldRemove = true;
            this.lifetime = this.age + this.trailInfo.trailLifetime();
        }

        this.age++;
        this.trailEdges.removeIf(edge -> !edge.isAlive());

        if (!this.canCreateNextCurve()) {
            return;
        }

        Vec3 lastPos = this.owner.getPosition(0.0F);
        double xd = Math.pow(this.owner.getX() - lastPos.x, 2);
        double yd = Math.pow(this.owner.getY() - lastPos.y, 2);
        double zd = Math.pow(this.owner.getZ() - lastPos.z, 2);
        float move = (float) Math.sqrt(xd + yd + zd) * 2.0F;
        this.setSize(this.bbWidth + move, this.bbHeight + move);
        this.createNextCurve();
    }

    private boolean canContinue() {
        return this.owner.isAlive() && !this.owner.isRemoved();
    }

    private boolean canCreateNextCurve() {
        return this.age % this.trailInfo.updateInterval() == 0 && !this.removed;
    }

    private void createNextCurve() {
        if (this.shouldRemove) {
            return;
        }

        this.rCol = this.owner.getTrailRed();
        this.gCol = this.owner.getTrailGreen();
        this.bCol = this.owner.getTrailBlue();

        boolean isFirstTrail = this.trailEdges.isEmpty();
        if (isFirstTrail) {
            this.lastXRot = this.owner.getXRot();
            this.lastYRot = 180.0F + this.owner.getYRot();
        }

        Vec3 posOld = this.owner.getPosition(0.0F);
        Vec3 posCur = this.owner.getPosition(1.0F);
        Vec3 posMid = MathUtils.lerpVector(posOld, posCur, 0.5F);

        float xRotO = this.lastXRot;
        float xRot = this.owner.getXRot();
        float xRotMod = Mth.rotLerp(0.5F, xRotO, xRot);
        float yRotO = this.lastYRot;
        float yRot = 180.0F + this.owner.getYRot();
        float yRotMod = Mth.rotLerp(0.5F, yRotO, yRot);

        OpenMatrix4f prevTransform = OpenMatrix4f
                .createTranslation((float) posOld.x, (float) posOld.y, (float) posOld.z)
                .rotateDeg(yRotO, Vec3f.Y_AXIS)
                .rotateDeg(xRotO, Vec3f.X_AXIS);
        OpenMatrix4f modTransform = OpenMatrix4f
                .createTranslation((float) posMid.x, (float) posMid.y, (float) posMid.z)
                .rotateDeg(yRotMod, Vec3f.Y_AXIS)
                .rotateDeg(xRotMod, Vec3f.X_AXIS);
        OpenMatrix4f curTransform = OpenMatrix4f
                .createTranslation((float) posCur.x, (float) posCur.y, (float) posCur.z)
                .rotateDeg(yRot, Vec3f.Y_AXIS)
                .rotateDeg(xRot, Vec3f.X_AXIS);

        Vec3 prevStartPos = OpenMatrix4f.transform(prevTransform, this.trailInfo.start());
        Vec3 prevEndPos = OpenMatrix4f.transform(prevTransform, this.trailInfo.end());
        Vec3 middleStartPos = OpenMatrix4f.transform(modTransform, this.trailInfo.start());
        Vec3 middleEndPos = OpenMatrix4f.transform(modTransform, this.trailInfo.end());
        Vec3 currentStartPos = OpenMatrix4f.transform(curTransform, this.trailInfo.start());
        Vec3 currentEndPos = OpenMatrix4f.transform(curTransform, this.trailInfo.end());

        List<Vec3> startPosList = Lists.newArrayList();
        List<Vec3> endPosList = Lists.newArrayList();
        TrailEdge edge1;
        TrailEdge edge2;

        if (isFirstTrail) {
            edge1 = new TrailEdge(prevStartPos, prevEndPos, -1);
            edge2 = new TrailEdge(middleStartPos, middleEndPos, -1);
        } else {
            edge1 = this.trailEdges.get(this.trailEdges.size() - (this.trailInfo.interpolateCount() / 2 + 1));
            edge2 = this.trailEdges.get(this.trailEdges.size() - 1);
            edge2.lifetime++;
        }

        startPosList.add(edge1.start);
        endPosList.add(edge1.end);
        startPosList.add(edge2.start);
        endPosList.add(edge2.end);
        startPosList.add(middleStartPos);
        endPosList.add(middleEndPos);
        startPosList.add(currentStartPos);
        endPosList.add(currentEndPos);

        List<Vec3> finalStartPositions = CubicBezierCurve.getBezierInterpolatedPoints(startPosList, 1, 3, this.trailInfo.interpolateCount());
        List<Vec3> finalEndPositions = CubicBezierCurve.getBezierInterpolatedPoints(endPosList, 1, 3, this.trailInfo.interpolateCount());

        if (!isFirstTrail) {
            finalStartPositions.remove(0);
            finalEndPositions.remove(0);
        }

        this.makeTrailEdges(finalStartPositions, finalEndPositions);
        this.lastXRot = xRot;
        this.lastYRot = yRot;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
        if (this.trailEdges.isEmpty()) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        int light = this.getLightColor(partialTick);
        this.setupPoseStack(poseStack, camera);
        Matrix4f matrix4f = poseStack.last().pose();
        int edges = this.trailEdges.size() - 1;
        boolean startFade = this.trailEdges.get(0).lifetime == 1;
        boolean endFade = this.trailEdges.get(edges).lifetime == this.trailInfo.trailLifetime();
        float startEdge = startFade ? this.trailInfo.interpolateCount() * 2 * partialTick : 0.0F;
        float endEdge = endFade ? Math.min(edges - (this.trailInfo.interpolateCount() * 2) * (1.0F - partialTick), edges - 1) : edges - 1;
        float interval = 1.0F / (endEdge - startEdge);
        float fading = 1.0F;

        if (this.shouldRemove) {
            fading = Mth.clamp(((this.lifetime - this.age) + (1.0F - partialTick)) / this.trailInfo.trailLifetime(), 0.0F, 1.0F);
        }

        float partialStartEdge = interval * (startEdge % 1.0F);
        float from = -partialStartEdge;
        float to = -partialStartEdge + interval;

        for (int i = (int) startEdge; i < (int) endEdge + 1; i++) {
            TrailEdge e1 = this.trailEdges.get(i);
            TrailEdge e2 = this.trailEdges.get(i + 1);
            Vector4f pos1 = new Vector4f((float) e1.start.x, (float) e1.start.y, (float) e1.start.z, 1.0F);
            Vector4f pos2 = new Vector4f((float) e1.end.x, (float) e1.end.y, (float) e1.end.z, 1.0F);
            Vector4f pos3 = new Vector4f((float) e2.end.x, (float) e2.end.y, (float) e2.end.z, 1.0F);
            Vector4f pos4 = new Vector4f((float) e2.start.x, (float) e2.start.y, (float) e2.start.z, 1.0F);

            pos1.mul(matrix4f);
            pos2.mul(matrix4f);
            pos3.mul(matrix4f);
            pos4.mul(matrix4f);

            float alphaFrom = Mth.clamp(from, 0.0F, 1.0F);
            float alphaTo = Mth.clamp(to, 0.0F, 1.0F);

            vertexConsumer.addVertex(pos1.x(), pos1.y(), pos1.z()).setUv(from, 1.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom * fading).setLight(light);
            vertexConsumer.addVertex(pos2.x(), pos2.y(), pos2.z()).setUv(from, 0.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom * fading).setLight(light);
            vertexConsumer.addVertex(pos3.x(), pos3.y(), pos3.z()).setUv(to, 0.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo * fading).setLight(light);
            vertexConsumer.addVertex(pos4.x(), pos4.y(), pos4.z()).setUv(to, 1.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo * fading).setLight(light);

            from += interval;
            to += interval;
        }
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(float partialTicks) {
        return net.minecraft.world.phys.AABB.INFINITE;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return EpicFightParticleRenderTypes.TRAIL_EFFECT.apply(this.trailInfo.texturePath());
    }

    private void setupPoseStack(PoseStack poseStack, Camera camera) {
        Vec3 vec3 = camera.getPosition();
        poseStack.translate((float) -vec3.x(), (float) -vec3.y(), (float) -vec3.z());
    }

    private void makeTrailEdges(List<Vec3> startPositions, List<Vec3> endPositions) {
        for (int i = 0; i < startPositions.size(); i++) {
            this.trailEdges.add(new TrailEdge(startPositions.get(i), endPositions.get(i), this.trailInfo.trailLifetime()));
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            int entityId = (int) x;
            if (!(level.getEntity(entityId) instanceof BasicSpellProjectileEntity projectile)) {
                return null;
            }
            return new BasicSpellProjectileTrailParticle(level, projectile);
        }
    }

    private static class TrailEdge {
        private final Vec3 start;
        private final Vec3 end;
        private int lifetime;

        private TrailEdge(Vec3 start, Vec3 end, int lifetime) {
            this.start = start;
            this.end = end;
            this.lifetime = lifetime;
        }

        private boolean isAlive() {
            return --this.lifetime > 0;
        }
    }
}
