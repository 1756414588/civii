package com.game.dataMgr;

import com.game.activity.ActivityEventManager;
import com.game.constant.ActivityConst;
import com.game.dao.s.StaticDataDao;
import com.game.domain.p.ActRecord;
import com.game.domain.p.ActShopProp;
import com.game.domain.p.ConfigException;
import com.game.domain.s.*;
import com.game.manager.ServerManager;
import com.game.pb.CommonPb;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.RandomHelper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

@Component
public class StaticActivityMgr extends BaseDataMgr {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StaticDataDao staticDataDao;

	@Autowired
	private ServerManager serverManager;

	// 活动列表
	private List<ActivityBase> activityList = new ArrayList<ActivityBase>();
	// 发放邮件列表
	private List<ActivityBase> sendMailList = new ArrayList<ActivityBase>();

	// 活动配置列表
	private Map<Integer, StaticActivity> activityMap = new HashMap<Integer, StaticActivity>();

	// 排行活动显示表
	private Map<Integer, List<StaticActRankDisplay>> rankDisplays = new HashMap<Integer, List<StaticActRankDisplay>>();

	private Map<Integer, Map<Integer, List<StaticActRankDisplay>>> allRankDisplay = new LinkedHashMap<>();

	// 活动奖励配置列表
	private Map<Integer, List<StaticActAward>> actAwards = new HashMap<Integer, List<StaticActAward>>();

	private Map<Integer, StaticDailyCheckin> dailyCheckinAwards = new HashMap<>();
	private Map<Integer, StaticActCommand> actCommands = new HashMap<Integer, StaticActCommand>();

	private Map<Integer, Map<Integer, List<StaticActAward>>> actSortAwards = new HashMap<Integer, Map<Integer, List<StaticActAward>>>();
	private Map<Integer, StaticActAward> actAwardMap = new HashMap<Integer, StaticActAward>();

	// 活动出售
	private Map<Integer, List<StaticActQuota>> quotaMaps = new HashMap<Integer, List<StaticActQuota>>();
	private Map<Integer, StaticActQuota> quotas = new HashMap<Integer, StaticActQuota>();

	// 罗盘数据
	private Map<Integer, DialEntity> dials = new HashMap<Integer, DialEntity>();

	// 镔铁罗盘
	private Map<Integer, StaticActDialStone> stoneDial = new HashMap<Integer, StaticActDialStone>();

	// 幸运罗盘awardId,vip,type
	private Map<Integer, Map<Integer, Map<Integer, StaticActDialLuck>>> luckDial = new HashMap<Integer, Map<Integer, Map<Integer, StaticActDialLuck>>>();

	// 紫装转盘awardId
	private Map<Integer, StaticActDialPurp> purpDial = new HashMap<Integer, StaticActDialPurp>();

	// 活动任务
	private Map<Integer, List<StaticActTask>> tasks = new HashMap<Integer, List<StaticActTask>>();
	private Map<Integer, Map<Integer, StaticActTask>> taskMap = new HashMap<Integer, Map<Integer, StaticActTask>>();

	// 活动商店
	private Map<Integer, Map<Integer, StaticActShop>> shopMap = new HashMap<Integer, Map<Integer, StaticActShop>>();

	// 屯田活动
	private Map<Integer, List<StaticActFoot>> footLists = new HashMap<Integer, List<StaticActFoot>>();

	// 掉落活动
	private Map<Integer, StaticActDrop> dropMaps = new HashMap<Integer, StaticActDrop>();

	// 翻倍活动
	private Map<Integer, StaticActDouble> doubleMaps = new HashMap<Integer, StaticActDouble>();

	// 活动兑换道具
	private Map<Integer, StaticExchangeItem> exchangeItemMap = new HashMap<Integer, StaticExchangeItem>();
	// 活动兑换英雄
	private Map<Integer, StaticExchangeHero> exchangeHeroMap = new HashMap<Integer, StaticExchangeHero>();

	// 特价礼包
	private Map<Integer, List<StaticActPayGift>> payGiftMap = new HashMap<Integer, List<StaticActPayGift>>();
	private Map<Integer, StaticActPayGift> payGifts = new HashMap<Integer, StaticActPayGift>();

	// 月卡&&季卡
	private Map<Integer, List<StaticActPayCard>> payCardMap = new HashMap<Integer, List<StaticActPayCard>>();
	private Map<Integer, StaticActPayCard> payCards = new HashMap<Integer, StaticActPayCard>();

	// 每日特惠直购物品
	private Map<Integer, List<StaticActPayMoney>> payMoneyMap = new HashMap<Integer, List<StaticActPayMoney>>();
	private Map<Integer, StaticActPayMoney> payMoneys = new HashMap<Integer, StaticActPayMoney>();

	// 首充礼包
	// private StaticActFirstPay staticActFirstPay;
	private Map<Integer, StaticActFirstPay> staticActFirstPay;

	// 开服七日活动
	private Map<Integer, StaticActSeven> sevens = new HashMap<Integer, StaticActSeven>();

	// 红装转盘
	private Map<Integer, StaticActRedDial> redDialMap = new HashMap<Integer, StaticActRedDial>();

	// 抢夺名城
	private Map<Integer, StaticActStealCity> stealCityMap = new HashMap<Integer, StaticActStealCity>();

	// 军备促销直购物品
	private Map<Integer, List<StaticActPayArms>> payArmsMap = new HashMap<Integer, List<StaticActPayArms>>();
	private Map<Integer, StaticActPayArms> payArms = new HashMap<Integer, StaticActPayArms>();

	/**
	 * 许愿池配置
	 */
	private Map<Integer, StaticActHope> staticActHopeMap = new HashMap<>();
	/**
	 * 装备精研配置
	 */
	private List<StaticActEquipUpdate> staticActEquipUpdateList = new ArrayList<>();

	// 通行证活动物品
	private Map<Integer, List<StaticPassPortAward>> passPortAwardMap = new HashMap<Integer, List<StaticPassPortAward>>();
	private Map<Integer, StaticPassPortAward> passPortAwards = new HashMap<Integer, StaticPassPortAward>();

	// 通行证活动的任务
	private Map<Integer, List<StaticPassPortTask>> passPortTaskMap = new HashMap<Integer, List<StaticPassPortTask>>();
	private Map<Integer, StaticPassPortTask> passPortTasks = new HashMap<Integer, StaticPassPortTask>();
	private List<StaticPassPortTask> passPortTaskList = new ArrayList<>();

	// 通行证活动购买
	private Map<Integer, List<StaticPayPassPort>> staticPayPassPortkMap = new HashMap<Integer, List<StaticPayPassPort>>();
	private Map<Integer, StaticPayPassPort> staticPayPassPorts = new HashMap<Integer, StaticPayPassPort>();
	private List<StaticPayPassPort> staticPayPassPortList = new ArrayList<>();

