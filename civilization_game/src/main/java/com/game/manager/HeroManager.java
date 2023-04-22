package com.game.manager;

import com.game.Loading;
import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.define.LoadData;
import com.game.domain.Award;
import com.game.domain.CountryData;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.flame.BuffType;
import com.game.flame.FlameWarManager;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.HeroExpLog;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.HeroPb;
import com.game.pb.RolePb;
import com.game.server.GameServer;
import com.game.service.AchievementService;
import com.game.service.CastleService;
import com.game.service.EquipService;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@LoadData(name = "英雄管理", type = Loading.LOAD_USER_DB, initSeq = 1600)
public class HeroManager extends BaseManager{

	@Autowired
	private StaticHeroMgr staticHeroDataMgr;

	@Autowired
	private EquipManager equipManager;

	@Autowired
	private StaticEquipDataMgr staticEquipMgr;

	@Autowired
	private KillEquipManager killEquipManager;

	@Autowired
	public StaticKillEquipMgr staticKillEquipMgr;

	@Autowired
	private TechManager techManager;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private SoldierManager soldierManager;

	@Autowired
	private CountryManager ctManger;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private RankManager rankManager;

	@Autowired
	private StaticCountryMgr staticCountryMgr;

	@Autowired
	private CountryManager countryManager;

	@Autowired
	private StaticMeetingTaskMgr staticMeetingTaskMgr;
	@Autowired
	private CastleService castleService;

	@Autowired
	private BeautyManager beautyManager;

	@Autowired
	private OmamentManager omamentManager;
	@Autowired
	private TDManager tdManager;
	@Autowired
	private StaticWarBookMgr staticWarBookMgr;
	@Autowired
	private WarBookManager warBookManager;

	@Autowired
	private EquipService equipService;

	@Autowired
	private SurpriseGiftManager surpriseGiftManager;

	@Autowired
	private CommandSkinManager commandSkinManager;
	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private HeroManager heroManager;
	@Autowired
	private BroodWarManager broodWarManager;

	@Autowired
	private FlameWarManager flameWarManager;
    @Autowired
    AchievementService achievementService;

	@Autowired
	private WorldTargetManager worldTargetManager;

	// equipId, level
	private Map<Integer, Map<Integer, StaticKillEquipLevel>> killEquipLevelKeymap = new HashMap<Integer, Map<Integer, StaticKillEquipLevel>>();

	// 突破英雄记录：用户ID,武将ID,武将数据
	@Getter
	private HashBasedTable<Long, Integer, Hero> divineAdvanceHeros = HashBasedTable.create();

	// 计算英雄属性
	// 总攻击公式=（武将基础攻击(配置)+ 攻击因素(配置)*攻击资质(当前英雄资质) + 攻击等级因素 * 等级 + 装备攻击 + 科技攻击 +
	// 国家加成攻击[暂时不做] + (国器攻击)
	// 最终可以理解为: 基础加成 + 等级加成 + 资质加成 + 装备以及技能加成 + 国家 + 国器
	// 统一接口，防止到处乱加造成的不一致
	// 单一英雄属性变化
	// 科技加成、国家加成、国器加成
//    public void caculateProp(Hero hero, Player player) {
//        if (hero == null) {
//            LogHelper.ERROR_LOGGER.error("hero is null");
//            return;
//        }
//
//        Property total = hero.getTotalProp();
//        total.clear();
//        //TODO 英雄基础属性[不穿装备，技能等]  jyb 这里要加入参谋部的加成
//        int soldierLines = playerManager.getSoldierLine(player) + staticMeetingTaskMgr.soldierNumByHero(player, hero.getHeroId());
//
//        // 基础
//        Property baseAdd = getBaseProperty(hero);
//        // LogHelper.GAME_DEBUG.error("heroId = " + hero.getHeroId() +
//        // ", nick = " + player.getNick());
//        // LogHelper.GAME_DEBUG.error("baseAdd = " + baseAdd);
//
//        // 资质
//        Property qulifyAdd = getQulifyProperty(hero, soldierLines);
//        // LogHelper.GAME_DEBUG.error("qulifyAdd = " + qulifyAdd);
//        // 国器
//
//        Property ctProperty = killEquipManager.getAllProperty(player);
//        // LogHelper.GAME_DEBUG.error("ctProperty = " + ctProperty);
//        //配饰
//        Property ommentProperty = new Property();
//        if (player.getEmbattleList().contains(hero.getHeroId())) {
//            ommentProperty = getOmmentProperty(player);
//        }
//        // 属性计算
//        int attack = baseAdd.getAttack() + qulifyAdd.getAttack() + ctProperty.getAttack() + ommentProperty.getAttack();
//        int defence = baseAdd.getDefence() + qulifyAdd.getDefence() + ctProperty.getDefence() + ommentProperty.getDefence();
//
//        // 装备属性, 直接加
//        Property equipProperty = getEquipProperty(hero); // hero.getEquipProperty();
//
//        //兵书属性,直接加
//        Property warBookProperty = getWarBookProperty(hero); // hero.getWarBookProperty();
//
//        // 爵位加成
//        Property titleProperty = ctManger.getTitleAttack(player);
//
//        //主城皮肤加成
//        Property skinProperty = commandSkinManager.getProperty(player, hero);
//        //美女加成
//        Property beautyProperty = getBeautyAddition(hero, player, qulifyAdd);
//
//        int soldierByPercentage = (int) Math.floor((baseAdd.getSoldierNum() / 4.0 * soldierLines + qulifyAdd.getSoldierNum()) * (1 + beautyProperty.getPercentageOfForceAdition() / DevideFactor.PERCENT_NUM));
//        int soldier = (int) Math.floor((float) (titleProperty.getSoldierNum() + ctProperty.getSoldierNum() + equipProperty.getSoldierNum() + warBookProperty.getSoldierNum() + ommentProperty.getSoldierNum() + skinProperty.getSoldierNum()+beautyProperty.getSoldierNum()) / 4.0f * (float) soldierLines) + soldierByPercentage;
//
//        total.setAttack(attack);
//        total.setDefence(defence);
//        total.setSoldierNum(soldier);
//        total.addAttack(equipProperty);
//        total.addDefence(equipProperty);
//        total.addAttack(warBookProperty);
//        total.addAttack(beautyProperty);
//        total.addDefence(warBookProperty);
//        //加入强攻 强防  攻城 守城
//        total.setDefenceCity(equipProperty.getDefenceCity() + warBookProperty.getDefenceCity());
//        total.setAttackCity(equipProperty.getAttackCity() + warBookProperty.getAttackCity());
//        total.setStrongDefence(equipProperty.getStrongDefence() + warBookProperty.getStrongDefence());
//        total.setStrongAttack(equipProperty.getStrongAttack() + warBookProperty.getStrongAttack());
//
//        // LogHelper.GAME_DEBUG.error("equipProperty = " + equipProperty);
//
//        // 科技加属性
//        List<List<Integer>> heroProperty = techManager.getHeroProperty(player, getSoldierType(hero.getHeroId()));
//        if (null != heroProperty && !heroProperty.isEmpty()) {
//            for (List<Integer> effect : heroProperty) {
//                if (null != effect && effect.size() == 2) {
//                    int effectType = effect.get(0);
//                    if (effectType == PropertyType.ATTCK) {
//                        total.setAttack(total.getAttack() + effect.get(1));
//                    } else if (effectType == PropertyType.DEFENCE) {
//                        total.setDefence(total.getDefence() + effect.get(1));
//                    } else if (effectType == PropertyType.STRONG_ATTACK) {
//                        total.setStrongAttack(total.getStrongAttack() + effect.get(1));
//                    } else if (effectType == PropertyType.STRONG_DEFENCE) {
//                        total.setStrongDefence(total.getStrongDefence() + effect.get(1));
//                    } else if (effectType == PropertyType.ATTACK_CITY) {
//                        total.setAttackCity(total.getAttackCity() + effect.get(1));
//                    } else if (effectType == PropertyType.DEFENCE_CITY) {
//                        total.setDefenceCity(total.getDefenceCity() + effect.get(1));
//                    }
//                }
//            }
//        }
//        // LogHelper.GAME_DEBUG.error("techAttack = " + techAttack);
//        total.setAttack(total.getAttack() + titleProperty.getAttack() + skinProperty.getAttack());
//        total.setDefence(total.getDefence() + titleProperty.getDefence() + skinProperty.getDefence());
//        // LogHelper.GAME_DEBUG.error("ctAttack = " + ctAttack);
//
//        // 神级突破
//        Property special = hero.getSpecialProp();
//        // LogHelper.GAME_DEBUG.error("special = " + special);
//
//        total.add(special);
//
//        // buff 加成
//        float addDefenceFactor = playerManager.getBuffAdd(player, BuffId.ADD_DEFENCE);
//        int addDefence = (int) (addDefenceFactor * (float) total.getDefence());
//
//        float addAttackFactor = playerManager.getBuffAdd(player, BuffId.ADD_ATTACK);
//        int addAttack = (int) (addAttackFactor * (float) total.getAttack());
//
//        total.addAttackValue(addAttack);
//        total.addDefenceValue(addDefence);
//        //LogHelper.GAME_DEBUG.error("addAttack = " + addAttack);
//        //LogHelper.GAME_DEBUG.error("addDefence = " + addDefence);
//        // System.err.println(total);
//        if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
//            hero.setCurrentSoliderNum(hero.getSoldierNum());
//        }
//        castleService.updateDefenseArmyByAttributeChange(player, hero);
//    }

