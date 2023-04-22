package com.game.manager;

import com.game.constant.*;
import com.game.dataMgr.StaticBuildingMgr;
import com.game.dataMgr.StaticTechMgr;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticEmployee;
import com.game.domain.s.StaticTechInfo;
import com.game.domain.s.StaticTechType;
import com.game.log.consumer.EventManager;
import com.game.pb.CommonPb;
import com.game.pb.MailPb.GetMailReportRs;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TechManager {

    @Autowired
    private StaticBuildingMgr staticBuildingMgr;

    @Autowired
    private BuildingManager buildingManager;

    @Autowired
    private StaticTechMgr staticTechMgr;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private HeroManager heroManager;

    @Autowired
    private EquipManager equipManager;
    @Autowired
    private DailyTaskManager dailyTaskManager;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private BroodWarManager broodWarManager;

    public TechInfo getTechLevelInfo(Player player, int techType) {
        if (player == null) {
            LogHelper.CONFIG_LOGGER.error("TechManager: player is null!");
            return null;
        }

        Tech tech = player.getTech();
        if (tech == null) {
            LogHelper.CONFIG_LOGGER.error("TechManager: tech is null!");
            return null;
        }

        TechInfo techInfo = tech.getTechInfo(techType);
        return techInfo;
    }

    public int getTechLevel(Player player, int techType) {
        TechInfo techInfo = getTechLevelInfo(player, techType);
        if (techInfo == null) {
            return 0;
        }
        return techInfo.getLevel();
    }

    private List<List<Integer>> getEffectValue(Player player, int techType) {
        TechInfo techInfo = getTechLevelInfo(player, techType);
        List<List<Integer>> ret = new ArrayList<List<Integer>>();
        if (techInfo == null) {
            return ret;
        }

        int techLevel = techInfo.getLevel();
        StaticTechInfo staticTechInfo = staticTechMgr.getStaticTechLevel(techType, techLevel);
        if (staticTechInfo == null) {
            return ret;
        }

        List<List<Integer>> effectValue = staticTechInfo.getEffectValue();
        if (effectValue == null) {
            return ret;
        }

        return effectValue;
    }

    // 获取资源百分比加成(eg. = 0.5)
    public double getResource(Player player, int techType, int techEffectId) {
        List<List<Integer>> effectValue = getEffectValue(player, techType);
        if (effectValue == null || effectValue.size() <= 0) {
            //LogHelper.ERROR_LOGGER.error("TechManager: effect value is null");
            return 0.0;
        }

        if (effectValue.size() != 1) {
            LogHelper.CONFIG_LOGGER.error("TechManager: effectValue size is not 1!");
            return 0.0;
        }

        List<Integer> action = effectValue.get(0);
        if (action.size() != 2) {
            LogHelper.CONFIG_LOGGER.error("TechManager: Resource action size is not 2!");
            return 0.0;
        }

        int id = action.get(0);
        int value = action.get(1);
        if (id != techEffectId) {
            LogHelper.CONFIG_LOGGER.error("TechManager: techEffectId error, techEffectId = " + techEffectId + ", config id = " + id);
            return 0.0;
        }

        return (double) value / DevideFactor.PERCENT_NUM;
    }

    // 获取资源辅助
    public long getResourceUtil(Player player, int techType, int techEffectId, int resourceType) {
        double percent = getResource(player, techType, techEffectId);
        long num = 0L;
        if (resourceType == ResourceType.IRON) {
            num = buildingManager.getBaseIron(player);
        } else if (resourceType == ResourceType.COPPER) {
            num = buildingManager.getBaseCopper(player);
        } else if (resourceType == ResourceType.OIL) {
            num = buildingManager.getBaseOil(player);
        } else if (resourceType == ResourceType.STONE) {
            num = buildingManager.getBaseStone(player);
        }
        double result = percent * (double) num;
        //母巢职位加成
        double commandBuff = broodWarManager.getCommandBuff(player, techEffectId);
        double cmdResult = commandBuff * num;
        return (long) (result + cmdResult);
    }

    // 增加生铁
    public long getIron(Player player) {
        return getResourceUtil(player, TechType.IRON, TechEffectId.ADD_IRON_PERCENT, ResourceType.IRON);
    }

    // 增加铜
    public long getCopper(Player player) {
        return getResourceUtil(player, TechType.COPPER, TechEffectId.ADD_COPPER_PERCENT, ResourceType.COPPER);
    }

    // 增加石油
    public long getOil(Player player) {
        return getResourceUtil(player, TechType.OIL, TechEffectId.ADD_OIL_PERCENT, ResourceType.OIL);
    }

    // 增加宝石
    public long getStone(Player player) {
        return getResourceUtil(player, TechType.STONE, TechEffectId.ADD_STONE_PERCENT, ResourceType.STONE);
    }

    // 1.科技加成
    public CommonPb.Resource.Builder getTechAdd(Player player) {
        CommonPb.Resource.Builder techAdd = CommonPb.Resource.newBuilder();
        long ironAdd = getIron(player);
        long copperAdd = getCopper(player);
        long oilAdd = getOil(player);
        long stoneAdd = getStone(player);
        techAdd.setIron(ironAdd);
        techAdd.setCopper(copperAdd);
        techAdd.setOil(oilAdd);
        techAdd.setStone(stoneAdd);
        return techAdd;
    }

    // 获取每分钟募兵数量
    public double getSoldierNum(Player player, int techType, int techEffectId) {
        List<List<Integer>> effectValue = getEffectValue(player, techType);
        if (effectValue == null || effectValue.size() <= 0) {
            //LogHelper.ERROR_LOGGER.error("TechManager: effect value is null");
            return 0L;
        }

        if (effectValue.size() != 1) {
            LogHelper.CONFIG_LOGGER.error("TechManager: effectValue size is not 1!");
            return 0L;
        }

        List<Integer> action = effectValue.get(0);
        if (action.size() != 2) {
            LogHelper.CONFIG_LOGGER.error("TechManager: Resource action size is not 2!");
            return 0L;
        }

        int id = action.get(0);
        int value = action.get(1);
        if (id != techEffectId) {
            LogHelper.CONFIG_LOGGER.error("TechManager: techEffectId error, techEffectId = " + techEffectId + ", config id = " + id);
            return 0L;
        }

        return (double) value / DevideFactor.PERCENT_NUM;
    }

    // 3.增加武将攻击力
//    public Property getHeroProperty(Player player, int soldierType,Property property) {
//        List<List<Integer>> ret = null;
//        List<List<Integer>> ret1 = null;
//        if (soldierType == SoldierType.ROCKET_TYPE) {
//            ret = getPropertyUtil(player, TechType.ROCKET_ATTACK);
//            ret1=getPropertyUtil(player, TechType.ROCKET_ATTACK_STAGE);
//        } else if (soldierType == SoldierType.TANK_TYPE) {
//            ret = getPropertyUtil(player, TechType.TANK_ATTACK);
//            ret1=getPropertyUtil(player, TechType.TANK_ATTACK_STAGE);
//        } else if (soldierType == SoldierType.WAR_CAR) {
//            ret = getPropertyUtil(player, TechType.WARCAR_ATTACK);
//            ret1=getPropertyUtil(player, TechType.WARCAR_ATTACK_STAGE);
//
//        }
//        if (null != ret && !ret.isEmpty()) {
//            for (List<Integer> effect : ret) {
//                if (null != effect && effect.size() == 2) {
//                    property.addValue(effect.get(0),effect.get(1));
//                }
//            }
//        }
//        if (null != ret1 && !ret1.isEmpty()) {
//            for (List<Integer> effect : ret1) {
//                if (null != effect && effect.size() == 2) {
//                    property.addValue(effect.get(0),effect.get(1));
//                }
//            }
//        }
//        return property;
//    }
    public Property getHeroProperty(Player player, int soldierType, Property property) {
        if (soldierType == SoldierType.ROCKET_TYPE) {
            getPropertyUtil(player, TechType.ROCKET_ATTACK, property);
            getPropertyUtil(player, TechType.ROCKET_ATTACK_STAGE, property);
        } else if (soldierType == SoldierType.TANK_TYPE) {
            getPropertyUtil(player, TechType.TANK_ATTACK, property);
            getPropertyUtil(player, TechType.TANK_ATTACK_STAGE, property);
        } else if (soldierType == SoldierType.WAR_CAR) {
            getPropertyUtil(player, TechType.WARCAR_ATTACK, property);
            getPropertyUtil(player, TechType.WARCAR_ATTACK_STAGE, property);
        }
        return property;
    }

    // 2.获取最终的募兵速度
    public double getSoldierSpeed(Player player, int soldierType) {
        double ret = 0.0;
        if (soldierType == SoldierType.ROCKET_TYPE) {
            ret = getSoldierNum(player, TechType.ROCKET_SOLDIER_NUM, TechEffectId.ADD_ROCKET_SOLDIER_NUM);
        } else if (soldierType == SoldierType.TANK_TYPE) {
            ret = getSoldierNum(player, TechType.TANK_SOLDIER_NUM, TechEffectId.ADD_TANK_SOLDIER_NUM);
        } else if (soldierType == SoldierType.WAR_CAR) {
            ret = getSoldierNum(player, TechType.WARCAR_SOLDIER_NUM, TechEffectId.ADD_WARCAR_SOLDIER_NUM);
        }
        return ret;
    }

    public Property getPropertyUtil(Player player, int techType, Property property) {
        List<List<Integer>> effectValue = getEffectValue(player, techType);
        if (effectValue == null || effectValue.size() <= 0) {
            return null;
        }
        for (List<Integer> effectId : effectValue) {
            if (effectId.size() != 2) {
                LogHelper.CONFIG_LOGGER.error("TechManager: Resource action size is not 2!");
                return null;
            }
            int id = effectId.get(0);
            if (id == TechEffectId.ADD_ROCKET_ATTACK || id == TechEffectId.ADD_TANK_ATTACK || id == TechEffectId.ADD_WARCAR_ATTACK) {
                property.addValue(PropertyType.ATTCK, effectId.get(1));
            } else if (id == TechEffectId.ADD_ROCKET_DEFENCE || id == TechEffectId.ADD_TANK_DEFENCE || id == TechEffectId.ADD_WARCAR_DEFENCE) {
                property.addValue(PropertyType.DEFENCE, effectId.get(1));
            } else if (id == TechEffectId.ADD_ROCKET_ATTACKCITY || id == TechEffectId.ADD_TANK_ATTACKCITY || id == TechEffectId.ADD_WARCAR_ATTACKCITY) {
                property.addValue(PropertyType.ATTACK_CITY, effectId.get(1));
            } else if (id == TechEffectId.ADD_ROCKET_DEFENCECITY || id == TechEffectId.ADD_TANK_DEFENCECITY || id == TechEffectId.ADD_WARCAR_DEFENCECITY) {
                property.addValue(PropertyType.DEFENCE_CITY, effectId.get(1));
            } else if (id == TechEffectId.ADD_ROCKET_STRONGATTACK || id == TechEffectId.ADD_TANK_STRONGATTACK || id == TechEffectId.ADD_WARCAR_STRONGATTACK) {
                property.addValue(PropertyType.STRONG_ATTACK, effectId.get(1));
            } else if (id == TechEffectId.ADD_ROCKET_STRONGDEFENCE || id == TechEffectId.ADD_TANK_STRONGDEFENCE || id == TechEffectId.ADD_WARCAR_STRONGDEFENCE) {
                property.addValue(PropertyType.STRONG_DEFENCE, effectId.get(1));
            }
        }
        return property;
    }

    // 增加募兵需要的石油
    public int getOil(Player player, int techType, int techEffectId) {
        List<List<Integer>> effectValue = getEffectValue(player, techType);
        if (effectValue == null || effectValue.size() <= 0) {
            //LogHelper.ERROR_LOGGER.error("TechManager: effect value is null");
            return 0;
        }

	/*	// 应该有两项,第一项是攻击力, 第二项是石油
		if (effectValue.size() != 2) {
			LogHelper.ERROR_LOGGER.error("TechManager: effectValue size is not 1!");
			return 0;
		}

		List<Integer> action = effectValue.get(1);
		if (action.size() != 2) {
			LogHelper.ERROR_LOGGER.error("TechManager: Resource action size is not 2!");
			return 0;
		}

		int id = action.get(0);
		int value = action.get(1);*/
        //动态获取配置,粮食的消耗
        int value = 0;
        boolean contains = false;
        for (List<Integer> effect : effectValue) {
            if (null != effect && effect.size() == 2) {
                if (effect.get(0) == techEffectId) {
                    value = effect.get(1);
                    contains = true;
                    break;
                }
            }
        }

		/*if (id != techEffectId) {
			LogHelper.ERROR_LOGGER.error("TechManager: techEffectId error, techEffectId = " + techEffectId + ", config id = " + id);
			return 0;
		}*/
        if (!contains) {
            LogHelper.CONFIG_LOGGER.error("TechManager: techEffectId error, techEffectId = " + techEffectId + ", config id = " + techEffectId);
        }

        return value;
    }

    // 增加石油
    public int getOilUtil(Player player, int techType, int techEffectId) {
        int oil = getOil(player, techType, techEffectId);
        return oil;
    }

    // 4.增加石油
    public int getOil(Player player, int soldierType) {
        int ret = 0;
        if (soldierType == SoldierType.ROCKET_TYPE) {
            ret = getOilUtil(player, TechType.ROCKET_ATTACK, TechEffectId.COST_OIL);
        } else if (soldierType == SoldierType.TANK_TYPE) {
            ret = getOilUtil(player, TechType.TANK_ATTACK, TechEffectId.COST_OIL);
        } else if (soldierType == SoldierType.WAR_CAR) {
            ret = getOilUtil(player, TechType.WARCAR_ATTACK, TechEffectId.COST_OIL);
        }
        return ret;
    }

    // 只有一个参数的效果, 返回整形
    public int getSingleEffect(Player player, int techType, int techEffectId) {
        List<List<Integer>> effectValue = getEffectValue(player, techType);
        if (effectValue == null || effectValue.size() <= 0) {
            return 0;
        }

        if (effectValue.size() != 1) {
            LogHelper.CONFIG_LOGGER.error("TechManager: effectValue size is not 1!");
            return 0;
        }

        List<Integer> action = effectValue.get(0);
        if (action.size() != 2) {
            LogHelper.CONFIG_LOGGER.error("TechManager: Resource action size is not 2!");
            return 0;
        }

        int id = action.get(0);
        int value = action.get(1);
        if (id != techEffectId) {
            LogHelper.CONFIG_LOGGER.error("TechManager: techEffectId error, techEffectId = " + techEffectId + ", config id = " + id);
            return 0;
        }

        return value;
    }

    // 暴击倍率
    public int getCritiEffect(Player player, int techType, int techEffectId) {
        List<List<Integer>> effectValue = getEffectValue(player, techType);
        if (effectValue == null || effectValue.size() <= 0) {
            //LogHelper.ERROR_LOGGER.error("TechManager: effect value is null");
            return 1;
        }

        if (effectValue.size() != 1) {
            LogHelper.CONFIG_LOGGER.error("TechManager: effectValue size is not 1!");
            return 1;
        }

        List<Integer> action = effectValue.get(0);
        if (action.size() != 2) {
            LogHelper.CONFIG_LOGGER.error("TechManager: Resource action size is not 2!");
            return 1;
        }

        int id = action.get(0);
        int value = action.get(1);
        if (id != techEffectId) {
            LogHelper.CONFIG_LOGGER.error("TechManager: techEffectId error, techEffectId = " + techEffectId + ", config id = " + id);
            return 1;
        }

        value = Math.max(1, value);

        return value;
    }


    // 5.增加兵排数+2
    public int getSoldierLine(Player player) {
        int primarySoldierLine = getSingleEffect(player, TechType.PRIMARY_SOLDIER_LINE, TechEffectId.ADD_SOLDIER_LINE);
        int middleSoldierLine = getSingleEffect(player, TechType.MIDDLE_SOLDIER_LINE, TechEffectId.ADD_SOLDIER_LINE);
        return primarySoldierLine + middleSoldierLine;
    }

    // 6.增加武将个数 +1
    public int getHeroNum(Player player) {
        int primaryHeroNum = getSingleEffect(player, TechType.PRIMARY_HERO_NUM, TechEffectId.ADD_HERO_NUM);
        int middleHeroNum = getSingleEffect(player, TechType.MIDDLE_HERO_NUM, TechEffectId.ADD_HERO_NUM);
        return primaryHeroNum + middleHeroNum;
    }

    // 7.增加行军速度
    public double getArmySp(Player player) {
        int value = getSingleEffect(player, TechType.MARCH, TechEffectId.ADD_MARCH_SPEED);
        double res = value / 100.0f;
        //res = Math.min(1.0, res);
        res = Math.max(0.0, res);
        return res;

    }

    // 只有一个参数的效果, 返回百分比
    public double getSinglePercent(Player player, int techType, int techEffectId) {
        List<List<Integer>> effectValue = getEffectValue(player, techType);
        if (effectValue == null || effectValue.size() <= 0) {
            //LogHelper.ERROR_LOGGER.error("TechManager: effect value is null");
            return 0.0;
        }

        if (effectValue.size() != 1) {
            LogHelper.CONFIG_LOGGER.error("TechManager: effectValue size is not 1!");
            return 0.0;
        }

        List<Integer> action = effectValue.get(0);
        if (action.size() != 2) {
            LogHelper.CONFIG_LOGGER.error("TechManager: Resource action size is not 2!");
            return 0.0;
        }

        int id = action.get(0);
        int value = action.get(1);
        if (id != techEffectId) {
            LogHelper.CONFIG_LOGGER.error("TechManager: techEffectId error, techEffectId = " + techEffectId + ", config id = " + id);
            return 0.0;
        }

        return (double) value / DevideFactor.PERCENT_NUM;
    }

    // 8.减少建筑升级时间
    public double getBuildReduce(Player player) {
        return getSinglePercent(player, TechType.BUILD_SPEED, TechEffectId.ADD_BUILD_SPEED);
    }

    // 9.增加容量
    public double getWareCapacity(Player player) {
        return getSinglePercent(player, TechType.WARE_CAPACITY, TechEffectId.ADD_WARE_CAPACITY);
    }

    // 10.降低生产耗时
    public float getWorkTime(Player player) {
        return (float) getSinglePercent(player, TechType.WORK_SPEED, TechEffectId.ADD_WORK_SPEED);
    }

    // 11.开启秘技 1开启 0 未开启
    public boolean isSpecialSkillOpen(Player player) {
        int specialSkill = getSingleEffect(player, TechType.SPECIAL_SKILL, TechEffectId.ADD_SPECIAL_SKILL);
        return specialSkill == 1;
    }

    // 12.开启极品装备[千分比], 打造的时候生成带秘技的装备
    public int getSpecialEquip(Player player) {
        return getSingleEffect(player, TechType.SPECIAL_EQUIP, TechEffectId.ADD_SPECIAL_EQUIP);
    }

    // 13.拆建筑 1 可以拆除 0 不能拆除
    public int getRebuild(Player player) {
        return getSingleEffect(player, TechType.REBUILD, TechEffectId.ADD_REBUILD);
    }

    // 14.采集加成
    public double getCollectSpeed(Player player) {
        return getSinglePercent(player, TechType.COLLECT_SPEED, TechEffectId.ADD_COLLECT_SPEED);
    }

    // 15.突破成功率
    public double getAdvanceRate(Player player) {
        return getSinglePercent(player, TechType.HERO_ADVANCE, TechEffectId.ADD_HERO_ADVANCE_RATE);
    }

    // 16.增加获得威望百分比
    public double getHonorPercent(Player player) {
        return getSinglePercent(player, TechType.HONOR, TechEffectId.ADD_HONOR_PERCENT);
    }

    public double getHonorAdd(long lordId) {
        Player player = playerManager.getPlayer(lordId);
        if (player == null) {
            return 0.0f;
        }

        return getHonorPercent(player);
    }


    // 17.暴击倍数
    public int getCriti(Player player) {
        return getCritiEffect(player, TechType.COUNTRY_ITEM_CRITI, TechEffectId.ADD_CRITI_TIMES);
    }

    // 18.增加侦察成功率
    public int getScoutLevel(Player player) {
        return getSingleEffect(player, TechType.SCOUT, TechEffectId.ADD_SCOUT_RATE);
    }


    // 19.是否自动补兵
    public int getAutoSoldier(Player player) {
        return getSingleEffect(player, TechType.AUTO_ADD_SOLDIER, TechEffectId.ADD_AUTO_SOLDIER);
    }

    public TechInfo createTechInfo(Player player, int techType) {
        TechInfo techInfo = new TechInfo();
        StaticTechType staticTechType = staticTechMgr.getStaticTechType(techType);
        if (staticTechType != null) {
            techInfo.setLevel(staticTechType.getMinLevel());
        } else {
            techInfo.setLevel(1);
        }
        techInfo.setTechType(techType);
        techInfo.setProcess(0);
        Tech tech = player.getTech();
        Map<Integer, TechInfo> techInfoMap = tech.getTechInfoMap();
        techInfoMap.put(techType, techInfo);

        return techInfo;
    }

    public CommonPb.TechInfo.Builder wrapTechPb(TechInfo techInfo) {
        CommonPb.TechInfo.Builder builder = CommonPb.TechInfo.newBuilder();
        int techType = techInfo.getTechType();
        StaticTechInfo staticTechInfo = staticTechMgr.getStaticTechLevel(techType, techInfo.getLevel());
        if (staticTechInfo != null) {
            techType = staticTechInfo.getTechType();
        }

        builder.setTechType(techType);
        builder.setTechLevel(techInfo.getLevel());
        builder.setProcess(techInfo.getProcess());
        return builder;
    }


    public CommonPb.TechQue.Builder wrapTechQuePb(Player player, TechQue techQue) {
        CommonPb.TechQue.Builder builder = CommonPb.TechQue.newBuilder();
        TechInfo techInfo = getTechLevelInfo(player, techQue.getTechType());
        if (techInfo == null) {
            LogHelper.CONFIG_LOGGER.error("techInfo is null, techType :" + techQue.getTechType());
            return builder;
        }

        builder.setLevel(techInfo.getLevel());
        int techLv = techQue.getLevel();
        if (techLv == 0) {
            techLv = 1;
        }

        StaticTechInfo staticTechInfo = staticTechMgr.getStaticTechLevel(techQue.getTechType(), techLv);
        if (staticTechInfo != null) {
            builder.setPeriod(staticTechInfo.getUpTime() * TimeHelper.SECOND_MS);
        }
        builder.setType(techQue.getTechType());
        builder.setEndTime(techQue.getEndTime());
        builder.setSpeed(techQue.getSpeed());
        builder.setSpeedTime(techQue.getSpeedTime());
        builder.setActivityDerateCD(techQue.getActivityDerateCD());
        return builder;
    }

    // 科技更新进度
    public void updateTechProcess(TechInfo techInfo, int techType) {
        techInfo.setProcess(techInfo.getProcess() + 1);
    }

    // 科技更新等级
    public void updateTechLevel(Player player, TechInfo techInfo, int techType) {
        techInfo.setLevel(techInfo.getLevel() + 1);
        techInfo.setProcess(0);

        if (isAttackTech(techType)) {
            heroManager.synBattleScoreAndHeroList(player,player.getAllHeroList());
        }

        if (techType == TechType.SPECIAL_SKILL) {
            equipManager.checkSpecialSkill(player);
        }
    }

    public boolean isAttackTech(int techType) {
        return techType == TechType.ROCKET_ATTACK ||
                techType == TechType.TANK_ATTACK ||
                techType == TechType.WARCAR_ATTACK ||
                techType == TechType.ROCKET_ATTACK_STAGE ||
                techType == TechType.TANK_ATTACK_STAGE ||
                techType == TechType.WARCAR_ATTACK_STAGE;
    }

    public TechInfo getTechInfo(Player player, int techType) {
        Tech tech = player.getTech();
        if (tech == null) {
            return null;
        }

        Map<Integer, TechInfo> techInfoMap = tech.getTechInfoMap();
        for (Map.Entry<Integer, TechInfo> elem : techInfoMap.entrySet()) {
            if (elem == null) {
                continue;
            }

            TechInfo techInfo = elem.getValue();
            if (techInfo == null) {
                continue;
            }

            int curType = techInfo.getTechType();
            if (curType == techType) {
                return techInfo;
            }
        }

        return null;
    }


    public void addTechInfo(TechInfo techInfo, Player player) {
        Tech tech = player.getTech();
        if (tech == null) {
            LogHelper.CONFIG_LOGGER.error("tech is null!");
            return;
        }

        Map<Integer, TechInfo> techInfoMap = tech.getTechInfoMap();
        if (techInfoMap == null) {
            LogHelper.CONFIG_LOGGER.error("techInfoMap is null!");
            return;
        }

        if (techInfoMap.get(techInfo.getTechType()) != null) {
            return;
        }

        techInfoMap.put(techInfo.getTechType(), techInfo);
    }

    // 检查科技的前置Id是否满足条件
    public boolean isPreOk(int techType, Player player) {
        Tech tech = player.getTech();
        if (tech == null) {
            LogHelper.CONFIG_LOGGER.error("tech == null");
            return false;
        }

        Map<Integer, TechInfo> techInfoMap = tech.getTechInfoMap();
        if (techInfoMap == null) {
            LogHelper.CONFIG_LOGGER.error("techInfoMap == null");
            return false;
        }


        Map<Integer, List<Integer>> preTechs = staticTechMgr.getPreTechs();

        boolean isOk = false;
        for (Map.Entry<Integer, List<Integer>> elem : preTechs.entrySet()) {
            if (elem == null) {
                continue;
            }

            List<Integer> value = elem.getValue();
            if (value == null) {
                continue;
            }


            if (value.size() != 3) {
                LogHelper.CONFIG_LOGGER.error("value.size() != 3");
                continue;
            }

            if (techType != value.get(0)) {
                continue;
            }


            TechInfo techInfo = techInfoMap.get(elem.getKey());
            if (techInfo == null) {
                continue;
            }


            if (techInfo.getLevel() >= value.get(2)) {
                return true;
            }
        }

        if (!isOk) {
            return true;
        }

        return false;

    }

    public void handlePlayerEmbatlle(Player player, int techType) {
        List<Integer> embattleList = player.getEmbattleList();
        if (techType == TechType.PRIMARY_HERO_NUM) {
            if (embattleList.get(2) == -1) {
                embattleList.set(2, 0);
            }
        } else if (techType == TechType.MIDDLE_HERO_NUM) {
            if (embattleList.get(3) == -1) {
                embattleList.set(3, 0);
            }
        }
    }

    public static void main(String[] args) {
        List<Integer> embattleList = new ArrayList<Integer>();
        embattleList.add(0);
        embattleList.add(0);
        embattleList.add(-1);
        embattleList.add(-1);

        int heroNum = 3;
        int curHeroNum = 0;
        for (int i = 0; i < embattleList.size(); i++) {
            if (embattleList.get(i) != -1) {
                curHeroNum++;
            }
        }

        // curHeroNUm = 3
        for (int i = curHeroNum; i < heroNum && i < embattleList.size(); i++) {
            embattleList.set(i, 0);
        }

        //System.out.println(embattleList);

    }

    // 是否有高级科研官
    public boolean hasTecher(Player player) {
        Lord lord = player.getLord();
        if (lord.getVipTech() == 0) {
            return false;
        }

        EmployInfo employ = player.getEmployInfo();
        if (employ == null) {
            return false;
        }

        Employee researcher = employ.getResearcher();
        if (researcher == null) {
            return false;
        }


        StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(researcher.getEmployeeId());
        if (staticEmployee == null) {
            return false;
        }

        return staticEmployee.getCostGold() > 0;
    }

    public boolean isTechBuilding(Player player) {
        long now = System.currentTimeMillis();
        Tech tech = player.getTech();
        if (tech == null) {
            LogHelper.CONFIG_LOGGER.error("tech is null!");
            return false;
        }

        // 是否正在建造科技
        int buildingId = tech.getBuildingId();
        // 建造队列剩余时间
        BuildQue buildQue = player.getBuildQue(buildingId);
        if (buildQue != null) {
            long buildQueEndTime = buildQue.getEndTime();
            // 科技馆正在建造
            if (buildQueEndTime > now) {
                return true;
            }
        }

        return false;
    }

    public boolean isTechResearching(Player player) {
        long now = System.currentTimeMillis();
        Tech tech = player.getTech();
        if (tech == null) {
            LogHelper.CONFIG_LOGGER.error("tech is null!");
            return false;
        }

        LinkedList<TechQue> techQues = tech.getTechQues();
        for (TechQue techQue : techQues) {
            if (techQue == null) {
                continue;
            }

            long endTime = techQue.getEndTime();
            if (endTime > now) {
                return true;
            }
        }

        return false;
    }


    /**
     * 是否有完成的科技
     *
     * @return
     */
    public boolean isHasSuccessTech(Player player) {
        long now = System.currentTimeMillis();
        Tech tech = player.getTech();
        Iterator<TechQue> it = tech.getTechQues().iterator();
        while (it.hasNext()) {
            TechQue techQue = it.next();
            if (now > techQue.getEndTime()) {
                return true;
            }
        }
        return false;
    }


    public void checkHeroTech(Player player) {
        int primaryHeroNum = getSingleEffect(player, TechType.PRIMARY_HERO_NUM, TechEffectId.ADD_HERO_NUM);
        int middleHeroNum = getSingleEffect(player, TechType.MIDDLE_HERO_NUM, TechEffectId.ADD_HERO_NUM);
        if (primaryHeroNum != 0) {
            handlePlayerEmbatlle(player, TechType.PRIMARY_HERO_NUM);
        }

        if (middleHeroNum != 0) {
            handlePlayerEmbatlle(player, TechType.MIDDLE_HERO_NUM);
        }

    }

    public GetMailReportRs.Builder addTechLv(Mail mail) {
        Player left = playerManager.getPlayer(mail.getReport().getLeftHead().getName());
        Player right = playerManager.getPlayer(mail.getReport().getRightHead().getName());

        CommonPb.ReportMsg.Builder reportMsg = mail.getReportMsg().toBuilder();
        CommonPb.FightBefore.Builder fightB = addTechLv(left, right, reportMsg.getFightBefore().toBuilder());
        CommonPb.FightIn.Builder fightI = addTechLv(left, right, reportMsg.getFightIn().toBuilder());
        reportMsg.clearFightBefore().setFightBefore(fightB);
        reportMsg.clearFightIn().setFightIn(fightI);
        GetMailReportRs.Builder builder = GetMailReportRs.newBuilder();
        builder.setReportMsg(reportMsg);
        return builder;
    }

    public CommonPb.FightBefore.Builder addTechLv(Player left, Player right, CommonPb.FightBefore.Builder fightBefore) {
        List<CommonPb.BattleEntity> listL = addTechLvToBattleEntity(left, fightBefore.getLeftEntitiesList());
        List<CommonPb.BattleEntity> listR = addTechLvToBattleEntity(right, fightBefore.getRightEntitiesList());
        fightBefore.clearLeftEntities().addAllLeftEntities(listL)
                .clearRightEntities().addAllRightEntities(listR);
        return fightBefore;
    }

    public CommonPb.FightIn.Builder addTechLv(Player left, Player right, CommonPb.FightIn.Builder fightin) {
        List<CommonPb.AttackInfo> listL = addTechLvToAttackInfo(left, fightin.getLeftInfoList());
        List<CommonPb.AttackInfo> listR = addTechLvToAttackInfo(right, fightin.getRightInfoList());
        fightin.clearLeftInfo().addAllLeftInfo(listL)
                .clearRightInfo().addAllRightInfo(listR);
        return fightin;
    }


    public List<CommonPb.BattleEntity> addTechLvToBattleEntity(Player player, List<CommonPb.BattleEntity> list) {
        List<CommonPb.BattleEntity> newList = new ArrayList<>();
        for (CommonPb.BattleEntity battleEntity : list) {
            int level = 0;
            if (battleEntity.getEntityType() == BattleEntityType.HERO
                    || battleEntity.getEntityType() == BattleEntityType.FRIEND_HERO) {
                TechInfo info = getTechLevelInfo(player, getTechType(battleEntity.getSoldierType()));
                level = info == null ? 0 : info.getLevel();
            }
            CommonPb.BattleEntity.Builder builder = battleEntity.toBuilder();
            builder.setTechLv(level);
            newList.add(builder.build());
        }
        return newList;
    }

    public List<CommonPb.AttackInfo> addTechLvToAttackInfo(Player player, List<CommonPb.AttackInfo> list) {
        List<CommonPb.AttackInfo> newList = new ArrayList<>();
        for (CommonPb.AttackInfo attackInfo : list) {
            int level = 0;
            if (attackInfo.getEntityType() == BattleEntityType.HERO
                    || attackInfo.getEntityType() == BattleEntityType.FRIEND_HERO) {
                TechInfo info = getTechLevelInfo(player, getTechType(heroManager.getSoldierType(attackInfo.getEntityId())));
                level = info == null ? 0 : info.getLevel();
            }
            CommonPb.AttackInfo.Builder builder = attackInfo.toBuilder();
            builder.setTechLv(level);
            newList.add(builder.build());
        }
        return newList;
    }

    public int getTechType(int type) {
        int ret = 0;
        if (type == SoldierType.ROCKET_TYPE) {
            ret = TechType.ROCKET_ATTACK;
        } else if (type == SoldierType.TANK_TYPE) {
            ret = TechType.TANK_ATTACK;
        } else if (type == SoldierType.WAR_CAR) {
            ret = TechType.WARCAR_ATTACK;
        }
        return ret;
    }
}
