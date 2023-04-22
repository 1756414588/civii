package com.game.constant;

public enum EventType {

    TRACK("track"), USER_SET("user_set"), USER_SET_ONCE("user_setOnce"), USER_ADD("user_add"), USER_DEL("user_del"), USER_UNSET("user_unset"), USER_APPEND("user_append"), TRACK_UPDATE("track_update"), TRACK_OVERWRITE("track_overwrite");

    private String type;

    private EventType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
