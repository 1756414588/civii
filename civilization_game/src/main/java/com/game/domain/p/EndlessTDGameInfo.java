package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.CommonPb.EndlessTDGameBase;
import com.game.pb.CommonPb.EndlessTDTowerPosRecord;
import com.game.pb.CommonPb.IntLong;
import com.game.pb.CommonPb.Prop;
import com.game.pb.CommonPb.ThreeInt;
import com.game.pb.CommonPb.TwoInt;
import com.game.pb.TDPb.EndlessTDReportRq;
import com.game.util.Md5Util;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/13 15:25
 **/
@Getter
@Setter
public class EndlessTDGameInfo {
	private int wave; // 存档波次
	private int startDate; // 本次挑战开始时间 "20211213" 不是当天则不扣除当天的挑战次数
	private int levelId; // 关卡id
	private int lifePoint;// 基地血量
	private int supplies; // 游戏物资数量
	private int fraction; // 成绩
	private Map<Integer, EndlessTDTowerRecord> towerPosRecord = new ConcurrentHashMap<>(); // 游戏炮塔位置信息
	private Map<Integer, Integer> itemMap = new ConcurrentHashMap<>(); // 玩家游戏中的道具背包 一次性道具 key:道具id value:道具数量
	private Map<Integer, Integer> takeEffect = new ConcurrentHashMap<>(); // 被动道具 被动道具 key:道具id value:道具数量
	private Map<Integer, Integer> levelProps = new ConcurrentHashMap<>();// 只生效一关的道具key:道具id value:道具数量
	private Map<Integer, Integer> awardItems = new ConcurrentHashMap<>();// 玩家奖励的道具 key:道具id value:道具数量
	private Map<Integer, Long> levelTime = new ConcurrentHashMap<>();// 通关时间 key:波次 value:通关时间
	private Map<Integer, Integer> monsterMap = new ConcurrentHashMap<>(); // 当前关卡的怪物列表 key:怪物id value:怪物数量
	private List<Integer> selectPropId = new ArrayList<>(); // 开始游戏时选择的道具
	private Map<Integer, Integer> propBuff = new ConcurrentHashMap<>();// 道具buff key:type value:数值
	private Map<Integer, Integer> waveRoute = new ConcurrentHashMap<>();// 当前关卡怪物出口位置 key:波次 value:出口位置
	private String token;// 校验token
	private int crtime;// 客户的那发过来的时间
	private HashBasedTable<Integer, Integer, Integer> levelFraction = HashBasedTable.create(); // 通关波次 关卡实体id 本关获得的分数

	public void init() {
		wave = 0;
		levelId = 0;
		lifePoint = 100;
		supplies = 0;
		fraction = 0;
		towerPosRecord.clear();
		itemMap.clear();
		takeEffect.clear();
		levelProps.clear();
		awardItems.clear();
		levelTime.clear();
		monsterMap.clear();
		propBuff.clear();
		waveRoute.clear();
		levelFraction.clear();
	}

	// 结算游戏小关卡分数
	public void addFraction(int count) {
		if (count < 0) {
			return;
		}
		fraction += count;
	}

	public EndlessTDGameBase.Builder wrapPb() {
		EndlessTDGameBase.Builder builder = EndlessTDGameBase.newBuilder();
		builder.setTowerId(wave);
		builder.setLifePoint(lifePoint);
		builder.setSupplies(supplies);
		// 游戏背包
		builder.addAllProp(itemMapWrapPb());
		builder.addAllPassiveProps(takeEffectWrapPb());
		builder.setToken(token);
		// 游戏炮塔信息
		towerPosRecord.values().forEach(e -> {
			builder.addTowerPosRecord(e.wrapPb());
		});
		builder.setFraction(fraction);
		return builder;
	}

	public List<Prop> itemMapWrapPb() {
		List<Prop> list = Lists.newArrayList();
		itemMap.forEach((k, v) -> {
			Prop.Builder prop = Prop.newBuilder();
			prop.setPropId(k);
			prop.setPropNum(v);
			list.add(prop.build());
		});
		return list;
	}

	public List<Prop> takeEffectWrapPb() {
		List<Prop> list = Lists.newArrayList();
		takeEffect.forEach((k, v) -> {
			Prop.Builder prop = Prop.newBuilder();
			prop.setPropId(k);
			prop.setPropNum(v);
			list.add(prop.build());
		});
		levelProps.forEach((k, v) -> {
			Prop.Builder prop = Prop.newBuilder();
			prop.setPropId(k);
			prop.setPropNum(v);
			list.add(prop.build());
		});
		return list;
	}

	// 资源变更
	public void changeSupplies(int count) {
		supplies += count;
		if (supplies < 0) {
			supplies = 0;
		}
	}