	// 通行证活动等级分布
	private Map<Integer, StaticPassPortLv> passPortLvMap = new HashMap<Integer, StaticPassPortLv>();

	// 0元礼包
	private Map<Integer, List<StaticActFreeBuy>> freebuyLists = new HashMap<Integer, List<StaticActFreeBuy>>();

	// 转盘消耗配置
	private Map<Integer, StaticDialCost> dialCostMap = new HashMap<>();

	// 双蛋活动
	private Map<Integer, StaticActExchange> actDoubleEggs = new HashMap<>();

	// 双旦礼包配置
	@Getter
	@Setter
	private Map<Integer, StaticActivityChrismas> chrismasMap = new HashMap<>();
	@Getter
	@Setter
	private Map<Integer, StaticActivityChrismasAward> chrismasAwardMap = new HashMap<>();

	private HashBasedTable<Integer, Integer, StaticMyExchange> staticMyExchangeMap = HashBasedTable.create();
	private Map<Integer, List<StaticMyExchange>> staticMyExchangeList = new HashMap<>();

	@Getter
	@Setter
	private Map<Integer, StaticHeroTask> staticHeroTaskMap = new HashMap<>();

	// @Getter
//    @Setter
	private Map<Integer, Map<Integer, StaticLimitGift>> staticLimitGiftMap = new HashMap<>();
	private Map<Integer, StaticLimitGift> giftMap = new HashMap<>();

	@Getter
	@Setter
	private Map<Integer, StaticDialAwards> staticDialAwardMap = new HashMap<>();
	@Getter
	@Setter
	private Map<Integer, List<StaticDialAwards>> staticDialAwards = new HashMap<>();
	@Getter
	@Setter
	private HashBasedTable<Integer, Integer, StaticDialAwards> dialTable = HashBasedTable.create();
	@Getter
	@Setter
	private Map<Integer, StaticPayCalculate> payCalMap = new HashMap<>();
	@Getter
	@Setter
	private Map<Integer, StaticResourceGift> staticResourceGiftMap = new HashMap<>();
	@Getter
	private Map<Integer, StaticMaterialSubstituteVip> materialSubstituteMap = new HashMap<>();
	@Getter
	private List<StaticMaterialSubstituteCost> materialSubstituteCostList = new ArrayList<>();
	private Map<Integer, List<StaticActSpringFestival>> springFestivalsMap = new HashMap<>();

	private Map<Integer, Map<Integer, StaticLimitGift>> staticSpringGiftMap = new HashMap<>();
	private Map<Integer, StaticLimitGift> springGiftMap = new HashMap<>();

	@Override
	public void init() throws Exception {
		staticActFirstPay = staticDataDao.selectActFirstPay();
		initBaseActivity();
		initActAward();
		initAct();
		initDial();
		initStoneDetail();
		initQuota();
		initActTask();
		initActShop();
		initActFoot();
		initActMonster();
		initActPayGift();
		initActPayCard();
		initActPayMoney();
		initActRankDisplay();
		initActSeven();
		initRedDial();
		initStealCityMap();
		initActCommand();
		initDailyCheckinAwards();
		initsActPayArms();
		initActHope();
		initActEquipUpdate();
		initActPassPortAward();
		initActpassPortLv();
		initFreeBuyList();
		initDialCost();
		initActDoubleEggs();
		initMyExchangeConfig();
		initStaticDialConfig();
		initPayCalMapConfig();
		staticHeroTaskMap = staticDataDao.loadStaticHeroTask();
		initLimitGift();
		iniResourceGift();
		iniMaterialSubstitute();
		iniSpringFestival();
		iniSpringGiftMap();

		// 活动事件监听
		ActivityEventManager.getInst().listen();
	}

	/**
	 * 初始化转盘消耗配置
	 */
	public void initDialCost() {
		if (dialCostMap.size() > 0) {
			dialCostMap.clear();
		}

		dialCostMap.putAll(staticDataDao.selectDialCost());

	}

	/**
	 * 初始化双旦礼包配置
	 */
	public void initActDoubleEggs() {
		actDoubleEggs.clear();
		actDoubleEggs = staticDataDao.selectActExchange();
		chrismasMap = staticDataDao.loadStaticActivityChrismas();
		chrismasAwardMap = staticDataDao.loadStaticActivityChrismasAward();
	}

	// 初始化魅影转盘兑换配置
	private void initMyExchangeConfig() {
		staticMyExchangeList.clear();
		List<StaticMyExchange> staticMyExchanges = staticDataDao.queryAllMyExchange();
		if (staticMyExchanges != null) {
			staticMyExchanges.forEach(x -> {
				staticMyExchangeMap.put(x.getAwardId(), x.getId(), x);
				List<StaticMyExchange> staticMyExchanges1 = staticMyExchangeList.computeIfAbsent(x.getAwardId(), a -> new ArrayList<>());
				staticMyExchanges1.add(x);
			});
		}
	}

	// 夺宝奇兵奖励配置
	private void initStaticDialConfig() {
		this.staticDialAwardMap.clear();
		this.staticDialAwards.clear();
		this.dialTable.clear();
		this.staticDialAwardMap = staticDataDao.queryALLDialAwardConfig();
		if (staticDialAwardMap != null) {
			staticDialAwardMap.values().forEach(x -> {
				List<StaticDialAwards> staticDialAwards = this.staticDialAwards.computeIfAbsent(x.getAwardId(), a -> new ArrayList<>());
				staticDialAwards.add(x);
				dialTable.put(x.getAwardId(), x.getSortId(), x);
			});
		}
	}

	private void initPayCalMapConfig() {
		payCalMap.clear();
		this.payCalMap = staticDataDao.queryPayCal();

	}

	private void initLimitGift() {
		giftMap.clear();
		staticLimitGiftMap.clear();
		this.giftMap = staticDataDao.loadStaticLimitGift();
		giftMap.values().forEach(x -> {
			Map<Integer, StaticLimitGift> integerStaticLimitGiftMap = staticLimitGiftMap.computeIfAbsent(x.getAwardId(), a -> new HashMap<>());
			integerStaticLimitGiftMap.put(x.getKeyId(), x);
		});
	}

	public StaticLimitGift getLimitGiftByKeyId(int keyId) {
		return giftMap.get(keyId);
	}

	public Map<Integer, StaticLimitGift> getLimitGiftByAward(int awardId) {
		return staticLimitGiftMap.get(awardId);
	}

	public Map<Integer, StaticDialCost> getDialCostMap() {
		return dialCostMap;
	}

	public void setDialCostMap(Map<Integer, StaticDialCost> dialCostMap) {
		this.dialCostMap = dialCostMap;
	}

