package com.game.timer;

import com.game.manager.BigMonsterManager;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 *
 * @date 2021/1/5 17:57
 * @description 巨型虫族活动定时器
 */
public class BigMonsterTimer extends TimerEvent {

    public BigMonsterTimer() {
        super(-1, TimeHelper.SECOND_MS);
    }

    @Override
    public void action() {
        SpringUtil.getBean(BigMonsterManager.class).checkBigMonster();
    }
}
