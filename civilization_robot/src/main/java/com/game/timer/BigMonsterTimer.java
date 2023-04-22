package com.game.timer;


import com.game.acion.events.GetBigMonsterWarEvent;
import com.game.acion.events.impl.GetBigMonsterWarAction;
import com.game.cache.ChatCacheManager;
import com.game.cache.ConfigCache;
import com.game.cache.UserHeroCache;
import com.game.cache.UserWarCache;
import com.game.define.AppTimer;
import com.game.domain.ChatShare;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.manager.LoginManager;
import com.game.manager.MessageEventManager;
import com.game.manager.MessageManager;
import com.game.manager.RobotManager;
import com.game.pb.CommonPb.HeroChange;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@AppTimer(desc = "巨型虫族")
public class BigMonsterTimer extends TimerEvent {

	// 10分钟同步一次地图怪物
	public BigMonsterTimer() {
		super(-1, 5000);
	}

	@Override
	public void action() {
		ChatCacheManager chatCacheManager = SpringUtil.getBean(ChatCacheManager.class);
		LoginManager loginManager = SpringUtil.getBean(LoginManager.class);
		RobotManager robotManager = SpringUtil.getBean(RobotManager.class);

		ConfigCache configCache = SpringUtil.getBean(ConfigCache.class);

		long currentTime = System.currentTimeMillis();
		int attendBigMonster = configCache.getIntValue("attend_big_monster");

		Map<Long, ChatShare> shareMap = chatCacheManager.getShares();
		if (shareMap.isEmpty()) {
			return;
		}

		List<RobotData> robotDataList = loginManager.getSleepRobots();
		if (robotDataList.isEmpty()) {
			LogHelper.CHANNEL_LOGGER.info("巨型虫族 机器人在线列表为空");
			return;
		}

		//
		Iterator<ChatShare> it = shareMap.values().iterator();
		while (it.hasNext()) {
			ChatShare next = it.next();
			if (next.getShareTime() < currentTime) {//删除时间到了再删除
				it.remove();
			}

			// 未命中,该次分享不会有协助
			if (next.getRan() > attendBigMonster || next.getAttend() == 1) {
				continue;
			}

			if (next.getDelayTime() > currentTime) {
				continue;
			}

			LogHelper.CHANNEL_LOGGER.info("巨型虫族 机器人准备参与巨型虫族pos:{}", next.getPos());

			// 设至为已参与状态
			if (attendBigMonster(next, robotDataList)) {
				next.setAttend(1);
			}
		}
	}


	/**
	 * 参加巨型虫族
	 */
	private boolean attendBigMonster(ChatShare chatShare, List<RobotData> robotDataList) {

		RobotManager robotManager = SpringUtil.getBean(RobotManager.class);
		Iterator<RobotData> it = robotDataList.stream().filter(e -> e.getCountry() == chatShare.getCountry()).iterator();

		while (it.hasNext()) {
			RobotData next = it.next();

			Robot robot = robotManager.getRobotByKey(next.getAccountKey());
			if (robot == null) {
				LogHelper.CHANNEL_LOGGER.info("巨型虫族 机器人不存在pos:{} robotId:{}", chatShare.getPos(), next.getAccountKey());
				continue;
			}

			// 该机器人没有空闲的武将
			UserHeroCache userHeroCache = robot.getCache().getHeroCache();
			if (userHeroCache.getEmptyEmbattle() == 0) {
				LogHelper.CHANNEL_LOGGER.info("巨型虫族 机器人无空闲武将pos:{} robotId:{}", chatShare.getPos(), next.getAccountKey());
				continue;
			}

			// 已参战不能参战
			UserWarCache userWarCache = robot.getCache().getUserWarCahce();
			if (userWarCache.getBigMonsterWar().containsKey(chatShare.getId())) {//已参与
				LogHelper.CHANNEL_LOGGER.info("巨型虫族 机器人已参与pos:{} robotId:{}", chatShare.getPos(), next.getAccountKey());
				continue;
			}

			userWarCache.getBigMonsterWar().put(chatShare.getId(), 1);

			GetBigMonsterWarEvent messageEvent = new GetBigMonsterWarEvent(robot, new GetBigMonsterWarAction(chatShare.getPosX(), chatShare.getPosY()), 100);
			SpringUtil.getBean(MessageEventManager.class).registerEvent(messageEvent);
			TimerServer.getInst().addDelayEvent(messageEvent);

			LogHelper.CHANNEL_LOGGER.info("巨型虫族 机器人参与巨型虫族pos:{} robotId:{}", chatShare.getPos(), next.getAccountKey());
			return true;
		}
		return false;
	}


}
