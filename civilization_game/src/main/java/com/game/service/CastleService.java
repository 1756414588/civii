package com.game.service;

import java.util.*;

import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.CountryData;
import com.game.domain.p.*;
import com.game.domain.s.StaticHero;
import com.game.domain.s.StaticMeetingCommand;
import com.game.domain.s.StaticMeetingSoldier;
import com.game.domain.s.StaticMeetingTask;
import com.game.manager.*;
import com.game.message.handler.cs.*;
import com.game.pb.CastlePb;
import com.game.pb.WorldPb;
import com.game.util.SynHelper;
import com.game.worldmap.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.domain.Player;
import com.game.message.handler.ClientHandler;
import com.game.pb.CastlePb.GetMiningHeroListRs;
import com.game.pb.CastlePb.MiningDownRq;
import com.game.pb.CastlePb.MiningDownRs;
import com.game.pb.CastlePb.MiningUpRq;
import com.game.pb.CastlePb.MiningUpRs;
import com.game.pb.CommonPb;
import com.game.util.LogHelper;

@Service
public class CastleService {

    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private HeroManager heroManager;
    @Autowired
    private SoldierManager soldierManager;
    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private StaticMeetingTaskMgr staticMeetingTaskMgr;
    @Autowired
    private WorldManager worldManager;

    @Autowired
    private StaticMeetingCommandMgr staticMeetingCommandMgr;

    @Autowired
    private CountryManager countryManager;

    @Autowired
    private StaticMeetingSoldierMgr staticMeetingSoldierMgr;
    @Autowired
    private StaticHeroMgr staticHeroMgr;
    @Autowired
    private TechManager techManager;
    @Autowired
    private StaticOpenManger staticOpenManger;

    /**
     * 检查参谋部是否开启
     *
     * @param player
     * @return
     */
    private GameError checkOpenStatff(Player player) {
        if (!staticOpenManger.isOpen(OpenConsts.OPEN_40, player)) {
            return GameError.COMMAND_LV_NOT_ENOUGH;
        }
        return GameError.OK;
    }

    /**
     * 检查采集部队或者城防部队是否开启
     *
     * @param player
     * @param armyType
     * @return
     */
    private GameError checkOpenByType(Player player, int armyType) {
        List<Integer> addition;
        if (armyType == CastleConsts.DEFENSEARMY) {
            addition = staticLimitMgr.getAddtion(CastleConsts.CONDITION_177);
        } else {
            addition = staticLimitMgr.getAddtion(CastleConsts.CONDITION_176);
        }
        int commndLevle = player.getBuilding(BuildingType.STAFF).getLevel();
        if (commndLevle < addition.get(0)) {
            return GameError.STAFF_LEVEL_NOT_ENOUGH;
        }
        if (player.getLevel() < addition.get(1)) {
            return GameError.LORD_LV_NOT_ENOUGH;
        }
        return GameError.OK;
    }

