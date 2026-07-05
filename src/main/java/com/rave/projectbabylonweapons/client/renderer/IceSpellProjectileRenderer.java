package com.rave.projectbabylonweapons.client.renderer;

import com.rave.projectbabylonweapons.client.model.IceSpellProjectileEntityModel;
import com.rave.projectbabylonweapons.world.entity.projectile.IceSpellProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class IceSpellProjectileRenderer extends AbstractSpellProjectileRenderer<IceSpellProjectileEntity> {
    public IceSpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new IceSpellProjectileEntityModel());
    }

    @Override
    protected float getAdditionalYawRotation(IceSpellProjectileEntity entity) {
        return 90.0F;
    }
}