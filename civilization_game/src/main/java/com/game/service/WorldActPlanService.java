package com.game.service;

import com.game.constant.GameError;
import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticWorldActPlan;
import com.game.flame.FlameWarManager;
import com.game.manager.ActManoeuvreManager;
import com.game.manager.BroodWarManager;
import com.game.manager.PlayerManager;
import com.game.manager.WorldManager;
import com.game.manager.WorldPvpMgr;
import com.game.manager.WorldTargetManager;
import com.game.manager.ZergManager;
import com.game.message.handler.cs.WorldActivityPlanHandler;
import com.game.pb.CommonPb;
import com.game.pb.WorldPb;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jyb
 * @date 2020/3/30 11:00
 * @description
 */
@Service
public class WorldActPlanService {

	@Resource
	private StaticWorldActPlanMgr worldActPlanMgr;

	@Resource
	private WorldManager worldManager;

	@Resource
	private PlayerManager playerManager;

	@Autowired
	private WorldPvpMgr worldPvpMgr;

	@Autowired
	private RoitService roitService;

	@Autowired
	private BroodWarManager broodWarManager;

	@Autowired
	private ZergManager zergManager;

	@Autowired
	private WorldTargetManager worldTargetManager;


	private Logger logger = LoggerFactory.getLogger(getClass());


