package com.game.domain.p;

import com.game.constant.WorldBoxState;
import com.game.util.TimeHelper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2021/1/10 0:13
 * @description
 */
@Getter
@Setter
public class WorldBox implements Cloneable {
    private int boxId;  //宝箱id
    private long openTime;//开启时间 时间戳s
    private long readyTime; //在开启状态下经过的时间
    private WorldBoxState state;//宝箱状态
    private boolean Changed = false;//宝箱是否改变过

    public WorldBox() {

    }


    @Override
    public WorldBox clone() {
        WorldBox worldBox = null;
        try {
            worldBox = (WorldBox) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return worldBox;
    }

    @Builder
    public WorldBox(int boxId, long openTime, WorldBoxState state) {
        this.boxId = boxId;
        this.openTime = openTime;
        this.state = state;
    }

    public void delayOpenTime(int time) {
        this.openTime -= time;
        this.openTime = Math.max(0, this.openTime);
        this.readyTime += time;
    }

    public void addTime() {
        this.readyTime++;
    }

    //结束时间
    public boolean isOpen() {
        return TimeHelper.getCurrentSecond() >= openTime;
    }

}