	/**
	 * 初始化通行证活动等级配置
	 *
	 * @throws Exception
	 */
	private void initActpassPortLv() throws Exception {
		passPortLvMap.clear();

		passPortLvMap = staticDataDao.loadStaticPassPortLv();
		if (passPortLvMap == null || passPortLvMap.size() == 0) {
			throw new Exception("通行证等级配置异常s_passport_lv");
		}
	}

	/**
	 * 初始化通行证证活动
	 */
	private void initActPassPortAward() throws Exception {
		passPortAwards.clear();

		passPortTasks.clear();

		passPortAwardMap.clear();

		staticPayPassPorts.clear();

		passPortTaskMap.clear();

		staticPayPassPortkMap.clear();

		staticPayPassPorts.clear();

		List<StaticPassPortAward> passPortAwardList = staticDataDao.selectPassPortAward();
		if (passPortAwardList.size() == 0) {
			throw new Exception("通行证奖励配置异常s_passport_award");
		}

		for (StaticPassPortAward passPort : passPortAwardList) {
			int awardId = passPort.getAwardId();

			List<StaticPassPortAward> passPortList = passPortAwardMap.get(awardId);
			if (passPortList == null) {
				passPortList = new ArrayList<StaticPassPortAward>();
				passPortAwardMap.put(awardId, passPortList);
			}
			passPortList.add(passPort);
			passPortAwards.put(passPort.getId(), passPort);
		}

		List<StaticPassPortTask> passPortTask = staticDataDao.selectPassPortTask();
		if (passPortTask.size() == 0) {
			throw new Exception("通行证任务配置异常s_passport_award");
		}
		for (StaticPassPortTask task : passPortTask) {
			passPortTasks.put(task.getId(), task);

			List<StaticPassPortTask> passPortList = passPortTaskMap.get(task.getType());
			if (passPortList == null) {
				passPortList = new ArrayList<StaticPassPortTask>();
				passPortTaskMap.put(task.getType(), passPortList);
			}
			passPortList.add(task);
			passPortTasks.put(task.getId(), task);
		}
		passPortTaskList = passPortTask;

		staticPayPassPortList = staticDataDao.selectStaticPayPassPorts();
		if (staticPayPassPortList.size() == 0) {
			throw new Exception("通行证计费点配置异常s_act_pay_passport");
		}
		for (StaticPayPassPort staticPayPassPort : staticPayPassPortList) {
			List<StaticPayPassPort> passPortPayList = staticPayPassPortkMap.get(staticPayPassPort.getAwardId());
			if (passPortPayList == null) {
				passPortPayList = new ArrayList<StaticPayPassPort>();
				staticPayPassPortkMap.put(staticPayPassPort.getAwardId(), passPortPayList);
			}
			passPortPayList.add(staticPayPassPort);
			staticPayPassPorts.put(staticPayPassPort.getKeyId(), staticPayPassPort);
		}

	}

	/**
	 * 初始化许愿池
	 */
	private void initActHope() {
		if (staticActHopeMap.size() > 0) {
			staticActHopeMap.clear();
		}
		staticActHopeMap.putAll(staticDataDao.loadStaticActHope());
	}

	/**
	 * 初始化装备精研
	 */
	private void initActEquipUpdate() {
		if (staticActEquipUpdateList.size() > 0) {
			staticActEquipUpdateList.clear();
		}
		staticActEquipUpdateList.addAll(staticDataDao.loadStaticActEquipUpdate());
	}

	/**
	 * 初始化活动
	 */
	private void initBaseActivity() {
		// Date openTime = DateHelper.parseDate(serverSetting.getOpenTime());
		Date openTime = serverManager.getServer().getOpenTime();
		// int actMoldId = serverSetting.getActMoldId();
		int actMoldId = serverManager.getServer().getActMold();

		activityMap = staticDataDao.selectActivity();

		Date now = new Date();

		if (sendMailList.size() > 0) {
			sendMailList.clear();
		}

		if (activityList.size() > 0) {
			activityList.clear();
		}

		List<StaticActivityPlan> planList = staticDataDao.selectActPlan();
		for (StaticActivityPlan plan : planList) {
			if (plan.getMoldId() != 0 && plan.getMoldId() != actMoldId) {
				continue;
			}

			StaticActivity staticActivity = activityMap.get(plan.getActivityId());
			if (staticActivity == null) {
				LogHelper.CONFIG_LOGGER.error("staticActivity is not exist : StaticActivityPlan {}", plan.toString());
				continue;
			}

			ActivityBase activity = new ActivityBase(openTime, staticActivity, plan);

			// 初始化活动
			if (!activity.initData()) {
				continue;
			}

			// 已过期活动,不加载到内存中
			if (activity.disappear(now)) {
				continue;
			}
			// 有邮件的则发送邮件
			if (activity.initSendMail()) {
				sendMailList.add(activity);
			}
			logger.info("[{}][{}][{}][{}][{}]", staticActivity.getActivityId(), activity.getAwardId(), staticActivity.getName(), DateHelper.formatDateTime(activity.getBeginTime(), DateHelper.format1), DateHelper.formatDateTime(activity.getEndTime(), DateHelper.format1));
			if (activity.getDisplayTime() != null) {
				logger.info("[{}]", DateHelper.formatDateTime(activity.getDisplayTime(), DateHelper.format1));
			}
			if (activity.getSendTime() != null) {
				logger.info("[{}]", DateHelper.formatDateTime(activity.getSendTime(), DateHelper.format1));
			}
			activityList.add(activity);
		}
	}

	/**
	 * 初始化基地升级
	 */
	private void initActCommand() {
		if (actCommands.size() > 0) {
			actCommands.clear();
		}
		List<StaticActCommand> commandList = staticDataDao.selectActCommand();
		for (StaticActCommand actAward : commandList) {
			actCommands.put(actAward.getKeyId(), actAward);
			List<CommonPb.Award> awardPbList = new ArrayList<CommonPb.Award>();
			for (List<Integer> e : actAward.getAwardList()) {
				int type = e.get(0);
				int id = e.get(1);
				int count = e.get(2);
				awardPbList.add(PbHelper.createAward(type, id, count).build());
			}
			actAward.setAwardPbList(awardPbList);
		}
	}

	/**
	 * 初始化30日签到
	 */
	private void initDailyCheckinAwards() {
		if (dailyCheckinAwards.size() > 0) {
			dailyCheckinAwards.clear();
		}

		dailyCheckinAwards.putAll(staticDataDao.selectDailyCheckinAwards());
	}

