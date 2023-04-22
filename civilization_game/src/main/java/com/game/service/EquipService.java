package com.game.service;

import com.alibaba.fastjson.JSONObject;
import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticBuildingMgr;
import com.game.dataMgr.StaticEquipDataMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticSkillMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.Building;
import com.game.domain.p.EmployInfo;
import com.game.domain.p.Employee;
import com.game.domain.p.Equip;
import com.game.domain.p.Hero;
import com.game.domain.p.HeroEquip;
import com.game.domain.p.Item;
import com.game.domain.p.Lord;
import com.game.domain.p.Resource;
import com.game.domain.p.WorkQue;
import com.game.domain.s.StaticEmployee;
import com.game.domain.s.StaticEquip;
import com.game.domain.s.StaticLimit;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticSkill;
import com.game.domain.s.StaticWashSkill;
import com.game.log.LogUser;
import com.game.log.constant.IronOperateType;
import com.game.log.constant.ResOperateType;
import com.game.log.consumer.EventManager;
import com.game.log.domain.EquipDecompoundLog;
import com.game.log.domain.EquipWashLog;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.ActivityManager;
import com.game.manager.BuildingManager;
import com.game.manager.ChatManager;
import com.game.manager.DailyTaskManager;
import com.game.manager.EquipManager;
import com.game.manager.HeroManager;
import com.game.manager.LootManager;
import com.game.manager.PlayerManager;
import com.game.manager.TaskManager;
import com.game.manager.TechManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.EquipPb;
import com.game.pb.EquipPb.BuyEquipSlotRs;
import com.game.pb.EquipPb.DecompoundEquipRs;
import com.game.pb.EquipPb.GetEquipBagRs;
import com.game.pb.EquipPb.TakeOffEquipRs;
import com.game.pb.EquipPb.WashEquipItemRs;
import com.game.pb.EquipPb.WashHeroEquipRs;
import com.game.pb.EquipPb.WearEquipRs;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EquipService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticEquipDataMgr staticEquipDataMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticSkillMgr staticSkillDataMgr;

	@Autowired
	private HeroManager heroDataManager;

	@Autowired
	private StaticBuildingMgr staticBuildingMgr;

	@Autowired
	private BuildingManager buildingManager;

	@Autowired
	private LootManager lootManager;

	@Autowired
	private EquipManager equipManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private StaticPropMgr staticPropMgr;

	@Autowired
	private TechManager techManager;

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private DailyTaskManager dailyTaskManager;

	@Autowired
	private EventManager eventManager;

	@Autowired
	AchievementService achievementService;
	@Autowired
	ActivityEventManager activityEventManager;
	/**
	 * Function:获取装备背包请求
	 *
	 * @param handler
	 */
	public void getEquipBagRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Map<Integer, Equip> equips = player.getEquips();
		GetEquipBagRs.Builder builder = GetEquipBagRs.newBuilder();
		for (Map.Entry<Integer, Equip> item : equips.entrySet()) {
			if (item == null) {
				continue;
			}
			Equip equip = item.getValue();
			if (equip == null) {
				continue;
			}
			builder.addEquipItem(equip.wrapPb());
		}

		handler.sendMsgToPlayer(GameError.OK, EquipPb.GetEquipBagRs.ext, builder.build());
	}

	/**
	 * Function:装备分解请求(走物品)
	 *
	 * @param handler
	 */
	public void decompoundEquipRq(EquipPb.DecompoundEquipRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 检查物品是否存在
		List<Integer> keyIdList = req.getKeyIdList();
		Map<Integer, Equip> equips = player.getEquips();
		List<Award> lootList = new ArrayList<>();
		DecompoundEquipRs.Builder builder = DecompoundEquipRs.newBuilder();
		HashBasedTable<Integer, Integer, Award> hashBasedTable = HashBasedTable.create();
		keyIdList.forEach(keyId -> {
			// 装备不存在
			Equip equip = equips.get(keyId);
			if (equip == null) {
				handler.sendErrorMsgToPlayer(GameError.NO_EQUIP);
				return;
			}
			// 找到装备
			int equipId = equip.getEquipId();
			// 查找配置
			StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(equipId);
			if (staticEquip == null) {
				handler.sendErrorMsgToPlayer(GameError.NO_EQUIP_CONFIG);
				return;
			}
			// 是否开启VIP装备分解
			List<List<Long>> decompose = staticEquip.getDecompose();
			lootManager.lootItem(decompose, hashBasedTable);
			// 图纸翻倍
			if (player.getLord().getVipEquip() == 1) {
				int random = RandomHelper.randomInSize(100);
				if (random <= 50) {
					for (List<Long> item : staticEquip.getCompose()) {
						int type = item.get(0).intValue();
						if (type != AwardType.PROP) {
							continue;
						}

						int id = item.get(1).intValue();
						StaticProp staticProp = staticPropMgr.getStaticProp(id);
						if (staticProp == null) {
							continue;
						}
						if (staticProp.getPropType() != ItemType.EQUIP_PAPER) {
							continue;
						}
						playerManager.addAward(player, type, id, item.get(2), Reason.DECOMPOUSE_EQUIP);
						Award award = new Award(type, id, item.get(2).intValue());
						builder.addAward(award.wrapPb());
						break;
					}
				}
			}
			equips.remove(keyId);
			builder.addKeyId(keyId);
			/**
			 * 装备分解日志埋点
			 */
			EquipDecompoundLog log = EquipDecompoundLog.builder().roleId(player.roleId).roleName(player.getNick()).roleLv(player.getLevel()).title(player.getTitle()).country(player.getCountry()).vip(player.getVip()).equipId(equipId).quality(staticEquip.getQuality()).roleCreateTime(player.account.getCreateDate()).channel(player.account.getChannel()).keyId(keyId).decompose(true).reason(0).build();
			SpringUtil.getBean(LogUser.class).equipDecompoundLog(log);

			List param = Lists.newArrayList(equip.getEquipId(), staticEquip.getEquipId(), staticEquip.getLordLv(), staticEquip.getQuality());
			SpringUtil.getBean(EventManager.class).equip_break(player, param);
		});
		hashBasedTable.values().forEach(x -> {
			playerManager.addAward(player, x.getType(), x.getId(), x.getCount(), Reason.DECOMPOUSE_EQUIP);
			builder.addAward(x.wrapPb());
		});
		handler.sendMsgToPlayer(GameError.OK, EquipPb.DecompoundEquipRs.ext, builder.build());
	}

	/**
	 * Function:装备背包格子购买请求
	 *
	 * @param handler
	 */
	public void buyEquipSlotRq(EquipPb.BuyEquipSlotRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		// 是否达到最大购买次数
		int maxBuyTimes = staticEquipDataMgr.maxBuyEquipSlotTimes();
		Lord lord = player.getLord();
		int currentBuyTimes = lord.getBuyEquipSlotTimes();
		// 达到最大购买次数
		if (currentBuyTimes >= maxBuyTimes) {
			LogHelper.CONFIG_LOGGER.error("reach max buy times, current times = " + currentBuyTimes + ", max times =" + maxBuyTimes);
			handler.sendErrorMsgToPlayer(GameError.MAX_EQUIP_BUY_TIMES);
			return;
		}

		int buyTimes = currentBuyTimes + 1;
		int price = staticEquipDataMgr.getBuySlotPrice(buyTimes);
		if (price <= 0) {
//			LogHelper.MESSAGE_LOGGER.error("buy equip slot price error! buy times:{}", buyTimes);
			handler.sendErrorMsgToPlayer(GameError.BUY_EQUIP_SLOT_PRICE_ERROR);
			return;
		}

		// 检查玩家元宝够不
		int ownGold = lord.getGold();
		if (price > ownGold) {
//			LogHelper.MESSAGE_LOGGER.error("not enought gold, owned gold = " + ownGold + ", need gold = " + price);
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// action
		playerManager.subGold(player, price, Reason.BUY_EQUIP_SLOP);
		lord.setBuyEquipSlotTimes(lord.getBuyEquipSlotTimes() + 1);

		// msg
		BuyEquipSlotRs.Builder builder = BuyEquipSlotRs.newBuilder();
		builder.setGold(lord.getGold());
		builder.setEquipBuyTimes(lord.getBuyEquipSlotTimes());
		handler.sendMsgToPlayer(GameError.OK, EquipPb.BuyEquipSlotRs.ext, builder.build());
	}

	/**
	 * Function:英雄穿装备 从装备背包删除一个装备，英雄身上产生一个装备
	 *
	 * @param handler
	 */
	public void wearEquipRq(EquipPb.WearEquipRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int heroId = req.getHeroId();
		Map<Integer, Hero> heros = player.getHeros();
		if (!heros.containsKey(heroId)) {
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}

		Hero hero = heros.get(heroId);

		// 检查物品是否存在
		int keyId = req.getKeyId();
		Map<Integer, Equip> equips = player.getEquips();
		if (!equips.containsKey(keyId)) {
			handler.sendErrorMsgToPlayer(GameError.NO_EQUIP);
			return;
		}

		// 装备不存在
		Equip equip = equips.get(keyId);

		// 获取装备类型
		int equipId = equip.getEquipId();
		int equipType = staticEquipDataMgr.getEquipType(equipId);
		if (equipType == Integer.MAX_VALUE) {
			LogHelper.CONFIG_LOGGER.error("no this equip type, not in [1~6], equipId = " + equipId);
			handler.sendErrorMsgToPlayer(GameError.NO_EQUIP_TYPE);
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

		// 检查有无装备
		HeroEquip heroEquip = heroDataManager.getEquip(hero, equipType);
		WearEquipRs.Builder builder = WearEquipRs.newBuilder();
		// 删除装备
		if (heroEquip != null) {
			builder.setRemoveEquipItemId(equipId);
			builder.setRemoveHeroEquipId(heroEquip.getEquip().getEquipId());
			// 克隆一份
			Equip equipClone = equip.cloneInfo();

			// 生成一个新的英雄装备
			HeroEquip newHeroEqup = new HeroEquip();
			newHeroEqup.setPos(equipType);
			newHeroEqup.setEquip(equipClone);

			// 生成一个新的背包装备
			Equip newEquip = new Equip();
			newEquip.copyData(heroEquip.getEquip());
			// remove and add
			hero.removeEquip(equipType);
			hero.addHeroEquip(newHeroEqup);

			// remove and add
			equips.remove(equip.getKeyId());
			equips.put(newEquip.getKeyId(), newEquip);
			handleWearTask(player, heroId, equipClone.getEquipId());
			doAllHeroWearHat(player, equipClone.getEquipId());
			doAllHeroWearTwo(player);
			builder.setAddEquipItem(newEquip.wrapPb());
			builder.setAddHeroEquip(newHeroEqup.wrapPb());
			builder.setHeroId(heroId);
			builder.setHasEquip(true);
		} else {
			// 增加英雄身上装备
			heroEquip = new HeroEquip();
			heroEquip.setPos(equipType);
			heroEquip.setEquip(equip.cloneInfo());
			hero.addHeroEquip(heroEquip);
			builder.setAddHeroEquip(heroEquip.wrapPb());
			handleWearTask(player, heroId, equip.getEquipId());
			doAllHeroWearHat(player, equip.getEquipId());
			doAllHeroWearTwo(player);
			// 减少背包装备
			builder.setRemoveEquipItemId(equip.getKeyId());
			equips.remove(equip.getKeyId());
			builder.setHeroId(heroId);
			builder.setHasEquip(false);
		}

		heroDataManager.caculateProp(hero, player);
		builder.setHeroProperty(hero.getTotalProp().wrapPb());
		handler.sendMsgToPlayer(GameError.OK, EquipPb.WearEquipRs.ext, builder.build());
		heroDataManager.updateHero(player, hero, Reason.WEAR_EQUIP);
		heroDataManager.synBattleScoreAndHeroList(player, hero);
		SpringUtil.getBean(EventManager.class).equip_wear(player, Lists.newArrayList(
			hero.getHeroId(),
			heroDataManager.getHeroName(heroId),
			JSONObject.toJSON(heroEquip)
		));
	}

	/**
	 * Function:英雄脱装备请求 从英雄身上删除一个装备，放到背包
	 *
	 * @param handler
	 */
	public void takeOffEquipRq(EquipPb.TakeOffEquipRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Map<Integer, Hero> heros = player.getHeros();
		int heroId = req.getHeroId();
		// 有无英雄
		Hero hero = heros.get(heroId);
		if (hero == null) {
			LogHelper.CONFIG_LOGGER.error("hero not exists, id = " + heroId);
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}

		int keyId = req.getKeyId();
		// 检查有无装备
		HeroEquip heroEquip = hero.getEquipByUId(keyId);
		if (heroEquip == null) {
			LogHelper.CONFIG_LOGGER.error("hero equip not exists, keyId" + " = " + keyId);
			handler.sendErrorMsgToPlayer(GameError.NO_HERO_EQUIP);
			return;
		}

		// 检查格子数够不够
		int freeSlot = equipManager.getFreeSlot(player);
		Map<Integer, Equip> equips = player.getEquips();
		int equipCount = equips.size();
		if (freeSlot <= 0) {
			LogHelper.CONFIG_LOGGER.error("not enough equip slot, equip slot " + equipCount + ", equip size = " + equips.size());
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_EQUIP_SLOT);
			return;
		}

		Equip equip = heroEquip.getEquip();
		// 获取装备类型
		int equipId = equip.getEquipId();
		int equipType = staticEquipDataMgr.getEquipType(equipId);
		if (equipType == Integer.MAX_VALUE) {
			LogHelper.CONFIG_LOGGER.error("no equip type, type = " + equipType);
			handler.sendErrorMsgToPlayer(GameError.NO_EQUIP_TYPE);
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

		if (player.hasPvpHero(heroId)) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IN_PVP_BATTLE);
			return;
		}

		if (!hero.isActivated()) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IS_NOT_ACTIVATE);
			return;
		}
		// 脱装备
		hero.removeEquip(equipType);
		// 增加背包装备
		Equip equipAdd = equip.cloneInfo();
		equips.put(keyId, equipAdd);
		heroDataManager.caculateProp(hero, player);

		TakeOffEquipRs.Builder builder = TakeOffEquipRs.newBuilder();
		builder.setHeroId(heroId);
		builder.setHeroEquipId(keyId);
		builder.setAddEquipItem(equipAdd.wrapPb());
		builder.setHeroProperty(hero.getTotalProp().wrapPb());

		//heroDataManager.caculateProp(hero, player);
		builder.setHeroProperty(hero.getTotalProp().wrapPb());
		handler.sendMsgToPlayer(GameError.OK, EquipPb.TakeOffEquipRs.ext, builder.build());
		heroDataManager.updateHero(player, hero, Reason.WEAR_EQUIP);
		heroDataManager.synBattleScoreAndHeroList(player, hero);

	}

	/**
	 * Function:英雄身上装备洗练请求 洗练规则：等级只会升不会降，每次升一级，一次升一个
	 *
	 * @param handler
	 */
	public void washHeroEquipRq(EquipPb.WashHeroEquipRq req, ClientHandler handler) {
		// 先判断是否洗练成功，然后再根据结果判断技能是否替换
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Map<Integer, Hero> heros = player.getHeros();
		int heroId = req.getHeroId();
		// 有无英雄
		Hero hero = heros.get(heroId);
		if (hero == null) {
			LogHelper.CONFIG_LOGGER.error("hero not exists, id = " + heroId);
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}

		int keyId = req.getKeyId();
		// 检查有无装备
		HeroEquip heroEquip = hero.getEquipByUId(keyId);
		if (heroEquip == null) {
			LogHelper.CONFIG_LOGGER.error("hero equip not exists, keyId" + " = " + keyId);
			handler.sendErrorMsgToPlayer(GameError.NO_HERO_EQUIP);
			return;
		}

		// 检查洗练类型[洗练回复TODO]
		int washType = req.getWashType();
		if (washType < 1 || washType > 2) {
			LogHelper.CONFIG_LOGGER.error("wash type error = " + washType);
			handler.sendErrorMsgToPlayer(GameError.WASH_TYPE_ERROR);
			return;
		}

		Lord lord = player.getLord();
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		// 开始洗练,获取装备技能
		ArrayList<Integer> skills = heroEquip.getEquipSkill();
		if (skills.size() <= 0) {
			LogHelper.CONFIG_LOGGER.error("this equip has no skills.");
			handler.sendErrorMsgToPlayer(GameError.NO_EQUIP_SKILL);
			return;
		}
		int goldNeed = 0;
		int quality = 0;
		Equip equip = heroEquip.getEquip();
		if (equip != null) {
			quality = equipManager.getQuality(equip.getEquipId());
		}

		if (washType == 1) {
			if (lord.getWashSkillTimes() <= 0) {
				// LogHelper.ERROR_LOGGER.error("not enough wash skill times, washSkill Times =
				// " + lord.getWashSkillTimes());
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_WASH_SKILL_TIMES);
				return;
			}
		} else if (washType == 2) {
			int gold = lord.getGold();
			// 如果含有秘技,则花费为
			boolean isPreOk = isPreOK(player, skills, quality);
			if (isPreOk) {
				goldNeed = staticLimitMgr.getNum(128);
				/**
				 * 装备秘技精研消耗钻石数量日志埋点
				 */
				int expertWashSkillTimes = lord.getExpertWashSkillTimes();
				if (expertWashSkillTimes > 0) {
					goldNeed = 0;
				}
			} else {
				goldNeed = staticLimit.getWashSkillPrice();
			}

			if (goldNeed > gold) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
		}

		handleWashSkill(equip, skills, quality, player, washType, 0);

		heroDataManager.caculateProp(hero, player);

		washEquipTask(player);
		WashHeroEquipRs.Builder builder = WashHeroEquipRs.newBuilder();
		// 扣除次数或者扣钱
		if (washType == 1) {
			lord.setWashSkillTimes(lord.getWashSkillTimes() - 1);
		} else {
			playerManager.subGold(player, goldNeed, Reason.WASH_HERO);
			if (goldNeed == 0) {
				int expertWashSkillTimes = lord.getExpertWashSkillTimes();
				if (expertWashSkillTimes > 0) {
					//lord.setExpertWashSkillTimes(expertWashSkillTimes - 1);
					//扣除免费秘技精研次数
					playerManager.subAward(player, AwardType.LORD_PROPERTY, LordPropertyType.EQUIP_EXPERT_WASH_TIMES, 1, Reason.SUB_EQUIP_EXPERT_WASH_TIMES);
				}
			}
		}

		builder.setWashSkillTimes(lord.getWashSkillTimes());
		builder.setExpertWashSkillTimes(lord.getExpertWashSkillTimes());
		builder.setWashSkillEndTime(lord.getWashSkillEndTime() + staticLimit.getWashSkillInterval() * TimeHelper.SECOND_MS);
		builder.setGold(lord.getGold());
		int i = equip.getEquipId() / 1000;
		if (i > 0) {
			equip.setEquipId(i);
		}
		builder.setEquipId(equip.getEquipId());
		for (Integer skillId : skills) {
			if (skillId == null) {
				continue;
			}
			builder.addSkillId(skillId);
		}
		heroDataManager.synBattleScoreAndHeroList(player, hero);
		// hero property
		handler.sendMsgToPlayer(GameError.OK, EquipPb.WashHeroEquipRs.ext, builder.build());

		heroDataManager.updateHero(player, hero, Reason.WASH_HERO);

		// heroDataManager.caculateProp(hero, player);
		activityManager.updActPerson(player, ActivityConst.ACT_WASH_RANK, 1, 0);

		// TODO 事件触发活动处理
		activityEventManager.activityTip(EventEnum.EQUIP_WASH, player, 1, 0);
