package com.game.worldmap.fight.process;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.define.Fight;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.Hero;
import com.game.domain.p.HeroAddExp;
import com.game.domain.p.Mail;
import com.game.domain.p.Team;
import com.game.domain.p.WorldMap;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.SerQuickWar;
import com.game.pb.WorldPb;
import com.game.util.LogHelper;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.PlayerCity;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.IWar;
import com.google.common.collect.HashBasedTable;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Component;

@Fight(warName = "闪电战", warType = {WarType.ATTACK_QUICK}, marthes = {MarchType.AttackCityQuick, MarchType.QUICK_ASSIST})
@Component
public class QuickWarProcess extends FightProcess {

    @Override
    public void init(int[] warTypes, int[] marches) {
        this.warTypes = warTypes;
        this.marches = marches;

        // 战斗等待过程
        registerProcess(WarState.Waiting, this::warWaiting);

        //注册行军
        registerMarch(MarchType.AttackCityQuick, MarchState.Begin, this::marchArrive);
        //registerMarch(MarchType.AttackCityQuick, MarchState.Fighting, this::doQuickAttack);
        registerMarch(MarchType.AttackCityQuick, MarchState.Back, this::doFinishedMarch);

        // 闪电战参与防守
        registerMarch(MarchType.QUICK_ASSIST, MarchState.Back, this::doFinishedMarch);
    }

    @Override
    public void checkMarch(MapInfo mapInfo, March march) {
    }


    public void warWaiting(MapInfo mapInfo, IWar war) {
        long now = System.currentTimeMillis();
        if (now >= war.getEndTime()) {// 等待结束
            WarInfo warInfo = (WarInfo) war;
            warInfo.setEndTime(warInfo.getEndTime() + 1000L);
            warInfo.setState(WarState.Fighting);
            return;
        }

        long defencerId = war.getDefencer().getId();
        Player target = playerManager.getPlayer(defencerId);
        if (target == null) {
            LogHelper.CONFIG_LOGGER.info("target is null!");
            return;
        }

        WarInfo warInfo = (WarInfo) war;
        Pos defendPos = war.getDefencer().getPos();
        Pos pos = target.getPos();

        int mapId = worldManager.getMapId(defendPos);
        if (target.getProectedTime() > now && mapId != MapId.FIRE_MAP) {// 防守方开启保护套

            for (March march : war.getAttacker().getMarchList()) {
                marchManager.doMarchReturn(mapInfo.getMapId(), march, MarchReason.FarProtectedTime);
                Player player = playerManager.getPlayer(march.getLordId());
                if (player != null) {
                    battleMailManager.sendProtectedMail(player, target);
                }
            }

            WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(warInfo);
            worldManager.synRemoveWar(target, synCityWarRq);

            worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
            warInfo.setState(WarState.Finish);
            return;
        }

        if (!defendPos.equals(pos)) {// 已经迁城
            for (March march : war.getAttacker().getMarchList()) {//进攻方行军返回
                marchManager.doMarchReturn(mapInfo.getMapId(), march, MarchReason.HighMove);
                Player player = playerManager.getPlayer(march.getLordId());
                if (player != null) {//不战而逃,我方开始返回
                    playerManager.sendNormalMail(player, MailId.ESCAPE_WAR, target.getNick());
                }
            }

            for (March march : war.getDefencer().getMarchList()) {//防守方行军返回
                marchManager.doMarchReturn(mapInfo.getMapId(), march, MarchReason.HighMove);
            }

            WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(warInfo);
            worldManager.synRemoveWar(target, synCityWarRq);

            worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
            warInfo.setState(WarState.Finish);
        }
    }


    /**
     * 行军抵达
     *
     * @param mapInfo
     * @param march
     */
    private void marchArrive(MapInfo mapInfo, March march) {
        long targetId = march.getDefencerId();
        long lordId = march.getLordId();
        Player player = playerManager.getPlayer(lordId);
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("quick player is null!");
            warManager.handleLostTarget(march, mapInfo, LostTargetReason.PLAYER_NULL);
            return;
        }

        Player target = playerManager.getPlayer(targetId);
        if (target == null) {
            LogHelper.CONFIG_LOGGER.info("target player is null!");
            warManager.handleLostTarget(march, mapInfo, LostTargetReason.TARGET_NULL);
            return;
        }

        // 玩家已迁城
        Pos marchEndPos = march.getEndPos();
        if (marchEndPos != null && marchEndPos.getX() != target.getPosX() && marchEndPos.getY() != target.getPosY()) {
            warManager.handleLostTarget(march, mapInfo, LostTargetReason.TARGET_FLY);
            return;
        }

