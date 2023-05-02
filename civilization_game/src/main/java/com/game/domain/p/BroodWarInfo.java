package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.util.LogHelper;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 母巢玩家属性
 *
 *
 * @date 2021/7/5 15:45
 */
@Getter
@Setter
public class BroodWarInfo {

	/**
	 * 优先出击次数
	 */
	private int firstAttack;
	/**
	 * 立即出击次数
	 */
	private int attackNow;
	/**
	 * 复活次数
	 */
	private int revive;
	/**
	 * 母巢BUFF 类型 ID
	 */
	private Map<Integer, Integer> broodWarBuff = new HashMap<>();
	/**
	 * 初始增益等级 类型 次数
	 */
	private Map<Integer, Integer> broodWarBuffBuy = new HashMap<>();

	/**
	 * 英雄ID 英雄恢复时间
	 */
	private Map<Integer, Long> heroInfo = new HashMap<>();

	/**
	 * 累积杀敌
	 */
	private int totalKill;

	/**
	 * 连续杀敌
	 */
	private int mulitKill;

	/**
	 * 损兵
	 */
	private int diedSolider;

	// 立即攻击英雄
	private List<Integer> fighNow = new LinkedList<>();

	public void addTotalKill(int kill) {
		this.totalKill += kill;
	}

	public void addTotalLost(int lost) {
		this.diedSolider += lost;
	}

	public void reset() {
		this.mulitKill = 0;
		this.diedSolider = 0;
		this.totalKill = 0;
		this.firstAttack = 0;
		this.attackNow = 0;
		this.revive = 0;
//        this.broodWarBuff.clear();
//        this.broodWarBuffBuy.clear();
		this.heroInfo.clear();
		this.fighNow.clear();
	}

	public byte[] toData() {
		DataPb.BroodWarInfoData.Builder builder = DataPb.BroodWarInfoData.newBuilder();
		builder.setIntList(DataPb.IntList.newBuilder()
			.addInts(firstAttack)
			.addInts(attackNow)
			.addInts(revive)
			.addInts(totalKill)
			.addInts(mulitKill)
			.addInts(diedSolider)
			.build());
		DataPb.TwoIntList.Builder buff = DataPb.TwoIntList.newBuilder();
		broodWarBuff.forEach((e, f) -> {
			buff.addTwoInts(CommonPb.TwoInt.newBuilder().setV1(e).setV2(f).build());
		});
		builder.setBuff(buff);
		DataPb.TwoIntList.Builder buffBuy = DataPb.TwoIntList.newBuilder();
		broodWarBuffBuy.forEach((e, f) -> {
			buffBuy.addTwoInts(CommonPb.TwoInt.newBuilder().setV1(e).setV2(f).build());
		});
		builder.setBuffBuy(buffBuy);
		DataPb.IntLongList.Builder info = DataPb.IntLongList.newBuilder();
		heroInfo.forEach((e, f) -> {
			info.addIntLong(CommonPb.IntLong.newBuilder().setV1(e).setV2(f).build());
		});
		builder.setHeroInfo(info);
		builder.setFightNow(DataPb.IntList.newBuilder().addAllInts(fighNow).build());
		return builder.build().toByteArray();
	}

	public void reloadData(byte[] data) {
		try {
			DataPb.BroodWarInfoData broodWarInfoData = DataPb.BroodWarInfoData.parseFrom(data);
			List<Integer> ints = broodWarInfoData.getIntList().getIntsList();
			if (ints.size() > 0) {
				this.firstAttack = ints.get(0);
				this.attackNow = ints.get(1);
				this.revive = ints.get(2);
				this.totalKill = ints.get(3);
				this.mulitKill = ints.get(4);
				this.diedSolider = ints.get(5);
			}
			broodWarInfoData.getBuff().getTwoIntsList().forEach(e -> {
				this.broodWarBuff.put(e.getV1(), e.getV2());
			});
			broodWarInfoData.getBuffBuy().getTwoIntsList().forEach(e -> {
				this.broodWarBuffBuy.put(e.getV1(), e.getV2());
			});
			broodWarInfoData.getHeroInfo().getIntLongList().forEach(e -> {
				this.heroInfo.put(e.getV1(), e.getV2());
			});
			fighNow.addAll(broodWarInfoData.getFightNow().getIntsList());

		} catch (InvalidProtocolBufferException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	public void cleanHero(int heroId) {
		if (fighNow.isEmpty()) {
			return;
		}
		Iterator<Integer> it = fighNow.iterator();
		while (it.hasNext()) {
			if (it.next().intValue() == heroId) {
				it.remove();
				break;
			}
		}

	}
}
