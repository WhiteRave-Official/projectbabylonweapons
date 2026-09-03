package com.rave.projectbabylonweapons.mixin;

import com.rave.projectbabylonweapons.animation.EntityCollisionAnimationHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityPushMixin {
    @ModifyVariable(
            method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("STORE"),
            ordinal = 0
    )
    private List<VoxelShape> projectBabylonWeapons$ignoreAnimationEntityCollisions(List<VoxelShape> collisions) {
        Entity self = (Entity) (Object) this;
        return EntityCollisionAnimationHelper.ignoresEntityCollision(self) ? List.of() : collisions;
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