//        activityManager.updActData(player, ActivityConst.TYPE_ADD, StaticActEquipUpdate.WASH_CONUT, 1, ActivityConst.ACT_WASH_EQUIP);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.WASH_EQUIP, 1);
//        activityManager.updActPerson(player, ActivityConst.ACT_SQUA, 1, NineCellConst.CELL_3);
//        activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.WASH_EQUPT, 0, 1);

		boolean equipWashFull = equipManager.isEquipWashFull(equip);
		if (equipWashFull) {
			int equipId = equip.getEquipId();
			StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(equipId);
			if (staticEquip != null) {
				player.updateWashEquipNum(keyId, staticEquip.getQuality());
			}
		}

		/**
		 * 装备洗练日志埋点
		 */
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(equip.getEquipId());
		logUser.equipWashLog(new EquipWashLog(player.roleId,
			player.getNick(),
			player.getLevel(),
			player.getTitle(),
			player.getCountry(),
			player.getVip(),
			staticEquip.getEquipId(),
			staticEquip.getQuality(),
			player.getLord().getWashSkillTimes(),
			player.account.getCreateDate(),
			equipWashFull,
			washType,
			keyId,
			player.account.getChannel()));

		//装备洗练
		List param = Lists.newArrayList(
			equip.getEquipId(),
			staticEquip.getEquipId(),
			staticEquip.getLordLv(),
			staticEquip.getQuality(),
			equipWashFull
		);
		if (washType == 2) {
			SpringUtil.getBean(EventManager.class).equip_train(player, param);
		} else {
			SpringUtil.getBean(EventManager.class).equip_research(player, param);
		}
		heroDataManager.caculateProp(hero, player);
		dailyTaskManager.record(DailyTaskId.WASH_EQUIP, player, 1);
	}

	/**
	 * Function:背包装备洗练请求
	 *
	 * @param handler
	 */
	public void washEquipItemRq(EquipPb.WashEquipItemRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int keyId = req.getKeyId();
		Map<Integer, Equip> equips = player.getEquips();

		// 检查有无装备
		Equip equip = equips.get(keyId);
		if (equip == null) {
			LogHelper.CONFIG_LOGGER.error("equip id = " + keyId + " not exists.");
			handler.sendErrorMsgToPlayer(GameError.NO_EQUIP);
			return;
		}

		// 检查洗练类型[洗练回复TODO]
		int washType = req.getWashType();
		if (washType < 1 || washType > 2) {
			LogHelper.CONFIG_LOGGER.error("wash type error = " + washType);
			handler.sendErrorMsgToPlayer(GameError.WASH_TYPE_ERROR);
			return;
		}

		Lord lord = player.getLord();
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		// 开始洗练,获取装备技能
		ArrayList<Integer> skills = equip.getSkills();
		if (skills.size() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_EQUIP_SKILL);
			return;
		}
		int goldNeed = 0;
		int quality = equipManager.getQuality(equip.getEquipId());
		if (washType == 1) {
			if (lord.getWashSkillTimes() <= 0) {
				LogHelper.CONFIG_LOGGER.error("no enough wash skill times, washskill times = " + lord.getWashSkillTimes());
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_WASH_SKILL_TIMES);
				return;
			}
		} else if (washType == 2) {
			int gold = lord.getGold();
			// 如果含有秘技,则花费为200
			boolean isPreOk = isPreOK(player, skills, quality);
			if (isPreOk) {
				goldNeed = staticLimitMgr.getNum(128);

				int expertWashSkillTimes = lord.getExpertWashSkillTimes();
				if (expertWashSkillTimes > 0) {
					goldNeed = 0;
				}
			} else {
				goldNeed = staticLimit.getWashSkillPrice();
			}

			if (goldNeed > gold) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
		}

		handleWashSkill(equip, skills, quality, player, washType, 0);

		washEquipTask(player);
		WashEquipItemRs.Builder builder = WashEquipItemRs.newBuilder();
		// 扣除次数或者扣钱
		if (washType == 1) {
			lord.setWashSkillTimes(lord.getWashSkillTimes() - 1);
		} else {
			playerManager.subGold(player, goldNeed, Reason.WASH_EQUIP);
			if (goldNeed == 0) {
				int expertWashSkillTimes = lord.getExpertWashSkillTimes();
				if (expertWashSkillTimes > 0) {
					lord.setExpertWashSkillTimes(expertWashSkillTimes - 1);
				}
			}
		}

		builder.setWashSkillTimes(lord.getWashSkillTimes());
		builder.setExpertWashSkillTimes(lord.getExpertWashSkillTimes());
		builder.setGold(lord.getGold());

		int i = equip.getEquipId() / 1000;
		if (i > 0) {
			equip.setEquipId(i);
		}
		builder.setEquipId(equip.getEquipId());
		for (Integer skillId : skills) {
			if (skillId == null) {
				continue;
			}
			builder.addSkillId(skillId);
		}
		builder.setWashSkillEndTime(lord.getWashSkillEndTime() + staticLimit.getWashSkillInterval() * TimeHelper.SECOND_MS);
		handler.sendMsgToPlayer(GameError.OK, EquipPb.WashEquipItemRs.ext, builder.build());

		activityEventManager.activityTip(EventEnum.EQUIP_WASH, player, 1, 0);
		activityManager.updActPerson(player, ActivityConst.ACT_WASH_RANK, 1, 0);
