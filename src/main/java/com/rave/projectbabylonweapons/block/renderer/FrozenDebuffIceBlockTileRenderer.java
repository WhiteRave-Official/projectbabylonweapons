package com.rave.projectbabylonweapons.block.renderer;

import com.rave.projectbabylonweapons.block.entity.FrozenDebuffIceBlockTileEntity;
import com.rave.projectbabylonweapons.block.model.FrozenDebuffIceBlockModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FrozenDebuffIceBlockTileRenderer extends GeoBlockRenderer<FrozenDebuffIceBlockTileEntity> {
    public FrozenDebuffIceBlockTileRenderer() {
        super(new FrozenDebuffIceBlockModel());
    }
}
