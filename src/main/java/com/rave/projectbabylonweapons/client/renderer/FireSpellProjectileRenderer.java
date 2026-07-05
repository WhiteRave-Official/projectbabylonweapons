package com.rave.projectbabylonweapons.client.renderer;

import com.rave.projectbabylonweapons.client.model.FireSpellProjectileEntityModel;
import com.rave.projectbabylonweapons.world.entity.projectile.FireSpellProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class FireSpellProjectileRenderer extends AbstractSpellProjectileRenderer<FireSpellProjectileEntity> {
    public FireSpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new FireSpellProjectileEntityModel());
    }
}