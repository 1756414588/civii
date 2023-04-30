package com.game.manager;

import com.game.constant.EndlessTDItemEffectType;
import com.game.constant.TdDropLimitId;
import com.game.constant.TdEndlessItemType;
import com.game.dao.s.StaticDataDao;
import com.game.dataMgr.BaseDataMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticTDMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.EndlessTDGameInfo;
import com.game.domain.p.EndlessTDInfo;
import com.game.domain.p.EndlessTDRank;
import com.game.domain.p.EndlessTDTowerRecord;
import com.game.domain.p.Hero;
import com.game.domain.p.Property;
import com.game.domain.p.TD;
import com.game.domain.s.*;
import com.game.job.ServerStatisticsJob;
import com.game.log.LogUser;
import com.game.log.domain.EndlessTDErrorLog;
import com.game.pb.CommonPb.EndlessTDGameBase;
import com.game.pb.CommonPb.EndlessTDShopGoods;
import com.game.pb.CommonPb.EndlessTDTowerPos;
import com.game.pb.CommonPb.IntDouble;
import com.game.pb.CommonPb.Monster;
import com.game.pb.CommonPb.Prop;
import com.game.pb.CommonPb.TDRank.Builder;
import com.game.pb.CommonPb.TDRankInfo;
import com.game.pb.CommonPb.TwoDouble;
import com.game.pb.CommonPb.TwoInt;
import com.game.pb.CommonPb.WaveInfo;
import com.game.pb.CommonPb.WaveList;
import com.game.pb.CommonPb.WayList;
import com.game.pb.TDPb.PlayEndlessTDRs;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import com.game.util.RandomUtil;
import com.game.spring.SpringUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @date 2020/8/19 14:59
 * @description
 */
@Component
public class TDManager  {

	@Autowired
	private HeroManager heroManager;
	@Autowired
	private KillEquipManager killEquipManager;
	@Autowired
	private TechManager techManager;
	@Autowired
	private CountryManager ctManger;
	@Autowired
	private StaticTDMgr staticTDMgr;


	// 无尽模式玩家周排行
	@Getter
	private List<EndlessTDRank> weekEndlessTDRanks = new LinkedList<>();
	// 无尽模式玩家历史排行
	@Getter
	private Map<Integer, List<EndlessTDRank>> historyEndlessTDRanks = new ConcurrentHashMap<>();

	public List<Integer> getBounds(Player player) {
		// 建筑
		int buildingScore = player.getBuildingScore();
		// 装备
		int equipBattleScore = 0;
		int tecBattleScore = 0;
		for (Integer heroId : player.getEmbattleList()) {
			Hero hero = player.getHeros().get(heroId);
			if (null != hero) {
				// 装备属性, 直接加
				equipBattleScore += heroManager.caculateBattleScore(heroManager.getEquipProperty(hero));
				// 科技加攻击
				/*
				 * tecBattleScore += heroManager.caculateBattleScore( new Property( (int) techManager.getHeroAttack(player, heroManager.getSoldierType(heroId)), 0, 0));
				 */
//                List<List<Integer>> heroProperty = techManager.getHeroProperty(player, heroManager.getSoldierType(heroId));
//                if (null != heroProperty && !heroProperty.isEmpty()) {
//                    for (List<Integer> effect : heroProperty) {
//                        if (null != effect && effect.size() == 2) {
//                            int effectType = effect.get(0);
//                            if (effectType == PropertyType.ATTCK) {
//                                tecBattleScore += heroManager
//                                        .caculateBattleScore(new Property((effect.get(1)), 0, 0));
//                            }
//                        }
//                    }
//                }
				Property heroProperty = techManager.getHeroProperty(player, heroManager.getSoldierType(heroId), new Property());
				tecBattleScore += heroManager.caculateBattleScore(new Property((heroProperty.getAttack()), 0, 0));

			}
		}
		// 神器
		int ctBattleScore = heroManager.caculateBattleScore(killEquipManager.getAllProperty(player)) * player.getEmbattleList().size();
		// 爵位加成
		int titleBattleScore = heroManager.caculateBattleScore(new Property((int) ctManger.getTitleAttack(player).getAttack(), (int) ctManger.getTitleAttack(player).getDefence(), (int) ctManger.getTitleAttack(player).getSoldierNum()));

		// 上阵英雄
		int basecaBattleScore = player.getMaxScore() - equipBattleScore - ctBattleScore - titleBattleScore - tecBattleScore - buildingScore;
		return Lists.newArrayList(buildingScore, basecaBattleScore, equipBattleScore, ctBattleScore);
	}

