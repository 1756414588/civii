package com.game.worldmap.fight.process;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticHeroMgr;
import com.game.define.Fight;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.Hero;
import com.game.domain.p.Team;
import com.game.domain.p.WorldMap;
import com.game.domain.s.StaticWorldResource;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Pos;
import com.game.worldmap.Resource;
import com.game.worldmap.WorldLogic;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Fight(warName = "采集资源", warType = {}, marthes = {MarchType.CollectResource})
@Component
public class CollectResourceProcess extends FightProcess {

	@Autowired
	private WorldLogic worldLogic;
	@Autowired
	private StaticHeroMgr staticHeroMgr;
	@Autowired
	ActivityEventManager activityEventManager;
	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;

		//注册行军
		registerMarch(MarchType.CollectResource, MarchState.Begin, this::marchArrive);
		registerMarch(MarchType.CollectResource, MarchState.Collect, this::doCollectResource);
		registerMarch(MarchType.CollectResource, MarchState.Back, this::doFinishedMarch);
	}

	/**
	 * 行军抵达
	 *
	 * @param mapInfo
	 * @param march
	 */
	private void marchArrive(MapInfo mapInfo, March march) {
		Entity entity = mapInfo.getEntity(march.getEndPos());
		if (entity == null) {// 目标点丢失, 发送邮件
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.RESOURCE_ENTITY_NULL);
			return;
		}

		if (!(entity instanceof Resource)) {// 目标点丢失, 发送邮件
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.NOT_RESOURCE_TYPE);
			return;
		}

		Resource resource = (Resource) entity;
		if (resource.getCount() <= 0) {
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.RESOURCE_COUNT_LESS_ZERO);
			return;
		}

		long lordId = march.getLordId();
		// 找到玩家
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return;
		}

		// 检查资源点上面是否有行军，如果没有，直接采集，如果有
		Pos resourcePos = resource.getPos();
		March hasMarch = mapInfo.getMarch(resourcePos);

		long minTime = getCollectMinTime(resource, march, player);
		if (minTime == Long.MIN_VALUE) {
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.COLLECT_TIME_ERROR);
			return;
		}

		if (hasMarch == null) {
			march.setState(MarchState.Collect);
			march.setEndTime(minTime + System.currentTimeMillis());
			march.setPeriod(minTime);
			worldManager.synMarch(mapInfo.getMapId(), march);
			resource.setPlayer(player);
		} else {
			// 开打
			Team attackerTeam = battleMgr.initPlayerTeam(player, march.getHeroIds(), BattleEntityType.HERO);
			Player defencer = playerManager.getPlayer(hasMarch.getLordId());
			Team defenceTeam = battleMgr.initPlayerTeam(defencer, hasMarch.getHeroIds(), BattleEntityType.HERO);
			// 采集时被攻打
			Random rand = new Random(System.currentTimeMillis());
			battleMgr.doTeamBattle(attackerTeam, defenceTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
			// 处理玩家扣血
			HashMap<Integer, Integer> attackRecMap = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> defenceRecMap = new HashMap<Integer, Integer>();
			worldManager.caculatePlayer(attackerTeam, player, attackRecMap);
			worldManager.caculatePlayer(defenceTeam, defencer, defenceRecMap);
			String[] param = {player.getNick(), defencer.getNick(), resource.getPosStr()};
			if (attackerTeam.isWin()) {
				// 行军时长
				long l = handleDoneRes(resource, player, hasMarch, mapInfo, true);
				// failed,防守失败返回
				worldLogic.handleCollectWar(MailId.COLLECT_BREAK, hasMarch, defencer, player, false, resource, l);

				// defencer被驱逐,此处的结束时间应该去除 之前的采集数量重新计算。
				minTime = getCollectMinTime(resource, march, player);
				march.setState(MarchState.Collect);
				march.setEndTime(minTime + System.currentTimeMillis());
				march.setPeriod(minTime);
				worldManager.synMarch(mapInfo.getMapId(), march);

				playerManager.sendReportMail(player, battleMailManager.createCollectWarReport(attackerTeam, defenceTeam, player, defencer), battleMailManager.createReportMsg(attackerTeam, defenceTeam), MailId.ATK_COLLECT_WIN, new ArrayList<Award>(), attackRecMap, param);
				resource.setPlayer(player);

				achievementService.addAndUpdate(player,AchiType.AT_32,1);
			} else {
				// win, 防守成功
				worldLogic.handleCollectWar(MailId.COLLECT_REPORT, hasMarch, defencer, player, true, resource, worldLogic.getCollectTime(hasMarch));
				marchManager.handleMarchReturn(march, MarchReason.CollectWarFailed);
				// 同步采集状态
				worldManager.synMarch(mapInfo.getMapId(), march);
				playerManager.sendReportMail(player, battleMailManager.createCollectWarReport(attackerTeam, defenceTeam, player, defencer), battleMailManager.createReportMsg(attackerTeam, defenceTeam), MailId.ATK_COLLECT_FAIL, new ArrayList<Award>(), attackRecMap, param);

			}

			worldManager.caculateTeamKill(attackerTeam, lordId);
			worldManager.caculateTeamDefenceKill(defenceTeam);

			playerManager.synChange(player, Reason.COLLECT_RES_WAR);
			playerManager.synChange(defencer, Reason.COLLECT_RES_WAR);
		}
		if (resource.getCount() > 0) {
			List<Entity> list = new ArrayList<>();
			resource.setFlush(1);
			list.add(resource);
			worldManager.synEntityAddRq(list);
			resource.setFlush(0);
		}
//		LogHelper.MESSAGE_LOGGER.info("marchArrive marchType:{} state:{}", march.getMarchType(), march.getState());
	}


	private void doCollectResource(MapInfo mapInfo, March march) {
//		LogHelper.MESSAGE_LOGGER.info("doCollectResource marchType:{} state:{}", march.getMarchType(), march.getState());
		Entity entity = mapInfo.getEntity(march.getEndPos());
		if (entity == null) {
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.RESOURCE_ENTITY_NULL);
			return;
		}

		if (!(entity instanceof Resource)) {
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.NOT_RESOURCE_TYPE);
			return;
		}

		Resource resource = (Resource) entity;
		if (resource.getCount() <= 0) {
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.RESOURCE_COUNT_LESS_ZERO);
			return;
		}

		List<Integer> heroIds = march.getHeroIds();
		if (heroIds == null || heroIds.size() != 1) {
			LogHelper.CONFIG_LOGGER.info("hero is not found!");
			return;
		}

		long lordId = march.getLordId();
		// 找到玩家
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("resource player is null!");
			return;
		}

		int heroId = heroIds.get(0);
		// 计算采集时间
		Hero hero = player.getHero(heroId);
		if (hero == null) {
			LogHelper.CONFIG_LOGGER.info("hero is not found!");
			return;
		}

		// 采集完成
		long collectTime = getCollectTime(march);
		// 采集处理
		handleDoneRes(resource, player, march, mapInfo, false);
		List<Award> awards = march.getAwards();
		long count = 0;
		int type = 0;
		if (awards != null && awards.size() == 1) {
			count = awards.get(0).getCount();
			type = awards.get(0).getType();
		}
		battleMailManager.sendCollectDone(MailId.COLLECT_WIN, resource, collectTime, count, heroId, hero.getHeroLv(), player, false, null);
		activityEventManager.activityTip(EventEnum.COLLECT, player, (int) count, type);
		if (resource.getCount() > 0) {
			List<Entity> list = new ArrayList<>();
			resource.setFlush(1);
			list.add(resource);
			worldManager.synEntityAddRq(list);
			resource.setFlush(0);
		}