//        activityManager.updActData(player, ActivityConst.TYPE_ADD, StaticActEquipUpdate.WASH_CONUT, 1, ActivityConst.ACT_WASH_EQUIP);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.WASH_EQUIP, 1);
//        activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.WASH_EQUPT, 0, 1);

		boolean equipWashFull = equipManager.isEquipWashFull(equip);
		if (equipWashFull) {
			int equipId = equip.getEquipId();
			StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(equipId);
			if (staticEquip != null) {
				player.updateWashEquipNum(keyId, staticEquip.getQuality());
			}
		}

		/**
		 * 装备洗练日志埋点
		 */
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(equip.getEquipId());
		logUser.equipWashLog(new EquipWashLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getCountry(), player.getVip(), staticEquip.getEquipId(), staticEquip.getQuality(), player.getLord().getWashSkillTimes(), player.account.getCreateDate(), equipWashFull, washType, keyId, player.account.getChannel()));

		// 装备洗练
		List param = Lists.newArrayList(staticEquip.getEquipId(), staticEquip.getEquipName(), staticEquip.getLordLv(), staticEquip.getQuality(), equipWashFull);
		SpringUtil.getBean(EventManager.class).equip_research(player, param);
		dailyTaskManager.record(DailyTaskId.WASH_EQUIP, player, 1);
	}

	// to do
	// [秘技洗练 = 开启秘技科技 + 且前面几个技能满级]
	// 技能等级不能超过武器的品质
	public void handleWashSkill(Equip equip, ArrayList<Integer> skills, int quality, Player player, int washType, int count) {
		// 洗练技能等级
		boolean isSpecialOpen = techManager.isSpecialSkillOpen(player);
		if (!isSpecialOpen) {
			while (skills.size() > 3) {
				skills.remove(skills.size() - 1);
			}
		} else {
			if (skills.size() > 3) {
				int totalLevel = 0;
				int maxLevel = 0;
				for (int i = 0; i < skills.size(); i++) {
					Integer skillId = skills.get(i);
					StaticSkill staticSkill = staticSkillDataMgr.getStaticSkill(skillId);
					if (staticSkill == null) {
						LogHelper.CONFIG_LOGGER.error("static skill is null, skillId = " + skillId);
						continue;
					}
					totalLevel += staticSkill.getLevel();
					maxLevel += quality;
				}
				if (totalLevel < maxLevel) {
					while (skills.size() > 3) {
						skills.remove(skills.size() - 1);
					}

				}
			}
		}
		List<Integer> skillLevelList = new ArrayList<Integer>();
		List<Integer> skillTypeList = new ArrayList<Integer>();
		// 先拷贝技能等级和类型
		int totalLevel = 0;
		int maxLevel = 0;
		for (Integer skillId : skills) {
			StaticSkill staticSkill = staticSkillDataMgr.getStaticSkill(skillId);
			if (staticSkill == null) {
				LogHelper.CONFIG_LOGGER.error("static skill is null, skillId = " + skillId);
				continue;
			}
			skillLevelList.add(staticSkill.getLevel());
			totalLevel += staticSkill.getLevel();
			skillTypeList.add(staticSkill.getSkillType());
			maxLevel += quality;
		}

		int nextLevel = totalLevel + 1;
		boolean isAllSkillLvOk = false; // 是否所有技能满级
		StaticWashSkill washSkillConfig = null;
		if (totalLevel >= maxLevel) {
			isAllSkillLvOk = true;
		} else {
			washSkillConfig = staticSkillDataMgr.getWashSkillConfig(washType, quality, nextLevel);
			if (washSkillConfig == null) {
				LogHelper.CONFIG_LOGGER.error("config error, totalLevel = " + nextLevel + ", quality = " + quality);
				return;
			}
		}

		boolean isWashOK = false;
		// 等级没满才能升级等级
		if (washSkillConfig != null) {
			isWashOK = RandomHelper.isSkillLevelUp(washSkillConfig.getRate());
		}

		if (washSkillConfig != null) {
			int washLimit = washSkillConfig.getWashLimit();
			if (washLimit > 0) {
				if (washType == 1) {
					if (equip.getFreeWashTimes() >= washLimit) {
						isWashOK = true;
					}
					equip.setFreeWashTimes(equip.getFreeWashTimes() + 1);
				} else if (washType == 2) {
					if (equip.getGoldWashTimes() >= washLimit) {
						isWashOK = true;
					}
					equip.setGoldWashTimes(equip.getGoldWashTimes() + 1);
				}
			}
		}

		for (int i = 0; i < skillLevelList.size() && isWashOK; i++) {
			Integer skillLv = skillLevelList.get(i);
			if (skillLv == null) {
				continue;
			}

			if (skillLv >= quality) {
				continue;
			}

			skillLevelList.set(i, skillLv + 1);
			break;
		}

		// 洗练之后排序
		Collections.sort(skillLevelList);

		// 3种洗练, 1.普通洗练 2.5金币洗练 3.200金币洗练
		// 这里只洗技能类型
		int washId = 1;
		if (washType == 1) {
			washId = 1;
		} else if (washType == 2 && !isAllSkillLvOk) {
			washId = 2;
		} else if (washType == 2 && isAllSkillLvOk) {
			washId = 3;
		}

		if (skillTypeList.size() == 4) {
			skillTypeList.remove(3);
			skillLevelList.remove(3);
			skills.remove(3);
		}
		boolean flag = false;
		if (!isSpecialOpen) {
			for (int i = 0; i < skillTypeList.size(); i++) {
				int randSkillType = RandomHelper.randSkillType(); // 技能暂时不洗练出命中和闪避
				skillTypeList.set(i, randSkillType);
			}
		} else {
			if (washId == 1 || washId == 2) {
				// 直接先随机前三个技能
				HashSet<Integer> isSameType = new HashSet<Integer>();
				int lastType = 0;
				for (int i = 0; i < skillTypeList.size(); i++) {
					int randSkillType = RandomHelper.randSkillType(); // 技能暂时不洗练出命中和闪避
					skillTypeList.set(i, randSkillType);
					isSameType.add(randSkillType);
					lastType = randSkillType;
				}

				// 检查前3个技能类型是否相同
				if (quality >= Quality.GOLD.get()) {
					if (isSameType.size() == 1 && isWashAllMaxLevel(skillLevelList, quality)) {
						// 新增第4个技能
						if (skillTypeList.size() <= 3) {
							skillTypeList.add(lastType);
							skillLevelList.add(quality);
							flag =true;

						}
					} else {
						// 如果有第4个技能，则去掉第4个技能
						if (skillTypeList.size() == 4) {
							skillLevelList.remove(3);
						}
					}
				}

			} else if (washId == 3) {
				// 只随机技能类型
				int randSkillType = RandomHelper.randSkillType();
				for (int i = 0; i < skillTypeList.size(); i++) {
					skillTypeList.set(i, randSkillType);
				}

				if (quality >= Quality.GOLD.get()) {
					if (skillTypeList.size() <= 3) {
						skillTypeList.add(randSkillType);
						skillLevelList.add(quality);
						flag =true;
					}
				}
			}
		}

		//装备进行秘技改造时，不会与原技能相同。
		for (int i = 0; i < skillLevelList.size() && skills.size() == 4; i++) {
			StaticSkill staticSkill = staticSkillDataMgr.getSkillByLvType(skillLevelList.get(i), skillTypeList.get(i));
			if (staticSkill == null) {
				continue;
			}
			if (i == 3 || washType == 1) {
				break;
			}
			if (skills.get(i) == staticSkill.getSkillId()) {
				if (count < 5) {
					handleWashSkill(equip, skills, quality, player, washType, ++count);
					return;
				}
			}
		}

		skills.clear();

		for (int i = 0; i < skillLevelList.size(); i++) {
			StaticSkill staticSkill = staticSkillDataMgr.getSkillByLvType(skillLevelList.get(i), skillTypeList.get(i));
			if (staticSkill == null) {
				continue;
			}
			skills.add(staticSkill.getSkillId());
		}
		if(flag){
			if(quality==Quality.RED.get()){
				achievementService.addAndUpdate(player,AchiType.AT_12,1);
			}
			if(quality==Quality.PURPLE.get()){
				achievementService.addAndUpdate(player,AchiType.AT_13,1);
			}

		}

	}

	public boolean isWashAllMaxLevel(List<Integer> skillLevelList, int quality) {
		int totalLevel = 0;
		int maxLevel = 0;
		for (Integer skillLevel : skillLevelList) {
			totalLevel += skillLevel;
			maxLevel += quality;
		}
		return totalLevel >= maxLevel;
	}

	public boolean isPreOK(Player player, ArrayList<Integer> skills, int quality) {
		int totalLevel = 0;
		int maxLevel = 0;
		for (Integer skillId : skills) {
			StaticSkill staticSkill = staticSkillDataMgr.getStaticSkill(skillId);
			if (staticSkill == null) {
				LogHelper.CONFIG_LOGGER.error("static skill is null, skillId = " + skillId);
				continue;
			}
			totalLevel += staticSkill.getLevel();
			maxLevel += quality;
		}

		boolean isAllSkillLvOk = false; // 是否所有技能满级
		if (totalLevel >= maxLevel) {
			isAllSkillLvOk = true;
		}

		boolean isSpecialOpen = techManager.isSpecialSkillOpen(player);
		boolean preCond = isAllSkillLvOk && isSpecialOpen && quality >= Quality.GOLD.get();
		return preCond;
	}

	/**
	 * Function:招募武器大师
	 */
	public void hireBlackSmith(EquipPb.HireBlackSmithRq req, ClientHandler handler) {
		// 检测是否有免费次数, 先检查玩家身上有没有这个武器大师
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int employeeId = req.getEmployeeId();
		StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(employeeId);
		if (staticEmployee == null) {
			handler.sendErrorMsgToPlayer(GameError.EMPLOYEE_CONFIG_ERROR);
			return;
		}

		// 检查employeeId的合法性
		int commandLv = staticEmployee.getCommandLv();
		int playerCommandLv = player.getCommandLv();
		// 武器大师要求的司令部等级不足
		if (playerCommandLv < commandLv) {
			handler.sendErrorMsgToPlayer(GameError.COMMAND_LEVEL_LOW_EMPLOYEE);
			return;
		}

		EmployInfo employInfo = player.getEmployInfo();
		if (employInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		Map<Integer, Employee> employeeMap = employInfo.getEmployeeMap();
		Employee employee = employeeMap.get(req.getEmployeeId());
		long now = System.currentTimeMillis();
		// 说明没有招募过这个武器大师
		int freeTimes = staticEmployee.getFreeBuyTimes();
		if (employee == null) {
			employee = buildingManager.createBlackEmployee(employeeId);
			boolean isFreeHire = employee.getUseTimes() < freeTimes;
			if (isFreeHire) { // 免费买
				employee.setEndTime(staticEmployee.getFreeDurationTime() * 1000 + System.currentTimeMillis());
			} else {
				employee.setEndTime(staticEmployee.getDurationTime() * TimeHelper.SECOND_MS + System.currentTimeMillis());
			}
			employeeMap.put(employeeId, employee);

		} else {
			if (employee.getEndTime() > now) {
				// handler.sendErrorMsgToPlayer(GameError.ALREADY_EMPLOYEE);
				// return;
			} else {
				boolean isFreeHire = employee.getUseTimes() < freeTimes;
				if (isFreeHire) { // 免费买
					employee.setEndTime(staticEmployee.getFreeDurationTime() * 1000 + System.currentTimeMillis());
				} else {
					employee.setEndTime(staticEmployee.getDurationTime() * TimeHelper.SECOND_MS + System.currentTimeMillis());
				}
			}
		}

		// 如果当前有武器大师，时间还需要累加
		Employee blackSmith = employInfo.getBlackSmith();
		long timeLeft = 0L;
		if (blackSmith != null) {
			timeLeft = blackSmith.getEndTime() - now;
			timeLeft = timeLeft >= 0 ? timeLeft : 0;
		}

		// 区分免费和非免费情形，简化逻辑
		if (employee.getUseTimes() < freeTimes) {
			// 扣除免费次数
			employee.setUseTimes(employee.getUseTimes() + 1);
			employee.setEndTime(timeLeft + employee.getEndTime());
		} else {
			int gold = staticEmployee.getCostGold();
			if (player.getGold() <= gold) {
				handler.sendErrorMsgToPlayer(GameError.HIRE_OFFICER_NOT_ENOUGH_GOLD);
				return;
			}
			employee.setEndTime(timeLeft + employee.getEndTime());
			if (blackSmith != null && blackSmith.getEmployeeId() != employee.getEmployeeId()) {
				blackSmith.setEndTime(now);
			}
			playerManager.subAward(player, AwardType.GOLD, 1, gold, Reason.HIRE_OFFICER);
		}

		// 更新当前武器大师id
		employInfo.setBlackSmithId(employeeId); // 设置武器大师id

		hireBlackSmithTask(player, staticEmployee.getLevel());

		// 同步消息到客户端
		EquipPb.HireBlackSmithRs.Builder builder = EquipPb.HireBlackSmithRs.newBuilder();
		builder.setEmployeeId(employee.getEmployeeId());
		builder.setEndTime(employee.getEndTime());
		builder.setUseTimes(employee.getUseTimes());
		builder.setGold(player.getGold());

		handler.sendMsgToPlayer(EquipPb.HireBlackSmithRs.ext, builder.build());
		eventManager.hireOfficer(player, Lists.newArrayList(
			employee.getUseTimes() < freeTimes,
			staticEmployee.getLevel(),
			staticEmployee.getName(),
			staticEmployee.getEmployId()
		));
	}

	/**
	 * Function: 装备打造 条件: 指挥官等级, 资源，图纸，材料
	 */
	public void compoundEquipItem(EquipPb.CompoundEquipRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int equipId = req.getEquipId();
		// 查找配置
		StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(equipId);
		if (staticEquip == null) {
			LogHelper.CONFIG_LOGGER.error("equipid = " + equipId + " honor config not found!");
			handler.sendErrorMsgToPlayer(GameError.NO_EQUIP_CONFIG);
			return;
		}

		// 检查指挥官等级
		int lordLv = staticEquip.getLordLv();
		if (player.getLevel() < lordLv) {
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}

		if (staticEquip.getCanCompose() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_COMPOSE);
			return;
		}

		// 检查队列是否存在
		Building buildings = player.buildings;
		LinkedList<WorkQue> workQueList = buildings.getEquipWorkQue();
		if (!workQueList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.EQUIP_WORKQUE_IS_EXISTS);
			return;
		}

		// 检查资源以及图纸
		List<List<Long>> compose = staticEquip.getCompose();
		Resource resource = player.getResource();
		Map<Integer, Item> itemMap = player.getItemMap();
		for (List<Long> itemNeed : compose) {
			if (itemNeed.size() != 3) {
				continue;
			}
			int awardType = itemNeed.get(0).intValue();
			int id = itemNeed.get(1).intValue();
			long count = itemNeed.get(2);
			// 检查资源
			if (awardType == AwardType.RESOURCE) {
				if (resource.getResource(id) < count) {
					handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
					return;
				}
			} else if (awardType == AwardType.PROP) { // 检查道具
				Item item = itemMap.get(id);
				if (item == null) {
					handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
					return;
				}

				if (item.getItemNum() < count) {
					handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
					return;
				}
			}
		}

		WorkQue workQue = new WorkQue();
		workQueList.add(workQue);
		long now = System.currentTimeMillis();

		if (workQue.getKeyId() == 0) {
			workQue.setKeyId(player.maxKey());
		}

		workQue.setBuildingId(BuildingId.EQUIP_BUILDING);
		long period = (long) staticEquip.getPeriod() * TimeHelper.SECOND_MS;
		workQue.setPeriod(period);
		workQue.setEndTime(now + period);
		workQue.setEmployWork(0);
		// 这个时候不发物品给客户端
		Award award = workQue.getAward();
		if (award != null) {
			award.setKeyId(0);
			award.setId(equipId);
			award.setCount(1);
			award.setType(AwardType.EQUIP);
		}

		startMakeEquip(player, equipId);
		EquipPb.CompoundEquipRs.Builder builder = EquipPb.CompoundEquipRs.newBuilder();
		for (List<Long> itemNeed : compose) {
			if (itemNeed.size() != 3) {
				continue;
			}
			int awardType = itemNeed.get(0).intValue();
			int id = itemNeed.get(1).intValue();
			long count = itemNeed.get(2);
			// 检查资源
			if (awardType == AwardType.RESOURCE) {
				playerManager.subAward(player, awardType, id, count, Reason.COMPOUND_ITEM);

				/**
				 * 装备打造资源消耗日志埋点
				 */
				LogUser logUser = SpringUtil.getBean(LogUser.class);
				logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(),
					player.account.getCreateDate(),
					player.getLevel(),
					player.getNick(),
					player.getVip(),
					player.getCountry(),
					player.getTitle(),
					player.getHonor(),
					player.getResource(id),
					RoleResourceLog.OPERATE_OUT, id, ResOperateType.MAKE_EQUIP_OUT.getInfoType(), count, player.account.getChannel()));
				logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
					player.getNick(),
					player.getLevel(),
					player.getTitle(),
					player.getHonor(),
					player.getCountry(),
					player.getVip(),
					player.account.getChannel(),
					1, count, IronOperateType.MAKE_EQUIP_OUT.getInfoType()), id);
			} else if (awardType == AwardType.PROP) { // 检查道具
				playerManager.subAward(player, awardType, id, count, Reason.COMPOUND_ITEM);
				Item item = itemMap.get(id);
				if (item != null) {
					builder.addProp(item.wrapPb());
				}
			}
		}
		if (staticEquip.getQuality() >= 2) {
			activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.MAKE_GREEN_EQUIP, 1);
			if (staticEquip.getQuality() >= 3) {
				activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.MAKE_BLUE_EQUIP, 1);
				if (staticEquip.getQuality() >= 4) {
					activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.MAKE_PURPLE_EQUIP, 1);
				}
			}
		}
		if(staticEquip.getQuality() == Quality.RED.get()){
			achievementService.addAndUpdate(player, AchiType.AT_10,1);
		}
		if(staticEquip.getQuality() == Quality.PURPLE.get()){
			achievementService.addAndUpdate(player, AchiType.AT_11,1);
		}
		builder.setResource(player.wrapResourcePb());
		builder.setWorkQue(workQue.wrapPb());

		handler.sendMsgToPlayer(EquipPb.CompoundEquipRs.ext, builder.build());
		//装备打造
		List param = Lists.newArrayList(
			equipId,
			staticEquip.getEquipName(),
			staticEquip.getLordLv(),
			Quality.getName(staticEquip.getQuality())
		);
		SpringUtil.getBean(EventManager.class).equip_build(player, param);
	}

	/**
	 * Function: 铁匠官减cd: 免费 是否有队列、是否有铁匠官、铁匠官时间结束了、秒完和没秒完有区别
	 */
	public void blackSmithFreeCd(EquipPb.BlackSmithFreeCdRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		long now = System.currentTimeMillis();
		Building building = player.buildings;
		LinkedList<WorkQue> workQueList = building.getEquipWorkQue();
		if (workQueList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.WORKQUE_NOT_EXISTS);
			return;
		}

		WorkQue workQue = workQueList.get(0);
		// 检查生产队列是否存在
		if (workQue.getEndTime() <= now) {
			handler.sendErrorMsgToPlayer(GameError.WORKQUE_NOT_EXISTS);
			return;
		}

		// 判断消费类型
		int cost = req.getCost();
		if (cost == 1) {
			EmployInfo employInfo = player.getEmployInfo();
			Employee employee = employInfo.getBlackSmith();
			if (employee == null) {
				handler.sendErrorMsgToPlayer(GameError.NO_BLACK_SMITH);
				return;
			}

			int employeeId = req.getEmployeeId();
			if (employee.getEmployeeId() != employeeId) {
				handler.sendErrorMsgToPlayer(GameError.NO_BLACK_SMITH);
				return;
			}

			// 检查铁匠官的时间
			if (employee.getEndTime() <= now) {
				handler.sendErrorMsgToPlayer(GameError.BLACK_SMITH_TIME_UP);
				return;
			}

			StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(employeeId);
			if (staticEmployee == null) {
				handler.sendErrorMsgToPlayer(GameError.EMPLOYEE_CONFIG_ERROR);
				return;
			}

			// 以前的设计：一个队列只能被免费加速一次, 现在改成可以被免费减多次,
			// 现在:当前免费cd时间 = 当前铁匠官可以减少的免费时间[读取配置]-speedTime
			long reduceTime = staticEmployee.getReduceTime() * TimeHelper.SECOND_MS;
			if (reduceTime <= workQue.getSpeedTime()) {
				handler.sendErrorMsgToPlayer(GameError.NO_FREE_EQUIP_CD_TIMES);
				return;
			}

			long realReduceTime = reduceTime - workQue.getSpeedTime();
			realReduceTime = Math.max(0, realReduceTime);

			workQue.setEndTime(workQue.getEndTime() - realReduceTime);
			workQue.setEmployWork(1);
			workQue.setSpeedTime(workQue.getSpeedTime() + realReduceTime);

			speedMakeEquip(player);
			EquipPb.BlackSmithFreeCdRs.Builder builder = EquipPb.BlackSmithFreeCdRs.newBuilder();
			builder.setWorkQue(workQue.wrapPb());
			handler.sendMsgToPlayer(EquipPb.BlackSmithFreeCdRs.ext, builder.build());

		} else if (cost == 2) {
			// 金币
			long minutes = TimeHelper.getTotalMinute(workQue.getEndTime());
			if (minutes <= 0) {
				handler.sendErrorMsgToPlayer(GameError.WORKQUE_NOT_EXISTS);
				return;
			}
			StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
			int buildCdLimit = staticLimit.getEquipCdPrice();
			int needGold = (int) minutes * buildCdLimit;
			int owned = player.getGold();
			if (owned < needGold) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, needGold, Reason.COMPOUND_KILL_CD);

			workQue.setEndTime(now);
			EquipPb.BlackSmithFreeCdRs.Builder builder = EquipPb.BlackSmithFreeCdRs.newBuilder();
			builder.setWorkQue(workQue.wrapPb());
			builder.setGold(player.getGold());
			handler.sendMsgToPlayer(EquipPb.BlackSmithFreeCdRs.ext, builder.build());
		} else {
			handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
			return;
		}

	}

	/**
	 * Function: 打造完成请求 检查建造队列的合法性
	 */
	public void doneEquip(EquipPb.DoneEquipRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Building building = player.buildings;
		LinkedList<WorkQue> workQueList = building.getEquipWorkQue();
		if (workQueList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.WORKQUE_NOT_EXISTS);
			return;
		}

		WorkQue workQue = workQueList.get(0);
		long now = System.currentTimeMillis();

		// 检查生产队列是否存在
		if (workQue.getEndTime() > now) {
			handler.sendErrorMsgToPlayer(GameError.EQUIP_WORK_QUE_NOT_OVER);
			return;
		}

		// 添加装备
		int freeSlot = equipManager.getFreeSlot(player);
		// 背包满
		if (freeSlot <= 0) {
			handler.sendErrorMsgToPlayer(GameError.EQUIP_FULL);
			return;
		}

		// 删除队列
		if (!workQueList.isEmpty()) {
			workQueList.remove(0);
		}

		Award award = workQue.getAward();
		int keyId = playerManager.addAward(player, AwardType.EQUIP, award.getId(), 0, Reason.COMPOUND_ITEM);
		award.setKeyId(keyId);

		// 打造完成
		awardEquip(player, award.getId());

		EquipPb.DoneEquipRs.Builder builder = EquipPb.DoneEquipRs.newBuilder();
		builder.setWorkQue(workQue.wrapPb());
		Equip equip = player.getEquipItem(keyId);
		StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(award.getId());
		if (equipManager.canMakeSpecialEquip(player, staticEquip)) {
			equipManager.makeSpecialEquip(equip, staticEquip);
			int chatId = ChatId.DO_EQUIP_GOLD;
			if (staticEquip.getQuality() == Quality.RED.get()) {
				chatId = ChatId.DO_EQUIP_RED;
			}
			chatManager.sendWorldChat(chatId, Country.get(player.getCountry()), player.getNick(), staticEquip.getEquipName());
		}
		builder.setEquipItem(equip.wrapPb());
		handler.sendMsgToPlayer(EquipPb.DoneEquipRs.ext, builder.build());
		if (staticEquip != null && staticEquip.getQuality() >= 3) {
			activityManager.updActPerson(player, ActivityConst.ACT_FORGE_RANK, 1, 0);
			chatManager.updateChatShow(ChatShowType.EQUIP, staticEquip.getEquipId(), player);
			eventManager.equipDone(player, Lists.newArrayList(staticEquip.getEquipId(), staticEquip.getEquipName()
			));
		}
	}

	public void handleWearTask(Player player, int heroId, int equipId) {
		wearTask(player, heroId, equipId);
		anyWear(player, equipId);
	}

	// 完成任务
	public void wearTask(Player player, int heroId, int equipId) {
		ArrayList<Integer> param = new ArrayList<Integer>();
		param.add(heroId);
		param.add(equipId);
		taskManager.doTask(TaskType.SINGLE_HERO_WEAR, player, param);
		ArrayList<Integer> param2 = new ArrayList<Integer>();
		param2.add(heroId);
		StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(equipId);
		param2.add(staticEquip.getEquipType());
		taskManager.doTask(TaskType.POS_WEAR_EQUIP, player, param2);
	}

	// 任意武将穿装备
	public void anyWear(Player player, int equipId) {
		ArrayList<Integer> param = new ArrayList<Integer>();
		param.add(equipId);
		taskManager.doTask(TaskType.ANY_WEAR, player, param);
	}

	// 打造装备
	public void startMakeEquip(Player player, int equipId) {
		ArrayList<Integer> param = new ArrayList<Integer>();
		param.add(equipId);
		taskManager.doTask(TaskType.START_MAKE_EQUIP, player, param);
		taskManager.doTask(TaskType.MAKE_SECOND_EQUIP, player, param);
	}

	// 收获打造装备
	public void awardEquip(Player player, int equipId) {
		ArrayList<Integer> param = new ArrayList<Integer>();
		param.add(equipId);
		taskManager.doTask(TaskType.AWARD_EQUIP, player, param);
	}

	// 招募军需官
	public void hireBlackSmithTask(Player player, int employeeLv) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(employeeLv);
		taskManager.doTask(TaskType.HIRE_BLACKSMITH, player, triggers);
	}

	// 军需官加速打造, 使用免费次数打造
	public void speedMakeEquip(Player player) {
		taskManager.doTask(TaskType.SPEED_MAKE_EQUIP, player, null);
	}

	// 装备洗练
	public void washEquipTask(Player player) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(1);
		taskManager.doTask(TaskType.WASH_EQUIP, player, triggers);
	}

	// 全部武将穿戴精铁盔
	public void doAllHeroWearHat(Player player, int equipId) {
		ArrayList<Integer> param = new ArrayList<Integer>();
		param.add(equipId);
		taskManager.doTask(TaskType.ALL_HERO_WEARHAT, player, param);
	}

	// 全部武将穿戴守备印和千营符
	public void doAllHeroWearTwo(Player player) {
		taskManager.doTask(TaskType.ALL_HERO_WEAR_TWO, player, null);
	}

}
