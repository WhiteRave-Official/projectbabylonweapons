package com.rave.projectbabylonweapons.item.shield;

public class GoldenBastionShieldItem extends PBGeoShieldItem implements BastionShield {
    public GoldenBastionShieldItem() {
        super(new Properties().durability(224), "geo/golden_bastion_shield.geo.json", "textures/item/golden_bastion_shield.png");
    }
}