	/**
	 * 计算塔防战力加成开启状态
	 *
	 * @param player
	 */
	public void openBouns(Player player) {
		// 建筑
		int buildingScore = player.getBuildingScore();
		// 装备
		int equipBattleScore = 0;
		int tecBattleScore = 0;
		for (Integer heroId : player.getEmbattleList()) {
			Hero hero = player.getHeros().get(heroId);
			if (null != hero) {
				// 装备属性, 直接加
				equipBattleScore += heroManager.caculateBattleScore(heroManager.getEquipProperty(hero));
				// 科技加攻击
				/*
				 * tecBattleScore += heroManager.caculateBattleScore( new Property( (int) techManager.getHeroAttack(player, heroManager.getSoldierType(heroId)), 0, 0));
				 */
//                List<List<Integer>> heroProperty = techManager.getHeroProperty(player, heroManager.getSoldierType(heroId));
//                if (null != heroProperty && !heroProperty.isEmpty()) {
//                    for (List<Integer> effect : heroProperty) {
//                        if (null != effect && effect.size() == 2) {
//                            int effectType = effect.get(0);
//                            if (effectType == PropertyType.ATTCK) {
//                                tecBattleScore += heroManager
//                                        .caculateBattleScore(new Property((effect.get(1)), 0, 0));
//                            }
//                        }
//                    }
//                }
				Property heroProperty = techManager.getHeroProperty(player, heroManager.getSoldierType(heroId), new Property());
				tecBattleScore += heroManager.caculateBattleScore(new Property((heroProperty.getAttack()), 0, 0));

			}
		}
		// 神器
		int ctBattleScore = heroManager.caculateBattleScore(killEquipManager.getAllProperty(player)) * player.getEmbattleList().size();

		// 爵位加成
		int titleBattleScore = heroManager.caculateBattleScore(new Property((int) ctManger.getTitleAttack(player).getAttack(), (int) ctManger.getTitleAttack(player).getDefence(), (int) ctManger.getTitleAttack(player).getSoldierNum()));
		// 上阵英雄
		int basecaBattleScore = player.getMaxScore() - equipBattleScore - ctBattleScore - titleBattleScore - tecBattleScore - buildingScore;
		// 依次判断对应类型的应该开启到什么等级
		// 建筑
		checkOpenBouns(player, 1, buildingScore);
		// 英雄
		checkOpenBouns(player, 2, basecaBattleScore);
		// 装备
		checkOpenBouns(player, 3, equipBattleScore);
		// 神器
		checkOpenBouns(player, 4, ctBattleScore);
	}

	private void checkOpenBouns(Player player, int type, int score) {
		List<StaticTowerWarBonus> list = staticTDMgr.getTowerWarBonusMap().values().stream().filter(e -> e.getType() == type).collect(Collectors.toList());
		if (list.size() == 0) {
			return;
		}
		list = list.stream().sorted(Comparator.comparingInt(StaticTowerWarBonus::getLevel)).collect(Collectors.toList());
		StaticTowerWarBonus bonus = null;
		for (StaticTowerWarBonus t : list) {
			if (score >= t.getValve()) {
				bonus = t;
			}
		}
		if (bonus != null) {
			Integer oldBound = player.getTdBouns().get(type);
			if (oldBound == null) {
				player.getTdBouns().put(type, bonus.getId());
			} else {
				StaticTowerWarBonus old = staticTDMgr.getTowerWarBonusMap(oldBound);
				if (old != null) {
					if (old.getValve() < bonus.getValve()) {
						player.getTdBouns().put(type, bonus.getId());
					}
				}
			}
		}

		// 无尽塔防战力加成更新
		List<StaticTowerWarBonus> collect1 = staticTDMgr.getEndlessTowerWarBonusMap().values().stream().filter(e -> e.getType() == type).collect(Collectors.toList());
		if (collect1.isEmpty()) {
			return;
		}
		collect1 = collect1.stream().sorted(Comparator.comparingInt(StaticTowerWarBonus::getLevel)).collect(Collectors.toList());
		StaticTowerWarBonus endlessBonus = null;
		for (StaticTowerWarBonus t : collect1) {
			if (score >= t.getValve()) {
				endlessBonus = t;
			}
		}
		Map<Integer, Integer> endlessTDBonus = getEndlessTDInfo(player).getEndlessTDBonus();
		if (endlessBonus != null) {
			Integer oldBound = endlessTDBonus.get(type);
			if (oldBound == null) {
				endlessTDBonus.put(type, endlessBonus.getId());
			} else {
				StaticTowerWarBonus old = staticTDMgr.getEndlessTowerWarBonusMap(oldBound);
				if (old != null) {
					if (old.getValve() < endlessBonus.getValve()) {
						endlessTDBonus.put(type, endlessBonus.getId());
					}
				}
			}
		}
	}