    /**
     * 获取采集上阵武将列表
     */
    public void getMiningHeroListRq(CastlePb.GetMiningHeroListRq re, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("getMiningHeroListRq error :player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Staff staff = player.buildings.getStaff();
        if (staff.getBuildingId() == 0 || staff.getLv() == 0) {
            LogHelper.CONFIG_LOGGER.info("getMiningHeroListRq error :staff is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        if (re.getArmyType() != CastleConsts.MINING && re.getArmyType() != CastleConsts.DEFENSEARMY) {
            LogHelper.CONFIG_LOGGER.equals("getMiningHeroListRq error :ArmyType is error ");
            return;
        }
        GameError gameError = checkOpenStatff(player);
        if (gameError != GameError.OK) {
            LogHelper.CONFIG_LOGGER.info("getMiningHeroListRq error : " + gameError.toString());
            handler.sendErrorMsgToPlayer(gameError);
            return;
        }
        gameError = checkOpenByType(player, re.getArmyType());
        if (gameError != GameError.OK) {
            LogHelper.CONFIG_LOGGER.info("getMiningHeroListRq error : " + gameError.toString());
            handler.sendErrorMsgToPlayer(gameError);
            return;
        }
        GetMiningHeroListRs.Builder builder = GetMiningHeroListRs.newBuilder();
        // 从新计算开启条件
        if (re.getArmyType() == CastleConsts.MINING) {
            playerManager.createMiningList(player);
        } else {
            playerManager.createDefanceArmyList(player);
        }
        List<Integer> miningList = player.getMeetingArmy(re.getArmyType());
        if (CollectionUtils.isNotEmpty(miningList)) {
            builder.addAllHeroId(miningList);
        }
        builder.setArmyType(re.getArmyType());
        handler.sendMsgToPlayer(GameError.OK, GetMiningHeroListRs.ext, builder.build());
        if (re.getArmyType() == CastleConsts.DEFENSEARMY) {
            updateDefenseSoldierByPlayer(player);
            SynHelper.synMsgToPlayer(player, CastlePb.SynUpdateDefenseSoldierRq.EXT_FIELD_NUMBER, CastlePb.SynUpdateDefenseSoldierRq.ext, synUpdateDefenseSoldierRq(player).build());
        }
    }

    /***
     * 检查坑位是否开启
     *
     * @param armyType
     * @param index    从1开始
     * @return
     */
    private GameError checkUpMiningHero(int armyType, int index, Player player) {
        List<Integer> addition;
        if (armyType == CastleConsts.DEFENSEARMY) {
            addition = staticLimitMgr.getAddtion(CastleConsts.CONDITION_188);
        } else {
            addition = staticLimitMgr.getAddtion(CastleConsts.CONDITION_187);
        }
        if (player.getLevel() < addition.get(index - 1)) {
            return GameError.LORD_LV_NOT_ENOUGH;
        }
        return GameError.OK;
    }

    public void upMiningHeroRq(MiningUpRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        GameError gameErrorJudge = heroManager.judgeHeroRepeat(player, req.getHeroId());
        if (gameErrorJudge != GameError.OK) {
            handler.sendErrorMsgToPlayer(gameErrorJudge);
            return;
        }
        // 检查index是否存在
        List<Integer> miningList = player.getMeetingArmy(req.getArmyType());
        if (req.getIndex() > 4 || req.getIndex() < 0) {
            LogHelper.CONFIG_LOGGER.info("CastleService upMiningHeroRq error embattle index = " + req.getIndex());
            handler.sendErrorMsgToPlayer(GameError.ERROR_MINING_INDEX);
            return;
        }
        GameError gameError = checkUpMiningHero(req.getArmyType(), req.getIndex(), player);
        if (gameError != GameError.OK) {
            LogHelper.CONFIG_LOGGER.info("CastleService upMiningHeroRq  GameError :" + gameError.toString());
            handler.sendErrorMsgToPlayer(gameError);
            return;
        }
        gameError = checkOpenByType(player, req.getArmyType());
        if (gameError != GameError.OK) {
            LogHelper.CONFIG_LOGGER.info("CastleService upMiningHeroRq error : " + gameError.toString());
            handler.sendErrorMsgToPlayer(gameError);
            return;
        }
        Map<Integer, Hero> heros = player.getHeros();
        int heroId = req.getHeroId();
        Hero hero = heros.get(heroId); // 要上阵的英雄Id

        MiningUpRs.Builder builder = MiningUpRs.newBuilder();
        // 是否替换武将装备
        int curHeroId = miningList.get(req.getIndex() - 1);

        Hero currentHero = heros.get(curHeroId);
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

            // 当前的兵力还回兵营
//			int soldierNum = currentHero.getCurrentSoliderNum();
//			currentHero.setCurrentSoliderNum(0);
//			StaticHero staticHero = staticHeroMgr.getStaticHero(curHeroId);
//			playerManager.addAward(player, AwardType.SOLDIER, staticHero.getSoldierType(), soldierNum, Reason.AUTO_KILL);

            if (req.getIsExchangeEquip()) {
                // 当前位置英雄Id 装备替换
                currentHero.swapEquip(hero);
                builder.setIsExchangeEquip(true);
                heroManager.caculateProp(hero, player);
                heroManager.caculateProp(currentHero, player);
                CommonPb.EquipExChange currentEquip = heroManager.wrapEquipExchange(currentHero);
                CommonPb.EquipExChange changeEquip = heroManager.wrapEquipExchange(hero);
                builder.addExchangeInfo(currentEquip);
                builder.addExchangeInfo(changeEquip);
            } else {
                builder.setIsExchangeEquip(false);
            }
        } else {
            heroManager.caculateProp(hero, player);
        }

        if (currentHero == null) { // 上阵的位置为空,直接补兵, 或者上阵的位置用英雄
            handlerSingleSoldier(hero, player, builder, req.getArmyType());
        } else {
            // 补兵
            handlerSoldier(currentHero, hero, player, builder, req.getArmyType());
        }
        //int beforeHeroId = miningList.get(req.getIndex() - 1);
        //if (req.getArmyType() == 2) {
        //	handleDefence(player, heroId, beforeHeroId);
        //}
        player.updateMeetingHero(req.getIndex() - 1, heroId, req.getArmyType());
        builder.addAllEmbattleHero(player.getMeetingArmy(req.getArmyType()));
        builder.setArmyType(req.getArmyType());
        if (!builder.hasIsExchangeEquip()) {
            builder.setIsExchangeEquip(false);
        }
        handler.sendMsgToPlayer(GameError.OK, MiningUpRs.ext, builder.build());
        // TODO 如果是城防军要给客户端更新城防军信息
//        if (req.getArmyType() == CastleConsts.DEFENSEARMY) {
//            updateDefenseSoldierByPlayer(player);
//            handler.sendMsgToPlayer(GameError.OK, CastlePb.UpdateDefenseSoldierRs.ext, updateDefenseSoldierRs(player).build());
//        }
    }

    public void handlerSingleSoldier(Hero hero, Player player, MiningUpRs.Builder builder, int type) {
        int heroSoliderType = heroManager.getSoldierType(hero.getHeroId());
        if (type != CastleConsts.DEFENSEARMY) {

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
        }
        CommonPb.HeroSoldier.Builder heroSoldier = CommonPb.HeroSoldier.newBuilder();
        heroSoldier.setHeroId(hero.getHeroId());
        heroSoldier.setSoldier(hero.getCurrentSoliderNum());
        builder.addHeroSoldier(heroSoldier);

        CommonPb.Soldier.Builder soldier = CommonPb.Soldier.newBuilder();
        soldier.setSoldierType(heroSoliderType);
        soldier.setNum(soldierManager.getSoldierNum(player, heroSoliderType));
        builder.addSoldier(soldier);
    }

    public void handlerSoldier(Hero currentHero, Hero hero, Player player, MiningUpRs.Builder builder, int type) {
        if (currentHero == null || hero == null) {
            // LogHelper.CONFIG_LOGGER.info("currentHero is null or hero is null!");
            return;
        }

        // 获得当前的英雄兵力
        int heroSoliderNum = currentHero.getCurrentSoliderNum();
        // 获得需要交换的武将的类型
        int currentSoldierType = heroManager.getSoldierType(currentHero.getHeroId());
        int changeSoldierType = heroManager.getSoldierType(hero.getHeroId());
        // 将多余的兵力还回去 城防军是不用还的 放上去的兵力就为0
        if (type != CastleConsts.DEFENSEARMY) {
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

            if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
                diff -= hero.getCurrentSoliderNum() - hero.getSoldierNum();
                hero.setCurrentSoliderNum(hero.getSoldierNum());
                LogHelper.CONFIG_LOGGER.info("hero.getCurrentSoliderNum() = " + hero.getCurrentSoliderNum() + ", hero.getSoldierNum() = " + hero.getSoldierNum());
            }

            // 武将补兵之后从兵营扣除兵力
            playerManager.subAward(player, AwardType.SOLDIER, changeSoldierType, diff, Reason.EMBATTLE);
        }
        currentHero.setCurrentSoliderNum(0);
        CommonPb.HeroSoldier.Builder currentHeroSoldier = CommonPb.HeroSoldier.newBuilder();
        currentHeroSoldier.setHeroId(currentHero.getHeroId());
        currentHeroSoldier.setSoldier(currentHero.getCurrentSoliderNum());

        CommonPb.HeroSoldier.Builder changeHeroSoldier = CommonPb.HeroSoldier.newBuilder();
        changeHeroSoldier.setHeroId(hero.getHeroId());
        changeHeroSoldier.setSoldier(hero.getCurrentSoliderNum());

        builder.addHeroSoldier(currentHeroSoldier);
        builder.addHeroSoldier(changeHeroSoldier);

        // 两个兵营的情况
        if (currentSoldierType != changeSoldierType) { // 如果两个英雄类型不一致
            CommonPb.Soldier.Builder soldier1 = CommonPb.Soldier.newBuilder();
            soldier1.setSoldierType(changeSoldierType);
            soldier1.setNum(soldierManager.getSoldierNum(player, changeSoldierType));

            CommonPb.Soldier.Builder soldier2 = CommonPb.Soldier.newBuilder();
            soldier2.setSoldierType(currentSoldierType);
            soldier2.setNum(soldierManager.getSoldierNum(player, currentSoldierType));
            builder.addSoldier(soldier1);
            builder.addSoldier(soldier2);

        } else {
            CommonPb.Soldier.Builder soldier1 = CommonPb.Soldier.newBuilder();
            soldier1.setSoldierType(changeSoldierType);
            soldier1.setNum(soldierManager.getSoldierNum(player, changeSoldierType));
            builder.addSoldier(soldier1);
        }
    }

