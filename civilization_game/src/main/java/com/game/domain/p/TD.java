package com.game.domain.p;

import com.game.pb.DataPb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cpz
 * @date 2020/8/19 17:13
 * @description
 */
public class TD implements Cloneable {
    private int levelId; // 关卡id
    private int state; // 开启状态 0 未开启 1已通关(奖励已领完) 2 待通关 3 奖励待领取
    private int star; // 通关星级
    private Map<Integer, Integer> starRewardStatus; // 星级奖励领取状态

    public static final int HAVE_REWARD = 0; // 有奖励可领取
    public static final int HAS_REWARD = 1; // 已领取
    public static final int NO_REWARD = 2; // 没有奖励

    @Override
    public TD clone() {
        TD td = null;
        try {
          td = (TD) super.clone();
          HashMap<Integer, Integer> map = new HashMap<>();
          map.putAll(this.starRewardStatus);
          td.setStarRewardStatus(map);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return td;
    }

    public TD(int levelId, int state, int star, List<Integer> starLimit) {
        this.levelId = levelId;
        this.state = state;
        this.star = star;
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < starLimit.size(); i++) {
            map.put(i + 1, NO_REWARD);
        }
        this.starRewardStatus = map;
    }

    public TD(DataPb.TDRecord record) {
        this.levelId = record.getLevelId();
        this.state = record.getState();
        this.star = record.getStar();
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < record.getStarRewardStatusList().size(); i++) {
            map.put(i + 1, record.getStarRewardStatusList().get(i));
        }
        this.starRewardStatus = map;
    }

    public void rewardStarReward(int star) {
        starRewardStatus.put(star, HAS_REWARD);
    }

    public void openStarReward(int maxStar) {
        for (int i = 1; i <= 3; i++) {
            if (getStarRewardStatus(i) == NO_REWARD && maxStar >= i) {
                starRewardStatus.put(i, HAVE_REWARD);
            }
        }
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public Map<Integer, Integer> getStarRewardStatus() {
        return starRewardStatus;
    }

    public void setStarRewardStatus(Map<Integer, Integer> starRewardStatus) {
        this.starRewardStatus = starRewardStatus;
    }

    public int getStarRewardStatus(int star) {
        return this.starRewardStatus.get(star);
    }
}