	/**
	 * 初始化活动奖励
	 */
	private void initActAward() {
		if (actAwardMap.size() > 0) {
			actAwardMap.clear();
		}

		if (actAwards.size() > 0) {
			actAwards.clear();
		}

		if (actSortAwards.size() > 0) {
			actSortAwards.clear();
		}
		List<StaticActAward> awardList = staticDataDao.selectActAward();
		for (StaticActAward actAward : awardList) {
			actAwardMap.put(actAward.getKeyId(), actAward);

			List<CommonPb.Award> awardPbList = new ArrayList<CommonPb.Award>();
			for (List<Integer> e : actAward.getAwardList()) {
				if (e.size() >= 3) {
					int type = e.get(0);
					int id = e.get(1);
					int count = e.get(2);
					awardPbList.add(PbHelper.createAward(type, id, count).build());
				}
			}
			actAward.setAwardPbList(awardPbList);

			int awardId = actAward.getAwardId();
			int sortId = actAward.getSortId();
			List<StaticActAward> tmp = actAwards.get(awardId);
			if (tmp == null) {
				tmp = new ArrayList<StaticActAward>();
				actAwards.put(awardId, tmp);
			}
			tmp.add(actAward);

			//
			Map<Integer, List<StaticActAward>> sortAward = actSortAwards.get(awardId);
			if (sortAward == null) {
				sortAward = new HashMap<Integer, List<StaticActAward>>();
				actSortAwards.put(awardId, sortAward);
			}

			List<StaticActAward> sorts = sortAward.get(sortId);
			if (sorts == null) {
				sorts = new ArrayList<StaticActAward>();
				sortAward.put(sortId, sorts);
			}
			sorts.add(actAward);
		}
	}

	private void initAct() {
		doubleMaps = staticDataDao.selectActDouble();
		dropMaps = staticDataDao.selectActDrop();
	}

	/**
	 * 初始化转盘数据
	 */
	private void initDial() {
		if (dials.size() > 0) {
			dials.clear();
		}

		List<StaticActDial> list = staticDataDao.selectActDial();
		for (StaticActDial next : list) {
			int awardId = next.getAwardId();
			DialEntity dailEntiy = dials.get(awardId);
			if (dailEntiy == null) {
				dailEntiy = new DialEntity(awardId);
				dials.put(awardId, dailEntiy);
			}
			dailEntiy.addStaticActDial(next);
		}
	}

	private void initStoneDetail() {
		if (stoneDial.size() > 0) {
			stoneDial.clear();
		}

		if (luckDial.size() > 0) {
			luckDial.clear();
		}

		if (purpDial.size() > 0) {
			purpDial.clear();
		}

		// 宝石罗盘
		List<StaticActDialStone> stlist = staticDataDao.selectActDialStone();
		for (StaticActDialStone next : stlist) {
			stoneDial.put(next.getCount(), next);
		}

		// 幸运罗盘
		List<StaticActDialLuck> lkList = staticDataDao.selectActDialLuck();
		for (StaticActDialLuck next : lkList) {
			int awardId = next.getAwardId();
			int type = next.getType();
			int vip = next.getVip();
			Map<Integer, Map<Integer, StaticActDialLuck>> actMap = luckDial.get(awardId);
			if (actMap == null) {
				actMap = new HashMap<Integer, Map<Integer, StaticActDialLuck>>();
				luckDial.put(awardId, actMap);
			}
			Map<Integer, StaticActDialLuck> vipMap = actMap.get(type);
			if (vipMap == null) {
				vipMap = new HashMap<Integer, StaticActDialLuck>();
				actMap.put(type, vipMap);
			}
			vipMap.put(vip, next);
		}
		// 紫装转盘
		List<StaticActDialPurp> ppList = staticDataDao.selectActDialPurp();
		for (StaticActDialPurp next : ppList) {
			int awardId = next.getAwardId();
			StaticActDialPurp dialPurp = purpDial.get(awardId);
			if (dialPurp == null) {
				purpDial.put(awardId, next);
			}
		}
	}

	/**
	 * 初始化礼包出售
	 */
	private void initQuota() {
		if (quotas.size() > 0) {
			quotas.clear();
		}

		if (quotaMaps.size() > 0) {
			quotaMaps.clear();
		}

		List<StaticActQuota> list = staticDataDao.selectActQuota();
		for (StaticActQuota actQuota : list) {
			quotas.put(actQuota.getQuotaId(), actQuota);

			List<StaticActQuota> quotaList = quotaMaps.get(actQuota.getAwardId());
			if (quotaList == null) {
				quotaList = new ArrayList<StaticActQuota>();
				quotaMaps.put(actQuota.getAwardId(), quotaList);
			}
			quotaList.add(actQuota);
		}
	}

	/**
	 * 初始化活动任务
	 */
	private void initActTask() {
		if (tasks.size() > 0) {
			tasks.clear();
		}

		if (taskMap.size() > 0) {
			taskMap.clear();
		}

		List<StaticActTask> list = staticDataDao.selectActTask();
		for (StaticActTask staticActTask : list) {
			int awardId = staticActTask.getAwardId();

			List<StaticActTask> tlist = tasks.get(awardId);
			if (tlist == null) {
				tlist = new ArrayList<StaticActTask>();
				tasks.put(awardId, tlist);
			}
			tlist.add(staticActTask);

			Map<Integer, StaticActTask> tmap = taskMap.get(awardId);
			if (tmap == null) {
				tmap = new HashMap<Integer, StaticActTask>();
				taskMap.put(awardId, tmap);
			}
			tmap.put(staticActTask.getType(), staticActTask);
		}
	}

	private void initActShop() {
		if (shopMap.size() > 0) {
			shopMap.clear();
		}

		List<StaticActShop> list = staticDataDao.selectActShop();
		for (StaticActShop staticActShop : list) {
			int awardId = staticActShop.getAwardId();

			Map<Integer, StaticActShop> shops = shopMap.get(awardId);
			if (shops == null) {
				shops = new HashMap<Integer, StaticActShop>();
				shopMap.put(awardId, shops);
			}

			List<List<Integer>> sellList = staticActShop.getSellList();
			if (sellList == null || sellList.isEmpty()) {
				continue;
			}

			// 设置每格总随机值
			for (List<Integer> sell : sellList) {
				int type = sell.get(0);
				int id = sell.get(1);
				int count = sell.get(2);
				int price = sell.get(3);
				int probability = sell.get(4);

				probability += staticActShop.getProbability();

				// 出售道具
				ActShopItem shopItem = new ActShopItem(type, id, count, price, probability);

				// 添加道具
				staticActShop.setProbability(probability);
				staticActShop.getActShopItems().add(shopItem);
			}

			shops.put(staticActShop.getGrid(), staticActShop);
		}
	}

