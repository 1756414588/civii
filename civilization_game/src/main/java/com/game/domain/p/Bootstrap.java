package com.game.domain.p;

public class Bootstrap {

    private int keyId;
    private int user;
    private int world;
    private int courty;
    private int activity;

    public Bootstrap() {
    }

    public Bootstrap(int keyId) {
        this.keyId = keyId;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public int getCourty() {
        return courty;
    }

    public void setCourty(int courty) {
        this.courty = courty;
    }

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }
}
