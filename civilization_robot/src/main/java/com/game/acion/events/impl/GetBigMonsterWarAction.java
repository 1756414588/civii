package com.game.acion.events.impl;

import com.game.acion.EventAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.AttackBigMonsterEvent;
import com.game.domain.Robot;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.BigMonsterWar;
import com.game.pb.CommonPb.Pos;
import com.game.pb.CommonPb.WarInfo;
import com.game.pb.WorldPb.GetBigMonsterWarRs;
import com.game.server.TimerServer;

/**
 *
 * @Description
 * @Date 2022/10/27 16:07
 **/

public class GetBigMonsterWarAction extends EventAction {

	private int x;
	private int y;

	public GetBigMonsterWarAction(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		if (base.getCode() != 200) {
			return;
		}

		// 巨型虫族战斗
		GetBigMonsterWarRs getBigMonsterWarRs = base.getExtension(GetBigMonsterWarRs.ext);

		// 巨型虫族战斗
		WarInfo warInfo = null;
		for (BigMonsterWar bigMonsterWar : getBigMonsterWarRs.getWarInfoList()) {
			Pos pos = bigMonsterWar.getWarInfo().getPos();
			if (pos.getX() == x && pos.getY() == y) {
				warInfo = bigMonsterWar.getWarInfo();
				break;
			}
		}

		if (warInfo == null) {
			return;
		}

		// 攻击巨型虫族
		AttackBigMonsterEvent attackBigMonsterEvent = new AttackBigMonsterEvent(robot, new EventAction(), 100, x, y);
		TimerServer.getInst().addDelayEvent(attackBigMonsterEvent);
	}
}
