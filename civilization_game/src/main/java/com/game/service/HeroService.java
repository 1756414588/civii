package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.enumerate.SearchTokeType;
import com.game.log.LogUser;
import com.game.log.consumer.EventManager;
import com.game.log.domain.HeroAdvanceLog;
import com.game.log.domain.HeroDivineLog;
import com.game.log.domain.HeroWashLog;
import com.game.log.domain.SeekLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.HeroPb;
import com.game.pb.HeroPb.*;
import com.game.spring.SpringUtil;
import com.game.util.*;
import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liyanze
 * @version 创建时间：2016-12-19 下午13:29:00
 * @declare
 */
@Service
public class HeroService {

	@Autowired
	private HeroManager heroManager;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticHeroMgr staticHeroDataMgr;

	@Autowired
	private LootManager lootManager;

	private final int RATE_NUM = 1000;

	private final int GOOD_LOOT_RATE = 100;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private TechManager techManager;

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private SoldierManager soldierManager;

	@Autowired
	private EquipManager equipMgr;

	@Autowired
	private CountryManager countryManager;

	@Autowired
	private StaticOpenManger staticOpenManger;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private StaticActivityMgr staticActivityMgr;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private SurpriseGiftManager surpriseGiftManager;
	@Autowired
	ActivityEventManager activityEventManager;
	/**
	 * Function:获取我的将领数据
	 *
	 * @param handler
	 */
	public void getHerosRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {

			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Map<Integer, Hero> heroes = player.getHeros();
		heroManager.caculateAllProperty(heroes, player);
		HeroPb.GetHeroRs.Builder builder = HeroPb.GetHeroRs.newBuilder();
		// check hero to delete
		Iterator<Hero> iterator = heroes.values().iterator();
		while (iterator.hasNext()) {
			Hero hero = iterator.next();
			if (hero == null) {
				continue;
			}

			// check country hero
			CountryHero countryHero = countryManager.getCountryHeroById(hero.getHeroId());
			if (countryHero == null) {
				continue;
			}

			// 删除多余的英雄
			if (countryHero.getLordId() == 0) {
				iterator.remove();
			}
		}

		for (Map.Entry<Integer, Hero> item : heroes.entrySet()) {
			if (item == null) {
				continue;
			}

			Hero hero = item.getValue();
			if (hero == null) {
				continue;
			}
			heroManager.caculateProp(hero, player);
			builder.addHero(hero.wrapPb());
		}

		List<Integer> embattleList = player.getEmbattleList();
		if (embattleList.size() < CommonDefine.MAX_EMBATTLE_POS) {
			// 简单做个容错处理
			for (int i = embattleList.size(); i < CommonDefine.MAX_EMBATTLE_POS; i++) {
				embattleList.add(-1);
			}
		}

		if (!embattleList.isEmpty()) {
			builder.addAllHeroId(embattleList);
		}

		handler.sendMsgToPlayer(GameError.OK, GetHeroRs.ext, builder.build());
	}

	/**
	 * Function:获取英雄上阵信息
	 */
	public void getEmbattleInfoRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		GetEmbattleInfoRs.Builder builder = GetEmbattleInfoRs.newBuilder();
		List<Integer> embattleList = player.getEmbattleList();
		if (!embattleList.isEmpty()) {
			List<Integer> removeDuplicate = StringUtil.removeDuplicate(embattleList);// 清除上阵英雄仲重复的英雄
			builder.addAllHeroId(removeDuplicate);
		}

		handler.sendMsgToPlayer(GameError.OK, GetEmbattleInfoRs.ext, builder.build());
	}

	/**
	 * Function: 请求英雄上阵
	 */
	public void embattleHeroRq(EmbattleHeroRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查英雄是否重复上阵
		GameError gameError = heroManager.judgeHeroRepeat(player, req.getHeroId());
		if (gameError != GameError.OK) {
			handler.sendErrorMsgToPlayer(gameError);
			return;
		}

		List<Integer> embattleInfo = player.getEmbattleList();
		// 检查index是否存在
		int index = req.getIndex();
		Integer indexFound = null;
		for (int i = 0; i < embattleInfo.size(); ++i) {
			if (index == i + 1) {
				indexFound = embattleInfo.get(i);
				break;
			}
		}

		if (indexFound == null) {
			LogHelper.CONFIG_LOGGER.info("error embattle index = " + index);
			handler.sendErrorMsgToPlayer(GameError.ERROR_EMBATTLE_INDEX);
			return;
		}

		// 上阵槽位是否开启
		if (indexFound == -1) {
			LogHelper.CONFIG_LOGGER.info("embattle index is locked, index = " + indexFound);
			handler.sendErrorMsgToPlayer(GameError.EMBATTLE_NOT_OPEN);
			return;
		}

		Map<Integer, Hero> heros = player.getHeros();
		int heroId = req.getHeroId();
		Hero hero = heros.get(heroId); // 要上阵的英雄Id

		// 检查英雄是否已经上阵
		if (indexFound == heroId) {
			LogHelper.CONFIG_LOGGER.info("hero already embattle = " + heroId);
			handler.sendErrorMsgToPlayer(GameError.HERO_ALREADY_EMBATTLE);
			return;
		}

		doHeroEmbattleTask(player, heroId);

		EmbattleHeroRs.Builder builder = EmbattleHeroRs.newBuilder();
		// 是否替换武将装备
		Hero currentHero = heros.get(indexFound);
		int beforeHeroId = embattleInfo.get(index - 1);
		boolean flag = true;
		if (currentHero != null) {
			// 英雄正在行军之中
			if (player.isInMarch(currentHero)) {
				handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
				return;
			}
			if (player.isInMass(currentHero.getHeroId())) {
				handler.sendErrorMsgToPlayer(GameError.HERO_IN_MASS);
				return;
			}
			if (!currentHero.isActivated()) {
				handler.sendErrorMsgToPlayer(GameError.HERO_IS_NOT_ACTIVATE);
				return;
			}
			if (player.hasPvpHero(currentHero.getHeroId())) {
				handler.sendErrorMsgToPlayer(GameError.HERO_IN_PVP_BATTLE);
				return;
			}
			embattleInfo.set(index - 1, heroId);
			flag = false;
			if (req.getIsExchangeEquip()) {
				currentHero.swapEquip(hero);
				builder.setIsExchangeEquip(true);
				if (currentHero.getCurrentSoliderNum() > 0) {
					playerManager.addAward(player, AwardType.SOLDIER, heroManager.getSoldierType(currentHero.getHeroId()), currentHero.getCurrentSoliderNum(), Reason.EMBATTLE);
					currentHero.setCurrentSoliderNum(0);
				}
				heroManager.caculateProp(hero, player);
				heroManager.caculateProp(currentHero, player);
				CommonPb.EquipExChange currentEquip = heroManager.wrapEquipExchange(currentHero);
				CommonPb.EquipExChange changeEquip = heroManager.wrapEquipExchange(hero);
				builder.addExchangeInfo(currentEquip);
				builder.addExchangeInfo(changeEquip);

			} else {
				builder.setIsExchangeEquip(false);
				heroManager.caculateProp(hero, player);
				heroManager.caculateProp(currentHero, player);
				CommonPb.EquipExChange currentEquip = heroManager.wrapEquipExchange(currentHero);
				CommonPb.EquipExChange changeEquip = heroManager.wrapEquipExchange(hero);
				builder.addExchangeInfo(currentEquip);
				builder.addExchangeInfo(changeEquip);
			}
		}
		if (indexFound == 0) { // 上阵的位置为空,直接补兵, 或者上阵的位置用英雄
			handlerSingleSoldier(hero, player, builder);
		} else {
			handlerSoldier(currentHero, hero, player, builder);
		}

		if (flag) {
			embattleInfo.set(index - 1, heroId);
		}
		//if (player.getWall() != null) {
		//	handleDefence(player, heroId, beforeHeroId);
		//
		//}
		builder.addAllDefenceHero(player.getMeetingArmy(CastleConsts.DEFENSEARMY));
		builder.addAllEmbattleHero(embattleInfo);
		handler.sendMsgToPlayer(GameError.OK, EmbattleHeroRs.ext, builder.build());

		ArrayList<Hero> list = new ArrayList<>();
		list.add(hero);
		list.add(currentHero);
		heroManager.synBattleScoreAndHeroList(player, list);
	}

	public void handlerSoldier(Hero currentHero, Hero hero, Player player, EmbattleHeroRs.Builder builder) {
		if (currentHero == null || hero == null) {
			// LogHelper.CONFIG_LOGGER.info("currentHero is null or hero is null!");
			return;
		}

		// 获得当前的英雄兵力
		int heroSoliderNum = currentHero.getCurrentSoliderNum();
		// 获得需要交换的武将的类型
		int currentSoldierType = heroManager.getSoldierType(currentHero.getHeroId());
		int changeSoldierType = heroManager.getSoldierType(hero.getHeroId());
		// 将多余的兵力还回去
		playerManager.addAward(player, AwardType.SOLDIER, currentSoldierType, heroSoliderNum, Reason.EMBATTLE);
		// 需要交换的武将类型拥有的最大兵力
		int hasSoldierNum = soldierManager.getSoldierNum(player, changeSoldierType);

		// 武将当前的兵力[自动给上阵的英雄补兵]
		int diff = Math.min(hasSoldierNum, hero.getSoldierNum());
		if (player.getSoldierAuto() == 0) {
			diff = 0;
		}

		// 当前兵力比属性还大
		hero.setCurrentSoliderNum(diff);

//        if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
//            diff -= hero.getCurrentSoliderNum() - hero.getSoldierNum();
//            hero.setCurrentSoliderNum(hero.getSoldierNum());
//            LogHelper.CONFIG_LOGGER.info("hero.getCurrentSoliderNum() = " + hero.getCurrentSoliderNum() + ", hero.getSoldierNum() = " + hero.getSoldierNum());
//        }

		// 武将补兵之后从兵营扣除兵力
		playerManager.subAward(player, AwardType.SOLDIER, changeSoldierType, diff, Reason.EMBATTLE);
		currentHero.setCurrentSoliderNum(0);
		CommonPb.HeroSoldier.Builder currentHeroSoldier = CommonPb.HeroSoldier.newBuilder();
		currentHeroSoldier.setHeroId(currentHero.getHeroId());
		currentHeroSoldier.setSoldier(currentHero.getCurrentSoliderNum());

		CommonPb.HeroSoldier.Builder changeHeroSoldier = CommonPb.HeroSoldier.newBuilder();
		changeHeroSoldier.setHeroId(hero.getHeroId());
		changeHeroSoldier.setSoldier(hero.getCurrentSoliderNum());

		builder.addHeroSoldier(currentHeroSoldier);
		builder.addHeroSoldier(changeHeroSoldier);

		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		for (Map.Entry<Integer, Soldier> soldierElem : soldierMap.entrySet()) {
			Soldier soldier = soldierElem.getValue();
			if (soldier == null) {
				continue;
			}
			builder.addSoldier(soldier.wrapPb());

		}
		// 两个兵营的情况
//		if (currentSoldierType != changeSoldierType) {  // 如果两个英雄类型不一致
//			CommonPb.Soldier.Builder soldier1 = CommonPb.Soldier.newBuilder();
//			soldier1.setSoldierType(changeSoldierType);
//			soldier1.setNum(soldierManager.getSoldierNum(player, changeSoldierType));
//			soldier1.setSoldierIndex(changeSoldierType);
//
//			CommonPb.Soldier.Builder soldier2 = CommonPb.Soldier.newBuilder();
//			soldier2.setSoldierType(currentSoldierType);
//			soldier2.setNum(soldierManager.getSoldierNum(player, currentSoldierType));
//			soldier2.setSoldierIndex(currentSoldierType);
//			builder.addSoldier(soldier1);
//			builder.addSoldier(soldier2);
//
//		} else {
//			CommonPb.Soldier.Builder soldier1 = CommonPb.Soldier.newBuilder();
//			soldier1.setSoldierType(changeSoldierType);
//			soldier1.setNum(soldierManager.getSoldierNum(player, changeSoldierType));
//			soldier1.setSoldierIndex(changeSoldierType);
//			builder.addSoldier(soldier1);
//		}
	}

	public void handlerSingleSoldier(Hero hero, Player player, EmbattleHeroRs.Builder builder) {
		int heroSoliderType = heroManager.getSoldierType(hero.getHeroId());
		int soldierAll = soldierManager.getSoldierNum(player, heroSoliderType);
		int heroSoldierNum = hero.getSoldierNum();
		int soldierAdd = Math.min(soldierAll, heroSoldierNum);
		if (player.getSoldierAuto() == 0) {
			soldierAdd = 0;
		}
		hero.setCurrentSoliderNum(soldierAdd);
		if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
			hero.setCurrentSoliderNum(hero.getSoldierNum());
			LoggerFactory.getLogger(getClass()).error("hero.getCurrentSoliderNum() = " + hero.getCurrentSoliderNum() + ", hero.getSoldierNum() = " + hero.getSoldierNum());
		}

		if (soldierAdd > 0) {
			playerManager.subAward(player, AwardType.SOLDIER, heroSoliderType, soldierAdd, Reason.EMBATTLE);
		}

		CommonPb.HeroSoldier.Builder heroSoldier = CommonPb.HeroSoldier.newBuilder();
		heroSoldier.setHeroId(hero.getHeroId());
		heroSoldier.setSoldier(hero.getCurrentSoliderNum());

		builder.addHeroSoldier(heroSoldier);

