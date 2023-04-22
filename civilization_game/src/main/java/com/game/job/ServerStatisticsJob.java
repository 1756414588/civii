package com.game.job;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.DailyTaskId;
import com.game.constant.SimpleId;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.Player;
import com.game.domain.TitleAward;
import com.game.domain.WorldData;
import com.game.domain.p.SmallCityGame;
import com.game.log.consumer.EventManager;
import com.game.log.domain.EndlessTDErrorLog;
import com.game.log.domain.OnlineLog;
import com.game.manager.*;
import com.game.message.handler.DealType;
import com.game.season.SeasonManager;
import com.game.season.SeasonService;
import com.game.server.GameServer;
import com.game.server.ICommand;
import com.game.server.exec.HttpExecutor;
import com.game.service.ActivityService;
import com.game.service.WorldTargetTaskService;
import com.game.util.HttpUtil;
import com.game.util.HttpUtils;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jyb
 * @date 2020/3/30 15:42
 * @description
 */
@Component
public class ServerStatisticsJob {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private PlayerManager playerManager;

	@Resource
	private ServerManager serverManager;

	@Resource
	private WorldManager worldManager;

	@Value("${gmServerUrl}")
	private String gmUrl;

	@Autowired
	private StaticActivityMgr staticActivityMgr;
	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private ActivityService activityService;
	@Autowired
	SeasonService seasonService;

	@Autowired
	SeasonManager seasonManager;

	/**
	 * 在线人数更新推送
	 */
	private void pushOnlineLog() {
		List<Player> onlineList = playerManager.getOnlinePlayer();
		// 获取所有区服
		Map<Integer, List<Player>> serverMap = playerManager.getAllPlayer().values().parallelStream().collect(Collectors.groupingBy(e -> {
			if (e.account != null) {
				return e.account.getServerId();
			}
			return 1;
		}));
		// 获取所有渠道
		Map<Integer, List<Player>> channelMap = playerManager.getAllPlayer().values().parallelStream().collect(Collectors.groupingBy(e -> {
			if (e.account != null) {
				return e.account.getChannel();
			}
			return 1;
		}));

		List<OnlineLog> results = Lists.newArrayList();
		// 分区服 分渠道获取在线人数
		for (int serverId : serverMap.keySet()) {
			for (int channel : channelMap.keySet()) {
				// 在线人数
				Long onlineNum = onlineList.parallelStream().filter(e -> {
					if (!e.isLogin) {
						return false;
					}
					if (e.account != null) {
						if (e.account.getServerId() == serverId && e.account.getChannel() == channel) {
							return true;
						}
					}
					return false;
				}).count();
				// 渠道注册人数
				Long registerNum = channelMap.get(channel).stream().filter(e -> {
					if (e.account != null) {
						return e.account.getChannel() == channel && e.account.getServerId() == serverId;
					}
					return false;
				}).count();
				OnlineLog onlineLog = OnlineLog.builder().serverId(serverId).channel(channel).onlineNum(onlineNum.intValue()).registerNum(registerNum.intValue()).build();
				results.add(onlineLog);
			}
		}
		try {
			if (results.size() == 0) {
				return;
			}
			String url = gmUrl + "/GM/api/areaOnlineData";
			Map<String, String> map = new HashMap<>();
			map.put("info", JSONObject.toJSONString(results));
			HttpUtil.sendPost(url, map);
		} catch (Exception e) {
			logger.error("上报在线人数异常->[{}]", e);
		}
	}

	/**
	 * 每分钟上报下在线数据
	 */
	@Scheduled(cron = "0 1/1 * * * ? ")
	private void sendLoggerConsumer() {
		Map<Integer, List<Player>> channels = playerManager.getAllPlayer().values().parallelStream().collect(Collectors.groupingBy(e -> {
			if (e.account != null) {
				return e.account.getChannel();
			}
			return 1;
		}));
		Map<Integer, List<Player>> result = playerManager.getOnlinePlayer().parallelStream().collect(Collectors.groupingBy(e -> {
			if (e.account != null) {
				return e.account.getChannel();
			}
			return 1;
		}));
		for (Map.Entry<Integer, List<Player>> channelList : result.entrySet()) {
			Map<String, Object> online = new HashMap<String, Object>();
			online.put("channel", channelList.getKey());
			online.put("server_id", serverManager.getServerId());
			online.put("platform", "android");
			online.put("online_user", channelList.getValue().size());
			online.put("#account_id", serverManager.getServerId());
			if (SpringUtil.getApplicationContext() != null) {
				EventManager manager = SpringUtil.getBean(EventManager.class);
				if (manager != null) {
					SpringUtil.getBean(EventManager.class).online_user_amount(online, serverManager.getServerId(), serverManager.getServerId());
					channels.remove(channelList.getKey());
				}
			}
		}
		for (Integer channel : channels.keySet()) {
			Map<String, Object> online = new HashMap<String, Object>();
			online.put("channel", channel);
			online.put("server_id", serverManager.getServerId());
			online.put("platform", "android");
			online.put("online_user", 0);
			if (SpringUtil.getApplicationContext() != null) {
				EventManager manager = SpringUtil.getBean(EventManager.class);
				if (manager != null) {
					SpringUtil.getBean(EventManager.class).online_user_amount(online, serverManager.getServerId(), serverManager.getServerId());
				}
			}
		}
		pushOnlineLog();
	}

