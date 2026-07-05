package com.rave.projectbabylonweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rave.projectbabylonweapons.client.model.HolyMagicalSealEntityModel;
import com.rave.projectbabylonweapons.world.entity.effect.HolyMagicalSealEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.UUID;

public class HolyMagicalSealRenderer extends GeoEntityRenderer<HolyMagicalSealEntity> {
    private static final double Y_OFFSET = 0.02D;

    public HolyMagicalSealRenderer(EntityRendererProvider.Context context) {
        super(context, new HolyMagicalSealEntityModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(HolyMagicalSealEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        Vec3 offset = this.computeRenderOffset(entity, partialTick);

        poseStack.pushPose();
        if (offset != null) {
            poseStack.translate(offset.x, offset.y, offset.z);
        }
        super.render(entity, 0.0F, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    private Vec3 computeRenderOffset(HolyMagicalSealEntity entity, float partialTick) {
        Player target = this.resolveTrackedPlayer(entity);
        if (target == null) {
            return null;
        }

        double targetX = Mth.lerp(partialTick, target.xo, target.getX());
        double targetY = Mth.lerp(partialTick, target.yo, target.getY()) + Y_OFFSET;
        double targetZ = Mth.lerp(partialTick, target.zo, target.getZ());

        double currentX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double currentY = Mth.lerp(partialTick, entity.yo, entity.getY());
        double currentZ = Mth.lerp(partialTick, entity.zo, entity.getZ());
        return new Vec3(targetX - currentX, targetY - currentY, targetZ - currentZ);
    }

    private Player resolveTrackedPlayer(HolyMagicalSealEntity entity) {
        Entity vehicle = entity.getVehicle();
        if (vehicle instanceof Player player) {
            return player;
        }

        UUID targetUuid = entity.getTargetUuid();
        return targetUuid == null ? null : entity.level().getPlayerByUUID(targetUuid);
    }
}
