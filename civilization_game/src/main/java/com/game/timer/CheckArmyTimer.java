package com.game.timer;


import com.game.manager.BattleManager;
import com.game.spring.SpringUtil;
import com.game.worldmap.WorldLogic;

// 行军定时器
public class CheckArmyTimer extends TimerEvent {

	public CheckArmyTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		SpringUtil.getBean(BattleManager.class).marchTimer();
	}

}