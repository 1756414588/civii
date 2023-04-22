package com.game.cache;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description
 * @Date 2022/10/21 17:36
 **/

@Getter
@Setter
public class UserJourneyCache {

	private int journeyId; // 征途关卡Id
	private int state; // 关卡状态 0 lock ,1 open, 2 complete
	private int journeyType; // 征途关卡类型

}
