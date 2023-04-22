package com.game.service;

import com.game.constant.BuildingId;
import com.game.constant.GameError;
import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticBeautyMgr;
import com.game.dataMgr.StaticManoeuvreMgr;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.Hero;
import com.game.domain.p.Item;
import com.game.domain.p.SimpleData;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticBeautyBase;
import com.game.domain.s.StaticManoeuvreShop;
import com.game.domain.s.StaticWorldActPlan;
import com.game.log.domain.ManoeuvreLog;
import com.game.manager.ActManoeuvreManager;
import com.game.manager.BeautyManager;
import com.game.manager.HeroManager;
import com.game.manager.ItemManager;
import com.game.manager.PlayerManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.ActManoeuvrePb.ActManoeuvreApplyLineRq;
import com.game.pb.ActManoeuvrePb.ActManoeuvreApplyLineRs;
import com.game.pb.ActManoeuvrePb.ActManoeuvreArmyRq;
import com.game.pb.ActManoeuvrePb.ActManoeuvreArmyRs;
import com.game.pb.ActManoeuvrePb.ActManoeuvreBuyShopRq;
import com.game.pb.ActManoeuvrePb.ActManoeuvreBuyShopRs;
import com.game.pb.ActManoeuvrePb.ActManoeuvreChangeLineRq;
import com.game.pb.ActManoeuvrePb.ActManoeuvreChangeLineRs;
import com.game.pb.ActManoeuvrePb.ActManoeuvreCourseRs;
import com.game.pb.ActManoeuvrePb.ActManoeuvreDetailRq;
import com.game.pb.ActManoeuvrePb.ActManoeuvreDetailRs;
import com.game.pb.ActManoeuvrePb.ActManoeuvreRecordRq;
import com.game.pb.ActManoeuvrePb.ActManoeuvreRecordRs;
import com.game.pb.ActManoeuvrePb.ActManoeuvreSignUpRq;
import com.game.pb.ActManoeuvrePb.ActManoeuvreSignUpRs;
import com.game.pb.ActManoeuvrePb.GetActManoeuvreRs;
import com.game.pb.ActManoeuvrePb.GetActManoeuvreShopRs;
import com.game.pb.CommonPb.ManoeuvreArmyPB;
import com.game.pb.CommonPb.ManoeuvreHeroRecordPB;
import com.game.pb.CommonPb.ManoeuvreMatchGroupPB;
import com.game.pb.CommonPb.ManoeuvreMatchPB;
import com.game.pb.CommonPb.ManoeuvreRoundPB;
import com.game.pb.CommonPb.ManoeuvreScoreGroupPB;
import com.game.pb.CommonPb.ManoeuvreScorePB;
import com.game.pb.CommonPb.ManoeuvreShopPB;
import com.game.pb.CommonPb.TwoInt;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.game.util.Pair;
import com.game.util.PbHelper;
import com.game.worldmap.fight.manoeuvre.ManoeuvreConst;
import com.game.worldmap.fight.manoeuvre.ManoeuvreCourse;
import com.game.worldmap.fight.manoeuvre.ManoeuvreData;
import com.game.worldmap.fight.manoeuvre.ManoeuvreDetail;
import com.game.worldmap.fight.manoeuvre.ManoeuvreFighter;
import com.game.worldmap.fight.manoeuvre.ManoeuvreRound;
import com.game.worldmap.fight.manoeuvre.ManoeuvreScore;
import com.google.common.collect.HashBasedTable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @version 创建时间：2021-12-20 下午17:36:23
 * @declare
 */
@Service
public class ActManoeuvreService {

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private ActManoeuvreManager actManoeuvreManager;
	@Autowired
	private StaticManoeuvreMgr staticManoeuvreMgr;
	@Autowired
	private StaticWorldActPlanMgr worldActPlanMgr;
	@Autowired
	private WorldManager worldManager;
	@Autowired
	private ItemManager itemManager;
	@Autowired
	private StaticBeautyMgr staticBeautyMgr;
	@Autowired
	private BeautyManager beautyManager;
	@Autowired
	private HeroManager heroManager;