	private void initActFoot() {
		if (footLists.size() > 0) {
			footLists.clear();
		}

		List<StaticActFoot> list = staticDataDao.selectActFoot();
		for (StaticActFoot actFoot : list) {
			int awardId = actFoot.getAwardId();
			List<StaticActFoot> fl = this.footLists.get(awardId);
			if (fl == null) {
				fl = new ArrayList<StaticActFoot>();
				footLists.put(awardId, fl);
			}
			fl.add(actFoot);
		}

	}

	public void initActMonster() {
		exchangeItemMap = staticDataDao.selectExchangeItem();
		exchangeHeroMap = staticDataDao.selectExchangeHero();
	}

	public void initActPayGift() {
		if (payGifts.size() > 0) {
			payGifts.clear();
		}
		if (payGiftMap.size() > 0) {
			payGiftMap.clear();
		}

		List<StaticActPayGift> payGiftList = staticDataDao.selectActPayGift();
		for (StaticActPayGift payGift : payGiftList) {
			int awardId = payGift.getAwardId();

			List<StaticActPayGift> gfitList = payGiftMap.get(awardId);
			if (gfitList == null) {
				gfitList = new ArrayList<StaticActPayGift>();
				payGiftMap.put(awardId, gfitList);
			}
			gfitList.add(payGift);
			payGifts.put(payGift.getPayGiftId(), payGift);
		}
	}

	/**
	 * 初始化月卡&&季卡
	 */
	public void initActPayCard() {
		if (payCards.size() > 0) {
			payCards.clear();
		}
		if (payCardMap.size() > 0) {
			payCardMap.clear();
		}

		List<StaticActPayCard> payCardList = staticDataDao.selectActPayCard();
		for (StaticActPayCard payCard : payCardList) {
			int awardId = payCard.getAwardId();

			List<StaticActPayCard> cardList = payCardMap.get(awardId);
			if (cardList == null) {
				cardList = new ArrayList<StaticActPayCard>();
				payCardMap.put(awardId, cardList);
			}
			cardList.add(payCard);
			payCards.put(payCard.getPayCardId(), payCard);
		}
	}

	/**
	 * 初始化每日特惠直购物品
	 */
	public void initActPayMoney() {
		if (payMoneys.size() > 0) {
			payMoneys.clear();
		}
		if (payMoneyMap.size() > 0) {
			payMoneyMap.clear();
		}

		List<StaticActPayMoney> payMoneyList = staticDataDao.selectActPayMoney();
		for (StaticActPayMoney payMoney : payMoneyList) {
			int awardId = payMoney.getAwardId();

			List<StaticActPayMoney> moneyList = payMoneyMap.get(awardId);
			if (moneyList == null) {
				moneyList = new ArrayList<StaticActPayMoney>();
				payMoneyMap.put(awardId, moneyList);
			}
			moneyList.add(payMoney);
			payMoneys.put(payMoney.getPayMoneyId(), payMoney);
		}
	}

	/**
	 * 初始化军备促销购物品
	 */
	public void initsActPayArms() {
		if (payArms.size() > 0) {
			payArms.clear();
		}
		if (payArmsMap.size() > 0) {
			payArmsMap.clear();
		}

		List<StaticActPayArms> payArmsList = staticDataDao.selectActPayArms();
		for (StaticActPayArms payArm : payArmsList) {
			int awardId = payArm.getAwardId();

			List<StaticActPayArms> armsList = payArmsMap.get(awardId);
			if (armsList == null) {
				armsList = new ArrayList<StaticActPayArms>();
				payArmsMap.put(awardId, armsList);
			}
			armsList.add(payArm);
			payArms.put(payArm.getPayArmsId(), payArm);
		}
	}

	public void initActRankDisplay() {
		if (rankDisplays.size() > 0) {
			rankDisplays.clear();
		}
		allRankDisplay.clear();
		List<StaticActRankDisplay> rankdisplayList = staticDataDao.selectActRankDisplay();
		for (StaticActRankDisplay rankDisplay : rankdisplayList) {
			int awardId = rankDisplay.getAwardId();
			List<StaticActRankDisplay> displayList = rankDisplays.get(awardId);
			if (displayList == null) {
				displayList = new ArrayList<StaticActRankDisplay>();
				rankDisplays.put(awardId, displayList);
			}
			displayList.add(rankDisplay);

			Map<Integer, List<StaticActRankDisplay>> integerListMap = allRankDisplay.computeIfAbsent(rankDisplay.getAwardId(), x -> new LinkedHashMap<>());
			List<StaticActRankDisplay> staticActRankDisplays = integerListMap.computeIfAbsent(rankDisplay.getAwardKeyId(), x -> new ArrayList<>());
			staticActRankDisplays.add(rankDisplay);
		}
	}

	public void initActSeven() {
		if (sevens.size() > 0) {
			sevens.clear();
		}

		List<StaticActSeven> sevenList = staticDataDao.selectActSeven();
		for (StaticActSeven seven : sevenList) {
			sevens.put(seven.getKeyId(), seven);
		}
	}

	public void initRedDial() throws ConfigException {
		redDialMap = staticDataDao.selectActRedDial();
		for (StaticActRedDial actRedDial : redDialMap.values()) {
			if (actRedDial == null) {
				continue;
			}

			if (actRedDial.getPrice().size() != 2) {
				throw new ConfigException("redDial price error");
			}
		}
	}

	public void initStealCityMap() throws ConfigException {
		setStealCityMap(staticDataDao.selectActStealCity());
		for (StaticActStealCity staticActStealCity : getStealCityMap().values()) {
			List<List<Integer>> time1 = staticActStealCity.getTime1();
			List<List<Integer>> time2 = staticActStealCity.getTime2();
			List<List<Integer>> time3 = staticActStealCity.getTime3();
			List<Integer> city1 = staticActStealCity.getCity1();
			List<Integer> city2 = staticActStealCity.getCity2();
			List<Integer> city3 = staticActStealCity.getCity3();
			List<List<Integer>> awards = staticActStealCity.getAwards();
			if (time1 == null || time1.size() != 2) {
				makeException();
			}

			if (time2 == null || time2.size() != 2) {
				makeException();
			}

			if (time3 == null || time3.size() != 2) {
				makeException();
			}

			if (city1 == null || city1.isEmpty()) {
				makeException();
			}

			if (city2 == null || city2.isEmpty()) {
				makeException();
			}

			if (city3 == null || city3.isEmpty()) {
				makeException();
			}

			if (awards == null || awards.isEmpty()) {
				makeException();
			}

		}
	}

	public void makeException() throws ConfigException {
		throw new ConfigException("config error..");
	}

