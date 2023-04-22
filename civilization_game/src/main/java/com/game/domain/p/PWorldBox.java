package com.game.domain.p;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.WorldBoxState;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @date 2021/1/7 15:33
 * @description
 */
@Getter
@Setter
public class PWorldBox implements Cloneable {
    private long lordId;
    private int points; //积分
    private int todayPoints;
    private int count;  //当日已获得宝箱个数
    private List<WorldBox> worldBoxList = new LinkedList<>();//世界宝箱

    public PWorldBox() {

    }

    @Override
    public PWorldBox clone() {
        PWorldBox pWorldBox = null;
        try {
            pWorldBox = (PWorldBox) super.clone();
            LinkedList<WorldBox> list = new LinkedList<>();
            this.worldBoxList.forEach(value -> {
                list.add(value.clone());
            });
            pWorldBox.setWorldBoxList(list);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return pWorldBox;
    }

    public void reset() {
        this.points = 0;
        this.todayPoints = 0;
        this.count = 0;
        this.worldBoxList.clear();
    }

    public void delPoints(int points) {
        this.points -= points;
        this.points = Math.max(0, this.points);
    }

    public void addPoints(int points) {
        this.points += points;
        this.todayPoints += points;
    }

    public void addCount() {
        this.count++;
    }

    public void addWorldBox(WorldBox box) {
        worldBoxList.add(box);
    }

    public WorldBox removeWorldBox() {
        return worldBoxList.remove(0);
    }

    public WorldBox getFirstOpenBox() {
        for (WorldBox box : worldBoxList) {
            if (box.getState() == WorldBoxState.OPENING) {
                return box;
            }
        }
        return null;
    }

    public WorldBox getFirstWaitBox() {
        for (WorldBox box : worldBoxList) {
            if (box.getState() == WorldBoxState.WAIT) {
                return box;
            }
        }
        return null;
    }

    public String toWorldBoxData() {
        JSONObject data = new JSONObject();
        data.put("points", points);
        data.put("todayPoints", todayPoints);
        data.put("count", count);
        data.put("worldBoxList", worldBoxList);
        return data.toJSONString();
    }

    public void insertAfterOpening(WorldBox worldBox) {
        for (int i = 0; i < worldBoxList.size(); i++) {
            WorldBox box = worldBoxList.get(i);
            if (box.getState() == WorldBoxState.TURNED_ON) {
                continue;
            }
            worldBoxList.add(i, worldBox);
            break;
        }
    }
}
