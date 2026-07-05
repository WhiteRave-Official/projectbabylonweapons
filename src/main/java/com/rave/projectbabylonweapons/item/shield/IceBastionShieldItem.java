package com.rave.projectbabylonweapons.item.shield;

public class IceBastionShieldItem extends PBGeoShieldItem implements BastionShield {
    public IceBastionShieldItem() {
        super(new Properties().durability(224), "geo/ice_bastion_shield.geo.json", "textures/item/ice_bastion_shield.png");
    }
}