	/**
	 * 拉取活动信息
	 *
	 * @param handler
	 */
	public void getActManoeuvreRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		long playerId = handler.getRoleId();
		ManoeuvreData manoeuvreData = actManoeuvreManager.getManoeuvreData();
		GetActManoeuvreRs.Builder builder = GetActManoeuvreRs.newBuilder();
		if (manoeuvreData == null) {//活动未初始化
			builder.setStage(0);
			builder.setStatus(0);
			handler.sendMsgToPlayer(GetActManoeuvreRs.ext, builder.build());
			return;
		}
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_14);
		if (staticWorldActPlan == null) {
			builder.setStage(0);
			builder.setStatus(0);
			handler.sendMsgToPlayer(GetActManoeuvreRs.ext, builder.build());
			return;
		}

		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_14);
		if (worldActPlan == null) {
			builder.setStage(0);
			builder.setStatus(0);
			handler.sendMsgToPlayer(GetActManoeuvreRs.ext, builder.build());
			return;
		}

		if (worldActPlan.getState() <= WorldActPlanConsts.NOE_OPEN) {
			builder.setStage(0);
			builder.setStatus(0);
			handler.sendMsgToPlayer(GetActManoeuvreRs.ext, builder.build());
			return;
		}

		int stage = manoeuvreData.getStage();
		int status = manoeuvreData.getStatus();
		if (status == ManoeuvreConst.STATUS_NONE) {
			builder.setStage(0);
			builder.setStatus(0);
			handler.sendMsgToPlayer(GetActManoeuvreRs.ext, builder.build());
			return;
		}
		builder.setStatus(manoeuvreData.getStatus());
		builder.setStage(manoeuvreData.getStage());

		long period = staticWorldActPlan.getContinues().get(0) * ManoeuvreConst.SECOND;// 每轮的间隔时间
		long endTime = status == ManoeuvreConst.STATUS_END ? worldActPlan.getEndTime() : worldActPlan.getOpenTime() + period * stage;
		builder.setEndTime(endTime);

		ManoeuvreFighter fighter = manoeuvreData.getApplyMap().get(playerId);
		builder.setApply(fighter == null ? 0 : 1);
		builder.setLine(fighter == null ? 0 : fighter.getLine());
		builder.addAllApplyLine(manoeuvreData.getApplyLinePb(player.getCountry(),manoeuvreData.getStage()));

		if (stage >= ManoeuvreConst.STAGE_ONE && stage <= ManoeuvreConst.STAGE_THRE) {
			ManoeuvreCourse manoeuvreCourse = manoeuvreData.getCourseMap().get(stage);
			builder.setCountry(TwoInt.newBuilder().setV1(manoeuvreCourse.getCountryLeft()).setV2(manoeuvreCourse.getCountryRight()).build());
		}

		handler.sendMsgToPlayer(GetActManoeuvreRs.ext, builder.build());
	}

	/**
	 * 报名
	 *
	 * @param handler
	 */
	public void actManoeuvreSignUpRq(ActManoeuvreSignUpRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		ManoeuvreData manoeuvreData = actManoeuvreManager.getManoeuvreData();

		if (manoeuvreData == null) {//活动未开启
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE);
			return;
		}
		int status = manoeuvreData.getStatus();
		if (status != ManoeuvreConst.STATUS_APPLY) {
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE_APPLY);
			return;
		}

		int line = req.getLine();
		if (line > 3 || line < 1) {
			handler.sendErrorMsgToPlayer(GameError.MANOEUVRE_PARAM_ERROR);
			return;
		}

		int commandLv = player.getBuildingLv(BuildingId.COMMAND);
		if (commandLv < ManoeuvreConst.APPLY_LEVEL) {
			handler.sendErrorMsgToPlayer(GameError.COMMAND_LV_NOT_ENOUGH_12);
			return;
		}

		List<Hero> heroList = new ArrayList<>();
		int maxSoilder = 0;
		for (int heroId : req.getHerosList()) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {//
				handler.sendErrorMsgToPlayer(GameError.NO_HERO_FIGHT);
				return;
			}
			Hero clone = hero.clone();
			maxSoilder += hero.getCurrentSoliderNum();
			heroList.add(clone);
		}

		actManoeuvreManager.applyFight(player, req.getLine(), maxSoilder, heroList).thenAcceptAsync(e -> {
			if (e == null) {
				return;
			}
			actManoeuvreManager.update(e);
			actManoeuvreManager.synManoeuvre();
		});

		ActManoeuvreSignUpRs.Builder builder = ActManoeuvreSignUpRs.newBuilder();
		handler.sendMsgToPlayer(ActManoeuvreSignUpRs.ext, builder.build());
	}

	public void actManoeuvreApplyLineRq(ActManoeuvreApplyLineRq req, ClientHandler handler) {
		ManoeuvreData manoeuvreData = actManoeuvreManager.getManoeuvreData();

		if (manoeuvreData == null) {//活动未开启
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE);
			return;
		}
		int status = manoeuvreData.getStatus();
		if (status != ManoeuvreConst.STATUS_APPLY) {
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE_APPLY);
			return;
		}

		int line = req.getLine();
		if (line > 3 || line < 1) {
			handler.sendErrorMsgToPlayer(GameError.MANOEUVRE_PARAM_ERROR);
			return;
		}

		ManoeuvreFighter manoeuvreFighter = manoeuvreData.getApplyMap().get(handler.getRoleId());
		if (manoeuvreFighter == null) {
			handler.sendErrorMsgToPlayer(GameError.MANOEUVRE_NEED_APPLY);
			return;
		}

		manoeuvreFighter.setLine(line);
		ActManoeuvreApplyLineRs.Builder builder = ActManoeuvreApplyLineRs.newBuilder();
		handler.sendMsgToPlayer(ActManoeuvreApplyLineRs.ext, builder.build());
		actManoeuvreManager.synManoeuvre();
	}

	/**
	 * @param req
	 * @param handler
	 */
	public void actManoeuvreArmyRq(ActManoeuvreArmyRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		ManoeuvreData data = actManoeuvreManager.getManoeuvreData();
		if (data == null || data.getStatus() == 0) {//活动未开启
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE);
			return;
		}

		int line = req.getLine();
		ActManoeuvreArmyRs.Builder builder = ActManoeuvreArmyRs.newBuilder();
		int status = data.getStatus();
		int country = player.getCountry();

		if (status <= ManoeuvreConst.STATUS_APPLY) {//报名阶段，取前25名显示
			if (!data.getApplyMap().isEmpty()) {// 倒序
				List<ManoeuvreFighter> list = data.getApplyMap().values().stream().filter(e -> e.getCountry() == country && e.getLine() == line).sorted(Comparator.comparing(ManoeuvreFighter::getPower).reversed().thenComparing(ManoeuvreFighter::getApplyTime)).collect(Collectors.toList());
				int pos = list.size() > 25 ? 25 : list.size();
				for (int i = 0; i < pos; i++) {
					ManoeuvreFighter fighter = list.get(i);
					ManoeuvreArmyPB.Builder army = ManoeuvreArmyPB.newBuilder();
					army.setLine(fighter.getLine());
					army.setPos(pos - i);
					army.setLordId(fighter.getPlayerId());
					Player target = playerManager.getPlayer(fighter.getPlayerId());
					army.setNick(target.getNick());
					army.setPower(fighter.getPower());
					builder.addArmys(army.build());
				}
			}
		} else {
			List<ManoeuvreFighter> list = data.getFights().get(country, line);
			int pos = list.size() > 25 ? 25 : list.size();
			for (int i = 0; i < pos; i++) {
				ManoeuvreFighter fighter = list.get(pos - 1 - i);
				ManoeuvreArmyPB.Builder army = ManoeuvreArmyPB.newBuilder();
				army.setLine(fighter.getLine());
				army.setPos(fighter.getPos());
				army.setLordId(fighter.getPlayerId());
				Player target = playerManager.getPlayer(fighter.getPlayerId());
				army.setNick(target.getNick());
				army.setPower(fighter.getPower());
				builder.addArmys(army.build());
			}
		}

		handler.sendMsgToPlayer(ActManoeuvreArmyRs.ext, builder.build());
