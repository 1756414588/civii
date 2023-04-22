package com.game.timer;

import com.game.acion.EventAction;
import com.game.acion.events.AttackCountryWarEvent;
import com.game.cache.UserWarCache;
import com.game.cache.WorldCacheManager;
import com.game.define.AppTimer;
import com.game.domain.Robot;
import com.game.domain.WarCache;
import com.game.domain.p.RobotData;
import com.game.manager.LoginManager;
import com.game.manager.RobotManager;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;
import com.game.util.Pair;
import java.util.Iterator;
import java.util.List;

@AppTimer(desc = "玩家参与城战")
public class AttackCountryWarTimer extends TimerEvent {

	public AttackCountryWarTimer() {
		super(-1, 5000);
	}

	@Override
	public void action() {
		WorldCacheManager worldCacheManager = SpringUtil.getBean(WorldCacheManager.class);
		RobotManager robotManager = SpringUtil.getBean(RobotManager.class);
		LoginManager loginManager = SpringUtil.getBean(LoginManager.class);

		long curTime = System.currentTimeMillis();
		Iterator<WarCache> it = worldCacheManager.getWarCache().values().iterator();

		List<RobotData> sleepRobots = loginManager.getSleepRobots();
		if (sleepRobots == null || sleepRobots.isEmpty()) {
			return;
		}

		int index = 0;
		int total = sleepRobots.size();

		while (it.hasNext()) {
			WarCache warCache = it.next();

			// 已经结束
			if (warCache.getEndTime() < curTime) {
				it.remove();
			}

			// 非玩家发起的国战
			if (warCache.getAttackCountry() < 1 || warCache.getAttackCountry() > 3) {
				continue;
			}

			int warId = (int) warCache.getWarId();

			//
			for (Pair<Long, Boolean> e : warCache.getAttackerList()) {
				if (e.getLeft() > curTime || e.getRight()) {
					continue;
				}

				for (int i = index; i < total; i++) {
					RobotData robotData = sleepRobots.get(i);
					Robot robot = robotManager.getRobotByKey(robotData.getAccountKey());
					if (robot == null) {
						continue;
					}

					UserWarCache userWarCache = robot.getCache().getUserWarCahce();
					if (userWarCache.isAttend(warId)) {// 已经处理过
						continue;
					}

					e.setRight(true);// 已处理
					attackCountryWar(warCache, robot);
					index = i;
				}
			}
		}
	}

	private void attackCountryWar(WarCache warCache, Robot robot) {
		AttackCountryWarEvent event = new AttackCountryWarEvent(robot, new EventAction(), 100, (int) warCache.getWarId(), warCache.getMapId());
		TimerServer.getInst().addDelayEvent(event);
	}


}
