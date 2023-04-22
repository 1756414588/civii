package com.game.manager;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.BattleEntityType;
import com.game.constant.BookEffectType;
import com.game.constant.CastleConsts;
import com.game.constant.CityType;
import com.game.constant.DevideFactor;
import com.game.constant.RiotBuff;
import com.game.constant.SoldierType;
import com.game.dataMgr.StaticBattleDataMgr;
import com.game.dataMgr.StaticHeroMgr;
import com.game.dataMgr.StaticLordDataMgr;
import com.game.dataMgr.StaticMeetingTaskMgr;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.dataMgr.StaticWallMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.p.AttackInfo;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.BattleProperty;
import com.game.domain.p.City;
import com.game.domain.p.CityMonster;
import com.game.domain.p.Hero;
import com.game.domain.p.LineEntity;
import com.game.domain.p.Property;
import com.game.domain.p.RecordEntity;
import com.game.domain.p.SimpleData;
import com.game.domain.p.Team;
import com.game.domain.p.Wall;
import com.game.domain.p.WallDefender;
import com.game.domain.p.WallFriend;
import com.game.domain.s.StaticBattle;
import com.game.domain.s.StaticLordLv;
import com.game.domain.s.StaticMonster;
import com.game.domain.s.StaticWallMonsterLv;
import com.game.domain.s.StaticWorldCity;
import com.game.season.SeasonManager;
import com.game.season.SeasonService;
import com.game.season.SkillModel;
import com.game.season.StaticSeasonMgr;
import com.game.season.seven.entity.SevenType;
import com.game.season.talent.entity.EffectType;
import com.game.service.CastleService;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.worldmap.March;
import com.game.worldmap.SuperGuard;
import com.game.worldmap.SuperResource;
import com.game.worldmap.WarInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BattleMgr {

    @Autowired
    private HeroManager heroDataManager;

    @Autowired
    private StaticMonsterMgr staticMonsterMgr;

    @Autowired
    private StaticBattleDataMgr staticBattleDataMgr;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticWorldMgr staticWorldMgr;

    @Autowired
    private CityManager cityManager;

    @Autowired
    private StaticWallMgr staticWallMgr;

    @Autowired
    private StaticHeroMgr staticHeroMgr;

    @Autowired
    private StaticMonsterMgr monsterMgr;

    @Autowired
    private StaticWallMgr wallMgr;

    @Autowired
    private StaticLordDataMgr staticLordDataMgr;

    @Autowired
    private TestManager testManager;

    @Autowired
    private StaticMeetingTaskMgr staticMeetingTaskMgr;

    @Autowired
    private CastleService castleService;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private TechManager techManager;

    @Autowired
    private WarBookManager warBookManager;

    @Autowired
    StaticSeasonMgr staticSeasonMgr;
    @Autowired
    SeasonManager seasonManager;
    @Autowired
    SeasonService seasonService;

    // GameEntities pk GameEntities
    // Team VS Team(所有战斗)
    // GameEntities: 玩家英雄，关卡怪物，城防军[可以为空啊]
    public void doTeamBattle(Team teamA, Team teamB, Random rand, boolean isWorldWar) {
        if (teamA.aliveNum() <= 0) {
            teamA.setWin(false);
            teamB.setWin(true);
            return;
        }
        if (teamB.aliveNum() <= 0) {
            teamA.setWin(true);
            teamB.setWin(false);
            return;
        }
        int beforeHpA = teamA.getCurSoldier();
        int beforeHpB = teamB.getCurSoldier();
        BattleEntity battleEntityA = teamA.getEntity();
        BattleEntity battleEntityB = teamB.getEntity();
        while (battleEntityA != null && battleEntityB != null) {
            battleEntityA.initSkill();
            battleEntityB.initSkill();
            doEntityBattle(teamA, teamB, battleEntityA, battleEntityB, rand, isWorldWar);
            if (battleEntityB.getLineNumber() <= 0) {
                battleEntityDie(teamB.getAttackInfos());
                removeBattleBookSkillEffectForEntity(teamA.getColumn());
                battleEntityB = teamB.getEntity();
            }
            if (battleEntityA.getLineNumber() <= 0) {
                battleEntityDie(teamA.getAttackInfos());
                removeBattleBookSkillEffectForEntity(teamB.getColumn());
                battleEntityA = teamA.getEntity();
            }
        }
        int afterHpA = teamA.getCurSoldier();
        int afterHpB = teamB.getCurSoldier();

        teamA.addKillNum(beforeHpB - afterHpB);
        teamB.addKillNum(beforeHpA - afterHpA);

        teamA.getColumn().clear();
        teamB.getColumn().clear();
        if (teamA.isAlive()) {
            teamA.setWin(true);
            teamB.setWin(false);
        } else {
            teamA.setWin(false);
            teamB.setWin(true);
        }

    }


    // 记录entity死亡
    public void battleEntityDie(ArrayList<AttackInfo> attackInfos) {
        if (!attackInfos.isEmpty()) {
            AttackInfo attackInfo = attackInfos.get(attackInfos.size() - 1);
            if (attackInfo != null) {
                attackInfo.setStatus(4);
            }
        }
    }

    public void doEntityBattle(Team teamA, Team teamB, BattleEntity battleEntityA, BattleEntity battleEntityB, Random rand, boolean isWorldWar) {
        int lineA = battleEntityA.getLineNumber(); // A列数
        int lineB = battleEntityB.getLineNumber(); // B列数
        LineEntity lineEntityA = battleEntityA.getLineEntity();
        LineEntity lineEntityB = battleEntityB.getLineEntity();
        int beforeHpA = battleEntityA.getLeftSoldier();
        int beforeHpB = battleEntityB.getLeftSoldier();

        long lordIdB = battleEntityB.getLordId();
        Player playerB = playerManager.getPlayer(lordIdB);
        long lordIdA = battleEntityA.getLordId();
        Player playerA = playerManager.getPlayer(lordIdA);
        while (lineA > 0 && lineB > 0) {
            doLineBattle(teamA, teamB, battleEntityA, playerA, battleEntityB, playerB, lineEntityA, lineEntityB, rand);
            if (lineEntityB.getSoldierNum() <= 0) {
                lineEntityDie(teamB.getAttackInfos());
                --lineB; // 如果等于0，直接结束
                // 计算兵书的技能加成
                addBattleBookSkillEffectTotal(battleEntityB, teamB.getColumn(), lineEntityB, BookEffectType.BOOK_EFFECT_10);
                addBattleBookSkillEffectTotal(battleEntityA, teamA.getColumn(), lineEntityA, BookEffectType.BOOK_EFFECT_15);
                if (lineB > 1) {
                    // 计算兵书的技能加成
                    addBattleBookSkillEffectTotal(battleEntityB, teamB.getColumn(), lineEntityB, BookEffectType.BOOK_EFFECT_17);
                    lineEntityB.setSoldierNum(battleEntityB.getSoldierNum());
                } else if (lineB == 1) {
                    // 计算兵书的技能加成
                    addBattleBookSkillEffectTotal(battleEntityB, teamB.getColumn(), lineEntityB, BookEffectType.BOOK_EFFECT_17);
                    lineEntityB.setSoldierNum(battleEntityB.getLastLineSoldierNum());
                    battleEntityB.setLeftSoldier(0);
                }
                teamB.addLine();
            }
            if (lineEntityA.getSoldierNum() <= 0) {
                lineEntityDie(teamA.getAttackInfos());
                --lineA; // 如果等于0，直接结束
                // 计算兵书的技能加成
                addBattleBookSkillEffectTotal(battleEntityA, teamA.getColumn(), lineEntityA, BookEffectType.BOOK_EFFECT_10);
                addBattleBookSkillEffectTotal(battleEntityB, teamB.getColumn(), lineEntityB, BookEffectType.BOOK_EFFECT_15);

                if (lineA > 1) {
                    // 计算兵书的技能加成
                    addBattleBookSkillEffectTotal(battleEntityA, teamA.getColumn(), lineEntityA, BookEffectType.BOOK_EFFECT_17);
                    lineEntityA.setSoldierNum(battleEntityA.getSoldierNum());
                } else if (lineA == 1) {
                    // 计算兵书的技能加成
                    addBattleBookSkillEffectTotal(battleEntityA, teamA.getColumn(), lineEntityA, BookEffectType.BOOK_EFFECT_17);
                    lineEntityA.setSoldierNum(battleEntityA.getLastLineSoldierNum());
                    battleEntityA.setLeftSoldier(0);
                }
                teamA.addLine();
            }
        }
        // if (lineB <= 0) {
        // battleEntityB.setLineNumber(0);
        // battleEntityB.setCurSoldierNum(0);
        // // break;
        // }
        // if (lineA <= 0) {
        // battleEntityA.setLineNumber(0);
        // battleEntityA.setCurSoldierNum(0);
        // }

        // 设置排数，进行下一个实体的战斗
        battleEntityA.setLineNumber(lineA);
        battleEntityB.setLineNumber(lineB);

        battleEntityA.cacSoldierNum();
        battleEntityB.cacSoldierNum();

        int afterHpA = battleEntityA.getLeftSoldier();
        int afterHpB = battleEntityB.getLeftSoldier();

        // 计算击杀的血量
        int killB = beforeHpB - afterHpB;
        int killA = beforeHpA - afterHpA;
        battleEntityA.addKillNum(killB);
        battleEntityB.addKillNum(killA);

        // 损兵数
        battleEntityB.addLostNum(killB);
        battleEntityA.addLostNum(killA);

        updateKill(battleEntityA, killB);

        updateKill(battleEntityB, killA);
        if (killB < 0) {
            LogHelper.ERROR_LOGGER.error("killB < 0 beforeHpB = " + beforeHpB + ", afterHpB = " + afterHpB);
            LogHelper.ERROR_LOGGER.error("battleEntityB Id= " + battleEntityB.getEntityId() + ", type = " + battleEntityB.getEntityType());
            LogHelper.ERROR_LOGGER.error("kbattleEntityB line= " + battleEntityB.getLineNumber() + ", soldierNum = " + battleEntityB.getLineEntity().getSoldierNum());
        }

        if (killA < 0) {
            LogHelper.ERROR_LOGGER.error("killA < 0 beforeHpA = " + beforeHpA + ", afterHpA = " + afterHpA);
            LogHelper.ERROR_LOGGER.error("battleEntityA Id= " + battleEntityA.getEntityId() + ", type = " + battleEntityA.getEntityType());
            LogHelper.ERROR_LOGGER.error("kbattleEntityA line= " + battleEntityA.getLineNumber() + ", soldierNum = " + battleEntityA.getLineEntity().getSoldierNum());
        }

        if (isWorldWar) {
            if (playerA != null) {
                ActivityEventManager.getInst().activityTip(EventEnum.LOSE_SOLDIER, playerA, killA, 0);
//                activityManager.updatePassPortTaskCond(playerA, ActPassPortTaskType.LOSS_SOLDIER, killA);
                seasonService.addSevenScore(SevenType.KILL, killB, playerA, 1);
                seasonService.addSevenScore(SevenType.LOSS, killA, playerA, 1);

            }
            if (playerB != null) {
                ActivityEventManager.getInst().activityTip(EventEnum.LOSE_SOLDIER, playerB, killB, 0);
//                activityManager.updatePassPortTaskCond(playerB, ActPassPortTaskType.LOSS_SOLDIER, killB);

                seasonService.addSevenScore(SevenType.KILL, killA, playerB, 1);
                seasonService.addSevenScore(SevenType.LOSS, killB, playerA, 1);
            }
        }
    }

    // 记录entity死亡
    public void lineEntityDie(ArrayList<AttackInfo> attackInfos) {
        if (!attackInfos.isEmpty()) {
            AttackInfo attackInfo = attackInfos.get(attackInfos.size() - 1);
            if (attackInfo != null) {
                attackInfo.setStatus(3);
            }
        }
    }

    // 最本质层
    public void doLineBattle(Team teamA, Team teamB, BattleEntity battleEntityA, Player playerA, BattleEntity battleEntityB, Player playerB, LineEntity lineEntityA, LineEntity lineEntityB, Random random) {
        if (lineEntityA == null || lineEntityB == null) {
            LogHelper.ERROR_LOGGER.error("lineEntityA == null || lineEntityB == null");
            return;
        }
        //if (lineEntityA.getSoldierNum() <= 0 || lineEntityB.getSoldierNum() <= 0) {
        //	return;
        //}
        boolean isFirstBattle = true;
        while (lineEntityA.getSoldierNum() > 0 && lineEntityB.getSoldierNum() > 0) {
            double hpPercentA = (double) lineEntityA.getSoldierNum() / (double) lineEntityA.getMaxSoldierNum();
            double hpPercentB = (double) lineEntityB.getSoldierNum() / (double) lineEntityB.getMaxSoldierNum();
            double factorA = hpPercentA / hpPercentB;
            double factorB = hpPercentB / hpPercentA;
            factorA = getRealAttackFactor(factorA);
            factorB = getRealAttackFactor(factorB);

            addBattleBookSkillEffect(battleEntityA, teamA.getColumn(), lineEntityA);
            addBattleBookSkillEffect(battleEntityB, teamB.getColumn(), lineEntityB);

            doAttack(teamA, teamB, playerA, playerB, lineEntityA, lineEntityB, random, factorA, isFirstBattle, battleEntityA);
            doAttack(teamB, teamA, playerB, playerA, lineEntityB, lineEntityA, random, factorB, isFirstBattle, battleEntityB);
            isFirstBattle = false;
            removeBattleBookSkillEffect(teamA.getColumn());
            removeBattleBookSkillEffect(teamB.getColumn());
        }
    }

    // 浮点型比较
    public double getRealAttackFactor(double factor) {
        if (factor < 0.1) {
            return 0.75;
        } else if (factor >= 0.1 && factor < 0.3) {
            return 0.85;
        } else if (factor >= 0.3 && factor < 1.0) {
            return 0.95;
        } else if (factor == 1.0) {
            return 1.0;
        } else if (factor > 1.0 && factor < 1.2) {
            return 1.1;
        } else if (factor >= 1.2 && factor < 1.5) {
            return 1.2;
        } else if (factor >= 1.5 && factor < 2.0) {
            return 1.25;
        } else if (factor >= 2.0) {
            return 1.4;
        }

        return 0.0;
    }

    public void doAttack(Team attack, Team def, Player playerA, Player playerB, LineEntity lineEntityA, LineEntity lineEntityB, Random random, double additionFactor, boolean isFirstBattle, BattleEntity entity) {
        // 第1版:战斗公式:每回合伤害=[（A攻击-B防御）* 浮动系数[1/1000]+（A强攻-B韧性）]*
        // 暴击伤害（2倍）*克制关系系数[A->B]
        // 第2版:战斗公式：[（攻击-防御）/4*浮动系数*系数+（强攻-强防）/4]*暴击伤害（2倍）*克制关系系数
        // 浮动系数 浮动系数=0.98-1.02 [980~1021), 最好写到配置表 TODO
        // 下面的计算是相互的，会写成一个函数
        AttackInfo attackInfo = new AttackInfo();
        int attackRes = lineEntityA.getAttack() - lineEntityB.getDefence();
        attackRes = Math.max(attackRes, 0);
        StaticBattle staticBattle = staticBattleDataMgr.getStaticBattle();
        if (staticBattle == null) {
            LogHelper.ERROR_LOGGER.error("staticBattle == null, check config!");
            return;
        }

        List<Integer> floatFactor = staticBattle.getFloatFactor();
        if (floatFactor.size() != 2) {
            LogHelper.ERROR_LOGGER.error("floatFactor.size() != 2");
            return;
        }

        int minFloatFactor = floatFactor.get(0);
        int maxFloatFactor = floatFactor.get(1);

        int attckFactor = RandomHelper.nextInt(random, minFloatFactor, maxFloatFactor);
        int strongAttackRes = lineEntityA.getStrongAttack() - lineEntityB.getStrongDefence();

        double seasonBuf = seasonManager.getSeasonBuf(playerA, EffectType.EFFECT_TYPE10);
        int v = (int) (1 + seasonBuf) * strongAttackRes;

        strongAttackRes = Math.max(v, 0);

        double critiRes = (double) (lineEntityA.getCriti() - lineEntityB.getTenacity()) * ((double) staticBattle.getCritiFactor() / getDevideFactor());

        // 暴击设置上下限
        critiRes = Math.min(critiRes, staticBattle.getMaxCriti());
        critiRes = Math.max(critiRes, staticBattle.getMinCriti());

        // 兵书技能加成战斗中战斗中对反坦克炮暴击增加30%
        if (attack.getColumn().get(BookEffectType.BOOK_EFFECT_16) != null) {
            if (lineEntityB.getEntityType() == 1) {
                Integer bookEffect = attack.getColumn().get(BookEffectType.BOOK_EFFECT_16);
                Integer warBookSoldierType = warBookManager.getWarBookSoldierType(playerB, lineEntityB.getEntityId());
                if (null != warBookSoldierType && warBookSoldierType.intValue() == BookEffectType.SOLDIER_TYPE_118) {
                    critiRes = critiRes * (1 + (bookEffect.intValue() / 1000.00f));
                }
            }
        }
        // 暴击设置上下限
        critiRes = Math.min(critiRes, staticBattle.getMaxCriti());
        critiRes = Math.max(critiRes, staticBattle.getMinCriti());

        double restraintRate = getRestraint(playerA, lineEntityA.getSoldierType(), lineEntityB.getSoldierType());
        double missRes = (double) (lineEntityB.getMiss() - lineEntityA.getHit()) * ((double) staticBattle.getMissFactor() / getDevideFactor());

        missRes = Math.min(missRes, staticBattle.getMaxMiss());
        missRes = Math.max(missRes, staticBattle.getMinMiss());

        double critiRate = 1.0;
        boolean isCriti;

        // 1.检测是否命中
        boolean isMiss = RandomHelper.isBattleActed((int) missRes);
        int damage = 0; // 最小伤害是1
        SkillModel model = entity.getModel();
        // 如果命中
        if (!isMiss || (model != null && model.getTotalDamage() > 0)) {
            // 2.检测是否暴击
            isCriti = RandomHelper.isBattleActed((int) critiRes);

            // 兵书技能加成战斗中每排兵出场对幻影坦克的首次伤害必定暴击
            if (attack.getColumn().get(BookEffectType.BOOK_EFFECT_13) != null && isFirstBattle) {
                if (lineEntityB.getEntityType() == 1) {
                    Integer warBookSoldierType = warBookManager.getWarBookSoldierType(playerB, lineEntityB.getEntityId());
                    if (null != warBookSoldierType && warBookSoldierType.intValue() == BookEffectType.SOLDIER_TYPE_114) {
                        isCriti = true;
                    }
                }
            }

            if (isCriti) {
                critiRate = (double) staticBattle.getCritiDamage() / getDevideFactor();
                attackInfo.setStatus(2);
            }
            double attackFactorRes = (double) attckFactor / getDevideFactor();
            int attackCity = 0;
            if (playerA != null && playerB != null) {
                attackCity = lineEntityA.getBaseProperty().getAttackCity() - lineEntityB.getBaseProperty().getDefenceCity();
                attackCity = Math.max(attackCity, 0);
            }
            damage = (int) (((double) attackRes / 4.0 * attackFactorRes * additionFactor + (double) strongAttackRes / 4.0 + (double) attackCity / 4.0) * critiRate * restraintRate);

            // 计算兵书技能的技能加成伤害值
            Set<Map.Entry<Integer, Integer>> effectEntriesA = attack.getColumn().entrySet();
            for (Map.Entry<Integer, Integer> entry : effectEntriesA) {
                Integer effectKey = entry.getKey();
                Integer effectValue = entry.getValue();
                if (effectKey.intValue() == BookEffectType.BOOK_EFFECT_10) {
                    damage = (int) (damage * (1 + (effectValue.intValue() / 1000.00f)));
                } else if (effectKey.intValue() == BookEffectType.BOOK_EFFECT_14) {
                    if (lineEntityB.getSoldierType() == SoldierType.ROCKET_TYPE) {
                        damage = (int) (damage * (1 + (effectValue.intValue() / 1000.00f)));
                    }
                } else if (effectKey.intValue() == BookEffectType.BOOK_EFFECT_17) {
                    damage = (int) (damage * (1 + (effectValue.intValue() / 1000.00f)));
                } else if (effectKey.intValue() == BookEffectType.BOOK_EFFECT_18) {
                    if (lineEntityB.getSoldierType() == SoldierType.TANK_TYPE) {
                        damage = (int) (damage * (1 + (effectValue.intValue() / 1000.00f)));
                    }
                } else if (effectKey.intValue() == BookEffectType.BOOK_EFFECT_19) {
                    if (lineEntityB.getEntityType() == 1) {
                        Integer warBookSoldierType = warBookManager.getWarBookSoldierType(playerB, lineEntityB.getEntityId());
                        if (null != warBookSoldierType && warBookSoldierType.intValue() == BookEffectType.SOLDIER_TYPE_112) {
                            damage = (int) (damage * (1 + (effectValue.intValue() / 1000.00f)));
                        }
                    }
                }
            }

            Set<Map.Entry<Integer, Integer>> effectEntriesB = def.getColumn().entrySet();
            for (Map.Entry<Integer, Integer> entry : effectEntriesB) {
                Integer effectKey = entry.getKey();
                Integer effectValue = entry.getValue();
                if (effectKey.intValue() == BookEffectType.BOOK_EFFECT_15) {
                    damage = (int) (damage * (1 - (effectValue.intValue() / 1000.00f)));
                } else if (effectKey.intValue() == BookEffectType.BOOK_EFFECT_12) {
                    if (lineEntityA.getSoldierType() == SoldierType.WAR_CAR) {
                        damage = (int) (damage * (1 - (effectValue.intValue() / 1000.00f)));
                    }
                }
            }
            damage = Math.max(damage, 3);

            // EFFECT_TYPE11(11, "城战时，5到8排受到的首次非技能伤害变为1"),被攻击者有这个buf 5-8排首次收到攻击生效
            int buf = seasonManager.getBuf(playerB, EffectType.EFFECT_TYPE11);
            if (buf > 0) {
                if (def.isEffectLine()) {
                    damage = 1;
                }
            }
            // 如果有技能伤害 在第一次碰撞直接全部打出来

            if (model != null && model.getTotalDamage() > 0) {
                damage += model.getTotalDamage();
                entity.getModel().setTotalDamage(0);
                AttackInfo attackInfo1 = new AttackInfo();
                attackInfo1.setType(1);
                attackInfo1.setSkillId(model.getSkillId());
                def.getAttackInfos().add(attackInfo1);

                AttackInfo attackInfo2 = new AttackInfo();
                attackInfo2.setType(2);
                attackInfo2.setSkillId(model.getSkillId());
                attack.getAttackInfos().add(attackInfo2);
            }
            double seasonA = seasonManager.getSeasonBuf(playerA, EffectType.EFFECT_TYPE36);
            double seasonB = seasonManager.getSeasonBuf(playerB, EffectType.EFFECT_TYPE37);
            damage = (int) Math.ceil(damage * Math.abs(1 + seasonA - seasonB));
        } else {
            attackInfo.setStatus(1); // 闪避不扣血
        }

        /*
         * // 计算B的剩余血量, damage: A对B造成的伤害 int soldierNumB = lineEntityB.getSoldierNum(); int soldierNumA = lineEntityA.getSoldierNum();
         */


        int hpRes = lineEntityB.getSoldierNum() - damage;
        int damageShow = Math.min(lineEntityB.getSoldierNum(), damage);
        hpRes = Math.max(hpRes, 0);
        lineEntityB.setSoldierNum(hpRes);
        attackInfo.setEntityId(lineEntityB.getEntityId());
        attackInfo.setTechLv(lineEntityB.getTechLv());
        attackInfo.setDamage(damageShow);
        attackInfo.setEntityType(lineEntityB.getEntityType());
        def.getAttackInfos().add(attackInfo);
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    public double getDevideFactor() {
        return DevideFactor.FACTOR_NUM;
    }

    // 克制关系系数做成配置
    public double getRestraint(Player player, int soliderTypeA, int soliderTypeB) {
        int factor = staticBattleDataMgr.getFactor(soliderTypeA, soliderTypeB);
        double v = (double) factor / DevideFactor.FACTOR_NUM;
        double seasonBuf = seasonManager.getSeasonBuf(player, EffectType.EFFECT_TYPE38);
        v += seasonBuf;
        return v;
    }

    // 增加城战模式
    public BattleEntity createBattleEntity(Hero hero, Player player, BattleProperty playerBattleProperty, int entityType, boolean isLostMode, boolean isCityMode, int fatherEntityType, boolean isAttacker) {
        // TODO jyb这里新加参谋部兵排数加成
        int soldierLines = playerManager.getSoldierLine(player) + staticMeetingTaskMgr.soldierNumByHero(player, hero.getHeroId());
        heroDataManager.caculateProp(hero, player);
        if (isCityMode) {
            heroDataManager.caculateCityProp(hero, player, fatherEntityType, isAttacker);
        }

        // testAddHero(hero); // 测试代码
        Property property = hero.getTotalProp();
        if (isLostMode) { // 损兵模式
            property.setMaxSoldier(hero.getCurrentSoliderNum());
        } else {
            property.setMaxSoldier(property.getSoldierNum());
        }

        if (testManager.isOpenTestMode()) {
            property.setSoldierNum(property.getSoldierNum() * 500);
            property.setAttack(property.getAttack() * 500);
            property.setDefence(property.getDefence() * 500);
        }

        BattleEntity battleEntity = new BattleEntity();
        BattleProperty battleProperty = new BattleProperty();
        // player
        battleProperty.add(playerBattleProperty);
        // equip
        BattleProperty equipBattleProperty = heroDataManager.getBattleProperty(hero);
        battleProperty.add(equipBattleProperty);
        //// book
        //BattleProperty bookBattleProperty = warBookManager.getBookBattleProperty(hero);
        //battleProperty.add(bookBattleProperty);

        int soldierType = staticHeroMgr.getSoldierType(hero.getHeroId());
        initBattleEntity(battleEntity, hero.getHeroId(), hero.getHeroLv(), soldierLines, property, soldierType, battleProperty, entityType, player.roleId);

        // 武将兵书
        if (hero.getHeroBooks() != null && !hero.getHeroBooks().isEmpty()) {
            battleEntity.getHeroBooks().addAll(hero.getHeroBooks());
        }
        // 如果是赛季英雄
        if (hero.getType() > 0) {
            battleEntity.setHero(hero);
        }
        return battleEntity;
    }

    public void initBattleEntity(BattleEntity battleEntity, int id, int level, int soldierLinesPar, Property property, int soldierType, BattleProperty battleProperty, int entityType, long lordId) {
        battleEntity.setEntityId(id);
        battleEntity.setLevel(level);
        // 虫族入侵buff处理
        int techLv = 0;
        Player player = playerManager.getPlayer(lordId);
        if (entityType == BattleEntityType.ROIT_MONSTER) {
            if (player != null) {
                SimpleData data = player.getSimpleData();
                if (data != null) {
                    Integer lessRoops = data.getRiotBuff().get(RiotBuff.LESSTROOPS);
                    lessRoops = lessRoops == null ? 0 : lessRoops;
                    float lessSoldierNum = property.getMaxSoldier() * (lessRoops / 100f);
                    property.setMaxSoldier(Math.round(property.getMaxSoldier() - lessSoldierNum));
                }
            }
        }
        if (player != null && (entityType == BattleEntityType.HERO || entityType == BattleEntityType.FRIEND_HERO)) {
            techLv = techManager.getTechLevel(player, techManager.getTechType(soldierType));
        }
        int soldierNum = 0;
        int leftSoldier = 0;
        int soldierLines = Math.max(1, soldierLinesPar);
        // 每排最大兵力
        int maxLinesSoldierNum = property.getSoldierNum() / soldierLines;
        //真实兵排
        int realSoldierLines = 0;
        if (maxLinesSoldierNum != 0) {
            realSoldierLines = property.getMaxSoldier() / maxLinesSoldierNum;
        }
        //不足一排的兵力
        int surplusSoldierNum = property.getMaxSoldier() - (maxLinesSoldierNum * realSoldierLines);
        if (surplusSoldierNum > 0) {
            if (realSoldierLines >= soldierLines) {
                realSoldierLines = soldierLines;
                leftSoldier = surplusSoldierNum;
                soldierNum = maxLinesSoldierNum;
            } else {
                // 单独一排
                realSoldierLines++;
                soldierNum = surplusSoldierNum;
                if (maxLinesSoldierNum == 0) {
                    maxLinesSoldierNum = surplusSoldierNum;
                }
            }
        } else if (surplusSoldierNum == 0 && realSoldierLines == 0) {
            soldierNum = 0;
        } else {
            soldierNum = maxLinesSoldierNum;
        }

        if (soldierLinesPar <= 0) {
            LogHelper.CONFIG_LOGGER.info("soldierLines is 0.");
            soldierNum = 0;
        }

        battleEntity.setLineNumber(realSoldierLines);
        battleEntity.setMaxLineNumber(soldierLines);
        // TODO
        // int maxSoldierNum = soldierNum * soldierLines;
        battleEntity.setCurSoldierNum(property.getMaxSoldier());
        battleEntity.setMaxSoldierNum(property.getMaxSoldier());
        battleEntity.setLeftSoldier(leftSoldier); // 多出来的兵力

//		// check soldiers
//		// 极限情况的逻辑处理
//		if (property.getMaxSoldier() >= 1 && property.getMaxSoldier() < soldierLines) {
//			int maxSoldier = property.getMaxSoldier();
//			soldierNum = 1;
//			battleEntity.setLineNumber(maxSoldier);
//			battleEntity.setMaxLineNumber(maxSoldier);
//			battleEntity.setCurSoldierNum(maxSoldier);
//			battleEntity.setMaxSoldierNum(maxSoldier);
//			leftSoldier = 0;
//			battleEntity.setLeftSoldier(leftSoldier);
//		}

        LineEntity lineEntity = new LineEntity();
        lineEntity.setSoldierType(soldierType);
        lineEntity.setLevel(level);
        Property basePropety = new Property();
        basePropety.setAttack(property.getAttack());
        basePropety.setDefence(property.getDefence());
        basePropety.setSoldierNum(soldierNum);
        basePropety.setMaxSoldier(maxLinesSoldierNum);

        lineEntity.setBaseProperty(basePropety);
        lineEntity.setMaxSoldierNum(maxLinesSoldierNum);
        lineEntity.setEntityId(id);
        lineEntity.setBattleProperty(battleProperty);
        lineEntity.setEntityType(entityType);
        lineEntity.setTechLv(techLv);

        battleEntity.setLineEntity(lineEntity);
        battleEntity.setKillNum(0);
        battleEntity.setEntityType(entityType);
        battleEntity.setSoldierType(soldierType);
        battleEntity.setLordId(lordId);
        battleEntity.setTechLv(techLv);
    }

    // 皇城血战、世界boss、采集战
    public Team initPlayerTeam(Player player, List<Integer> heroList, int entityType) {
        return initPlayerTeam(player, heroList, entityType, false, 0, false);
    }

    // 1.初始化战队[玩家英雄], world, 损兵模式
    public Team initPlayerTeam(Player player, List<Integer> heroList, int entityType, boolean isCityMode, int fatherEntityType, boolean isAttacker) {
        if (player == null) {
            return null;
        }

        Team team = new Team();
        // 应该用玩家出战的英雄
        HashMap<Integer, Hero> heroes = player.getHeros();
        BattleProperty playerBp = getBattleProperty(player.getLevel());
        for (Integer heroId : heroList) {
            Hero hero = heroes.get(heroId);
            if (hero == null) {
                LogHelper.CONFIG_LOGGER.info("hero is null");
                continue;
            }

            if (hero.getCurrentSoliderNum() <= 0) {
                continue;
            }

            BattleEntity battleEntity = createBattleEntity(hero, player, playerBp, entityType, true, isCityMode, fatherEntityType, isAttacker);
            if (battleEntity != null) {
                team.add(battleEntity);
            }
        }

        team.setCountry(player.getCountry());
        team.setPortrait(player.getPortrait());

        clearTeam(team);

        return team;
    }

    // 4个属性
    public BattleProperty getBattleProperty(int playerLv) {
        StaticLordLv staticLordLv = staticLordDataMgr.getStaticLordLv(playerLv);
        BattleProperty battleProperty = new BattleProperty();
        if (staticLordLv == null) {
            return battleProperty;
        }
        battleProperty.setCriti(staticLordLv.getCriti());
        battleProperty.setHit(staticLordLv.getHit());
        battleProperty.setMiss(staticLordLv.getMiss());
        battleProperty.setTenacity(staticLordLv.getTenacity());
        return battleProperty;
    }

    // 初始化pve玩家队伍,不损兵模式
    public Team initPvePlayerTeam(Player player, List<Integer> heroList, int entityType) {
        if (player == null) {
            return null;
        }

        Team team = new Team();
        // 应该用玩家出战的英雄
        HashMap<Integer, Hero> heroes = player.getHeros();
        BattleProperty playerBp = getBattleProperty(player.getLevel());
        for (Integer heroId : heroList) {
            Hero hero = heroes.get(heroId);
            if (hero == null) {
                LogHelper.CONFIG_LOGGER.info("hero is null");
                continue;
            }

            BattleEntity battleEntity = createBattleEntity(hero, player, playerBp, entityType, false, false, 0, false);
            if (battleEntity != null) {
                team.add(battleEntity);
            }
        }

        team.setCountry(player.getCountry());
        team.setPortrait(player.getPortrait());

        return team;
    }

    public BattleEntity createMonster(int monsterId, int entityType) {
        StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
        if (staticMonster == null) {
            LogHelper.CONFIG_LOGGER.info("staticMonster == null");
            return null;
        }

        Property property = getPveMonsterPro(staticMonster);

        BattleEntity battleEntity = new BattleEntity();

        // 二级属性：战斗属性
        BattleProperty battleProperty = getPveMonsterBp(staticMonster);

        initBattleEntity(battleEntity, monsterId, staticMonster.getLevel(), staticMonster.getSoldierLines(), property, staticMonster.getSoldierType(), battleProperty, entityType, 0L);

        return battleEntity;

    }

    // 指定血量
    public BattleEntity createMonster(int monsterId, int entityType, int soldier) {
        StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
        if (staticMonster == null) {
            LogHelper.CONFIG_LOGGER.info("staticMonster == null");
            return null;
        }

        Property property = getPveMonsterPro(staticMonster);
        property.setMaxSoldier(soldier);

        BattleEntity battleEntity = new BattleEntity();

        // 二级属性：战斗属性
        BattleProperty battleProperty = getPveMonsterBp(staticMonster);

        initBattleEntity(battleEntity, monsterId, staticMonster.getLevel(), staticMonster.getSoldierLines(), property, staticMonster.getSoldierType(), battleProperty, entityType, 0L);

        return battleEntity;

    }

    /**
     * @param monsterId
     * @param entityType
     * @param player
     * @return
     */
    public BattleEntity createRiotMonster(int monsterId, int entityType, Player player) {
        StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
        if (staticMonster == null) {
            LogHelper.CONFIG_LOGGER.info("staticMonster == null");
            return null;
        }

        Property property = getPveMonsterPro(staticMonster);

        BattleEntity battleEntity = new BattleEntity();

        // 二级属性：战斗属性
        BattleProperty battleProperty = getPveMonsterBp(staticMonster);

        initBattleEntity(battleEntity, monsterId, staticMonster.getLevel(), staticMonster.getSoldierLines(), property, staticMonster.getSoldierType(), battleProperty, entityType, player.roleId);

        return battleEntity;

    }

    // 2.初始化怪物
    public Team initMonsterTeam(List<Integer> monsters, int entityType) {
        Team team = new Team();
        for (Integer monsterId : monsters) {
            BattleEntity battleEntity = createMonster(monsterId, entityType);
            if (battleEntity != null) {
                team.add(battleEntity);
            }
        }

        return team;
    }

    // 指定血量初始化战队
    public Team initMonsterTeam(List<Integer> monsters, List<Integer> soldiers, int entityType) {
        Team team = new Team();
        for (int i = 0; i < monsters.size(); i++) {
            int monsterId = monsters.get(i);
            Integer soldier = soldiers.get(i);
            // 血量为0的不加入战斗
            if (soldier == null || soldier == 0) {
                continue;
            }

            BattleEntity battleEntity = createMonster(monsterId, entityType, soldiers.get(i));
            if (battleEntity != null) {
                team.add(battleEntity);
            }
        }

        return team;
    }

    // 3.初始化玩家友军team
    // 闪电、奔袭或者远征
    public Team initFriendTeam(Player target, Wall wall, int entityType, int fatherEntityType, boolean isAttacker) {
        Team team = new Team();
        if (wall == null) {
            return team;
        }

        HashMap<Integer, WallFriend> wallFriends = wall.getWallFriends();
        if (wallFriends.isEmpty()) {
            return team;
        }
        double seasonBuf = seasonManager.getSeasonBuf(target, EffectType.EFFECT_TYPE13);// 如果被驻防的人带这个buf
        // 所有友军
        for (WallFriend wallFriend : wallFriends.values()) {
            if (wallFriend == null) {
                continue;
            }

            int heroId = wallFriend.getHeroId();
            List<Integer> allHeros = new ArrayList<Integer>();
            allHeros.add(heroId);

            Player player = playerManager.getPlayer(wallFriend.getLordId());
            Team friendTeam = initPlayerTeam(player, allHeros, entityType, true, fatherEntityType, isAttacker);
            if (friendTeam == null) {
                continue;
            }
            if (seasonBuf > 0) {
                ArrayList<BattleEntity> allEnities = friendTeam.getAllEnities();
                allEnities.forEach(x -> {
                    LineEntity lineEntity = x.getLineEntity();
                    Property baseProperty = lineEntity.getBaseProperty();
                    if (seasonBuf > 0) {
                        double v = baseProperty.getAttack() * (1 + seasonBuf);
                        baseProperty.setAttack((int) v);
                    }
                });
            }

            team.addTeam(friendTeam);
        }

        clearTeam(team);

        return team;
    }

    // 4.初始化城防军
    public Team initDefenderTeam(Wall wall, Player target, int fatherEntityType) {
        Team team = new Team();
        if (wall == null) {
            return team;
        }

        Map<Integer, WallDefender> wallDefenders = wall.getWallDefenders();
        if (wallDefenders.isEmpty()) {
            return team;
        }
        double seasonBuf = seasonManager.getSeasonBuf(target, EffectType.EFFECT_TYPE15);
        int buf = seasonManager.getBuf(target, EffectType.EFFECT_TYPE16);

        if (!wallDefenders.isEmpty()) {
            for (WallDefender defender : wallDefenders.values()) {
                int level = defender.getLevel();
                int quality = defender.getQuality();
                StaticWallMonsterLv config = staticWallMgr.getWallMonster(level, quality);
                if (config == null) {
                    LogHelper.ERROR_LOGGER.error("config is error!!!!!!!!!!!");
                    continue;
                }

                if (config.getSoldierLines() <= 0) {
                    continue;
                }

                BattleEntity battleEntity = createWallMonster(defender.getKeyId(), defender.getId(), config, defender.getSoldier(), defender.getSoldierNum(), target.roleId, fatherEntityType);
                battleEntity.setQuality(quality);
                battleEntity.setWallLordId(target.getRoleId());
                team.add(battleEntity);
                LineEntity lineEntity = battleEntity.getLineEntity();
                Property baseProperty = lineEntity.getBaseProperty();
                // EFFECT_TYPE15(15, "城墙上的禁卫军攻击属性增加（百分比）"), //面板属性 增加
                if (seasonBuf > 0) {
                    if (baseProperty != null) {
                        double v = baseProperty.getAttack() * (1 + seasonBuf);
                        baseProperty.setAttack((int) v);
                    }
                }
                // EFFECT_TYPE16(16, "城墙上的禁卫军防御属性增加（固定数值）"),////面板属性 增加
                if (buf > 0) {
                    baseProperty.addDefenceValue(buf);
                }
            }
        }

        clearTeam(team);

        return team;
    }

    // 5.初始化驻防的武将,闪电、奔袭或者远征
    public Team initAssistTeam(Player target, int faterEntityType, boolean isAttacker) {
        List<Integer> targetHeros = target.getWall().getDefenceDisHero();
        ArrayList<Integer> freeHeros = new ArrayList<Integer>();
        for (Integer heroId : targetHeros) {
            Hero hero = target.getHero(heroId);
            if (hero == null) {
                continue;
            }

            // 当前英雄状态
            if (!playerManager.isHeroFree(target, heroId)) {
                continue;
            }

            freeHeros.add(heroId);
        }
        Team team = initPlayerTeam(target, freeHeros, BattleEntityType.HERO, true, faterEntityType, isAttacker);

        clearTeam(team);

        return team;
    }

    // 5.初始化驻防的武将,闪电、奔袭或者远征(参谋部城防军)
    public Team initDefenceArmyTeam(Player target, int fatherEntityType, boolean isAttacker) {
        castleService.updateDefenseSoldierByPlayer(target);
        List<Integer> targetHeros = target.getMeetingArmy(CastleConsts.DEFENSEARMY);
        List<Integer> freeHeros = new ArrayList<Integer>();
        for (Integer heroId : targetHeros) {
            Hero hero = target.getHero(heroId);
            if (hero == null) {
                continue;
            }

            // 当前英雄状态
            if (!playerManager.isHeroFree(target, heroId)) {
                continue;
            }

            freeHeros.add(heroId);
        }

        Team team = initPlayerTeam(target, freeHeros, BattleEntityType.DEFENSE_ARMY_HERO, true, fatherEntityType, isAttacker);
        clearTeam(team);
        return team;
    }

    // 初始化被攻击者的team
    // 闪电、奔袭或者远征
    public Team initTargetTeam(Player target, int faterEntityType, boolean isAttacker) {
        Team allTeam = new Team();
        if (target == null) {
            LogHelper.CONFIG_LOGGER.info("target is null!");
            return allTeam;
        }

        Wall wall = target.getWall();
        // 玩家驻防武将
        Team targetTeam = initAssistTeam(target, faterEntityType, isAttacker);
        allTeam.addTeam(targetTeam);

        // TODO 初始化玩家的参谋部城防部队
        Team defenceTeam = initDefenceArmyTeam(target, faterEntityType, isAttacker);
        allTeam.addTeam(defenceTeam);

        // 城防军
        Team defenderTeam = initDefenderTeam(wall, target, faterEntityType);
        allTeam.addTeam(defenderTeam);

        // 友军驻防城墙的武将
        Team friendTeam = initFriendTeam(target, wall, BattleEntityType.WALL_FRIEND_HERO, faterEntityType, isAttacker);
        allTeam.addTeam(friendTeam);

        clearTeam(allTeam);

        return allTeam;
    }

    public Team initMarchTeam(March march, int entityType, boolean isAttacker) {
        Team team = new Team();
        long lordId = march.getLordId();
        Player player = playerManager.getPlayer(lordId);
        if (player == null) {
            return team;
        }

        team = initPlayerTeam(player, march.getHeroIds(), entityType, true, 0, isAttacker);
        clearTeam(team);

        return team;
    }

    // 国战,远征或者奔袭,伏击叛军
    public Team initAttackerWarTeam(WarInfo warInfo, boolean isAttacker) {
        ConcurrentLinkedDeque<March> attender = warInfo.getAttackMarches();
        Team team = new Team();
        int entityType = 0;
        for (March march : attender) {
            if (march.getLordId() == warInfo.getAttackerId()) {
                entityType = BattleEntityType.HERO;
            } else {
                entityType = BattleEntityType.FRIEND_HERO;
            }
            Team marchTeam = initMarchTeam(march, entityType, isAttacker);
            team.addTeam(marchTeam);
        }

        clearTeam(team);

        return team;
    }

    /**
     * 巨型虫族
     *
     * @param attender
     * @param isAttacker
     * @return
     */
    public Team initBigMonsterWarTeam(List<March> attender, boolean isAttacker) {
        Team team = new Team();
        for (March march : attender) {
            Team marchTeam = initMarchTeam(march, BattleEntityType.HERO, isAttacker);
            team.addTeam(marchTeam);
        }
        clearTeam(team);
        return team;
    }

    public Team initDefenceWarTeam(WarInfo warInfo, boolean isAttacker) {
        ConcurrentLinkedDeque<March> defencer = warInfo.getDefenceMarches();
        Team team = new Team();
        for (March march : defencer) {
            Team marchTeam = initMarchTeam(march, BattleEntityType.FRIEND_HERO, isAttacker);
            team.addTeam(marchTeam);
        }

        clearTeam(team);

        return team;
    }

    // 初始化守城怪物
    public Team initCityTeam(int cityId) {
        Team team = new Team();
        StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
        if (worldCity == null) {
            LogHelper.CONFIG_LOGGER.info("worldCity is null, cityId = " + cityId);
            return team;
        }

        List<Integer> monsters = getCityMonster(cityId, worldCity);
        if (monsters == null) {
            LogHelper.CONFIG_LOGGER.info("city has no monster = " + cityId);
            return team;
        }

        List<Integer> soldiers = new ArrayList<Integer>();
        CityMonster cityMonster = cityManager.getCityMonster(cityId);
        for (Integer monsterId : monsters) {
            int soldier = cityMonster.getSoldier(monsterId);
            // soldier = 40;
            soldiers.add(soldier);
        }

        team = initMonsterTeam(monsters, soldiers, BattleEntityType.CITY_MONSTER);
        City city = cityManager.getCity(cityId);
        if (city != null) {
            team.setCountry(city.getCountry());
        }

        clearTeam(team);
        return team;
    }

    // 初始化守城怪物
    public Team initBroodWarCityTeam(int cityId) {
        Team team = new Team();
        StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
        if (worldCity == null) {
            LogHelper.CONFIG_LOGGER.info("worldCity is null, cityId = " + cityId);
            return team;
        }

        List<Integer> monsters = getCityMonster(cityId, worldCity);
        if (monsters == null) {
            LogHelper.CONFIG_LOGGER.info("city has no monster = " + cityId);
            return team;
        }
        team = initMonsterTeam(monsters, BattleEntityType.CITY_MONSTER);

        clearTeam(team);
        return team;
    }

    public Team initRiotMonsterTeam(List<Integer> monsters, int entityType, Player player) {
        Team team = new Team();
        for (Integer monsterId : monsters) {
            BattleEntity battleEntity = createRiotMonster(monsterId, entityType, player);
            if (battleEntity != null) {
                team.add(battleEntity);
            }
        }

        return team;
    }

    public List<Integer> getCityMonster(int cityId, StaticWorldCity worldCity) {
        if (worldCity.getType() == CityType.POINT) {
            // 如果是1级的返回preMonster, 否则返回Monster
            City city = cityManager.getCity(cityId);
            if (city.getCityLv() <= 1) {
                return worldCity.getPreMonsters();
            } else {
                return worldCity.getMonsters();
            }
        } else {
            return worldCity.getMonsters();
        }

    }

    // 6.初始化世界boss
    public Team initWorldMonsterTeam(int bossId, int soldier) {
        Team team = new Team();
        BattleEntity battleEntity = createMonster(bossId, BattleEntityType.WORLD_BOSS, soldier);
        if (battleEntity != null) {
            team.add(battleEntity);
        }

        return team;
    }

    public Property getPveMonsterPro(StaticMonster staticMonster) {
        Property property = new Property(); // lineEntity的属性
        property.setAttack(staticMonster.getAttack());
        property.setDefence(staticMonster.getDefence());
        property.setSoldierNum(staticMonster.getSoldierCount());
        property.setMaxSoldier(staticMonster.getSoldierCount());

        return property;
    }

    public BattleProperty getPveMonsterBp(StaticMonster staticMonster) {
        // 二级属性：战斗属性
        BattleProperty battleProperty = new BattleProperty();
        battleProperty.setStrongAttack(staticMonster.getStrongAttack());
        battleProperty.setStrongDefence(staticMonster.getStrongDefence());
        battleProperty.setHit(staticMonster.getHit());
        battleProperty.setMiss(staticMonster.getMiss());
        battleProperty.setCriti(staticMonster.getCriti());
        battleProperty.setTenacity(staticMonster.getTenacity());
        return battleProperty;
    }

    public BattleEntity createWallMonster(int keyId, int configId, StaticWallMonsterLv staticMonster, int soldierType, int soldierLeftNum, long targetId, int fatherEntityType) {
        Property property = new Property(); // lineEntity的属性
        property.setAttack(staticMonster.getAttack());
        property.setDefence(staticMonster.getDefence());

        // 虫族入侵buff //处理攻击防御加成
        if (fatherEntityType == BattleEntityType.ROIT_MONSTER) {
            Player player = playerManager.getPlayer(targetId);
            if (player != null) {
                SimpleData data = player.getSimpleData();
                if (data != null) {
                    Integer riotAttack = data.getRiotBuff().get(RiotBuff.ATTACK);
                    Integer riotDefence = data.getRiotBuff().get(RiotBuff.DEFENCE);
                    float riotAddAttack = riotAttack == null ? 0 : riotAttack / 100f;
                    float riotAddDefence = riotDefence == null ? 0 : riotDefence / 100f;
                    int addRiotAttack = Math.round(property.getAttack() * riotAddAttack);
                    int addRiotDefence = Math.round(property.getDefence() * riotAddDefence);
                    property.addAttackValue(addRiotAttack);
                    property.addDefenceValue(addRiotDefence);
                }
            }
        }
        int soldierLines = staticMonster.getSoldierLines();
        int soldierNum = soldierLeftNum / soldierLines;
        property.setSoldierNum(soldierLeftNum);
        property.setMaxSoldier(soldierLeftNum);

        // 整个实体
        BattleEntity battleEntity = new BattleEntity();
        // 二级属性：战斗属性
        BattleProperty battleProperty = new BattleProperty();
        battleProperty.setStrongAttack(staticMonster.getStrongAttack());
        battleProperty.setStrongDefence(staticMonster.getStrongDefence());
        battleProperty.setHit(staticMonster.getHit());
        battleProperty.setMiss(staticMonster.getMiss());
        battleProperty.setCriti(staticMonster.getCriti());
        battleProperty.setTenacity(staticMonster.getTenacity());

        initBattleEntity(battleEntity, configId, staticMonster.getDefenceLv(), staticMonster.getSoldierLines(), property, soldierType, battleProperty, BattleEntityType.WALL_DEFENCER, 0L);
        battleEntity.setWallDefencerkeyId(keyId);
        battleEntity.setWallLordId(targetId);
        return battleEntity;

    }

    // 闪电、奔袭或者远征, 国战
    public Team initDefencer(WarInfo warInfo, int faterEntityType, boolean isAttacker) {
        // 援助
        Team team = initDefenceWarTeam(warInfo, isAttacker);
        // 玩家武将
        Player target = playerManager.getPlayer(warInfo.getDefencerId());
        if (target != null) {
            Team playerTeam = initTargetTeam(target, faterEntityType, isAttacker);
            team.addTeam(playerTeam);
        }

        return team;
    }

    public void clearTeam(Team team) {
        Iterator<BattleEntity> iterator = team.getAllEnities().iterator();
        while (iterator.hasNext()) {
            BattleEntity entity = iterator.next();
            if (entity.getCurSoldierNum() <= 0) {
                iterator.remove();
            }
        }
    }

    // 记录击杀的情况, 用来计算经验值
    // 记录击杀的type, level, quality相关的值
    public void updateKill(BattleEntity battleEntity, int killNum) {
        Map<Integer, Map<Integer, RecordEntity>> recordEntityMap = battleEntity.getRecordEntityMap();
        int type = battleEntity.getEntityType();
        int level = battleEntity.getLevel();
        int id = battleEntity.getEntityId();

        int quality = getQuality(id, type);
        if (quality == 0) {
            return;
        }

        // 两层map结构
        Map<Integer, RecordEntity> entityMap = recordEntityMap.get(type);
        RecordEntity recordEntity = null;
        if (entityMap == null) {
            recordEntity = new RecordEntity(quality, killNum);
            entityMap = new HashMap<Integer, RecordEntity>();
            entityMap.put(level, recordEntity);
            recordEntityMap.put(type, entityMap);
        } else {
            recordEntity = entityMap.get(level);
            if (recordEntity == null) {
                recordEntity = new RecordEntity(quality, killNum);
                entityMap.put(level, recordEntity);
            } else {
                recordEntity.update(quality, killNum);
            }
        }

        if (killNum < 0) {
            LogHelper.CONFIG_LOGGER.info("updateKill killNum = " + killNum);
        }

    }

    // 通过id,type找到对应的quality
    public int getQuality(int id, int type) {
        if (isHeroType(type)) {
            return staticHeroMgr.getQuality(id);
        } else if (isPveMonsterType(type)) {
            return monsterMgr.getQuality(id);
        } else if (isWallMonster(type)) {
            return wallMgr.getQuality(id);
        }

        return 0;
    }

    public boolean isHeroType(int type) {
        return type == BattleEntityType.HERO || type == BattleEntityType.FRIEND_HERO || type == BattleEntityType.WALL_FRIEND_HERO;
    }

    public boolean isPveMonsterType(int type) {
        return type == BattleEntityType.REBEL || type == BattleEntityType.WORLD_BOSS || type == BattleEntityType.CITY_MONSTER || type == BattleEntityType.MONSTER || type == BattleEntityType.GUARD_MONSTER;
    }

    public boolean isWallMonster(int type) {
        return type == BattleEntityType.WALL_DEFENCER;
    }

    /**
     * 兵书不累计技能的加成值
     *
     * @param battleEntity
     * @param column
     * @param lineEntity
     */
    public void addBattleBookSkillEffect(BattleEntity battleEntity, Map<Integer, Integer> column, LineEntity lineEntity) {
        if (lineEntity.getEntityType() == 1) {
            Integer heroWarBookSkillEffect12 = warBookManager.getHeroWarBookSkillEffect(battleEntity.getHeroBooks(), lineEntity.getEntityId(), BookEffectType.BOOK_EFFECT_12);
            if (null != heroWarBookSkillEffect12) {
                column.put(BookEffectType.BOOK_EFFECT_12, heroWarBookSkillEffect12.intValue());
            }

            Integer heroWarBookSkillEffect13 = warBookManager.getHeroWarBookSkillEffect(battleEntity.getHeroBooks(), lineEntity.getEntityId(), BookEffectType.BOOK_EFFECT_13);
            if (null != heroWarBookSkillEffect13) {
                column.put(BookEffectType.BOOK_EFFECT_13, heroWarBookSkillEffect13.intValue());
            }

            Integer heroWarBookSkillEffect14 = warBookManager.getHeroWarBookSkillEffect(battleEntity.getHeroBooks(), lineEntity.getEntityId(), BookEffectType.BOOK_EFFECT_14);
            if (null != heroWarBookSkillEffect14) {
                column.put(BookEffectType.BOOK_EFFECT_14, heroWarBookSkillEffect14.intValue());
            }

            Integer heroWarBookSkillEffect16 = warBookManager.getHeroWarBookSkillEffect(battleEntity.getHeroBooks(), lineEntity.getEntityId(), BookEffectType.BOOK_EFFECT_16);
            if (null != heroWarBookSkillEffect16) {
                column.put(BookEffectType.BOOK_EFFECT_16, heroWarBookSkillEffect16.intValue());
            }

            Integer heroWarBookSkillEffect18 = warBookManager.getHeroWarBookSkillEffect(battleEntity.getHeroBooks(), lineEntity.getEntityId(), BookEffectType.BOOK_EFFECT_18);
            if (null != heroWarBookSkillEffect18) {
                column.put(BookEffectType.BOOK_EFFECT_18, heroWarBookSkillEffect18.intValue());
            }

            Integer heroWarBookSkillEffect19 = warBookManager.getHeroWarBookSkillEffect(battleEntity.getHeroBooks(), lineEntity.getEntityId(), BookEffectType.BOOK_EFFECT_19);
            if (null != heroWarBookSkillEffect19) {
                column.put(BookEffectType.BOOK_EFFECT_19, heroWarBookSkillEffect19.intValue());
            }
        }
    }


    /**
     * 兵书累计技能的加成值
     *
     * @param battleEntity
     * @param column
     * @param lineEntity
     * @param bookEfectType
     */
    public void addBattleBookSkillEffectTotal(BattleEntity battleEntity, Map<Integer, Integer> column, LineEntity lineEntity, int bookEfectType) {
        if (lineEntity.getEntityType() == 1) {
            Integer heroWarBookSkillEffect = warBookManager.getHeroWarBookSkillEffect(battleEntity.getHeroBooks(), lineEntity.getEntityId(), bookEfectType);
            if (null != heroWarBookSkillEffect) {
                Integer effectValue = column.get(bookEfectType);
                if (null != effectValue) {
                    column.put(bookEfectType, heroWarBookSkillEffect + effectValue);
                } else {
                    column.put(bookEfectType, heroWarBookSkillEffect);
                }
            }
        }
    }


    /**
     * 移除不累计累计技能的加成值
     *
     * @param column
     */
    public void removeBattleBookSkillEffect(Map<Integer, Integer> column) {
        column.remove(BookEffectType.BOOK_EFFECT_12);
        column.remove(BookEffectType.BOOK_EFFECT_13);
        column.remove(BookEffectType.BOOK_EFFECT_14);
        column.remove(BookEffectType.BOOK_EFFECT_16);
        column.remove(BookEffectType.BOOK_EFFECT_18);
        column.remove(BookEffectType.BOOK_EFFECT_19);
    }

    /**
     * 移除累计累计技能的加成值
     *
     * @param column
     */
    public void removeBattleBookSkillEffectForEntity(Map<Integer, Integer> column) {
        column.remove(BookEffectType.BOOK_EFFECT_15);
    }

    public Team createSuperResDefer(SuperResource resource) {
        Team team = new Team();
        ConcurrentLinkedDeque<March> helpArmy = resource.getHelpArmy();
        helpArmy.forEach(x -> {
            Player player = playerManager.getPlayer(x.getLordId());
            if (player != null) {
                Team team1 = initPlayerTeam(player, x.getHeroIds(), BattleEntityType.FRIEND_HERO);
                team.addTeam(team1);
            }
        });
        ConcurrentLinkedDeque<SuperGuard> collectArmy = resource.getCollectArmy();
        collectArmy.forEach(x -> {
            March march = x.getMarch();
            Player player = playerManager.getPlayer(march.getLordId());
            if (player != null) {
                Team team1 = initPlayerTeam(player, march.getHeroIds(), BattleEntityType.HERO);
                team.addTeam(team1);
            }
        });
        return team;
    }

}
