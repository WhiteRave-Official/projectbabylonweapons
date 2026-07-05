package com.rave.projectbabylonweapons.client.renderer;

import com.rave.projectbabylonweapons.client.model.EnderSpellProjectileEntityModel;
import com.rave.projectbabylonweapons.world.entity.projectile.EnderSpellProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class EnderSpellProjectileRenderer extends AbstractSpellProjectileRenderer<EnderSpellProjectileEntity> {
    public EnderSpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new EnderSpellProjectileEntityModel());
    }
}