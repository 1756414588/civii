package com.game.domain.p;

import com.game.domain.Player;
import com.game.util.TimeHelper;

public class WorldHitRank {

    private Player player;

    private int hit;

    private int totalHit;

    private long time;

    private int index;

    private String desc;

    public WorldHitRank() {

    }

    public WorldHitRank(Player player, int hit) {
        this.player = player;
        this.hit = hit;
        this.time = System.currentTimeMillis();
        this.totalHit = hit;
    }

    public WorldHitRank(Player player, int hit, int totalHit, long time) {
        this.player = player;
        this.hit = hit;
        this.time = time;
        this.totalHit = totalHit;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getHit() {
        if (!TimeHelper.isSameDayOfMillis(this.time, System.currentTimeMillis())) {
            this.hit = 0;
        }
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public int getTotalHit() {
        return totalHit;
    }

    public void setTotalHit(int totalHit) {
        this.totalHit = totalHit;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void addHit(int hit) {
        if (!TimeHelper.isSameDayOfMillis(this.time, System.currentTimeMillis())) {
            this.hit = 0;
        }
        this.hit += hit;
        this.totalHit += hit;
        this.time = System.currentTimeMillis();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "WorldHitRank{" +
                "player=" + player +
                ", hit=" + hit +
                ", totalHit=" + totalHit +
                ", time=" + time +
                ", index=" + index +
                ", desc='" + desc + '\'' +
                '}';
    }
}