	// 获取玩家排行
	public synchronized EndlessTDRank getPlayerEndlessTDRank(Player player) {
		for (EndlessTDRank endlessTDRank : this.weekEndlessTDRanks) {
			if (endlessTDRank.getLordId() == player.roleId) {
				int weekMaxFraction = player.getEndlessTDInfo().getWeekMaxFraction();
				if (weekMaxFraction > endlessTDRank.getWeekMaxFraction()) {
					//endlessTDRank.setWeekMaxFraction(weekMaxFraction);
					updateWeekEndlessTDRank(player);
				}
				return endlessTDRank;
			}
		}
		return new EndlessTDRank(player);
	}

	// 更新本周排行
	public void updateWeekEndlessTDRank(Player player) {
		synchronized (this.weekEndlessTDRanks) {
			boolean flag = false;
			for (EndlessTDRank e : this.weekEndlessTDRanks) {
				if (e.getLordId() == player.roleId) {
					e.updateScore(player);
					flag = true;
					break;
				}
			}
			if (!flag) {
				EndlessTDRank endlessTDRank = new EndlessTDRank(player);
				if (endlessTDRank.getWeekMaxFraction() <= 0) {
					return;
				}
				this.weekEndlessTDRanks.add(endlessTDRank);
			}
			weekEndlessTDRanks.sort(Comparator.comparing(EndlessTDRank::getWeekMaxFraction).reversed());
			for (int i = 0; i < weekEndlessTDRanks.size(); i++) {
				weekEndlessTDRanks.get(i).setRank(i + 1);
			}
		}
	}

