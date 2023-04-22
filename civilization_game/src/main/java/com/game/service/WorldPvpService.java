package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticPvpCost;
import com.game.domain.s.StaticPvpDig;
import com.game.domain.s.StaticPvpExchange;
import com.game.log.domain.HatcheryLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.PvpBattlePb;
import com.game.pb.PvpBattlePb.*;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.spring.SpringUtil;
import com.game.worldmap.MapInfo;
import com.game.worldmap.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class WorldPvpService {
    @Autowired
    private WorldPvpMgr worldPvpMgr;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticWorldMgr staticWorldMgr;

    @Autowired
    private HeroManager heroManager;

    @Autowired
    private BattleMgr battleMgr;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private LootManager lootManager;

    @Autowired
    private SoldierManager soldierManager;

    @Autowired
    private ChatManager chatManager;

    @Autowired
    private StaticPropMgr staticPropMgr;

    @Autowired
    private WarBookManager warBookManager;

    public void checkWar() {
        if (worldManager.isNotOk()) {
            return;
        }

        if (worldPvpMgr.getOpen() == 0) {
            //LogHelper.GAME_DEBUG.error("皇城血战没有开启!!");
            return;
        }

        worldPvpMgr.checkPvpOpen();
        worldPvpMgr.pvpLogic();
    }

    // pvp battle mass hero
    public void massHero(MassActionRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        HashSet<Integer> massHeroes = player.getMassHeroes();
        List<Integer> heroIds = req.getHeroIdList();
        if (heroIds.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        HashSet<Integer> checkHero = new HashSet<Integer>();
        checkHero.addAll(heroIds);
        if (checkHero.size() != heroIds.size()) {
            handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
            return;
        }

        // check mass number
        if (massHeroes.size() + checkHero.size() > 4) {
            handler.sendErrorMsgToPlayer(GameError.MASS_TO_MANY_HERO);
            return;
        }

        // check heroId
        for (Integer heroId : heroIds) {
            // check same
            if (massHeroes.contains(heroId)) {
                handler.sendErrorMsgToPlayer(GameError.MASS_SAME_HERO);
                LogHelper.CONFIG_LOGGER.info("mass same hero, heroId =" + heroId);
                return;
            }

            // check exists.
            Hero hero = player.getHero(heroId);
            if (hero == null) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
                return;
            }

            // check in march
            if (player.isInMarch(hero)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
                return;
            }

            // check is not embattle
            if (!player.isEmbattle(heroId)) {
                handler.sendErrorMsgToPlayer(GameError.NOT_EMBATTLE_HERO);
                return;
            }

            // check soldier num
            if (hero.getCurrentSoliderNum() <= 0) {
//                System.out.println(hero.getHeroId());
                handler.sendErrorMsgToPlayer(GameError.ALREADY_FAIL);
                return;
            }

            // check in battle
            if (worldPvpMgr.isHeroInBattle(heroId, player.roleId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_IS_IN_PVP_BATTLE);
                return;
            }
        }

        // 集结所有英雄
        for (Integer heroId : heroIds) {
            massHeroes.add(heroId);
        }

        MassActionRs.Builder builder = MassActionRs.newBuilder();
        builder.addAllHeroId(massHeroes);
        handler.sendMsgToPlayer(MassActionRs.ext, builder.build());
    }

    // 前往或者进攻操作[集结、攻击方兵力为0、或者战斗没有开始]
    // 如果当前已经是PvpHero了: 攻击方兵力为0
    // 否则: 处于集结
    public void goAction(GoActionRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int heroId = req.getHeroId();
        int placeId = req.getPlaceId();

        PvpHero pvpHero = player.getPvpHero(heroId);
        if (pvpHero != null) {
            pvpHeroGoAction(player, pvpHero, heroId, placeId, handler);
        } else {
            massHeroGoAction(player, heroId, placeId, handler);
        }
        LogHelper.GAME_DEBUG.error("玩家进行前往和进攻操作, 英雄的Id=" + heroId);

    }

    // in battle status change
    public void pvpHeroGoAction(Player player,
                                PvpHero pvpHero,
                                int heroId,
                                int placeId,
                                ClientHandler handler) {
        PvpBattle pvpBattle = worldPvpMgr.getPvpBattle(pvpHero.getPlaceId());
        if (pvpBattle == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_BATTLE_IS_NULL);
            LogHelper.CONFIG_LOGGER.info("pvp battle is null.");
            return;
        }


        // check battle started
//        if (!pvpBattle.isBattleTick()) {
//            handler.sendErrorMsgToPlayer(GameError.PVP_BATTLE_CAN_NOT_ACTION);
//            return;
//        }

        // can not go the same place
        if (pvpHero.getPlaceId() == placeId) {
            handler.sendErrorMsgToPlayer(GameError.CAN_NOT_GO_TO_SAME_PLACE);
            return;
        }

        // check placeId correct.
        if (placeId < PvpPlaceId.UP || placeId > PvpPlaceId.CENTER) {
            handler.sendErrorMsgToPlayer(GameError.PLACEID_IS_WRONG);
            return;
        }

        Hero hero = player.getHero(heroId);
        if (hero == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        // check soldier num
        if (hero.getCurrentSoliderNum() <= 0) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_FAIL);
            return;
        }

        // check in march
        if (player.isInMarch(hero)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
            return;
        }

        // check is not embattle
        if (!player.isEmbattle(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.NOT_EMBATTLE_HERO);
            return;
        }

        // pvpHero from battleA -> battleB
        PvpBattle targetBattle = worldPvpMgr.getPvpBattle(placeId);
        if (targetBattle == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_BATTLE_IS_NULL);
            LogHelper.CONFIG_LOGGER.info("target battle is null.");
            return;
        }
        if (placeId == 4 && pvpHero.getCountry() != targetBattle.getLastHeroCountry() && targetBattle.getDefenceTeam().size() < 1) {
            worldPvpMgr.setPvpCountry(pvpHero.getCountry());
            targetBattle.setLastHeroCountry(pvpHero.getCountry());
            targetBattle.setFlag(true);
        }
        pvpBattle.removeHero(pvpHero);
        targetBattle.addHero(pvpHero);
        checkHeroMove(pvpBattle, targetBattle, pvpHero);
        pvpHero.setPlaceId(placeId);

        // send msg
        GoActionRs.Builder goActionRs = GoActionRs.newBuilder();
        goActionRs.setPvpHero(worldPvpMgr.wrapPvpHero(pvpHero));
        handler.sendMsgToPlayer(GoActionRs.ext, goActionRs.build());
        worldPvpMgr.getPvpCountry();
        // 两个位置的兵力发生变化，所以同步兵力需要支持repeated结构
        worldPvpMgr.synBothPvpSoldier(pvpBattle, targetBattle);

        //PvpBattle pvpBattle1 = worldPvpMgr.getCenterBattle();
        // System.out.println(worldPvpMgr.getPvpCountry()+"===============pvpHeroGoAction==============="+pvpBattle1.getLastHeroCountry());

    }

    public void checkHeroMove(PvpBattle pvpBattle, PvpBattle targetBattle, PvpHero pvpHero) {
        if (pvpBattle.hasPvpHero(pvpHero)) {
            makeException("remove hero error, heroId = " + pvpHero.getHeroId());
        }

        if (!targetBattle.hasPvpHero(pvpHero)) {
            makeException("add hero error, heroId = " + pvpHero.getHeroId());
        }


    }

    public void makeException(String str) {
        try {
            throw new LogicException(str);
        } catch (LogicException e) {
            e.printStackTrace();
        }
    }

    // in mass status change
    public void massHeroGoAction(Player player,
                                 int heroId,
                                 int placeId,
                                 ClientHandler handler) {
        // must in mass status
        if (!player.isInMass(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_IN_MASS);
            return;
        }


        if (player.hasPvpHero(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_PVP_BATTLE);
            return;
        }

        // check country
        int country = player.getCountry();
        // check placeId correct.
        if (placeId < PvpPlaceId.UP || placeId > PvpPlaceId.RIGHT) {
            handler.sendErrorMsgToPlayer(GameError.PLACEID_IS_WRONG);
            return;
        }

        if (placeId != country) {
            handler.sendErrorMsgToPlayer(GameError.PLACE_AND_COUNTRY_NOT_SAME);
            return;
        }

        // check defence team, if defence team is empty, then turn to defence,
        // if defence team is not empty, then check defence country, if defence
        // country = player country, then go for defence, else go for attack
        PvpBattle pvpBattle = worldPvpMgr.getPvpBattle(placeId);
        if (pvpBattle == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_BATTLE_IS_NULL);
            return;
        }

        Hero hero = player.getHero(heroId);
        if (hero == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        // check soldier num
        if (hero.getCurrentSoliderNum() <= 0) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_FAIL);
            return;
        }


        // check in march
        if (player.isInMarch(hero)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
            return;
        }

        // check is not embattle
        if (!player.isEmbattle(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.NOT_EMBATTLE_HERO);
            return;
        }

        if (player.hasPvpHero(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.SAME_HERO_GO_ACTION);
            return;
        }

        PvpHero pvpHero = player.addPvpHero(heroId);
        if (pvpHero == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_HERO_IS_NULL);
            LogHelper.CONFIG_LOGGER.info("pvp hero is null!");
            return;
        }
        pvpHero.setPlaceId(placeId);

        //兵书技能效果加成
