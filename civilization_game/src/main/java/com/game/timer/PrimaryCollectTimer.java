package com.game.timer;


public class PrimaryCollectTimer extends TimerEvent {

	public PrimaryCollectTimer() {
		super(-1, 1000L);
	}

	@Override
	public void action() {
		//  SpringUtil.getBean(WorldService.class).checkPrimaryCollect();
	}

}