	/**
	 * 刷新商店数据
	 */
	public void refreshShop(ActRecord actRecord, int actAwardId, int state) {
		actRecord.getShops().clear();
		if (!shopMap.containsKey(actAwardId)) {
			return;
		}
		Iterator<StaticActShop> it = shopMap.get(actAwardId).values().iterator();
		while (it.hasNext()) {
			StaticActShop next = it.next();
			int grid = next.getGrid();

			ActShopItem shopItem = next.randomItem();
			actRecord.getShops().put(grid, new ActShopProp(grid, shopItem));

			actRecord.getShops().put(grid, new ActShopProp(grid, shopItem));
			if (actRecord.getShops().get(grid).getPropId() == Integer.valueOf(next.getParam())) {
				if (state >= 7) {
					actRecord.getShops().get(grid).setIsBuy(2);
				}
			}
		}
	}

	public void initShop(ActRecord actRecord, int actAwardId, int state) {
		if (!actRecord.getShops().isEmpty()) {
			return;
		}
		actRecord.getShops().clear();
		if (!shopMap.containsKey(actAwardId)) {
			return;
		}
		Iterator<StaticActShop> it = shopMap.get(actAwardId).values().iterator();
		while (it.hasNext()) {
			StaticActShop next = it.next();
			int grid = next.getGrid();

			ActShopItem shopItem = next.randomItem();

			actRecord.getShops().put(grid, new ActShopProp(grid, shopItem));
			if (actRecord.getShops().get(grid).getPropId() == Integer.valueOf(next.getParam())) {
				if (state >= 7) {
					actRecord.getShops().get(grid).setIsBuy(2);
				}
			}
		}
	}

	public List<ActivityBase> getActivityList() {
		return activityList;
	}

	/**
	 * 获取发放奖励活动
	 *
	 * @return
	 */
	public List<ActivityBase> getSendAwardList() {
		Date now = new Date();
		List<ActivityBase> list = new ArrayList<ActivityBase>();
		for (ActivityBase base : sendMailList) {
			if (!base.isSendMail()) {
				continue;
			}
			if (DateHelper.isSameDate(base.getSendTime(), now) && now.after(base.getSendTime())) {
				list.add(base);
			}
		}
		return list;
	}

	public List<ActivityBase> getSendMailList() {
		return sendMailList;
	}

	public ActivityBase getActivityById(int activityId) {
		for (ActivityBase base : activityList) {
			if (base.getActivityId() != activityId) {
				continue;
			}
			if (base.getStep() == ActivityConst.ACTIVITY_CLOSE) {
				continue;
			}
			// 修复特价礼包未开启由于预热导致的Bug
			if (base.getStep() == ActivityConst.ACTIVITY_TO_BEGIN && base.getActivityId() == ActivityConst.ACT_PAY_GIFT) {
				continue;
			}
			return base;
		}
		return null;
	}

	public List<StaticActAward> getActAwardById(int awardId) {
		return actAwards.get(awardId);
	}

	public Map<Integer, StaticDailyCheckin> getDailyCheckinAwards() {
		return dailyCheckinAwards;
	}

	public StaticActCommand getActCommand(int keyId) {
		return actCommands.get(keyId);
	}

	public Map<Integer, StaticActCommand> getActCommands() {
		return actCommands;
	}

	public StaticActAward getActRankAward(int awardId, int rank) {
		List<StaticActAward> rankAwardList = getActAwardById(awardId);
		if (rankAwardList == null) {
			return null;
		}
		StaticActAward actAward = null;
		for (StaticActAward e : rankAwardList) {
			int cond = e.getCond();
			if (actAward == null && cond >= rank) {
				actAward = e;
			} else if (actAward != null && cond >= rank && cond < actAward.getCond()) {
				actAward = e;
			}
		}
		return actAward;
	}

	public List<StaticActAward> getActAwardById(int awardId, int sortId) {
		if (actSortAwards.containsKey(awardId)) {
			Map<Integer, List<StaticActAward>> p = actSortAwards.get(awardId);
			return p.get(sortId);
		}
		return new ArrayList<StaticActAward>();
	}

	public StaticActAward getActAward(int keyId) {
		return actAwardMap.get(keyId);
	}

	public List<StaticActQuota> getQuotaList(int activityId) {
		return quotaMaps.get(activityId);
	}

	public StaticActQuota getQuotaById(int quotaId) {
		return quotas.get(quotaId);
	}

	public StaticActDialStone getActDial(int count) {
		if (stoneDial.containsKey(count)) {
			return stoneDial.get(count);
		} else {
			return stoneDial.get(stoneDial.size());
		}
	}

	public StaticActDialStone getLastCountStone() {
		return stoneDial.get(stoneDial.size());
	}

	/**
	 * @param stoneCost
	 * @return1.获得次数2.剩余多少升级
	 */
	public long[] getCostCount(long stoneCost) {
		long total = 0L;
		long rets[] = { 0, 0 };
		int count = stoneDial.size();// 总次数
		for (int i = 1; i <= count; i++) {
			StaticActDialStone staticStone = stoneDial.get(i);
			if (stoneCost >= total + staticStone.getStone()) {
				rets[0] = i;
				total += staticStone.getStone();
			} else {
				long over = stoneCost - total;
				rets[1] = staticStone.getStone() - over;
				break;
			}
		}
		if (rets[0] == count) {
			StaticActDialStone last = stoneDial.get(count);
			long over = stoneCost - total;
			long overCount = over / last.getStone();
			rets[0] += overCount;
			rets[1] = last.getStone() - over % last.getStone();
		}
		return rets;
	}

	public StaticActDialStone getStoneCount(long stoneCost) {
		StaticActDialStone dialStone = null;
		Iterator<StaticActDialStone> it = stoneDial.values().iterator();
		while (it.hasNext()) {
			StaticActDialStone next = it.next();
			if (next.getStone() <= stoneCost && dialStone == null) {
				dialStone = next;
			} else if (next.getStone() <= stoneCost && dialStone.getStone() < stoneCost) {
				dialStone = next;
			}
		}
		return dialStone;
	}

	public List<StaticActDial> getActDialList(int awardId, int type) {
		return dials.get(awardId).getActDialList(type);
	}

	public DialEntity getActDialMap(int activityId) {
		return dials.get(activityId);
	}

	public List<StaticActTask> getActTasks(int awardId) {
		return tasks.get(awardId);
	}

	public StaticActTask getActTask(int awardId, int type) {
		if (taskMap.containsKey(awardId)) {
			return taskMap.get(awardId).get(type);
		}
		return null;
	}

	public StaticActShop getActShop(int awardId, int grid) {
		if (shopMap.containsKey(awardId)) {
			return shopMap.get(awardId).get(grid);
		}
		return null;
	}

	public Map<Integer, StaticActShop> getActShopByAward(int awardId) {
		return shopMap.get(awardId);
	}

	public List<StaticActFoot> getActFoots(int awardId) {
		return footLists.get(awardId);
	}