    // 处理城防军,如果当前城防军有这个英雄则替换英雄，如果没有则放到后面
    public void handleDefence(Player player, int addHeroId, int beforeHeroId) {
        //Wall wall = player.getWall();
        //List<Integer> defenceHero = wall.getDefenceHero();
        //if (beforeHeroId <= 0) { // -1 0,
        //	defenceHero.add(addHeroId);
        //} else { // 有上阵的武将
        //	boolean found = false;
        //	int index = -1;
        //	for (int i = 0; i < defenceHero.size(); i++) {
        //		Integer heroId = defenceHero.get(i);
        //		if (heroId == null) {
        //			LogHelper.CONFIG_LOGGER.info("hero is null, in handle Defence");
        //			continue;
        //		}
        //		if (beforeHeroId == heroId) {
        //			found = true;
        //			index = i;
        //			break;
        //		}
        //	}
        //
        //	if (found) {
        //		defenceHero.set(index, addHeroId);
        //	} else {
        //		defenceHero.add(addHeroId);
        //	}
        //}

    }

    public void downMiningHeroRq(MiningDownRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 检查index是否存在
        List<Integer> miningList = player.getMeetingArmy(req.getArmyType());
        if (req.getIndex() > 4 || req.getIndex() < 1) {
            LogHelper.CONFIG_LOGGER.info("error embattle index = " + req.getIndex());
            handler.sendErrorMsgToPlayer(GameError.ERROR_MINING_INDEX);
            return;
        }
        GameError gameError = checkUpMiningHero(req.getArmyType(), req.getIndex(), player);
        if (gameError != GameError.OK) {
            LogHelper.CONFIG_LOGGER.info("CastleService upMiningHeroRq  GameError :" + gameError.toString());
            handler.sendErrorMsgToPlayer(gameError);
            return;
        }

        // 检查英雄是否存在
       Map<Integer, Hero> heros = player.getHeros();
        // 有无英雄
        Hero hero = heros.get(miningList.get(req.getIndex() - 1)); // 要下阵的英雄
        if (hero == null) {
            LogHelper.CONFIG_LOGGER.info("hero not exists");
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        // 英雄正在行军之中
        if (player.isInMarch(hero)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
            return;
        }

        if (player.isInMass(hero.getHeroId())) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MASS);
            return;
        }

