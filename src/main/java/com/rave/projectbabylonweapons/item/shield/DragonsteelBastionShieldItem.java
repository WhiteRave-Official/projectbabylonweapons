package com.rave.projectbabylonweapons.item.shield;

public class DragonsteelBastionShieldItem extends PBGeoShieldItem implements BastionShield {
    public DragonsteelBastionShieldItem() {
        super(new Properties().durability(448), "geo/dragonsteel_bastion_shield.geo.json", "textures/item/dragonsteel_bastion_shield.png");
    }
}

