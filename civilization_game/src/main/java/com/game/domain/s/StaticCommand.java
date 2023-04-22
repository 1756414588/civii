package com.game.domain.s;

import java.util.List;

public class StaticCommand {
    private int keyId;
    private int timeInterVal;
    private int collectLimit;
    private List<List<Integer>> openOfficer;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getTimeInterVal() {
        return timeInterVal;
    }

    public void setTimeInterVal(int timeInterVal) {
        this.timeInterVal = timeInterVal;
    }

    public int getCollectLimit() {
        return collectLimit;
    }

    public void setCollectLimit(int collectLimit) {
        this.collectLimit = collectLimit;
    }

    public List<List<Integer>> getOpenOfficer() {
        return openOfficer;
    }

    public void setOpenOfficer(List<List<Integer>> openOfficer) {
        this.openOfficer = openOfficer;
    }
}