	public StaticActFoot getActFoot(int awardId, int footId) {
		List<StaticActFoot> list = footLists.get(awardId);
		for (StaticActFoot e : list) {
			if (e.getFootId() == footId) {
				return e;
			}
		}
		return null;
	}

	public StaticActDrop getActDrop(int awardId) {
		return dropMaps.get(awardId);
	}

	public StaticActDialLuck getDialLuck(int activityId, int type, int vip) {
		if (luckDial.containsKey(activityId)) {
			return luckDial.get(activityId).get(type).get(vip);
		}
		return null;
	}

	public StaticActDialPurp getDialPurp(int activityId) {
		if (purpDial.containsKey(activityId)) {
			return purpDial.get(activityId);
		}
		return null;
	}

	public StaticActDouble getActDouble(int awardId) {
		return doubleMaps.get(awardId);
	}

	public StaticExchangeHero getExchangeHero(int heroId) {
		return exchangeHeroMap.get(heroId);
	}

	public StaticExchangeItem getExchangeItem(int itemId) {
		return exchangeItemMap.get(itemId);
	}

	public List<StaticActPayGift> getPayGiftList(int awardId) {
		return payGiftMap.get(awardId);
	}

	public StaticActPayGift getPayGift(int giftId) {
		return payGifts.get(giftId);
	}

	public List<StaticActPayCard> getPayCardList(int awardId) {
		return payCardMap.get(awardId);
	}

	public StaticActPayCard getPayCard(int cardId) {
		return payCards.get(cardId);
	}

	public Map<Integer, StaticActPayCard> getPayCard() {
		return payCards;
	}

	public List<StaticActPayMoney> getPayMoneyList(int awardId) {
		return payMoneyMap.get(awardId);
	}

	public StaticActPayMoney getPayMoney(int payId) {
		return payMoneys.get(payId);
	}

	public List<StaticActPayArms> getPayArmsList(int awardId) {
		return payArmsMap.get(awardId);
	}

	public StaticActPayArms getPayArms(int payId) {
		return payArms.get(payId);
	}

	public List<StaticPassPortAward> getPassPortList(int awardId) {
		return passPortAwardMap.get(awardId);
	}

	public StaticPassPortAward getPassPortAward(int id) {
		return passPortAwards.get(id);
	}

	public StaticPassPortTask getPassPortTask(int taskId) {
		if(passPortTasks.containsKey(taskId)) {
		return passPortTasks.get(taskId);
		}
		return null;
	}

	public List<StaticPassPortTask> getPassPortTaskList() {
		return this.passPortTaskList;
	}

	public StaticActFirstPay getStaticActFirstPay(int moldId) {
		return staticActFirstPay.get(moldId);
	}

	public List<StaticActRankDisplay> getActRankDisplay(int awardId) {
		return rankDisplays.get(awardId);
	}

	public Map<Integer, List<StaticActRankDisplay>> getActRankDisplays(int awardId) {
		return allRankDisplay.get(awardId);
	}

	public Map<Integer, StaticActSeven> getSevens() {
		return sevens;
	}

	public Map<Integer, StaticActHope> getStaticActHopeMap() {
		return staticActHopeMap;
	}

	public void setStaticActHopeMap(Map<Integer, StaticActHope> staticActHopeMap) {
		this.staticActHopeMap = staticActHopeMap;
	}

	public StaticActRedDial getRedDial(int awardId) {
		for (StaticActRedDial staticActRedDial : redDialMap.values()) {
			if (staticActRedDial == null) {
				continue;
			}

			if (staticActRedDial.getAwardId() == awardId) {
				return staticActRedDial;
			}
		}

		return null;
	}

	public Map<Integer, StaticActStealCity> getStealCityMap() {
		return stealCityMap;
	}

	public void setStealCityMap(Map<Integer, StaticActStealCity> stealCityMap) {
		this.stealCityMap = stealCityMap;
	}

	public List<StaticActEquipUpdate> getStaticActEquipUpdateList() {
		return staticActEquipUpdateList;
	}

	public void setStaticActEquipUpdateList(List<StaticActEquipUpdate> staticActEquipUpdateList) {
		this.staticActEquipUpdateList = staticActEquipUpdateList;
	}

	public List<StaticPassPortTask> getStaticPassPortTaskByType(int type) {
		return passPortTaskMap.get(type);
	}

	/**
	 * 获取通行证活动配置
	 *
	 * @return
	 */
	public Map<Integer, StaticPassPortLv> getPassPortLvMap() {
		return this.passPortLvMap;
	}

	public List<StaticPayPassPort> getStaticPayPassPortList(int awardId) {
		return this.staticPayPassPortkMap.get(awardId);
	}

	public StaticPayPassPort getStaticPayPassPort(int keyId) {
		return staticPayPassPorts.get(keyId);
	}

	public int getMaxPassPortScore() {
		StaticPassPortLv staticPassPortLv = passPortLvMap.get(passPortLvMap.size() - 1);
		if (staticPassPortLv != null) {
			return staticPassPortLv.getScore();
		}
		return 0;
	}

	/**
	 * 获取当前分数对应的等级
	 *
	 * @param score
	 * @return
	 */
	public Integer getPassPortLv(int score) {
		if (!passPortLvMap.isEmpty()) {
			if (score >= passPortLvMap.get(passPortLvMap.size() - 1).getScore()) {
				return passPortLvMap.get(passPortLvMap.size() - 1).getLv();
			}

			Set<Map.Entry<Integer, StaticPassPortLv>> entries = passPortLvMap.entrySet();
			for (Map.Entry<Integer, StaticPassPortLv> entry : entries) {
				StaticPassPortLv value = entry.getValue();
				if (null != value) {
					int beforeScore = value.getScore();
					int lv = value.getLv();
					StaticPassPortLv staticPassPortLv = passPortLvMap.get(lv + 1);
					if (null != staticPassPortLv && score >= beforeScore && score < staticPassPortLv.getScore()) {
						return lv;
					}
				}
			}
		}
		return 0;
	}

	/**
	 * @Description 获取当前经验对应得分数
	 * @Date 2021/1/22 10:17
	 * @Param [lv]
	 * @Return
	 **/
	public Integer getPassPortExp(int lv) {
		if (passPortLvMap.size() > 0) {
			if (lv >= passPortLvMap.get(passPortLvMap.size() - 1).getLv()) {
				return passPortLvMap.get(passPortLvMap.size() - 1).getScore();
			}

			Set<Entry<Integer, StaticPassPortLv>> entries = passPortLvMap.entrySet();

			for (Entry<Integer, StaticPassPortLv> entity : entries) {
				StaticPassPortLv sPassPortLv = entity.getValue();
				if (lv == sPassPortLv.getLv()) {
					return sPassPortLv.getScore();
				}
			}
		}
		return 0;
	}

