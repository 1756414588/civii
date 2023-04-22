package com.game.domain.p;

import com.game.pb.SerializePb;
import com.game.util.DateHelper;
import com.game.util.TimeHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cpz
 * @date 2020/10/28 1:24
 * @description
 */
@Getter
@Setter
public class SmallCityGame {
    /**
     * 总点击次数 0点重置
     */
    private int total;
    /**
     * 虫子位置
     */
    private Map<Integer, Integer> worms = new HashMap<>();
    /**
     * 上次刷新时间
     */
    private long lastRefushTime;
    /**
     * 奖励类型对应获得次数
     */
    private Map<Integer, Integer> rewards = new HashMap<>();
    private long lastSendTime;

    public SmallCityGame() {

    }

    /**
     * 重置
     *
     * @param total
     */
    public void reset(int total) {
        this.total = total;
        this.rewards.clear();
    }

    public SmallCityGame(SerializePb.SerSmallGame smallGame) {
        this.total = smallGame.getTotal();
        smallGame.getWormsList().forEach(e -> {
            worms.put(e.getV1(), e.getV2());
        });
        lastRefushTime = smallGame.getLastRefushTime();
        smallGame.getRewardsList().forEach(e -> {
            rewards.put(e.getV1(), e.getV2());
        });
    }

    public void addTotal() {
        this.total++;
    }


    public void refushAll() {
        if (lastRefushTime != 0) {
            Date lastRefushTime = new Date(lastSendTime * TimeHelper.SECOND_MS);
            if (!DateHelper.isToday(lastRefushTime)) {
                //设置成当日已刷虫子数量
                reset(0);
            }
        }

    }
}