	// 添加历史排行
	public void addHistoryRank(List<EndlessTDRank> ranks) {
		if (ranks == null) {
			return;
		}
		Set<Integer> keySet = this.historyEndlessTDRanks.keySet();
		if (keySet.size() >= 3) {
			List<Integer> collect = keySet.stream().sorted(Comparator.comparing(Integer::intValue)).collect(Collectors.toList());
			for (Integer integer : collect) {
				this.historyEndlessTDRanks.remove(integer);
				if (this.historyEndlessTDRanks.size() < 3) {
					break;
				}
			}
		}
		int limit = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.RANK_COUNT).getLimit();
		limit = limit == 0 ? 20 : limit;
		limit = ranks.size() > limit ? limit : ranks.size();
		this.historyEndlessTDRanks.put(GameServer.getInstance().currentDay, Lists.newArrayList(ranks.subList(0, limit)));
		this.historyBuilders.clear();
	}

	// 缓存历史排行
	private List<TDRankInfo> historyBuilders = Lists.newArrayList();

	// 获取历史排行
	public List<TDRankInfo> getHistoryRankBuilders() {
		if (!this.historyBuilders.isEmpty()) {
			return this.historyBuilders;
		}
		List<Integer> historyKey = this.historyEndlessTDRanks.keySet().stream().sorted(Comparator.comparing(Integer::intValue).reversed()).collect(Collectors.toList());
		historyKey.forEach(e -> {
			TDRankInfo.Builder builder = TDRankInfo.newBuilder();
			builder.setRankdate(String.valueOf(e));
			List<EndlessTDRank> list = this.historyEndlessTDRanks.get(e);
			list.forEach(x -> {
				Builder b = x.wrapTDRank();
				b.setScore(x.getWeekMaxFraction());
				builder.addTdRank(b);

			});
			this.historyBuilders.add(builder.build());
		});
		return this.historyBuilders;
	}

	// 获取兑换商店物品列表
	public List<EndlessTDShopGoods> getConvertShopList(Player player) {
		if (player == null) {
			return null;
		}
		Map<Integer, Integer> convertShopInfo = getEndlessTDInfo(player).getConvertShopInfo();
		List<EndlessTDShopGoods> list = new ArrayList<>();
		staticTDMgr.getEndlessShopMap().forEach((k, v) -> {
			EndlessTDShopGoods.Builder b = EndlessTDShopGoods.newBuilder();
			b.setPos(k);
			List<List<Integer>> prop = v.getProp();
			if (prop == null || prop.isEmpty()) {
				return;
			}
			prop.forEach(e -> {
				if (e != null && e.size() == 3) {
					b.setAward(new Award(e.get(0), e.get(1), e.get(2)).wrapPb());
				}
			});
			Integer times = convertShopInfo.computeIfAbsent(v.getId(), x -> 0);
			b.setBuyTimes(v.getBuy_time() - times);
			b.setPrice(v.getCoin_price());
			b.setDiscount(0);
			list.add(b.build());
		});
		return list;
	}

	// 获取军械商店物品列表
	public List<EndlessTDShopGoods> getArmoryShopList(Player player) {
		if (player == null) {
			return null;
		}
		EndlessTDInfo endlessTDInfo = getEndlessTDInfo(player);
		Map<Integer, Integer> armoryShop = endlessTDInfo.getArmoryShop();
		if (armoryShop.isEmpty()) {
			refreshArmoryShop(player);
			endlessTDInfo.getArmoryShopInfo().clear();
		}
		Map<Integer, Integer> armoryShopInfo = endlessTDInfo.getArmoryShopInfo();
		List<EndlessTDShopGoods> list = new ArrayList<>();
		armoryShop.forEach((key, value) -> {
			EndlessTDShopGoods.Builder b = EndlessTDShopGoods.newBuilder();
			b.setPos(key);
			StaticEndlessArmory endlessArmory = staticTDMgr.getEndlessArmory(key);
			if (endlessArmory == null) {
				return;
			}
			List<List<Integer>> prop = endlessArmory.getProp();
			if (prop == null || prop.isEmpty()) {
				return;
			}
			prop.forEach(e -> {
				if (e != null && e.size() == 3) {
					b.setAward(new Award(e.get(0), e.get(1), e.get(2)).wrapPb());
				}
			});
			Integer times = armoryShopInfo.computeIfAbsent(endlessArmory.getId(), x -> 0);
			b.setBuyTimes(endlessArmory.getBuy_time() - times);
			b.setPrice(endlessArmory.getPrice());
			b.setDiscount(value);
			list.add(b.build());
		});
		return list.stream().sorted(Comparator.comparing(EndlessTDShopGoods::getDiscount).reversed()).collect(Collectors.toList());
	}

	// 刷新军械商店
	public void refreshArmoryShop(Player player) {
		List<Integer> quantity = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.ENDLESS_ARMORY_SHOP_QUANTITY).getParam();
		if (quantity == null || quantity.isEmpty()) {
			return;
		}
		int total = quantity.get(0);
		int discount = quantity.get(1);
		EndlessTDInfo endlessTDInfo = getEndlessTDInfo(player);
		Map<Integer, Integer> armoryShop = endlessTDInfo.getArmoryShop();
		armoryShop.clear();
		ArrayList<StaticEndlessArmory> staticEndlessArmories = Lists.newArrayList(staticTDMgr.getEndlessArmoryMap().values());
		Collections.shuffle(staticEndlessArmories);
		for (int i = 0; i < discount; i++) {
			if (staticEndlessArmories.isEmpty() || armoryShop.size() >= total) {
				break;
			}
			StaticEndlessArmory staticEndlessArmory = staticEndlessArmories.get(0);
			armoryShop.put(staticEndlessArmory.getId(), staticEndlessArmory.getGoodsDiscount());
			staticEndlessArmories.remove(0);
		}
		while (armoryShop.size() < total && !staticEndlessArmories.isEmpty()) {
			StaticEndlessArmory staticEndlessArmory = staticEndlessArmories.get(0);
			armoryShop.put(staticEndlessArmory.getId(), 0);
			staticEndlessArmories.remove(0);
		}
	}

	// 获取军械商店刷新消耗钻石的数量 type 1 :军械商店
	public int getShopRefreshConsume(Player player) {
		EndlessTDInfo endlessTDInfo = getEndlessTDInfo(player);
		Map<Integer, Integer> refreshShopTimes = endlessTDInfo.getRefreshShopTimes();
		int times = refreshShopTimes.computeIfAbsent(1, x -> 0);
		List<Integer> param = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.ENDLESS_ARMORY_SHOP_PRICE).getParam();
		int consume = param.get(0);
		int increment = param.get(1);
		return consume + (times * increment);
	}

	// 是否有折扣商品
	public int getDiscountGoods(Player player) {
		EndlessTDInfo endlessTDInfo = getEndlessTDInfo(player);
		Map<Integer, Integer> armoryShop = endlessTDInfo.getArmoryShop();
		Map<Integer, Integer> armoryShopInfo = endlessTDInfo.getArmoryShopInfo();
		List<Integer> list = new ArrayList<>();
		for (Entry<Integer, Integer> entry : armoryShop.entrySet()) {
			if (entry.getValue() == 0) {
				continue;
			}
			StaticEndlessArmory endlessArmory = staticTDMgr.getEndlessArmory(entry.getKey());
			if (endlessArmory != null && armoryShopInfo.computeIfAbsent(entry.getKey(), x -> 0) < endlessArmory.getBuy_time()) {
				list.add(entry.getValue());
			}
		}
		if (list.isEmpty()) {
			return 0;
		}
		return list.stream().sorted(Comparator.comparing(Integer::intValue).reversed()).collect(Collectors.toList()).get(0);
	}

	// 获取玩家无尽塔防信息
	public EndlessTDInfo getEndlessTDInfo(Player player) {
		return player.getEndlessTDInfo();
	}

	// 获取玩家无尽塔防游戏信息
	public EndlessTDGameInfo getGameInfo(Player player) {
		return player.getEndlessTDInfo().getGameInfo();
	}

	// 获取排行奖励
	public List<Award> getRankAwards(int rank) {
		List<Award> list = new ArrayList<>();
		staticTDMgr.getEndlessAwardList().stream().sorted(Comparator.comparing(StaticEndlessAward::getRankdown)).forEach(e -> {
			if (rank >= e.getRankup() && rank <= e.getRankdown()) {
				e.getAward().forEach(x -> {
					if (x.size() == 3) {
						list.add(new Award(x.get(0), x.get(1), x.get(2)));
					}
				});
			}
		});
		return list;
	}

	// 设置新关卡的数据
	public void putGameInfo(Player player) {
		StaticEndlessBaseinfo endlessBaseInfo = staticTDMgr.getEndlessBaseInfo();
		EndlessTDGameInfo gameInfo = getGameInfo(player);
		gameInfo.init();
		// 设置初始波次
		gameInfo.setWave(1);
		gameInfo.setLevelId(1);
		// 设置初始资源
		gameInfo.setSupplies(endlessBaseInfo.getBase_supplies());
		// 设置初始血量
		gameInfo.setLifePoint(endlessBaseInfo.getLife_point());
		Map<Integer, EndlessTDTowerRecord> towerPosRecord = gameInfo.getTowerPosRecord();
		towerPosRecord.clear();
		for (int i = 1; i <= endlessBaseInfo.getTower_base_list().size(); i++) {
			EndlessTDTowerRecord endlessTDTowerRecord = new EndlessTDTowerRecord(i);
			towerPosRecord.put(endlessTDTowerRecord.getPos(), endlessTDTowerRecord);
		}
	}

	// 获取游戏数据信息
	public PlayEndlessTDRs.Builder getPlayEndlessTDRs(Player player) {
		PlayEndlessTDRs.Builder builder = PlayEndlessTDRs.newBuilder();
		builder.setBase(getBaseBuilder(player));
		StaticEndlessBaseinfo endlessBaseInfo = staticTDMgr.getEndlessBaseInfo();
		// 可建造防御塔信息,id 等级
		endlessBaseInfo.getTower_list().forEach(e -> {
			if (e.size() != 2) {
				return;
			}
			TwoInt.Builder towerMax = TwoInt.newBuilder();
			towerMax.setV1(e.get(0)).setV2(e.get(1));
			builder.addTowerMax(towerMax);
		});
		// 关卡中防御塔基座的位点坐标的集合
		for (int i = 1; i <= endlessBaseInfo.getTower_base_list().size(); i++) {
			List<Double> e = endlessBaseInfo.getTower_base_list().get(i - 1);
			if (e.size() != 2) {
				continue;
			}
			EndlessTDTowerPos.Builder b = EndlessTDTowerPos.newBuilder();
			b.setPos(i);
			TwoDouble.Builder v = TwoDouble.newBuilder();
			v.setXPos(e.get(0)).setYPos(e.get(1));
			b.addTowerPos(v);
			builder.addTowerPosList(b);
		}
		// 关卡中路径中转点的坐标集合
		endlessBaseInfo.getWay_point_list().forEach(e -> {
			if (e.size() != 2) {
				return;
			}
			TwoDouble.Builder b = TwoDouble.newBuilder();
			b.setXPos(e.get(0)).setYPos(e.get(1));
			builder.addWayPointList(b);
		});
		// 摄像移动范围
		endlessBaseInfo.getCamera_limit().forEach(e -> {
			builder.addCameraLimit(e);
		});
		// 相机的初始位置
		endlessBaseInfo.getCamera_init().forEach(e -> {
			builder.addCameraInit(e);
		});
		EndlessTDGameInfo gameInfo = getGameInfo(player);
		// 玩家在哪一状态 0:新的游戏 1:选择奖励 2：继续游戏
		int state = 0;
		if (gameInfo.getWave() == 0) {
			state = 0;
		} else if (!gameInfo.getAwardItems().isEmpty()) {
			// 玩家的奖励道具信息
			gameInfo.getAwardItems().forEach((k, v) -> {
				Prop.Builder prop = Prop.newBuilder();
				prop.setPropId(k);
				prop.setPropNum(v);
				builder.addAward(prop);
			});
			state = 1;
		} else {
			state = 2;
		}
		builder.setState(state);
		return builder;
	}

	public EndlessTDGameBase.Builder getBaseBuilder(Player player) {
		StaticEndlessBaseinfo endlessBaseInfo = staticTDMgr.getEndlessBaseInfo();
		EndlessTDGameInfo gameInfo = getGameInfo(player);
		Integer addLv = gameInfo.getLevelProps().get(289);
		addLv = addLv == null ? 0 : addLv;
		for (EndlessTDTowerRecord e : gameInfo.getTowerPosRecord().values()) {
			if (e.isExist()) {
				e.setTempLv(0);
				List<Integer> list = endlessBaseInfo.getTower_list().stream().filter(v -> v.get(0) == e.getTowerType()).findFirst().orElse(null);
				if (list == null) {
					continue;
				} else if (e.getLv() >= list.get(1)) {
					// 炮塔等级大于最大等级 取最大等级
					e.setLv(list.get(1));
				} else {
					// 保持炮塔等级加临时等级不超过炮塔的最大的等级
					e.setTempLv((e.getLv() + addLv) <= list.get(1) ? addLv : list.get(1) - e.getLv());
				}
			}
		}
		EndlessTDGameBase.Builder builder = gameInfo.wrapPb();
		StaticEndlessLevel endlessLevel = staticTDMgr.getEndlessLevel(gameInfo.getLevelId());
		gameInfo.getMonsterMap().clear();
		Map<Integer, Integer> monsterMap = gameInfo.getMonsterMap();
		if (endlessLevel != null) {
			// 波次信息
			List<Entry<Integer, Map<Integer, List<Integer>>>> wave_list = endlessLevel.getWave_list().entrySet().stream().sorted(Comparator.comparing(e -> e.getKey())).collect(Collectors.toList());
			wave_list.forEach((w) -> {
				WaveList.Builder waveList = WaveList.newBuilder();
				waveList.setTriggerTime(w.getKey());
				w.getValue().forEach((e, f) -> {
					WaveInfo.Builder waveInfo = WaveInfo.newBuilder();
					// 怪物出口位置
					Integer route = gameInfo.getWaveRoute().get(e);
					if (route == null) {
						int random = f.get(RandomUtil.getRandomNumber(f.size()));
						waveInfo.setRoute(random);
						gameInfo.getWaveRoute().put(e, random);
					} else {
						waveInfo.setRoute(route);
					}
					StaticTowerWarWave tdEndlessWave = staticTDMgr.getTdEndlessWave(e);
					tdEndlessWave.getMonster_list().forEach(x -> {
						IntDouble.Builder monster = IntDouble.newBuilder();
						monster.setV1(x.get(1).intValue());
						Integer quick = gameInfo.getLevelProps().get(288);
						if (quick != null && quick > 0) {
							monster.setV2(0);
						} else {
							monster.setV2(x.get(0));
						}
						waveInfo.addMonster(monster);
						monsterMap.put(monster.getV1(), monsterMap.getOrDefault(monster.getV1(), 0) + 1);
					});

					waveList.addWaveInfo(waveInfo);
				});
				builder.addWaveList(waveList);
			});
			// 路线索引
			endlessLevel.getWay_list().entrySet().stream().sorted(Comparator.comparing(e -> e.getKey())).forEach(e -> {
				WayList.Builder wayList = WayList.newBuilder();
				wayList.setIndex(e.getKey());
				wayList.addAllPos(e.getValue());
				builder.addWayList(wayList);
			});
			// 怪物信息
			int wave = gameInfo.getWave();
			monsterMap.keySet().forEach(e -> {
				StaticTowerWarMonster monster = staticTDMgr.getTdEndlessMonster(e);
				if (monster == null) {
					LogHelper.CONFIG_LOGGER.debug("StaticTowerWarMonster  is  null  monsterId=[{}]", e);
					return;
				}
				Monster.Builder monsterInfo = monster.wrapPb();
				int hp = monsterInfo.getHp();
				int total = 0;
				int endWave = wave - 1;
				for (List<Integer> list : endlessBaseInfo.getLevel_zergbuff()) {
					if (list.size() != 3) {
						continue;
					}
					int start = list.get(0);
					int end = list.get(1);
					double increase = Double.valueOf(list.get(2)) / 100;
					if (endWave < start) {
						continue;
					} else if (endWave >= end) {
						total += (int) Math.ceil(increase * hp * (end - start + 1));
					} else {
						total += (int) Math.ceil(increase * hp * (endWave - start + 1));
					}
				}
				monsterInfo.setHp(hp + total);
				builder.addMonster(monsterInfo);
			});
		}
		return builder;
	}

	// 无尽塔防获得道具 type 0:扣除 1:添加
	public void obtainProp(Player player, int proId, int type) {
		StaticEndlessItem endlessItem = staticTDMgr.getEndlessItem(proId);
		if (endlessItem == null) {
			return;
		}
		EndlessTDGameInfo gameInfo = getGameInfo(player);
		List<Integer> limit = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.SERVER_LOGIC_LIST).getParam();
		List<Integer> limitOnce = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.ONCE_LEVEL_EFFECT).getParam();
		// 需要后端处理的道具列表
		if (limit.contains(proId)) {
			// 被动道具只在当前关卡生效
			if (limitOnce.contains(proId)) {
				Map<Integer, Integer> levelProps = gameInfo.getLevelProps();
				levelProps.put(proId, levelProps.getOrDefault(proId, 0) + 1);
			}
		} else if (endlessItem.getTdType() == TdEndlessItemType.disposableProps) {
			Map<Integer, Integer> itemMap = gameInfo.getItemMap();
			// 一次性道具
			if (type == 0) {
				int count = itemMap.getOrDefault(proId, 1) - 1;
				if (count <= 0) {
					itemMap.remove(proId);
				} else {
					itemMap.put(proId, count);
				}
			} else {
				itemMap.put(proId, itemMap.getOrDefault(proId, 0) + 1);
			}
		} else {
			Map<Integer, Integer> takeEffect = gameInfo.getTakeEffect();
			takeEffect.put(proId, takeEffect.getOrDefault(proId, 0) + 1);
		}
		addBuff(player, endlessItem);
	}

	public void addBuff(Player player, StaticEndlessItem endlessItem) {
		EndlessTDGameInfo gameInfo = getGameInfo(player);
		if (endlessItem == null || endlessItem.getEffect() == null) {
			return;
		}
		endlessItem.getEffect().forEach(e -> {
			if (e.size() != 2) {
				return;
			}
			int type = e.get(0);
			int value = e.get(1);
			switch (type) {
			case EndlessTDItemEffectType.type_1:
			case EndlessTDItemEffectType.type_4:
				break;
			case EndlessTDItemEffectType.type_5:
				break;
			case EndlessTDItemEffectType.type_18:
				gameInfo.changeSupplies(value);
				break;
			case EndlessTDItemEffectType.type_19:
				gameInfo.changeLifePoint(value);
				break;
			case EndlessTDItemEffectType.type_23:
			default:
				Map<Integer, Integer> propBuff = gameInfo.getPropBuff();
				int orDefault = propBuff.getOrDefault(type, 0);
				gameInfo.getPropBuff().put(type, orDefault + Math.abs(value));
				break;
			}
		});
	}

	// 检测玩家游戏中是否使用了道具
	public int checkSpecialItem(Player player, int propId) {
		return getGameInfo(player).getTakeEffect().getOrDefault(propId, 0);
	}

	// 下一关
	public void nextLevel(Player player) {
		EndlessTDGameInfo gameInfo = getGameInfo(player);
		gameInfo.setWave(gameInfo.getWave() + 1);
		gameInfo.setLevelId(gameInfo.getLevelId() + 1);
		Map<Integer, StaticEndlessLevel> endlessLevelMap = staticTDMgr.getEndlessLevelMap();
		// 设置真实关卡id
		int levelTotal = endlessLevelMap.size();
		if (gameInfo.getWave() > levelTotal) {
			StaticEndlessBaseinfo endlessBaseInfo = staticTDMgr.getEndlessBaseInfo();
			List<Integer> level_loop = endlessBaseInfo.getLevel_loop();
			int start = level_loop.get(0);
			int end = level_loop.get(1);
			Map<Integer, StaticEndlessLevel> levelIdMap = endlessLevelMap.values().stream().filter(e -> {
				return e.getId() >= start && e.getId() <= end;
			}).collect(Collectors.toMap(StaticEndlessLevel::getId, StaticEndlessLevel -> StaticEndlessLevel));
			ArrayList<Integer> levelIdList = Lists.newArrayList(levelIdMap.keySet());
			if (levelIdList.isEmpty()) {
				return;
			}
			// 1:随机 0:顺序
			int rule = level_loop.get(2);
			if (rule == 0) {
				Collections.sort(levelIdList);
				if (levelIdMap.get(gameInfo.getLevelId()) == null) {
					gameInfo.setLevelId(levelIdList.get(0));
				}
			} else {
				Collections.shuffle(levelIdList);
				gameInfo.setLevelId(levelIdList.get(0));
			}
		}
		gameInfo.getWaveRoute().clear();
	}

	// 无尽塔防每周五24点定榜
	public void updateEndlessTDRank() {
		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		playerManager.getPlayers().values().forEach(player -> {
			if (player == null) {
				return;
			}
			EndlessTDInfo endlessTDInfo = getEndlessTDInfo(player);
			endlessTDInfo.setLastWeekRank(0);
			endlessTDInfo.setLastWeekFraction(0);
			endlessTDInfo.setRankAward(false);
			if (endlessTDInfo.getWeekMaxFraction() <= 0) {
				endlessTDInfo.setWeekMaxFraction(0);
				return;
			}
			endlessTDInfo.setLastWeekFraction(endlessTDInfo.getWeekMaxFraction());
			EndlessTDRank playerEndlessTDRank = getPlayerEndlessTDRank(player);
			endlessTDInfo.setRankAward(!getRankAwards(playerEndlessTDRank.getRank()).isEmpty());
			endlessTDInfo.setLastWeekRank(playerEndlessTDRank.getRank());
			endlessTDInfo.setWeekMaxFraction(0);
		});
		addHistoryRank(getWeekEndlessTDRanks());
		getWeekEndlessTDRanks().clear();
	}

	// 检查无尽模式是否开启
	public boolean checkEndlessTDOpen(Player player) {
		StaticEndlessBaseinfo endlessBaseInfo = staticTDMgr.getEndlessBaseInfo();
		List<Integer> condition = endlessBaseInfo.getCondition();
		if (condition == null || condition.size() != 2) {
			return false;
		}
		// 1:指挥中心等 2:经典模式关卡进度
		int limit = condition.get(0);
		int value = condition.get(1);
		switch (limit) {
		case 1:
			TD td = player.getTdMap().values().stream().filter(e -> e.getLevelId() == value).findFirst().orElse(null);
			return (td != null && td.getState() != 0) ? true : false;
		case 2:
			return player.getCommand().getLv() >= value;
		default:
			break;
		}
		return false;
	}

	// 校验前端的分数
	public boolean checkFraction(Player player, int fraction, int newLifePoint) {
		EndlessTDGameInfo gameInfo = getGameInfo(player);
		int oldFraction = gameInfo.getFraction();
		int lifePoint = gameInfo.getLifePoint();
		if (newLifePoint < lifePoint) {
			// 剩余剩余血量
			gameInfo.setLifePoint(newLifePoint);
		}
		int count = 0;
		for (Entry<Integer, Integer> entry : gameInfo.getMonsterMap().entrySet()) {
			StaticTowerWarMonster monster = staticTDMgr.getTdEndlessMonster(entry.getKey());
			if (monster == null || monster.getAward() == 0 || entry.getValue() == 0) {
				continue;
			}
			count += monster.getAward() * entry.getValue();
		}
		int current = fraction - oldFraction;
		StaticEndlessLevel endlessLevel = staticTDMgr.getEndlessLevel(gameInfo.getLevelId());
		long limitTime = Long.valueOf(endlessLevel.getTimeLimit());
		// 客户端上报的通关时间
		long actual = gameInfo.getLevelTime().getOrDefault(gameInfo.getWave(), limitTime);
		// 1:分数异常 2:时间异常 3:血量异常 [1,2,3]
		ArrayList<Integer> errorTypeList = Lists.newArrayList();
		if (current > count) {
			errorTypeList.add(1);
		}
		if (actual < limitTime) {
			errorTypeList.add(2);
		}
		if (newLifePoint > lifePoint) {
			errorTypeList.add(3);
		}
		if (!errorTypeList.isEmpty()) {
			gameInfo.getLevelFraction().put(gameInfo.getWave(), gameInfo.getLevelId(), count);
			gameInfo.addFraction(count);
			EndlessTDErrorLog endlessTDErrorLog = new EndlessTDErrorLog(player.roleId, player.getNick(), player.getAccount().getServerId(), gameInfo.getWave(), gameInfo.getLevelId(), current, count, actual, limitTime, newLifePoint, lifePoint, errorTypeList.toString(), gameInfo.getStartDate());
			SpringUtil.getBean(LogUser.class).endlessTDErrorLog(endlessTDErrorLog);
			SpringUtil.getBean(ServerStatisticsJob.class).recordEndlessTDErrorData(endlessTDErrorLog);
			return false;
		}
		gameInfo.getLevelFraction().put(gameInfo.getWave(), gameInfo.getLevelId(), current);
		gameInfo.addFraction(current);
		return true;
	}

}
