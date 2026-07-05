package com.rave.projectbabylonweapons.client.renderer;

import com.rave.projectbabylonweapons.client.model.BasicSpellProjectileEntityModel;
import com.rave.projectbabylonweapons.world.entity.projectile.BasicSpellProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BasicSpellProjectileRenderer extends AbstractSpellProjectileRenderer<BasicSpellProjectileEntity> {
    public BasicSpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new BasicSpellProjectileEntityModel());
    }
}