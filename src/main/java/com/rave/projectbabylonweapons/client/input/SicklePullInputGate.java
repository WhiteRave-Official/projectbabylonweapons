package com.rave.projectbabylonweapons.client.input;

public final class SicklePullInputGate {
    private int projectile = -1;
    private boolean armed;

    public boolean accept(int id, boolean tethered, boolean buttonsReleased, boolean clicked) {
        if (projectile != id) {
            projectile = id;
            armed = false;
        }
        if (id < 0 || !tethered) {
            armed = false;
            return false;
        }
        boolean accepted = armed && clicked;
        if (clicked) armed = false;
        else if (buttonsReleased) armed = true;
        return accepted;
    }

    public void reset() {
        projectile = -1;
        armed = false;
    }
}
