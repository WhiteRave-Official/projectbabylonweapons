package com.rave.projectbabylonweapons.world.capabilities.item;

import yesman.epicfight.world.capabilities.item.Style;

public enum PBArclightStyles implements Style {
    EVERGATE(false);

    private final boolean canUseOffhand;
    private final int id;

    PBArclightStyles(boolean canUseOffhand) {
        this.id = Style.ENUM_MANAGER.assign(this);
        this.canUseOffhand = canUseOffhand;
    }

    @Override
    public int universalOrdinal() { return this.id; }

    @Override
    public boolean canUseOffhand() { return this.canUseOffhand; }
}