	public List<StaticActFreeBuy> getActFreeBuy(int awardId) {
		return freebuyLists.get(awardId);
	}

	public StaticActFreeBuy getActFreeBuy(int awardId, int footId) {
		List<StaticActFreeBuy> list = freebuyLists.get(awardId);
		for (StaticActFreeBuy e : list) {
			if (e.getFootId() == footId) {
				return e;
			}
		}
		return null;
	}

	private void initFreeBuyList() {
		if (freebuyLists.size() > 0) {
			freebuyLists.clear();
		}

		List<StaticActFreeBuy> list = staticDataDao.selectActFreeBuy();
		for (StaticActFreeBuy actFoot : list) {
			int awardId = actFoot.getAwardId();
			List<StaticActFreeBuy> fl = this.freebuyLists.get(awardId);
			if (fl == null) {
				fl = new ArrayList<StaticActFreeBuy>();
				freebuyLists.put(awardId, fl);
			}
			fl.add(actFoot);
		}
	}

	/**
	 * 获取通用转盘保底的物品(仅仅处理只有一种类型的转盘,不包含幸运转盘和至尊转盘)
	 *
	 * @param awardId
	 * @return
	 */
	public List<StaticActDial> getActDialMinGuaranteeList(int activityId, int awardId) {
		List<StaticActDial> tarStaticActDial = new ArrayList<>();
		List<StaticActDial> actDialList = dials.get(awardId).getActDialList(1);
		for (StaticActDial staticActDial : actDialList) {
			if (null != staticActDial) {
				int tarAwardId = staticActDial.getAwardId();
				int minGuarantee = staticActDial.getMinGuarantee();
				if (tarAwardId == awardId && minGuarantee == 1) {
					tarStaticActDial.add(staticActDial);
				}
			}
		}
		return tarStaticActDial;
	}

	public Map<Integer, StaticActExchange> getActDoubleEggs() {
		return actDoubleEggs;
	}

	public StaticMyExchange getStaticMyExchange(int awardId, int id) {
		return staticMyExchangeMap.get(awardId, id);
	}

	public List<StaticMyExchange> getStaticMyExchangeList(int awardId) {
		return staticMyExchangeList.get(awardId);
	}

//    this.staticDialAwardMap.clear();
//        this.staticDialAwards.clear();
//        this.dialTable.clear();
//

	// 夺宝奇兵 相关
	public StaticDialAwards getStaticDialAwardsByKey(int keyId) {
		return staticDialAwardMap.get(keyId);
	}

	public List<StaticDialAwards> getStaticDialAwardsList(int awardId) {
		return staticDialAwards.get(awardId);
	}

	public StaticDialAwards getStaticDialAwards(int awardId, int sort) {
		return dialTable.get(awardId, sort);
	}

	public StaticDialAwards getRandomDail(int awardId, Map<Integer, Integer> records) {
		List<StaticDialAwards> list = staticDialAwards.get(awardId);
		if (list == null) {
			return null;
		}
		int seed = 0;
		if (records != null && !records.isEmpty()) {
			List<StaticDialAwards> tempList = new ArrayList<StaticDialAwards>();
			for (StaticDialAwards e : list) {
				if (e.getKeyId() != 0 && records.containsValue(e.getKeyId())) {
					continue;
				}
				seed += e.getWeight();
				tempList.add(e);
			}
			return random(tempList, seed);
		}
		return random(list, list.stream().mapToInt(x -> x.getWeight()).sum());
	}

	private StaticDialAwards random(List<StaticDialAwards> list, int seed) {
		if (list == null || list.isEmpty() || seed == 0) {
			return null;
		}
		int total = 0;
		int random = RandomHelper.randomInSize(seed);
		for (StaticDialAwards e : list) {
			total += e.getWeight();
			if (total >= random) {
				return e;
			}
		}
		return list.get(0);
	}

	public StaticPayCalculate getStaticPayCalculate(int type) {
		return payCalMap.get(type);
	}

	private void iniResourceGift() {
		staticResourceGiftMap.clear();
		staticDataDao.loadStaticResourceGift().forEach(e -> {
			if (e != null) {
				staticResourceGiftMap.put(e.getPayid(), e);
			}
		});
	}

	private void iniMaterialSubstitute() {
		materialSubstituteMap.clear();
		materialSubstituteCostList.clear();
		materialSubstituteMap = staticDataDao.loadMaterialSubstituteVip();
		materialSubstituteCostList = staticDataDao.loadMaterialSubstituteCost().stream().sorted(Comparator.comparing(StaticMaterialSubstituteCost::getTimes)).collect(Collectors.toList());
	}

	private void iniSpringFestival() {
		springFestivalsMap.clear();
		staticDataDao.loadSpringFestival().forEach(e -> {
			springFestivalsMap.computeIfAbsent(e.getAwardId(), x -> new ArrayList<>()).add(e);
		});
	}

	public List<StaticActSpringFestival> getSpringFestivals(int awardId) {
		return springFestivalsMap.get(awardId);
	}

	public List<StaticActSpringFestival> getSpringFestivalsByType(int awardId, int type) {
		List<StaticActSpringFestival> value = springFestivalsMap.get(awardId);
		if (value == null) {
			return Lists.newArrayList();
		}
		return value.stream().filter(e -> e.getType() == type).sorted(Comparator.comparing(e -> e.getSortId())).collect(Collectors.toList());
	}

	public Map<Integer, StaticResourceGift> getStaticResourceGiftMap() {
		return staticResourceGiftMap;
	}

	public StaticResourceGift getStaticResourceGift(int keyId) {
		return staticResourceGiftMap.get(keyId);
	}

	public int getMaterialSubstituteCost(int convertCount) {
		int max = 0;
		for (StaticMaterialSubstituteCost value : getMaterialSubstituteCostList()) {
			if (convertCount >= value.getTimes()) {
				max = value.getCost();
			} else {
				break;
			}
		}
		return max;
	}

	private void iniSpringGiftMap() {
		staticSpringGiftMap.clear();
		springGiftMap.clear();
		staticDataDao.loadSpringGift().forEach(e -> {
			Map<Integer, StaticLimitGift> mapByAwardId = staticSpringGiftMap.computeIfAbsent(e.getAwardId(), x -> new HashMap<>());
			mapByAwardId.put(e.getKeyId(), e);
			springGiftMap.put(e.getKeyId(), e);
		});
	}

	public Map<Integer, StaticLimitGift> getSpringGiftMap(int awardId) {
		return staticSpringGiftMap.get(awardId);
	}

	public StaticLimitGift getSpringGift(int keyId) {
		return springGiftMap.get(keyId);
	}
}