	//世界进程开启
	public void openWorldTarget(int targetId) {
		List<StaticWorldActPlan> list = worldActPlanMgr.getListByTargetId(targetId);
		for (StaticWorldActPlan staticWorldActPlan : list) {
			if (staticWorldActPlan == null) {
				continue;
			}

			if (!zergManager.isFinsihCond(staticWorldActPlan.getId())) {
				continue;
			}
			// 活动为沙盘演武
			if (staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_14) {
				continue;
			}
			WorldData worldData = worldManager.getWolrdInfo();
			WorldActPlan worldActPlan = new WorldActPlan();
			worldActPlan.setId(staticWorldActPlan.getId());
			worldActPlan.setTargetSuccessTime(System.currentTimeMillis());
			worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
			worldData.getWorldActPlans().put(worldActPlan.getId(), worldActPlan);

			synchronized (worldActPlan) {
				refreshWorldActPlan(worldActPlan);
				initEndTime(worldActPlan);
			}
			logger.info("******世界活动{}  开启时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getOpenTime()));
			logger.info("******世界活动{}  预热时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getPreheatTime()));
			if (worldActPlan.getEndTime() != 0) {
				logger.info("******世界活动{}  结束时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getEndTime()));
			} else {
				logger.info("******世界活动{}  结束时间待定 ***********", staticWorldActPlan.getName());
			}
			logger.info("******世界活动{}  worldActPlan {} ***********", staticWorldActPlan.getName(), worldActPlan.toString());
		}
		syncWorldActivityPlan();
	}

	/***
	 * 刷新一个活动的时间
	 * @param worldActPlan
	 */
	public void refreshWorldActPlan(WorldActPlan worldActPlan) {
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(worldActPlan.getId());

		//如果未开启过  看世界进程的完成情况
		if (worldActPlan.getState() == WorldActPlanConsts.NOE_OPEN) {
			if (worldActPlan.getTargetSuccessTime() == 0) {
				return;
			}
			//预热阶段检查什么时候开始
			if (worldActPlan.getPreheatTime() == 0) {
				if (staticWorldActPlan.getPreheat() != 0) {
					if (worldActPlan.getOpenTime() == 0 && staticWorldActPlan.getOpenWeek() != 0) {
						long date = 0;
						date = TimeHelper.getTime(worldActPlan.getTargetSuccessTime(), staticWorldActPlan.getOpenWeek(), staticWorldActPlan.getWeekTime(),
							staticWorldActPlan.getTime());
						worldActPlan.setOpenTime(date);
					}
					long preheat = TimeHelper.getTime(new Date(worldActPlan.getOpenTime()), staticWorldActPlan.getPreheat());
					worldActPlan.setPreheatTime(preheat);
				} else {
					worldActPlan.setPreheatTime(0);
				}
			}

			//检查是否达到预热的时间点
			if (worldActPlan.getPreheatTime() != 0 && isBefore(new Date(worldActPlan.getPreheatTime()))) {
				worldActPlan.setState(WorldActPlanConsts.PREHEAT);
				syncWorldActivityPlan();
				if (worldActPlan.getId() == WorldActivityConsts.ACTIVITY_12) {
					broodWarManager.openBroodWarCheck();
				}
			}
			//说明已经初始化过了
			if (worldActPlan.getOpenTime() != 0) {
				return;
			}
			long date;
			if (staticWorldActPlan.getOpenWeek() == 0) {
				date = worldActPlan.getTargetSuccessTime();
				//虫族入侵 初始化下等级
				if (worldActPlan.getId() == WorldActivityConsts.ACTIVITY_7) {
					roitService.initRoitMonsterLevel(worldActPlan);
				}
				worldActPlan.setState(WorldActPlanConsts.OPEN);
				syncWorldActivityPlan();
			} else {
				date = TimeHelper.getTime(worldActPlan.getTargetSuccessTime(), staticWorldActPlan.getOpenWeek(), staticWorldActPlan.getWeekTime(),
					staticWorldActPlan.getTime());
			}
			worldActPlan.setOpenTime(date);
		}
		if (worldActPlan.getState() == WorldActPlanConsts.PREHEAT) {
			if (isBefore(new Date(worldActPlan.getOpenTime()))) {
				worldActPlan.setState(WorldActPlanConsts.OPEN);
				syncWorldActivityPlan();
			}
		}
		if (worldActPlan.getState() == WorldActPlanConsts.OPEN) {
			//结束时候检测下一次开启
		}
		if (worldActPlan.getState() == WorldActPlanConsts.END) {
			activityEnd(worldActPlan);
		}
	}

	/**
	 * 活动结束 处理逻辑 (计算下一次活动开启的时间)
	 *
	 * @param worldActPlan
	 */
	public void activityEnd(WorldActPlan worldActPlan) {
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(worldActPlan.getId());
		if (staticWorldActPlan == null || staticWorldActPlan.getRoundType() == 0) {
			return;
		}
		int roundType = worldTargetManager.getRoundType(worldManager.getWolrdInfo(), staticWorldActPlan);
		long date = TimeHelper.getTime(worldActPlan.getEndTime(), roundType, staticWorldActPlan.getWeekTime(),
			staticWorldActPlan.getTime());
		worldActPlan.setOpenTime(date);
		worldActPlan.setEndTime(0);
		//虫族入侵设置未开启不走此处理
		if (worldActPlan.getId() != WorldActivityConsts.ACTIVITY_8) {
			worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
		}
		//检查是否达到预热的时间点
		if (staticWorldActPlan.getPreheat() != 0) {
			long preheat = TimeHelper.getTime(new Date(worldActPlan.getOpenTime()), staticWorldActPlan.getPreheat());
			worldActPlan.setPreheatTime(preheat);
		} else {
			worldActPlan.setPreheatTime(0);
		}
		initEndTime(worldActPlan);
		//检查是否达到预热的时间点
		if (worldActPlan.getPreheatTime() != 0 && isBefore(new Date(worldActPlan.getPreheatTime()))) {
			worldActPlan.setState(WorldActPlanConsts.PREHEAT);
			syncWorldActivityPlan();
		}
		//检查是否达到开启的时间点
		if (isBefore(new Date(worldActPlan.getOpenTime()))) {
			worldActPlan.setState(WorldActPlanConsts.OPEN);
			syncWorldActivityPlan();
		}
		logger.debug("******世界活动{}  开启时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getOpenTime()));
		logger.debug("******世界活动{}  预热时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getPreheatTime()));
		if (worldActPlan.getEndTime() != 0) {
			logger.debug("******世界活动{}  结束时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getEndTime()));
		} else {
			logger.debug("******世界活动{}  结束时间待定 ***********", staticWorldActPlan.getName());
		}
		logger.debug("******世界活动{}  worldActPlan {} ***********", staticWorldActPlan.getName(), worldActPlan.toString());
	}


	/**
	 * 是否在当前时间之前
	 *
	 * @param date 时间
	 * @return
	 */
	public boolean isBefore(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date);
		if (c1.before(c)) {
			return true;
		}
		return false;
	}


	/**
	 * 拿到活动信息
	 *
	 * @param handler
	 */
	public void WorldActivityPlan(WorldActivityPlanHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		WorldData worldData = worldManager.getWolrdInfo();
		WorldPb.WorldActivityPlanRs.Builder builder = WorldPb.WorldActivityPlanRs.newBuilder();
		for (WorldActPlan p : worldData.getWorldActPlans().values()) {

			refreshWorldActPlan(p);
			StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(p.getId());
			if (staticWorldActPlan == null || staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_1 || staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_14 || staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_15) {
				continue;
			}
			WorldPb.WorldActivityPlan.Builder worldActivityPlan = WorldPb.WorldActivityPlan.newBuilder();
			CommonPb.WorldActPlan.Builder worldActPlan = CommonPb.WorldActPlan.newBuilder();
			worldActPlan.setId(p.getId());
			worldActPlan.setPreheatTime(p.getPreheatTime());
			worldActPlan.setOpenTime(p.getOpenTime());
			worldActPlan.setState(p.getState());
			worldActPlan.setEndTime(p.getEndTime());
			worldActivityPlan.setWorldActPlan(worldActPlan);
			worldActivityPlan.addAllPrams(staticWorldActPlan.getContinues());
			builder.addWorldActivityPlan(worldActivityPlan);
		}

		handler.sendMsgToPlayer(WorldPb.WorldActivityPlanRs.ext, builder.build());
	}

	/**
	 * 给所有玩家推送活动消息
	 */
	public void syncWorldActivityPlan() {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldPb.SyncWorldActivityPlan.Builder builder = WorldPb.SyncWorldActivityPlan.newBuilder();
		for (WorldActPlan p : worldData.getWorldActPlans().values()) {
			StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(p.getId());
			if (staticWorldActPlan == null || staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_1 || staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_14) {
				continue;
			}
			WorldPb.WorldActivityPlan.Builder worldActivityPlan = WorldPb.WorldActivityPlan.newBuilder();
			CommonPb.WorldActPlan.Builder worldActPlan = CommonPb.WorldActPlan.newBuilder();
			worldActPlan.setId(p.getId());
			worldActPlan.setPreheatTime(p.getPreheatTime());
			worldActPlan.setOpenTime(p.getOpenTime());
			worldActPlan.setState(p.getState());
			worldActPlan.setEndTime(p.getEndTime());
			worldActivityPlan.setWorldActPlan(worldActPlan);
			worldActivityPlan.addAllPrams(staticWorldActPlan.getContinues());
			builder.addWorldActivityPlan(worldActivityPlan);
		}

		WorldPb.SyncWorldActivityPlan msg = builder.clone().build();

		for (Player player : playerManager.getOnlinePlayer()) {
			SynHelper.synMsgToPlayer(player, WorldPb.SyncWorldActivityPlan.EXT_FIELD_NUMBER, WorldPb.SyncWorldActivityPlan.ext, msg);
		}
	}

	/**
	 * 给指定玩家推送活动消息
	 */
	public void syncWorldActivityPlan(Player player) {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldPb.SyncWorldActivityPlan.Builder builder = WorldPb.SyncWorldActivityPlan.newBuilder();
		for (WorldActPlan p : worldData.getWorldActPlans().values()) {
			StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(p.getId());
			if (staticWorldActPlan == null || staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_1) {
				continue;
			}
			WorldPb.WorldActivityPlan.Builder worldActivityPlan = WorldPb.WorldActivityPlan.newBuilder();
			CommonPb.WorldActPlan.Builder worldActPlan = CommonPb.WorldActPlan.newBuilder();
			worldActPlan.setId(p.getId());
			worldActPlan.setPreheatTime(p.getPreheatTime());
			worldActPlan.setOpenTime(p.getOpenTime());
			worldActPlan.setState(p.getState());
			worldActPlan.setEndTime(p.getEndTime());
			worldActivityPlan.setWorldActPlan(worldActPlan);
			worldActivityPlan.addAllPrams(staticWorldActPlan.getContinues());
			builder.addWorldActivityPlan(worldActivityPlan);
		}

		WorldPb.SyncWorldActivityPlan msg = builder.clone().build();
		SynHelper.synMsgToPlayer(player, WorldPb.SyncWorldActivityPlan.EXT_FIELD_NUMBER, WorldPb.SyncWorldActivityPlan.ext, msg);
	}


	/**
	 * 强制打开母巢之战(清空上一次的数据. 测试用)
	 */
	public void openGmWorldPvp() {
		worldPvpMgr.doGmEndLogic();
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = new WorldActPlan();
		worldActPlan.setId(WorldActivityConsts.ACTIVITY_1);
		worldActPlan.setTargetSuccessTime(System.currentTimeMillis());
		worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
		worldData.getWorldActPlans().put(worldActPlan.getId(), worldActPlan);
		worldActPlan.setOpenTime(worldActPlan.getTargetSuccessTime());
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(worldActPlan.getId());
		if (staticWorldActPlan.getPreheat() != 0) {
			long preheat = TimeHelper.getTime(new Date(worldActPlan.getOpenTime()), staticWorldActPlan.getPreheat());
			worldActPlan.setPreheatTime(preheat);
		}
		logger.info("**openWorldPvp****世界活动{}  开启时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getOpenTime()));
		logger.info("**openWorldPvp****世界活动{}  预热时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getPreheatTime()));
	}

	@Autowired
	FlameWarManager flameWarManager;

	public void initEndTime(WorldActPlan worldActPlan) {
		if (worldActPlan.getOpenTime() != 0) {
			StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(worldActPlan.getId());
			int time = staticWorldActPlan.getEndTime();
			if (staticWorldActPlan.getNomal() == 0) {
				worldActPlan.setEndTime(worldActPlan.getOpenTime() + TimeHelper.MINUTE_MS * time);
			}
			if (worldActPlan.getId() == WorldActivityConsts.ACTIVITY_15) {
				long enterTime = TimeHelper.getTimeMinute(new Date(worldActPlan.getOpenTime()), -5);
				long exhibitionTime = TimeHelper.getTimeMinute(new Date(worldActPlan.getEndTime()), 180);
				worldActPlan.setEnterTime(enterTime);
				worldActPlan.setExhibitionTime(exhibitionTime);
				// flameWarManager.initMap();
			}
		}
	}

	/**
	 * 处理 夜袭虫群 伤兵恢复加成
	 *
	 * @param player
	 * @param solderRecMap
	 * @return
	 */
	public float doActivity5(Player player, HashMap<Integer, Integer> solderRecMap) {
		float percent = 0;
		//夜袭虫群的活动,当有伤兵恢复的次数
		WorldActPlan worldActPlan = worldManager.getWorldActPlan(WorldActivityConsts.ACTIVITY_5);
		if (worldActPlan != null && worldActPlan.getState() == WorldActPlanConsts.OPEN) {
			long lastStrikeSwarm = player.getSimpleData().getLastStrikeSwarm();
			if (lastStrikeSwarm < worldActPlan.getOpenTime()) {
				player.getSimpleData().setStrikeSwarm(0);
			}
			StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(worldActPlan.getId());
			if (player.getSimpleData().getStrikeSwarm() < staticWorldActPlan.getContinues().get(1)) {
				percent = staticWorldActPlan.getContinues().get(0) * 1.0f / 100;
				if (solderRecMap.size() > 0) {
					player.getSimpleData().setStrikeSwarm(player.getSimpleData().getStrikeSwarm() + 1);
					player.getSimpleData().setLastStrikeSwarm(System.currentTimeMillis());
				}
			}
		}
		return percent;
	}

	//兼容世界进程开始进程和当前进程不一致的活动
	public void openWorldAct() {
		// 查找所有活动配置
		Map<Integer, StaticWorldActPlan> plans = worldActPlanMgr.getPlans();
		// 查找所有已经开启活动配置
		WorldData worldData = worldManager.getWolrdInfo();
		Map<Integer, WorldActPlan> worldActPlans = worldData.getWorldActPlans();
		Integer max = Collections.max(worldData.getTasks().keySet());

		Set<Integer> plansSet = plans.keySet();
		Set<Integer> worldActPlansSet = worldActPlans.keySet();

		for (Integer planId : plansSet) {
			if (worldActPlansSet.contains(planId)) {
				// 说明已经世界活动开启过
				continue;
			}

			StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(planId);
			int targetId = staticWorldActPlan.getTargetId();
			if (null != staticWorldActPlan && null != max && targetId <= max) {
				if (!zergManager.isFinsihCond(staticWorldActPlan.getId())) {// 虫族主宰需要额外的条件才能开启
					continue;
				}
				if (staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_14 || staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_15) {
					continue;
				}
				WorldActPlan worldActPlan = new WorldActPlan();
				worldActPlan.setId(staticWorldActPlan.getId());
				worldActPlan.setTargetSuccessTime(System.currentTimeMillis());
				worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
				worldData.getWorldActPlans().put(worldActPlan.getId(), worldActPlan);
				synchronized (worldActPlan) {
					refreshWorldActPlan(worldActPlan);
					initEndTime(worldActPlan);
				}
				LogHelper.GAME_LOGGER.info("******世界活动{}  开启时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getOpenTime()));
				LogHelper.GAME_LOGGER.info("******世界活动{}  预热时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getPreheatTime()));
				if (worldActPlan.getEndTime() != 0) {
					LogHelper.GAME_LOGGER.info("******世界活动{}  结束时间{} ***********", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getEndTime()));
				} else {
					LogHelper.GAME_LOGGER.info("******世界活动{}  结束时间待定 ***********", staticWorldActPlan.getName());
				}
				LogHelper.GAME_LOGGER.info("******世界活动{}  worldActPlan {} ***********", staticWorldActPlan.getName(), worldActPlan.toString());
			}
		}
	}

	public boolean openWorldAct(int activityId) {
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(activityId);
		if (staticWorldActPlan == null) {
			return false;
		}
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(activityId);
		if (worldActPlan == null) {
			worldActPlan = new WorldActPlan();
			worldActPlan.setId(staticWorldActPlan.getId());
			worldActPlan.setTargetSuccessTime(System.currentTimeMillis());
			worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
			worldData.getWorldActPlans().put(worldActPlan.getId(), worldActPlan);
		}
		//10s后开启
		long date = System.currentTimeMillis() + TimeHelper.MINUTE_MS;
		worldActPlan.setOpenTime(date);
		worldActPlan.setEndTime(0);
		worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
		//检查是否达到预热的时间点
		if (staticWorldActPlan.getPreheat() != 0) {
			long preheat = TimeHelper.getTime(new Date(worldActPlan.getOpenTime()), staticWorldActPlan.getPreheat());
			worldActPlan.setPreheatTime(preheat);
		} else {
			worldActPlan.setPreheatTime(0);
		}
		initEndTime(worldActPlan);
		if (worldActPlan.getId() != WorldActivityConsts.ACTIVITY_15) {
			if (worldActPlan.getPreheatTime() != 0 && isBefore(new Date(worldActPlan.getPreheatTime()))) {
				worldActPlan.setState(WorldActPlanConsts.PREHEAT);
				syncWorldActivityPlan();
				if (worldActPlan.getId() == WorldActivityConsts.ACTIVITY_12) {
					broodWarManager.gmOpenBroodWarCheck();
				}
			}
			//检查是否达到开启的时间点
			if (isBefore(new Date(worldActPlan.getOpenTime()))) {
				worldActPlan.setState(WorldActPlanConsts.OPEN);
				syncWorldActivityPlan();
			}
			LogHelper.GAME_LOGGER.info("****GM开启世界活动->[{}] 开启时间->[{}] 预热时间->[{}],结束时间->[{}],worldActPlan->[{}]", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getPreheatTime()), worldActPlan.getEndTime() == 0 ? 0 : DateHelper.getDate(worldActPlan.getEndTime()), worldActPlan.toString());
		}
		LogHelper.GAME_LOGGER.info("****GM开启世界活动->[{}] 开启时间->[{}] 预热时间->[{}],结束时间->[{}],worldActPlan->[{}]",
			staticWorldActPlan.getName(),
			DateHelper.getDate(worldActPlan.getOpenTime()),
			DateHelper.getDate(worldActPlan.getPreheatTime()),
			worldActPlan.getEndTime() == 0 ? 0 : DateHelper.getDate(worldActPlan.getEndTime()),
			worldActPlan.toString()
		);
		return true;
	}


	/**
	 * 圣域争霸检测
	 */
	public void checkBroodWar() {
		/**
		 * 更新夜袭虫群活动的状态
		 */
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_12);
		if (worldActPlan != null) {
			refreshWorldActPlan(worldActPlan);
		}
	}

}