//		LogHelper.MESSAGE_LOGGER.info("ActManoeuvreArmyRs:{}", builder.build());
	}


	public void actManoeuvreChangeLineRq(ActManoeuvreChangeLineRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		ManoeuvreData data = actManoeuvreManager.getManoeuvreData();
		if (data == null || data.getStatus() == 0) {//活动未开启
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE);
			return;
		}

		int status = data.getStatus();
		if (status != ManoeuvreConst.STATUS_PREPARE) {
			handler.sendErrorMsgToPlayer(GameError.MANOEUVRE_NEED_PARE);
			return;
		}

		int lineA = req.getLineA();
		int lineB = req.getLineB();
		if (lineA < ManoeuvreConst.LINE_ONE || lineA > ManoeuvreConst.LINE_THREE || lineB < ManoeuvreConst.LINE_ONE || lineB > ManoeuvreConst.LINE_THREE || lineA == lineB) {
			handler.sendErrorMsgToPlayer(GameError.MANOEUVRE_PARAM_ERROR);
			return;
		}

		int country = player.getCountry();

		HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> fighters = data.getFights();
		List<ManoeuvreFighter> listToB = fighters.get(country, lineA);
		List<ManoeuvreFighter> listToA = fighters.get(country, lineB);

		for (ManoeuvreFighter fighter : listToB) {
			fighter.setLine(lineB);
		}

		for (ManoeuvreFighter fighter : listToA) {
			fighter.setLine(lineA);
		}

		fighters.put(country, lineA, listToA);
		fighters.put(country, lineB, listToB);

		ActManoeuvreChangeLineRs.Builder builder = ActManoeuvreChangeLineRs.newBuilder();
		handler.sendMsgToPlayer(ActManoeuvreChangeLineRs.ext, builder.build());
		actManoeuvreManager.synManoeuvre();
	}


	/**
	 * 赛程信息
	 *
	 * @param handler
	 */
	public void actManoeuvreCourseRq(ClientHandler handler) {
		ManoeuvreData data = actManoeuvreManager.getManoeuvreData();
		if (data == null || data.getStatus() == 0) {//活动未开启
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE);
			return;
		}

		ActManoeuvreCourseRs.Builder builder = ActManoeuvreCourseRs.newBuilder();
		List<ManoeuvreData> historyList = actManoeuvreManager.getHistory();
		for (int i = 0; i < historyList.size() && i < ManoeuvreConst.SHOW_COURSE; i++) {
			ManoeuvreData history = historyList.get(i);
			manoeuvreScoreInfo(history, builder);
		}

