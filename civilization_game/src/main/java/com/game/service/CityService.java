package com.game.service;

import com.game.domain.Award;
import com.game.domain.CountryData;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.WorldPb;
import com.game.util.PbHelper;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.domain.Player;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;

@Service
public class CityService {

	@Autowired
	private CityManager cityManager;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticMonsterMgr staticMonsterMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private BattleMailManager battleMailMgr;

	@Autowired
	private StealCityManager stealCityManager;

	@Autowired
	private StaticWorldActPlanMgr worldActPlanMgr;

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private StaticPropMgr staticPropMgr;
	@Autowired
	private RankManager rankManager;
	@Autowired
	private CountryManager countryManager;

	// 检查每个城池的自动回复以及参选情况
	public void checkCityLogic() {
		long now = System.currentTimeMillis();
		checkElection(now); // 检查城池的参选情况,按照爵位和时间排序
		checkCitySolider(now); // 城池回血
		checkLevel(); // 检查都城
		for (City city : cityManager.getCityMap().values()) {
			sendCityMail(now, city); // 给城主发邮件
			makeItem(now, city); // 生产道具
			checkCityOwner(now, city); // 检查城主过期时间
			refreshCityProtective(now, city); // 推送保护罩过期时间
			cityChange(now, city);
		}
		cityRemarkChange(now);
		stealCityManager.checkStealCityOpen();
	}

	public void cityRemarkChange(long now) {
		WorldData worldData = worldManager.getWolrdInfo();
		Map<Integer, CityRemark> cityRemarkMap = worldData.getCityRemarkMap();
		Iterator<CityRemark> iterator = cityRemarkMap.values().iterator();
		while (iterator.hasNext()) {
			CityRemark next = iterator.next();
			if (now > next.getNextTime()) {
				iterator.remove();
				City city = cityManager.getCity(next.getCityId());
				synCityMapToPlayer(city);
			}
		}
	}

	// 重置城池
	public void cityChange(long now, City city) {
		if (city.getCityType() == 7 && city.getCityTime() != 0 && now >= city.getCityTime()) {
			city.setCityTime(0);
			int famousCityNum = cityManager.getFamousCityNum(city.getCountry());
			if (famousCityNum > 4 && city.getLordId() == 0) {
				city.reset();
				cityManager.handleCityMonster(city);
				synCityMapToPlayer(city);
			}
		}
	}

	public void checkCityOwner(long now, City city) {
		if (city == null) {
			LogHelper.CONFIG_LOGGER.info("city is null in check election!");
			return;
		}

		if (city.getLordId() == 0) {
			return;
		}

		if (city.getEndTime() > now) {
			return;
		}

		handleCityOver(city);
	}

	public void checkElection(long now) {
		// cityId, election data
		ConcurrentHashMap<Integer, Map<Long, CityElection>> cityElection = cityManager.getCityElectionMap();
		if (cityElection.isEmpty()) {
			return;
		}

		// 检查超时
		Iterator<Map<Long, CityElection>> iterator = cityElection.values().iterator();
		while (iterator.hasNext()) {
			Map<Long, CityElection> cityElectionMap = iterator.next();
			if (cityElectionMap == null || cityElectionMap.isEmpty()) {
				iterator.remove();
				continue;
			}

			int cityId = 0;
			for (CityElection data : cityElectionMap.values()) {
				if (data == null) {
					continue;
				}

				cityId = data.getCityId();
				break;
			}

			City city = cityManager.getCity(cityId);
			if (city == null) {
				continue;
			}

			if (city.getElectionEndTime() > now) {
				continue;
			}

			List<ElectionCompare> assitList = new ArrayList<ElectionCompare>();
			for (CityElection info : cityElectionMap.values()) {
				ElectionCompare electionCompare = cityManager.createElectionCompare(info);
				if (electionCompare == null) {
					// LogHelper.CONFIG_LOGGER.info("electionCompare == null!!");
					continue;
				}
				assitList.add(electionCompare);
			}

			if (assitList.isEmpty()) {
				handleNoOneCityOwn(city, cityElectionMap);
				cityElectionMap.clear();
				// 参战人员全部清除
				HashSet<Long> warAttenders = cityManager.getWarAttenders(cityId);
				if (warAttenders != null && !warAttenders.isEmpty()) {
					warAttenders.clear();
				}
				worldManager.synMapCity(0, cityId);
			} else {
				Collections.sort(assitList);
				//ElectionCompare electionCompare = assitList.get(0);
				// handleCityOwn(city, electionCompare, cityElectionMap);
				handleCityOwn(city, assitList);
				cityElectionMap.clear();

				// 参战人员全部清除
				HashSet<Long> warAttenders = cityManager.getWarAttenders(cityId);
				if (warAttenders != null && !warAttenders.isEmpty()) {
					warAttenders.clear();
				}
				worldManager.synMapCity(0, cityId);
			}
			iterator.remove();
		}
	}