        if (!hero.isActivated()) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IS_NOT_ACTIVATE);
            return;
        }

        if (player.hasPvpHero(hero.getHeroId())) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_PVP_BATTLE);
            return;
        }
        // 获得当前武将的兵力
        int heroSoliderNum = hero.getCurrentSoliderNum();
        // 获得需要下阵的武将的类型
        int currentSoldierType = heroManager.getSoldierType(hero.getHeroId());
        if (req.getArmyType() != CastleConsts.DEFENSEARMY) {
            // 把该武将的兵力还回兵营
            playerManager.addAward(player, AwardType.SOLDIER, currentSoldierType, heroSoliderNum, Reason.EMBATTLE);
        }
        // 当前的兵力设置为0
        hero.setCurrentSoliderNum(0);

        CommonPb.HeroSoldier.Builder heroSoldier = CommonPb.HeroSoldier.newBuilder();
        heroSoldier.setHeroId(hero.getHeroId());
        heroSoldier.setSoldier(hero.getCurrentSoliderNum());

        CommonPb.Soldier.Builder soldier = CommonPb.Soldier.newBuilder();
        soldier.setSoldierType(currentSoldierType);
        soldier.setNum(soldierManager.getSoldierNum(player, currentSoldierType));
        player.updateMeetingHero(req.getIndex() - 1, 0, req.getArmyType());
        //int i = req.getIndex() - 1;
        //WarDefenseHero warDefenseHero = player.getDefenseArmyList().get(i);
        //if (warDefenseHero != null) {
        //    warDefenseHero.reset();
        //}
        MiningDownRs.Builder builder = MiningDownRs.newBuilder();
        builder.setHeroSoldier(heroSoldier);
        builder.setSoldier(soldier);
        builder.setArmyType(req.getArmyType());
        builder.addAllEmbattleHero(player.getMeetingArmy(req.getArmyType()));
        handler.sendMsgToPlayer(GameError.OK, MiningDownRs.ext, builder.build());
    }

    /**
     * 获取指挥部当前任务
     *
     * @param handler
     */
    public void getGetMeetingTask(GetMeetingTaskHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        GameError gameError = checkMeetingOpen(player);
        if (gameError == GameError.OK) {
//            LogHelper.CONFIG_LOGGER.info("getGetMeetingTask error GameError: " + gameError.toString());
//            handler.sendErrorMsgToPlayer(gameError);
//            return;
            MeetingTask meetingTask = player.getMeetingTask();
            synchronized (meetingTask) {
                if (meetingTask.getId() == 0) {
                    StaticMeetingTask staticMeetingTask = staticMeetingTaskMgr.getStartMeetingTask();
                    meetingTask.setId(staticMeetingTask.getId());
                    meetingTask.setProcess(0);
                    meetingTask.setState(MeetingTaskStateConsts.NOT_OPEN);
                } else if (meetingTask.getState() == MeetingTaskStateConsts.OPEN) {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (meetingTask.getStartTime() == 0) {
                        meetingTask.setStartTime(currentTimeMillis);
                    }
                    if ((currentTimeMillis - meetingTask.getStartTime()) / (1000 * 3600 * 24) >= 7) {
                        StaticMeetingTask staticMeetingTask = staticMeetingTaskMgr.getStaticMeetingTask(meetingTask.getId());
                        if (staticMeetingTask != null) {
                            meetingTask.setState(MeetingTaskStateConsts.NEXT_STEP_OPEN);
                            meetingTask.setProcess(staticMeetingTask.getMonsterNum());
                        }
                    }
                }
                CountryData country = countryManager.getCountry(player.getCountry());
                CastlePb.GetMeetingTaskRs.Builder builder = CastlePb.GetMeetingTaskRs.newBuilder();
                builder.setId(meetingTask.getId());
                builder.setProcess(meetingTask.getProcess());
                builder.setState(meetingTask.getState());
                builder.setKillMonsterNum(country.getKillNum());
                builder.setLevel(staticMeetingSoldierMgr.getStaticMeetingTask(country.getKillNum()).getLevel());
                handler.sendMsgToPlayer(GameError.OK, CastlePb.GetMeetingTaskRs.ext, builder.build());
            }
        }
    }

    private GameError checkMeetingOpen(Player player) {
        List<Integer> addtions = staticLimitMgr.getAddtion(CastleConsts.CONDITION_178);
        BuildingBase baseBuilder = player.getBuilding(BuildingType.STAFF);
        if (baseBuilder == null) {
            return GameError.LORD_LV_NOT_ENOUGH;
        }
        int commndLevle = player.getBuilding(BuildingType.STAFF).getLevel();
        if (player.getLevel() < addtions.get(1)) {
            return GameError.LORD_LV_NOT_ENOUGH;
        }
        if (commndLevle < addtions.get(0)) {
            return GameError.STAFF_LEVEL_NOT_ENOUGH;
        }
        return GameError.OK;

    }

    /**
     * 激活任务
     *
     * @param handler
     * @param rq
     */
    public void openMeetingTask(OpenMeetingTaskHandler handler, CastlePb.OpenMeetingTaskRq rq) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int taskId = rq.getId();
        MeetingTask meetingTask = player.getMeetingTask();
        if (meetingTask.getId() != taskId && meetingTask.getState() != MeetingTaskStateConsts.NOT_OPEN) {
            LogHelper.CONFIG_LOGGER.info("MEETING_TASK_STATE_ERROR ");
            handler.sendErrorMsgToPlayer(GameError.MEETING_TASK_STATE_ERROR);
            return;
        }
        StaticMeetingTask staticMeetingTask = staticMeetingTaskMgr.getStaticMeetingTask(taskId);
