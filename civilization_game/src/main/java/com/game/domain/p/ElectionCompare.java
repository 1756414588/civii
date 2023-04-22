package com.game.domain.p;

import com.game.domain.Award;
import java.util.ArrayList;
import java.util.List;

public class ElectionCompare implements Comparable<ElectionCompare>{
    private long lordId;
    private int title;
    private long time;
    private int cityId;
    private List<Award> awards = new ArrayList<Award>(); // 选举人的消耗

    public ElectionCompare(long lordId, int title, long time) {
        this.lordId = lordId;
        this.title = title;
        this.time = time;
    }

    public ElectionCompare() {
    }


    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int compareTo(ElectionCompare compareEc) {
        int title = compareEc.getTitle();
        if (this.title < title) {
            return 1;
        }

        if (this.title > title) {
            return -1;
        }


        if (this.getTime() > compareEc.getTime()) {
            return 1;
        }

        if (this.getTime() < compareEc.getTime()) {
            return -1;
        }

        return 0;

    }

    public String toString() {
        return ("lordId = " + lordId + ", title = " + title + ", time = " +time + "\n");
    }


    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }


    public List<Award> getAwards() {
        return awards;
    }

    public void setAwards(List<Award> awards) {
        this.awards = awards;
    }
}