	@Override
	public void init() throws Exception{
		List<Player> list = playerManager.getAllPlayer().values().stream().filter(e -> e.getLevel() >= 100).collect(Collectors.toList());
		list.stream().forEach(player -> {
			for (Hero hero : player.getHeros().values()) {
				if (hero.getDiviNum() > 0) {
					divineAdvanceHeros.put(player.getRoleId(), hero.getHeroId(), hero);
				}
			}
		});
	}

	public void caculateProp(Hero hero, Player player) {
		if (hero == null) {
			LogHelper.CONFIG_LOGGER.info("hero is null");
			return;
		}
		Property total = hero.getTotalProp();
		total.clear();
		//TODO 英雄基础属性[不穿装备，技能等]  jyb 这里要加入参谋部的加成
		int soldierLines = playerManager.getSoldierLine(player) + staticMeetingTaskMgr.soldierNumByHero(player, hero.getHeroId());
		// 基础
		Property baseAdd = getBaseProperty(hero);
		// 资质
		Property qulifyAdd = getQulifyProperty(hero, soldierLines);
		// 国器
		Property ctProperty = killEquipManager.getAllProperty(player);
		//配饰
		if (player.getEmbattleList().contains(hero.getHeroId())) {
			getOmmentProperty(player, ctProperty);
		}
		// 装备属性, 直接加
		getEquipProperty(hero, ctProperty); // hero.getEquipProperty();
		//兵书属性,直接加
		getWarBookProperty(hero, ctProperty); // hero.getWarBookProperty();
		//母巢加成
		broodWarManager.getBroodWarProperty(player, hero, ctProperty);
		// 爵位加成
		ctManger.getTitleAttack(player, ctProperty);
		//主城皮肤加成
		commandSkinManager.getProperty(player, hero, ctProperty);
		//美女加成
		Property beautyProperty = getBeautyAddition(hero, player, qulifyAdd, ctProperty);
		//科技加成（包括兵种进阶）
		techManager.getHeroProperty(player, getSoldierType(hero.getHeroId()), ctProperty);

		int soldierByPercentage = (int) Math.floor((baseAdd.getSoldierNum() / 4.0 * soldierLines + qulifyAdd.getSoldierNum()) * (1
			+ beautyProperty.getPercentageOfForceAdition() / DevideFactor.PERCENT_NUM));
		int soldier = (int) Math.floor((float) (ctProperty.getSoldierNum()) / 4.0f * (float) soldierLines) + soldierByPercentage;
		// 属性计算
		int attack = baseAdd.getAttack() + qulifyAdd.getAttack() + ctProperty.getAttack();
		int defence = baseAdd.getDefence() + qulifyAdd.getDefence() + ctProperty.getDefence();
		total.setAttack(attack);
		total.setDefence(defence);
		total.setSoldierNum(soldier);
		//加入强攻 强防  攻城 守城
		total.setDefenceCity(ctProperty.getDefenceCity() + baseAdd.getDefenceCity());
		total.setAttackCity(ctProperty.getAttackCity() + baseAdd.getAttackCity());
		total.setStrongDefence(ctProperty.getStrongDefence() + baseAdd.getStrongDefence());
		total.setStrongAttack(ctProperty.getStrongAttack() + baseAdd.getStrongAttack());
		// 神级突破
		Property special = hero.getSpecialProp();
		total.add(special);
		// buff 加成
		float addDefenceFactor = playerManager.getBuffAdd(player, BuffId.ADD_DEFENCE);
		int addDefence = (int) (addDefenceFactor * (float) total.getDefence());

		float addAttackFactor = playerManager.getBuffAdd(player, BuffId.ADD_ATTACK);
		int addAttack = (int) (addAttackFactor * (float) total.getAttack());

		//战火燎原活动 buff加成
		float v = flameWarManager.addFlameProperty(player, BuffType.buff_1);
		int flameAddAttBuff = (int) (v * total.getAttack());
		total.addAttackValue(flameAddAttBuff);

		float v1 = flameWarManager.addFlameProperty(player, BuffType.buff_2);
		int flameAddDefBuff = (int) (v1 * total.getDefence());
		total.addDefenceValue(flameAddDefBuff);

		total.addAttackValue(addAttack);
		total.addDefenceValue(addDefence);
		if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
			int diffs = hero.getCurrentSoliderNum() - hero.getSoldierNum();
			playerManager.addAward(player, AwardType.SOLDIER, heroManager.getSoldierType(hero.getHeroId()), diffs, Reason.TAKE_EQUIP);
			hero.setCurrentSoliderNum(hero.getSoldierNum());
			playerManager.synChange(player, Reason.TAKE_EQUIP);
		}
		castleService.updateDefenseArmyByAttributeChange(player, hero);
	}

	// 资质增加
	public Property getQulifyProperty(Hero hero, int soldierLine) {
		Property qualifyProp = hero.getQualifyProp();

		Property qualifyPropAdd = new Property();
		int heroLv = hero.getHeroLv();
		int heroType = staticHeroDataMgr.getHeroType(hero.getHeroId());
		PropertyFactor propertyFactor = staticHeroDataMgr.getPropertyFactor(heroLv, heroType);

		int attack = (int) ((float) (qualifyProp.getAttack()) * propertyFactor.getAttackFactor());
		int defence = (int) ((float) (qualifyProp.getDefence()) * propertyFactor.getDefenceFactor());

		int soldier = (int) (qualifyProp.getSoldierNum() * propertyFactor.getSoldierFactor() * soldierLine);

		qualifyPropAdd.setAttack(attack);
		qualifyPropAdd.setDefence(defence);
		qualifyPropAdd.setSoldierNum(soldier);

		return qualifyPropAdd;
	}

	// 配置基础属性
	public Property getBaseProperty(Hero hero) {
		Property property = new Property();
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		if (staticHero == null) {
			return property;
		}

		property.setAttack(staticHero.getBaseAttack());
		property.setDefence(staticHero.getBaseDefence());
		property.setSoldierNum(staticHero.getBaseSoldierCount());

		int talentLevel = hero.getTalentLevel();
		while (talentLevel > 0) {
			StaticHeroTalent staticHeroTalent = staticHeroDataMgr.getStaticHeroTalent(staticHero.getTalentType(), talentLevel, staticHero.getSoldierType());
			if (staticHeroTalent != null) {
				List<Integer> effect = staticHeroTalent.getEffect();
				property.addValue(effect.get(0), effect.get(1));
			}
			talentLevel--;
		}

		return property;

	}

	/**
	 * Function:添加英雄, need reason
	 */
	public Hero addHero(Player player, int heroId, int reason) {
		Map<Integer, Hero> heros = player.getHeros();
		Hero hasHero = heros.get(heroId);
		if (hasHero != null) {
			LogHelper.CONFIG_LOGGER.info("hero already exists, id = " + heroId + ", reason = " + reason);
			return hasHero;
		}

		if (hasHeroType(player, heroId)) {
			return null;
		}

		StaticHero staticHero = staticHeroDataMgr.getStaticHero(heroId);
		if (staticHero == null) {
			LogHelper.CONFIG_LOGGER.info("hero Id = " + heroId + " static hero[config] not found!");
			return null;
		}

		Hero hero = new Hero();
		// 初始化英雄基础属性
		hero.init(staticHero);
		caculateProp(hero, player);
		// 添加英雄
		heros.put(heroId, hero);
		autoWearEquip(player, hero);
        achievementService.addAndUpdate(player, AchiType.AT_4, 1);
		/**
		 * 英雄升级日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.heroExpLog(new HeroExpLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), staticHero.getHeroType(),
			hero.getHeroId(), staticHero.getHeroName(), hero.getHeroLv(), hero.getExp(), 0, reason, player.account.getChannel()));
		List param = Lists.newArrayList(staticHero.getHeroId(), staticHero.getHeroType(), staticHero.getQuality(), hero.getQualifyProp().getAttack(),
			hero.getQualifyProp().getDefence(), hero.getQualifyProp().getSoldierNum(), staticHero.getMaxTotal(), staticHero.getMaxTotal(), 0,
			hero.getAdvanceProcess(), reason, staticHero.getHeroName(), staticHero.getSoldierType());
		SpringUtil.getBean(EventManager.class).get_hero(player, param);
		SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.add_hero);
		surpriseGiftManager.doSurpriseGift(player, SuripriseId.GetHero, heroId, true);
		return hero;
	}

	public void autoWearEquip(Player player, Hero hero) {
		List<Integer> autoWearEquipHero = staticLimitMgr.getAddtion(SimpleId.WEAR_HERO);
		if (autoWearEquipHero.contains(hero.getHeroId())) {
			List<Integer> autoWearEquip = staticLimitMgr.getAddtion(SimpleId.WEAR_EQUIP);
			for (int equipId : autoWearEquip) {
				// 先给自己发装备
				StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equipId);
				Equip equip = equipManager.addEquip(player, equipId, Reason.AUTO_WARD);
				// 增加英雄身上装备
				HeroEquip heroEquip = new HeroEquip();
				heroEquip.setPos(staticEquip.getEquipType());
				heroEquip.setEquip(equip.cloneInfo());
				hero.addHeroEquip(heroEquip);
				// equipService.handleWearTask(player, hero.getHeroId(), equip.getEquipId());
				equipService.doAllHeroWearHat(player, equip.getEquipId());
				equipService.doAllHeroWearTwo(player);
				player.getEquips().remove(equip.getKeyId());
			}
		}
	}

	public CommonPb.EquipExChange wrapEquipExchange(Hero hero) {
		return PbHelper.createEquipExchangePb(hero);
	}

	// 返回值为英雄是否升级
	public boolean addExp(Hero hero, Player player, long exp, int reason) {
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		// 标记英雄是否升级
		boolean isHeroUpLevel = false;

		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null in addExp.");
			return false;
		}
		if (exp <= 0) {
			// LogHelper.CONFIG_LOGGER.info("exp is less or equal zero.");
			return false;
		}

		int playerLevel = player.getLevel();
		// 英雄等级不能高于玩家等级
		int heroQuality = staticHeroDataMgr.getQuality(hero.getHeroId());
		if (heroQuality == 0) {
			LogHelper.CONFIG_LOGGER.info("heroQuality is 0.");
			return false;
		}

		int maxLevel = staticHeroDataMgr.maxLevel();
		int evertLvExp;
		int levelIndex = hero.getHeroLv() + 1;

		long totalExp = exp + hero.getExp();
		for (; levelIndex <= maxLevel; levelIndex++) {
			// evertLvExp = staticHeroDataMgr.getExp(levelIndex, heroQuality);
			evertLvExp = staticHeroDataMgr.getExp(hero.getHeroLv(), heroQuality);
			if (totalExp < evertLvExp) {
				break;
			}

			if (hero.getHeroLv() < playerLevel) {
				totalExp -= evertLvExp;
				hero.setHeroLv(hero.getHeroLv() + 1);
				isHeroUpLevel = true;

				// 英雄升级报送
				List param = Lists.newArrayList(staticHero.getHeroId(), staticHero.getHeroType(), staticHero.getQuality(), hero.getQualifyProp().getAttack(), hero.getQualifyProp().getDefence(), hero.getQualifyProp().getSoldierNum(), staticHero.getMaxTotal(), staticHero.getMaxTotal(), 0, hero.getAdvanceProcess(), hero.getHeroLv(), staticHero.getHeroName(), staticHero.getSoldierType()

				);
				SpringUtil.getBean(EventManager.class).hero_level_up(player, param);

				if (staticHero.getQuality() >= Quality.GREEN.get()) {
					activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.HERO_LEVLE_UP + hero.getHeroLv(), 0, 1);
				}
				if (staticHero.getQuality() >= Quality.GOLD.get()) {
					activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.HERO_ORANGE_LEVEL_UP + hero.getHeroLv(), 0, 1);
				}
				if (staticHero.getQuality() >= Quality.RED.get()) {
					activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.HERO_RED_LEVEL_UP + hero.getHeroLv(), 0, 1);
				}

			}

			// 经验加满
			if (hero.getHeroLv() >= playerLevel) {
				if (hero.getHeroLv() < maxLevel) {
					// long nextExp = staticHeroDataMgr.getExp(hero.getHeroLv() + 1, heroQuality);
					long nextExp = staticHeroDataMgr.getExp(hero.getHeroLv(), heroQuality);
					if (totalExp >= nextExp) {
						hero.setExp(nextExp);
						totalExp = nextExp;
					}
				}
				break;
			}
		}

		// 剩余经验值
		if (totalExp > 0) {
			hero.setExp(totalExp);
		}

		// 经验值不能超过上限
		if (hero.getHeroLv() >= playerLevel && hero.getHeroLv() < maxLevel) {
			long maxExp = staticHeroDataMgr.getExp(hero.getHeroLv() + 1, heroQuality);
			if (hero.getExp() >= maxExp) {
				hero.setExp(maxExp);
			}
		}

		// 达到最高等级
		if (hero.getHeroLv() >= maxLevel) {
			hero.setExp(0);
		}

		// 处理国家武将的等级
		handleCountryHeroLv(hero);

		SpringUtil.getBean(EventManager.class).hero_get_exp(player, Lists.newArrayList(exp, reason, hero.getHeroId(), hero.getHeroId(), hero.getHeroLv(), 1, staticHero.getHeroType(), staticHero.getQuality()));
		/**
		 * 英雄升级日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		StaticHero sHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		try {
			logUser.heroExpLog(new HeroExpLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), sHero.getHeroType(), hero.getHeroId(), sHero.getHeroName(), hero.getHeroLv(), hero.getExp(), exp, reason, player.account.getChannel()));
		} catch (Exception e) {
			LogHelper.CONFIG_LOGGER.info("记录日志错误->[{}] ->[{}] ->[{}] ->[{}]", e, player.getLord().getLordId(), player.account, player.account != null ? player.account.getCreateDate() : "null");
		}
		return isHeroUpLevel;
	}

	public void handleCountryHeroLv(Hero hero) {
		if (hero == null) {
			return;
		}
		int heroId = hero.getHeroId();
		Integer country = staticCountryMgr.getCountryByHeroId(heroId);
		if (country == null) {
			return;
		}

		CountryData countryData = countryManager.getCountry(country);
		if (countryData == null) {
			return;
		}

		CountryHero countryHero = countryData.getCountryHero(heroId);
		if (countryHero == null) {
			return;
		}
		countryHero.setHeroLv(hero.getHeroLv());
	}

	// 一级属性
	public Property getEquipProperty(Hero hero, Property equipProperty) {
		ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
		for (HeroEquip heroEquip : heroEquips) {
			if (heroEquip == null) {
				continue;
			}
			Equip equip = heroEquip.getEquip();
			equipProperty.add(equipManager.getProperty(equip));
		}
		return equipProperty;
	}

	// 一级属性
	public Property getEquipProperty(Hero hero) {
		Property equipProperty = new Property();
		ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
		for (HeroEquip heroEquip : heroEquips) {
			if (heroEquip == null) {
				continue;
			}
			Equip equip = heroEquip.getEquip();
			equipProperty.add(equipManager.getProperty(equip));
		}
		return equipProperty;
	}

	//美女英雄加成
	public Property getBeautyAddition(Hero hero, Player player, Property qulifyAdd) {
		Property property = new Property();
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		if (staticHero == null) {
			return property;
		}
		//基础攻击加成
		int attackBonus = 0;
		//百分比攻击加成
		int attackBonusPercentage = 0;
		//英雄兵种攻击力加成
		attackBonus += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.ATTACK_BONUS, staticHero.getSoldierType());
		//所有兵种基础攻击力加成
		attackBonus += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.ATTACK_BONUS, 4);
		//英雄兵种攻击力百分比加成
		attackBonusPercentage += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.ATTACK_BONUS_PERCENTAGE, staticHero.getSoldierType());
		//英雄所有兵种攻击力百分比加成
		attackBonusPercentage += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.ATTACK_BONUS_PERCENTAGE, 4);
		//英雄基础攻击力
		int baseAttack = staticHero.getBaseAttack() + qulifyAdd.getAttack();
		//美女攻击力总加成
		attackBonus += (int) Math.floor(baseAttack * (attackBonusPercentage / DevideFactor.PERCENT_NUM));
		property.setAttack(attackBonus);

		//基础攻击加成兵力
		int forceBonus = 0;
		//百分比兵力加成
		int percentageOfForceAdition = 0;
		//英雄所有兵种兵力加成
		forceBonus += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.FORCE_BONUS, 4);
		//英雄所有兵种兵力百分比加成
		percentageOfForceAdition += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.PERCENTAGE_OF_FORCE_ADDITION, 4);
		property.setSoldierNum(forceBonus);
		property.setPercentageOfForceAdition(percentageOfForceAdition);
		return property;
	}

	//美女英雄加成
	public Property getBeautyAddition(Hero hero, Player player, Property qulifyAdd, Property property) {

		StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		if (staticHero == null) {
			return property;
		}
		//基础攻击加成
		int attackBonus = 0;
		//百分比攻击加成
		int attackBonusPercentage = 0;

		//英雄兵种攻击力加成
		attackBonus += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.ATTACK_BONUS, staticHero.getSoldierType());
		//所有兵种基础攻击力加成
		attackBonus += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.ATTACK_BONUS, 4);
		//英雄兵种攻击力百分比加成
		attackBonusPercentage += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.ATTACK_BONUS_PERCENTAGE, staticHero.getSoldierType());
		//英雄所有兵种攻击力百分比加成
		attackBonusPercentage += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.ATTACK_BONUS_PERCENTAGE, 4);
		//英雄基础攻击力
		int baseAttack = staticHero.getBaseAttack() + qulifyAdd.getAttack();
		//美女攻击力总加成
		attackBonus += (int) Math.floor(baseAttack * (attackBonusPercentage / DevideFactor.PERCENT_NUM));
		property.addAttackValue(attackBonus);

		// 基础防御加成
		int defenceBonus = 0;
		//防御百分比加成
		int defenceBonusPercentage = 0;
		defenceBonus += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.DEFENSE_BONUS, 4);
		defenceBonusPercentage += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.DEFENSE_BONUS_PERCENTAGE, 4);
		// 英雄的基础防御值
		int baseDefend = staticHero.getBaseDefence() + qulifyAdd.getDefence();
		// 美女防御值总加成
		defenceBonus += (int) Math.floor(baseDefend * (defenceBonusPercentage / DevideFactor.PERCENT_NUM));
		property.addDefenceValue(defenceBonus);

		//兵力加成
		int forceBonus = 0;
		//百分比兵力加成
		int percentageOfForceAdition = 0;
		//英雄所有兵种兵力加成
		forceBonus += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.FORCE_BONUS, 4);
		//英雄所有兵种兵力百分比加成
		percentageOfForceAdition += beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.PERCENTAGE_OF_FORCE_ADDITION, 4);
		property.addSoldierNumValue(forceBonus);
		property.setPercentageOfForceAdition(percentageOfForceAdition);
		return property;
	}

	// 兵书一级属性
	public Property getWarBookProperty(Hero hero, Property bookProperty) {
		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		for (HeroBook heroBook : heroBooks) {
			if (heroBook == null) {
				continue;
			}
			WarBook book = heroBook.getBook();
			StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
			bookProperty.add(warBookManager.getProperty(book, staticHero));
		}
		return bookProperty;
	}

	// 兵书一级属性
	public Property getWarBookProperty(Hero hero) {
		Property bookProperty = new Property();
		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		for (HeroBook heroBook : heroBooks) {
			if (heroBook == null) {
				continue;
			}
			WarBook book = heroBook.getBook();
			StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
			bookProperty.add(warBookManager.getProperty(book, staticHero));
		}
		return bookProperty;
	}


	// 二级属性
	public BattleProperty getBattleProperty(Hero hero) {
		BattleProperty battleProperty = new BattleProperty();
		ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
		for (HeroEquip heroEquip : heroEquips) {
			if (heroEquip == null) {
				continue;
			}
			Equip equip = heroEquip.getEquip();
			if (equip == null) {
				continue;
			}
			List<Integer> skills = equip.getSkills();
			battleProperty.add(equipManager.getBattleProperty(skills));
		}
		//天赋
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		int talentLevel = hero.getTalentLevel();
		while (talentLevel > 0) {
			StaticHeroTalent staticHeroTalent = staticHeroDataMgr.getStaticHeroTalent(staticHero.getTalentType(), talentLevel, staticHero.getSoldierType());
			if (staticHeroTalent != null) {
				List<Integer> effect = staticHeroTalent.getEffect();
				battleProperty.addValue(effect.get(0), effect.get(1));
			}
			talentLevel--;
		}

		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		if (null == hero || heroBooks.size() == 0) {
			return battleProperty;
		}

		//StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		if (null == staticHero) {
			return battleProperty;
		}

		HeroBook heroBook = heroBooks.get(0);
		WarBook book = heroBook.getBook();
		if (null == book) {
			return battleProperty;
		}

		// 获得兵书主属性
		ArrayList<Integer> baseProperty = book.getBaseProperty();
		if (baseProperty.size() == 1) {
			StaticWarBookBaseProperty warBookBasePropById = staticWarBookMgr.getWarBookBasePropById(baseProperty.get(0));
			if (warBookBasePropById == null) {
				return battleProperty;
			}
			List<List<Integer>> affect = warBookBasePropById.getAffect();
			if (affect.size() > 0) {
				for (List<Integer> affectValue : affect) {
					battleProperty.addValue(affectValue.get(0), affectValue.get(1));
				}
			}
		}

		ArrayList<Integer> currentSkill = book.getCurrentSkill();
		if (currentSkill.size() > 0) {
			for (Integer skillId : currentSkill) {
				StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(skillId);
				if (null != warBookSkillById) {
					int soldierType = warBookSkillById.getSoldierType();
					if (soldierType != 0 && staticHero.getSoldierType() != soldierType) {// 判断英雄的兵种类型是否与当前技能对应
						continue;
					}
					List<List<Integer>> affect = warBookSkillById.getAffect();
					for (List<Integer> affectValue : affect) {
						battleProperty.addValue(affectValue.get(0), affectValue.get(1));
					}
				}
			}
		}
		return battleProperty;
	}

	//// 二级属性
	//public BattleProperty getBattleProperty(Hero hero) {
	//	BattleProperty battleProperty = new BattleProperty();
	//	ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
	//	for (HeroEquip heroEquip : heroEquips) {
	//		if (heroEquip == null) {
	//			continue;
	//		}
	//		Equip equip = heroEquip.getEquip();
	//		if (equip == null) {
	//			continue;
	//		}
	//		List<Integer> skills = equip.getSkills();
	//		battleProperty.add(equipManager.getBattleProperty(skills));
	//	}
	//	return battleProperty;
	//}

	// 只加上阵武将的经验
	public List<Hero> addAllHeroExp(Player player, int exp, int reason) {
		Map<Integer, Hero> heros = player.getHeros();
		ArrayList<Hero> list = new ArrayList<>();
		List<Integer> embattleList = player.getEmbattleList();
		for (Integer heroId : embattleList) {
			Hero hero = heros.get(heroId);
			if (hero == null) {
				continue;
			}
			boolean b = addExp(hero, player, exp, reason);
			if (b) {
				list.add(hero);
			}
		}
		return list;
	}

	// 只加上阵武将的经验
	public List<Hero> addAllHeroExp(Player player, List<Integer> heroIds, int exp, int reason) {
		Map<Integer, Hero> heros = player.getHeros();
		// 需要推送的英雄
		ArrayList<Hero> list = new ArrayList<>();
		for (Integer heroId : heroIds) {
			Hero hero = heros.get(heroId);
			if (hero == null) {
				continue;
			}
			boolean b = addExp(hero, player, exp, reason);
			if (b) {
				list.add(hero);
			}
		}
		return list;
	}

	public ArrayList<Award> checkAward(Player player, ArrayList<Award> lootAward, List<Integer> heroIds) {
		ArrayList<Award> lastAward = new ArrayList<Award>();
		for (int i = 0; i < lootAward.size(); i++) {
			Award award = lootAward.get(i);
			if (award == null) {

				continue;
			}
			boolean isHeroType = award.getType() == AwardType.HERO;
			boolean hasHeroType = hasHeroType(player, award.getId());
			boolean awardHasHero = heroIds.contains(award.getId());
			if (isHeroType && (hasHeroType || awardHasHero)) {
				StaticHero staticHero = staticHeroDataMgr.getStaticHero(award.getId());
				List<Integer> heroChip = staticHero.getHeroChip();
				if (heroChip == null) {
					continue;
				}

				if (heroChip.size() != 3) {
					continue;
				}

				lastAward.add(new Award(0, AwardType.PROP, heroChip.get(1), heroChip.get(2)));
			} else {
				if (award.getType() == AwardType.HERO) {
					heroIds.add(award.getId());
				}
				lastAward.add(new Award(award.getKeyId(), award.getType(), award.getId(), award.getCount()));
			}
		}

		return lastAward;

	}

	public boolean hasHeroType(Player player, int heroId) {
		// 查找武将的类型
		int targetHeroType = staticHeroDataMgr.getHeroType(heroId);
		Map<Integer, Hero> heroMap = player.getHeros();
		for (Map.Entry<Integer, Hero> entry : heroMap.entrySet()) {
			Hero hero = entry.getValue();
			if (hero == null) {
				continue;
			}
			int heroType = staticHeroDataMgr.getHeroType(hero.getHeroId());
			if (heroType == 0) {
				continue;
			}

			if (targetHeroType == heroType) {
				return true;
			}
		}

		return false;
	}

	public boolean hasHero(Player player, int heroId) {
		Map<Integer, Hero> heroMap = player.getHeros();
		return heroMap.containsKey(heroId);
	}

	// 武将进阶
	public Hero advanceHero(Hero hero, Player player) {
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		// 替换武将的Id
		int advanceId = staticHero.getAdvancedId();
		StaticHero advanceConfig = staticHeroDataMgr.getStaticHero(advanceId);

		// 进阶后的武将
		Hero advanceHero = new Hero();
		advanceHero.init(advanceConfig);
		Property qualifyProp = advanceHero.getQualifyProp();
		// 把加的洗练属性加过来
		qualifyProp.add(getWashProperty(hero));
		// 把装备以及技能带过来
		advanceHero.cloneHeroEquip(hero);
		//把兵书带过来
		advanceHero.cloneHeroBook(hero);

		// 带等级和经验过来
		advanceHero.setHeroLv(hero.getHeroLv());
		advanceHero.setExp(hero.getExp());
		advanceHero.setCurrentSoliderNum(hero.getCurrentSoliderNum());
		advanceHero.setAdvanceProcess(0);
		advanceHero.setAdvanceTime(0);

		// 重新计算属性
		caculateProp(advanceHero, player);
		Map<Integer, Hero> heroMap = player.getHeros();
		heroMap.remove(hero.getHeroId());
		heroMap.put(advanceHero.getHeroId(), advanceHero);

		//英雄升级报送
		List param = Lists.newArrayList(
			staticHero.getHeroId(),
			staticHero.getHeroType(),
			staticHero.getQuality(),
			hero.getQualifyProp().getAttack(),
			hero.getQualifyProp().getDefence(),
			hero.getQualifyProp().getSoldierNum(),
			staticHero.getMaxTotal(),
			staticHero.getMaxTotal(),
			0,
			hero.getAdvanceProcess()
		);
		SpringUtil.getBean(EventManager.class).hero_breakthrough(player, param);
		return advanceHero;
	}

	public Property getWashProperty(Hero hero) {
		Property qualifyProp = hero.getQualifyProp();
		Property washProperty = qualifyProp.cloneInfo();
		Property initProperty = new Property();
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		if (staticHero != null) {
			initProperty.setAttack(staticHero.getAttack());
			initProperty.setDefence(staticHero.getDefence());
			initProperty.setSoldierNum(staticHero.getSoldierCount());
		}
		washProperty.sub(initProperty);
		return washProperty;
	}

	public HeroEquip getEquip(Hero hero, int equipType) {
		ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
		for (HeroEquip heroEquip : heroEquips) {
			if (heroEquip == null) {
				continue;
			}

			Equip equip = heroEquip.getEquip();
			if (equip == null) {
				continue;
			}

			int equipId = equip.getEquipId();
			if (equipId == 0) {
				continue;
			}
			StaticEquip staticEquip = staticEquipMgr.getStaticEquip(equipId);
			if (staticEquip == null) {
				continue;
			}

			if (staticEquip.getEquipType() == equipType) {
				return heroEquip;
			}
		}

		return null;
	}

	public void caculateAllProperty(Map<Integer, Hero> heroes, Player player) {
		if (heroes == null) {
			return;
		}

		for (Hero hero : heroes.values()) {
			if (hero == null) {
				continue;
			}

			caculateProp(hero, player);
		}

	}

	public boolean isEmbattleHero(Player player, int heroId) {
		List<Integer> embattleHero = player.getEmbattleList();
		for (Integer elem : embattleHero) {
			if (elem == heroId) {
				return true;
			}
		}

		return false;
	}

	// 更新英雄属性
	public void updateHero(Player player, Hero hero, int reason) {
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null!");
			return;
		}

		int currentSoldier = hero.getCurrentSoliderNum();
		int propSoldier = hero.getSoldierNum();
		if (currentSoldier <= propSoldier) {
			return;
		}

		RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
		int diff = currentSoldier - propSoldier;
		if (diff <= 0) {
			return;
		}
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		Soldier soldier = soldierMap.get(getSoldierType(hero.getHeroId()));
		if (soldier == null) {
			return;
		}
		hero.setCurrentSoliderNum(propSoldier);
		soldierManager.addSoldier(player, soldier, diff, Reason.UpdateHero);
		builder.addHeroChange(hero.createHeroChange());
		builder.addSoldierInfo(soldier.wrapPb());
		if (player.isLogin && player.getChannelId() != -1) {
			SynHelper.synMsgToPlayer(player, RolePb.SynChangeRq.EXT_FIELD_NUMBER, RolePb.SynChangeRq.ext, builder.build());
		}
	}

	// 获取所有上阵英雄战力
	public Property getTotalProperty(Player player) {
		Property totalProperty = new Property();
		List<Integer> embattleList = player.getEmbattleList();
		for (Integer heroId : embattleList) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}
			caculateProp(hero, player);
			Property property = hero.getTotalProp();
			if (property != null) {
				totalProperty.add(property);
			}
		}

		return totalProperty;
	}

	public Property getOmmentProperty(Player player, Property totalOmamentProperty) {
		Map<Integer, PlayerOmament> OmamentList = player.getPlayerOmaments();
		for (Map.Entry<Integer, PlayerOmament> omament : OmamentList.entrySet()) {
			int omamentId = omament.getValue().getOmamentId();
			Property property = omamentManager.findOmamentProperty(omamentId);
			if (property != null) {
				totalOmamentProperty.add(property);
			}
		}
		return totalOmamentProperty;
	}

	// 获取所有上阵英雄配饰战力
	public Property getOmmentProperty(Player player) {
		Property totalOmamentProperty = new Property();
		Map<Integer, PlayerOmament> OmamentList = player.getPlayerOmaments();
		for (Map.Entry<Integer, PlayerOmament> omament : OmamentList.entrySet()) {
			int omamentId = omament.getValue().getOmamentId();
			Property property = omamentManager.findOmamentProperty(omamentId);
			if (property != null) {
				totalOmamentProperty.add(property);
			}
		}
		return totalOmamentProperty;
	}

	// 计算武将的战斗力, 两部分组成[英雄+建筑]
	public int caculateBattleScore(Player player) {
		Property property = getTotalProperty(player);

		double attackFactor = (double) staticLimitMgr.getNum(77) / DevideFactor.PERCENT_NUM;
		double defenceFactor = (double) staticLimitMgr.getNum(76) / DevideFactor.PERCENT_NUM;
		double soldierFactor = (double) staticLimitMgr.getNum(78) / DevideFactor.PERCENT_NUM;
		double strongAttackFactor = (double) staticLimitMgr.getNum(219) / DevideFactor.PERCENT_NUM;
		double strongDefenceFactor = (double) staticLimitMgr.getNum(220) / DevideFactor.PERCENT_NUM;
		double attackCityFactor = (double) staticLimitMgr.getNum(221) / DevideFactor.PERCENT_NUM;
		double defenceCityFactor = (double) staticLimitMgr.getNum(222) / DevideFactor.PERCENT_NUM;
		double critiFactor = (double) staticLimitMgr.getNum(272) / DevideFactor.PERCENT_NUM;
		double missFactor = (double) staticLimitMgr.getNum(273) / DevideFactor.PERCENT_NUM;

		BattleProperty bookBattleTotalProperty = warBookManager.getBookBattleTotalProperty(player);

		// 攻城 守城 强攻 强防 属性加成
		double other = strongAttackFactor * property.getStrongAttack() + strongDefenceFactor * property.getStrongDefence() + attackCityFactor * property.getAttackCity() + defenceCityFactor * property.getDefenceCity() + bookBattleTotalProperty.getCriti() * critiFactor + bookBattleTotalProperty.getMiss() * missFactor;

		// 武将部分的战力
		int battleScore = (int) ((double) property.getAttack() * attackFactor + (double) property.getDefence() * defenceFactor + (double) property.getSoldierNum() * soldierFactor + other);
		player.setBattleScore(battleScore);

		// 建筑的战力
		int buildingScore = player.getBuildingScore(); // 英雄加建筑
		int total = battleScore + buildingScore;
		rankManager.checkRankList(player.getLord()); // 检查排行榜
		tdManager.openBouns(player);
		player.setMaxScore(total);

		HeroPb.SynScoreRs.Builder builder = HeroPb.SynScoreRs.newBuilder();
		builder.setScore(player.getMaxScore());
		GameServer.getInstance().sendMsgToPlayer(player, PbHelper.createSynBase(HeroPb.SynScoreRs.EXT_FIELD_NUMBER, HeroPb.SynScoreRs.ext, builder.build()));
		return total;
	}

	public int calcHeroBattleScore(Player player, List<Hero> heroList) {
		Property property = new Property();
		for (Hero hero : heroList) {
			caculateProp(hero, player);
			Property heroProperty = hero.getTotalProp();
			if (heroProperty != null) {
				property.add(heroProperty);
			}
		}
		double attackFactor = (double) staticLimitMgr.getNum(77) / DevideFactor.PERCENT_NUM;
		double defenceFactor = (double) staticLimitMgr.getNum(76) / DevideFactor.PERCENT_NUM;
		double soldierFactor = (double) staticLimitMgr.getNum(78) / DevideFactor.PERCENT_NUM;
		double strongAttackFactor = (double) staticLimitMgr.getNum(219) / DevideFactor.PERCENT_NUM;
		double strongDefenceFactor = (double) staticLimitMgr.getNum(220) / DevideFactor.PERCENT_NUM;
		double attackCityFactor = (double) staticLimitMgr.getNum(221) / DevideFactor.PERCENT_NUM;
		double defenceCityFactor = (double) staticLimitMgr.getNum(222) / DevideFactor.PERCENT_NUM;
		double critiFactor = (double) staticLimitMgr.getNum(272) / DevideFactor.PERCENT_NUM;
		double missFactor = (double) staticLimitMgr.getNum(273) / DevideFactor.PERCENT_NUM;

		BattleProperty bookBattleTotalProperty = warBookManager.getBookBattleTotalProperty(player);

		// 攻城 守城 强攻 强防 属性加成
		double other = strongAttackFactor * property.getStrongAttack() + strongDefenceFactor * property.getStrongDefence() + attackCityFactor * property.getAttackCity() + defenceCityFactor * property.getDefenceCity() + bookBattleTotalProperty.getCriti() * critiFactor + bookBattleTotalProperty.getMiss() * missFactor;
		// 武将部分的战力
		int battleScore = (int) ((double) property.getAttack() * attackFactor + (double) property.getDefence() * defenceFactor + (double) property.getSoldierNum() * soldierFactor + other);
		return battleScore;
	}

	// 同步玩家改变的英雄信息和战力
	public void synBattleScoreAndHeroList(Player player, List<Hero> heroes) {
		if (heroes != null && !heroes.isEmpty()) {
			caculateBattleScore(player);
			checkHeroList(player, heroes);
		}
	}

	// 同步玩家改变的英雄信息和战力
	public void synBattleScoreAndHeroList(Player player, Hero hero) {
		if (hero != null) {
			caculateBattleScore(player);
			ArrayList<Hero> list = new ArrayList<>();
			list.add(hero);
			checkHeroList(player, list);
		}
	}

	// 异步向客户端同步英雄列表
	public void checkHeroList(Player player, List<Hero> heroes) {
		if (heroes == null) {
			return;
		}
		HeroPb.SynHeroRs.Builder synHeroPb = HeroPb.SynHeroRs.newBuilder();
		heroes.forEach(e -> {
			if (e != null) {
				// 计算英雄属性
				heroManager.caculateProp(e, player);
				synHeroPb.addHero(e.wrapPb());
			}
		});
		GameServer.getInstance().sendMsgToPlayer(player, PbHelper.createSynBase(HeroPb.SynHeroRs.EXT_FIELD_NUMBER, HeroPb.SynHeroRs.ext, synHeroPb.build()));
	}

	public boolean heroWear(Hero hero, int equipId) {
		List<HeroEquip> heroEquips = hero.getHeroEquips();
		if (heroEquips == null) {
			return false;
		}

		for (HeroEquip heroEquip : heroEquips) {
			if (heroEquip.getEquip() == null) {
				continue;
			}

			if (heroEquip.getEquip().getEquipId() == equipId) {
				return true;
			}
		}

		return false;
	}

	public boolean allHeroWearEquip(Player player, int equipId) {
		for (Integer heroId : player.getEmbattleList()) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}

			if (heroWear(hero, equipId)) {
				return true;
			}
		}

		return false;
	}

	public int getSoldierType(int heroId) {
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(heroId);
		if (staticHero == null) {
			LogHelper.CONFIG_LOGGER.info("hero config is null, heroId = " + heroId);
			return 1;
		}

		return staticHero.getSoldierType();
	}

	public String getHeroName(int heroId) {
		StaticHero staticHero = staticHeroDataMgr.getStaticHero(heroId);
		if (staticHero == null) {
			LogHelper.CONFIG_LOGGER.info("hero config is null, heroId = " + heroId);
			return "";
		}

		return staticHero.getHeroName();
	}

	// 检查英雄状态
	public boolean checkHero(Hero hero, ClientHandler handler) {
		if (hero == null) {
			return false;
		}
		if (!hero.isActivated()) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IS_NOT_ACTIVATE);
			return false;
		}

		return true;
	}

	public int getQuality(int heroId) {
		StaticHero config = staticHeroDataMgr.getStaticHero(heroId);
		if (config == null) {
			return 1;
		}

		return config.getQuality();
	}

	public int getQualityNum(Player player, int quality) {
		int count = 0;
		Iterator<Hero> it = player.getHeros().values().iterator();
		while (it.hasNext()) {
			Hero next = it.next();
			if (next == null) {
				continue;
			}
			StaticHero staticHero = staticHeroDataMgr.getStaticHero(next.getHeroId());
			if (staticHero != null && staticHero.getQuality() == quality) {
				count++;
			}
		}
		return count;
	}

	public int getQualityWashMax(Player player, int quality) {
		int count = 0;
		Iterator<Hero> it = player.getHeros().values().iterator();
		while (it.hasNext()) {
			Hero next = it.next();
			if (next == null) {
				continue;
			}
			StaticHero staticHero = staticHeroDataMgr.getStaticHero(next.getHeroId());
			if (staticHero == null || staticHero.getQuality() < quality) {
				continue;
			}

			// 洗练满的判断
			Property qualifyProp = next.getQualifyProp();
			int currentAttri = qualifyProp.getAttack() + qualifyProp.getDefence() + qualifyProp.getSoldierNum();
			int initExtra = staticHero.getInitExtra();
			int maxConfig = staticHero.getMaxTotal();
			if (currentAttri >= initExtra + maxConfig) {
				count++;
			}

		}
		return count;
	}

	/**
	 * 只计算攻城守城属性添加
	 *
	 * @param hero
	 * @param player
	 * @param fatherEntityType
	 * @param isAttacker
	 */
	public void caculateCityProp(Hero hero, Player player, int fatherEntityType, boolean isAttacker) {
		if (hero == null) {
			LogHelper.CONFIG_LOGGER.info("hero is null");
			return;
		}

		Property total = hero.getTotalProp();
		//处理下攻城守城属性就好了
		int totalAttack = total.getAttack();
		int totalDefence = total.getDefence();
		if (isAttacker) {
			total.addAttackValue(total.getAttackCity());
		} else {
			//兵书技能加成
			Integer heroWarBookSkillEffect = warBookManager.getHeroWarBookSkillEffect(hero.getHeroBooks(), hero.getHeroId(), BookEffectType.BOOK_EFFECT_11);
			if (null != heroWarBookSkillEffect) {
				total.addAttackValue(total.getAttackCity());
//                System.err.println("攻城拔寨技能:攻城属性在守城时也生效");
			}
			total.addDefenceValue(total.getDefenceCity());
		}
		// LogHelper.GAME_DEBUG.error("addAttack = " + addAttack);
		// LogHelper.GAME_DEBUG.error("addDefence = " + addDefence);
		//虫族入侵buff //处理攻击防御加成
		if (fatherEntityType == BattleEntityType.ROIT_MONSTER) {
			SimpleData data = player.getSimpleData();
			if (data != null) {
				Integer riotAttack = data.getRiotBuff().get(RiotBuff.ATTACK);
				Integer riotDefence = data.getRiotBuff().get(RiotBuff.DEFENCE);
				float riotAddAttack = riotAttack == null ? 0 : riotAttack / 100f;
				float riotAddDefence = riotDefence == null ? 0 : riotDefence / 100f;
				int addRiotAttack = Math.round(totalAttack * riotAddAttack);
				int addRiotDefence = Math.round(totalDefence * riotAddDefence);
				total.addAttackValue(addRiotAttack);
				total.addDefenceValue(addRiotDefence);
			}
		}

	}

	// 1.英雄属性是否洗满
	public boolean isHeroWashFull(Hero hero) {
		if (hero == null) {
			return false;
		}

		StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		if (staticHero == null) {
			return false;
		}

		// 洗练满的判断
		Property qualifyProp = hero.getQualifyProp();
		int currentAttri = qualifyProp.getAttack() + qualifyProp.getDefence() + qualifyProp.getSoldierNum();
		int initExtra = staticHero.getInitExtra();
		int maxConfig = staticHero.getMaxTotal();
		if (currentAttri >= initExtra + maxConfig) {
			return true;
		}

		return false;
	}


	public void chechHeroWashMax(Player player) {
		Iterator<Hero> it = player.getHeros().values().iterator();
		while (it.hasNext()) {
			Hero next = it.next();
			if (next == null) {
				continue;
			}
			StaticHero staticHero = staticHeroDataMgr.getStaticHero(next.getHeroId());
			if (staticHero == null) {
				continue;
			}

			// 洗练满的判断
			Property qualifyProp = next.getQualifyProp();
			int currentAttri = qualifyProp.getAttack() + qualifyProp.getDefence() + qualifyProp.getSoldierNum();
			int initExtra = staticHero.getInitExtra();
			int maxConfig = staticHero.getMaxTotal();
			if (currentAttri >= initExtra + maxConfig) {
				player.updateWashHeroNum(staticHero.getHeroId(), staticHero.getQuality());
			}

		}
	}

	public int caculateBattleScore(Property property) {
		if (null != property) {
			double attackFactor = (double) staticLimitMgr.getNum(77) / DevideFactor.PERCENT_NUM;
			double defenceFactor = (double) staticLimitMgr.getNum(76) / DevideFactor.PERCENT_NUM;
			double soldierFactor = (double) staticLimitMgr.getNum(78) / DevideFactor.PERCENT_NUM;
			double strongAttackFactor = (double) staticLimitMgr.getNum(219) / DevideFactor.PERCENT_NUM;
			double strongDefenceFactor = (double) staticLimitMgr.getNum(220) / DevideFactor.PERCENT_NUM;
			double attackCityFactor = (double) staticLimitMgr.getNum(221) / DevideFactor.PERCENT_NUM;
			double defenceCityFactor = (double) staticLimitMgr.getNum(222) / DevideFactor.PERCENT_NUM;
//            double critiFactor = (double) staticLimitMgr.getNum(272) / DevideFactor.PERCENT_NUM;
//            double missFactor = (double) staticLimitMgr.getNum(273) / DevideFactor.PERCENT_NUM;
			// 武将部分的战力
			int battleScore = (int) ((double) property.getAttack() * attackFactor + (double) property.getDefence() * defenceFactor
				+ (double) property.getSoldierNum() * soldierFactor
				+ (double) property.getStrongAttack() * strongAttackFactor + (double) property.getStrongDefence() * strongDefenceFactor
				+ (double) property.getAttackCity() * attackCityFactor + (double) property.getDefenceCity() * defenceCityFactor);
			return battleScore;
		} else {
			return 0;
		}
	}

	public HeroBook getWarBook(Hero hero, int bookType) {
		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		for (HeroBook hroBook : heroBooks) {
			if (hroBook == null) {
				continue;
			}

			WarBook book = hroBook.getBook();
			if (book == null) {
				continue;
			}

			int bookId = book.getBookId();
			StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(bookId);
			if (staticWarBook == null) {
				continue;
			}

			if (staticWarBook.getType() == bookType) {
				return hroBook;
			}
		}
		return null;
	}

	/**
	 * @Description 检查上场英雄是否重复
	 * @Param [player, targetHeroId]
	 * @Return com.game.constant.GameError
	 * @Date 2021/7/26 11:35
	 **/
	public GameError judgeHeroRepeat(Player player, int targetHeroId) {
		// 检查英雄是否存在
		// 有无英雄
		Hero hero = player.getHeros().get(targetHeroId); // 要上阵的英雄Id
		if (hero == null) {
			return GameError.HERO_NOT_EXISTS;
		}
		// 英雄正在行军之中
		if (player.isInMarch(hero)) {
			return GameError.HERO_IN_MARCH;
		}
		if (player.isInMass(targetHeroId)) {
			return GameError.HERO_IN_MASS;
		}
		if (!hero.isActivated()) {
			return GameError.HERO_IS_NOT_ACTIVATE;
		}
		if (player.hasPvpHero(targetHeroId)) {
			return GameError.HERO_IN_PVP_BATTLE;
		}
		for (Integer heroId : player.getEmbattleList()) {
			if (heroId.intValue() == targetHeroId) {
				return GameError.HERO_ALREADY_EMBATTLE;
			}
		}
		for (Integer heroId : player.getMiningList()) {
			if (heroId.intValue() == targetHeroId) {
				return GameError.HERO_ALREADY_COLLECTED;
			}
		}
		for (WarDefenseHero warDefenseHero : player.getDefenseArmyList()) {
			if (warDefenseHero.getHeroId() == targetHeroId) {
				return GameError.HERO_ALREADY_DEFENSE_ARMY;
			}
		}

		return GameError.OK;
	}

	/**
	 * heroType -> heroId
	 */
	public int getHeroIdByType(int heroType, Player player) {
		int heroId = 0;
		Map<Integer, Hero> heros = player.getHeros();
		for (Hero hero : heros.values()) {
			if (staticHeroDataMgr.getHeroType(hero.getHeroId()) == heroType) {
				heroId = hero.getHeroId();
			}
		}
		return heroId;
	}

	public List<Integer> checkHeroChange(Player player, List<Integer> heroList) {
		List<Integer> list = new ArrayList<>();
		Map<Integer, Hero> heroMap = player.getHeros();
		for (Integer heroId : heroList) {
			if (heroId == 0 || heroMap.containsKey(heroId)) {
				list.add(heroId);
				continue;
			}

			StaticHero staticHero = staticHeroDataMgr.getStaticHero(heroId);
			List<StaticHero> typeList = staticHeroDataMgr.getHerTypeList(staticHero.getHeroType());
			list.add(heroChanageId(heroMap, typeList, heroId));
		}
		return list;
	}

	public Integer heroChanageId(Map<Integer, Hero> heroMap, List<StaticHero> typeList, int heroId) {
		if (typeList == null || typeList.isEmpty()) {
			return heroId;
		}
		for (StaticHero staticHero : typeList) {
			if (heroMap.containsKey(staticHero.getHeroId())) {
				return staticHero.getHeroId();
			}
		}
		return heroId;
	}

	/**
	 * 武将突破
	 *
	 * @param player
	 * @param hero
	 */
	public void addDivineAdvanceRecord(Player player, Hero hero) {
		if (hero.getDiviNum() < 1) {
			return;
		}
		divineAdvanceHeros.put(player.getRoleId(), hero.getHeroId(), hero);
	}
}
