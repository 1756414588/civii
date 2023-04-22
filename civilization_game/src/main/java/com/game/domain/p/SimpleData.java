package com.game.domain.p;

import com.game.domain.Award;
import com.game.manager.WarManager;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.IntLong;
import com.game.pb.CommonPb.PairIntLong;
import com.game.pb.CommonPb.TwoLong;
import com.game.pb.CommonPb.TwoLong.Builder;
import com.game.pb.DataPb;
import com.game.pb.SerializePb;
import com.game.pb.SerializePb.SerSimpleData;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.game.util.Pair;
import com.game.util.PbHelper;
import com.game.worldmap.March;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.Setter;


/**
 * 玩家相关逻辑数据存盘[比较零散打信息存盘]
 */
@Getter
@Setter
public class SimpleData {

	private int lootGoodState;       //  掉落状态机
	private int threeLootHasHero;    //  前3抽是否有武将
	private int lootGoodTotalTimes;  //  已经抽取英雄总次数
	private int buyBuildTeam;        //  是否购买过商业建造队
	private int killActMonsterDay;   //  击杀活动怪的时间
	private int killActMonsterTimes; //  击杀活动怪的次数
	private int pvpScore;            //  血战积分
	private int killRebelDay;        //  击杀叛军的时间
	private int killRebelTimes;      //  击杀叛军的次数
	private int totalKillNum;        //  血战累杀兵数, 所有玩家活动结束之后清零或者或者活动开始前清零
	private int buyWordTimes;        //  买酒套话次数:是否使用 [mark]
	private HashSet<Integer> digState = new HashSet<Integer>();  //  已经使用的次数
	private Pos digPos = new Pos();     //  挖宝坐标
	private boolean isPaperDiged;       //  是否挖到图纸
	private boolean isGreeted;          //  是否已经恭贺了
	private HashMap<Integer, Buff> buffMap = new HashMap<Integer, Buff>();  // 玩家道具加成
	// 参谋部任务
	private HashMap<Integer, StaffTask> staffTaskMap = new HashMap<Integer, StaffTask>();
	private int curAttackDay;        // 当前攻打时间[2阶段黄巾军或者西凉军]
	private int nextAttackDay;       // 下一次攻打的时间[2阶段黄巾军或者西凉军]
	private int attackWave;          // 当前打到的波次[2阶段黄巾军或者西凉军]
	private long nextWaveTime;       // 下一次刷新时间
	private boolean isWaveContinue;  // 是否可以继续下一波
	private Map<Long, WarInfo> riotWar = new HashMap<Long, WarInfo>(); // 玩家当前的战斗
	private int riotItem;            // 暴乱信物
	private int riotScore;           // 暴乱积分
	private int killRiot;               //杀虫次数
	private boolean isExchangeEquip;  // 是否兑换金装
	private HashMap<Integer, Integer> icons = new HashMap<Integer, Integer>();
	private SevenRecord sevenRecord = new SevenRecord();
	private int rebelScore;          // 叛军活动积分
	//伏击叛军兑换信息
	private Map<Integer, Integer> rebelExchange = new HashMap<>();

	private int strikeSwarm;   //夜袭 虫群次数

	private long lastStrikeSwarm;//上一次夜袭虫群刷新的时间

	private long closeSpeakTime;//禁言的结束时间  为0的话表示没有禁言

	//叛军信物兑换信息
	private Map<Integer, Integer> riotItemExchange = new HashMap<>();
	//虫族入侵 buff信息
	private Map<Integer, Integer> riotBuff = new HashMap<>();
	//虫族入侵行军
	private March riotMarchs;

	//主宰积分
	private int zergScore;//主宰积分
	// 主宰商店购买记录proId,propId,次数,活动开启时间
	private HashMap<Integer, Pair<Integer, Long>> zergShop = new HashMap<>();

	// 沙盘演武积分
	@Getter
	@Setter
	private int manoeuvreScore;//沙盘演武积分
	// 沙盘演武商店购买记录proId,propId,次数,活动开启时间
	@Getter
	private HashMap<Integer, Pair<Integer, Long>> manoeuvreShop = new HashMap<>();

	/**
	 * 巨型虫族每日奖励次数
	 */
	private int bigMonsterReward = 0;