	// 血量变更
	public void changeLifePoint(int count) {
		lifePoint += count;
		if (lifePoint <= 0) {
			lifePoint = 1;
		}
	}

	// 设置token
	public void putToken() {
		token = Md5Util.string2MD5(String.valueOf(System.currentTimeMillis()));
	}

	// 检查客户端上报的token是否正确
	public boolean checkToken(String compare, String value) {
		return Md5Util.string2MD5(token + value).equals(compare);
	}

	// 关卡小结
	public void gameSettlement(EndlessTDReportRq rq) {
		// 剩余资源
		supplies = rq.getSupplies();
		levelTime.put(wave, rq.getClearanceTime());
		// 当前防御塔分布
		List<EndlessTDTowerPosRecord> towerPosRecordList = rq.getTowerPosRecordList();
		towerPosRecord.clear();
		towerPosRecordList.forEach(e -> {
			towerPosRecord.put(e.getPos(), new EndlessTDTowerRecord(e));
		});
		awardItems.clear();
		levelProps.clear();
	}

	public CommonPb.EndlessTDGameInfo ser() {
		CommonPb.EndlessTDGameInfo.Builder builder = CommonPb.EndlessTDGameInfo.newBuilder();
		builder.setWave(wave).setStartDate(startDate).setLevelId(levelId).setLifePoint(lifePoint).setSupplies(supplies).setFraction(fraction);
		towerPosRecord.forEach((k, v) -> {
			builder.addTowerPosRecord(v.wrapPb());
		});
		itemMap.forEach((k, v) -> {
			builder.addItemMap(TwoInt.newBuilder().setV1(k).setV2(v).build());
		});
		takeEffect.forEach((k, v) -> {
			builder.addTakeEffect(TwoInt.newBuilder().setV1(k).setV2(v).build());
		});
		levelProps.forEach((k, v) -> {
			builder.addLevelProps(TwoInt.newBuilder().setV1(k).setV2(v).build());
		});
		awardItems.forEach((k, v) -> {
			builder.addAwardItems(TwoInt.newBuilder().setV1(k).setV2(v).build());
		});
		levelTime.forEach((k, v) -> {
			builder.addLevelTime(IntLong.newBuilder().setV1(k).setV2(v).build());
		});
		monsterMap.forEach((k, v) -> {
			builder.addMonsterMap(TwoInt.newBuilder().setV1(k).setV2(v).build());
		});
		selectPropId.forEach(e -> {
			builder.addSelectPropId(e);
		});
		propBuff.forEach((k, v) -> {
			builder.addPropBuff(TwoInt.newBuilder().setV1(k).setV2(v).build());
		});
		waveRoute.forEach((k, v) -> {
			builder.addWaveRoute(TwoInt.newBuilder().setV1(k).setV2(v).build());
		});
		levelFraction.columnMap().forEach((k, v) -> {
			ThreeInt.Builder b = ThreeInt.newBuilder();
			b.setV1(k);
			v.forEach((n, m) -> {
				b.setV2(n);
				b.setV3(m);
			});
			builder.addLevelFraction(b);
		});
		return builder.build();
	}

	public void dser(CommonPb.EndlessTDGameInfo build) {
		if (build == null) {
			return;
		}
		wave = build.getWave();
		startDate = build.getStartDate();
		levelId = build.getLevelId();
		lifePoint = build.getLifePoint();
		supplies = build.getSupplies();
		fraction = build.getFraction();
		build.getTowerPosRecordList().forEach(e -> {
			towerPosRecord.put(e.getPos(), new EndlessTDTowerRecord(e));
		});
		build.getItemMapList().forEach(e -> {
			itemMap.put(e.getV1(), e.getV2());
		});
		build.getTakeEffectList().forEach(e -> {
			takeEffect.put(e.getV1(), e.getV2());
		});
		build.getLevelPropsList().forEach(e -> {
			levelProps.put(e.getV1(), e.getV2());
		});
		build.getAwardItemsList().forEach(e -> {
			awardItems.put(e.getV1(), e.getV2());
		});
		build.getLevelTimeList().forEach(e -> {
			levelTime.put(e.getV1(), e.getV2());
		});
		build.getMonsterMapList().forEach(e -> {
			monsterMap.put(e.getV1(), e.getV2());
		});
		build.getSelectPropIdList().forEach(e -> {
			selectPropId.add(e);
		});
		build.getPropBuffList().forEach(e -> {
			propBuff.put(e.getV1(), e.getV2());
		});
		build.getWaveRouteList().forEach(e -> {
			waveRoute.put(e.getV1(), e.getV2());
		});
		build.getLevelFractionList().forEach(e -> {
			levelFraction.put(e.getV1(), e.getV2(), e.getV3());
		});
	}

}
