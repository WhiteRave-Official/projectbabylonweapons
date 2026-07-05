package com.rave.projectbabylonweapons.client.renderer;

import com.rave.projectbabylonweapons.client.model.GoldenSpellProjectileEntityModel;
import com.rave.projectbabylonweapons.world.entity.projectile.GoldenSpellProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class GoldenSpellProjectileRenderer extends AbstractSpellProjectileRenderer<GoldenSpellProjectileEntity> {
    public GoldenSpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new GoldenSpellProjectileEntityModel());
    }
}