	private boolean firstBigMonsterReward = false;
	// 兵力池
	private Map<Integer, Integer> soliderPool = new HashMap<>();
	// 当前状态0 未清剿 1清剿中 2领取
	private int autoState = 0;
	// 击杀虫族等级
	private int autoKillLevel = 1;
	private int autoMaxKillLevel = 1;
	private List<Award> autoRewards = new ArrayList<>();
	// 玩家母巢之战中死亡的损兵数
	private int dieSoldiers = 0;
	// 本次玩家获得的血战积分
	private int thisTimePvpScore = 0;
	// 是否拜过师 为0没有拜过 不为0,即拜过师isHaveMaster为师傅lordId
	private long isHaveMaster = 0;
	// 下一次拜师时间
	private long nextHaveMasterTime = 0;
	// 拒绝拜师的列表 key:lordId value:time 拒绝的时间
	private Map<Long, Long> applyMasterRefuse = new HashMap<>();
	//材料礼包记录
	private Map<Integer, Long> resourceGiftRecord = new HashMap<>();
	//是否领取了首次叛军引导奖励
	@Getter
	@Setter
	private boolean firstRebelGuideAward = false;
	//是否补发了塔防活动奖励
	private boolean isReissueTDTaskAward = false;

	private int autoOnline = 1;// 1.未开启 2.开启
	private int autoNum;// 单次开启离线杀虫 杀虫次数

	private long searchTime;
	private List<Pos> searchPos = new ArrayList<>();

	public SimpleData() {
		this.autoKillLevel = 1;
		this.autoMaxKillLevel = 1;
	}