//		LogHelper.MESSAGE_LOGGER.info("doCollectResource marchType:{} state:{}", march.getMarchType(), march.getState());
	}


	public long getCollectMinTime(Resource resource, March march, Player player) {
		List<Integer> heroIds = march.getHeroIds();
		if (heroIds == null || heroIds.size() != 1) {
			LogHelper.CONFIG_LOGGER.info("hero is not found!");
			return Long.MIN_VALUE;
		}

		int heroId = heroIds.get(0);
		// caculate time
		Hero hero = player.getHero(heroId);
		if (hero == null) {
			LogHelper.CONFIG_LOGGER.info("hero is not found!");
			return Long.MIN_VALUE;
		}

		// check qulaity
		int quality = staticHeroMgr.getQuality(heroId);
		if (quality == 0) {
			LogHelper.CONFIG_LOGGER.info("quality is error, quality = " + quality + " in getCollectMinTime.");
			return Long.MIN_VALUE;
		}

		// collect time 可以采集的时间
		long limit = worldManager.getCollectPeriod(quality) * TimeHelper.SECOND_MS;
		long leftCount = resource.getCount();
		long minTime = 0;
		// caculate res count
		int resourceId = (int) resource.getId();
		StaticWorldResource config = staticWorldMgr.getStaticWorldResource(resourceId);
		if (config == null) {
			LogHelper.CONFIG_LOGGER.info("config is null, no resourceId found = " + resourceId);
			return Long.MIN_VALUE;
		}

		// per seconds
		BigDecimal speed = new BigDecimal(config.getSpeed()).divide(new BigDecimal(TimeHelper.HOUR_MS), 20, BigDecimal.ROUND_DOWN);
//        float speed = (float) config.getSpeed() / (float) TimeHelper.HOUR_MS;
		// left time
		if (leftCount > 0) {
			long leftTime = new BigDecimal(leftCount).divide(speed, 2, BigDecimal.ROUND_DOWN).longValue();
//            long leftTime = (long) (Math.round((float) leftCount / speed));
			minTime = Math.min(limit, leftTime);
			if (leftTime <= limit) { // 剩余时间 <= 限制时间
				march.setCollectDone(true);
			}
		}
		return minTime;
	}

	public long handleDoneRes(Resource resource, Player player, March march, MapInfo mapInfo, boolean isBreak) {
		long collectTime = worldLogic.getCollectTime(march);
		Award award = worldManager.caculateResCount(march, resource, collectTime, player, isBreak);
		march.addAwards(award);

		// 清除资源坐标
		if (resource.getCount() <= 0) {
//			worldManager.clearResourcePos(mapInfo, march.getEndPos());
//			worldManager.synEntityRemove(resource, mapInfo.getMapId(), resource.getPos());

			mapInfo.clearPos(resource.getPos());
		}
		marchManager.handleMarchReturn(march, MarchReason.CollectDone);
		worldManager.synMarch(mapInfo.getMapId(), march);
		resource.setStatus(0);//采集完成后设置状态为 0 未被采集
		resource.setPlayer(null);

		int count = award.getCount();
		if(count>0){
			achievementService.addAndUpdate(player, AchiType.AT_37,count);
		}
		return collectTime;

	}

	public long getCollectTime(March march) {
		long leftTime = march.getEndTime() - System.currentTimeMillis();
		leftTime = Math.max(0, leftTime);
		long collectTime = march.getPeriod() - leftTime;
		collectTime = Math.max(0, collectTime);

		return collectTime;
	}

	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {

	}
}