	// 每日转点将特价礼包刷新
	@Scheduled(cron = "1 0 0 * * ?")
	private void activityRewardLogic() {
//		LogHelper.ERROR_LOGGER.error("重新计算活动生成");
		GameServer.getInstance().currentDay = TimeHelper.getCurrentDay();
		GameServer.getInstance().mainLogicServer.addCommand(() -> {
			resetBigMonster();
			PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
			resetPlayer(playerManager);
			// resetTopUpPerson(playerManager);
			// refreshBeauty(playerManager);
			// refEndlessTD(playerManager);
			// refFishing(playerManager);
			activityService.activityRewardLogic();
			pushAct();
			resetCityRemarkCount();
		}, DealType.MAIN);
	}

	private void resetBigMonster() {
		BigMonsterManager bigMonsterManager = SpringUtil.getBean(BigMonsterManager.class);
		SpringUtil.getBean(WorldManager.class).getAllMap().forEach(e -> {
			bigMonsterManager.setKey(bigMonsterManager.getMonsterKillKey(1, e.intValue()), 0);
			bigMonsterManager.setKey(bigMonsterManager.getMonsterKillKey(2, e.intValue()), 0);
			bigMonsterManager.setKey(bigMonsterManager.getMonsterKillKey(3, e.intValue()), 0);

		});
	}

	private void resetPlayer(PlayerManager playerManager) {
		DailyTaskManager dailyTaskManager = SpringUtil.getBean(DailyTaskManager.class);
		playerManager.getOnlinePlayer().forEach(e -> {
			GameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
				@Override
				public void action() {
					playerManager.wrapRoleLoginRs(e);
					dailyTaskManager.record(DailyTaskId.LOGIN, e, 1);
					SmallCityGame smallCityGame = e.getSmallCityGame();
					if (smallCityGame != null) {
						smallCityGame.refushAll();
					}
					playerManager.refreshBeauty(e, true);
					playerManager.refEndlessTD(e);
					playerManager.refFishing(e);

					TitleAward titleAward = e.getTitleAward();
					titleAward.setRecv(0);
				}
			}, DealType.MAIN);
		});
	}

	// private void resetTopUpPerson(PlayerManager playerManager) {
	// DailyTaskManager dailyTaskManager = GameServer.ac.getBean(DailyTaskManager.class);
	// playerManager.getOnlinePlayer().forEach(e -> {
	// GameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
	// @Override
	// public void action() {
	// playerManager.wrapRoleLoginRs(e);
	// dailyTaskManager.record(DailyTaskId.LOGIN, e, 1);
	// SmallCityGame smallCityGame = e.getSmallCityGame();
	// if (smallCityGame != null) {
	// smallCityGame.refushAll();
	// }
	// }
	// }, DealType.MAIN);
	// });
	// }

	// public void refreshBeauty(PlayerManager playerManager) {
	// for (Player player : playerManager.getOnlinePlayer()) {
	// playerManager.refreshBeauty(player, true);
	// }
	// }
	//
	// public void refEndlessTD(PlayerManager playerManager) {
	// for (Player player : playerManager.getOnlinePlayer()) {
	// playerManager.refEndlessTD(player);
	// }
	// }
	//
	// public void refFishing(PlayerManager playerManager) {
	// for (Player player : playerManager.getOnlinePlayer()) {
	// playerManager.refFishing(player);
	// }
	// }

	@Scheduled(cron = "5 0 0,6,12,15,18,21 * * ?")
	private void synAllPlayerTimeLogic() {
		logger.error("synAllPlayerTimeLogic");
		SpringUtil.getBean(WorldTargetTaskService.class).synAllPlayerTime();
	}

	@Scheduled(cron = "0 0 12,21 * * ?")
	private void updateWorldRankLogic() {
		logger.error("updateWorldRankLogic");
		SpringUtil.getBean(WorldTargetTaskService.class).updateWorldRank();
	}

	@Scheduled(cron = "59 59 23 * * FRI")
	private void updateEndlessTDRand() {
		activityService.sendBroodAct();
		GameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				SpringUtil.getBean(TDManager.class).updateEndlessTDRank();
			}
		}, DealType.MAIN);
	}

	//	@Scheduled(cron = "59 59 23 * * FRI")
//	private void senBroodActAward() {
//
//	}
	// 无尽塔防错误日志上报
	public void recordEndlessTDErrorData(EndlessTDErrorLog endlessTDErrorLog) {
		SpringUtil.getBean(HttpExecutor.class).add(() -> {
			String url = gmUrl.substring(0, gmUrl.lastIndexOf(":") + 1) + "1000/api/endlessTDErrorLog.do";
			try {
				HttpUtils.sentPost(url, JSONObject.toJSONString(endlessTDErrorLog));
			} catch (Exception e) {
				LogHelper.ERROR_LOGGER.debug("record EndlessTD ErrorData fail url-->[{}]", url);
			}
		});
	}

	//零点活动相关处理
	public void pushAct() {
		// 推送春节活动红点
		activityManager.pushSpringTips();
		activityManager.sendTDTaskAward();
	}

	@Autowired
	StaticLimitMgr staticLimitMgr;

	private void resetCityRemarkCount() {
		WorldData wolrdInfo = worldManager.getWolrdInfo();
		if (wolrdInfo != null) {
			int num = staticLimitMgr.getNum(SimpleId.CITY_REMARK_COUNT);
			Map<Integer, AtomicInteger> remarkMap = wolrdInfo.getRemarkMap();
			remarkMap.values().forEach(x -> {
				x.set(num);
			});
		}
	}

	@Scheduled(cron = "59 59 23 * * ?")
	private void sendAward() {
		seasonManager.sendSevenRankAward();
	}

	@Scheduled(cron = "59 59 23 * * Tue")
	private void sendSeasonTreasury() {
		seasonManager.sendTreasuryAward();
	}

}