	// 存数据
	public SerSimpleData.Builder writeData() {
		SerSimpleData.Builder builder = SerSimpleData.newBuilder();
		builder.setLootGoodState(getLootGoodState());
		builder.setLootGoodTotalTimes(lootGoodTotalTimes);
		builder.setThreeLootHasHero(threeLootHasHero);
		builder.setBuyBuildTeam(buyBuildTeam);
		builder.setKillActMonsterDay(killActMonsterDay);
		builder.setKillActMonsterTimes(killActMonsterTimes);
		builder.setPvpScore(pvpScore);
		builder.setKillRebelDay(killRebelDay);
		builder.setKillRebelTimes(killRebelTimes);
		builder.setTotalKillNum(totalKillNum);
		builder.setBuyWordTimes(buyWordTimes);
		builder.setDieSoldiers(dieSoldiers);
		builder.setThisTimePvpScore(thisTimePvpScore);
		builder.setIsHaveMaster(isHaveMaster);
		for (Integer state : digState) {
			builder.addDigState(state);
		}

		builder.setDigPos(digPos.writeData());
		builder.setIsPaperDiged(isPaperDiged);
		builder.setIsGreeted(isGreeted);
		for (Buff buff : buffMap.values()) {
			builder.addBuffData(buff.writeData());
		}
		builder.setIsExchangeEquip(isExchangeEquip());
		if (!getIcons().isEmpty()) {
			for (Map.Entry<Integer, Integer> entry : icons.entrySet()) {
				if (entry == null) {
					continue;
				}

				int iconId = entry.getKey();
				int state = entry.getValue();
				if (iconId == 0) {
					continue;
				}
				DataPb.LordIconsData.Builder lordIconsData = DataPb.LordIconsData.newBuilder();
				lordIconsData.setIconId(iconId);
				lordIconsData.setIconState(state);
				builder.addIcons(lordIconsData);
			}
		}
		builder.setSevenRec(getSevenRecord().wrapPb());

		for (StaffTask task : staffTaskMap.values()) {
			builder.addStaffTask(task.wrap());
		}

		builder.setCurAttackDay(curAttackDay);
		builder.setNextAttackDay(nextAttackDay);
		builder.setIsWaveContinue(isWaveContinue);
		for (WarInfo warInfo : riotWar.values()) {
			if (warInfo != null) {
				builder.setRiotWar(warInfo.writeData());
				break;
			}
		}
		builder.setRiotItem(riotItem);
		builder.setRiotScore(riotScore);
		builder.setRebelScore(rebelScore);
		for (Map.Entry<Integer, Integer> entry : rebelExchange.entrySet()) {
			builder.addRebelExchange(SerializePb.RebelExchange.newBuilder().setAwardId(entry.getKey()).setExNum(entry.getValue()));
		}
		builder.setStrikeSwarm(strikeSwarm);
		builder.setLastStrikeSwarm(lastStrikeSwarm);
		riotItemExchange.forEach((e, f) -> {
			builder.addRiotExchange(CommonPb.TwoInt.newBuilder().setV1(e).setV2(f).build());
		});
		riotBuff.forEach((e, f) -> {
			builder.addRiotBuff(CommonPb.TwoInt.newBuilder().setV1(e).setV2(f).build());
		});
		builder.setKillRiot(killRiot);
		if (riotMarchs != null) {
			builder.setMarchData(riotMarchs.writeMarch());
		}
		builder.setAttackWave(attackWave);
		builder.setBigMonsterReward(bigMonsterReward);
		builder.setFirstBigMonsterReward(firstBigMonsterReward ? 1 : 0);
		if (soliderPool != null) {
			soliderPool.forEach((e, f) -> {
				builder.addSoliderPool(CommonPb.TwoInt.newBuilder().setV1(e).setV2(f).build());
			});
		}
//        builder.setAutoState(autoState);
		autoRewards.forEach(e -> {
			builder.addAutoAwards(PbHelper.createAward(e.getType(), e.getId(), e.getCount()));
		});
		builder.setAutoKillLevel(autoKillLevel);
		builder.setAutoMaxKillLevel(autoMaxKillLevel);
		for (Entry<Long, Long> entry : applyMasterRefuse.entrySet()) {
			Builder builder1 = TwoLong.newBuilder();
			builder1.setV1(entry.getKey());
			builder1.setV2(entry.getValue());
			builder.addApplyMasterRefuse(builder1);
		}
		for (Entry<Integer, Long> entry : resourceGiftRecord.entrySet()) {
			IntLong.Builder builder1 = IntLong.newBuilder();
			builder1.setV1(entry.getKey());
			builder1.setV2(entry.getValue());
			builder.addResourceGiftRecord(builder1);
		}
		builder.setZergScore(zergScore);
		for (Entry<Integer, Pair<Integer, Long>> entiy : zergShop.entrySet()) {
			PairIntLong.Builder zergShopBuilder = PairIntLong.newBuilder();
			zergShopBuilder.setV1(entiy.getKey());
			zergShopBuilder.setV2(entiy.getValue().getLeft());
			zergShopBuilder.setV3(entiy.getValue().getRight());
			builder.addZergShopRecord(zergShopBuilder.build());
		}
		builder.setManoeuvreScore(manoeuvreScore);
		for (Entry<Integer, Pair<Integer, Long>> entiy : manoeuvreShop.entrySet()) {
			PairIntLong.Builder manoeuvreShopBuilder = PairIntLong.newBuilder();
			manoeuvreShopBuilder.setV1(entiy.getKey());
			manoeuvreShopBuilder.setV2(entiy.getValue().getLeft());
			manoeuvreShopBuilder.setV3(entiy.getValue().getRight());
			builder.addManoeuvreShopRecord(manoeuvreShopBuilder.build());
		}
		builder.setFirstRebelGuideAward(firstRebelGuideAward);
		builder.setIsReissueTDTaskAward(isReissueTDTaskAward);

		builder.setAutoOnline(this.autoOnline);
		builder.setAutoNum(this.autoNum);
		builder.setNextHaveMasterTime(this.nextHaveMasterTime);
		return builder;
	}

	public byte[] serSimpleData() {
		SerSimpleData.Builder builder = writeData();
		return builder.build().toByteArray();
	}

