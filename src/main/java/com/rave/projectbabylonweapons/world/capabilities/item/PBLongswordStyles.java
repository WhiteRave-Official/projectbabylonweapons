package com.rave.projectbabylonweapons.world.capabilities.item;

import yesman.epicfight.world.capabilities.item.Style;

public enum PBLongswordStyles implements Style {
    BASTION(true);

    private final boolean canUseOffhand;
    private final int id;

    PBLongswordStyles(boolean canUseOffhand) {
        this.id = Style.ENUM_MANAGER.assign(this);
        this.canUseOffhand = canUseOffhand;
    }

    @Override
    public int universalOrdinal() {
        return this.id;
    }

    public boolean canUseOffhand() {
        return this.canUseOffhand;
    }
}