	public void handleCityOver(City city) {
		if (city.getLordId() != 0) {
			Player player = playerManager.getPlayer(city.getLordId());
			if (player != null) {
				int mapId = worldManager.getMapId(player);
				worldManager.synMapCity(mapId, city.getCityId());
			}

			cityManager.clearCityLordId(city);
			city.setEndTime(0);
			StaticWorldCity worldCity = staticWorldMgr.getCity(city.getCityId());
			// 野怪也血量也清零
			CityMonster cityMonster = cityManager.getCityMonster(city.getCityId());
			if (cityMonster != null) {
				Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
				if (monsterInfoMap != null) {
					for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
						cityMonsterInfo.setSoldier(0);
					}
				}
			}
		}
	}

	public void handleCityOwn(City city, List<ElectionCompare> assitList) {
		Iterator<ElectionCompare> iterator = assitList.iterator();
		ElectionCompare electionCompare = null;
		Player owner = null;// 当选者
		int index = 0;
		while (iterator.hasNext()) {
			electionCompare = iterator.next();
			long lordId = electionCompare.getLordId();
			owner = playerManager.getPlayer(lordId);
			if (owner == null || owner.getCityId() != 0) {
				electionCompare = null;
				continue;
			}
			if (electionCompare != null) {
				break;
			}
		}
		//选出城主
		if (owner != null && electionCompare != null) {
			owner.setCityId(city.getCityId());
			city.setLordId(owner.roleId);
			// 给玩家发送一封当上的邮件
			battleMailMgr.sendElectionOk(owner, city.getCityId());
			cityManager.handleRecSoldier(city.getCityId()); // 兵力回满
			StaticWorldCity worldCity = staticWorldMgr.getCity(city.getCityId());
			// TODO 给当前玩家推送兵力
			worldManager.synMapCity(worldCity.getMapId(), city.getCityId(), owner);
			index = assitList.indexOf(electionCompare);
		}
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(city.getCityId());
		long period;
		if (staticWorldCity == null) {
			period = 240 * TimeHelper.HOUR_MS;
		} else {
			period = staticWorldCity.getOwnPeriod() * TimeHelper.HOUR_MS;
		}
		city.setEndTime(System.currentTimeMillis() + period);
		for (int i = 0; i < assitList.size(); i++) {
			ElectionCompare electionCompare1 = assitList.get(0);
			if (electionCompare != null && electionCompare == electionCompare1) {
				continue;
			}
			int mailId = MailId.CITY_ELECTION_FAILED;
			//排位在当选择之前的 || 没任何人当选  就发143
			//之后的 就发 27
			if (i < index || electionCompare == null) {
				mailId = MailId.CITY_ELECTIONOTHER_FAILED;
			}
			Player player = playerManager.getPlayer(electionCompare1.getLordId());
			if (player != null) {
				battleMailMgr.sendElectionFailed(player, owner, electionCompare1.getAwards(), city.getCityId(), mailId);
			}
		}
		// 设置城主的邮件发放时间
		cityManager.handleCityAwardTime(city);
	}

	// 没人当上城主
	public void handleNoOneCityOwn(City city, Map<Long, CityElection> electionMap) {
		// 没当上城主的发邮件
		for (CityElection cityElection : electionMap.values()) {
			if (cityElection == null) {
				continue;
			}

			Player player = playerManager.getPlayer(cityElection.getLordId());
			if (player == null) {
				LogHelper.CONFIG_LOGGER.info("player is null!!!");
				continue;
			}

			battleMailMgr.sendElectionFailed(player, player, cityElection.copyAwards(), city.getCityId(), MailId.CITY_ELECTION_FAILED);
		}
	}

	// 城池野怪兵力回复=总兵力
	public void checkCitySolider(long now) {
		ConcurrentHashMap<Integer, CityMonster> cityMonsterMap = cityManager.getCityMonsterMap();
		for (CityMonster cityMonster : cityMonsterMap.values()) {
			if (cityMonster == null) {
				continue;
			}

			int cityId = cityMonster.getCityId();
			City city = cityManager.getCity(cityId);
			if (city == null) {
				continue;
			}

			if (cityId == 25) {
				continue;
			}

			// npc城池才能恢复
			if (city.getCountry() != 0) {
				continue;
			}

			// 只有npc城池可以恢复
			recoverCityMonster(cityMonster, now);

		}
	}

	// 恢复城池怪物
	public void recoverCityMonster(CityMonster cityMonster, long now) {
		if (cityMonster.getLastReoverTime() > now) {
			return;
		}

		if (cityMonster.isFullHp()) {
			return;
		}

		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityMonster.getCityId());
		if (staticWorldCity == null) {
			LogHelper.CONFIG_LOGGER.info("staticWorldCity is null!");
			return;
		}

		/*
		 * if (staticWorldCity.getType() == CityType.FAMOUS_CITY || staticWorldCity.getType() == CityType.SQUARE_FORTRESS) { return; }
		 */
		// 跟策划确认名城是可以中立的
		if (staticWorldCity.getType() == CityType.SQUARE_FORTRESS) {
			return;
		}

		cityMonster.setLastReoverTime(now + staticWorldCity.getRecoverTime() * TimeHelper.SECOND_MS);
		List<Integer> monsterIds = cityMonster.getMonsterIds();
		int totalSodiler = 0;
		for (Integer monsterId : monsterIds) {
			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
			if (staticMonster == null) {
				continue;
			}
			totalSodiler += staticMonster.getSoldierCount();
		}

		// 恢复速度
		int recoverSoldier = staticWorldCity.getRecoverSoldier();
		double percent = (double) recoverSoldier / 100.0;
		int soldierAdd = (int) (percent * (double) totalSodiler);
		soldierAdd = Math.max(0, soldierAdd);
		int totalCount;

		Map<Integer, CityMonsterInfo> cityMonsterMap = cityMonster.getMonsterInfoMap();
		for (CityMonsterInfo monsterInfo : cityMonsterMap.values()) {
			int current = monsterInfo.getSoldier();
			totalCount = current + soldierAdd;
			if (totalCount < monsterInfo.getMaxSoldier()) {
				monsterInfo.setSoldier(totalCount);
			} else {
				monsterInfo.setSoldier(monsterInfo.getMaxSoldier());
				soldierAdd -= (monsterInfo.getMaxSoldier() - current);
			}
		}

	}

	// 给城主发邮件
	public void sendCityMail(long now, City city) {
		if (city == null) {
			return;
		}

		if (city.getSendAwardTime() > now) {
			return;
		}

		long lordId = city.getLordId();
//			if (lordId <= 0) {
//                cityManager.handleSetAwardTime(city);
//                continue;
//            }

		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return;
		}

		if (player.getCityId() != city.getCityId()) {
			city.setLordId(0L);
			return;
		}

		List<Award> awards = cityManager.getAwards(city.getCityId());
		// 由于您占据%s%s，百姓在21点生产出下列物资，献于城主大人您，您作为本城城主的任期还剩%s天。
		int cityId = city.getCityId();
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);

		long leftTime = city.getEndTime() - now;
		leftTime = Math.max(0, leftTime);
		int leftDay = (int) (leftTime / TimeHelper.DAY_MS);
		if (leftTime % TimeHelper.DAY_MS != 0) {
			leftDay += 1;
		}

		if (worldCity != null) {
			String params[] = {String.valueOf(worldCity.getMapId()), String.valueOf(cityId), String.valueOf(leftDay)};
			playerManager.sendAttachMail(player, awards, MailId.CITY_OWNER_AWARD, params);
		}

		int sendTime = staticLimitMgr.getNum(46);
		long nighHour = TimeHelper.getCityMailTime(sendTime);
		city.setSendAwardTime(nighHour + TimeHelper.DAY_MS);

		// 结束城主
		if (city.getEndTime() <= now) {
			cityManager.clearCityLordId(city);
		}
	}

	public long getMakePeriod(int cityId) {
		/*
		 * StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId); if (staticWorldCity == null) { LogHelper.CONFIG_LOGGER.info("staticWorldCity is null!"); return 3600000L; } long period = staticWorldCity.getPeriod(); return period * TimeHelper.SECOND_MS;
		 */
		return cityManager.getMakePeriod(cityId);
	}

	// 生产道具
	public void makeItem(long now, City city) {
		if (city == null) {
			return;
		}

		if (city.getCountry() == 0) {
			return;
		}

		// 抢夺名城的城池不继续生产
		if (city.getState() != CityState.COMMON_MAKE_ITEM) {
			return;
		}

		// 8和9的城池不生产道具
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(city.getCityId());
		if (staticWorldCity == null) {
			return;
		}
		int cityType = staticWorldCity.getType();
		if (cityType == 8 || cityType == 9) {
			return;
		}

		long period = getMakePeriod(city.getCityId());
		if (city.getMakeItemTime() <= 0) {
			city.setMakeItemTime(now + period);
			return;
		}

		if (city.getMakeItemTime() > now) {
			return;
		}

		int maxNum = worldManager.getCityMaxAward(city.getCountry());
		if (city.getAwardNum() >= maxNum) {
			city.setMakeItemTime(now + period);
			return;
		}

		city.setAwardNum((byte) (city.getAwardNum() + 1));
		city.setMakeItemTime(now + period);
		// 向在在线玩家同步征收信息
		synCityMapToPlayer(city);
	}

	// 检查四方要塞的等级
	public void checkLevel() {
		boolean isSquareLevelFull = cityManager.isSquareLevelFull();
		if (isSquareLevelFull) {
			return;
		}

		List<Integer> squareCityId = cityManager.getSquareFortress();
		// 检查群雄的城池是否升级
		int country = 0; // 国家个数
		int minLevel = 3;
		for (Integer cityId : squareCityId) {
			City city = cityManager.getCity(cityId);
			if (city == null) {
				continue;
			}

			if (city.getCountry() == 0) {
				continue;
			}

			country += 1;
			if (city.getCityLv() < minLevel) {
				minLevel = city.getCityLv();
			}
		}

		// 至少保证有3个国家有四方要塞
		if (country != 3) {
			return;
		}

		for (Integer cityId : squareCityId) {
			City city = cityManager.getCity(cityId);
			if (city == null) {
				continue;
			}

			if (city.getCountry() == 0 && city.getCityLv() < minLevel) {
				city.setCityLv(minLevel);
				worldManager.synCityDev(city);
				break;
			}
		}
	}

	/**
	 * 领取抢夺名城的奖励
	 *
	 * @param req
	 */
	public void getStealCityAward(WorldPb.GetStealCityAwardRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		if (!req.hasCityId()) {
			handler.sendErrorMsgToPlayer(GameError.NO_CITY);
			return;
		}

		int cityId = req.getCityId();
		City city = cityManager.getCity(cityId);
		if (city == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CITY);
			return;
		}
		if (city.getCountry() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CITY_COUNTRY_ERROR);
			return;
		}

		int country = player.getCountry();
		if (country != city.getCountry()) {
			handler.sendErrorMsgToPlayer(GameError.CITY_COUNTRY_ERROR);
			return;
		}

		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int cityType = staticWorldCity.getType();
		if (cityType == 8 || cityType == 9) {
			handler.sendErrorMsgToPlayer(GameError.CITY_TYPE_ERROR);
			return;
		}

		// 不能跨区域征收
		int mapId = worldManager.getMapId(player);
		if (staticWorldCity.getMapId() != mapId) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_COLLECT_PAPER);
			return;
		}

		StaticWorldMap staticMap = staticWorldMgr.getStaticWorldMap(mapId);
		if (staticMap == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		StaticActStealCity config = stealCityManager.getConfig();
		if (null == config) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		WorldData worldData = worldManager.getWolrdInfo();
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_6);
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_6);
		if (worldActPlan == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		WorldTargetTask worldTargetTask = worldData.getTasks().get(staticWorldActPlan.getTargetId());
		if (worldTargetTask == null) {
			if (worldActPlan != null) {
				worldData.getWorldActPlans().remove(worldActPlan.getId());
			}
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		if (worldActPlan.getOpenTime() == 0) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		if (worldActPlan.getState() != WorldActPlanConsts.OPEN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		Award stealCityAward = stealCityManager.getStealCityAward(cityId);
		int state = city.getState();
		if (state != CityState.RED_MAKE_DONE) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		synchronized (this) {
			byte num = city.getAwardNum();
			if (num <= 0) {
				handler.sendErrorMsgToPlayer(GameError.STEAL_CITY_IS_OVER);
				return;
			}

			boolean receiveAward = stealCityManager.isReceiveAward(player, cityId);
			if (receiveAward) {
				handler.sendErrorMsgToPlayer(GameError.STEAL_CITY_IS_AWARD);
				return;
			}
			stealCityManager.receiveAward(player, cityId);

			WorldPb.GetStealCityAwardRs.Builder builder = WorldPb.GetStealCityAwardRs.newBuilder();
			playerManager.addAward(player, stealCityAward.getType(), stealCityAward.getId(), stealCityAward.getCount(), Reason.STEAL_CITY);
			builder.addAward(PbHelper.createAward(stealCityAward.getType(), stealCityAward.getId(), stealCityAward.getCount()));
			city.setAwardNum((byte) (num - 1));
			builder.setAwardTimes(city.getAwardNum());
			handler.sendMsgToPlayer(WorldPb.GetStealCityAwardRs.ext, builder.build());

			String propName = staticPropMgr.getStaticProp(stealCityAward.getId()) == null ? "" : staticPropMgr.getStaticProp(stealCityAward.getId()).getPropName();

			boolean chat = stealCityManager.isChat(stealCityAward);
			if (chat) {
				chatManager.sendWorldChat(ChatId.STEAL_CITY, String.valueOf(player.getCountry()), player.getNick(), propName);
			}

		}
	}

	/**
	 * 获取当前选中的名城
	 *
	 * @param req
	 * @param handler
	 */
	public void getNowStealCityRq(WorldPb.GetNowStealCityRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		WorldData worldData = worldManager.getWolrdInfo();
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_6);
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_6);
		if (worldActPlan == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		WorldTargetTask worldTargetTask = worldData.getTasks().get(staticWorldActPlan.getTargetId());
		if (worldTargetTask == null) {
			if (worldActPlan != null) {
				worldData.getWorldActPlans().remove(worldActPlan.getId());
			}
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		if (worldActPlan.getOpenTime() == 0) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		if (worldActPlan.getState() != WorldActPlanConsts.OPEN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		/**
		 * 当前活动到第级阶段
		 */
		int index = stealCityManager.queryIndex();
		TreeSet<Integer> stealCityByIndex = stealCityManager.getStealCityByIndex(index);
		if (stealCityByIndex != null) {
			WorldPb.GetNowStealCityRs.Builder builder = WorldPb.GetNowStealCityRs.newBuilder();
			for (Integer id : stealCityByIndex) {
				if (id == null) {
					continue;
				}
				City city = cityManager.getCity(id);
				if (city == null) {
					continue;
				}
				int state = city.getState();
				CommonPb.StealCityInfoItem.Builder item = CommonPb.StealCityInfoItem.newBuilder();
				item.setCityId(city.getCityId());
				item.setEndTime(city.getMakeItemTime());
				item.setProtectedTime(city.getProtectedTime());
				item.setAwardNum(city.getAwardNum());
				item.setState(state);
				item.setCountry(city.getCountry());
				item.setPeriod(TimeHelper.HOUR_MS);
				builder.addStealCityInfoItem(item);
			}
			handler.sendMsgToPlayer(WorldPb.GetNowStealCityRs.ext, builder.build());
		}
	}

	public void refreshCityProtective(long currentTime, City city) {
		if (!city.isRefreshProtected() && city.getProtectedTime() < currentTime) {
			city.setRefreshProtected(true);
			synCityMapToPlayer(city);
		}
	}

	public void synCityMapToPlayer(City city) {
		WorldPb.SynMapCityRq.Builder builder = WorldPb.SynMapCityRq.newBuilder();
		CommonPb.CityOwnerInfo.Builder cityOwner = worldManager.createCityOwner(city);
		WorldData worldData = worldManager.getWolrdInfo();
		playerManager.getOnlinePlayer().forEach(e -> {
			CityRemark cityRemark = worldData.getCityRemark(e.getCountry());
			if (cityRemark != null && cityRemark.getCityId() == city.getCityId()) {
				cityOwner.setCityRemark(cityRemark.encode());
			}
			builder.setInfo(cityOwner);
			playerManager.synMapCityRq(e, builder.build());
		});
	}



	public void cityRemark(WorldPb.CityRemarkRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		WorldData worldData = worldManager.getWolrdInfo();
		List<Lord> rankList = rankManager.getCountryRankList(player.getCountry());
		int myRank = rankManager.getMyRank(rankList, player);
		CountryData country = countryManager.getCountry(player.getCountry());
		int governId = country.getGovernId(player.getRoleId());
		if (myRank > 3 && (governId == 0 || governId > CountryConst.GOVERN_ADVISER)) {
			handler.sendErrorMsgToPlayer(GameError.REMARK_ERROR);
			return;
		}
		int cityId = rq.getCityId();
		City city = cityManager.getCity(cityId);
		if (city == null) {
			return;
		}
		int type = rq.getType();
		WorldPb.CityRemarkRs.Builder builder = WorldPb.CityRemarkRs.newBuilder();
		CityRemark cityRemark;
		int mailId = MailId.REMARK_MAIL_157;
		AtomicInteger remarkCount = worldData.getRemarkMap().get(player.getCountry());
		if (type == 1) {
			int oldCity = 0;
			int i = remarkCount.get();
			if (i <= 0) {
				handler.sendErrorMsgToPlayer(GameError.COUNT_ERROR);
				return;
			}
			String remark = rq.getRemark();
			int remarkHour = rq.getRemarkHour();
			int remarkMin = rq.getRemarkMin();
			LocalDateTime now = LocalDateTime.now();
			int hour = now.getHour();
			int minute = now.getMinute();
			int day;
			// 如果时间大于
			if (remarkHour > hour) {
				day = 0;
			} else {
				day = 1;
				if (remarkHour == hour && remarkMin > minute) {
					day = 0;
				}
			}
			long remarkTime = TimeHelper.getRemarkTime(System.currentTimeMillis(), day, remarkHour, remarkMin);// 时间
			cityRemark = worldData.getCityRemark(player.getCountry());
			if (cityRemark == null) {
				cityRemark = new CityRemark();

			} else {
				if (cityRemark.getCityId() != cityId) {
					oldCity = cityRemark.getCityId();
				}
			}
			cityRemark.setCountry(player.getCountry());
			cityRemark.setRemarkHour(remarkHour);
			cityRemark.setRemarkMin(remarkMin);
			cityRemark.setCityId(cityId);
			cityRemark.setMsg(remark);
			cityRemark.setRemarkTime(remarkTime);
			int num = staticLimitMgr.getNum(SimpleId.CITY_REMARK_TIME);
			cityRemark.setNextTime(TimeHelper.curentTime() + num);
			worldData.getCityRemarkMap().put(cityRemark.getCountry(), cityRemark);
			remarkCount.decrementAndGet();
			builder.setRemark(remark);
			builder.setRemarkTime(remarkTime);
			builder.setRemarkHour(remarkHour);
			builder.setRemarkMin(remarkMin);
			if (oldCity != 0) {
				City city1 = cityManager.getCity(oldCity);
				synCityMapToPlayer(city1);
			}
		} else {
			cityRemark = worldData.getCityRemarkMap().remove(player.getCountry());
			mailId = MailId.REMARK_MAIL_158;
		}
		builder.setCityId(cityId);
		builder.setCount(remarkCount.get());
		builder.setCountry(player.getCountry());
		handler.sendMsgToPlayer(WorldPb.CityRemarkRs.ext, builder.build());
		synCityMapToPlayer(city);
		if (cityRemark != null) {
			String remark[] = { player.getNick(),cityRemark.getRemarkHour() + ":" + cityRemark.getRemarkMin(), cityRemark.getMsg(), String.valueOf(city.getMapId()), String.valueOf(city.getCityId()), city.getPos().toPosStr() };
			Map<Long, Player> players = playerManager.getPlayers();
			int finalMailId = mailId;
			players.values().stream().filter(x -> x.getCountry() == player.getCountry() && x.getLevel() > 30).forEach(y -> {
				playerManager.sendNormalMail(y, finalMailId, remark);
			});
		}
	}
}