	// 读数据
	public void dserSimpleData(SerSimpleData data) {
		if (data == null) {
			return;
		}
		lootGoodState = data.getLootGoodState();
		threeLootHasHero = data.getThreeLootHasHero();
		lootGoodTotalTimes = data.getLootGoodTotalTimes();
		buyBuildTeam = data.getBuyBuildTeam();
		killActMonsterDay = data.getKillActMonsterDay();
		killActMonsterTimes = data.getKillActMonsterTimes();
		pvpScore = data.getPvpScore();
		killRebelDay = data.getKillRebelDay();
		killRebelTimes = data.getKillRebelTimes();
		totalKillNum = data.getTotalKillNum();
		buyWordTimes = data.getBuyWordTimes();
		dieSoldiers = data.getDieSoldiers();
		thisTimePvpScore = data.getThisTimePvpScore();
		isHaveMaster = data.getIsHaveMaster();
		digState.clear();
		for (Integer state : data.getDigStateList()) {
			digState.add(state);
		}
		digPos.readData(data.getDigPos());
		isPaperDiged = data.getIsPaperDiged();
		isGreeted = data.getIsGreeted();
		List<DataPb.BuffData> buffDatas = data.getBuffDataList();
		for (DataPb.BuffData buffData : buffDatas) {
			Buff buff = new Buff();
			buff.readData(buffData);
			buffMap.put(buff.getBuffId(), buff);
		}
		setExchangeEquip(data.getIsExchangeEquip());
		getIcons().clear();
		if (data.getIconsCount() > 0) {
			for (DataPb.LordIconsData iconsData : data.getIconsList()) {
				if (iconsData == null) {
					continue;
				}
				if (iconsData.getIconId() == 0) {
					continue;
				}
				if (iconsData.getIconId() == 10011) {
					icons.put(11, iconsData.getIconState());
				} else {
					icons.put(iconsData.getIconId(), iconsData.getIconState());
				}
			}
		}
		for (CommonPb.StaffTask taskData : data.getStaffTaskList()) {
			StaffTask staffTask = new StaffTask();
			staffTask.unwrap(taskData);
			staffTaskMap.put(staffTask.getTaskId(), staffTask);
		}

		curAttackDay = data.getCurAttackDay();
		nextAttackDay = data.getNextAttackDay();
		isWaveContinue = data.getIsWaveContinue();
		riotWar.clear();
		if (data.hasRiotWar()) {
			DataPb.WarData warData = data.getRiotWar();
			WarInfo warInfo = SpringUtil.getBean(WarManager.class).createRiotWar(warData);
			riotWar.put(warInfo.getWarId(), warInfo);
		}

		riotItem = data.getRiotItem();
		riotScore = data.getRiotScore();
		getSevenRecord().unwrap(data.getSevenRec());
		rebelScore = data.getRebelScore();
		for (SerializePb.RebelExchange rebelExchange : data.getRebelExchangeList()) {
			this.rebelExchange.put(rebelExchange.getAwardId(), rebelExchange.getExNum());

		}
		strikeSwarm = data.getStrikeSwarm();
		lastStrikeSwarm = data.getLastStrikeSwarm();
		data.getRiotExchangeList().forEach(e -> {
			riotItemExchange.put(e.getV1(), e.getV2());
		});
		data.getRiotBuffList().forEach(e -> {
			riotBuff.put(e.getV1(), e.getV2());
		});
		killRiot = data.getKillRiot();
		March march = new March();
		march.readMarch(data.getMarchData());
		if (march.getLordId() != 0) {
			riotMarchs = march;
		}
		attackWave = data.getAttackWave();
		bigMonsterReward = data.getBigMonsterReward();
		firstBigMonsterReward = data.getFirstBigMonsterReward() == 1;
		data.getSoliderPoolList().forEach(e -> {
			soliderPool.put(e.getV1(), e.getV2());
		});
//        autoState = data.getAutoState();
		autoKillLevel = data.getAutoKillLevel();
		autoMaxKillLevel = data.getAutoMaxKillLevel();
		data.getAutoAwardsList().forEach(e -> {
			autoRewards.add(new Award(e.getType(), e.getId(), e.getCount()));
		});
		if (autoRewards.size() > 0) {
			autoState = 2;
		}
		if (autoKillLevel == 0) {
			autoKillLevel = 1;
		}
		if (autoMaxKillLevel == 0) {
			autoMaxKillLevel = 1;
		}
		for (TwoLong twoLong : data.getApplyMasterRefuseList()) {
			applyMasterRefuse.put(twoLong.getV1(), twoLong.getV2());
		}
		for (IntLong intLong : data.getResourceGiftRecordList()) {
			resourceGiftRecord.put(intLong.getV1(), intLong.getV2());
		}
		this.zergScore = data.getZergScore();
		for (PairIntLong pairIntLong : data.getZergShopRecordList()) {
			zergShop.put(pairIntLong.getV1(), new Pair<>(pairIntLong.getV2(), pairIntLong.getV3()));
		}
		this.manoeuvreScore = data.getManoeuvreScore();
		for (PairIntLong pairIntLong : data.getManoeuvreShopRecordList()) {
			manoeuvreShop.put(pairIntLong.getV1(), new Pair<>(pairIntLong.getV2(), pairIntLong.getV3()));
		}
		this.firstRebelGuideAward = data.getFirstRebelGuideAward();
		this.isReissueTDTaskAward = data.getIsReissueTDTaskAward();

		this.autoOnline = data.getAutoOnline();
		this.autoNum = data.getAutoNum();
		this.nextHaveMasterTime = data.getNextHaveMasterTime();
	}