//		LogHelper.MESSAGE_LOGGER.info("actManoeuvreCourseRs:{}", builder.build());
		handler.sendMsgToPlayer(ActManoeuvreCourseRs.ext, builder.build());
	}

	private void manoeuvreScoreInfo(ManoeuvreData manoeuvreData, ActManoeuvreCourseRs.Builder builder) {
		ManoeuvreMatchGroupPB.Builder matchGroup = ManoeuvreMatchGroupPB.newBuilder();
		ManoeuvreScoreGroupPB.Builder scoreGroup = ManoeuvreScoreGroupPB.newBuilder();
		for (ManoeuvreCourse e : manoeuvreData.getCourseMap().values()) {
			ManoeuvreMatchPB.Builder match = ManoeuvreMatchPB.newBuilder();
			match.setManoeuvreId(manoeuvreData.getKeyId());
			match.setStartTime(manoeuvreData.getStartTime());
			match.setStage(e.getStage());
			match.setRoundTime(e.getTime());
			match.setLeftCountry(e.getCountryLeft());
			match.setLeftScore(e.getScoreLeft());
			match.setRightCountry(e.getCountryRight());
			match.setRightScore(e.getScoreRight());
			matchGroup.addMatches(match.build());
		}

		// 排行信息
		for (ManoeuvreScore e : manoeuvreData.getScoreList()) {
			ManoeuvreScorePB.Builder pb = ManoeuvreScorePB.newBuilder();
			pb.setManoeuvreId(manoeuvreData.getKeyId());
			pb.setStartTime(manoeuvreData.getStartTime());
			pb.setCountry(e.getCountry());
			pb.setScore(e.getScore());
			pb.setCaptureFlag(e.getCaptureFlag());
			pb.setKillSoidler(e.getKillSoidler());
			scoreGroup.addManoeuvreScore(pb.build());
		}

		builder.addMatchGroup(matchGroup);
		if (scoreGroup.getManoeuvreScoreCount() > 0) {
			builder.addScoreGroup(scoreGroup);
		}
	}

	/**
	 * 沙盘演武战报
	 *
	 * @param handler
	 */
	public void actManoeuvreRecordRq(ActManoeuvreRecordRq req, ClientHandler handler) {
		long manoeuvreId = req.getManoeuvreId();
		int stage = req.getStage();
		ActManoeuvreRecordRs.Builder builder = ActManoeuvreRecordRs.newBuilder();
//		LogHelper.MESSAGE_LOGGER.info("actManoeuvreRecordRq manoeuvreId:{} stage:{}", manoeuvreId, stage);
		ManoeuvreData data = actManoeuvreManager.getManoeuvreData(manoeuvreId);
		if (data == null) {
			handler.sendMsgToPlayer(ActManoeuvreRecordRs.ext, builder.build());
			return;
		}
		ManoeuvreCourse course = data.getCourseMap().get(stage);
		if (course == null) {
			handler.sendMsgToPlayer(ActManoeuvreRecordRs.ext, builder.build());
			return;
		}

		builder.setManoeuvreId(manoeuvreId);
		builder.setLeftCountry(course.getCountryLeft());
		builder.setLeftScore(course.getScoreLeft());
		builder.setRightCountry(course.getCountryRight());
		builder.setRightScore(course.getScoreRight());

		List<ManoeuvreRound> roundList = course.getRoundList();
		for (ManoeuvreRound e : roundList) {//每个玩家对战信息
			ManoeuvreRoundPB.Builder pb = ManoeuvreRoundPB.newBuilder();
			pb.setRound(e.getRound());
			pb.setLine(e.getLine());
			pb.setLeftPos(e.getPostLeft());
			pb.setLeftRoleId(e.getPlayerIdLeft());
			pb.setLeftBlood(e.getBloodLeft());
			pb.setLeftNick(e.getNickLeft());
			pb.setRightPos(e.getPostRight());
			pb.setRightRoleId(e.getPlayerIdRight());
			pb.setRightBlood(e.getBloodRight());
			pb.setRightNick(e.getNickRight());
			builder.addRoundPb(pb.build());
		}

		HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> fights = data.getFights();
		// 三路
		for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
			List<ManoeuvreFighter> listLeft = fights.get(course.getCountryLeft(), line);
			List<ManoeuvreFighter> listRight = fights.get(course.getCountryRight(), line);
			TwoInt twoInt = TwoInt.newBuilder().setV1(listLeft.size()).setV2(listRight.size()).build();
			builder.addLineNums(twoInt);
		}

		handler.sendMsgToPlayer(ActManoeuvreRecordRs.ext, builder.build());
