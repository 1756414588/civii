package com.game.timer;

import com.game.service.CountryService;
import com.game.spring.SpringUtil;

// 同步国家排行榜
public class SynGloryRankTimer extends TimerEvent {

	public SynGloryRankTimer() {
		super(-1, 600000L);
	}

	@Override
	public void action() {
		SpringUtil.getBean(CountryService.class).synRankData();
	}

}