//		CommonPb.Soldier.Builder soldier = CommonPb.Soldier.newBuilder();
//		soldier.setSoldierType(heroSoliderType);
//		soldier.setNum(soldierManager.getSoldierNum(player, heroSoliderType));
//		soldier.setSoldierIndex(heroSoliderType);
//		builder.addSoldier(soldier);
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		for (Map.Entry<Integer, Soldier> soldierElem : soldierMap.entrySet()) {
			Soldier soldier = soldierElem.getValue();
			if (soldier == null) {
				continue;
			}
			builder.addSoldier(soldier.wrapPb());

		}
	}

	// 处理城防军,如果当前城防军有这个英雄则替换英雄，如果没有则放到后面
	//public void handleDefence(Player player, int addHeroId, int beforeHeroId) {
	//	Wall wall = player.getWall();
	//	List<Integer> defenceHero = wall.getDefenceHero();
	//	List<Integer> embattleList = player.getEmbattleList();
	//	if (defenceHero.size() > 4) {
	//		defenceHero.clear();
	//		defenceHero.addAll(embattleList);
	//	} else {
	//		if (defenceHero.contains(beforeHeroId)) {
	//			int index = defenceHero.indexOf(beforeHeroId);
	//			defenceHero.set(index, addHeroId);
	//		} else {
	//			if (defenceHero.size() < 4) {
	//				defenceHero.add(addHeroId);
	//			} else {
	//				defenceHero.clear();
	//				defenceHero.addAll(embattleList);
	//			}
	//		}
	//	}
	//}

	/**
	 * Function: 洗练武将请求, 安装总属性的1/10进行洗练，总的洗练不变
	 */
	public void washHeroRq(WashHeroRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查英雄是否存在
		Map<Integer, Hero> heros = player.getHeros();
		int heroId = req.getHeroId();
		// 有无英雄
		Hero hero = heros.get(heroId); // 要上阵的英雄Id
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(heroId);
		if (staticHero.getQuality() < Quality.BLUE.get()) {
			handler.sendErrorMsgToPlayer(GameError.HERO_CAN_NOT_WASH);
			return;
		}

		if (!heroManager.checkHero(hero, handler)) {
			return;
		}

		// 检测洗练类型
		int washType = req.getWashType();
		if (washType != 1 && washType != 2) {
			handler.sendErrorMsgToPlayer(GameError.WASH_TYPE_ERROR);
			return;
		}

		Lord lord = player.getLord();
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		int washHeroPrice = staticLimit.getWashHeroPrice();
		if (washType == 1) {
			if (lord.getWashHeroTimes() <= 0) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_WASH_HERO_TIMES);
				return;
			}
		} else {
			int gold = lord.getGold();
			if (washHeroPrice <= 0) {
				handler.sendErrorMsgToPlayer(GameError.WASH_HERO_PRICE_ERROR);
				return;
			}

			if (washHeroPrice > gold) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
		}

		// 开始洗练
		// 获取额外属性值
		Property qualifyProp = hero.getQualifyProp();
		Property property = handleWash(qualifyProp, staticHero, washType);
		qualifyProp.clear();
		qualifyProp.add(property);

		// 计算英雄属性
		heroManager.caculateProp(hero, player);
		WashHeroRs.Builder builder = WashHeroRs.newBuilder();
		int costGold = 0;
		if (washType == 1) {
			lord.setWashHeroTimes(lord.getWashHeroTimes() - 1);
		} else {
			int count = staticLimitMgr.getNum(260);
			count = count == 0 ? 10 : count;
			ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_HALF_WASH);
			if (actRecord != null && actRecord.getStatus(0L) < count) {
				float actFactor = activityManager.actDouble(ActivityConst.ACT_HALF_WASH);// 折扣比例
				if (actFactor > 0 && 1 > actFactor) {
					washHeroPrice = (int) (washHeroPrice * (1 - actFactor));
				}
			}
			costGold = washHeroPrice;
			playerManager.subAward(player, AwardType.GOLD, 1, washHeroPrice, Reason.WASH_HERO);
			activityManager.updActData(player, ActivityConst.TYPE_ADD, 0L, 1, ActivityConst.ACT_HALF_WASH);
		}

		builder.setHeroId(heroId);
		builder.setWashTimes(lord.getWashHeroTimes());
		builder.setGold(lord.getGold());
		builder.setProperty(hero.getTotalProp().wrapPb());
		builder.setQualifyProp(qualifyProp.wrapPb());
		builder.setWashHeroEndTime(lord.getWashHeroEndTime() + staticLimit.getWashHeroInterval() * TimeHelper.SECOND_MS);
		handler.sendMsgToPlayer(GameError.OK, WashHeroRs.ext, builder.build());

		heroManager.updateHero(player, hero, Reason.WASH_HERO);
		heroManager.synBattleScoreAndHeroList(player, hero);

		// 记录当前武将是否洗满
		boolean heroWashFull = heroManager.isHeroWashFull(hero);
		if (heroWashFull) {
			player.updateWashHeroNum(heroId, staticHero.getQuality());
		}

		/**
		 * 英雄洗练日志埋点
		 */
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		logUser.heroWashLog(HeroWashLog.builder().roleId(player.getLord().getLordId()).rolelv(player.getLevel()).roleCreateTime(player.account.getCreateDate()).heroType(staticHero.getHeroType()).heroId(hero.getHeroId()).heroName(staticHero.getHeroName()).heroLev(hero.getHeroLv()).quality(staticHero.getQuality()).attack(hero.getQualifyProp().getAttack()).defence(hero.getQualifyProp().getDefence()).soldierNum(hero.getQualifyProp().getSoldierNum()).maxTotalLimit(staticHero.getMaxTotal()).flag(heroWashFull).channel(player.account.getChannel()).costGold(costGold).build());

		// TODO 事件触发的活动
		activityEventManager.activityTip(EventEnum.HERO_WASH, player, 1, 0);
		// 更新通行证活动进度
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.HERO_WASH, 1);
//		activityManager.updActData(player, ActivityConst.TYPE_ADD, 0L, 1, ActivityConst.ACT_HERO_WASH);

		// 英雄升级报送
		List param = Lists.newArrayList(staticHero.getHeroId(), staticHero.getHeroType(), staticHero.getQuality(), hero.getQualifyProp().getAttack(), hero.getQualifyProp().getDefence(), hero.getQualifyProp().getSoldierNum(), staticHero.getMaxTotal(), staticHero.getMaxTotal(), heroWashFull ? 1 : 0, hero.getAdvanceProcess());
		SpringUtil.getBean(EventManager.class).hero_train(player, param);
		dailyTaskManager.record(DailyTaskId.WASH_HERO, player, 1);
	}

	// 1.计算分配比例
	// 2.彩色部分4/5按照比例返还
	// 3.彩色部分1/5拿出来洗+洗出来的点,设置上下限
	public Property handleWash(Property qualifyProp, StaticHero config, int washType) {
		if (qualifyProp.isZero()) {
			qualifyProp.setAttack(config.getAttack());
			qualifyProp.setDefence(config.getDefence());
			qualifyProp.setSoldierNum(config.getSoldierCount());
		}

		Property oldProperty = new Property(qualifyProp);
		// 1.计算分配比例: 用来计算最后的每一项的白字
		// int totalBase = config.getAttack() + config.getDefence() + config.getSoldierCount();
		// 当前资质的总点数
		int currentAttri = qualifyProp.getAttack() + qualifyProp.getDefence() + qualifyProp.getSoldierNum();
		// 属性比例
		double attackRatio = (double) qualifyProp.getAttack() / (double) currentAttri;
		double defenceRatio = (double) qualifyProp.getDefence() / (double) currentAttri;

		// 总白字配置基础值
		int initExtra = config.getInitExtra(); // 白字
		// 总彩字配置值
		int maxConfig = config.getMaxTotal(); // 彩字
		// 增加上限判断
		if (currentAttri >= initExtra + maxConfig) {
			currentAttri = initExtra + maxConfig;
		}
		// 洗练比例
		double process = (double) currentAttri / (double) (initExtra + maxConfig);
		int floorProcess = (int) (Math.floor(process * 1000));
		// 获取洗练配置
		StaticHeroWash staticHeroWash = staticHeroDataMgr.getWashRate(floorProcess, washType);
		int addAttri = getAttri(staticHeroWash, floorProcess, washType);
		int total = initExtra + maxConfig;
		// 最多能增加的属性
		int maxAdd = total - currentAttri; // 最多能增加的点数
		addAttri = Math.min(maxAdd, addAttri);
		boolean isSpecial = RandomUtil.getRandomNumber(RATE_NUM) <= staticHeroWash.getRate();
//        boolean isSpecial = true;
		// 按照指定比例分配属性
		if (isSpecial) {
			int totalAttri = currentAttri + addAttri;
			int attackRes = Double.valueOf((double) totalAttri / total * config.getMaxAttack()).intValue();
			int defenceRes = Double.valueOf((double) totalAttri / total * config.getMaxDefence()).intValue();
			int soldierRes = totalAttri - attackRes - defenceRes;
			Property property = new Property(attackRes, defenceRes, soldierRes);
			return property;
//            List<Integer> rates = staticLimitMgr.getAddtion(SimpleId.WASH_HERO);
//            if (rates.size() >= 3) {
//                int totalAttri =
////
////                int attackRes = Double.valueOf(totalAttri * rates.get(0) / Double.valueOf(GOOD_LOOT_RATE)).intValue();
////                int defenceRes = Double.valueOf(totalAttri * rates.get(1) / Double.valueOf(GOOD_LOOT_RATE)).intValue();
//
//                int attackRes = Double.valueOf(totalAttri * rates.get(0) / Double.valueOf(GOOD_LOOT_RATE)).intValue();
//                int defenceRes = Double.valueOf(totalAttri * rates.get(1) / Double.valueOf(GOOD_LOOT_RATE)).intValue();
//
//
//                int soldierRes = totalAttri - attackRes - defenceRes;
//                //攻击随机已经超过上限了
//                if (attackRes > config.getMaxAttack()) {
//                    int realAttackRes = Math.min(attackRes, config.getMaxAttack());
//                    //多出的点数 按照比例分配给防御和兵力
//                    int attriRes = attackRes - realAttackRes;
//                    attackRes = realAttackRes;
//                    //先算给兵力的
//                    Double lessRates = rates.get(2) / Double.valueOf(rates.get(1) + rates.get(2));
//                    //实际应该给兵力的
//                    int addToDefence = attriRes - lessRates.intValue();
//                    defenceRes += addToDefence;
//                    soldierRes += lessRates;
//                }
//                //防御也超过上限了
//                if (defenceRes > config.getMaxDefence()) {
//                    int addToSoldier = defenceRes - config.getMaxDefence();
//                    defenceRes = config.getMaxDefence();
//                    soldierRes += addToSoldier;
//                }
//
//                Property property = new Property(attackRes, defenceRes, soldierRes);
//                return property;
//            }
		}

		// 3.彩色部分4/5按照比例返还
		// 彩色部分总和: 当前资质 - initExtra
		int colorTotal = currentAttri - config.getInitExtra();
		int colorDevide = (int) ((double) colorTotal * 4.0 / 5.0);
		int leftColor = colorTotal - colorDevide;
		// 返还
		int backAttack = qualifyProp.getAttack() - (int) (leftColor * attackRatio);
		int backDefence = qualifyProp.getDefence() - (int) (leftColor * defenceRatio);
		int backSoldier = currentAttri - backAttack - backDefence - leftColor;

		// 彩色部分1/5拿出来洗+洗出来的点,设置上下限
		int attackRes = 0;
		int defenceRes = 0;
		int soldierRes = 0;
		// 总的参与洗练值
		ArrayList<Integer> randNum = getRandNum(addAttri, leftColor);
		attackRes = backAttack + randNum.get(0);
		defenceRes = backDefence + randNum.get(1);
		soldierRes = backSoldier + randNum.get(2);
		int tryTimes = 0;
		while (attackRes <= 0 || attackRes > config.getMaxAttack() || defenceRes <= 0 || defenceRes > config.getMaxDefence() || soldierRes <= 0 || soldierRes > config.getMaxSoldierCount() || (attackRes == qualifyProp.getAttack() && defenceRes == qualifyProp.getDefence() && soldierRes == qualifyProp.getSoldierNum())) {
			randNum = getRandNum(addAttri, leftColor);
			// 基础白字 + 4/5 彩色 + 1/5 参与洗练值
			attackRes = backAttack + randNum.get(0);
			defenceRes = backDefence + randNum.get(1);
			soldierRes = backSoldier + randNum.get(2);
			// 洗练尝试次数
			tryTimes++;
			if (tryTimes > 50) {
				LogHelper.CONFIG_LOGGER.info("tryTimes > 50");
				break;
			}
		}

		if (attackRes <= 0 || attackRes > config.getMaxAttack() || defenceRes <= 0 || defenceRes > config.getMaxDefence() || soldierRes <= 0 || soldierRes > config.getMaxSoldierCount()) {
			return oldProperty;
		} else {
			Property property = new Property(attackRes, defenceRes, soldierRes);
			return property;
		}
	}

	public ArrayList<Integer> getRandNum(int addAttri, int leftColor) {
		int leftTotal = addAttri + leftColor;
		int attackRand = 0;
		int defenceRand = 0;
		int soldierRand = 0;
		// 随机洗练
		attackRand = RandomHelper.threadSafeRand(1, leftTotal);
		leftTotal -= attackRand;
		if (leftTotal >= 1) {
			defenceRand = RandomHelper.threadSafeRand(1, leftTotal);
		}
		leftTotal -= defenceRand;
		soldierRand = leftTotal;
		long seed = System.nanoTime();
		ArrayList<Integer> randNum = new ArrayList<Integer>();
		randNum.add(attackRand);
		randNum.add(defenceRand);
		randNum.add(soldierRand);
		Collections.shuffle(randNum, new Random(seed));
		return randNum;
	}

	/**
	 * 根据洗练配置获取额外加成的属性
	 *
	 * @param staticHeroWash
	 * @param floorProcess
	 * @param washType
	 * @return
	 */
	public int getAttri(StaticHeroWash staticHeroWash, int floorProcess, int washType) {
		if (staticHeroWash == null) {
			return 0;
		}
		int addAttri = 0;
		// 检查进度区间
		List<List<Integer>> washRateList = staticHeroWash.getWashRate();
		if (washRateList == null) {
			return 0;
		}

		// 检查配置合法性
		int totalRate = 0;
		for (List<Integer> itemRate : washRateList) {
			if (itemRate.size() != 2) {
				LogHelper.CONFIG_LOGGER.info("itemRate.size()  != 2");
				continue;
			}
			totalRate += itemRate.get(1);
		}

		if (totalRate != RATE_NUM) {
			LogHelper.CONFIG_LOGGER.info("totalRate  != 1000");
			return 0;
		}

		// 随机掉落，检查掉落区间
		int rate = RandomHelper.randHeroRate();
		int currentRate = 0;

		for (List<Integer> itemRate : washRateList) {
			currentRate += itemRate.get(1);
			if (rate <= currentRate) {
				addAttri = itemRate.get(0);
				break;
			}
		}

		return addAttri;
	}

	/**
	 * Function: 武将寻访
	 */
	public void lootHero(HeroPb.LootHeroRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int lootId = req.getLootId();
		StaticLootHero staticLootHero = staticHeroDataMgr.getStaticLootHero(lootId);
		if (staticLootHero == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_LOOT_HERO_ID);
			return;
		}

		// 检查等级
		int needLevel = staticLimitMgr.getStaticLimit().getLootHeroNeedLevel();
		if (player.getLevel() < needLevel) {
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}

		int lootType = staticLootHero.getLootType();

		int lootCommontHero = player.getLord().getLootCommonHero();
		if (lootType == 1 || lootType == 2) {
			lootCommonHero(player, staticLootHero, handler);
		} else if (lootType == 3 || lootType == 4) {
			lootGoodHeroRefactor(player, staticLootHero, handler);
		} else {
			handler.sendErrorMsgToPlayer(GameError.ERROR_CONFIG_LOOT_HERO_TYPE);
			return;
		}
		surpriseGiftManager.doSurpriseGift(player, SuripriseId.SearchHero, lootCommontHero == 0 ? 1 : lootCommontHero, true);
	}

	// 良将, 时间发送到登录
	public void lootCommonHero(Player player, StaticLootHero staticLootHero, ClientHandler handler) {
		Lord lord = player.getLord();
		// 有免费次数扣免费次数，没有扣金币
		long endTime = lord.getLootCommonHeroTime();
		long now = System.currentTimeMillis();
		int nowToken = 0; // 玩家现有搜寻令个数
		int propId = 0; // 搜寻令id
		Iterator<Item> items = player.getItemMap().values().iterator();
		while (items.hasNext()) {
			Item item = items.next();
			if (item.getItemId() == ItemType.SEARCH_TOKEN) {
				nowToken = item.getItemNum();
				propId = item.getItemId();
			}
		}

		int needProp = SearchTokeType.getIndex(staticLootHero.getLootType()).getIndex();// 搜寻时需要扣除的搜寻令
		int totalCost = 0;
		int lootCommon = 0;
		// 次数累加
		if (staticLootHero.getLootType() == 1) {
			if (endTime >= now) {
				if (nowToken >= needProp) {
					playerManager.subAward(player, AwardType.PROP, propId, needProp, Reason.LOOT_COMMON_HERO);
					nowToken--;
					totalCost = 1;
				} else {

					int needGold = staticLootHero.getPrice();
					int owned = player.getGold();
					if (owned < needGold) {
						handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
						return;
					}
					// 扣除金币
					playerManager.subAward(player, AwardType.GOLD, 1, staticLootHero.getPrice(), Reason.LOOT_COMMON_HERO);
					totalCost = staticLootHero.getPrice();

				}
			} else {
				lord.setLootCommonFreeTimes(0);
				StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
				if (staticLimit != null && lord.getLootCommonFreeTimes() <= 0) {
					lord.setLootCommonHeroTime(now + staticLimit.getCommonHeroPeriod() * TimeHelper.SECOND_MS);
					lord.setLootCommonFreeTimes(1);// 重置免费次数刷新时间后,增加一次免费次数
				}
			}
			lootCommon =1;

		} else if (staticLootHero.getLootType() == 2) {

			if (nowToken >= needProp) {
				playerManager.subAward(player, AwardType.PROP, propId, needProp, Reason.LOOT_COMMON_HERO);
				nowToken = nowToken - 10;
				lootCommon =10;
			} else {
				int needGold = staticLootHero.getPrice();
				int owned = player.getGold();
				if (owned < needGold) {
					handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
					return;
				}
				// 扣除金币
				playerManager.subAward(player, AwardType.GOLD, 1, staticLootHero.getPrice(), Reason.LOOT_COMMON_HERO);
				totalCost = staticLootHero.getPrice();
				lootCommon =10;
				//lord.setLootCommonHero(lord.getLootCommonHero() + 10);
			}
		}
		lord.setLootCommonHero(lord.getLootCommonHero() + lootCommon);

		achievementService.addAndUpdate(player,AchiType.AT_6,lootCommon);
		int awardId = staticLootHero.getAwardId();
		int lootType = staticLootHero.getLootType();
		if (lord.getLootCommonHero() != 0 && lord.getLootCommonHero() % 10 == 0 && lootType == HeroLootType.SINGLE_LOOT_COMMON) {
			awardId = staticLootHero.getAdditionAwardId();
		}

		// 最终的掉落
		List<Integer> lootAwardIndex = new ArrayList<Integer>();
		List<Integer> heroIds = new ArrayList<Integer>();
		ArrayList<Award> lootAwards = lootManager.doLootItem(player, awardId, Reason.LOOT_COMMON_HERO, lootAwardIndex, heroIds);
		ArrayList<Award> additionAwards = new ArrayList<Award>();
		if (lootType == HeroLootType.TEN_LOOT_COMMON) {
			additionAwards = lootManager.doLootItem(player, staticLootHero.getAdditionAwardId(), Reason.LOOT_COMMON_HERO, lootAwardIndex, heroIds);
		}

		// 增加10抽道具
		if (!additionAwards.isEmpty()) {
			lootAwards.addAll(additionAwards);
		}

		// 增加神将抽取进度
		int times = 1;
		if (staticLootHero.getLootType() == 1) {
			times = 1;

		} else if (staticLootHero.getLootType() == 2) {
			times = 10;
		}

		int searchTimes = times;

		List<Integer> addtion = staticLimitMgr.getAddtion(182);// 普通搜寻额外赠送的突破卡配置
		if (null != addtion && addtion.size() == 3) {
			playerManager.addAward(player, addtion.get(0), addtion.get(1), addtion.get(2) * times, Reason.LOOT_GOOD_HERO);
		}

		if (lord.getLootGoodHeroEndTime() <= now) {
			double searchSurpised = 1 + activityManager.actDouble(ActivityConst.ACT_SEARCH_SURPRISED);
			times = Double.valueOf(searchSurpised * times).intValue();
			int current = lord.getGoodHeroProcess() + times * staticLimitMgr.getStaticLimit().getGoodHeroProcessAdd();
			if (current >= GOOD_LOOT_RATE) {
				current = GOOD_LOOT_RATE;
			}
			lord.setGoodHeroProcess(current);
		}

		SimpleData simpleData = player.getSimpleData();
		if (lord.getGoodHeroProcess() >= GOOD_LOOT_RATE && lord.getLootGoodHeroEndTime() <= now) {
			// 开启神将抽取
			lord.setLootGoodFreeTimes(1);
			simpleData.setLootGoodTotalTimes(0);
		}

		// 超过10次重置
//        if (lord.getLootCommonHero() >= CommonDefine.LOOT_HERO_TIMES) {
//            lord.setLootCommonHero(lord.getLootCommonHero() % CommonDefine.LOOT_HERO_TIMES);
//        }

		/*
		 * if (staticLootHero.getLootType() == 2) { if (lootAwardIndex.size() == 10) { // 修复显示问题 //int bingoIndex = 10 - lord.getLootCommonHero() - 1; int bingoIndex = 10 - lord.getLootCommonHero()-1; Collections.swap(lootAwardIndex, bingoIndex, 9); Award award = lootAwards.get(bingoIndex); Award lastAward = lootAwards.get(9); Award.swap(award, lastAward); } }
		 */
		HeroPb.LootHeroRs.Builder builder = HeroPb.LootHeroRs.newBuilder();
		for (Award award : lootAwards) {
			builder.addAward(award.wrapPb());
		}

		builder.setGold(player.getGold());

		builder.setCommonHeroTimes(lord.getLootCommonHero() % CommonDefine.LOOT_HERO_TIMES);
		builder.setGoodHeroProcess(lord.getGoodHeroProcess());
		builder.setCommonHeroEndTime(lord.getLootCommonHeroTime());

		for (Integer awardIndex : lootAwardIndex) {
			builder.addRealAwardIndex(awardIndex);
		}
		Map<Integer, Hero> heroMap = player.getHeros();
		for (Integer heroId : heroIds) {
			Hero hero = heroMap.get(heroId);
			if (hero != null) {
				builder.addHero(hero.wrapPb());
			}
		}
		builder.setGoodHeroEndTime(lord.getLootGoodHeroEndTime());
		builder.setLootGoodFreeTimes(lord.getLootGoodFreeTimes());
		CommonPb.Prop.Builder prop = CommonPb.Prop.newBuilder();
		prop.setPropNum(nowToken);
		prop.setPropId(propId);
		builder.setProp(prop);
		builder.setGoodHeroTimes(simpleData.getLootGoodTotalTimes() % 10);
		handler.sendMsgToPlayer(HeroPb.LootHeroRs.ext, builder.build());

		doCommonHeroTask(player, times);
		SpringUtil.getBean(EventManager.class).summon(player, 0, totalCost, lootAwards, staticLootHero.getLootType());
		SpringUtil.getBean(LogUser.class).seek_log(SeekLog.builder().lordId(player.roleId).level(player.getLevel()).nick(player.getNick()).vip(player.getVip()).serarchType(staticLootHero.getLootType()).searchNum(searchTimes).costGold(totalCost).build());
	}

	public void doCommonHeroTask(Player player, int times) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(times);
		taskManager.doTask(TaskType.COMMON_HERO, player, triggers);
	}

	// 第一个五次必得神将
	// 写好注释
	public void lootGoodHeroRefactor(Player player, StaticLootHero staticLootHero, ClientHandler handler) {
		// 神将抽取不开启不能抽奖
		Lord lord = player.getLord();
		SimpleData simpleData = player.getSimpleData();
		long endTime = lord.getLootGoodHeroEndTime();
		long now = System.currentTimeMillis();
		int nowAdvancedToken = 0; // 玩家现有高级搜寻令个数
		int AdvancedPropId = 0;
		Iterator<Item> items = player.getItemMap().values().iterator();
		while (items.hasNext()) {
			Item item = items.next();
			if (item.getItemId() == ItemType.ADVANCED_SEARCH_TOKEN) {
				nowAdvancedToken = item.getItemNum();
				AdvancedPropId = item.getItemId();
			}
		}
		// 神将抽取进度不足, 则返回错误码
		if (lord.getGoodHeroProcess() < GOOD_LOOT_RATE && endTime <= now) {
			handler.sendErrorMsgToPlayer(GameError.GOOD_HERO_PROCESS_LESS);
			return;
		}

		// 如果超过进度，且倒计时到，则清除相关的状态后进行抽取
		if (lord.getGoodHeroProcess() >= GOOD_LOOT_RATE && endTime <= now) {
			// 开启神将抽取

			long period = staticLimitMgr.getStaticLimit().getGoodHeroPeriod() * TimeHelper.SECOND_MS;
			lord.setLootGoodHeroEndTime(now + period); // 倒计时
			lord.setLootGoodFreeTimes(1); // 每次开启+1，关闭减1
			lord.setLootGoodHeroFiveTimes(0); // 抽取5次必中
			lord.setGoodHeroProcess(0);
			simpleData.setLootGoodTotalTimes(0);
			simpleData.setThreeLootHasHero(0);
			simpleData.setLootGoodState(LootGoodState.Special_Three_Loot);
		}
		int needProp = SearchTokeType.getIndex(staticLootHero.getLootType()).getIndex();// 搜寻时需要扣除的搜寻令
		// 增加抽取次数
		int times = 0;
		int totalCost = 0;
		if (staticLootHero.getLootType() == 3) {
			times = 1;
			// 根据是否有免费次数扣次数或者金币
			int lootGoodFreeTimes = lord.getLootGoodFreeTimes();
			if (lootGoodFreeTimes <= 0) {
				if (nowAdvancedToken >= needProp) {
					playerManager.subAward(player, AwardType.PROP, AdvancedPropId, needProp, Reason.LOOT_COMMON_HERO);
					nowAdvancedToken--;
					totalCost = 1;
				} else {
					int needGold = staticLootHero.getPrice();
					int owned = player.getGold();
					if (owned < needGold) {
						handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
						return;
					}

					// 扣除金币
					playerManager.subAward(player, AwardType.GOLD, 1, staticLootHero.getPrice(), Reason.LOOT_GOOD_HERO);
					totalCost = staticLootHero.getPrice();
				}
			} else {
				lord.setLootGoodFreeTimes(lord.getLootGoodFreeTimes() - 1);
			}
		} else if (staticLootHero.getLootType() == 4) {
			times = 10;
			if (nowAdvancedToken >= needProp) {
				playerManager.subAward(player, AwardType.PROP, AdvancedPropId, needProp, Reason.LOOT_COMMON_HERO);
				nowAdvancedToken = nowAdvancedToken - 10;
				totalCost = 10;
			} else {

				int needGold = staticLootHero.getPrice();
				int owned = player.getGold();
				if (owned < needGold) {
					handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
					return;
				}

				// 扣除金币
				playerManager.subAward(player, AwardType.GOLD, 1, staticLootHero.getPrice(), Reason.LOOT_GOOD_HERO);
				totalCost = staticLootHero.getPrice();

			}
		}
		List<Integer> addtion = staticLimitMgr.getAddtion(183);// 普通搜寻额外赠送的突破卡配置
		if (null != addtion && addtion.size() == 3) {
			playerManager.addAward(player, addtion.get(0), addtion.get(1), addtion.get(2) * times, Reason.LOOT_GOOD_HERO);
		}

		// 最终的掉落
		List<Integer> lootAwardIndex = new ArrayList<Integer>();
		List<Integer> heroIds = new ArrayList<Integer>();
		ArrayList<Award> lootAwards = new ArrayList<Award>();
		achievementService.addAndUpdate(player,AchiType.AT_7,times);
		// 随机掉落[]
		for (int index = 1; index <= times; index++) {
			simpleData.setLootGoodTotalTimes(simpleData.getLootGoodTotalTimes() + 1);
			// 根据当前状态进行抽取
			if (simpleData.getLootGoodTotalTimes() >= 1 && simpleData.getLootGoodTotalTimes() <= 3) {
				simpleData.setLootGoodState(LootGoodState.Special_Three_Loot);
			} else if (simpleData.getLootGoodTotalTimes() == 4 && simpleData.getThreeLootHasHero() == 1) {
				simpleData.setLootGoodState(LootGoodState.Four_Loot_Hero_Chip);
			} else if (simpleData.getLootGoodTotalTimes() == 4 && simpleData.getThreeLootHasHero() == 0) {
				simpleData.setLootGoodState(LootGoodState.Four_Loot_Hero);
			} else if (lord.getLootGoodHeroFiveTimes() == 0 && simpleData.getLootGoodTotalTimes() == 5) {
				simpleData.setLootGoodState(LootGoodState.Five_Times_Speical);
				lord.setLootGoodHeroFiveTimes(1);
			} else if (simpleData.getLootGoodTotalTimes() % 10 == 0) {
				simpleData.setLootGoodState(LootGoodState.Ten_Times_Special);
			} else {
				simpleData.setLootGoodState(LootGoodState.Common_Loot);
			}
			doLootAward(player, lootAwards, simpleData, staticLootHero, lootAwardIndex, heroIds);
		}

		// 分享走马灯
		List<StaticHero> shareList = new ArrayList<StaticHero>();
		HeroPb.LootHeroRs.Builder builder = HeroPb.LootHeroRs.newBuilder();
		for (Award award : lootAwards) {
			builder.addAward(award.wrapPb());
			if (award.getType() == AwardType.HERO) {
				StaticHero staticHero = staticHeroDataMgr.getStaticHero(award.getId());
				if (staticHero != null && staticHero.getShare() == 1) {
					shareList.add(staticHero);
				}
			}
		}

		builder.setGold(player.getGold());
		builder.setGoodHeroTimes(simpleData.getLootGoodTotalTimes() % 10);
		builder.setGoodHeroEndTime(lord.getLootGoodHeroEndTime());
		builder.setLootGoodFreeTimes(lord.getLootGoodFreeTimes());
		for (Integer awardIndex : lootAwardIndex) {
			builder.addRealAwardIndex(awardIndex);
		}

		Map<Integer, Hero> heroMap = player.getHeros();
		for (Integer heroId : heroIds) {
			Hero hero = heroMap.get(heroId);
			if (hero != null) {
				builder.addHero(hero.wrapPb());
			}
		}
		CommonPb.Prop.Builder prop = CommonPb.Prop.newBuilder();
		prop.setPropNum(nowAdvancedToken);
		prop.setPropId(AdvancedPropId);
		builder.setProp(prop);
		handler.sendMsgToPlayer(HeroPb.LootHeroRs.ext, builder.build());
		// 完成神将抽取任务
		doGoodHeroTask(player, times);
		// 神将分享
		for (StaticHero staticHero : shareList) {
			String params[] = {player.getNick(), staticHero.getHeroName()};
			chatManager.sendWorldChat(ChatId.GOT_GOD_HERO, params);
		}
		SpringUtil.getBean(EventManager.class).summon(player, 1, totalCost, lootAwards, staticLootHero.getLootType());
		SpringUtil.getBean(LogUser.class).seek_log(SeekLog.builder().lordId(player.roleId).level(player.getLevel()).nick(player.getNick()).vip(player.getVip()).serarchType(staticLootHero.getLootType()).searchNum(times).costGold(totalCost).build());
	}

	// 一次只掉落一个, 最终加上所有的掉落
	// 不管是单抽和10抽，获取当前的配置
	public void doLootAward(Player player, ArrayList<Award> lootAwards, SimpleData simpleData, StaticLootHero staticLootHero, List<Integer> lootAwardIndex, List<Integer> heroIds) {
		if (simpleData.getLootGoodState() == LootGoodState.Special_Three_Loot) {
			int awardId = staticLootHero.getThreeLoot();
			ArrayList<Award> awards = lootManager.doLootItem(player, awardId, Reason.LOOT_GOOD_HERO, lootAwardIndex, heroIds);
			lootAwards.addAll(awards);
			if (!heroIds.isEmpty()) {
				simpleData.setThreeLootHasHero(1);
			}
		} else if (simpleData.getLootGoodState() == LootGoodState.Four_Loot_Hero) {
			int awardId = staticLootHero.getFourLoot1();
			ArrayList<Award> awards = lootManager.doLootItem(player, awardId, Reason.LOOT_GOOD_HERO, lootAwardIndex, heroIds);
			lootAwards.addAll(awards);
		} else if (simpleData.getLootGoodState() == LootGoodState.Four_Loot_Hero_Chip) {
			int awardId = staticLootHero.getFourLoot2();
			ArrayList<Award> awards = lootManager.doLootItem(player, awardId, Reason.LOOT_GOOD_HERO, lootAwardIndex, heroIds);
			lootAwards.addAll(awards);
		} else if (simpleData.getLootGoodState() == LootGoodState.Five_Times_Speical) {
			int awardId = staticLootHero.getAdditionAwardId();
			ArrayList<Award> awards = lootManager.doLootItem(player, awardId, Reason.LOOT_GOOD_HERO, lootAwardIndex, heroIds);
			lootAwards.addAll(awards);
		} else if (simpleData.getLootGoodState() == LootGoodState.Ten_Times_Special) {
			int awardId = staticLootHero.getAdditionAwardId();
			ArrayList<Award> awards = lootManager.doLootItem(player, awardId, Reason.LOOT_GOOD_HERO, lootAwardIndex, heroIds);
			lootAwards.addAll(awards);
		} else if (simpleData.getLootGoodState() == LootGoodState.Common_Loot) {
			int awardId = staticLootHero.getAwardId();
			ArrayList<Award> awards = lootManager.doLootItem(player, awardId, Reason.LOOT_GOOD_HERO, lootAwardIndex, heroIds);
			lootAwards.addAll(awards);
		}

	}

	public void doGoodHeroTask(Player player, int times) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(times);
		taskManager.doTask(TaskType.GOOD_HERO, player, triggers);
	}

	// 英雄突破
	public void advanceHero(HeroPb.AdvanceHeroRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int heroId = req.getHeroId();
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(heroId);
		if (staticHero.getCanAdvance() == AdvanceType.CAN_NOT_ADVANCE) {
			handler.sendErrorMsgToPlayer(GameError.HERO_CAN_NOT_ADVANCE);
			return;
		}

		// 替换武将的Id
		int advanceId = staticHero.getAdvancedId();
		StaticHero advanceConfig = staticHeroDataMgr.getStaticHero(advanceId);
		if (advanceConfig == null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_ADVANCE_CONFIG_NULL);
			return;
		}

		// 武将突破价格
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		int limitLordLv = staticLimit.getHeroAdvanceLordLv();
		if (player.getLevel() < limitLordLv) {
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}

		// 检查将令
		int advanceItemId = staticLimit.getAdvanceItemId();
		Map<Integer, Item> itemMap = player.getItemMap();
		Item item = itemMap.get(advanceItemId);
		if (item == null) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		int advanceCost = staticLimit.getAdvanceCost();
		if (item.getItemNum() < staticLimit.getAdvanceCost()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		// 开始突破, 计算突破概率 当前将令值/总将令值
		int quality = staticHero.getQuality();
		int type = 0;
		if (quality == Quality.GREEN.get()) {
			type = 1;
		} else if (quality == Quality.GOLD.get()) {
			type = 2;
		} else if (quality == Quality.RED.get()) {
			type = 3;
		}

		StaticHeroAdvance staticHeroAdvance = staticHeroDataMgr.getStaticHeroAdvance(type);
		if (staticHeroAdvance == null) {
			handler.sendErrorMsgToPlayer(GameError.ADVANCE_HERO_TYPE_ERROR);
			return;
		}

		Map<Integer, Hero> heroMap = player.getHeros();
		Hero hero = heroMap.get(heroId);
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}

		// 出征的武将不能突破
		if (!playerManager.isHeroFree(player, heroId)) {
			handler.sendErrorMsgToPlayer(GameError.HERO_STATE_CAN_NOT_ADVANCE);
			return;
		}

		if (!heroManager.checkHero(hero, handler)) {
			return;
		}

		Hero checkAdvanceHero = heroMap.get(advanceId);
		if (checkAdvanceHero != null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_ADVANCE_ALREADY_EXISTS);
			return;
		}

		// 先扣将令，再计算概率
		playerManager.subAward(player, AwardType.PROP, advanceItemId, advanceCost, Reason.ADVANCE_HERO);
		hero.setAdvanceProcess(hero.getAdvanceProcess() + advanceCost);

		int advanceProcess = hero.getAdvanceProcess();
		int totalCost = staticHeroAdvance.getCost();
		double percent = (double) advanceProcess / (double) totalCost;
		hero.setTryAdvanceTimes(hero.getTryAdvanceTimes() + 1);
		// 计算突破概率
		// y=x/(x+c1),c1为品质控制值，X为点击突破的次数,原突破值满时直接成功规则不变
		// 优化突破概率公式为=x/（x+2c1）
		if (percent < 1.0) {
			percent = (double) hero.getTryAdvanceTimes() / (double) (hero.getTryAdvanceTimes() + 2 * staticHeroAdvance.getC1());
		}

		// 科技增加的突破成功率
		double techAdd = techManager.getAdvanceRate(player);
		double totalPercent = percent + techAdd;

		int rand = (int) (totalPercent * 100);
		Random random = new Random(System.nanoTime());
		int randNum = random.nextInt(100) + 1;

		boolean advanceSuccess = false;
		if (randNum <= rand) {
			advanceSuccess = true;
			// LogHelper.CONFIG_LOGGER.info("randNum = " + randNum + ", rand = " + rand);
		}

		Lord lord = player.getLord();
