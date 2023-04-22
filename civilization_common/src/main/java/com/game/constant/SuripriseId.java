package com.game.constant;

/**
 * @author zcp
 * @date 2021/3/9 14:22
 * 诵我真名者,永不见bug
 */
public enum SuripriseId {
    MarkerFlop(1, "市场翻牌"),
    Level(2, "等级礼包"),
    VipLevel(3, "VIP等级礼包"),
    GetHero(4, "活动领取7日狂欢英雄"),
    SearchHero(5, "搜寻特惠"),
    FirstCharge(6, "首充礼包"),
    Login(7, "登录礼包"),
    ;

    SuripriseId(int val, String desc) {
        this.val = val;
        this.desc = desc;
    }

    int val;
    String desc;

    public int get() {
        return val;
    }
}