//        if (player.getLevel() < staticMeetingTask.getLevel()) {
//            LogHelper.CONFIG_LOGGER.info("player level is not enough ");
//            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
//            return;
//        }
        meetingTask.setState(MeetingTaskStateConsts.OPEN);
        meetingTask.setStartTime(System.currentTimeMillis());
        // 任务刷怪逻辑
        List<Entity> list = new ArrayList<>();
        MapInfo mapInfo = worldManager.getMapInfo(worldManager.getMapId(player));
        for (int i = 0; i < staticMeetingTask.getMonsterNum(); i++) {

            Pos monsterPos = new Pos();
            // 周围24个格子刷满了,往外扩大一个格子,刷满或者刷到了5个格子,停止
            int num = staticLimitMgr.getNum(SimpleId.MEETING_MONSTER_CELL_NUM);
            int count = 0;
            while (count <= (num - CastleConsts.MEETING_MONSTER_CELL_NUM)) {
                monsterPos = mapInfo.randPos(player, CastleConsts.MEETING_MONSTER_CELL_NUM + count);
                count++;
                if (monsterPos != null && !monsterPos.isError()) {
                    break;
                }
            }
            if (monsterPos == null || monsterPos.isError()) {
                continue;
            }

            Monster monster = worldManager.addMonster(monsterPos, staticMeetingTask.getMonsterId(), staticMeetingTask.getLevel(), mapInfo, AddMonsterReason.ADD_MEETING_TASK_MONSTER);
            if (monster != null) {
                list.add(monster);
            }

        }
        worldManager.synEntityAddRq(list);
        SynHelper.synMsgToPlayer(player, CastlePb.SynUpdaeTaskRq.EXT_FIELD_NUMBER, CastlePb.SynUpdaeTaskRq.ext, synUpdateTaskRq(meetingTask).build());
    }

    /**
     * 指挥官任务
     *
     * @param monsterId
     * @throws Exception
     */
    public void doMeetingTask(int monsterId, Player self) throws Exception {
        if (!staticMeetingTaskMgr.checkTaskMonster(monsterId)) {
            return;
        }
        for (Player player : playerManager.getPlayers().values()) {
            if (player == null || player.getLord().getCountry() != self.getLord().getCountry()) {
                continue;
            }
            MeetingTask meetingTask = player.getMeetingTask();
            if (meetingTask == null) {
                continue;
            }
            if (meetingTask.getState() != MeetingTaskStateConsts.OPEN || meetingTask.getId() == 0) {
                continue;
            }
            StaticMeetingTask staticMeetingTask = staticMeetingTaskMgr.getStaticMeetingTask(meetingTask.getId());
            if (staticMeetingTask == null) {
                LogHelper.CONFIG_LOGGER.info("doMeetingTask error : StaticMeetingTask is not exist  meetingTask =" + meetingTask.toString());
                continue;
            }
            /**
             * 说明杀死的怪 不是任务怪
             */
            if (staticMeetingTask.getMonsterId() != monsterId) {
                continue;
            }
            CastlePb.SynUpdaeTaskRq.Builder builder;
            synchronized (meetingTask) {
                meetingTask.setProcess(meetingTask.getProcess() + 1);
                if (meetingTask.getProcess() >= staticMeetingTask.getMonsterNum()) {
                    meetingTask.setProcess(staticMeetingTask.getMonsterNum());
                    meetingTask.setState(MeetingTaskStateConsts.NEXT_STEP_OPEN);
                }
                builder = synUpdateTaskRq(meetingTask);
            }
            SynHelper.synMsgToPlayer(player, CastlePb.SynUpdaeTaskRq.EXT_FIELD_NUMBER, CastlePb.SynUpdaeTaskRq.ext, builder.build());
        }
    }

    private CastlePb.SynUpdaeTaskRq.Builder synUpdateTaskRq(MeetingTask meetingTask) {
        CastlePb.SynUpdaeTaskRq.Builder builder = CastlePb.SynUpdaeTaskRq.newBuilder();
        builder.setId(meetingTask.getId());
        builder.setProcess(meetingTask.getProcess());
        builder.setState(meetingTask.getState());
        return builder;
    }

    /**
     * 激活指挥学
     *
     * @param handler
     */
    public void openPointSoldiers(OpenPointSoldiersHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("OpenPointSoldiersHandler player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        MeetingTask meetingTask = player.getMeetingTask();
        if (meetingTask == null || meetingTask.getId() == 0) {
            return;
        }
        StaticMeetingTask staticMeetingTask = staticMeetingTaskMgr.getStaticMeetingTask(meetingTask.getId());
        if (staticMeetingTask == null) {
            LogHelper.CONFIG_LOGGER.info("openPointSoldiers error : StaticMeetingTask is not exist  meetingTask =" + meetingTask.toString());
            return;
        }
        if (meetingTask.getState() != MeetingTaskStateConsts.SUCCESS_NO_OPEN) {
            return;
        }
        if (player.getLevel() < staticMeetingTask.getLevel()) {
            LogHelper.CONFIG_LOGGER.info("player level is not enough ");
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }
        StaticMeetingCommand staticMeetingCommand = staticMeetingCommandMgr.getStaticMeetingCommand(staticMeetingTask.getAward());
        if (staticMeetingCommand == null) {
            LogHelper.CONFIG_LOGGER.info("openPointSoldiers  staticMeetingCommand  is null : staticMeetingCommandId =" + staticMeetingTask.getAward());
            return;
        }
        synchronized (meetingTask) {
            if (staticMeetingTask.getTrigger() != 0) {
                meetingTask.setId(staticMeetingTask.getTrigger());
                meetingTask.setState(MeetingTaskStateConsts.NOT_OPEN);
                meetingTask.setProcess(0);
            } else {
                meetingTask.setProcess(MeetingTaskStateConsts.SUCCESS_OPEN);
            }
        }
        SynHelper.synMsgToPlayer(player, CastlePb.SynUpdaeTaskRq.EXT_FIELD_NUMBER, CastlePb.SynUpdaeTaskRq.ext, synUpdateTaskRq(meetingTask).build());
        handler.sendMsgToPlayer(GameError.OK, CastlePb.GetSoldierLineRs.ext, getSoldierLineRs(meetingTask.getId()).build());
    }

    /**
     * 拿到指挥学加成的兵排
     *
     * @param handler
     */
    public void getSoldierLine(GetSoldierLineHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("GetSoldierLine player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        MeetingTask meetingTask = player.getMeetingTask();
        handler.sendMsgToPlayer(GameError.OK, CastlePb.GetSoldierLineRs.ext, getSoldierLineRs(meetingTask.getId()).build());
    }

    /**
     * 拿到兵排的回复，更新也推这个
     *
     * @param taskId
     * @return
     */
    private CastlePb.GetSoldierLineRs.Builder getSoldierLineRs(int taskId) {
        CastlePb.GetSoldierLineRs.Builder builder = CastlePb.GetSoldierLineRs.newBuilder();
        if (taskId == 0) {
            builder.addNum(0);
            builder.addNum(0);
            builder.addNum(0);
        } else {
            builder.addNum(staticMeetingTaskMgr.soldierNumByType(taskId, ArmyEnum.ARMY_ONE));
            builder.addNum(staticMeetingTaskMgr.soldierNumByType(taskId, ArmyEnum.ARMY_TWO));
            builder.addNum(staticMeetingTaskMgr.soldierNumByType(taskId, ArmyEnum.ARMY_THREE));
        }
        return builder;
    }

    /**
     * 点击进行下一步
     *
     * @param handler
     */
    public void OpenNextStep(OpenNextStepHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        MeetingTask meetingTask = player.getMeetingTask();
        StaticMeetingTask staticMeetingTask = staticMeetingTaskMgr.getStaticMeetingTask(meetingTask.getId());
        if ((meetingTask.getProcess() < staticMeetingTask.getMonsterNum())) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.MEETING_TASK_PROCESS_ERROR);
            return;
        }
        meetingTask.setState(MeetingTaskStateConsts.SUCCESS_NO_OPEN);
        // 拿到国家杀怪的数量
        CountryData country = countryManager.getCountry(player.getCountry());

        StaticMeetingSoldier staticMeetingSoldier = staticMeetingSoldierMgr.getStaticMeetingTask(country.getKillNum());
        int level = staticMeetingSoldier.getEffects().get(meetingTask.getId());
        CastlePb.OpenNextStepRs.Builder builder = CastlePb.OpenNextStepRs.newBuilder();
        builder.setLevel(staticMeetingTask.getLevel() - level);
        handler.sendMsgToPlayer(GameError.OK, CastlePb.OpenNextStepRs.ext, builder.build());
    }

    /**
     * 拿到
     *
     * @param handler
     */
    public void updateDefenseSoldier(UpdateDefenseSoldierHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("updateDefenseSoldier player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        updateDefenseSoldierByPlayer(player);
        handler.sendMsgToPlayer(GameError.OK, CastlePb.UpdateDefenseSoldierRs.ext, updateDefenseSoldierRs(player).build());
    }

    /**
     * 拿到当前正在自动恢复的hero
     *
     * @param player
     * @return
     */
    public WarDefenseHero getAutoAddSoldierHero(Player player) {
        WarDefenseHero warHero = null;
        for (WarDefenseHero warDefenseHero : player.getDefenseArmyList()) {
            // 这里主要是 给满兵的武将设置上次更新的时间是当前时间，避免战斗打完直接补满
            Hero hero = player.getHero(warDefenseHero.getHeroId());
            if (warDefenseHero.getHeroId() > 0 && warDefenseHero.isAddSoldier()) {
                warHero = warDefenseHero;
            }

            if (hero != null && hero.getCurrentSoliderNum() >= hero.getSoldierNum()) {
                warDefenseHero.setLastRefreshTime(System.currentTimeMillis());
            }
        }

        if (warHero != null) {
            WarDefenseHero surWarDefenseHero = getMinSurSolderHero(player);
            if (surWarDefenseHero != null && surWarDefenseHero.getHeroId() != warHero.getHeroId()) {
                warHero.setLastRefreshTime(System.currentTimeMillis());
                warHero.setAddSoldier(false);

                surWarDefenseHero.setAddSoldier(true);
                surWarDefenseHero.setLastRefreshTime(System.currentTimeMillis());
                return surWarDefenseHero;
            }
            return warHero;
        }

        for (WarDefenseHero warDefenseHero : player.getDefenseArmyList()) {
            if (warDefenseHero.getHeroId() > 0) {
                Hero hero = player.getHero(warDefenseHero.getHeroId());
                if (hero == null) {
                    continue;
                }
                if (hero.getCurrentSoliderNum() < hero.getSoldierNum()) {
                    warDefenseHero.setAddSoldier(true);
                    warDefenseHero.setLastRefreshTime(System.currentTimeMillis());
                    return warDefenseHero;
                }

            }
        }
        return null;

    }

    /**
     * 拿到剩余百分比最小的WarDefenseHero
     *
     * @param player
     * @return
     */
    public WarDefenseHero getMinSurSolderHero(Player player) {
        List<SurHeroSoldier> surHeroSoldiers = new ArrayList<>();
        for (WarDefenseHero warDefenseHero : player.getDefenseArmyList()) {
            Hero hero = player.getHero(warDefenseHero.getHeroId());
            if (hero == null) {
                continue;
            }
            int surSoldiers = hero.getSoldierNum() - hero.getCurrentSoliderNum();
            float percent = surSoldiers * 1.0f / hero.getSoldierNum();
            if (percent > 0 && percent < 1) {
                surHeroSoldiers.add(new SurHeroSoldier(hero.getHeroId(), percent));
            }
        }
        if (surHeroSoldiers.size() > 0) {
            // 要找出剩余百分比最少的hero
            Collections.sort(surHeroSoldiers);
            return player.getDefenseHero(surHeroSoldiers.get(0).getHeroId());
        }
        return null;
    }

    /**
     * 跟新城防部队补兵
     *
     * @param player
     */
    public void updateDefenseSoldierByPlayer(Player player) {
        WarDefenseHero warDefenseHero = getAutoAddSoldierHero(player);
        if (warDefenseHero == null) {
            return;
        }
        Hero hero = player.getHero(warDefenseHero.getHeroId());
        StaticHero staticHero = staticHeroMgr.getStaticHero(hero.getHeroId());
        int soldierPrice = staticLimitMgr.getStaticLimit().getRecruitOilCost();
        int techOil = techManager.getOil(player, staticHero.getSoldierType());
        if (techOil != 0) {
            soldierPrice = techOil;
        }
        // 当前最大兵力
        int maxSoldierNum = hero.getSoldierNum();
        // 每次恢复的兵力
        int _refreshNum = (int) Math.ceil(maxSoldierNum * 1.0f * CastleConsts.REFRESH_SOLDIER_PERCENT);
        // 武将当前的兵力
        int soldiers = hero.getCurrentSoliderNum();
        // 一共需要恢复的兵力
        int surSoldiers = (maxSoldierNum - soldiers >= 0 ? maxSoldierNum - soldiers : 0);
        if (surSoldiers == 0) {
            warDefenseHero.setAddSoldier(false);
            warDefenseHero.setLastRefreshTime(System.currentTimeMillis());
            return;
        }
        // 最大能刷新的次数
        int maxRefreshTime = (int) Math.ceil(surSoldiers * 1.0f / _refreshNum);
        // 下面计算出 时间间隔该玩家能恢复几次
        long curTime = System.currentTimeMillis();
        long time = curTime - warDefenseHero.getLastRefreshTime();
        int refreshTime = (int) (Math.floor(time * 1.0f / CastleConsts.REFRESH_SOLDIER_TIME));
        // 取出当前能刷新几次
        maxRefreshTime = Math.min(maxRefreshTime, refreshTime);
        // 城防军补满兵力完成的时间
        long completeTime = 0L;

        if (maxRefreshTime > 0) {
            // 每恢复5%消耗的石油
            int cost = soldierPrice * _refreshNum;
            // 计算出石油最大能恢复几次 向下取整
            int refreshTimeMax = (int) Math.floor(player.getOil() * 1.0f / cost);
            maxRefreshTime = Math.min(refreshTimeMax, maxRefreshTime);
            if (maxRefreshTime > 0) {
                int refreshNum = maxRefreshTime * _refreshNum;
                refreshNum = Math.min(surSoldiers, refreshNum);
                int totalCost = refreshNum * soldierPrice;
                // 扣除石油
                playerManager.subAward(player, AwardType.RESOURCE, ResourceType.OIL, totalCost, Reason.REFRESH_MEETING_HERO_SOLDIERS);
                hero.setCurrentSoliderNum(refreshNum + hero.getCurrentSoliderNum());
                // 获取城防军获取完成补兵时间
                if (hero.getCurrentSoliderNum() >= hero.getSoldierNum()) {
                    completeTime = warDefenseHero.getLastRefreshTime() + maxRefreshTime * CastleConsts.REFRESH_SOLDIER_TIME;
                }
                // 刷新城防军最后一次补兵时间
                warDefenseHero.setLastRefreshTime(curTime);
            }
        }

        int index = player.getDefenseArmyList().indexOf(warDefenseHero);
        // 处理并满的情况,下一个开始自动补兵的情况
        if (hero.getCurrentSoliderNum() >= hero.getSoldierNum()) {
            warDefenseHero.setAddSoldier(false);
            warDefenseHero.setLastRefreshTime(System.currentTimeMillis());
            WarDefenseHero nextHero = getNextAutoAddSoliderHero(index, player);
            if (nextHero != null) {
                nextHero.setAddSoldier(true);
                // 上一个城防军结束时间就是当前城防军开始时间
                completeTime = completeTime != 0L ? completeTime : curTime;
                nextHero.setLastRefreshTime(completeTime);
                updateDefenseSoldierByPlayer(player);
            }
        }
    }

    /**
     * 拿到下一个自动补兵的英雄
     *
     * @param index
     * @param player
     */
    public WarDefenseHero getNextAutoAddSoliderHero(int index, Player player) {
        // 拿到还剩余最少的补兵英雄
        WarDefenseHero surWarDefenseHero = getMinSurSolderHero(player);
        if (surWarDefenseHero != null) {
            return surWarDefenseHero;
        }
        // 先往后找 最多四个英雄 index 最多等于三

        if (index == 3) {
            for (int i = 0; i < 3; i++) {
                WarDefenseHero warDefenseHero = player.getDefenseArmyList().get(i);
                if (warDefenseHero.getHeroId() > 0) {
                    return warDefenseHero;
                }
            }
        } else if (index == 0) {
            for (int i = 1; i <= 3; i++) {
                WarDefenseHero warDefenseHero = player.getDefenseArmyList().get(i);
                if (warDefenseHero.getHeroId() > 0) {
                    return warDefenseHero;
                }
            }
        } else if (index > 0 && index < 3) {
            for (int i = index + 1; i <= 3; i++) {
                WarDefenseHero warDefenseHero = player.getDefenseArmyList().get(i);
                if (warDefenseHero.getHeroId() > 0) {
                    return warDefenseHero;
                }
            }
            // 如果没找到 往前找

            for (int i = 0; i < index; i++) {
                WarDefenseHero warDefenseHero = player.getDefenseArmyList().get(i);
                if (warDefenseHero.getHeroId() > 0) {
                    return warDefenseHero;
                }
            }
        }
        return null;
    }

    public CastlePb.UpdateDefenseSoldierRs.Builder updateDefenseSoldierRs(Player player) {
        CastlePb.UpdateDefenseSoldierRs.Builder builder = CastlePb.UpdateDefenseSoldierRs.newBuilder();
        List<WarDefenseHero> warDefenseHeroes = player.getDefenseArmyList();
        warDefenseHeroes.forEach(warDefenseHero -> {

            CommonPb.DefenseHeroSoldier.Builder defenseHeroSoldier = CommonPb.DefenseHeroSoldier.newBuilder();
            defenseHeroSoldier.setHeroId(warDefenseHero.getHeroId());
            defenseHeroSoldier.setLastRefreshTime(warDefenseHero.getLastRefreshTime());
            defenseHeroSoldier.setIsAddSoldier(warDefenseHero.isAddSoldier());
            Hero hero = player.getHero(warDefenseHero.getHeroId());
            int num = 0;
            if (hero != null) {
                num = hero.getCurrentSoliderNum();

            }
            defenseHeroSoldier.setSoldierNum(num);
            builder.addDefenseHeroSoldier(defenseHeroSoldier);
        });
        builder.setGrainAndGrass(player.getOil());
        return builder;
    }

    public CastlePb.SynUpdateDefenseSoldierRq.Builder synUpdateDefenseSoldierRq(Player player) {
        CastlePb.SynUpdateDefenseSoldierRq.Builder builder = CastlePb.SynUpdateDefenseSoldierRq.newBuilder();
        List<WarDefenseHero> warDefenseHeroes = player.getDefenseArmyList();
        warDefenseHeroes.forEach(warDefenseHero -> {

            CommonPb.DefenseHeroSoldier.Builder defenseHeroSoldier = CommonPb.DefenseHeroSoldier.newBuilder();
            defenseHeroSoldier.setHeroId(warDefenseHero.getHeroId());
            defenseHeroSoldier.setLastRefreshTime(warDefenseHero.getLastRefreshTime());
            defenseHeroSoldier.setIsAddSoldier(warDefenseHero.isAddSoldier());
            Hero hero = player.getHero(warDefenseHero.getHeroId());
            int num = 0;
            if (hero != null) {
                num = hero.getCurrentSoliderNum();

            }
            defenseHeroSoldier.setSoldierNum(num);
            builder.addDefenseHeroSoldier(defenseHeroSoldier);
        });
        builder.setGrainAndGrass(player.getOil());
        return builder;
    }

    public void buyDefenseSoldier(BuyDefenseSoldiersHandler handler, CastlePb.BuyDefenseSoldiersRq rq) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        List<WarDefenseHero> warDefenseHeroes = player.getDefenseArmyList();
        WarDefenseHero warDefenseHero = null;
        for (WarDefenseHero hero : warDefenseHeroes) {
            if (hero.getHeroId() == rq.getHero()) {
                warDefenseHero = hero;
                break;
            }
        }
        if (warDefenseHero == null) {
            LogHelper.CONFIG_LOGGER.info("buyDefenseSoldier error GameError =" + GameError.DEFENSE_SOLDIER_NOT_EXIST);
            handler.sendErrorMsgToPlayer(GameError.DEFENSE_SOLDIER_NOT_EXIST);
            return;
        }
        updateDefenseSoldierByPlayer(player);
        Hero hero = player.getHero(warDefenseHero.getHeroId());
        int curSoldier = hero.getSoldierNum() - hero.getCurrentSoliderNum();
        curSoldier = Math.max(0, curSoldier);
        if (curSoldier > 0) {
            int requireGold = (int) Math.floor(curSoldier * 1.0f / hero.getSoldierNum() * 100);
            if (player.getGold() < requireGold) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            // 扣除元宝
            playerManager.subAward(player, AwardType.GOLD, AwardType.GOLD, requireGold, Reason.BUY_DEFENSE_HERO_SOLDIER);
            hero.setCurrentSoliderNum(hero.getSoldierNum());
            warDefenseHero.setLastRefreshTime(System.currentTimeMillis());

        }
        CastlePb.BuyDefenseSoldiersRs.Builder builder = CastlePb.BuyDefenseSoldiersRs.newBuilder();
        builder.setGold(player.getGold());
        // 如果立即完成的防守军是正在恢复的 改变其状态
        if (warDefenseHero.isAddSoldier()) {
            warDefenseHero.setAddSoldier(false);
        }
        updateDefenseSoldierByPlayer(player);
        handler.sendMsgToPlayer(GameError.OK, CastlePb.BuyDefenseSoldiersRs.ext, builder.build());
//        updateDefenseSoldierByPlayer(player);
//        handler.sendMsgToPlayer(GameError.OK, CastlePb.UpdateDefenseSoldierRs.ext, updateDefenseSoldierRs(player).build());
    }

    /**
     * 属性变化从先计算补兵规则
     *
     * @param player
     * @param hero
     */
    public void updateDefenseArmyByAttributeChange(Player player, Hero hero) {
        try {
            if (!player.isDefenseHero(hero)) {
                return;
            }
            updateDefenseSoldierByPlayer(player);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
