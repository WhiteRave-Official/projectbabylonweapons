package com.rave.projectbabylonweapons.item.shield;

public class NetheriteBastionShieldItem extends PBGeoShieldItem implements BastionShield {
    public NetheriteBastionShieldItem() {
        super(new Properties().durability(896).fireResistant(), "geo/netherite_bastion_shield.geo.json", "textures/item/netherite_bastion_shield.png");
    }
}