//		LogHelper.MESSAGE_LOGGER.info("ActManoeuvreRecordRs:{}", builder.build());
	}

	/**
	 * 沙盘演武战报详情
	 *
	 * @param handler
	 */
	public void actManoeuvreDetailRq(ActManoeuvreDetailRq req, ClientHandler handler) {
		long manoeuvreId = req.getManoeuvreId();
		int stage = req.getStage();
		long playerA = req.getLeftRoldId();
		long playerB = req.getRightRoldId();

//		LogHelper.MESSAGE_LOGGER.info("actManoeuvreDetailRq manoeuvreId:{} stage:{} playerA:{} playerB:{}", manoeuvreId, stage, playerA, playerB);

		ActManoeuvreDetailRs.Builder builder = ActManoeuvreDetailRs.newBuilder();
		ManoeuvreData data = actManoeuvreManager.getManoeuvreData(manoeuvreId);
		if (data == null) {
			handler.sendMsgToPlayer(ActManoeuvreDetailRs.ext, builder.build());
			return;
		}
		ManoeuvreCourse course = data.getCourseMap().get(stage);
		if (course == null) {
			handler.sendMsgToPlayer(ActManoeuvreDetailRs.ext, builder.build());
			return;
		}

		Optional<ManoeuvreRound> optional = course.getRoundList().stream().filter(e -> e.getPlayerIdLeft() == playerA && e.getPlayerIdRight() == playerB).findFirst();
		if (!optional.isPresent()) {
			handler.sendMsgToPlayer(ActManoeuvreDetailRs.ext, builder.build());
			return;
		}
		ManoeuvreRound manoeuvreRound = optional.get();

		List<ManoeuvreDetail> detailList = manoeuvreRound.getDetailList();
		for (ManoeuvreDetail e : detailList) {
			ManoeuvreHeroRecordPB.Builder pb = ManoeuvreHeroRecordPB.newBuilder();
			pb.setHeroId(e.getHeroId());
			pb.setKillSoilder(e.getKillSoilder());
			pb.setLostSoilder(e.getLostSoilder());
			pb.setDiviNum(e.getDiviNum());
			pb.setMaxSoilder(e.getMaxSoilder());
			if (manoeuvreRound.getPlayerIdLeft() == e.getPlayerId()) {
				builder.addLeftDetail(pb.build());
			} else {
				builder.addRightDetail(pb.build());
			}
		}
		handler.sendMsgToPlayer(ActManoeuvreDetailRs.ext, builder.build());
//		LogHelper.MESSAGE_LOGGER.info("actManoeuvreDetailRs:{}", builder.build());
	}

	public void getActManoeuvreShopRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());

		int country = player.getCountry();
		int victoryCount = actManoeuvreManager.getVictorr(country);

		ManoeuvreData manoeuvreData = actManoeuvreManager.getManoeuvreData();
		if (manoeuvreData == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE);
			return;
		}
		WorldActPlan worldActPlan = worldManager.getWorldActPlan(WorldActivityConsts.ACTIVITY_14);
		if (worldActPlan == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE);
			return;
		}
		SimpleData simpleData = player.getSimpleData();
		Map<Integer, StaticManoeuvreShop> shops = staticManoeuvreMgr.getShops();
		GetActManoeuvreShopRs.Builder builder = GetActManoeuvreShopRs.newBuilder();

		builder.setScore(simpleData.getManoeuvreScore());
		builder.setUnlock(victoryCount <= 1 ? 0 : victoryCount == 2 ? 1 : 2);
		long refreshTime = worldActPlan.getPreheatTime() + 7 * 24 * 3600 * 1000l;
		builder.setRefreshTime(refreshTime);

		HashMap<Integer, Pair<Integer, Long>> shopMap = simpleData.getManoeuvreShop();
		long openTime = manoeuvreData.getStartTime();

		shops.values().forEach(e -> {
			if (e.getBeautyId() != 0) {// 秘书已解锁,则不在显示改道具
				if (player.getBeautys().containsKey(e.getBeautyId())) {
					return;
				}
			}

			ManoeuvreShopPB.Builder shopPb = ManoeuvreShopPB.newBuilder();
			shopPb.setType(e.getType());
			shopPb.setShopId(e.getId());
			List<Integer> award = e.getAward();
			if (award.size() != 3) {
				return;
			}
			shopPb.setItemType(award.get(0));
			shopPb.setItemId(award.get(1));
			shopPb.setItemCount(award.get(2));
			shopPb.setPrice(e.getPrice());
			if (!shopMap.containsKey(e.getId())) {//没有购买过
				shopPb.setBuyCount(e.getLimitCount());
			} else {
				Pair<Integer, Long> history = shopMap.get(e.getId());
				int buyCount = history.getRight() == openTime ? e.getLimitCount() - history.getLeft() : e.getLimitCount();
				shopPb.setBuyCount(buyCount);
			}
			shopPb.setMaxCount(e.getLimitCount());
			builder.addShop(shopPb.build());
		});
		handler.sendMsgToPlayer(GetActManoeuvreShopRs.ext, builder.build());
