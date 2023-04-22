package com.game.domain.p;

import com.game.pb.CommonPb.SerEndlessTDInfo;
import com.game.pb.CommonPb.TwoInt;
import com.game.pb.TDPb.EndlessTowerDefenseInitRs;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/11/25 11:21
 **/
@Getter
@Setter
public class EndlessTDInfo {
	private int remainingTimes; // 剩余挑战次数
	private int refreshTime; // 刷新时间
	private int weekMaxFraction; // 本周最好成绩
	private int historyMaxFraction; // 历史最好成绩
	private boolean rankAward; // 是否有排行奖励 true:可以领奖 false:不可以领奖
	private int lastWeekFraction; // 上周分数
	private int lastWeekRank; // 上周排名
	private Map<Integer, Integer> endlessTDBonus = new ConcurrentHashMap<>(); // 玩家无尽塔防加成信息 key:类型，1机枪塔，2火箭炮塔，3电磁塔，4激光塔 value:s_tower_war_bonus id
	private Map<Integer, Integer> convertShopInfo = new ConcurrentHashMap<>(); // 兑换商店购买信息 key:物品id value:够买次数
	private Map<Integer, Integer> armoryShop = new ConcurrentHashMap<>(); // 军械商店物品信息 key:物品id value:折扣
	private Map<Integer, Integer> armoryShopInfo = new ConcurrentHashMap<>(); // 军械商店购买信息 key:物品id value:够买次数
	private Map<Integer, Integer> refreshShopTimes = new ConcurrentHashMap<>();// 刷新商店次数 key:商店类型 value:刷新次数 1:军械商店
	private EndlessTDGameInfo gameInfo = new EndlessTDGameInfo();

	public EndlessTDInfo() {
	}

	// 跨天重置数据
	public void resetInfo(int currentDay) {
		setRemainingTimes(1);
		setRefreshTime(currentDay);
		convertShopInfo.clear();
		armoryShop.clear();
		armoryShopInfo.clear();
		refreshShopTimes.clear();
	}

	// 扣除挑战次数
	public void deductRemainingTimes() {
		gameInfo.setStartDate(0);
		setRemainingTimes(getRemainingTimes() - 1);
	}

	// 设置玩家分数
	public void putWeekMaxFraction(int fraction) {
		if (fraction < getWeekMaxFraction()) {
			return;
		}
		setWeekMaxFraction(fraction);
		if (fraction >= getHistoryMaxFraction()) {
			setHistoryMaxFraction(fraction);
		}
	}

	public void wrapPb(EndlessTowerDefenseInitRs.Builder builder) {
		builder.setRemainingTimes(this.remainingTimes);
		builder.setWeekMaxFraction(this.weekMaxFraction);
		builder.setHistoryMaxFraction(this.historyMaxFraction);
		builder.setRankAward(this.rankAward);
		builder.setLastWeekFraction(lastWeekFraction);
		builder.setLastWeekRank(lastWeekRank);
		builder.setCurrentWave(gameInfo.getWave());
	}

	public void dserEndlessTDInfo(SerEndlessTDInfo ser) {
		this.remainingTimes = ser.getRemainingTimes();
		this.refreshTime = (int) ser.getRefreshTime();
		this.weekMaxFraction = ser.getWeekMaxFraction();
		this.historyMaxFraction = ser.getHistoryMaxFraction();
		this.rankAward = ser.getRankAward();
		this.lastWeekFraction = ser.getLastWeekFraction();
		this.lastWeekRank = ser.getLastWeekRank();
		ser.getEndlessTDBonusList().forEach(e -> {
			this.endlessTDBonus.put(e.getV1(), e.getV2());
		});
		ser.getConvertShopInfoList().forEach(e -> {
			this.convertShopInfo.put(e.getV1(), e.getV2());
		});
		ser.getArmoryShopList().forEach(e -> {
			armoryShop.put(e.getV1(), e.getV2());
		});
		ser.getArmoryShopInfoList().forEach(e -> {
			armoryShopInfo.put(e.getV1(), e.getV2());
		});
		ser.getRefreshShopTimesList().forEach(e -> {
			refreshShopTimes.put(e.getV1(), e.getV2());
		});
		gameInfo.dser(ser.getEndlessTDGameInfo());
	}

	public SerEndlessTDInfo serEndlessTDInfo() {
		SerEndlessTDInfo.Builder builder = SerEndlessTDInfo.newBuilder();
		builder.setRemainingTimes(this.remainingTimes);
		builder.setRefreshTime(this.refreshTime);
		builder.setWeekMaxFraction(this.weekMaxFraction);
		builder.setHistoryMaxFraction(this.historyMaxFraction);
		builder.setRankAward(this.rankAward);
		builder.setLastWeekFraction(this.lastWeekFraction);
		builder.setLastWeekRank(this.lastWeekRank);
		this.endlessTDBonus.forEach((k, v) -> {
			builder.addEndlessTDBonus(TwoInt.newBuilder().setV1(k).setV2(v));
		});
		this.convertShopInfo.forEach((k, v) -> {
			builder.addConvertShopInfo(TwoInt.newBuilder().setV1(k).setV2(v));
		});
		this.armoryShop.forEach((k, v) -> {
			builder.addArmoryShop(TwoInt.newBuilder().setV1(k).setV2(v));
		});
		this.armoryShopInfo.forEach((k, v) -> {
			builder.addArmoryShopInfo(TwoInt.newBuilder().setV1(k).setV2(v));
		});
		this.refreshShopTimes.forEach((k, v) -> {
			builder.addRefreshShopTimes(TwoInt.newBuilder().setV1(k).setV2(v));
		});
		builder.setEndlessTDGameInfo(gameInfo.ser());
		return builder.build();
	}
}
