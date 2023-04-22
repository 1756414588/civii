package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Activity {

    private int activityId;
    private int beginTime;
    private int awardId;
    private int sortord;
    private int history;
    private String params;
    private byte[] ranks;
    private byte[] addtion;
    private byte[] status;
    private byte[] records;
    private byte[] campMembers;
    private long lastSaveTime;
    private byte[] record;
}
