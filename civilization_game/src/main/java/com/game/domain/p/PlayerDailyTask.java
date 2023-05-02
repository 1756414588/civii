package com.game.domain.p;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @date 2021/3/2 14:47
 *
 * 每日任务记录
 */
@Getter
@Setter
public class PlayerDailyTask implements Cloneable {
    /**
     * 活跃
     */
    private int active;
    /**
     * 任务完成状态
     */
    private Map<Integer, Integer> taskCount = new HashMap<>();
    /**
     * 任务领取状态
     */
    private Map<Integer, Integer> taskState = new HashMap<>();
    /**
     * 活跃奖励领取状态
     */
    private Map<Integer, Integer> activeState = new HashMap<>();

    /**
     * 转点重置状态喝活跃
     */
    public void reset() {
        this.active = 0;
        this.taskCount.clear();
        this.taskState.clear();
        this.activeState.clear();
    }

    public int getNeedTime(int key) {
        if (taskCount.containsKey(key)) {
            return taskCount.get(key);
        }
        return 0;
    }

    public int getTaskState(int key) {
        if (taskState.containsKey(key)) {
            return taskState.get(key);
        }
        return 0;
    }

    public int getActiveState(int key) {
        if (activeState.containsKey(key)) {
            return activeState.get(key);
        }
        return 0;
    }

    public void addActive(int active) {
        this.active += active;
    }

    public int getTaskCount(int key) {
        if (taskCount.containsKey(key)) {
            return taskCount.get(key);
        }
        return 0;
    }

    public String toData() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("active", active);
        jsonObject.put("taskCount", taskCount);
        jsonObject.put("taskState", taskState);
        jsonObject.put("activeState", activeState);
        return jsonObject.toJSONString();
    }

    public void serData(String value) {
        JSONObject jsonObject = JSON.parseObject(value);
        active = jsonObject.getIntValue("active");
        taskCount = jsonstr2map(jsonObject.getString("taskCount"));
        taskState = jsonstr2map(jsonObject.getString("taskState"));
        activeState = jsonstr2map(jsonObject.getString("activeState"));
    }

    private Map<Integer, Integer> jsonstr2map(String jsonstr) {
        Map<String, ?> m = JSONObject.parseObject(jsonstr);
        Map<Integer, Integer> res = new HashMap<>();
        m.entrySet().stream().forEach(e -> {
            res.put(Integer.parseInt(e.getKey()), (Integer) e.getValue());
        });
        return res;
    }

    @Override
    public PlayerDailyTask clone() {
        PlayerDailyTask playerDailyTask = null;
        try {
            playerDailyTask = (PlayerDailyTask) super.clone();
            HashMap<Integer, Integer> map1 = new HashMap<>();
            map1.putAll(this.taskCount);
            playerDailyTask.setTaskCount(map1);
            HashMap<Integer, Integer> map2 = new HashMap<>();
            map2.putAll(this.taskState);
            playerDailyTask.setTaskState(map2);
            HashMap<Integer, Integer> map3 = new HashMap<>();
            map3.putAll(activeState);
            playerDailyTask.setActiveState(map3);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return playerDailyTask;
    }
}
