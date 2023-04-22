package com.game.timer;

import com.game.manager.BattleManager;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 战斗定时器
 */
public class BattleTimer extends TimerEvent {

	public BattleTimer() {
		super(-1, TimeHelper.SECOND_MS);
	}

	@Override
	public void action() {
		BattleManager battleManager = SpringUtil.getBean(BattleManager.class);
		battleManager.battleTimer();
	}
}
