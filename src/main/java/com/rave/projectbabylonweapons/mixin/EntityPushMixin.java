package com.rave.projectbabylonweapons.mixin;

import com.rave.projectbabylonweapons.animation.EntityCollisionAnimationHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityPushMixin {
    @Redirect(
            method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    private List<VoxelShape> projectBabylonWeapons$ignoreAnimationEntityCollisions(
            Level level,
            Entity entity,
            AABB bounds
    ) {
        return EntityCollisionAnimationHelper.ignoresEntityCollision(entity)
                ? List.of()
                : level.getEntityCollisions(entity, bounds);
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void projectBabylonWeapons$ignoreAnimationEntityPush(Entity other, CallbackInfo callback) {
        Entity self = (Entity) (Object) this;
        if (EntityCollisionAnimationHelper.ignoresEntityCollision(self)
                || EntityCollisionAnimationHelper.ignoresEntityCollision(other)) {
            callback.cancel();
        }
    }
}