//		LogHelper.MESSAGE_LOGGER.info("GetActManoeuvreShopRs:{}", builder.build());
	}

	/**
	 * 购买商店道具
	 *
	 * @param req
	 * @param handler
	 */
	public void actManoeuvreBuyShopRq(ActManoeuvreBuyShopRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());

		ManoeuvreData manoeuvreData = actManoeuvreManager.getManoeuvreData();
		if (manoeuvreData == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_MANOEUVRE);
			return;
		}

		int shopId = req.getShopId();

		int country = player.getCountry();
		int victoryCount = actManoeuvreManager.getVictorr(country);

		Map<Integer, StaticManoeuvreShop> shops = staticManoeuvreMgr.getShops();
		if (!shops.containsKey(shopId)) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		StaticManoeuvreShop shop = shops.get(shopId);
		if (shop.getType() == ManoeuvreConst.TYPE_WIN_TWO) {
			if (victoryCount < 2) {
				handler.sendErrorMsgToPlayer(GameError.WIN_COUNT_NOT_ENOUGH);// 连胜次数不足
				return;
			}
		} else if (shop.getType() == ManoeuvreConst.TYPE_WIN_THREE) {
			if (victoryCount < 3) {
				handler.sendErrorMsgToPlayer(GameError.WIN_COUNT_NOT_ENOUGH);// 连胜次数不足
				return;
			}
		}

		SimpleData simpleData = player.getSimpleData();
		if (simpleData.getManoeuvreScore() < shop.getPrice()) {
			handler.sendErrorMsgToPlayer(GameError.SCORE_NOT_ENOUGH);// 积分不足
			return;
		}

		List<Integer> award = shop.getAward();
		if (award.size() != 3) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);// 配置错误
			return;
		}

		long openTime = manoeuvreData.getStartTime();
		HashMap<Integer, Pair<Integer, Long>> shopMap = simpleData.getManoeuvreShop();
		int itemType = award.get(0);
		int itemId = award.get(1);
		int itemCount = award.get(2);

		if (shop.getBeautyId() != 0) {// 查看是否能解锁秘书
			if (player.getBeautys().containsKey(shop.getBeautyId())) {
				handler.sendErrorMsgToPlayer(GameError.SCORE_NOT_ENOUGH);// 秘书已拥有
				return;
			}
		}

		Pair<Integer, Long> pair = shopMap.get(shopId);
		if (pair == null) {
			pair = new Pair<>(0, 0L);
			shopMap.put(shopId, pair);
		}
		if (pair.getRight() == openTime) {
			if (pair.getLeft() >= shop.getLimitCount()) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_COUNT);// 次数不足
				return;
			}
			pair.setLeft(pair.getLeft() + 1);
		} else {
			pair.setLeft(1);
			pair.setRight(openTime);
		}

		simpleData.setManoeuvreScore(simpleData.getManoeuvreScore() - shop.getPrice());

		ActManoeuvreBuyShopRs.Builder builder = ActManoeuvreBuyShopRs.newBuilder();

		// 添加奖励
		playerManager.addAward(player, itemType, itemId, itemCount, 0);
		builder.addAward(PbHelper.createAward(itemType, itemId, itemCount));
		builder.setScore(simpleData.getManoeuvreScore());

		if (shop.getBeautyId() != 0) {// 查看是否能解锁秘书
			StaticBeautyBase beautyBase = staticBeautyMgr.getStaticBeautyBase(shop.getBeautyId());
			Item item = itemManager.getItem(player, itemId);
			int count = beautyBase.getParam().get(2);//解锁数量
			if (item.getItemNum() >= count) {//添加秘书
				beautyManager.addBeautyInfo(player, shop.getBeautyId(), 0);
				itemManager.subItem(player, itemId, item.getItemNum(), 0);
			}
		}

		shops.values().forEach(e -> {
			if (e.getBeautyId() != 0) {// 秘书已解锁,则不在显示改道具
				if (player.getBeautys().containsKey(e.getBeautyId())) {
					return;
				}
			}
			ManoeuvreShopPB.Builder shopPb = ManoeuvreShopPB.newBuilder();
			shopPb.setType(e.getType());
			shopPb.setShopId(e.getId());
			shopPb.setPrice(e.getPrice());
			List<Integer> eaward = e.getAward();
			if (eaward.size() != 3) {
				return;
			}
			shopPb.setItemType(eaward.get(0));
			shopPb.setItemId(eaward.get(1));
			shopPb.setItemCount(eaward.get(2));
			if (!shopMap.containsKey(e.getId())) {//没有购买过
				shopPb.setBuyCount(e.getLimitCount());
			} else {
				Pair<Integer, Long> history = shopMap.get(e.getId());
				int buyCount = history.getRight() == openTime ? e.getLimitCount() - history.getLeft() : e.getLimitCount();
				shopPb.setBuyCount(buyCount);
			}
			shopPb.setMaxCount(e.getLimitCount());
			builder.addShop(shopPb.build());
		});

//		LogHelper.GAME_LOGGER.info("兑换 {} {} {}",player.getNick(),shop.getPrice(),itemId);
		// 日志记录 消耗积分
		SpringUtil.getBean(com.game.log.LogUser.class).manoeuvre_log(
				ManoeuvreLog.builder()
						.roleId(player.roleId)
						.nick(player.getNick())
						.level(player.getLevel())
						.vipLevel(player.getVip())
						.changePoint(shop.getPrice())
						.itemId(itemId)
						.itemNum(itemCount)
						.source(0)   // 消耗积分记为0
						.type(1)    // type=1 为消耗积分
						.point(simpleData.getManoeuvreScore())
						.build()
		);

		handler.sendMsgToPlayer(ActManoeuvreBuyShopRs.ext, builder.build());
//		LogHelper.MESSAGE_LOGGER.info("ActManoeuvreBuyShopRs:{}", builder.build());
	}
}
