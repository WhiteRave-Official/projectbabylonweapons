package com.rave.projectbabylonweapons.client.renderer;

import com.rave.projectbabylonweapons.client.model.DiamondSpellProjectileEntityModel;
import com.rave.projectbabylonweapons.world.entity.projectile.DiamondSpellProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DiamondSpellProjectileRenderer extends AbstractSpellProjectileRenderer<DiamondSpellProjectileEntity> {
    public DiamondSpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new DiamondSpellProjectileEntityModel());
    }
}