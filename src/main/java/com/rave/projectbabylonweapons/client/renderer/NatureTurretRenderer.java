package com.rave.projectbabylonweapons.client.renderer;

import com.rave.projectbabylonweapons.client.model.NatureTurretModel;
import com.rave.projectbabylonweapons.world.entity.mob.NatureTurretEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class NatureTurretRenderer<T extends NatureTurretEntity> extends GeoEntityRenderer<T> {
    public NatureTurretRenderer(EntityRendererProvider.Context context) {
        super(context, new NatureTurretModel<>());
        shadowRadius = .5F;
    }
    @Override protected float getDeathMaxRotation(T entity) { return 0; }
}
