package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.Player;

/**
 * 支付事件
 */
public class PayActor extends BaseActivityActor {

    private int money;
    private long coin;
    private int payTime;

    public PayActor(Player player, int money, long coin, int payTime) {
        this.player = player;
        this.money = money;
        this.coin = coin;
        this.payTime = payTime;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public long getCoin() {
        return coin;
    }

    public void setCoin(long coin) {
        this.coin = coin;
    }

    public int getPayTime() {
        return payTime;
    }

    public void setPayTime(int payTime) {
        this.payTime = payTime;
    }
}