//        Integer heroWarBookSkillEffect = warBookManager.getHeroWarBookSkillEffect(hero, BookEffectType.REVIVE_HERO_ROYAL_CITY);
//        if (null != heroWarBookSkillEffect) {
//            pvpHero.setFreeeRebornTimes(1);
//        }

        pvpBattle.addHero(pvpHero);
        player.removeMassHero(heroId);

        if (!pvpBattle.hasPvpHero(pvpHero)) {
            makeException("add hero error, heroId = " + pvpHero.getHeroId());
        }

        if (player.isInMass(heroId)) {
            makeException("hero is in mass, heroId = " + heroId);
        }

        // send msg
        GoActionRs.Builder goActionRs = GoActionRs.newBuilder();
        goActionRs.setPvpHero(worldPvpMgr.wrapPvpHero(pvpHero));
        handler.sendMsgToPlayer(GoActionRs.ext, goActionRs.build());

        //worldPvpMgr.synPvpSoldier(pvpBattle);

        worldPvpMgr.synPvpAllSoldier(pvpBattle);

    }

    // 回防或者偷袭
    public void fightAction(FightActionRq req, ClientHandler handler) {
        LogHelper.GAME_DEBUG.error("玩家进行英雄进行回访或者偷袭操作, heroId = " + req.getHeroId() + ", placeId = " + req.getPlaceId());
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // check battle state
        int placeId = req.getPlaceId();

        // check placeId correct.
        if (placeId < PvpPlaceId.UP || placeId > PvpPlaceId.CENTER) {
            handler.sendErrorMsgToPlayer(GameError.PLACEID_IS_WRONG);
            return;
        }

        int heroId = req.getHeroId();
        Hero hero = player.getHero(heroId);
        if (hero == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        PvpHero pvpHero = player.getPvpHero(heroId);
        if (pvpHero == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_HERO_IS_NULL);
            return;
        }

        // check soldier num
        if (hero.getCurrentSoliderNum() <= 0) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_FAIL);
            return;
        }

        // check in march
        if (player.isInMarch(hero)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
            return;
        }

        // check is not embattle
        if (!player.isEmbattle(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.NOT_EMBATTLE_HERO);
            return;
        }

        // 回防前的战场
        int beforePlaceId = pvpHero.getPlaceId();
        PvpBattle pvpBattle = worldPvpMgr.getPvpBattle(beforePlaceId);
        if (pvpBattle == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_BATTLE_IS_NULL);
            LogHelper.CONFIG_LOGGER.info("pvp battle is null.");
            return;
        }

        //        if (!pvpBattle.isInBattleState()) {
        //            handler.sendErrorMsgToPlayer(GameError.NOT_IN_PVP_BATTLE_STATE);
        //            return;
        //        }

        // pvpHero from battleA -> battleB
        PvpBattle targetBattle = worldPvpMgr.getPvpBattle(placeId);
        if (targetBattle == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_BATTLE_IS_NULL);
            LogHelper.CONFIG_LOGGER.info("target battle is null.");
            return;
        }

        int pvpState = targetBattle.getPvpState(pvpHero);

        int needGold;
        //LogHelper.GAME_DEBUG.error("pvp State = " + pvpState + ", defenceTimes = "
        //            + pvpHero.getDefenceTimes() + ", attackTimes = "
        //            + pvpHero.getAttackTimes());
        if (pvpState == 2) {  // 回防
            int defenceTimes = pvpHero.getDefenceTimes();
            StaticPvpCost pvpCost = staticWorldMgr.getStaticPvpCost(PvpAction.DEFENCE);
            List<Integer> costList = pvpCost.getCost();  // 0,1,2,3
            if (costList == null || costList.isEmpty()) {
                handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
                return;
            }

            if (defenceTimes >= costList.size()) {
                defenceTimes = costList.size() - 1;
            }

            needGold = costList.get(defenceTimes);
            if (needGold > player.getGold()) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            pvpHero.setDefenceTimes(pvpHero.getDefenceTimes() + 1);
        } else {  // 偷袭
            int attackTimes = pvpHero.getAttackTimes();
            StaticPvpCost pvpCost = staticWorldMgr.getStaticPvpCost(PvpAction.ATTACK);
            List<Integer> costList = pvpCost.getCost();
            if (costList == null || costList.isEmpty()) {
                handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
                return;
            }

            if (attackTimes >= costList.size()) {
                attackTimes = costList.size() - 1;
            }

            needGold = costList.get(attackTimes);
            if (needGold > player.getGold()) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            pvpHero.setAttackTimes(pvpHero.getAttackTimes() + 1);
        }

        playerManager.subGoldOk(player, needGold, Reason.WORLD_PVP_BATTLE);


        pvpBattle.removeHero(pvpHero);
        targetBattle.addHero(pvpHero);
        pvpHero.setPlaceId(placeId);
        checkHeroMove(pvpBattle, targetBattle, pvpHero);
        // 检测攻防转换
        worldPvpMgr.checkPvpBattle(pvpBattle);
        worldPvpMgr.synBothPvpSoldier(pvpBattle, targetBattle);

        // send msg
        FightActionRs.Builder actionRs = FightActionRs.newBuilder();
        actionRs.setPvpHero(worldPvpMgr.wrapPvpHero(pvpHero));
        actionRs.setGold(player.getGold());
        handler.sendMsgToPlayer(FightActionRs.ext, actionRs.build());

    }

    // 救援: 英雄死亡之后，进行救援
    public void saveAction(SaveActionRq req, ClientHandler handler) {
        LogHelper.GAME_DEBUG.error("玩家对英雄进行救援, 英雄Id = " + req.getHeroId());
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 检测英雄有没有兵力
        int heroId = req.getHeroId();
        Hero hero = player.getHero(heroId);
        if (hero == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        PvpHero pvpHero = player.getPvpHero(heroId);
        if (pvpHero == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_HERO_IS_NULL);
            return;
        }

        // check soldier num
        if (hero.getCurrentSoliderNum() > 0) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IS_ALIVE);
            return;
        }

        int rebornTimes = pvpHero.getRebornTimes();
        StaticPvpCost pvpCost = staticWorldMgr.getStaticPvpCost(PvpAction.REBORN);
        List<Integer> costList = pvpCost.getCost();
        if (costList == null || costList.isEmpty() || costList.size() != 4) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int cost = 0;
        int freeeRebornTimes = pvpHero.getFreeeRebornTimes();
        if (freeeRebornTimes == 0) {
            if (rebornTimes <= 0) {
                cost = costList.get(0);
            } else if (rebornTimes >= 4) {
                cost = costList.get(3);
            } else {
                cost = costList.get(rebornTimes);
            }
        }
        int needGold = cost;
        if (needGold > player.getGold()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        // 有插队操作
        // pvpHero from battleA -> battleB
        PvpBattle pvpBattle = worldPvpMgr.getPvpBattle(pvpHero.getPlaceId());
        if (pvpBattle == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_BATTLE_IS_NULL);
            LogHelper.CONFIG_LOGGER.info("target battle is null.");
            return;
        }

        // 获取兵营兵力
        int soldierType = heroManager.getSoldierType(heroId);
        Soldier soldier = player.getSoldier(soldierType);
        if (soldier == null) {
            handler.sendErrorMsgToPlayer(GameError.SOLDIER_TYPE_ERROR);
            return;
        }

        int currentSoldier = soldierManager.getSoldierNum(player, soldierType);
        if (currentSoldier <= 0) {
            handler.sendErrorMsgToPlayer(GameError.SOLDIER_NOT_ENOUGH);
            return;
        }

        // 应该增加的英雄的兵力
        heroManager.caculateProp(hero, player);
        int addSoldierNum = hero.getSoldierNum() - hero.getCurrentSoliderNum();
        int diff = Math.min(currentSoldier, addSoldierNum);
        if (diff <= 0) {
            handler.sendErrorMsgToPlayer(GameError.SOLDIER_FULL);
            return;
        }

        playerManager.subAward(player, AwardType.SOLDIER, soldierType, diff, Reason.WORLD_PVP_BATTLE);
        hero.setCurrentSoliderNum(hero.getCurrentSoliderNum() + diff);

        playerManager.subGoldOk(player, needGold, Reason.WORLD_PVP_BATTLE);
        if (freeeRebornTimes == 0) {
            pvpHero.setRebornTimes(pvpHero.getRebornTimes() + 1);
        } else if (freeeRebornTimes > 0) {
            pvpHero.setFreeeRebornTimes(freeeRebornTimes - 1);
        }
        pvpHero.setDeadTime(0L);
        pvpBattle.insertHero(pvpHero);

        if (!pvpBattle.hasPvpHero(pvpHero)) {
            makeException("insert hero failed, heroId = " + pvpHero.getHeroId());
        }
        if (pvpBattle.getPlaceId() == 4 && !pvpBattle.hasAttacker() && worldPvpMgr.getPvpCountry() != pvpHero.getCountry()) {
            worldPvpMgr.setPvpCountry(pvpHero.getCountry());
            pvpBattle.setFlag(true);
            pvpBattle.setLastHeroCountry(pvpHero.getCountry());
        }

        SaveActionRs.Builder actionRs = SaveActionRs.newBuilder();
        actionRs.setPvpHero(worldPvpMgr.wrapPvpHero(pvpHero));
        actionRs.setGold(player.getGold());
        actionRs.setSoldierInfo(soldier.wrapPb());
        handler.sendMsgToPlayer(SaveActionRs.ext, actionRs.build());

        // 同步pvpBattle兵力
        worldPvpMgr.synPvpAllSoldier(pvpBattle);
    }

    // 单挑: 1打多, 挑2~n的队伍进行打, 一场一场打，然后记录战斗信息
    public void soloAction(SoloActionRq req, ClientHandler handler) {
        LogHelper.GAME_DEBUG.error("玩家开始单挑操作, 玩家英雄Id =" + req.getHeroId());
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 防守方打攻击方或者攻击方打防守方
        int heroId = req.getHeroId();
        Hero hero = player.getHero(heroId);
        if (hero == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            LogHelper.GAME_DEBUG.error("开始进行单挑 hero == null");

            return;
        }

        PvpHero pvpHero = player.getPvpHero(heroId);
        if (pvpHero == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_HERO_IS_NULL);
            LogHelper.GAME_DEBUG.error("开始进行单挑 pvpHero == null");

            return;
        }

        // check soldier num
        if (hero.getCurrentSoliderNum() <= 0) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_FAIL);
            LogHelper.GAME_DEBUG.error("开始进行单挑 当前英雄兵力 <= 0");

            return;
        }

        // check pvp battle
        PvpBattle pvpBattle = worldPvpMgr.getPvpBattle(pvpHero.getPlaceId());
        if (pvpBattle == null) {
            handler.sendErrorMsgToPlayer(GameError.PVP_BATTLE_IS_NULL);
            LogHelper.CONFIG_LOGGER.info("pvp battle is null.");
            LogHelper.GAME_DEBUG.error("开始进行单挑 当前pvp battle is null");

            return;
        }


        // check pvp state
        int pvpState = pvpBattle.getPvpState(pvpHero);
        LinkedList<PvpHero> oppositeTeam = pvpBattle.getOppositeTeam(pvpState);

        // 获得当前英雄的index,如果index <= 1 表示不能单挑, "不满足单挑条件"
        int index = pvpBattle.getHeroIndex(pvpHero);
        if (index <= 1 || oppositeTeam.size() <= 1) {
            handler.sendErrorMsgToPlayer(GameError.PVP_SOLO_STATE_ERROR);
            LogHelper.GAME_DEBUG.error("开始进行单挑, 对面的队伍为空!!");
            return;
        }

        // query solo cost
        int soloTimes = pvpHero.getSoloTimes();
        StaticPvpCost pvpCost = staticWorldMgr.getStaticPvpCost(PvpAction.SOLO);
        List<Integer> costList = pvpCost.getCost();
        if (costList == null || costList.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            LogHelper.GAME_DEBUG.error("单挑消耗配置为空!");
            return;
        }

        // range check
        if (soloTimes >= costList.size()) {
            soloTimes = costList.size() - 1;
            LogHelper.GAME_DEBUG.error("当前单挑的次数为:" + soloTimes);

        }

        int needGold = costList.get(soloTimes);
        if (needGold > player.getGold()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            LogHelper.GAME_DEBUG.error("单挑需要的金币不足,需要的金币为=" + needGold + ", 当前金币 =" + player.getGold());
            return;
        }

        // set solo times
        pvpHero.setSoloTimes(pvpHero.getSoloTimes() + 1);
        LogHelper.GAME_DEBUG.error("当前英雄已经单挑次数为=" + pvpHero.getSoloTimes());
        playerManager.subGoldOk(player, needGold, Reason.WORLD_PVP_BATTLE);
        // start fight
        List<SoloInfo> killInfo = new ArrayList<SoloInfo>();
        List<SoloInfo> playerKill = new ArrayList<SoloInfo>();
        Random rand = new Random(System.currentTimeMillis());
        int beforeTotalSoldier = hero.getCurrentSoliderNum();
        LogHelper.GAME_DEBUG.error("单挑英雄的起始兵力为=" + beforeTotalSoldier);
        LogHelper.GAME_DEBUG.error("防守方英雄个数为=" + oppositeTeam.size());
        for (int i = 1; i < oppositeTeam.size(); i++) {
            PvpHero targetPvpHero = oppositeTeam.get(i);
            if (targetPvpHero == null) {
                LogHelper.CONFIG_LOGGER.info("targetPvpHero is null.");
                continue;
            }


            Player target = playerManager.getPlayer(targetPvpHero.getLordId());
            if (target == null) {
                LogHelper.GAME_DEBUG.error("防守方的玩家不存在!");
                continue;
            }

            Hero targetHero = target.getHero(targetPvpHero.getHeroId());
            if (targetHero == null) {
                LogHelper.GAME_DEBUG.error("防守方的英雄不存在!!!!");
                continue;
            }

            if (targetHero.getCurrentSoliderNum() <= 0) {
                LogHelper.GAME_DEBUG.error("防守方有个死人英雄，逻辑错误!");
                continue;
            }

            List<Integer> myHeroIds = new ArrayList<Integer>();
            List<Integer> tragetHeroIds = new ArrayList<Integer>();
            myHeroIds.add(heroId);
            tragetHeroIds.add(targetPvpHero.getHeroId());
            // 计算攻击方的损兵量
            Team attacker = battleMgr.initPlayerTeam(player, myHeroIds, BattleEntityType.HERO);       // 攻击方
            Team defencer = battleMgr.initPlayerTeam(target, tragetHeroIds, BattleEntityType.HERO);   // 防守方

            SoloInfo soloInfo = new SoloInfo();
            soloInfo.setLordName(target.getNick());
            soloInfo.setHeroId(targetHero.getHeroId());

            LogHelper.GAME_DEBUG.error("开始单挑战斗");
            battleMgr.doTeamBattle(attacker, defencer, rand, ActPassPortTaskType.IS_WORLD_WAR);

            // 计算经验值
            worldManager.caculateTeamKill(attacker, player.roleId);
            worldManager.caculateTeamDefenceKill(defencer);

            int beforeHeroNum = hero.getCurrentSoliderNum();
            int beforeEnemySoldier = targetHero.getCurrentSoliderNum();

            LogHelper.GAME_DEBUG.error("单挑序列 i = " + i);
            LogHelper.GAME_DEBUG.error("我方英雄战斗前血量 = " + beforeHeroNum);
            LogHelper.GAME_DEBUG.error("敌人战斗前血量 = " + beforeEnemySoldier);


            // 更新玩家英雄血量
            worldPvpMgr.cacPlayerSoldier(attacker, player);
            worldPvpMgr.cacPlayerSoldier(defencer, target);

            int afterHeroSoldier = hero.getCurrentSoliderNum();
            int afterEnemySoldier = targetHero.getCurrentSoliderNum();

            LogHelper.GAME_DEBUG.error("战斗结束后, 我方英雄血量 = " + afterHeroSoldier);
            LogHelper.GAME_DEBUG.error("战斗结束后,  敌人血量 = " + afterEnemySoldier);


            int killNum = beforeEnemySoldier - afterEnemySoldier;
            LogHelper.GAME_DEBUG.error("战斗结束后, 我方英雄击杀个数 = " + killNum);
            worldPvpMgr.handlePvpKill(targetPvpHero, killNum);
            soloInfo.setLost(killNum);
            if (targetHero.getCurrentSoliderNum() <= 0) {
                LogHelper.GAME_DEBUG.error("战斗结束后, 敌人血量 <= 0, 已经死亡!");
                worldPvpMgr.handleHeroDead(targetPvpHero, pvpBattle);
            }

            SoloInfo mySoloInfo = new SoloInfo();
            mySoloInfo.setLordName(player.getNick());
            mySoloInfo.setHeroId(heroId);
            mySoloInfo.setLost(beforeHeroNum - afterHeroSoldier);
            playerKill.add(mySoloInfo);
            killInfo.add(soloInfo);
            if (!attacker.isWin()) {
                LogHelper.GAME_DEBUG.error("我方英雄挑战失败!");
                break;
            }
        }

        SoloActionRs.Builder builder = SoloActionRs.newBuilder();
        builder.setGold(player.getGold());
        builder.setPvpHero(worldPvpMgr.wrapPvpHero(pvpHero));
        for (SoloInfo info : killInfo) {
            builder.addKillInfo(info.wrapPb());
            worldPvpMgr.addPvpBattleScore(info.getLost());
        }

        for (SoloInfo info : playerKill) {
            builder.addCurrentInfo(info.wrapPb());
        }

        int afterTotalSoldier = hero.getCurrentSoliderNum();
        LogHelper.GAME_DEBUG.error("单挑英雄的最后结算兵力为=" + afterTotalSoldier);
        int totalLost = beforeTotalSoldier - afterTotalSoldier;
        worldPvpMgr.addPvpBattleScore(totalLost);
        handler.sendMsgToPlayer(SoloActionRs.ext, builder.build());

        // 通知英雄挂掉
        if (afterTotalSoldier <= 0) {
            worldPvpMgr.handleHeroDead(pvpHero, pvpBattle);
            LogHelper.GAME_DEBUG.error("单挑的英雄兵力为0!!!");
        }

        // 计算所有发起方的累杀、连杀
        for (SoloInfo info : killInfo) {
            if (info.getLost() <= 0) {
                LogHelper.CONFIG_LOGGER.info("lost is <= 0.");
                continue;
            }
            worldPvpMgr.handlePvpKill(pvpHero, info.getLost());
        }

        // 需要同步兵力以及血战经验值
        worldPvpMgr.synPvpSoldierAndExp(pvpBattle);
        // 检查攻防转换
        worldPvpMgr.checkPvpBattle(pvpBattle);
        // 需要同步兵力信息
        worldPvpMgr.synPvpSoldierAndExp(pvpBattle);
    }

    public void getPvpInfo(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        GetPvpInfoRs.Builder builder = GetPvpInfoRs.newBuilder();
        builder.addAllPvpSoldier(worldPvpMgr.wrapAllSoldier());
        HashMap<Integer, PvpHero> pvpHeroMap = player.getPvpHeroMap();
        if (worldPvpMgr.isPvpOver()) {
            player.getPvpHeroMap().clear();
            player.getMassHeroes().clear();
            builder.setEndTime(0L);
        } else {
            worldPvpMgr.getSet().add(player.getLord().getLordId());
            HashSet<Integer> massHeroes = player.getMassHeroes();
            if (!massHeroes.isEmpty()) {
                builder.addAllMassHeroId(massHeroes);
            }
            for (PvpHero pvpHero : pvpHeroMap.values()) {
                builder.addPvpHero(worldPvpMgr.wrapPvpHero(pvpHero));
            }
        }
        Iterator<PvpHero> iterator = pvpHeroMap.values().iterator();
        while (iterator.hasNext()) {
            PvpHero pvpHero = iterator.next();
            if (pvpHero.getPlaceId() == 0) {
                iterator.remove();
            }
            if (player.isInMass(pvpHero.getHeroId())) {
                player.removeMassHero(pvpHero.getHeroId());
            }
        }
        builder.setEndTime(worldPvpMgr.getPvpEndTime());
        SimpleData simpleData = player.getSimpleData();
        builder.setTotalKill(simpleData.getTotalKillNum());
        builder.setPvpScore(simpleData.getPvpScore());
        builder.setPvpBattleScore(worldPvpMgr.getPvpBattleScore());
        builder.setIsOpen(worldPvpMgr.getOpen());
        builder.setBanquetEndTime(worldPvpMgr.getBanquetEndTime());
        handler.sendMsgToPlayer(GetPvpInfoRs.ext, builder.build());
    }

    public void attendPvp(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        if (worldPvpMgr.getWorldPvpState() != WorldPvpState.START) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_PVP_STATE_ERROR);
            return;
        }

        // check player level
        if (player.getLevel() < staticLimitMgr.getNum(35)) {
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }

        AttendPvpRs.Builder builder = AttendPvpRs.newBuilder();
        worldPvpMgr.addPlayer(player);
        handler.sendMsgToPlayer(AttendPvpRs.ext, builder.build());
    }

    // 获取pvp排行榜信息
    public void getRankInfo(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        LinkedList<RankPvpHero> rankList = worldPvpMgr.getRankListCopy();
        GetPvpRankInfoRs.Builder builder = GetPvpRankInfoRs.newBuilder();
        long lordId = player.roleId;

        // 检查玩家是否在前9名
        int indexFound = -1;
        List<RankPvpHero> results = new ArrayList<RankPvpHero>();
        List<Integer> ranks = new ArrayList<Integer>();
        for (int i = 0; i < rankList.size() && i < 9; i++) {
            RankPvpHero rankPvpHero = rankList.get(i);
            if (rankPvpHero == null) {
                continue;
            }

            ranks.add(i + 1);
            results.add(rankPvpHero);
            if (lordId == rankPvpHero.getLordId()) {
                indexFound = i;
            }
        }

        // 如果在前9名
        if (indexFound != -1 || rankList.size() < 9) {
            for (int i = 0; i < ranks.size() && i < results.size(); i++) {
                builder.addPvpRankInfo(worldPvpMgr.wrapPvpRank(results.get(i), ranks.get(i)));
            }
            handler.sendMsgToPlayer(GetPvpRankInfoRs.ext, builder.build());
            return;
        }
        //获取玩家实际排名
        for (int i = 9; i < rankList.size(); i++) {
            RankPvpHero rankPvpHero = rankList.get(i);
            if (rankPvpHero == null) {
                continue;
            }
            if (lordId == rankPvpHero.getLordId()) {
                indexFound = i;
                break;
            }
        }

        // 如果玩家上榜了,取玩家排名放在第九名之后
        if (results.size() >= 9) {
            int n = 6;
            for (int i = 0; i <= 2; i++) {
                results.remove(n);
                ranks.remove(n);
            }
        }
        // 如果玩家不在前九名
        int pre1;
        int pre2;
        //不在榜上或者是最后一名 找前二
        boolean isAdd = false;
        if (indexFound == -1) {
            indexFound = rankList.size() - 1;
            isAdd = true;
        }
        if (indexFound >= rankList.size() - 1) {  // 找前三名
            if (isAdd) {
                pre1 = indexFound - 1;
            } else {
                pre1 = indexFound - 2;
            }
            pre2 = indexFound;
        } else {
            // 找index前后
            pre1 = indexFound - 1;
            pre2 = indexFound + 1;
        }

        for (; pre1 >= 0 && pre1 <= pre2 && pre2 <= rankList.size() - 1; pre1++) {
            results.add(rankList.get(pre1));
            ranks.add(pre1 + 1);
        }
        for (int i = 0; i < ranks.size() && i < results.size(); i++) {
            builder.addPvpRankInfo(worldPvpMgr.wrapPvpRank(results.get(i), ranks.get(i)));
        }
        handler.sendMsgToPlayer(GetPvpRankInfoRs.ext, builder.build());
    }

    // 买酒套话
    public void buyWine(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        SimpleData simpleData = player.getSimpleData();
        // 当前的套话次数
        int buyWordTimes = simpleData.getBuyWordTimes();
        HashSet<Integer> digState = simpleData.getDigState();
        // 检查是否还有宝物没有挖
        if (buyWordTimes != 0 && !digState.contains(buyWordTimes)) {
            handler.sendErrorMsgToPlayer(GameError.DIG_STATE_ERROR);
            return;
        }

        int maxBuyWordTimes = staticWorldMgr.getMaxDigTimes();
        if (buyWordTimes >= maxBuyWordTimes) {
            handler.sendErrorMsgToPlayer(GameError.REACH_MAX_WORD_TIMES);
            return;
        }

        if (simpleData.isPaperDiged()) {
            handler.sendErrorMsgToPlayer(GameError.IS_ALREADY_DIGED_PAPER);
            return;
        }

        int keyId = buyWordTimes + 2; // 0 : 2, 1 : 3, 2 : 4, 3 : 5, 4 : 6
        int price = staticWorldMgr.getBuyCost(keyId);

        if (price == 0) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        if (player.getGold() < price) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        // 在当前地图挖宝
        int currentMapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(currentMapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.MAP_ID_ERROR);
            return;
        }

        Pos monsterPos = mapInfo.randPickPos();
        //for (int i = 1; i < 6; i++) {
        //    if (monsterPos.isError() || !mapInfo.isFreePos(monsterPos)) {
        //        monsterPos = mapInfo.randPickPos();
        //    } else {
        //        break;
        //    }
        //}

        Pos digPos = simpleData.getDigPos();
        digPos.initPos(monsterPos);
        playerManager.subGoldOk(player, price, Reason.DIG_PAPER);
        simpleData.setBuyWordTimes(buyWordTimes + 1);
        BuyWineRs.Builder builder = BuyWineRs.newBuilder();
        builder.setBuyTimes(simpleData.getBuyWordTimes());
        builder.setGold(player.getGold());
        builder.setPos(monsterPos.wrapPb());
        builder.setPosUsed(simpleData.getPosUsed());
        handler.sendMsgToPlayer(BuyWineRs.ext, builder.build());

    }

    // 挖宝道具
    public void digProp(PvpBattlePb.DigPropRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        CommonPb.Pos pos = req.getPos();
        Pos reqPos = new Pos(pos.getX(), pos.getY());
        if (!digCheck(req, handler)) {
            return;
        }

        // 检查是否还有宝物没有挖
        if (isBuyDig(player, reqPos)) { // 套话挖宝
            handleBuyDig(player, handler);
        } else { // 普通挖宝
            handleCommonDig(player, handler);
        }
    }

    // 挖宝检查
    public boolean digCheck(PvpBattlePb.DigPropRq req, ClientHandler handler) {
        CommonPb.Pos pos = req.getPos();
        Pos reqPos = new Pos(pos.getX(), pos.getY());
        if (reqPos.isError() || (reqPos.getX() == 0 && reqPos.getY() == 0)) {
            handler.sendErrorMsgToPlayer(GameError.POS_ERROR);
            return false;
        }

        int mapId = worldManager.getMapId(reqPos);
        if (mapId == 0) {
            handler.sendErrorMsgToPlayer(GameError.POS_ERROR);
            return false;
        }

        if (worldPvpMgr.isBanquetOver()) {
            handler.sendErrorMsgToPlayer(GameError.BANQUET_OVER);
            return false;
        }
        return true;
    }

    // 处理套话挖宝
    public void handleBuyDig(Player player, ClientHandler handler) {
        if (!checkDigOk(player, handler)) {
            return;
        }

        SimpleData simpleData = player.getSimpleData();
        int buyWordTimes = simpleData.getBuyWordTimes();
        StaticPvpDig staticPvpDig = staticWorldMgr.getPvpDig(buyWordTimes);
        HashSet<Integer> digState = simpleData.getDigState();
        digState.add(buyWordTimes);
        if (isPaperLooted(player, staticPvpDig)) {
            handlePaperLoot(player, staticPvpDig, handler);
        } else {
            lootDigItem(player, staticPvpDig, handler);
        }
    }

    // 处理普通挖宝
    public void handleCommonDig(Player player, ClientHandler handler) {
        SimpleData simpleData = player.getSimpleData();
        StaticPvpDig staticPvpDig = staticWorldMgr.getPvpDig(0);
        if (staticPvpDig == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        int needScore = staticPvpDig.getDigCost();
        if (simpleData.getPvpScore() < needScore) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_PVP_SCORE);
            return;
        }

        // 检查是否可以随机到红色图纸
        if (isPaperLooted(player, staticPvpDig)) {
            lootDigPaper(player, staticPvpDig, handler);
        } else {
            lootDigItem(player, staticPvpDig, handler);
        }
    }

    // 能否进行套话挖宝
    public boolean isBuyDig(Player player, Pos reqPos) {
        // 检查当前玩家是否可以进行套话挖宝
        SimpleData simpleData = player.getSimpleData();
        int buyWordTimes = simpleData.getBuyWordTimes();
        Pos digPos = simpleData.getDigPos();
        HashSet<Integer> digState = simpleData.getDigState();
        boolean isOk = digPos.isEqual(reqPos) && buyWordTimes != 0 && !digState.contains(buyWordTimes);
        return isOk;
    }

    // 检查挖宝逻辑检查
    public boolean checkDigOk(Player player, ClientHandler handler) {
        SimpleData simpleData = player.getSimpleData();
        int buyWordTimes = simpleData.getBuyWordTimes();
        StaticPvpDig staticPvpDig = staticWorldMgr.getPvpDig(buyWordTimes);  // 1,2,3,4,5
        if (staticPvpDig == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return false;
        }

        int needScore = staticPvpDig.getDigCost();
        if (simpleData.getPvpScore() < needScore) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_PVP_SCORE);
            return false;
        }

        List<Integer> papers = staticPvpDig.getLootPaper();
        if (papers == null) {
            LogHelper.CONFIG_LOGGER.info("paper config is null.");
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return false;
        }

        return true;
    }

    // 检测是否能掉落红色图纸
    public boolean isPaperLooted(Player player, StaticPvpDig staticPvpDig) {
        List<Integer> papers = staticPvpDig.getLootPaper();
        int lootPaperRate = staticPvpDig.getLootPaperRate();
        int randNum = RandomHelper.threadSafeRand(1, 1000);
        boolean isAllDig = worldPvpMgr.isAllDig(player.getCountry(), papers);
        boolean isOk = !isAllDig && randNum <= lootPaperRate;

        return isOk;
    }

    public boolean hasPaper(Player player, StaticPvpDig staticPvpDig) {
        List<Integer> papers = staticPvpDig.getLootPaper();
        List<Integer> leftPapers = worldPvpMgr.getLeftPapers(player.getCountry(), papers);
        if (leftPapers == null || leftPapers.isEmpty()) {
            return false;
        }

        return true;
    }

    // 掉落图纸或者道具
    public void handlePaperLoot(Player player, StaticPvpDig staticPvpDig, ClientHandler handler) {
        if (hasPaper(player, staticPvpDig)) { // 如果还有图纸
            lootDigPaper(player, staticPvpDig, handler);
        } else {  // 没有图纸
            lootDigItem(player, staticPvpDig, handler);
        }
    }

    // 随机掉落道具, 扣还是扣相同的积分
    public void lootDigItem(Player player, StaticPvpDig staticPvpDig, ClientHandler handler) {
        Award award = lootManager.lootAwardByRate(staticPvpDig.getLootProp());
        if (!award.isOk()) {
            LogHelper.CONFIG_LOGGER.info("award is error!");
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        handleDigRq(player, award, staticPvpDig, handler);
    }

    // 随机掉落图纸[扣除积分、处理套话状态]
    public void lootDigPaper(Player player, StaticPvpDig staticPvpDig, ClientHandler handler) {
        List<Integer> lootPaper = staticPvpDig.getLootPaper();
        List<Integer> leftPapers = worldPvpMgr.getLeftPapers(player.getCountry(), lootPaper);
        int index = RandomHelper.threadSafeRand(1, leftPapers.size());
        Integer itemId = leftPapers.get(index - 1);
        if (itemId == null) {
            LogHelper.CONFIG_LOGGER.info("item is null.");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        SimpleData simpleData = player.getSimpleData();
        simpleData.setPaperDiged(true);
        worldPvpMgr.updateDig(player.getCountry(), itemId, player.roleId);

        Award award = new Award(0, AwardType.PROP, itemId, 1);

        String propName = staticPropMgr.getStaticProp(itemId) == null ? "" : staticPropMgr.getStaticProp(itemId).getPropName();
        //修改阵营公告
        chatManager.sendCountryChat(player.getCountry(), ChatId.DIG_PAPER, String.valueOf(player.getCountry()), player.getNick(), propName);
        handleDigRq(player, award, staticPvpDig, handler);
    }

    public void handleDigRq(Player player, Award award, StaticPvpDig staticPvpDig, ClientHandler handler) {
        playerManager.addAward(player, award, Reason.DIG_PAPER);
        SimpleData simpleData = player.getSimpleData();
        simpleData.subScore(staticPvpDig.getDigCost());
        DigPropRs.Builder builder = DigPropRs.newBuilder();
        builder.setAward(award.wrapPb());
        builder.setScore(simpleData.getPvpScore());
        builder.setPosUsed(simpleData.getPosUsed());
        builder.setIsDigPaper(simpleData.isPaperDiged());
        handler.sendMsgToPlayer(DigPropRs.ext, builder.build());
        SpringUtil.getBean(com.game.log.LogUser.class).hatchery_log(
                HatcheryLog.builder()
                        .lordId(player.roleId)
                        .nick(player.getNick())
                        .lv(player.getLevel())
                        .title(player.getTitle())
                        .point(-staticPvpDig.getDigCost())
                        .source(HatcheryLog.DIG_PAPER)
                        .build()
        );
    }

    // 获取当前国家需要恭贺的玩家
    public void getGreetRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        HashMap<Integer, DigInfo> digInfoMap = worldPvpMgr.getDigInfo(player.getCountry());
        GetGreetRs.Builder builder = GetGreetRs.newBuilder();
        for (DigInfo digInfo : digInfoMap.values()) {
            CommonPb.DigInfo.Builder data = CommonPb.DigInfo.newBuilder();
            data.setItemId(digInfo.getItemId());
            long lordId = digInfo.getLordId();
            Player target = playerManager.getPlayer(lordId);
            if (target != null) {
                data.setNick(target.getNick());
            }
            builder.addDigInfo(data);
        }
        SimpleData simpleData = player.getSimpleData();
        builder.setBuyWordTimes(simpleData.getBuyWordTimes());
        builder.setPosUsed(simpleData.getPosUsed());
        builder.setPos(simpleData.getDigPos().wrapPb());
        builder.setIsDigPaper(simpleData.isPaperDiged());
        builder.setIsGreeted(simpleData.isGreeted());

        handler.sendMsgToPlayer(GetGreetRs.ext, builder.build());

    }

    // 兑换图纸
    public void exchangeRq(PvpBattlePb.ExchangeRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int propId = req.getPropId();
        StaticPvpExchange pvpExchange = staticWorldMgr.getNewBroodWarShop(propId);
        if (pvpExchange == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        SimpleData simpleData = player.getSimpleData();
        int pvpScore = simpleData.getPvpScore();
        if (pvpScore < pvpExchange.getScore()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_PVP_SCORE);
            return;
        }

        simpleData.subScore(pvpExchange.getScore());
        Award award = new Award(AwardType.PROP, propId, pvpExchange.getNum() == 0 ? 1 : pvpExchange.getNum());
        playerManager.addAward(player, award, Reason.EXCHANGE_PAPER);
        ExchangeRs.Builder builder = ExchangeRs.newBuilder();
        builder.setAward(award.wrapPb());
        builder.setScore(simpleData.getPvpScore());
        handler.sendMsgToPlayer(ExchangeRs.ext, builder.build());
        SpringUtil.getBean(com.game.log.LogUser.class).hatchery_log(
                HatcheryLog.builder()
                        .lordId(player.roleId)
                        .nick(player.getNick())
                        .lv(player.getLevel())
                        .title(player.getTitle())
                        .point(-pvpExchange.getScore())
                        .source(HatcheryLog.EXCHANGE)
                        .build()
        );
    }

    // 恭贺图纸[随机赠送一张图纸]
    public void greetAction(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        SimpleData simpleData = player.getSimpleData();
        if (simpleData.isGreeted()) {
            handler.sendErrorMsgToPlayer(GameError.PVP_IS_GREETED_PAPER);
            return;
        }

        simpleData.setGreeted(true);
        List<Integer> papers = staticLimitMgr.getAddtion(132);
        if (papers == null || papers.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        int index = RandomHelper.threadSafeRand(0, papers.size() - 1);
        Integer itemId = papers.get(index);
        if (itemId == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        Award award = new Award(AwardType.PROP, itemId, 1);
        playerManager.addAward(player, award, Reason.GREET_BANQUET);
        GreetActionRs.Builder builder = GreetActionRs.newBuilder();
        builder.setAward(award.wrapPb());
        handler.sendMsgToPlayer(GreetActionRs.ext, builder.build());
    }
}