	public void subScore(int score) {
		if (score < 0) {
			LogHelper.CONFIG_LOGGER.info("score zero");
			return;
		}

		if (pvpScore < score) {
			LogHelper.CONFIG_LOGGER.info("pvpScore less then score.");
			return;
		}

		pvpScore -= score;
		pvpScore = Math.max(0, pvpScore);
	}

	public void addPvpScore(int score) {
		if (score < 0) {
			LogHelper.CONFIG_LOGGER.info("score zero");
			return;
		}

		if (pvpScore >= Integer.MAX_VALUE - score) {
			return;
		}
		thisTimePvpScore += score;
		pvpScore += score;
	}

	public boolean getPosUsed() {
		return buyWordTimes != 0 && digState.contains(buyWordTimes);
	}

	// 清除国宴数据
	public void clearBanquetInfo() {
		buyWordTimes = 0;
		digState.clear();
		digPos = new Pos();
		isPaperDiged = false;
		isGreeted = false;
	}

	public WarInfo getRiotWarInfo() {
		if (riotWar.isEmpty()) {
			return null;
		}

		WarInfo warInfo = null;
		for (WarInfo w : riotWar.values()) {
			warInfo = w;
			break;
		}

		return warInfo;
	}

	public boolean hasIcon(int iconId) {
		return icons.containsKey(iconId);
	}

	public List<CommonPb.LordIcons> wrapIcons() {
		List<CommonPb.LordIcons> iconsPb = new ArrayList<CommonPb.LordIcons>();
		for (Map.Entry<Integer, Integer> entry : icons.entrySet()) {
			if (entry == null) {
				continue;
			}
			CommonPb.LordIcons.Builder builder = CommonPb.LordIcons.newBuilder();
			builder.setIconId(entry.getKey());
			builder.setIconState(entry.getValue());
			iconsPb.add(builder.build());
		}
		return iconsPb;
	}

	public void updateWashHeroNum(int heroId, int quality) {
		getSevenRecord().updateWashHeroNum(heroId, quality);
	}

	public void updateMakeEquipNum(int quality, int equipType) {
		getSevenRecord().updateMakeEquipNum(quality, equipType);
	}

	public void updateWashEquipNum(int keyId, int quality) {
		getSevenRecord().updateWashEquipNum(keyId, quality);
	}

	public int getMaxScore() {
		return getSevenRecord().getMaxScore();
	}

	public void setMaxScore(int maxScore) {
		getSevenRecord().setMaxScore(maxScore);
	}

	public int getWashEquipMax(int quality) {
		return getSevenRecord().getWashEquipMax(quality);
	}

	public int getWashHeroMax(int quality) {
		return getSevenRecord().getWashHeroMax(quality);
	}

	public int getEquipMake(int quality, int equipType) {
		return getSevenRecord().getEquipMake(quality, equipType);
	}

	public void addRebelExchange(int id) {
		Integer num = rebelExchange.get(id);
		if (num == null) {
			rebelExchange.put(id, 1);
		} else {
			rebelExchange.put(id, num + 1);
		}
	}

	public void addKillRiot() {
		this.killRiot += 1;
	}

	public void clearRiotData() {
		this.riotItem = 0;
		this.riotBuff.clear();
		this.riotItemExchange.clear();
		this.killRiot = 0;
		this.riotWar.clear();
		this.attackWave = 0;
		this.isWaveContinue = false;
		this.riotMarchs = null;
	}

	public void addDieSoldiers(int score) {
		if (score < 0) {
			LogHelper.CONFIG_LOGGER.info("score zero");
			return;
		}

		if (pvpScore >= Integer.MAX_VALUE - score) {
			return;
		}
		this.dieSoldiers += score;
	}

	public void addZergScore(int score) {
		if (score < 0) {
			return;
		}
		this.zergScore += score;
	}

	public void subZergScore(int score) {
		if (score < 0) {
			return;
		}
		this.zergScore -= score;
	}

	public int getZergScore() {
		return zergScore;
	}

	public HashMap<Integer, Pair<Integer, Long>> getZergShop() {
		return zergShop;
	}

	public void setZergShop(HashMap<Integer, Pair<Integer, Long>> zergShop) {
		this.zergShop = zergShop;
	}

	public void addAutoNum() {
		this.autoNum += 1;
	}

}
