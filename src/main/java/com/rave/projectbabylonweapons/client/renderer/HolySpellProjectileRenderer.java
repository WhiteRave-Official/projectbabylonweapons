package com.rave.projectbabylonweapons.client.renderer;

import com.rave.projectbabylonweapons.client.model.HolySpellProjectileEntityModel;
import com.rave.projectbabylonweapons.world.entity.projectile.HolySpellProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class HolySpellProjectileRenderer extends AbstractSpellProjectileRenderer<HolySpellProjectileEntity> {
    public HolySpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new HolySpellProjectileEntityModel());
    }
}