//        if (endTime <= now) {
//            int current = lord.getGoodHeroProcess() + staticLimitMgr.getStaticLimit().getGoodHeroProcessAdd();
//            if (current >= GOOD_LOOT_RATE) {
//                current = GOOD_LOOT_RATE;
//            }
//            lord.setGoodHeroProcess(current);
//        }
		SimpleData simpleData = player.getSimpleData();
		// 突破值每天凌晨清0
		// 突破成功，改变武将Id, 洗练的资质替换过去, 重新计算属性
		// 突破失败, 扣除将令, 增加突破进度

		if (advanceSuccess) {
			int count = staticLimitMgr.getAddtion(SimpleId.HERO_ADVANCE_ADD).get(type - 1);
			int current = lord.getGoodHeroProcess() + count;
			if (current >= GOOD_LOOT_RATE) {
				current = GOOD_LOOT_RATE;
			}
			lord.setGoodHeroProcess(current);
			// 进度达到且时间达到 就刷新次数
			if (lord.getGoodHeroProcess() >= GOOD_LOOT_RATE && lord.getLootGoodHeroEndTime() <= System.currentTimeMillis()) {
				// 开启神将抽取
				lord.setLootGoodFreeTimes(1);
				simpleData.setLootGoodTotalTimes(0);
			}
			// 资质重新计算
			Hero advanceHero = heroManager.advanceHero(hero, player);
			// 布阵英雄id
			handleAdvance(player, hero, staticHero);

			HeroPb.AdvanceHeroRs.Builder builder = HeroPb.AdvanceHeroRs.newBuilder();
			builder.setCode(1);
			builder.setProp(item.wrapPb());
			builder.setHero(advanceHero.wrapPb());
			builder.setHeroRemove(heroId);
			builder.setGoodHeroProcess(lord.getGoodHeroProcess());
			builder.setGoodHeroEndTime(lord.getLootGoodHeroEndTime());
			builder.setLootGoodFreeTimes(lord.getLootGoodFreeTimes());
			handler.sendMsgToPlayer(HeroPb.AdvanceHeroRs.ext, builder.build());

			if (type == 3) {// 突破红色突破到紫色则广播
				String[] params = {player.getNick(), String.valueOf(staticHero.getHeroName())};
				chatManager.sendWorldChat(ChatId.HERO_UP, params);
				achievementService.addAndUpdate(player,AchiType.AT_5,1);
			}

			heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());

		} else {
			HeroPb.AdvanceHeroRs.Builder builder = HeroPb.AdvanceHeroRs.newBuilder();
			builder.setCode(2);
			builder.setProp(item.wrapPb());
			builder.setHero(hero.wrapPb());
			builder.setGoodHeroProcess(lord.getGoodHeroProcess());
			builder.setGoodHeroEndTime(lord.getLootGoodHeroEndTime());
			builder.setLootGoodFreeTimes(lord.getLootGoodFreeTimes());
			builder.setGoodHeroTimes(simpleData.getLootGoodTotalTimes());
			handler.sendMsgToPlayer(HeroPb.AdvanceHeroRs.ext, builder.build());
		}
		SpringUtil.getBean(EventManager.class).cost_promote_card(player, Lists.newArrayList(advanceCost, 0, hero.getHeroId(), hero.getHeroId(), hero.getHeroLv(), 1, staticHero.getHeroType(), staticHero.getQuality()));

		/**
		 * 英雄突破日志埋点
		 */
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		logUser.heroAvanceLog(new HeroAdvanceLog(player.roleId, player.account.getCreateDate(), player.getLevel(), staticHero.getHeroType(), hero.getHeroId(), staticHero.getHeroName(), hero.getHeroLv(), staticHero.getQuality(), advanceCost, player.account.getChannel(), player.getItemNum(82)));
	}

	@Autowired
	AchievementService achievementService;

	public void handleAdvance(Player player, Hero hero, StaticHero staticHero) {
		List<Integer> embattleList = player.getEmbattleList();
		for (int i = 0; i < embattleList.size(); i++) {
			if (embattleList.get(i) != null && embattleList.get(i) == hero.getHeroId()) {
				embattleList.set(i, staticHero.getAdvancedId());
				break;

			}
		}

		List<Integer> sweepHeroList = player.getSweepHeroList();
		for (int i = 0; i < sweepHeroList.size(); i++) {
			if (sweepHeroList.get(i) != null && sweepHeroList.get(i) == hero.getHeroId()) {
				sweepHeroList.set(i, staticHero.getAdvancedId());
				break;

			}
		}

		//Wall wall = player.getWall();
		//if (wall != null) {
		//	List<Integer> defenceHero = wall.getDefenceHero();
		//	for (int i = 0; i < defenceHero.size(); i++) {
		//		if (defenceHero.get(i) != null && defenceHero.get(i) == hero.getHeroId()) {
		//			defenceHero.set(i, staticHero.getAdvancedId());
		//			break;
		//
		//		}
		//	}
		//}

		List<Integer> mingHeroList = player.getMiningList();
		for (int i = 0; i < mingHeroList.size(); i++) {
			if (mingHeroList.get(i) != null && mingHeroList.get(i) == hero.getHeroId()) {
				mingHeroList.set(i, staticHero.getAdvancedId());
				break;

			}
		}

		List<WarDefenseHero> defenseArmyList = player.getDefenseArmyList();
		for (int i = 0; i < defenseArmyList.size(); i++) {
			if (defenseArmyList.get(i) != null && defenseArmyList.get(i).getHeroId() == hero.getHeroId()) {
				defenseArmyList.get(i).setHeroId(staticHero.getAdvancedId());
				break;
			}
		}
	}

	// 开启神将抽取
	public void lootOpen(HeroPb.LootOpenRq req, ClientHandler handler) {
		// 检查当前神将的进度
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null.");
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Lord lord = player.getLord();
		// 神将抽取进度不足
		if (lord.getGoodHeroProcess() < 100) {
			handler.sendErrorMsgToPlayer(GameError.LOOT_GOOD_HERO_PROCESS_LOW);
			return;
		}

		// 开启神将抽取
		long now = System.currentTimeMillis();
		long period = staticLimitMgr.getStaticLimit().getGoodHeroPeriod() * TimeHelper.SECOND_MS;
		lord.setLootGoodHeroEndTime(now + period);
		lord.setLootGoodFreeTimes(1);
		lord.setGoodHeroProcess(0);

		HeroPb.LootOpenRs.Builder builder = HeroPb.LootOpenRs.newBuilder();
		builder.setGoodHeroProcess(lord.getGoodHeroProcess());
		builder.setGoodHeroEndTime(lord.getLootGoodHeroEndTime());
		builder.setPeriod(period);
		builder.setLootGoodFreeTimes(lord.getLootGoodFreeTimes());
		handler.sendMsgToPlayer(HeroPb.LootOpenRs.ext, builder.build());
	}

	// 武将上阵任务
	public void doHeroEmbattleTask(Player player, int heroId) {
		List<Integer> arrayList = new ArrayList<Integer>();
		//arrayList.add(heroId);
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(heroId);
		if(staticHero!=null){
			arrayList.add(staticHero.getSoldierType());
		}
		taskManager.doTask(TaskType.EMBATTLE_HERO, player, arrayList);
	}

	// 神将突破
	// 1.武将达到100级才可进行神将突破，只有紫色武将
	// 2.神级突破需要6个不同部位的对应秘技和对应红装
	@Autowired
	StaticMeetingTaskMgr staticMeetingTaskMgr;

	public void divineAdvance(HeroPb.DivineAdvanceRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int heroId = req.getHeroId();
		Hero hero = player.getHero(heroId);
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}

		// 英雄正在行军之中
		if (player.isInMarch(hero)) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
			return;
		}

		if (player.isInMass(heroId)) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IN_MASS);
			return;
		}

		if (!hero.isActivated()) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IS_NOT_ACTIVATE);
			return;
		}

		if (player.hasPvpHero(heroId)) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IN_PVP_BATTLE);
			return;
		}

		// 品质
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(heroId);
		if (staticHero.getQuality() < Quality.PURPLE.get()) {
			handler.sendErrorMsgToPlayer(GameError.HERO_QUALITY_NOT_ENOUGH);
			return;
		}
		int diviNum = hero.getDiviNum();
		StaticHerDiviConfig staticHerDiviConfig = staticHeroDataMgr.getStaticHerDiviConfig(1);
		if (diviNum != 0) {
			staticHerDiviConfig = staticHeroDataMgr.getStaticHerDiviConfig(diviNum);
			if (staticHerDiviConfig == null) {
				handler.sendErrorMsgToPlayer(GameError.ALREADY_SPECIAL_HERO);
				return;
			}
			staticHerDiviConfig = staticHeroDataMgr.getStaticHerDiviConfig(staticHerDiviConfig.getNextId());
			if (staticHerDiviConfig == null || player.getLevel() < staticHerDiviConfig.getLevel()) {
				handler.sendErrorMsgToPlayer(GameError.HERO_LEVEL_NOT_ENOUGH);
				return;
			}
		}
		List<List<Integer>> equiplist = staticHerDiviConfig.getEquiplist();
		List<Integer> equipIdList = req.getEquipIdList();
		if (equipIdList.size() != 6) {
			handler.sendErrorMsgToPlayer(GameError.HERO_EQUIPNUM_NOT_ENOUGH);
			return;
		}
		List<Integer> collect = equiplist.stream().flatMap(x -> x.stream().limit(1)).collect(Collectors.toList());
		for (Integer x : equipIdList) {
			Equip equip = player.getEquips().get(x);
			if (equip == null || (!collect.contains(equip.getEquipId()) && !collect.contains(equip.getEquipId() / 1000))) {
				handler.sendErrorMsgToPlayer(GameError.HERO_EQUIPNUM_NOT_ENOUGH);
				return;
			}
		}
		equipMgr.removeEquips(player, equipIdList);
		Property property = hero.getSpecialProp();
		List<List<Integer>> property1 = staticHerDiviConfig.getProperty();
		property1.forEach(x -> {
			int attr = x.get(0);
			int val = x.get(1);
			if (attr == PropertyType.SOLDIER_NUM) {
				int soldierLines = playerManager.getSoldierLine(player) + staticMeetingTaskMgr.soldierNumByHero(player, hero.getHeroId());
				val = val / 4 * soldierLines;
			}
			property.addValue(attr, val);
		});
		// hero.setSpecialProp(property);
		hero.setDiviNum(staticHerDiviConfig.getId());
		heroManager.caculateProp(hero, player);
		HeroPb.DivineAdvanceRs.Builder builder = HeroPb.DivineAdvanceRs.newBuilder();
		builder.setHero(hero.wrapPb());
		builder.addAllEquipKeyId(equipIdList);
		handler.sendMsgToPlayer(HeroPb.DivineAdvanceRs.ext, builder.build());
		heroManager.synBattleScoreAndHeroList(player, hero);
		int chatId = ChatId.HERO_DIVINE;
		if (hero.getDiviNum() == 2) {
			chatId = ChatId.SECOND_HERO_DIVINE;
		}
		chatManager.sendWorldChat(chatId, String.valueOf(player.getCountry()), player.getNick(), staticHero.getHeroName());
		/**
		 * 英雄晋升传奇级日志埋点
		 */
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		logUser.heroDivineLog(new HeroDivineLog(player.roleId, player.account.getCreateDate(), player.getLevel(), staticHero.getHeroType(), hero.getHeroId(), staticHero.getHeroName(), hero.getHeroLv(), staticHero.getQuality(), player.account.getChannel()));

		// 英雄升级报送
		List param = Lists.newArrayList(staticHero.getHeroId(), staticHero.getHeroType(), staticHero.getQuality(), hero.getQualifyProp().getAttack(), hero.getQualifyProp().getDefence(), hero.getQualifyProp().getSoldierNum(), staticHero.getMaxTotal(), staticHero.getMaxTotal(), 0, hero.getAdvanceProcess());
		SpringUtil.getBean(EventManager.class).hero_become_legend(player, param);
		heroManager.addDivineAdvanceRecord(player, hero);
		achievementService.addAndUpdate(player,AchiType.AT_14,1);

	}

	public void heroLvUp(Player player, int heroId, int lv) {
		int playerLevel = player.getLevel();
		if (lv > playerLevel) {
			return;
		}
		Hero hero = player.getHero(heroId);
		if (hero == null) {
			return;
		}
		// 英雄等级不能高于玩家等级
		int heroQuality = staticHeroDataMgr.getQuality(hero.getHeroId());
		if (heroQuality == 0) {
			LogHelper.CONFIG_LOGGER.info("heroQuality is 0.");
			return;
		}
		hero.setHeroLv(lv);
		heroManager.synBattleScoreAndHeroList(player, hero);
		// 处理国家武将的等级
		heroManager.handleCountryHeroLv(hero);
	}

	public void lookHero(LookHeroRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Player target = playerManager.getPlayer(req.getLordId());
		if (target == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Hero hero = target.getHero(req.getHeroId());
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.NOT_HERO);
			return;
		}
		LookHeroRs.Builder builder = LookHeroRs.newBuilder();
		builder.setHero(hero.wrapPb());
		handler.sendMsgToPlayer(LookHeroRs.ext, builder.build());
	}

	public void telnetHero(TelnetHeroRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(req.getHeroId());
		if (staticHero == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		Hero hero = player.getHero(req.getHeroId());
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.NOT_HERO);
			return;
		}
		if (hero.getDiviNum() < 1) {
			handler.sendErrorMsgToPlayer(GameError.NOT_HERO);
			return;
		}
		int type = req.getType();
		TelnetHeroRs.Builder builder = TelnetHeroRs.newBuilder();
		StaticHeroTalent staticHeroTalent;
		if (type == 1) {
			staticHeroTalent = staticHeroDataMgr.getStaticHeroTalent(staticHero.getTalentType(), hero.getTalentLevel() + 1, staticHero.getSoldierType());
			if (staticHeroTalent == null) {
				handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
				return;
			}
			if (staticHeroTalent != null) {
				List<List<Integer>> consume = staticHeroTalent.getConsume();
				boolean b = playerManager.checkAndSubItem(player, consume, Reason.TELNET);
				if (!b) {
					handler.sendErrorMsgToPlayer(GameError.NO_PROP);
					return;
				}
				hero.setTalentLevel(staticHeroTalent.getLevel());

				consume.forEach(x -> {
					CommonPb.Prop.Builder builder1 = CommonPb.Prop.newBuilder();
					x.forEach(prop -> {
						builder1.setPropId(x.get(1));
						Item item = player.getItem(x.get(1));
						builder1.setPropNum(item == null ? 0 : item.getItemNum());
					});
					builder.addProp(builder1);
				});
			}
		} else {
			List<Integer> addtion = staticLimitMgr.getAddtion(SimpleId.HERO_TALNET);
			boolean flag = false;
			if (addtion != null) {
				List<List<Integer>> lists = new ArrayList<>();
				lists.add(addtion);
				flag = playerManager.checkAndSubItem(player, lists, Reason.USE_ITEM);
			}
			if (!flag) {
				handler.sendErrorMsgToPlayer(GameError.PROP_NOT_ENOUGH);
				return;
			}
			CommonPb.Prop.Builder builder1 = CommonPb.Prop.newBuilder();
			builder1.setPropId(addtion.get(1));
			Item item = player.getItem(addtion.get(1));
			builder1.setPropNum(item == null ? 0 : item.getItemNum());
			builder.addProp(builder1);
			while (hero.getTalentLevel() > 1) {
				staticHeroTalent = staticHeroDataMgr.getStaticHeroTalent(staticHero.getTalentType(), hero.getTalentLevel(), staticHero.getSoldierType());
				if (staticHeroTalent != null) {
					List<List<Integer>> consume = staticHeroTalent.getConsume();
					List<Award> list = new ArrayList<>();
					consume.forEach(x -> {
						list.add(new Award(0, x.get(0), x.get(1), (int) (x.get(2) * 0.8f)));
					});
					playerManager.addAward(player, list, Reason.TELNET);
					hero.setTalentLevel(hero.getTalentLevel() - 1);
					consume.forEach(x -> {
						CommonPb.Prop.Builder b = CommonPb.Prop.newBuilder();
						x.forEach(prop -> {
							b.setPropId(x.get(1));
							Item item1 = player.getItem(x.get(1));
							b.setPropNum(item1 == null ? 0 : item1.getItemNum());
						});
						builder.addProp(b);
					});
				}
			}
		}
		builder.setLevel(hero.getTalentLevel());
		builder.setHeroId(hero.getHeroId());
		heroManager.synBattleScoreAndHeroList(player, hero);

		builder.setProperty(hero.getTotalProp().wrapPb());
		handler.sendMsgToPlayer(TelnetHeroRs.ext, builder.build());
	}

}