        long now = System.currentTimeMillis();

        // 如果不是发起者，则进入等待, 否则进行战斗
        long warId = march.getWarId();
        WarInfo warInfo = (WarInfo) mapInfo.getWar(warId);
        if (warInfo == null) {
            warManager.handleLostTarget(march, mapInfo, LostTargetReason.NO_QUICK_WAR);
            return;
        }

        if (lordId == warInfo.getAttackerId()) {
            march.setState(MarchState.Fighting);
            worldManager.synMarch(mapInfo.getMapId(), march);
            doQuickAttack(mapInfo, march);
        } else { // add in defence
            march.setState(MarchState.Waiting);
            march.setEndTime(warInfo.getEndTime());
            long period = warInfo.getEndTime() - now;
            period = Math.max(0, period);
            march.setPeriod(period);
            // syn march
            worldManager.synMarch(mapInfo.getMapId(), march);
        }
    }

    /**
     * 闪电战
     *
     * @param march   进攻方的行军
     * @param mapInfo
     */
    public void doQuickAttack(MapInfo mapInfo, March march) {
        Entity entity = mapInfo.getEntity(march.getEndPos());
        if (entity == null) {
            // 目标点丢失, 发送邮件
            warManager.handleLostTarget(march, mapInfo, LostTargetReason.QUICK_ENTITY_NULL);
            return;
        }

        if (!(entity instanceof PlayerCity)) {
            // 目标点丢失, 发送邮件
            warManager.handleLostTarget(march, mapInfo, LostTargetReason.NOT_PLAYER_CITY);
            return;
        }

        PlayerCity playerCity = (PlayerCity) entity;
        long targetLordId = march.getDefencerId();
        if (targetLordId != playerCity.getLordId()) {
            // 目标点丢失,发送邮件
            warManager.handleLostTarget(march, mapInfo, LostTargetReason.PLAYER_CITY_ERROR);
            return;
        }

        long lordId = march.getLordId();
        // 找到玩家
        Player player = playerManager.getPlayer(lordId);
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("quick player is null!");
            return;
        }

        Player target = playerManager.getPlayer(targetLordId);
        if (target == null) {
            LogHelper.CONFIG_LOGGER.info("target player is null!");
            return;
        }

        // 通知玩家删除战斗
        long warId = march.getWarId();
        WarInfo warInfo = (WarInfo) mapInfo.getWar(warId);
        if (warInfo == null) {
            LogHelper.CONFIG_LOGGER.info("warInfo is not exist!");
            warManager.handleLostTarget(march, mapInfo, LostTargetReason.DO_QUICK_ATTACK_WAR_NULL);
            return;
        }

        // 创建行军team和玩家team进行pk
        // 玩家Team: 友军+玩家Team+城防军
        // 我方的战队, 无论是国战还是攻城战,都是team vs team
        Team attacker = battleMgr.initPlayerTeam(player, march.getHeroIds(), BattleEntityType.HERO, true, 0, true);
        // 敌方的战队
        Team defencer = battleMgr.initDefencer(warInfo, BattleEntityType.HERO, false);
        Random rand = new Random(System.currentTimeMillis());
        // seed 开始战斗
        battleMgr.doTeamBattle(attacker, defencer, rand, ActPassPortTaskType.IS_WORLD_WAR);
        activityManager.calcuKillAll(warInfo, attacker, defencer);
        HeroAddExp heroAddExp = worldManager.caculateTeamKill(attacker, player.roleId);
        // 计算击杀经验值
        worldManager.caculateTeamDefenceKill(defencer);

        // 处理玩家扣血
        HashMap<Integer, Integer> attackRec = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> defenceRec = new HashMap<Integer, Integer>();
        // 计算攻击方的血量
        worldManager.caculatePlayer(attacker, player, attackRec);
        HashMap<Long, HashMap<Integer, Integer>> allDefenceRec = new HashMap<>();
        worldManager.handleDefenceSoldier(defencer, target, MarchReason.QuickAttack, allDefenceRec);
        defenceRec = allDefenceRec.get(player.getLord().getLordId());

        // 同步所有参战人员的属性变化, 扣除血量
        HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();
        handleQuickWarHp(warInfo, attacker, defencer, allSoldierRec, WarType.ATTACK_QUICK);
        if (attacker.isWin()) { // 攻击方胜利, 防守方城池被干飞
            // send mail
            // 玩家掉落的值
            List<Award> robeAward = worldManager.getLost(target);
            // 掉落的20%是可以抢的
            List<Award> gotAward = worldManager.getGot(target);
            // 是否超过掠夺上限
            boolean isLimit = worldManager.isLimit(player, gotAward);
            // 玩家实际能抢的值
            gotAward = worldManager.getRobBuyWareHouser(player, gotAward);
            if (isLimit) {
                List<Award> calReward = new ArrayList<>();
                gotAward.forEach(e -> {
                    calReward.add(e);
                });
                // 损失按照1.5倍
                robeAward = worldManager.getLostMax(calReward);
                // 不能超过最大仓库保护下限
                robeAward = worldManager.getLostMax(target, robeAward);
            }
            // 双旦活动掉落
            List<Award> actDoubleEgg = activityService.actDoubleEggReward(player, false);
            gotAward.addAll(actDoubleEgg);
            for (Award award : gotAward) {
                march.addAwards(award);
            }
            marchManager.handleMarchReturn(march, MarchReason.QuickAttackWin);

            battleMailManager.sendCityReport(target, player);

            // 删除防守者是自己的战斗
            worldManager.callDefecneOtherMarchReturn(target, warInfo.getWarId(), MarchReason.QuickPlayerFly);
            worldManager.handPlayerLost(target, robeAward);
            countryManager.doCountryTask(player, CountryTaskType.PVP_CITY_WIN, 1);

            battleMailManager.handleSendQuickWar(attacker, defencer, player, target, robeAward, gotAward, heroAddExp, warInfo, attackRec, defenceRec, allSoldierRec);


            if (mapInfo.getMapId() != MapId.FIRE_MAP) {
                worldManager.handPlayerLost(target, robeAward);
                worldManager.playerFly(target); // 玩家被击飞
            } else {
                flameWarService.playerFly(target);
            }


            if (actDoubleEgg.size() > 0) {
                Mail mail = playerManager.sendNormalMail(player, MailId.NATIONL_DAY);
                List<CommonPb.Award> awards = new ArrayList<>();
                actDoubleEgg.forEach(e -> {
                    awards.add(e.wrapPb().build());
                });
                mail.setAward(awards);
                mail.setAwardGot(2);
            }

            int sortId = 14000 + target.getCommandLv();
            ActivityEventManager.getInst().activityTip(EventEnum.CITY_WAR_WIN, player, 1, 0);

            activityManager.updActSeven(player, ActivityConst.TYPE_ADD, sortId, 0, 1);
            activityManager.updActSeven(player, ActivityConst.TYPE_ADD, 15000, 0, 1);
            activityManager.updActWorldBattle(player, ActivityConst.TYPE_ADD, ActWorldBattleConst.ATK_CITY, 0, 1);
            activityManager.updActPerson(player, ActivityConst.ACT_CITY_RANK, 1, 0);
            worldBoxManager.calcuPoints(WorldBoxTask.CITY_FIGHT, player, 1);

            activityManager.updActPerson(player, ActivityConst.ACT_SQUA, 1, NineCellConst.CELL_2);
        } else { // 防守方胜利
            // 直接回城
            marchManager.handleMarchReturn(march, MarchReason.QuickAttackFailed);
            battleMailManager.handleSendQuickWar(attacker, defencer, player, target, new ArrayList<Award>(), new ArrayList<Award>(), heroAddExp, warInfo, attackRec, defenceRec, allSoldierRec);
        }
        // 统计累计杀敌
        if (mapInfo.getMapId() == MapId.FIRE_MAP) {
            flameWarService.calKill(player, attacker);
            flameWarService.calKill(target, defencer);
        }
        // 通知所有在线玩家进攻方的行军变更
        worldManager.synMarch(mapInfo.getMapId(), march);

        // 所有防守方回城
        worldManager.soldierAutoAdd(target);
        synAllPlayerChange(warInfo);
        handleQucikWarMarch(warInfo, mapInfo, MarchReason.QUICK_WAR_FINISH);
        // 通知参战双方删除战斗
        warManager.handePvpWarRemove(warInfo);
        playerManager.synChange(player, Reason.QUICK_WAR);
        playerManager.synChange(target, Reason.QUICK_WAR);
        // 处理城战排行
        // handleQuickWarRank(player, target);
        // 城战荣誉
        countryManager.updCountryHoror(player, CountryConst.RANK_CITY);
        // 删除闪电战役
        worldManager.removeQuickWar(mapInfo.getMapId(), march.getWarId());
        player.addAccackPlayerCity();
        target.addAccackPlayerCity();
        dailyTaskManager.record(DailyTaskId.CITY_WAR, player, 1);
        // 战斗结束
        warInfo.setEnd(true);
    }

    // 闪电战
    public void handleQucikWarMarch(WarInfo warInfo, MapInfo mapInfo, int reason) {
        ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
        ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
        HashSet<Long> attackers = new HashSet<Long>();
        for (March march : attackerMarches) {
            long lordId = march.getLordId();
            Player player = playerManager.getPlayer(lordId);
            if (player != null) {
                // 处理自动补兵
                //soldierManager.autoAdd(player, march.getHeroIds());
                attackers.add(lordId);
            }
        }
        for (Long lordId : attackers) {
            Player player = playerManager.getPlayer(lordId);
            if (player == null) {
                continue;
            }
            playerManager.synChange(player, Reason.ATTACK_CITY);
        }

        for (March march : defencerMarches) {
            marchManager.handleMarchReturn(march, reason);
            worldManager.synMarch(mapInfo.getMapId(), march);
            long lordId = march.getLordId();
            Player player = playerManager.getPlayer(lordId);
            if (player != null) {
                // 处理自动补兵
                //soldierManager.autoAdd(player, march.getHeroIds());
                playerManager.synChange(player, Reason.ATTACK_CITY);
                worldManager.synPlayerFlyMarch(player, march, mapInfo.getMapId());
            }
        }
    }

    // 远征或者奔袭扣除血量
    public void handleQuickWarHp(WarInfo warInfo, Team attacker, Team defencer,
                                 HashBasedTable<Long, Integer, Integer> allSoldierRec, int warType) {
        handleWarChange(warInfo.getAttackerId(), attacker, allSoldierRec, warType);
        handleWarChange(warInfo.getDefencerId(), defencer, allSoldierRec, warType);
    }

    // 扣除血量, 以及增加荣誉值
    public void handleWarChange(long lordId, Team team,
                                HashBasedTable<Long, Integer, Integer> allSoldierRec, int warType) {
        Player player = playerManager.getPlayer(lordId);
        if (player == null) {
            return;
        }
        int soilder = 0;
        for (BattleEntity battleEntity : team.getAllEnities()) {
            Hero hero = null;
            if (battleEntity.getEntityType() != BattleEntityType.WALL_DEFENCER) {
                hero = player.getHero(battleEntity.getEntityId());
                if (hero == null || battleEntity.getLordId() != player.roleId) {
                    continue;
                }
            }
            if (hero != null) {
                warManager.handlePlayerSoldierRec(player.roleId, hero, battleEntity, allSoldierRec);
                hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
            }
            double techAdd = techManager.getHonorAdd(battleEntity.getLordId());
            int lastHonor = battleMailManager.getHonor(battleEntity, warType);
            int honor = (int) ((double) lastHonor * (1.0f + techAdd));
            playerManager.addAward(player, AwardType.LORD_PROPERTY, LordPropertyType.HONOR, honor,
                    Reason.ATTACK_CITY);

            soilder += battleEntity.getMaxSoldierNum() - battleEntity.getCurSoldierNum();
        }

        if (soilder > 0) {
            activityManager.updActPerson(player, ActivityConst.ACT_SOILDER_RANK, soilder, 0);
        }
    }

    @Override
    public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
        if (worldMap.getQuickWarData() == null) {
            return;
        }
        SerQuickWar serQuickWar = null;
        try {
            serQuickWar = SerQuickWar.parseFrom(worldMap.getQuickWarData());
            if (serQuickWar == null) {
                return;
            }

            List<DataPb.WarData> warDatas = serQuickWar.getWarDataList();
            if (warDatas == null) {
                return;
            }

            for (DataPb.WarData warData : warDatas) {
                if (warData == null) {
                    LogHelper.CONFIG_LOGGER.info("warDatas is null!");
                    continue;
                }
                DataPb.PosData pos = warData.getDefencerPos();
                if (pos.getX() == 0 && pos.getY() == 0) {
                    continue;
                }

                WarInfo warInfo = warManager.createFarWar(warData, mapInfo);
                if (warInfo.getEndTime() < System.currentTimeMillis()) {
                    continue;
                }

                mapInfo.addWar(warInfo);
                worldManager.flushWar(warInfo, true, warInfo.attacker.getCountry());
            }
        } catch (InvalidProtocolBufferException e) {
            LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
        }
    }


}
