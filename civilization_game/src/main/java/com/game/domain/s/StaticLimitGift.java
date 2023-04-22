package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zcp
 * @date 2021/3/9 11:38
 * 诵我真名者,永不见bug
 */
@Getter
@Setter
public class StaticLimitGift {
    private int keyId;
    /**
     * 奖励id
     */
    private List<List<Integer>> awardList;
    /**
     * 礼包名称
     */
    private String name;
    /**
     * 总价值钻石数
     */
    private int display;
    /**
     * 价格（RMB）
     */
    private int money;
    /**
     * 限购次数
     */
    private int count;
    /**
     * 限时时间（单位/秒）
     */
    private int time;
    /**
     * 描述
     */
    private String desc;
    /**
     * 背景图
     */
    private String asset;
    /**
     * 标题艺术字
     */
    private String icon;
    /**
     * 礼包出现条件
     */
    private List<Integer> limit;
    /**
     * 玩家创号时间、礼包仅会出现在创号时间内 若未填写则礼包均会出现
     */
    private List<Integer> timelimit;
    /**
     * 0为特殊种类，该礼包互斥，仅能购买一次
     */
    private List<Integer> typeid;
    /**
     * 是否互斥
     */
    private int mutex;

    private int awardId;

}
