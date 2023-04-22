package com.game.service;

import com.game.domain.Award;
import com.game.log.LogUser;
import com.game.season.SeasonManager;
import com.game.season.talent.entity.EffectType;
import com.game.server.exec.LoginExecutor;
import com.game.spring.SpringUtil;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import com.game.worldmap.fight.war.ZergWarInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.chat.domain.Chat;
import com.game.chat.domain.SystemChat;
import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.Nation;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.flame.FlameMap;
import com.game.flame.FlamePlayer;
import com.game.flame.FlameWarManager;
import com.game.log.constant.OilOperateType;
import com.game.log.constant.ResOperateType;
import com.game.log.consumer.EventManager;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.cs.AttendRebelWarHandler;
import com.game.message.handler.cs.FindNearMonsterHandler;
import com.game.message.handler.cs.GetRebelWarHandler;
import com.game.message.handler.cs.RebelFightHelpHandler;
import com.game.message.handler.cs.RebelFightShareHandler;
import com.game.message.handler.cs.*;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.ThreeInt;
import com.game.pb.WorldPb;
import com.game.pb.WorldPb.*;
import com.game.server.GameServer;
import com.game.util.*;
import com.game.worldmap.*;
import com.game.worldmap.Resource;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;

@Service
public class WorldService {

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private ItemManager itemManager;

    @Autowired
    private StaticPropMgr staticPropDataMgr;

    @Autowired
    private StaticWorldMgr staticWorldMgr;

    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private WarManager warManager;

    @Autowired
    private CountryManager countryManager;

    @Autowired
    private CityManager cityManager;

    @Autowired
    private StaticMonsterMgr staticMonsterMgr;

    @Autowired
    private StaticCountryMgr staticCountryMgr;

    @Autowired
    private BattleMgr battleMgr;

    @Autowired
    private LordManager lordManager;

    @Autowired
    private HeroManager heroManager;

    @Autowired
    private TechManager techManager;

    @Autowired
    private StaticScoutMgr scoutMgr;

    @Autowired
    private SoldierManager soldierManager;

    @Autowired
    private StaticHeroMgr staticHeroMgr;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private WorldLogic worldLogic;

    @Autowired
    private ChatManager chatManager;

    @Autowired
    private StaticVipMgr staticVipMgr;

    @Autowired
    private BattleMailManager battleMailMgr;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private WorldPvpMgr worldPvpMgr;

    @Autowired
    private TestManager testManager;

    @Autowired
    private WorldTargetTaskService worldTargetTaskService;

    @Autowired
    private StaticActWorldBossMgr staticActWorldBossMgr;

    @Autowired
    private StaticWorldNewTargetMgr staticWorldNewTargetMgr;

    @Autowired
    private StaticWorldCityTypeMgr staticWorldCityTypeMgr;


    @Autowired
    private StaticPropMgr staticPropMgr;

    @Autowired
    private StealCityManager stealCityManager;
    @Autowired
    private WarBookManager warBookManager;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private BroodWarManager broodWarManager;
    @Autowired
    private ZergManager zergManager;
    @Autowired
    private MarchManager marchManager;
    @Autowired
    private FlameWarManager flameWarManager;
    @Autowired
    SeasonManager seasonManager;


    private Logger logger = LoggerFactory.getLogger(getClass());

    // 获取场景玩家城池信息
    public void getMap(WorldPb.GetMapRq req, ClientHandler handler) {
        CommonPb.Pos pos = req.getPos();
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        WorldPb.GetMapRs.Builder builder = WorldPb.GetMapRs.newBuilder();
        // 视野范围内的实体
        int posX = pos.getX();
        int posY = pos.getY();
//        LogHelper.ERROR_LOGGER.error("getMap , Posx = " + posX + ", Posy =" + posY + "=======" + player.getRoleId());
        // XY轴最小是0 最大是500
        int minX = Math.max(0, posX - 15);
        int maxX = Math.min(500, posX + 15);
        int minY = Math.max(0, posY - 15);
        int maxY = Math.min(500, posY + 15);
        Pos checkPos = new Pos();
        StaticWorldMap staticWorldMap = null;
        MapInfo checkMapInfo = null;
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                checkPos.setPos(i, j);
                if (player.getPushPos().containsKey(checkPos.toPosStr())) {
                    continue;
                }
                player.getPushPos().put(checkPos.toPosStr(), true);
                if (staticWorldMap == null) {
                    staticWorldMap = worldManager.getMap(checkPos);
                    if (staticWorldMap == null) {
                        continue;
                    }
                    checkMapInfo = worldManager.getMapInfo(staticWorldMap.getMapId());
                }
                if (i >= staticWorldMap.getX1() && i <= staticWorldMap.getX2() && j >= staticWorldMap.getY1() && j <= staticWorldMap.getY2()) {
                    // do nothing
                } else {
                    staticWorldMap = worldManager.getMap(checkPos);
                    if (staticWorldMap == null) {
                        continue;
                    }
                    checkMapInfo = worldManager.getMapInfo(staticWorldMap.getMapId());
                }

                if (checkMapInfo == null) {
                    continue;
                }

                Entity entity = checkMapInfo.getEntity(checkPos);
                if (entity == null) {
                    continue;
                }

                // npc 城池过滤
                if (entity.isExceptEntity()) {
                    continue;
                }

                if (entity.getEntityType() == 0) {
                    LogHelper.CONFIG_LOGGER.info("entity type is 0, it can be error, Pos = " + entity.getPos() + ", level =" + entity.getLevel());
                    continue;
                }
//                WorldEntity worldEntity = playerManager.addEntity(entity);
                try {
                    builder.addAddEntities(entity.wrapPb());
                } catch (Exception e) {
                    LogHelper.ERROR_LOGGER.error("getMap:{}", e.getMessage(), e);
                }
            }
        }
        handler.sendMsgToPlayer(WorldPb.GetMapRs.ext, builder.build());
    }

    // 攻击叛军
    public void attackRebelRq(WorldPb.AttackRebelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        CommonPb.Pos pos = req.getPos();
        // 目标点
        Pos targetPos = new Pos(pos.getX(), pos.getY());

        Monster rebelMonster = checkMonster(mapInfo, targetPos);
        Monster bigMonster = checkBigMonster(mapInfo, targetPos);
        if (rebelMonster == null && bigMonster == null) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_MONSTER_NOT_FOUND);
            return;
        }

        Monster monster = rebelMonster == null ? bigMonster : rebelMonster;
        if (monster instanceof BigMonster) {
            // 玩家等级不足35级 无法参与巨行虫族战斗
            if (player.getLevel() < 35) {
                handler.sendErrorMsgToPlayer(GameError.LEVEL_LIMIT);
                return;
            }
        }
        int monsterId = (int) monster.getId();
        StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster(monsterId);
        if (staticWorldMonster == null) {
            LogHelper.CONFIG_LOGGER.info("config error!");
            return;
        }

        // 玩家只能打比自己打过高一级的怪
        if (player.getMaxMonsterLv() + 1 < staticWorldMonster.getLevel() && staticWorldMonster.getType() == 1) {
            handler.sendErrorMsgToPlayer(GameError.MONSTER_LEVEL_TO_HIGH);
            return;
        }

        // 行军英雄
        List<Integer> heroIds = req.getHeroIdList();
        if (heroIds.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH_HEROS);
            return;
        }
        Map<Integer, Hero> heroMap = player.getHeros();
        // 检测英雄是否重复
        HashSet<Integer> checkHero = new HashSet<Integer>();
        // 检查英雄是否上阵
        for (Integer heroId : heroIds) {
            if (!isEmbattle(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
                return;
            }

            if (player.isHeroInMarch(heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
                return;
            }

            checkHero.add(heroId);
        }

        // 有相同的英雄出征
        if (checkHero.size() != heroIds.size()) {
            handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
            return;
        }

        // 检查英雄是否可以出征
        for (Integer heroId : heroIds) {
            Hero hero = heroMap.get(heroId);
            if (hero == null) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
                return;
            }

            if (!playerManager.isHeroFree(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
                return;
            }

            // 检查武将带兵量
            if (hero.getCurrentSoliderNum() <= 0) {
                handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
                return;
            }
        }
        // 出兵消耗
        int oilCost = worldManager.getMarchOil(heroIds, player, targetPos);
        if (staticWorldMonster.getType() == 1) {
            if (player.getResource(ResourceType.OIL) < oilCost) {
                handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
                return;
            }
        }
        SimpleData simpleData = player.getSimpleData();
        int marchType = MarchType.AttackMonster;
        if (staticWorldMonster.getType() == 4) {
            if (simpleData.getKillActMonsterTimes() >= staticLimitMgr.getNum(122)) {
                handler.sendErrorMsgToPlayer(GameError.REACH_MAX_KILL_DIMO_TIMES);
                return;
            }
            simpleData.setKillActMonsterTimes(simpleData.getKillActMonsterTimes() + 1);

        } else if (staticWorldMonster.getType() == 1) {
            if (simpleData.getKillRebelTimes() >= staticLimitMgr.getNum(11)) {
                handler.sendErrorMsgToPlayer(GameError.REACH_MAX_KILL_REBEL_TIMES);
                return;
            }

        } else if (staticWorldMonster.getType() == EntityType.RIOT_MONSTER) {
            if (simpleData.getKillRiot() >= staticLimitMgr.getNum(SimpleId.KILL_RIOT_MAX)) {
                handler.sendErrorMsgToPlayer(GameError.REACH_MAX_KILL_REBEL_TIMES);
                return;
            }
        } else if (staticWorldMonster.getType() == EntityType.BIG_MONSTER) {
            marchType = MarchType.BigWar;
        }

        monster.setStatus(1);

        March march = worldManager.createRebelAndAssistMarch(player, null, heroIds, targetPos, marchType);
        march.setFightTime(march.getEndTime() + 1000L, MarchReason.KillRebel);
        march.setMarchType(marchType);
        // 添加行军到玩家身上
        player.addMarch(march);
        // 加到世界地图中
        worldManager.addMarch(mapId, march);

        if (staticWorldMonster.getType() == 1) {
            playerManager.subAward(player, AwardType.RESOURCE, ResourceType.OIL, oilCost, Reason.KILL_WORLD_MONSTER);

            /** 部队行军资源消耗的日志埋点 */
            LogUser logUser = SpringUtil.getBean(LogUser.class);
            logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.OIL), RoleResourceLog.OPERATE_OUT, ResourceType.OIL, ResOperateType.MARCH_OUT.getInfoType(), oilCost, player.account.getChannel()));
            logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, oilCost, OilOperateType.MARCH_OUT.getInfoType()), ResourceType.OIL);
        }

        WorldPb.AttackRebelRs.Builder builder = WorldPb.AttackRebelRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(WorldPb.AttackRebelRs.ext, builder.build());
        worldManager.synMarch(mapInfo.getMapId(), march);
    }

    private Monster checkMonster(MapInfo mapInfo, Pos targetPos) {
        Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
        if (monsterMap == null) {
            return null;
        }
        Monster monster = monsterMap.get(targetPos);
        if (monster == null) {
            return null;
        }
        return monster;
    }

    private BigMonster checkBigMonster(MapInfo mapInfo, Pos targetPos) {
        Map<Pos, BigMonster> monsterMap = mapInfo.getBigMonsterMap();
        if (monsterMap == null) {
            return null;
        }
        BigMonster monster = monsterMap.get(targetPos);
        if (monster == null) {
            return null;
        }
        return monster;
    }

    public boolean isEmbattle(Player player, int heroId) {
        List<Integer> embattleList = player.getEmbattleList();
        List<Integer> miningList = player.getMiningList();
        return embattleList.contains(heroId) || miningList.contains(heroId);
    }

    // 刷新怪物
    public void flushWorldMonster() {
        if (worldManager.isNotOk()) {
            return;
        }
        // 现将地图上的虫族补充到rate*10 的数量，然后再刷新玩家周边格子的虫族
        worldManager.checkInitMonster();
        Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
        int cellNum = staticLimitMgr.getNum(SimpleId.WORLD_MONSTER_CELL_NUM);
//        // 怪物刷新上限
//        HashBasedTable<Integer, Integer, Integer> monsterFlushNum = staticWorldMgr.getMonsterFlushNum();
//        // 怪物刷新比例
//        HashBasedTable<Integer, Integer, Integer> monsterFlushRate = staticWorldMgr.getMonsterFlushRate();
        // 怪物刷新上限
        HashBasedTable<Integer, Integer, Map<Integer, Integer>> monsterFlushNum = staticWorldMgr.getMonsterFlushNum();
        // 怪物刷新比例
        HashBasedTable<Integer, Integer, Map<Integer, Integer>> monsterFlushRate = staticWorldMgr.getMonsterFlushRate();
        // 刷怪上限
        int monsterNumRange = staticLimitMgr.getNum(SimpleId.WORLD_MONSTER_NUM);

        SpringUtil.getBean(LoginExecutor.class).add(() -> {
            List<Entity> list = new ArrayList<>();
            playerManager.getOnlinePlayer().parallelStream().forEach(player -> {
                flushPlayerMonster(player, cellNum, monsterFlushNum, monsterFlushRate, monsterNumRange, list);
            });
            worldManager.synEntityAddRq(list);
        });
    }

    public void flushPlayerMonster(Player player, int cellNum, HashBasedTable<Integer, Integer, Map<Integer, Integer>> monsterFlushNum, HashBasedTable<Integer, Integer, Map<Integer, Integer>> monsterFlushRate, int monsterNumRange, List<Entity> list) {
        // 当前地图Id
        int mapId = worldManager.getMapId(player);
        if (mapId <= 0) {
            return;
        }
        // 世界系统开放后初级区域即可生成叛军，每个区域生成叛军数量与玩家基地数量相对应，比例为4（叛军数量）：1（玩家基地数量）
        // 当前玩家周围的野怪
        int monsterNum = worldManager.getMonsterNum(player);
        // 剩余野怪
        int leftNum = monsterNumRange - monsterNum;
        if (leftNum <= 0) {
            return;
        }
        // 当前地图信息
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            // 还没进入世界地图
            return;
        }

        // 当前地图类型
        int areaType = worldManager.getMapAreaType(mapId);
        // 皇城单独刷怪
        if (areaType == 3) {
            return;
        }

        // 当前玩家基地等级
        int commandLv = player.getCommandLv();
        // 当前玩家周围能生成的野怪的等级范围
        Range monsterLvRange = staticWorldMgr.getRange(commandLv, areaType);
        if (monsterLvRange == null) {
            LogHelper.CONFIG_LOGGER.info("monsterLvRange is null!");
            return;
        }

        if (monsterLvRange.getBeg() == null) {
            LogHelper.CONFIG_LOGGER.info("monsterLvRange.getBeg() == null, commandLv =" + commandLv);
            return;
        }

        if (monsterLvRange.getEnd() == null) {
            LogHelper.CONFIG_LOGGER.info("monsterLvRange.getBeg() == null, commandLv = " + commandLv);
            return;
        }

        // 玩家能刷的剩余等级
        List<Integer> leftMonsterLv = new ArrayList<Integer>();
        WorldData wolrdInfo = worldManager.getWolrdInfo();
        Map<Integer, Integer> integerIntegerMap = monsterFlushNum.get(wolrdInfo.getTarget(), areaType);
        Map<Integer, Integer> integerIntegerMap1 = monsterFlushRate.get(wolrdInfo.getTarget(), areaType);
        for (int i = monsterLvRange.getBeg(); i <= monsterLvRange.getEnd(); i++) {
            if (integerIntegerMap == null || integerIntegerMap1 == null) {
                break;
            }
            Integer limit = integerIntegerMap.get(i);
            if (limit == null) {
                continue;
            }
            Integer curNum = mapInfo.getMonsterNum(i);
            if (curNum < limit) {
                leftMonsterLv.add(i);
            }
        }
        // 怪物都满了的情况
        if (leftMonsterLv.isEmpty()) {
            return;
        }
        // 计算总的权重
        int totalWeight = 0;
        // 当前玩家能刷的等级
        for (int i = 0; i < leftMonsterLv.size(); i++) {
            totalWeight += integerIntegerMap1.get(leftMonsterLv.get(i));
        }
        // 给玩家生成野怪
        while (leftNum > 0) {
            // 给玩家随机一个等级怪
            int randNum = RandomHelper.randMonster(totalWeight);
            int checkNum = 0;
            for (int i = 0; i < leftMonsterLv.size(); i++) {
                int monsterLv = leftMonsterLv.get(i);
                // 权重检查
                checkNum += integerIntegerMap1.get(monsterLv);
                if (randNum > checkNum) {
                    continue;
                }
                if (mapInfo.getMonsterNum(i) >= integerIntegerMap.get(monsterLv)) {
                    // 如果达到上限，当前玩家随机到的数量减1
                    leftNum--;
                    break;
                }
                // 没有达到上限，则创建一个野怪
                Pos monsterPos = worldManager.randPos(player, cellNum);
                // 检查野怪是否正在被攻打,正在被攻打的野怪不能被刷新
                if (!isMonsterPosOk(monsterPos, mapInfo)) {
                    leftNum--;
                    break;
                }
                // 创建一个野怪
                Monster monster = worldManager.addMonster(monsterPos, 1000 + monsterLv, monsterLv, mapInfo, AddMonsterReason.ADD_PLAYER_MONSTER);
                list.add(monster);
                leftNum--;

                break;
            }
        } // end while
    }

    // 刷新玩家怪物
//    public void flushPlayerMonster(
//            Player player,
//            int cellNum,
//            HashBasedTable<Integer, Integer, Integer> monsterFlushNum,
//            HashBasedTable<Integer, Integer, Integer> monsterFlushRate,
//            int monsterNumRange) {
//        // 当前地图Id
//        int mapId = worldManager.getMapId(player);
//        if (mapId <= 0) {
//            return;
//        }
//
//        // 世界系统开放后初级区域即可生成叛军，每个区域生成叛军数量与玩家基地数量相对应，比例为4（叛军数量）：1（玩家基地数量）
//        // 当前玩家周围的野怪
//        int monsterNum = worldManager.getMonsterNum(player);
//        // 剩余野怪
//        int leftNum = monsterNumRange - monsterNum;
//        if (leftNum <= 0) {
//            return;
//        }
//        // System.out.println("=============================leftNum
//        // ======================================");
//
//        // 当前地图信息
//        MapInfo mapInfo = worldManager.getMapInfo(mapId);
//        if (mapInfo == null) {
//            // 还没进入世界地图
//            return;
//        }
//
//        // 当前地图类型
//        int areaType = worldManager.getMapAreaType(mapId);
//        // 皇城单独刷怪
//        if (areaType == 3) {
//            return;
//        }
//
//        // 当前玩家基地等级
//        int commandLv = player.getCommandLv();
//        // 当前玩家周围能生成的野怪的等级范围
//        Range monsterLvRange = staticWorldMgr.getRange(commandLv, areaType);
//        if (monsterLvRange == null) {
//            LogHelper.CONFIG_LOGGER.info("monsterLvRange is null!");
//            return;
//        }
//
//        if (monsterLvRange.getBeg() == null) {
//            LogHelper.CONFIG_LOGGER.info("monsterLvRange.getBeg() == null, commandLv =" + commandLv);
//            return;
//        }
//
//        if (monsterLvRange.getEnd() == null) {
//            LogHelper.CONFIG_LOGGER.info("monsterLvRange.getBeg() == null, commandLv = " + commandLv);
//            return;
//        }
//
//        // 玩家能刷的剩余等级
//        List<Integer> leftMonsterLv = new ArrayList<Integer>();
//        for (int i = monsterLvRange.getBeg(); i <= monsterLvRange.getEnd(); i++) {
//            Integer limit = monsterFlushNum.get(areaType, i);
//            if (limit == null) {
//                // LogHelper.CONFIG_LOGGER.info("limit is null, areaType = " +
//                // areaType + ", lv = " + i);
//                continue;
//            }
//
//            Integer curNum = mapInfo.getMonsterNum(i);
//            if (curNum < limit) {
//                leftMonsterLv.add(i);
//            }
//        }
//
//        // 怪物都满了的情况
//        if (leftMonsterLv.isEmpty()) {
//            return;
//        }
//
//        // 计算总的权重
//        int totalWeight = 0;
//        // 当前玩家能刷的等级
//        for (int i = 0; i < leftMonsterLv.size(); i++) {
//            totalWeight += monsterFlushRate.get(areaType, leftMonsterLv.get(i));
//        }
//
//        // 给玩家生成野怪
//        while (leftNum > 0) {
//            // 给玩家随机一个等级怪
//            int randNum = RandomHelper.randMonster(totalWeight);
//            int checkNum = 0;
//            for (int i = 0; i < leftMonsterLv.size(); i++) {
//                int monsterLv = leftMonsterLv.get(i);
//                // 权重检查
//                checkNum += monsterFlushRate.get(areaType, monsterLv);
//                if (randNum > checkNum) {
//                    continue;
//                }
//
//                // 随机到一个等级, 并检查当前等级是否达到上限
//                if (mapInfo.getMonsterNum(i) >= monsterFlushNum.get(areaType, monsterLv)) {
//                    // 如果达到上限，当前玩家随机到的数量减1
//                    leftNum--;
//                    break;
//                }
//
//                // 没有达到上限，则创建一个野怪
//                Pos monsterPos = worldManager.randPos(player, cellNum);
//                // 检查野怪是否正在被攻打,正在被攻打的野怪不能被刷新
//                if (!isMonsterPosOk(monsterPos, mapInfo)) {
//                    leftNum--;
//                    break;
//                }
//
//                // 创建一个野怪
//                worldManager.addMonster(
//                        monsterPos, 1000 + monsterLv, monsterLv, mapInfo, AddMonsterReason.ADD_PLAYER_MONSTER);
//                // logger.error("flushPlayerMonster addMonster player {} pos {} ",
//                // player.getLord().getLordId(), monsterPos.toString());
//                leftNum--;
//
//                break;
//            }
//        } // end while
//    }

    public boolean isMonsterPosOk(Pos monsterPos, MapInfo mapInfo) {
        if (monsterPos.isError()) {
            return false;
        }

        if (!mapInfo.isFreePos(monsterPos)) {
            return false;
        }

        Entity entity = mapInfo.getEntity(monsterPos);
        if (entity != null) {
            return false;
        }

        return true;
    }

    // 刷新资源
    // 玩家周围3*3格子范围内刷新时不生成采集点, 皇城没有这个限制
    public void flushWorldResource() {
        if (worldManager.isNotOk()) {
            return;
        }

        // 地图管理器
        ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
        // 资源数量分布
        Map<Integer, StaticWorldResNum> resNumMap = staticWorldMgr.getResNumMap();
        // 资源等级分布
        HashBasedTable<Integer, Integer, Integer> resourceConfig = staticWorldMgr.getResFlushConfig();
        // type, level, count
        // 遍历所有地图
        for (MapInfo mapInfo : worldMapInfo.values()) {
            if (mapInfo == null) {
                LogHelper.CONFIG_LOGGER.info("map info is null!");
                continue;
            }

            int mapId = mapInfo.getMapId();
            // 只有中级区域刷资源
            if (mapId < MapId.MIDDLE_CITY_BEGIN_ID || mapId > MapId.MIDDLE_CITY_END_ID) {
                continue;
            }

            Map<Pos, Resource> resourceMap = mapInfo.getResourceMap();
            // 刷掉低于资源30%的资源
            Iterator<Resource> iterator = resourceMap.values().iterator();
            while (iterator.hasNext()) {
                Resource r = iterator.next();
                if (r.getStatus() == 1) {
                    continue;
                }

                int resId = (int) r.getId();
                StaticWorldResource worldResource = staticWorldMgr.getStaticWorldResource(resId);
                if (worldResource == null) {
                    continue;
                }

                float left = (float) r.getCount() / (float) worldResource.getResource();
                // 大于0.3资源的比例没刷掉
                if (left > staticLimitMgr.getResFactor()) {
                    continue;
                }

                worldManager.removeResPosOnly(mapInfo, r.getPos());
                worldManager.synEntityRemove(r, mapInfo.getMapId(), r.getPos()); // 同步资源
                iterator.remove();
            }

            // 当前资源类型、等级、数量
            HashBasedTable<Integer, Integer, Integer> currentRes = HashBasedTable.create();
            for (Resource elem : resourceMap.values()) {
                if (elem == null) {
                    continue;
                }
                int resId = (int) elem.getId();
                StaticWorldResource worldResource = staticWorldMgr.getStaticWorldResource(resId);
                if (worldResource == null) {
                    continue;
                }
                int resType = worldResource.getType();
                Integer count = currentRes.get(resType, elem.getLevel());
                if (count == null) {
                    currentRes.put(resType, elem.getLevel(), 1);
                } else {
                    currentRes.put(resType, elem.getLevel(), count + 1);
                }
            }

            // 资源分布
            for (StaticWorldResNum staticWorldResNum : resNumMap.values()) {
                if (staticWorldResNum == null) {
                    continue;
                }

                int resType = staticWorldResNum.getType();
                int resLv = staticWorldResNum.getLevel();
                int count = (int) staticWorldResNum.getCount();
                Integer currentCount = currentRes.get(resType, resLv);
                if (currentCount == null) {
                    currentCount = 0;
                }
                int configCount = resourceConfig.get(resType, resLv);
                if (currentCount >= configCount) {
                    continue;
                }

                // 一直随机
                for (int i = 1; i <= count; i++) {
                    // 给资源点选择一个位置
                    Pos pos = null;
                    if (resLv >= 7) {
                        pos = mapInfo.randResPickPos();
                    } else {
                        pos = mapInfo.randPickPos();
                    }
                    if (pos.isError() || !mapInfo.isFreePos(pos)) {
                        Entity entity = mapInfo.getEntity(pos);
                        if (!(entity instanceof Resource)) {
                            continue;
                        }

                        // 正在被采集的资源不被刷新
                        Resource resource = (Resource) entity;
                        if (resource.getStatus() == 1) {
                            continue;
                        }
                    }

                    // 创建一个资源点
                    Resource resource = worldManager.createResource(EntityType.Resource, staticWorldResNum.getType(), staticWorldResNum.getLevel());
                    if (resource != null) {
                        mapInfo.addPos(pos, resource);
                        playerManager.clearPos(pos);
                        resourceMap.put(pos, resource);
                        resource.setPos(pos);
                        // LogHelper.GAME_DEBUG.error("刷新资源.....");
                    }

                    Integer nowRes = currentRes.get(resType, resLv);
                    if (nowRes == null) {
                        nowRes = 0;
                    }
                    currentRes.put(resType, resLv, nowRes + 1);
                    if (nowRes >= configCount) {
                        break;
                    }
                } // end 2nd for
            } // end 1rd for
        }
    }

    public void collectResRq(WorldPb.CollectResRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        CommonPb.Pos pos = req.getPos();
        // 目标点
        Pos targetPos = new Pos(pos.getX(), pos.getY());
        Entity entity = mapInfo.getEntity(targetPos);
        if (entity == null) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_RESOURCE_NOT_FOUND);
            return;
        }

        if (entity.getEntityType() == EntityType.Resource) {
//            if (cityManager.isInFortress(targetPos) && player.getCountry() != worldPvpMgr.getPvpCountry() && worldPvpMgr.getPvpCountry() != 0) {
//                handler.sendErrorMsgToPlayer(GameError.NOT_YOUR_COUNTRY_AREA);
//                return;
//            }
        } else if (entity.getEntityType() == EntityType.BIG_RESOURCE) {
            SuperResource resource = (SuperResource) entity;
            if (resource.getState() != SuperResource.STATE_PRODUCED) {
                handler.sendErrorMsgToPlayer(GameError.MARCH_ERR);
                return;
            }
        }

        if (player.getColectNum()) {
            handler.sendErrorMsgToPlayer(GameError.COLLECT_NUN_MAX);
            return;
        }

        // 行军英雄
        int heroId = req.getHeroId();
        Map<Integer, Hero> heroMap = player.getHeros();
        // 检查英雄是否上阵
        if (!isEmbattle(player, heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
            return;
        }

        List<March> hasMarch = player.getMarch(entity.getPos());
        if (hasMarch != null) {
            March march = hasMarch.stream().filter(x -> x.getMarchType() == MarchType.SUPER_COLLECT || x.getMarchType() == MarchType.CollectResource)
                    .findFirst().orElse(null);
            if (march != null) {
                handler.sendErrorMsgToPlayer(GameError.HERO_ALREADY_COLLECTED);
                return;
            }
        }

        // 检查英雄是否可以出征
        Hero hero = heroMap.get(heroId);
        if (hero == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
            return;
        }

        if (!playerManager.isHeroFree(player, heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
            return;
        }

        if (player.isHeroInMarch(heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
            return;
        }

        // 检查武将带兵量
        if (hero.getCurrentSoliderNum() <= 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
            return;
        }

        // 出兵消耗
        int oilCost = getOneHeroMarchOil(heroId, player, targetPos);
        if (player.getResource(ResourceType.OIL) < oilCost) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        //处理普通采集
        List<Integer> heroIds = new ArrayList<Integer>();
        heroIds.add(heroId);
        March march = worldManager.createMarch(player, heroIds, targetPos);
        double season = 0d;
        int id = 0;
        if (entity.getEntityType() == EntityType.Resource) {
            //如果采集点上有驻军,则取消自己城墙保护
            March collectMarch = mapInfo.getMarch(entity.getPos());
            if (collectMarch != null) {
                player.getLord().setProtectedTime(System.currentTimeMillis());
            }
            Resource resource = (Resource) entity;
            resource.setStatus(1);
            // 季节加成
            int resourceId = (int) resource.getId();
            // 剩余数量
            StaticWorldResource config = staticWorldMgr.getStaticWorldResource(resourceId);
            if (config == null) {
                LogHelper.CONFIG_LOGGER.info("config is null, no resourceId found = " + resourceId);
                return;
            }
            id = config.getType();
            //season = worldManager.getResEffect(id);
            march.setMarchType(MarchType.CollectResource);
        } else if (entity.getEntityType() == EntityType.BIG_RESOURCE) {
            SuperResource resource = (SuperResource) entity;
            int resourceId = resource.getResId();
            // 剩余数量
            StaticSuperRes config = staticSuperResMgr.getStaticSuperRes(resourceId);
            if (config == null) {
                LogHelper.CONFIG_LOGGER.info("config is null, no resourceId found = " + resourceId);
                return;
            }
            id = config.getResType();
            march.setMarchType(MarchType.SUPER_COLLECT);
        }
        int addFactor = getAddCollect(id, hero, player);
        march.setAddFactor(addFactor);
        // 添加行军到玩家身上
        player.addMarch(march);
        // 加到世界地图中
        worldManager.addMarch(mapId, march);
        playerManager.subAward(player, AwardType.RESOURCE, ResourceType.OIL, oilCost, Reason.KILL_WORLD_MONSTER);
        WorldPb.CollectResRs.Builder builder = WorldPb.CollectResRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(WorldPb.CollectResRs.ext, builder.build());

        // 同步到世界
        worldManager.synMarch(mapInfo.getMapId(), march);
    }

    public int getAddCollect(int resType, Hero hero, Player player) {
        double season = worldManager.getResEffect(resType);//季节加成
        season += activityManager.actDouble(ActivityConst.ACT_COLLECT_DOUBLE);//活动加成
        Integer heroWarBookSkillEffect = warBookManager.getHeroWarBookSkillEffect(hero.getHeroBooks(), hero.getHeroId(), BookEffectType.SPEED_UP_COLLECT);
        if (null != heroWarBookSkillEffect) {
            season += heroWarBookSkillEffect / 1000.0f;
        }
        StaticWorldCity city = staticWorldMgr.getCity(player.getLord().getCity());
        if (city != null) {
            City city1 = cityManager.getCity(city.getCityId());
            if (city1 != null && city1.getCountry() == player.getCountry()) {
                List<List<Integer>> buff = city.getBuff();
                if (buff != null) {
                    for (List<Integer> x : buff) {
                        if (x.get(0) == CityBuffType.COLLECT && x.get(1) == resType) {
                            season += x.get(2) / DevideFactor.PERCENT_NUM;
                        }
                    }
                }
            }
        }
        season += techManager.getCollectSpeed(player);

        return (int) (season * 100);

    }

    // 城战: 皇城打州、郡可以跨区域,其他只能在自己区域打
    public void attackCityRq(WorldPb.AttackCityRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        CommonPb.Pos pos = req.getPos();
        // 查看当前pos存放的实体
        int mapId = worldManager.getMapId(pos);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        // 增加等级限制
        int playerLevel = player.getLevel();
        if (playerLevel < staticLimitMgr.getNum(36)) {
            handler.sendErrorMsgToPlayer(GameError.CITY_WAR_LEVEL_NOT_ENOUGH);
            return;
        }

        int playerMapId = worldManager.getMapId(player);

        Pos targetPos = new Pos(pos.getX(), pos.getY());
        Entity entity = mapInfo.getEntity(targetPos);
        if (entity == null) {
            handler.sendErrorMsgToPlayer(GameError.TARGET_LOST);
            return;
        }

        if (!(entity instanceof PlayerCity)) {
            handler.sendErrorMsgToPlayer(GameError.TARGET_LOST);
            return;
        }

        PlayerCity playerCity = (PlayerCity) entity;

        // 检查类型
        int type = req.getType();
        int energyCost = worldManager.getEnergy(type);
        int deleteEnergy = 0;
        // 国家官员则减少体力扣除
        CtyGovern govern = countryManager.getGovern(player);
        if (govern != null) {
            StaticCountryGovern staticCountryGovern = staticCountryMgr.getGovern(govern.getGovernId(), 2);
            if (staticCountryGovern != null) {
                deleteEnergy = staticCountryGovern.getPower();
            }
        }
        int buf = seasonManager.getBuf(player, EffectType.EFFECT_TYPE8);
        deleteEnergy = deleteEnergy > buf ? deleteEnergy : buf;
        energyCost -= deleteEnergy;
        energyCost = Math.max(0, energyCost);
        if (energyCost < 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        Item item = itemManager.getItem(player, 197);
        StaticProp staticProp = staticPropMgr.getStaticProp(197);
        int value = staticProp.getEffectValue().get(0).get(0).intValue();
        int num = (int) Math.ceil(energyCost * 1.0f / value);
        int costNum = 0;
        if (item != null && item.getItemNum() > 0) {
            if (item.getItemNum() >= num) {
                energyCost = 0;
                costNum = num;
            } else {
                costNum = item.getItemNum();
                energyCost = energyCost - value * item.getItemNum();
            }
        }
        int energy = player.getEnergy();
        if (energy < energyCost) {
            handler.sendErrorMsgToPlayer(GameError.ENERGY_NOT_ENOUGH);
            return;
        }

        // 检查玩家国家
        long targetLordId = playerCity.getLordId();
        Player target = playerManager.getPlayer(targetLordId);
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 相同国家不能发生城战
        int targetCountry = target.getCountry();
        int myCountry = player.getCountry();
        if (myCountry == targetCountry) {
            handler.sendErrorMsgToPlayer(GameError.SAME_COUNTRY);
            return;
        }

        // 是否在一个区域
        if (playerMapId == MapId.CENTER_MAP_ID && mapId == MapId.CENTER_MAP_ID) { // 不是皇城
            // pass
        } else if (playerMapId == MapId.CENTER_MAP_ID && mapId != MapId.CENTER_MAP_ID) {
            // pass
        } else if (playerMapId != MapId.CENTER_MAP_ID && mapId == MapId.CENTER_MAP_ID) {
            handler.sendErrorMsgToPlayer(GameError.CAN_NOT_ATTACK_PLAYER);
            return;
        } else if (playerMapId != MapId.CENTER_MAP_ID && mapId != MapId.CENTER_MAP_ID) {
            if (playerMapId != mapId) {
                handler.sendErrorMsgToPlayer(GameError.CAN_NOT_ATTACK_PLAYER);
                return;
            } else {
                // pass
            }
        }

        long now = System.currentTimeMillis();

        // 玩家城池是否受保护
        if (mapId != MapId.FIRE_MAP) {
            long protectedEndTime = target.getLord().getProtectedTime();
            if (protectedEndTime > now) {
                handler.sendErrorMsgToPlayer(GameError.PLAYER_IS_PROTECTED);
                return;
            }
        } else {
            // 安全区内不让打
            boolean safePos = flameWarManager.isSafePos(targetPos);
            if (safePos) {
                handler.sendErrorMsgToPlayer(GameError.FIRE_NOT_ATT_SAFE);
                return;
            }
        }

        // 行军英雄
        List<Integer> heroIds = req.getHeroIdList();
        if (heroIds.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH_HEROS);
            return;
        }

        // 检测英雄是否重复
        HashSet<Integer> checkHero = new HashSet<Integer>();
        // 检查英雄是否上阵,是否已经出征
        for (Integer heroId : heroIds) {
            if (!isEmbattle(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
                return;
            }

            if (player.isHeroInMarch(heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
                return;
            }

            checkHero.add(heroId);
        }

        // 有相同的英雄出征
        if (checkHero.size() != heroIds.size()) {
            handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
            return;
        }

        // 检查行军时间
        Lord lord = player.getLord();
        Pos playerPos = player.getPos();

        // 兵书对行军的影响值
        //float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);
        //long period = worldManager.getPeriod(player, playerPos, targetPos, bookEffectMarch);

        long attackPeriod = worldManager.getAttackPeriod(type);
        if (attackPeriod == 0L) {
            LogHelper.CONFIG_LOGGER.info("attackPeriod == 0L!");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        // 生成行军
        March march = worldManager.createMarch(player, heroIds, targetPos);
        long period = march.getPeriod();
        // 跨区域
        if (playerMapId == MapId.CENTER_MAP_ID && playerMapId != mapId) {
            if (type == 1) {
                period = staticLimitMgr.getNum(104) * TimeHelper.SECOND_MS;
            } else if (type == 2) {
                period = staticLimitMgr.getNum(105) * TimeHelper.SECOND_MS;
            } else if (type == 3) {
                period = staticLimitMgr.getNum(106) * TimeHelper.SECOND_MS;
            }
        } else {
            if (period > attackPeriod) {
                handler.sendErrorMsgToPlayer(GameError.TO_LONG_MARCH);
                return;
            }
        }

        long warTime = 0;
        if (type == 1) {
            warTime = period + 1000L;
        } else if (type == 2) {
            warTime = getRunWarTime(period);
        } else if (type == 3) {
            warTime = getFarTime(period);
        }

        // 检查是否皇城跨区域打玩家
        if (playerMapId == MapId.CENTER_MAP_ID && mapId != playerMapId) {
            if (type == 1) {
                warTime = staticLimitMgr.getNum(104) * TimeHelper.SECOND_MS + 1000L;
            } else if (type == 2) {
                warTime = staticLimitMgr.getNum(105) * TimeHelper.SECOND_MS + 1000L;
            } else if (type == 3) {
                warTime = staticLimitMgr.getNum(106) * TimeHelper.SECOND_MS + 1000L;
            }
        }

        if (type == 1) {
            march.setMarchType(MarchType.AttackCityQuick);
        } else if (type == 2 || type == 3) {
            // 生成一个新的warId
            march.setMarchType(MarchType.AttackCityFar); // 需要放到战役里面去
            // 需要计算战争的时间
        }
        int warType = 0;
        if (type == 1) {
            warType = WarType.ATTACK_QUICK;
        } else if (type == 2) {
            warType = WarType.Attack_WARFARE;
        } else if (type == 3) {
            warType = WarType.ATTACK_FAR;
        }

        long warId = mapInfo.maxKey();
        WarInfo warInfo = warManager.createFarWar(warTime, player, target, playerPos, targetPos, warType, warId);
        mapInfo.addWar(warInfo);

        worldManager.synAddCityWar(target, warInfo);

        march.setWarId(warId);
        march.setDefencerId(targetLordId);
        march.setAttackerId(player.roleId);
        march.setSide(1);
        // 检查是否皇城跨区域打玩家
        if (playerMapId == MapId.CENTER_MAP_ID && mapId != playerMapId) {
            if (type == 1) {
                march.setPeriod(staticLimitMgr.getNum(104) * TimeHelper.SECOND_MS);
            } else if (type == 2) {
                march.setPeriod(staticLimitMgr.getNum(105) * TimeHelper.SECOND_MS);
            } else if (type == 3) {
                march.setPeriod(staticLimitMgr.getNum(106) * TimeHelper.SECOND_MS);
            }
            march.setEndTime(now + march.getPeriod());
        }

        if (type == 1) {
            march.setFightTime(march.getEndTime() + 1000L, MarchReason.QuickAttack);
        } else if (type == 2 || type == 3) {
            march.setFightTime(march.getEndTime() + 1000L, MarchReason.FarAttack);
        }

        // 添加行军到玩家身上
        player.addMarch(march);
        // 行军加入战斗
        warInfo.addAttackMarch(march);
        // 加到世界地图中
        worldManager.addMarch(mapId, march);
        // 扣除道具
        if (costNum > 0) {
            playerManager.subAward(player, AwardType.PROP, 197, costNum, Reason.ATTACK_CITY);
        }
        // 扣除体力
        playerManager.subAward(player, AwardType.LORD_PROPERTY, LordPropertyType.ENERGY, energyCost, Reason.ATTACK_CITY);
        // 返回消息
        WorldPb.AttackCityRs.Builder builder = WorldPb.AttackCityRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        builder.setResource(player.wrapResourcePb());
        builder.setEnergy(player.getEnergy());
        builder.setEnergyCD(playerManager.getEnergyCD(player));
        if (item != null) {
            builder.setProp(item.wrapPb());
        }
        handler.sendMsgToPlayer(WorldPb.AttackCityRs.ext, builder.build());
        worldManager.synMarch(mapInfo.getMapId(), march);
        // 玩家保护时间去掉
        playerManager.handleClearProtected(player);
        eventManager.attackCity(player, Lists.newArrayList(type));
    }

    public long getRunWarTime(long period) {
        long marchTime1 = staticLimitMgr.getNum(21) * TimeHelper.SECOND_MS;
        long marchTime2 = staticLimitMgr.getNum(22) * TimeHelper.SECOND_MS;
        if (period < marchTime1) {
            return period + marchTime1;
        } else if (period >= marchTime1 && period < marchTime2) {
            return period;
        }
        return period;
    }

    public long getFarTime(long period) {
        long marchTime1 = staticLimitMgr.getNum(21) * TimeHelper.SECOND_MS;
        long marchTime2 = staticLimitMgr.getNum(22) * TimeHelper.SECOND_MS;
        if (period < marchTime1) {
            return period + marchTime2;
        } else if (period >= marchTime1 && period < marchTime2) {
            return period + marchTime1;
        }

        return period;
    }

    public int getOneHeroMarchOil(int heroId, Player player, Pos targetPos) {
        List<Integer> heroIds = new ArrayList<Integer>();
        heroIds.add(heroId);
        return worldManager.getMarchOil(heroIds, player, targetPos);
    }

    // 获取远征军城池信息
    public void getAttackCity(WorldPb.GetPvpCityRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 获取坐标点
        CommonPb.Pos reqPos = req.getPos();
        Pos pos = new Pos();
        pos.unwrapPb(reqPos);
        // 找到对应的地图信息
        int mapId = worldManager.getMapId(pos);
        // 获取mapInfo
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.info("mapInfo is null!");
            WorldPb.GetPvpCityRs.Builder builder = WorldPb.GetPvpCityRs.newBuilder();
            handler.sendMsgToPlayer(WorldPb.GetPvpCityRs.ext, builder.build());
            return;
        }

        Entity entity = mapInfo.getEntity(pos);
        if (entity == null) {
            // LogHelper.CONFIG_LOGGER.info("entity == null , getPvpCity , pos x = "
            // + pos.getX() + ", pos y = " + pos.getY());
            WorldPb.GetPvpCityRs.Builder builder = WorldPb.GetPvpCityRs.newBuilder();
            handler.sendMsgToPlayer(WorldPb.GetPvpCityRs.ext, builder.build());
            return;
        }

        if (!(entity instanceof PlayerCity)) {
            LogHelper.CONFIG_LOGGER.info("entity type = " + entity.getEntityType() + ", entity level = " + entity.getLevel() + "entity Id =" + entity.getId() + ", entity pos = " + entity.getPos());
            WorldPb.GetPvpCityRs.Builder builder = WorldPb.GetPvpCityRs.newBuilder();
            handler.sendMsgToPlayer(WorldPb.GetPvpCityRs.ext, builder.build());
            return;
        }

        PlayerCity playerCity = (PlayerCity) entity;
        long lordId = playerCity.getLordId();
        WorldPb.GetPvpCityRs.Builder builder = WorldPb.GetPvpCityRs.newBuilder();
        // 如果打的过程中,有迁城的操作,则销毁当前的战斗
        List<CommonPb.CityWarInfo> crateCityWarInfo = worldManager.crateAllWar(lordId, mapInfo, player);
        if (!crateCityWarInfo.isEmpty()) {
            builder.addAllCityWarInfo(crateCityWarInfo);
        }

        handler.sendMsgToPlayer(WorldPb.GetPvpCityRs.ext, builder.build());
//		LogHelper.MESSAGE_LOGGER.info("GetPvpCityRs :{}", builder.build());
    }

    public void attendPvpCityRq(WorldPb.AttendPvpCityRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 增加等级限制
        int playerLevel = player.getLevel();
        if (playerLevel < staticLimitMgr.getNum(36)) {
            handler.sendErrorMsgToPlayer(GameError.CITY_WAR_LEVEL_NOT_ENOUGH);
            return;
        }

        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        long warId = req.getKeyId();
        WarInfo warInfo = worldManager.getPvpWarInfo(mapInfo, warId);
        if (warInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.WAR_IS_OVER);
            return;
        }

        long defencerId = warInfo.getDefencerId();
        Player target = playerManager.getPlayer(defencerId);
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.WAR_IS_OVER);
            return;
        }

        // 相同国家不能发生城战
        int defencerCountry = target.getCountry();
        int myCountry = player.getCountry();
        int side;
        if (myCountry == defencerCountry) {
            side = 2;
        } else {
            side = 1;
        }

        if (side == 1 && warInfo.getWarType() == WarType.ATTACK_QUICK) {
            handler.sendErrorMsgToPlayer(GameError.QUICK_WAR_CAN_NOT_ATTEND);
            return;
        }

        // 是否在一个区域
        int MapId = worldManager.getMapId(player.getPosX(), player.getPosY());
        int targetMapId = worldManager.getMapId(target.getPosX(), target.getPosY());
        if (MapId != 20) { // 不是皇城
            if (mapId != targetMapId) {
                handler.sendErrorMsgToPlayer(GameError.NOT_SAME_MAP);
                return;
            }
        }

        // 玩家城池是否受保护
        long protectedEndTime = target.getLord().getProtectedTime();
        long now = System.currentTimeMillis();
        if (protectedEndTime > now) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_IS_PROTECTED);
            return;
        }

        // 行军英雄
        List<Integer> heroIds = req.getHeroIdList();
        if (heroIds.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.NO_HERO_MARCH);
            return;
        }

        Map<Integer, Hero> heroMap = player.getHeros();
        // 检测英雄是否重复
        HashSet<Integer> checkHero = new HashSet<Integer>();
        // 检查英雄是否上阵
        for (Integer heroId : heroIds) {
            if (!isEmbattle(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
                return;
            }
            checkHero.add(heroId);
        }

        // 有相同的英雄出征
        if (checkHero.size() != heroIds.size()) {
            handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
            return;
        }

        // 检查英雄是否可以出征
        for (Integer heroId : heroIds) {
            Hero hero = heroMap.get(heroId);
            if (hero == null) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
                return;
            }

            if (!playerManager.isHeroFree(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
                return;
            }

            // 检查武将带兵量
            if (hero.getCurrentSoliderNum() <= 0) {
                handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
                return;
            }
        }

        // 行军消耗
        // 出兵消耗
        Pos targetPos = warInfo.getDefencerPos();

        // 检查行军时间
        Pos playerPos = player.getPos();
        int distance = worldManager.distance(playerPos, targetPos);
        int configNum = staticLimitMgr.getNum(10);
        // long period = distance * configNum * TimeHelper.SECOND_MS;

        // 兵书对行军影响值
        float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);

        long period = worldManager.getPeriod(player, playerPos, targetPos, bookEffectMarch);
        int type = warInfo.getWarType();
        long attackPeriod = 0;
        if (type == WarType.Attack_WARFARE) {
            attackPeriod = staticLimitMgr.getNum(22) * TimeHelper.SECOND_MS;
        } else if (type == WarType.ATTACK_FAR || type == WarType.RIOT_WAR) {
            attackPeriod = staticLimitMgr.getNum(23) * TimeHelper.SECOND_MS;
        } else if (type == WarType.ATTACK_QUICK) {
            attackPeriod = warInfo.getEndTime() - now;
        }

        if (attackPeriod <= 0L) {
            handler.sendErrorMsgToPlayer(GameError.WAR_IS_STARTED);
            return;
        }

        if (period > attackPeriod) {
            handler.sendErrorMsgToPlayer(GameError.TO_LONG_MARCH);
            return;
        }

        // 检查战斗是否已经结束了
        if (worldManager.isPvpWarOver(warInfo)) {
            handler.sendErrorMsgToPlayer(GameError.WAR_IS_STARTED);
            return;
        }

        // 生成行军
        March march = worldManager.createMarch(player, heroIds, targetPos);
        // 检查行军是否超过战斗时间
        if (!isMarchWarOk(march.getPeriod(), warInfo)) {
            handler.sendErrorMsgToPlayer(GameError.TO_LONG_MARCH);
            return;
        }

        march.setDefencerId(warInfo.getDefencerId());
        march.setAttackerId(warInfo.getAttackerId());
        march.setSide(side);
        if (warInfo.getWarType() == WarType.ATTACK_FAR || warInfo.getWarType() == WarType.Attack_WARFARE) {
            march.setMarchType(MarchType.AttackCityFar); // 需要放到战役里面去
        } else if (warInfo.getWarType() == WarType.ATTACK_QUICK) {
            march.setMarchType(MarchType.AttackCityQuick);
            if (side == 2) {
                march.setMarchType(MarchType.QUICK_ASSIST);
            }
        } else if (warInfo.getWarType() == WarType.RIOT_WAR) {
            march.setMarchType(MarchType.RiotWar);
        }

        march.setWarId(warInfo.getWarId());
        march.setFightTime(march.getEndTime() + 1000L, MarchReason.AttendPvpWar);
        // add march to player
        player.addMarch(march);
        // attack or defence
        worldManager.synAddCityWar(target, warInfo);
        // add world map
        worldManager.addMarch(mapId, march);
        if (side == 1) {
            warInfo.addAttackMarch(march);
        } else if (side == 2) {
            warInfo.addDefenceMarch(march);
        }

        // return msg
        WorldPb.AttendPvpCityRs.Builder builder = WorldPb.AttendPvpCityRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(WorldPb.AttendPvpCityRs.ext, builder.build());
        worldManager.synMarch(mapInfo.getMapId(), march);

        // clear protected.
        if (side == 1) {
            playerManager.handleClearProtected(player);
        }
    }

    // 发起国战
    public synchronized void countryWarRq(WorldPb.CountryWarRq req, ClientHandler handler) {
        // 需要45级才能宣战
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int cityId = req.getCityId();
        // 找到city的配置
        StaticWorldCity config = staticWorldMgr.getCity(cityId);
        if (config == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        StaticWorldCityType staticWorldCityType = staticWorldCityTypeMgr.getStaticWorldCityType(config.getType());
        WorldData worldData = worldManager.getWolrdInfo();
        if (worldData.getTasks().get(staticWorldCityType.getNeedTarget()) == null) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_TARGET_NOT_OPEN);
            return;
        }

        City city = cityManager.getCity(cityId);
        if (city == null || city.getCountry() == player.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.SAME_COUNTRY);
            return;
        }

        // 需要的等级
        int needLevel = staticLimitMgr.getNum(35);
        if (player.getLevel() < needLevel) {
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }

        int playerMapId = worldManager.getMapId(player);
        int cityMapId = config.getMapId();
        if (playerMapId != MapId.CENTER_MAP_ID && playerMapId != cityMapId) { // 皇城可以宣战任何地方
            handler.sendErrorMsgToPlayer(GameError.CAN_NOT_CALL_WAR);
            return;
        }

        // cityType = 8 的只有国王才能宣战
        if (config.getType() == 8) {
            if (!countryManager.hasWarPermission(player.roleId)) {
                handler.sendErrorMsgToPlayer(GameError.WAR_PERMISSION_ERROR);
                return;
            }

            // 如果一个国家已经有都城，就不能对其他都城宣战
            if (cityManager.hasSquareFortress(player.getCountry())) {
                handler.sendErrorMsgToPlayer(GameError.ALREADY_HAS_CITY);
                return;
            }

            // 如果这个都城已经有国家了，也不能宣战
            if (cityManager.squareHasCountry(cityId)) {
                handler.sendErrorMsgToPlayer(GameError.SQUARE_HAS_CITY);
                return;
            }
        }

        if (config.getType() == 9) {
            handler.sendErrorMsgToPlayer(GameError.CAN_NOT_CALL_WAR);
            return;
        }

        if (config.getType() == CityType.FAMOUS_CITY) {
            // 如果当前国家已经有7个名城了，不能宣战群雄
            if (city.getCountry() == 0 && cityManager.hasFullFamous(player.getCountry()) && city.getState() == CityState.COMMON_MAKE_ITEM) {
                handler.sendErrorMsgToPlayer(GameError.REACH_MAX_FAMOUS_NUM);
                return;
            }
        }

        // 查看当前pos存放的实体
        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        // 检查city是否处于保护时间
        long now = System.currentTimeMillis();
        if (city.getProtectedTime() > now) {
            handler.sendErrorMsgToPlayer(GameError.CITY_IS_PROTECTED);
            return;
        }

        // 检查当前城池是否有战争,如果有说明已经宣战了
        IWar war = worldManager.getCountryWar(cityId, player.getCountry(), mapInfo);
        if (war != null) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_CALL_COUNTRY_WAR);
            return;
        }

        // 已经宣战了一个不能宣战第二个
        boolean isCall;
        if (mapInfo.getWarMap().values().stream().filter(e -> {
            if (e instanceof CountryCityWarInfo) {
                CountryCityWarInfo countryCityWarInfo = (CountryCityWarInfo) e;
                StaticWorldCity staticWorldCity = staticWorldMgr.getCity(countryCityWarInfo.getCityId());
                if (staticWorldCity != null && staticWorldCity.getType() == CityType.SQUARE_FORTRESS) {// 已宣要塞
                    return true;
                }
            }
            return false;
        }).isParallel()) {
            isCall = true;
        } else {
            isCall = false;
        }

        if (isCall) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_CALL_COUNTRY_WAR);
            return;
        }

        /**
         * 抢夺名城官员才能宣战
         */
        /*
         * if(city.getState() != CityState.COMMON_MAKE_ITEM){ int governId = 0; CountryData country = countryManager.getCountry(player.getLord().getCountry()); if (null != country) { CtyGovern king = country.getCtyGovernOffer(player.getLord().getLordId()); if (null != king) { governId = king.getGovernId(); } } if(governId ==0){ handler.sendErrorMsgToPlayer(GameError.STEAL_CITY_IS_NOT_GOVERN); return; } }
         */

        // 创建一个战争
        Lord lord = player.getLord();
        Pos playerPos = player.getPos();
        Pos targetPos = new Pos(config.getX(), config.getY());

        long period = config.getWarPeriod() * TimeHelper.MINUTE_MS;

        if (staticLimitMgr.isSimpleWarOpen()) {
            period = staticLimitMgr.getNum(115);
        }

        // 抢夺名称阵营战的时间(分钟)
        if (city.getState() != CityState.COMMON_MAKE_ITEM) {
            StaticActStealCity stealConfig = stealCityManager.getConfig();
            if (null != stealConfig && stealConfig.getWarPeriod() != 0) {
                period = stealConfig.getWarPeriod() * TimeHelper.MINUTE_MS;
            }
        }

        if (testManager.isOpenTestMode()) {
            period = 30000L;
        }

        CountryCityWarInfo countryWar = warManager.createCountryWar(period, lord.getLordId(), player.getCountry(), cityId, playerPos, targetPos, WarType.ATTACK_COUNTRY);
        if (countryWar != null) {
            mapInfo.addWar(countryWar);
        } else {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        WorldPb.CountryWarRs.Builder builder = WorldPb.CountryWarRs.newBuilder();
        worldManager.handleWarSoldier(countryWar);
        builder.setWarInfo(countryWar.wrapPb(countryWar.isJoin(player)));
        handler.sendMsgToPlayer(WorldPb.CountryWarRs.ext, builder.build());

        // 同步给地图上的玩家
        warManager.synWarInfo(countryWar);

        // 通知国家所有玩家国家战
        chatManager.synCountryWar(player, city, mapId, config.getX(), config.getY());
    }

    // 45级以下的不能参加国战
    public void attendCountryWar(WorldPb.AttendCountryWarRq req, ClientHandler handler) {
        // 需要45级才能宣战
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 查看当前pos存放的实体
        int mapId = req.getMapId();
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        // 需要的等级
        int needLevel = staticLimitMgr.getNum(35);
        if (player.getLevel() < needLevel) {
            handler.sendErrorMsgToPlayer(GameError.CANNOT_ATTEND_COUNTRY);
            return;
        }

        long warId = (long) req.getWarId();
//		Map<Long, WarInfo> countryWarMap = mapInfo.getCountryWarMap();
//		WarInfo warInfo = countryWarMap.get(warId);
        IWar war = mapInfo.getWar(warId);
        if (war == null) {
            handler.sendErrorMsgToPlayer(GameError.WAR_NOT_EXISTS);
            return;
        }

        // 检查国战是否结束
        long now = System.currentTimeMillis();
        if (war.getEndTime() <= now) {
            handler.sendErrorMsgToPlayer(GameError.WAR_IS_OVER);
            return;
        }

        // 行军英雄
        List<Integer> heroIds = req.getHeroIdList();
        if (heroIds.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH_HEROS);
            return;
        }
        Map<Integer, Hero> heroMap = player.getHeros();
        // 检测英雄是否重复
        HashSet<Integer> checkHero = new HashSet<Integer>();
        // 检查英雄是否上阵
        for (Integer heroId : heroIds) {
            if (!isEmbattle(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
                return;
            }

            if (player.isHeroInMarch(heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
                return;
            }

            checkHero.add(heroId);
        }

        // 有相同的英雄出征
        if (checkHero.size() != heroIds.size()) {
            handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
            return;
        }

        // 检查英雄是否可以出征
        for (Integer heroId : heroIds) {
            Hero hero = heroMap.get(heroId);
            if (hero == null) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
                return;
            }

            if (!playerManager.isHeroFree(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
                return;
            }

            // 检查武将带兵量
            if (hero.getCurrentSoliderNum() <= 0) {
                handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
                return;
            }
        }

        CountryCityWarInfo warInfo = (CountryCityWarInfo) war;

        // 行军消耗
        // 出兵消耗
        int side = 0;
        if (player.getCountry() == warInfo.getAttackerCountry()) {
            side = 1;
        } else if (player.getCountry() == warInfo.getDefencerCountry()) {
            side = 2;
        } else {
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_ERROR);
            return;
        }

        // 生成行军
        March march = worldManager.createMarch(player, heroIds, warInfo.getDefencerPos());
        // 检查行军是否超过战斗时间
        if (!isMarchWarOk(march.getPeriod(), warInfo)) {
            handler.sendErrorMsgToPlayer(GameError.TO_LONG_MARCH);
            return;
        }

        march.setFightTime(march.getEndTime() + 1000L, MarchReason.AttendCountryWar);
        march.setDefencerId(warInfo.getDefencerId());
        march.setAttackerId(warInfo.getAttackerId());

        march.setSide(side);
        march.setMarchType(MarchType.CountryWar); // 需要放到战役里面去
        march.setWarId(warId);
        // 添加行军到玩家身上
        player.addMarch(march);
        if (side == 1) {
            warInfo.addAttackMarch(march);
        } else if (side == 2) {
            warInfo.addDefenceMarch(march);
        }
        // 加到世界地图中
        worldManager.addMarch(mapId, march);
        // 返回消息
        WorldPb.AttendCountryWarRs.Builder builder = WorldPb.AttendCountryWarRs.newBuilder();
        worldManager.handleWarSoldier(warInfo);
        builder.setWarInfo(warInfo.wrapPb(warInfo.isJoin(player)));
        builder.setMarch(worldManager.wrapMarchPb(march));
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(WorldPb.AttendCountryWarRs.ext, builder.build());

        worldManager.synMarch(mapInfo.getMapId(), march);

        // 玩家保护时间去掉
        if (side == 1) {
            playerManager.handleClearProtected(player);
        }
    }

    // 获取竞选信息
    public void getElection(WorldPb.GetElectionRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int cityId = req.getCityId();
        City city = cityManager.getCity(cityId);
        if (city == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CITY);
            return;
        }

        long now = System.currentTimeMillis();
        if (city.getElectionEndTime() <= now) {
            handler.sendErrorMsgToPlayer(GameError.ELECTION_IS_OVER);
            return;
        }

        // 获取城池信息
        Map<Long, CityElection> cityElections = cityManager.getCityElection(cityId);
        if (cityElections == null) {
            WorldPb.GetElectionRs.Builder builder = WorldPb.GetElectionRs.newBuilder();
            builder.setElectionEndTime(city.getElectionEndTime());
            handler.sendMsgToPlayer(WorldPb.GetElectionRs.ext, builder.build());
        } else {
            List<CityElection> electionList = new ArrayList<CityElection>();
            for (CityElection election : cityElections.values()) {
                electionList.add(election);
            }
            WorldPb.GetElectionRs.Builder builder = WorldPb.GetElectionRs.newBuilder();
            Collections.sort(electionList);
            for (CityElection cityElection : electionList) {
                if (cityElection == null) {
                    continue;
                }
                long lordId = cityElection.getLordId();
                Player election = playerManager.getPlayer(lordId);
                if (election == null) {
                    continue;
                }
                CommonPb.CityElection.Builder data = CommonPb.CityElection.newBuilder();
                data.setName(election.getNick());
                data.setTitle(election.getTitle());
                data.setEndTime(cityElection.getElectionTime());
                builder.addCityElection(data);
            }
            builder.setElectionEndTime(city.getElectionEndTime());
            handler.sendMsgToPlayer(WorldPb.GetElectionRs.ext, builder.build());
        }
    }

    // 参加选举
    public void electionCityRq(WorldPb.ElectionCityRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int ownerCityId = player.getCityId();
        if (ownerCityId != 0) {
            handler.sendErrorMsgToPlayer(GameError.IS_ALREADY_CITY_OWNER);
            return;
        }

        int cityId = req.getCityId();
        City city = cityManager.getCity(cityId);
        if (city == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CITY);
            return;
        }

        if (city.getCountry() == 0) {
            handler.sendErrorMsgToPlayer(GameError.CITY_NOT_TAKEN);
            return;
        }

        long now = System.currentTimeMillis();
        if (city.getElectionEndTime() <= now) {
            handler.sendErrorMsgToPlayer(GameError.ELECTION_IS_OVER);
            return;
        }

        // 检查选举的消耗 = 重建消耗
        StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
        if (staticWorldCity == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int rebuildIron = staticWorldCity.getRebuildIron() / staticWorldCity.getRebuildSoldier() * 100;
        long iron = player.getIron();
        if (iron < rebuildIron) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        int rebuildCopper = staticWorldCity.getRebuildCopper() / staticWorldCity.getRebuildSoldier() * 100;
        long copper = player.getCopper();
        if (copper < rebuildCopper) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        // 检查是否已经参加竞选了
        if (cityManager.isCityElectionExists(cityId, player.roleId)) {
            handler.sendErrorMsgToPlayer(GameError.ELECTION_ALREADY_ATTEND);
            return;
        }

        // 检查国家
        if (player.getCountry() != city.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.ELECTION_ALREADY_ATTEND);
            return;
        }

        // 检查是否是参战的人员
        HashSet<Long> warAttenders = cityManager.getWarAttenders(cityId);
        if (warAttenders.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        if (!warAttenders.contains(player.roleId)) {
            handler.sendErrorMsgToPlayer(GameError.NO_ATTEND_WAR);
            return;
        }

        // 生成竞选
        List<Award> awards = new ArrayList<Award>();
        awards.add(new Award(0, AwardType.RESOURCE, ResourceType.IRON, rebuildIron));
        awards.add(new Award(0, AwardType.RESOURCE, ResourceType.COPPER, rebuildCopper));
        CityElection cityElection = cityManager.createCityElection(player.roleId, awards, cityId);
        if (cityElection != null) {
            cityManager.addCityElection(cityElection, cityId);
        } else {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        playerManager.subIron(player, rebuildIron, Reason.REBUILD_CITY);
        playerManager.subCopper(player, rebuildCopper, Reason.REBUILD_CITY);

        WorldPb.ElectionCityRs.Builder builder = WorldPb.ElectionCityRs.newBuilder();
        CommonPb.CityElection.Builder data = CommonPb.CityElection.newBuilder();
        data.setName(player.getNick());
        data.setTitle(player.getTitle());
        data.setEndTime(cityElection.getElectionTime());
        builder.setCityElection(data);
        handler.sendMsgToPlayer(WorldPb.ElectionCityRs.ext, builder.build());
    }

    // 城主撤销
    public void cancelCityOwner(WorldPb.CancelCityOwnerRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int cityId = req.getCityId();
        City city = cityManager.getCity(cityId);
        if (city == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CITY);
            return;
        }

        cityManager.clearCityLordId(city);
        city.setEndTime(0);
        StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
        // 野怪也血量也清零
        CityMonster cityMonster = cityManager.getCityMonster(cityId);
        if (cityMonster != null) {
            Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
            if (monsterInfoMap != null) {
                for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
                    cityMonsterInfo.setSoldier(0);
//                    if (worldCity.getType() == CityType.FAMOUS_CITY
//                            || worldCity.getType() == CityType.SQUARE_FORTRESS) {
//                        // pass
//                    } else {
//
//                    }
                }
            }
        }

        WorldPb.CancelCityOwnerRs.Builder builder = WorldPb.CancelCityOwnerRs.newBuilder();
        CommonPb.CityOwnerInfo.Builder info = worldManager.createCityOwnerInfo(city, player);
        builder.setInfo(info);
        handler.sendMsgToPlayer(WorldPb.CancelCityOwnerRs.ext, builder.build());
        worldManager.synMapCity(worldManager.getMapId(player), cityId);
        doCancelOwnerDaily(player, city);
    }

    // XXX深明大义，让出了YY区域ZZ据点[坐标]
    public void doCancelOwnerDaily(Player player, City city) {
        if (player == null || city == null) {
            LogHelper.CONFIG_LOGGER.info("player == null || city == null");
            return;
        }

        int cityId = city.getCityId();
        StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
        if (worldCity == null) {
            LogHelper.CONFIG_LOGGER.info("worldCity is null!");
            return;
        }

        CtyDaily ctyDaily = new CtyDaily();
        ctyDaily.setDailyId(CountryDailyId.City_Cancel_Owner);
        ctyDaily.setTime(System.currentTimeMillis());
        ctyDaily.setMapId(worldCity.getMapId());
        ctyDaily.setCityId(cityId);
        ctyDaily.setPlayerName(player.getNick());
        countryManager.addCountryDaily(player.getCountry(), ctyDaily);
    }

    public void getPlayerPosRq(WorldPb.GetPlayerPosRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int mapId = req.getMapId();
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.MAP_INFO_NOT_EXISTS);
            return;
        }

        Map<Pos, PlayerCity> playerCityMap = mapInfo.getPlayerCityMap();
        StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapId);
        if (staticWorldMap == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int res;
        WorldPb.GetPlayerPosRs.Builder builder = WorldPb.GetPlayerPosRs.newBuilder();
        builder.setMapId(mapId);
        for (PlayerCity playerCity : playerCityMap.values()) {
            if (playerCity == null) {
                continue;
            }
            Pos pos = playerCity.getPos();
            if (pos.isError()) {
                continue;
            }
            res = playerCity.getLevel() * 10000000 + pos.getX() * 10000 + pos.getY() * 10 + playerCity.getCountry();
            builder.addInfo(res);
        }
        handler.sendMsgToPlayer(WorldPb.GetPlayerPosRs.ext, builder.build());
    }

    // 获取城主信息
    public void getCityOwnRq(WorldPb.GetCityOwnRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int cityId = req.getCityId();
        City city = cityManager.getCity(cityId);
        if (city == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CITY);
            return;
        }

        long lordId = city.getLordId();

        WorldPb.GetCityOwnRs.Builder builder = WorldPb.GetCityOwnRs.newBuilder();
        long now = System.currentTimeMillis();
        int status = 0;
        int country = city.getCountry();
        if (city.getCityId() == CityId.WORLD_CITY_ID) {
            country = worldPvpMgr.getPvpCountry();
        }

        if (lordId != 0 && city.getEndTime() > now && country != 0) {
            Player owner = playerManager.getPlayer(lordId);
            if (owner != null) {
                builder.setName(owner.getNick());
            }
            status = 2;
        }

        if (lordId == 0 && country != 0) {
            status = 1;
        }

        if (country == 0) {
            status = 0;
        }
        builder.setCountry(country);
        builder.setStatus(status);
        builder.setEndTime(city.getEndTime());
        builder.setOwnerId(city.getLordId());
        handler.sendMsgToPlayer(WorldPb.GetCityOwnRs.ext, builder.build());
    }

    // 重建城池, 不能跨区域
    public void rebuildCityRq(WorldPb.RebuildCityRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int cityId = req.getCityId();
        City city = cityManager.getCity(cityId);
        if (city == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CITY);
            return;
        }

        int mapId = worldManager.getMapId(player);
        StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
        if (staticWorldCity == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        // 重建的时候判断城主
        int ownerCityId = player.getCityId();
        if (ownerCityId != 0) {
            City cityOwn = cityManager.getCity(ownerCityId);
            if (cityOwn != null && cityOwn.getLordId() == player.getLord().getLordId()) {
                handler.sendErrorMsgToPlayer(GameError.IS_ALREADY_CITY_OWNER);
                return;
            }
        }

        if (city.getLordId() != 0) {
            handler.sendErrorMsgToPlayer(GameError.CITY_HAS_OWNER);
            return;
        }

        if (staticWorldCity.getMapId() != mapId) {
            handler.sendErrorMsgToPlayer(GameError.NOT_SAME_MAP);
            return;
        }

        // 正在选举中不能重建
        long now = System.currentTimeMillis();
        long electionEndTime = city.getElectionEndTime();
        // 正在选举中
        if (electionEndTime > now) {
            handler.sendErrorMsgToPlayer(GameError.IS_ELECTIONING);
            return;
        }

        // 检查是否已经参加竞选了
        if (cityManager.isCityElectionExists(cityId, player.roleId)) {
            handler.sendErrorMsgToPlayer(GameError.ELECTION_ALREADY_ATTEND);
            return;
        }

        // 已经有人选举了
        if (city.getLordId() != 0 && city.getEndTime() > now) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_HAVE_CITY_OWNER);
            return;
        }

        // 城池不是当前国家的
        if (city.getCountry() != player.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_ERROR);
            return;
        }

        // 检查选举的消耗 = 重建消耗
        // 消耗翻倍

        int one = staticWorldCity.getRecoverSoldier();
        // int res = MathHelper.devide(100, one);
        int rebuildIron = staticWorldCity.getRebuildIron() / staticWorldCity.getRebuildSoldier() * 100;
        long iron = player.getIron();
        if (iron < rebuildIron) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        int rebuildCopper = staticWorldCity.getRebuildCopper() / staticWorldCity.getRebuildSoldier() * 100;
        long copper = player.getCopper();
        if (copper < rebuildCopper) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        // 当选城主
        city.setLordId(player.roleId);
        player.setCityId(cityId);
        long period = staticWorldCity.getOwnPeriod() * TimeHelper.HOUR_MS;
        city.setEndTime(System.currentTimeMillis() + period);

        // 设置城主的邮件发放时间
        cityManager.handleCityAwardTime(city);

        playerManager.subIron(player, rebuildIron, Reason.REBUILD_CITY);
        playerManager.subCopper(player, rebuildCopper, Reason.REBUILD_CITY);

        cityManager.handleRecSoldier(cityId);

        // 参战人员全部清除
        warManager.clearWarAttender(cityId);

        WorldPb.RebuildCityRs.Builder builder = WorldPb.RebuildCityRs.newBuilder();
        builder.setInfo(worldManager.createCityOwnerInfo(city, player));
        builder.setResource(player.wrapResourcePb());

        handler.sendMsgToPlayer(WorldPb.RebuildCityRs.ext, builder.build());

        worldManager.synMapCity(worldManager.getMapId(player), cityId);

        doRebuildCountryDaily(player, mapId, cityId);
    }

    // 2.XX重建了YY区域ZZ据点[坐标]
    public void doRebuildCountryDaily(Player player, int mapId, int cityId) {
        if (player == null) {
            return;
        }
        CtyDaily ctyDaily = new CtyDaily();
        ctyDaily.setDailyId(CountryDailyId.City_Fixed_Id);
        ctyDaily.setTime(System.currentTimeMillis());
        ctyDaily.setMapId(mapId);
        ctyDaily.setCityId(cityId);
        ctyDaily.setPlayerName(player.getNick());
        countryManager.addCountryDaily(player.getCountry(), ctyDaily);
    }

    // 修复城池, 不能跨区域
    public void fixCityRq(WorldPb.FixCityRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int cityId = req.getCityId();
        // 不能跨区域
        int mapId = worldManager.getMapId(player);
        StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
        if (staticWorldCity == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        if (staticWorldCity.getMapId() != mapId) {
            handler.sendErrorMsgToPlayer(GameError.NOT_SAME_MAP);
            return;
        }

        City city = cityManager.getCity(cityId);
        if (city == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CITY);
            return;
        }

        // 城池不是当前国家的
        if (city.getCountry() != player.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_ERROR);
            return;
        }

        // 没有城主不能修复
        if (city.getLordId() == 0) {
            handler.sendErrorMsgToPlayer(GameError.CITY_NO_OWNER);
            return;
        }

        // 检查消耗
        int rebuildIron = staticWorldCity.getRebuildIron();
        long iron = player.getIron();
        if (iron < rebuildIron) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        int rebuildCopper = staticWorldCity.getRebuildCopper();
        long copper = player.getCopper();
        if (copper < rebuildCopper) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        // 检查城池的血量
        CityMonster cityMonster = cityManager.getCityMonster(cityId);
        if (cityMonster == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            LogHelper.CONFIG_LOGGER.info("cityMonster is null!");
            return;
        }

        Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
        if (monsterInfoMap == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            LogHelper.CONFIG_LOGGER.info("monsterInfoMap is null!");
            return;
        }

        int totalSodiler = 0;
        int currentSoldier = 0;
        for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
            if (cityMonsterInfo == null) {
                continue;
            }
            currentSoldier += cityMonsterInfo.getSoldier();
            totalSodiler += cityMonsterInfo.getMaxSoldier();
        }

        if (currentSoldier >= totalSodiler) {
            handler.sendErrorMsgToPlayer(GameError.CITY_SOLDIER_FULL);
            return;
        }

        // 恢复速度
        int recoverSoldier = staticWorldCity.getRebuildSoldier();
        double percent = (double) recoverSoldier / 100.0;
        int soldierAdd = (int) (percent * (double) totalSodiler);
        soldierAdd = Math.max(0, soldierAdd);
        int totalCount;

        for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
            int current = cityMonsterInfo.getSoldier();
            totalCount = current + soldierAdd;
            if (totalCount < cityMonsterInfo.getMaxSoldier()) {
                cityMonsterInfo.setSoldier(totalCount);
                break;
            } else {
                int max = cityMonsterInfo.getMaxSoldier();
                cityMonsterInfo.setSoldier(cityMonsterInfo.getMaxSoldier());
                soldierAdd -= (max - current);
            }
        }

        playerManager.subIron(player, rebuildIron, Reason.FIX_CITY);
        playerManager.subCopper(player, rebuildCopper, Reason.FIX_CITY);

        // 计算城池血量
        int total = 0;
        for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
            total += cityMonsterInfo.getSoldier();
        }

        // 返回消息
        WorldPb.FixCityRs.Builder builder = WorldPb.FixCityRs.newBuilder();
        builder.setResource(player.wrapResourcePb());
        builder.setCityId(cityId);
        builder.setSoldier(total);
        handler.sendMsgToPlayer(WorldPb.FixCityRs.ext, builder.build());

        worldManager.synMapCity(worldManager.getMapId(player), cityId);
    }

    // 击杀世界boss
    public void killWorldBoss(WorldPb.KillWorldBossRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        WorldTargetTask worldTargetTask = worldTargetTaskService.getWorldBossTarget();
        if (worldTargetTask == null) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_BOSS_IS_DIE);
            logger.error("AttackWorldBoss GameError {}", GameError.WORLD_BOSS_IS_DIE.toString());
            return;
        }

        StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(worldTargetTask.getTaskId());
        StaticActWorldBoss staticActWorldBoss = staticActWorldBossMgr.getStaticActWorldBoss(staticWorldNewTarget.getSubObject());
        WorldData worldData = worldManager.getWolrdInfo();
        if (worldData == null) {
            LogHelper.CONFIG_LOGGER.info("worldData is null");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        Lord lord = player.getLord();
        if (lord == null) {
            LogHelper.CONFIG_LOGGER.info("lord is null");
            handler.sendErrorMsgToPlayer(GameError.NO_LORD);
            return;
        }

        WorldBoss worldBoss = worldData.getShareBoss();
        // 找当前世界boss的血量
        if (worldBoss == null) {
            LogHelper.CONFIG_LOGGER.info("worldBoss is null!");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        if (worldBoss.getSoldier() <= 0) {
            handler.sendErrorMsgToPlayer(GameError.WOLRDBOSS_IS_FIGHT);
            return;
        }

        StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(worldBoss.getMonsterId());
        if (staticMonster == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        // 没有出站的英雄
        List<Integer> heroList = req.getHeroIdsList();
        if (heroList.size() <= 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_HERO_FIGHT);
            return;
        }

        // 应该用玩家出战的英雄
        // 出战英雄的Id有没有重复
        Set<Integer> checkHeroSet = new HashSet<Integer>();
        HashMap<Integer, Hero> heros = player.getHeros();
        // 检查出战的英雄Id的合法性
        for (Integer heroId : heroList) {
            Hero hero = heros.get(heroId);
            if (hero == null) {
                handler.sendErrorMsgToPlayer(GameError.HERO_FIGHT_NOT_EXISTS);
                return;
            }
            checkHeroSet.add(heroId);
        }

        // 检查出战的英雄有重复的
        if (checkHeroSet.size() != heroList.size()) {
            handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
            return;
        }
        // 检查是否是非上阵武将
        for (Integer heroId : heroList) {
            if (!heroManager.isEmbattleHero(player, heroId)) {
                LogHelper.CONFIG_LOGGER.info("not embattle hero, heroId =" + heroId);
                handler.sendErrorMsgToPlayer(GameError.NOT_EMBATTLE_HERO);
                return;
            }
        }

        WorldPersonalGoal worldPersonalGoal = player.getPersonalGoals().get(worldTargetTask.getTaskId());
        if (worldPersonalGoal == null) {
            worldPersonalGoal = new WorldPersonalGoal();
            worldPersonalGoal.setTaskId(worldTargetTask.getTaskId());
            player.getPersonalGoals().put(worldPersonalGoal.getTaskId(), worldPersonalGoal);
        } else {
            // 看是否是同一天
            if (!TimeHelper.isSameDayOfMillis(worldPersonalGoal.getLastAttackBossTime(), System.currentTimeMillis())) {
                worldPersonalGoal.setChallengeNumber(0);
                worldPersonalGoal.setLastAttackBossTime(System.currentTimeMillis());
            }
        }
        // 看次数够不够
        if (worldPersonalGoal.getChallengeNumber() >= staticActWorldBoss.getChallengeNumber()) {
            handler.sendErrorMsgToPlayer(GameError.ATTACK_WORLD_BOSS_TIME_NOT_ENOUGH);
            logger.error("AttackWorldBoss GameError {}", GameError.ATTACK_WORLD_BOSS_TIME_NOT_ENOUGH.toString());
            return;
        }
        if (worldPersonalGoal.getChallengeNumber() > 0) {
            // 需要花钱
            if (player.getGold() < staticActWorldBoss.getChallengeCost()) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                logger.error("AttackWorldBoss GameError {}", GameError.NOT_ENOUGH_GOLD.toString());
                return;
            }
            // 扣除元宝
            playerManager.subAward(player, AwardType.GOLD, 0, staticActWorldBoss.getChallengeCost(), Reason.REFRESH_MEETING_HERO_SOLDIERS);
        }
        worldPersonalGoal.setChallengeNumber(worldPersonalGoal.getChallengeNumber() + 1);
        worldPersonalGoal.setLastAttackBossTime(System.currentTimeMillis());

        Team playerTeam = battleMgr.initPvePlayerTeam(player, heroList, BattleEntityType.HERO);
        int bossSodier = getBossSoldier(worldBoss);
        Team monsterTeam = battleMgr.initWorldMonsterTeam(worldBoss.getMonsterId(), bossSodier);

        CommonPb.FightBefore.Builder fightBefore = CommonPb.FightBefore.newBuilder();
        // 玩家
        ArrayList<BattleEntity> playerEntities = playerTeam.getAllEnities();
        for (BattleEntity battleEntity : playerEntities) {
            fightBefore.addLeftEntities(battleEntity.wrapPb());
        }

        // 野怪
        ArrayList<BattleEntity> monsterEntities = monsterTeam.getAllEnities();
        for (BattleEntity battleEntity : monsterEntities) {
            fightBefore.addRightEntities(battleEntity.wrapPb());
        }

        // 随机seed不用存盘，没有回放, 种子需要发送到客户端
        Random rand = new Random(System.currentTimeMillis());
        // seed 开始战斗
        battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);

        // 战中信息
        CommonPb.FightIn.Builder fightIn = CommonPb.FightIn.newBuilder();
        // 玩家
        ArrayList<AttackInfo> playerAttackInfos = playerTeam.getAttackInfos();
        for (AttackInfo attackInfo : playerAttackInfos) {
            fightIn.addLeftInfo(attackInfo.wrapPb());
        }

        // 野怪
        ArrayList<AttackInfo> monsterAttackInfos = monsterTeam.getAttackInfos();
        for (AttackInfo attackInfo : monsterAttackInfos) {
            fightIn.addRightInfo(attackInfo.wrapPb());
        }
        // 同步boss血量给客户端
        // 给主公经验值, exp = 伤害 / 15
        List<BattleEntity> battleEntity = monsterTeam.getAllEnities();
        if (battleEntity == null || battleEntity.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("battleEntity is null");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        int currentSoldier = battleEntity.get(0).getCurSoldierNum();
        int diff = bossSodier - currentSoldier;
        diff = Math.max(0, diff);
        // int exp = (int) ((double) diff / (double) (staticLimitMgr.getNum(159)));
        // exp = Math.min(staticLimitMgr.getNum(160), exp);
        // exp = Math.max(0, exp);
        int exp = staticActWorldBoss.getAwardPlayerExp();
        lordManager.addExp(player, exp, Reason.KILL_WORLD_BOSS);
        updateBossSoldier(worldBoss, currentSoldier);
        // 说明已经击杀 这里是 突破
//            if (worldBoss.getSoldier() <= 0) {
//
//            }
        worldTargetTask.setNum(worldTargetTask.getNum() + 1);
        // 记录玩家在世界进程中杀的怪
        int killNum = playerTeam.getKillNum();
        WorldHitRank worldHitRank = worldTargetTask.getHitRank().computeIfAbsent(player.roleId, x -> new WorldHitRank(player, 0));
        worldHitRank.addHit(killNum);
        worldTargetTask.rank();
        // 此处给玩家奖励并且加军工
        Award award = staticActWorldBoss.getAward();
        if (award != null) {
            playerManager.addAward(player, award, Reason.WORLD_RANK_AWARD);
        }
        playerManager.addAward(player, AwardType.LORD_PROPERTY, LordPropertyType.HONOR, killNum * 2, Reason.WORLD_BODD_HOBOR);// 杀敌1:2给军工
        // 跟新国家进度（按伤害排行）
        worldTargetTask.updateCountryProcess(player, worldTargetTask, killNum);
        WorldPb.KillWorldBossRs.Builder builder = WorldPb.KillWorldBossRs.newBuilder();
        builder.setBossId(worldBoss.getMonsterId());
        builder.setBossSoldier(currentSoldier);
        builder.setLordExp(player.getExp());
        builder.setLordLevel(player.getLevel());
        builder.setFightBefore(fightBefore);
        builder.setFightIn(fightIn);
        builder.setIsWin(playerTeam.isWin());
        builder.setAwardExp(exp);
        builder.setChallengeNumber(worldPersonalGoal.getChallengeNumber());
        builder.setGold(player.getGold());
        builder.setTaskId(worldTargetTask.getTaskId());
        if (award != null) {
            builder.addAward(CommonPb.Award.newBuilder().setType(award.getType()).setId(award.getId()).setCount(award.getCount()));
        }
        builder.addAward(CommonPb.Award.newBuilder().setType(AwardType.LORD_PROPERTY).setId(LordPropertyType.HONOR).setCount(killNum * 2));
        builder.setRankInfo(worldTargetTask.getWorldHitRankInfo(player));
        handler.sendMsgToPlayer(WorldPb.KillWorldBossRs.ext, builder.build());
        // 判断是否开启下一个世界目标
        if (worldBoss.getSoldier() <= 0) {
            // 发放世界目标排行奖励
            worldTargetTaskService.sendRankAward(worldTargetTask, staticWorldNewTarget);
            // 开启下一个世界进程
            worldTargetTaskService.openWorldTarget(staticWorldNewTarget.getNextId());
            WorldPb.SynWorldBossClose.Builder close = WorldPb.SynWorldBossClose.newBuilder();
            for (Player player1 : playerManager.getPlayers().values()) {
                if (player1 == null) {
                    continue;
                }
                // worldTargetTaskService.synUpdateWorldTargetTask(player1);
                if (player1 != player) {
                    SynHelper.synMsgToPlayer(player1, WorldPb.SynWorldBossClose.EXT_FIELD_NUMBER, WorldPb.SynWorldBossClose.ext, close.build());
                }
            }
        }
    }

    public int getBossSoldier(WorldBoss worldBoss) {
        return worldBoss.getSoldier();
    }

    public void updateBossSoldier(WorldBoss worldBoss, int soldier) {
        worldBoss.setSoldier(soldier);
    }

    // 击杀流寇, 城池，世界boss:每个条件不一样
    public void getWorldTargerAward(WorldPb.GetWorldTargerAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int targetId = req.getTargetId();
        WorldTarget worldTarget = worldManager.getWorldTargetById(player, targetId);
        if (worldTarget == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_WORLD_TARGET);
            return;
        }

        int targetCountry = worldTarget.getCountry();
        StaticWorldTarget staticWorldTarget = staticWorldMgr.getStaticWorldTarget(targetId);
        if (staticWorldTarget == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        // 检查玩家是否已经领取
        Map<Integer, WorldTargetAward> worldTargetAwardMap = player.getWorldTargetAwardMap();
        WorldTargetAward worldTargetAward = worldTargetAwardMap.get(targetId);
        if (worldTargetAward == null) {
            worldTargetAward = new WorldTargetAward();
            worldTargetAward.setStatus(0);
            worldTargetAward.setTargetId(targetId);
            worldTargetAwardMap.put(targetId, worldTargetAward);
        }

        if (worldTargetAward.getTargetId() != targetId) {
            worldTargetAward.setTargetId(targetId);
        }

        if (worldTargetAward.getStatus() == 2) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_AWARD_HAD_TAKEN);
            return;
        }

        List<List<Integer>> config = staticWorldTarget.getAward();
        int awardTimes = 1;
        int country = player.getCountry();
        // 攻城战有双倍
        if (staticWorldTarget.getType() == 2 && targetCountry == country) {
            awardTimes = 2;
        }

        List<Award> awards = getAwards(config, awardTimes);
        playerManager.addAward(player, awards, Reason.WORLD_TARGET);
        GetWorldTargerAwardRs.Builder builder = GetWorldTargerAwardRs.newBuilder();
        builder.setTargetId(targetId);
        worldTargetAward.setStatus(2);
        builder.setStatus(2);
        // 发送奖励
        if (targetId == WorldTargetType.KILL_MONSTER) {
            player.setWorldKillMonsterStatus(2);
            worldTargetAward.setStatus(2);
        } else {
            worldTargetAward.setStatus(2);
        }

        for (Award award : awards) {
            builder.addAward(award.wrapPb());
        }

        // 开启下一个任务
        int nextTargetId = staticWorldTarget.getNextId();
        StaticWorldTarget nextTargetConfig = staticWorldMgr.getStaticWorldTarget(nextTargetId);
        if (nextTargetConfig != null) {
            List<Integer> res = worldManager.getTargetRes(nextTargetId, player);
            if (nextTargetId == 5 && res.get(0) == 1) {
                nextTargetId = nextTargetId + 1; // 取下一个
            }

            WorldTargetAward nextTarget = worldTargetAwardMap.get(nextTargetId);
            if (nextTarget == null) {
                nextTarget = new WorldTargetAward();
                nextTarget.setStatus(res.get(0));
                nextTarget.setCountry(res.get(1));
                nextTarget.setTargetId(nextTargetId);
                worldTargetAwardMap.put(nextTargetId, nextTarget);
            } else {
                nextTarget.setStatus(res.get(0));
                nextTarget.setCountry(res.get(1));
                nextTarget.setTargetId(nextTargetId);
            }

            //
            if (nextTarget.getTargetId() == 9 && res.get(0) == 1) {
                // pass, no next
            } else {
                WorldTarget data = new WorldTarget();
                data.setStatus(nextTarget.getStatus());
                data.setTargetId(nextTargetId);
                data.setCountry(nextTarget.getCountry());
                builder.setNextTarget(data.wrapPb());
            }
        }

        handler.sendMsgToPlayer(GetWorldTargerAwardRs.ext, builder.build());
    }

    public List<Award> getAwards(List<List<Integer>> config, int awardTimes) {
        List<Award> awards = new ArrayList<Award>();
        for (List<Integer> elem : config) {
            if (elem == null || elem.size() != 3) {
                continue;
            }
            awards.add(new Award(0, elem.get(0), elem.get(1), elem.get(2) * awardTimes));
        }

        return awards;
    }

    public void getSeasonRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        WorldData worldData = worldManager.getWolrdInfo();
        GetSeasonRs.Builder builder = GetSeasonRs.newBuilder();
        CommonPb.SeasonInfo.Builder data = CommonPb.SeasonInfo.newBuilder();
        data.setSeasonId(worldData.getSeason());
        data.setEndTime(worldData.getSeasonEndTime());
        data.setEffectId(worldData.getEffect());
        builder.setInfo(data);
        handler.sendMsgToPlayer(GetSeasonRs.ext, builder.build());
    }

    public void getCityAwardRq(WorldPb.GetCityAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        if (!req.hasCityId()) {
            handler.sendErrorMsgToPlayer(GameError.NO_CITY);
            return;
        }

        int cityId = req.getCityId();
        City city = cityManager.getCity(cityId);
        if (city == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CITY);
            return;
        }
        if (city.getCountry() == 0) {
            handler.sendErrorMsgToPlayer(GameError.CITY_COUNTRY_ERROR);
            return;
        }

        int country = player.getCountry();
        if (country != city.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.CITY_COUNTRY_ERROR);
            return;
        }

        StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
        if (staticWorldCity == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int cityType = staticWorldCity.getType();
        if (cityType == 8 || cityType == 9) {
            handler.sendErrorMsgToPlayer(GameError.CITY_TYPE_ERROR);
            return;
        }

        // 不能跨区域征收
        int mapId = worldManager.getMapId(player);
        if (staticWorldCity.getMapId() != mapId) {
            handler.sendErrorMsgToPlayer(GameError.CAN_NOT_COLLECT_PAPER);
            return;
        }

        StaticWorldMap staticMap = staticWorldMgr.getStaticWorldMap(mapId);
        if (staticMap == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        long honor = player.getHonor();
        long cost = staticWorldCity.getHonor();
        if (honor < cost) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_HONOR);
            return;
        }

        // 检查次数
        if (getAwardNum(city) <= 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_CITY_AWARD_NUM);
            return;
        }

        // 检查是否有强征令牌
        int times = 0;
        int itemId = getItemId(cityType);
        Item item = null;
        if (itemId == -1) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        } else {
            item = player.getItem(itemId);
            if (item != null && item.getItemNum() > 0) {
                times = 1;
            }
        }

        Award award = cityManager.getCityAward(city.getCityId());
        if (award == null || !award.isOk()) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        playerManager.subAward(player, AwardType.LORD_PROPERTY, LordPropertyType.HONOR, cost, Reason.GET_CITY_AWARD);
        removeAwardNum(city);
        if (times == 1) {
            award.setCount(award.getCount() * 2);
        }

        if (itemId != -1 && item != null && item.getItemNum() > 0) {
            playerManager.subAward(player, AwardType.PROP, itemId, 1, Reason.GET_CITY_AWARD);
        }

        playerManager.addAward(player, award, Reason.GET_CITY_AWARD);
        activityManager.updActWorldBattle(player, ActivityConst.TYPE_ADD, ActWorldBattleConst.COLLECT_PAPER, 0, 1);
        WorldPb.GetCityAwardRs.Builder builder = WorldPb.GetCityAwardRs.newBuilder();
        builder.setHonor(player.getHonor());
        builder.setAward(award.wrapPb());
        Item next = player.getItem(itemId);
        if (next != null) {
            builder.setProp(next.wrapPb());
        }

        builder.setAwardTimes(city.getAwardNum());
        // long period = getMakePeriod(city.getCityId());
        long period = cityManager.getMakePeriod(cityId);
        long now = System.currentTimeMillis();
        if (city.getAwardNum() <= 0) {
            if (city.getMakeItemTime() < now) {
                city.setMakeItemTime(System.currentTimeMillis() + period);
            }
            builder.setAwardEndTime(city.getMakeItemTime());
        } else {
            builder.setAwardEndTime(0);
        }

        builder.setAwardPeriod(period);
        handler.sendMsgToPlayer(WorldPb.GetCityAwardRs.ext, builder.build());

        SystemChat sysChat;
        if (times == 1) {
            String[] params = {player.getNick(), String.valueOf(staticMap.getMapId()), String.valueOf(staticWorldCity.getCityId()), String.valueOf(award.getId()), String.valueOf(itemId)};
            sysChat = chatManager.createSysChat(ChatId.COLLECTION_CHART_2, params);
        } else {
            String[] params = {player.getNick(), String.valueOf(staticMap.getMapId()), String.valueOf(staticWorldCity.getCityId()), String.valueOf(award.getId())};
            sysChat = chatManager.createSysChat(ChatId.COLLECTION_CHART, params);
        }
        // 通知在线玩家这个城市的状态
        WorldPb.SynMapCityRq.Builder builder1 = WorldPb.SynMapCityRq.newBuilder();
        builder1.setInfo(worldManager.createCityOwner(city));
        WorldPb.SynMapCityRq msg = builder1.build();
        SpringUtil.getBean(LoginExecutor.class).add(() -> {
            chatManager.sendCountryShare(player, sysChat);
            playerManager.getOnlinePlayer().forEach(e -> {
                playerManager.synMapCityRq(e, msg);
            });
        });
    }

    public long getMakePeriod(int cityId) {
        StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
        if (staticWorldCity == null) {
            LogHelper.CONFIG_LOGGER.info("staticWorldCity is null!");
            return 3600000L;
        }
        int period = staticWorldCity.getPeriod();
        return period * TimeHelper.SECOND_MS;
    }

    // 获取当前城池对应的令牌Id
    public int getItemId(int cityType) {
        switch (cityType) {
            case CityType.POINT:
                return 115;
            case CityType.TOWN:
                return 116;
            case CityType.WALL:
                return 117;
            case CityType.CAMP:
                return 118;
            case CityType.CITY:
                return 119;
            case CityType.CAPITAL:
                return 120;
            case CityType.FAMOUS_CITY:
                return 121;
        }

        return -1;
    }

    public int getAwardNum(City city) {
        return city.getAwardNum();
    }

    public synchronized void removeAwardNum(City city) {
        city.setAwardNum((byte) (city.getAwardNum() - 1));
    }

    public void scoutRq(WorldPb.ScoutRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int type = req.getScoutType();
        if (type < 1 || type > 3) {
            handler.sendErrorMsgToPlayer(GameError.SCOUT_TYPE_ERRPR);
            return;
        }

        long targetId = req.getLordId();
        Player target = playerManager.getPlayer(targetId);
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_TARGET);
            return;
        }

        StaticScout staticScout = scoutMgr.getScout(type);
        if (staticScout == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            LogHelper.CONFIG_LOGGER.info("staticScout is null, type = " + type);
            return;
        }

        int scoutLevel = techManager.getTechLevel(player, TechType.SCOUT);
        int targetScoutLevel = techManager.getTechLevel(target, TechType.SCOUT);
        int delta = scoutLevel - targetScoutLevel + staticScout.getAddLevel();
        int levelDelta = player.getCommandLv() - target.getCommandLv();
        StaticScoutLv staticScoutLv = scoutMgr.getScoutLv(levelDelta);
        if (staticScoutLv == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            LogHelper.CONFIG_LOGGER.info("staticScoutLv is null, delta Level = " + levelDelta);
            return;
        }

        long ironCost = 0;
        int goldCost = 0;
        if (type == 1) {
            ironCost = staticScoutLv.getPrimaryIron();
            if (player.getIron() < ironCost) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_IRON);
                return;
            }

        } else if (type == 2) {
            ironCost = staticScoutLv.getMiddleIron();
            if (player.getIron() < ironCost) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_IRON);
                return;
            }

        } else if (type == 3) {
            goldCost = staticLimitMgr.getNum(44);
            if (player.getGold() < goldCost) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
        }

        // 扣除资源
        if (type == 1 || type == 2) {
            playerManager.subIron(player, ironCost, Reason.SCOUT);
        } else {
            playerManager.subGoldOk(player, goldCost, Reason.SCOUT);
        }

        // 计算侦察成功率
        int scoutRate = staticScout.getScoutRate() + delta * 20;
        scoutRate = Math.min(100, scoutRate);
        scoutRate = Math.max(staticLimitMgr.getNum(56), scoutRate);
        int scoutRandNum = RandomHelper.threadSafeRand(1, 100);
        if (scoutRandNum >= scoutRate) {
            handlerScoutFail(player, target, handler); // 侦察失败
            battleMailMgr.sendScotTargetFailed(player, target);
            return;
        }

        // 计算显示成功率
        // 计算中级显示率
        int showSoldier = staticScout.getShowSoldierRate() + delta * 10;
        showSoldier = Math.min(100, showSoldier);
        showSoldier = Math.max(0, showSoldier);
        int soldierRandNum = RandomHelper.threadSafeRand(1, 100);
        if (soldierRandNum > showSoldier) { // 侦察成功
            handleScoutRes(player, target, handler);
            battleMailMgr.sendScotMain(player, target); // 发给被侦察的人
            return;
        }

        // 计算英雄显示率
        int showHero = staticScout.getShowHeroRate() + delta * 10;
        showHero = Math.min(100, showHero);
        showHero = Math.max(0, showHero);
        int heroRandNum = RandomHelper.threadSafeRand(1, 100);
        if (heroRandNum > showHero) {
            handleScoutSoldier(player, target, handler);
            battleMailMgr.sendScotMain(player, target); // 发给被侦察的人
            return;
        }

        handlerScoutAll(player, target, handler);
        battleMailMgr.sendScotMain(player, target); // 发给被侦察的人
    }

    // 1.发送侦察失败的邮件, 增加侦察成功率
    public void handlerScoutFail(Player player, Player target, ClientHandler handler) {
        WorldPb.ScoutRs.Builder builder = WorldPb.ScoutRs.newBuilder();
        builder.setResource(player.wrapResourcePb());
        builder.setGold(player.getGold());
        // 侦查[%s]%s失败，请主公选择其他侦查方式，或提高侦查科技等级，提高侦查的成功率
        String name = target.getNick();
        String pos = String.format("%s,%s", target.getPosX(), target.getPosY());
        Mail mail = playerManager.addNormalMail(player, MailId.SCOT_FAILED, name, pos);
        if (mail != null) {
            mail.setState(1);
            builder.setMail(mail.serDefault());
        }

        handler.sendMsgToPlayer(WorldPb.ScoutRs.ext, builder.build());
    }

    public String[] getBase(Player target) {
        List<String> baseInfo = new ArrayList<String>();
        String country = String.valueOf(target.getCountry());
        String level = String.valueOf(target.getLevel());
        String name = target.getNick();
        String pos = String.format("%s,%s", target.getPosX(), target.getPosY());
        String people = String.valueOf(target.getPeople());
        String iron = String.valueOf(target.getIron());
        String copper = String.valueOf(target.getCopper());
        String oil = String.valueOf(target.getOil());
        String stone = String.valueOf(target.getStone());
        baseInfo.add(country);
        baseInfo.add(level);
        baseInfo.add(name);
        baseInfo.add(pos);
        baseInfo.add(people);
        baseInfo.add(iron);
        baseInfo.add(copper);
        baseInfo.add(oil);
        baseInfo.add(stone);
        String[] res = new String[baseInfo.size()];
        for (int i = 0; i < baseInfo.size(); i++) {
            res[i] = baseInfo.get(i);
        }
        return res;
    }

    public String[] getSoldier(Player target) {
        List<String> soldier = new ArrayList<String>();
        String wallLv = String.valueOf(target.getWallLv());
        String battlScore = String.valueOf(target.getBattleScore());
        String rocket = String.valueOf(soldierManager.getSoldierNum(target, SoldierType.ROCKET_TYPE));
        String tank = String.valueOf(soldierManager.getSoldierNum(target, SoldierType.TANK_TYPE));
        String warCar = String.valueOf(soldierManager.getSoldierNum(target, SoldierType.WAR_CAR));
        soldier.add(wallLv);
        soldier.add(battlScore);
        soldier.add(rocket);
        soldier.add(tank);
        soldier.add(warCar);
        String[] res = new String[soldier.size()];
        for (int i = 0; i < soldier.size(); i++) {
            res[i] = soldier.get(i);
        }
        return res;
    }

    // 2.显示资源, 兵力失败,武将失败, 增加显示成功率, 清除侦察成功率
    public void handleScoutRes(Player player, Player target, ClientHandler handler) {
        WorldPb.ScoutRs.Builder builder = WorldPb.ScoutRs.newBuilder();
        builder.setResource(player.wrapResourcePb());
        builder.setGold(player.getGold());
        // 侦察目标:[%s]Lv.%s %s [%s]城市人口x%s银币x%s木材x%s粮草x%s镔铁x%s
        // 基地信息/n本次侦察未能查到其他信息，可尝试再次侦察，提高侦察等级或加大侦察力度有机会获得更多信息
        String[] baseInfo = getBase(target);
        Mail mail = playerManager.addNormalMail(player, MailId.SCOT_2, baseInfo);
        if (mail != null) {
            mail.setState(1);
            builder.setMail(mail.serDefault());
        }
        handler.sendMsgToPlayer(WorldPb.ScoutRs.ext, builder.build());
    }

    // 3.显示资源, 兵力成功,武将失败, 增加显示成功率, 清除侦察成功率
    public void handleScoutSoldier(Player player, Player target, ClientHandler handler) {
        WorldPb.ScoutRs.Builder builder = WorldPb.ScoutRs.newBuilder();
        builder.setResource(player.wrapResourcePb());
        builder.setGold(player.getGold());
        // 侦察目标:[%s]Lv.%s %s [%s]城市人口x%s银币x%s木材x%s粮草x%s镔铁x%s
        // 城池信息城墙等级：LV.%s 战斗力：%s步兵X%s 骑兵X%s 弓兵X%s
        // 将领信息：/n本次侦察未能查到将领驻防信息，可尝试再次侦察，提高侦察等级或加大侦察力度有机会获得更多信息

        String[] baseInfo = getBase(target);
        String[] soldier = getSoldier(target);
        String[] both = ArrayUtils.addAll(baseInfo, soldier);
        Mail mail = playerManager.addNormalMail(player, MailId.SCOT_3, both);
        if (mail != null) {
            mail.setState(1);
            builder.setMail(mail.serDefault());
        }

        handler.sendMsgToPlayer(WorldPb.ScoutRs.ext, builder.build());
    }

    public List<CommonPb.HeroScot> getHeroInfo(Player target) {
        List<CommonPb.HeroScot> heroScots = new ArrayList<CommonPb.HeroScot>();
        for (Integer heroId : target.getEmbattleList()) {
            Hero hero = target.getHero(heroId);
            if (hero == null) {
                continue;
            }
            CommonPb.HeroScot.Builder heroScot = CommonPb.HeroScot.newBuilder();
            heroScot.setHeroId(heroId);
            heroScot.setHeroLv(hero.getHeroLv());
            heroScot.setSoldier(hero.getCurrentSoliderNum());
            heroScot.setPlayerName(target.getNick());
            int state = target.getHeroState(heroId);
            if (state == 0) {
                heroScot.setState(8);
            } else {
                heroScot.setState(state);
            }

            March march = target.getHeroMarch(heroId);
            if (state == MarchState.CityAssist) {
                if (march != null) {
                    long assistId = march.getAssistId();
                    Player targetPlayer = playerManager.getPlayer(assistId);
                    if (targetPlayer != null) {
                        heroScot.setTargetPlayerName(targetPlayer.getNick());
                    }
                } /*
                 * else { heroScot.setState(9); }
                 */
            }

            if (march != null) {
                long period = march.getPeriod();
                long minutes = period / TimeHelper.MINUTE_MS;
                long mod = period % TimeHelper.MINUTE_MS;
                if (mod > 0) {
                    minutes += 1;
                }
                heroScot.setPeriod((int) minutes);
            }

            if (march != null) {
                Pos pos = march.getEndPos();
                Resource resource = worldManager.getResource(pos, target);
                if (resource != null) {
                    heroScot.setResourceId((int) resource.getId());
                } /*
                 * else { heroScot.setState(9); }
                 */
            }

            heroScots.add(heroScot.build());
        }

        return heroScots;
    }

    // 4.全部显示, 清空显示成功率和侦察成功率
    // 需要放英雄信息
    public void handlerScoutAll(Player player, Player target, ClientHandler handler) {
        WorldPb.ScoutRs.Builder builder = WorldPb.ScoutRs.newBuilder();
        builder.setResource(player.wrapResourcePb());
        builder.setGold(player.getGold());
        // 侦察目标:[%s]Lv.%s %s [%s]城市人口x%s银币x%s木材x%s粮草x%s镔铁x%s
        // 城池信息城墙等级：LV.%s 战斗力：%s步兵X%s 骑兵X%s 弓兵X%s
        // 武将信息%s （%s ）兵力%s
        String[] baseInfo = getBase(target);
        String[] soldier = getSoldier(target);
        String[] both = ArrayUtils.addAll(baseInfo, soldier);
        List<CommonPb.HeroScot> heroInfo = getHeroInfo(target);
        if (heroInfo != null && heroInfo.size() > 0) {
            Mail mail = playerManager.addNormalMail(player, MailId.SCOT_4, both);
            if (mail != null) {
                mail.setState(1);
                mail.setHeroScots(heroInfo);
                builder.setMail(mail.serDefault());
            }
        } else {
            Mail mail = playerManager.addNormalMail(player, MailId.SCOT_4, both);
            if (mail != null) {
                mail.setState(1);
                builder.setMail(mail.serDefault());
            }
        }

        handler.sendMsgToPlayer(WorldPb.ScoutRs.ext, builder.build());
    }

    // 补兵
    public void addSoldierRq(WorldPb.AddSoldierRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 行军英雄
        Integer heroId = req.getHeroId();
        Map<Integer, Hero> heroMap = player.getHeros();
        // 检查英雄是否上阵
        if (!isEmbattle(player, heroId)) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
            return;
        }

        Hero hero = heroMap.get(heroId);
        if (hero == null) {
            handler.sendErrorMsgToPlayer(GameError.HERO_NULL);
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

        // 给英雄补兵
        StaticHero staticHero = staticHeroMgr.getStaticHero(heroId);
        if (staticHero == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        int soldierType = staticHero.getSoldierType();
        // 应该增加的英雄的兵力
        heroManager.caculateProp(hero, player);
        // 获取当前的兵力
        int currentSoldier = soldierManager.getSoldierNum(player, soldierType);
        if (currentSoldier <= 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
            return;
        }
        int addSoldierNum = hero.getSoldierNum() - hero.getCurrentSoliderNum();
        int diff = Math.min(currentSoldier, addSoldierNum);
        if (diff <= 0) {
            handler.sendErrorMsgToPlayer(GameError.SOLDIER_FULL);
            return;
        }

        playerManager.subAward(player, AwardType.SOLDIER, soldierType, diff, Reason.ADD_SOLDIER);
        hero.setCurrentSoliderNum(hero.getCurrentSoliderNum() + diff);

        if (hero.getCurrentSoliderNum() > hero.getSoldierNum()) {
            hero.setCurrentSoliderNum(hero.getSoldierNum());
            LoggerFactory.getLogger(getClass()).error("hero.getCurrentSoliderNum() = " + hero.getCurrentSoliderNum() + ", hero.getSoldierNum() = " + hero.getSoldierNum());
        }

        WorldPb.AddSoldierRs.Builder builder = WorldPb.AddSoldierRs.newBuilder();
        CommonPb.HeroSoldier.Builder heroSoldier = CommonPb.HeroSoldier.newBuilder();
        heroSoldier.setHeroId(heroId);
        heroSoldier.setSoldier(hero.getCurrentSoliderNum());
        builder.setHeroSoldier(heroSoldier);

        CommonPb.Soldier.Builder soldierPb = CommonPb.Soldier.newBuilder();
        soldierPb.setNum(soldierManager.getSoldierNum(player, soldierType));
        soldierPb.setSoldierType(soldierType);
        builder.setSoldier(soldierPb);

        handler.sendMsgToPlayer(WorldPb.AddSoldierRs.ext, builder.build());
    }

    // 随机迁城
    public void mapMoveRq(WorldPb.MapMoveRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        ConcurrentLinkedDeque<March> marches = player.getMarchList();
        if (!marches.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
            return;
        }

        // 迁城
        int type = req.getType();
        if (type == 1) {
            mapMoveByItem(req, handler);
        } else if (type == 2) {
            mapMoveByGold(req, handler);
        } else if (type == 3) {
            mapMoveByFlameItem(req, handler);
        }
    }

    public void mapMoveByItem(WorldPb.MapMoveRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int itemId = req.getPropId();
        Item item = player.getItem(itemId);
        if (item == null) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }

        if (item.getItemNum() < 1) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }

        if (itemId == 30) { // 初级
            handlePrimaryMove(req, handler, 0);
        } else if (itemId == 31) { // 中级
            handleMiddleMove(req, handler, 0);
        } else if (itemId == 32) { // 高级
            handleHighMove(req, handler, 0);
        } else if (itemId == 233) {
            handleTopHighMove(req, handler);
        } else {
            handler.sendErrorMsgToPlayer(GameError.ITEM_ID_NOT_EXISTS);
            return;
        }
    }

    /**
     * 战火燎原内使用道具逻辑
     *
     * @param req
     * @param handler
     */
    public void mapMoveByFlameItem(WorldPb.MapMoveRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
        WorldData worldData = worldManager.getWolrdInfo();
        if (worldData == null) {
            return;
        }
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
        if (worldActPlan == null || worldActPlan.getState() != WorldActPlanConsts.OPEN) {
            return;
        }
        int itemId = req.getPropId();
        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
        Map<Integer, Item> prop = flamePlayer.getProp();
        Item item = prop.get(itemId);
        if (item == null) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }

        if (item.getItemNum() < 1) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }
        if (itemId == 31) { // 中级
            handleMiddleFLameMove(req, handler, flamePlayer);
        } else if (itemId == 32) { // 高级
            handleHighFlameMove(req, handler, flamePlayer);
        } else {
            handler.sendErrorMsgToPlayer(GameError.ITEM_ID_NOT_EXISTS);
            return;
        }
    }

    public void mapMoveByGold(WorldPb.MapMoveRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int itemId = req.getPropId();
        StaticProp staticProp = staticPropDataMgr.getStaticProp(itemId);
        if (staticProp == null) {
            return;
        }

        int price = staticProp.getPrice();
        if (player.getGold() < price) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        if (itemId == 30) { // 初级
            handlePrimaryMove(req, handler, price);
        } else if (itemId == 31) { // 中级
            handleMiddleMove(req, handler, price);
        } else if (itemId == 32) { // 高级
            handleHighMove(req, handler, price);
        } else {
            handler.sendErrorMsgToPlayer(GameError.ITEM_ID_NOT_EXISTS);
            return;
        }
    }

    // 随机迁城
    public void handlePrimaryMove(WorldPb.MapMoveRq req, ClientHandler handler, int price) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        Map<Integer, MapStatus> mapStatuses = player.getMapStatusMap();
        List<MapStatus> openMap = new ArrayList<MapStatus>();
        for (MapStatus mapStatus : mapStatuses.values()) {
            if (mapStatus.getStatus() == 2) {
                openMap.add(mapStatus);
            }
        }

        int currentMapId = worldManager.getMapId(player);
        int mapType = worldManager.getMapAreaType(currentMapId);
        StaticMapMove staticMapMove = staticWorldMgr.getStaticMapMove(mapType);
        if (staticMapMove == null) {
            LogHelper.CONFIG_LOGGER.info("player mapId is error, mapType = " + mapType);
            return;
        }

        // 初始化掉落的内容
        List<Integer> randPosRate = new ArrayList<Integer>();
        if (mapType == 1) { // up1, up2
            randPosRate.add(staticMapMove.getCurrentMap());
            randPosRate.add(staticMapMove.getUp1());
            randPosRate.add(staticMapMove.getUp2());
            randPosRate.add(staticMapMove.getOtherSameMap());

        } else if (mapType == 2) { // down1, up1
            randPosRate.add(staticMapMove.getCurrentMap());
            randPosRate.add(staticMapMove.getDown1());
            randPosRate.add(staticMapMove.getUp1());
            randPosRate.add(staticMapMove.getOtherSameMap());
        } else if (mapType == 3) { // down1, down2
            randPosRate.add(staticMapMove.getCurrentMap());
            randPosRate.add(staticMapMove.getDown1());
            randPosRate.add(staticMapMove.getDown2());
            randPosRate.add(staticMapMove.getOtherSameMap());
        }

        // 判断当前地图实际开启的掉落
        int maxMapType = 1;
        for (MapStatus status : openMap) {
            int elem = worldManager.getMapAreaType(status.getMapId());
            if (maxMapType < elem) {
                maxMapType = elem;
            }
        }

        if (maxMapType == 1) {
            randPosRate.set(1, 0);
            randPosRate.set(2, 0);
        } else if (maxMapType == 2) {
            randPosRate.set(2, 0);
        }

        // 检查有没有同类型的,如果没有设置为0
        boolean hasSame = false;
        for (MapStatus status : openMap) {
            int elem = worldManager.getMapAreaType(status.getMapId());
            if (elem == mapType && status.getMapId() != currentMapId) {
                hasSame = true;
                break;
            }
        }

        if (!hasSame) {
            randPosRate.set(3, 0);
        }

        int totalRate = 0;
        for (Integer rate : randPosRate) {
            totalRate += rate;
        }

        int randNum = RandomHelper.threadSafeRand(1, totalRate);
        int moveId = 0;
        int checkNum = 0;
        for (int i = 0; i < randPosRate.size(); i++) {
            checkNum += randPosRate.get(i);
            if (randNum <= checkNum) {
                moveId = i;
                break;
            }
        }

        Pos pos = null;
        if (mapType == 1) {
            if (moveId == 0) { // 当前地图
                pos = worldManager.randByMapId(player, currentMapId);
            } else if (moveId == 1) { // 升1级
                pos = worldManager.randTargetPosMap(player, mapType + 1, openMap);
            } else if (moveId == 2) { // 升2级
                pos = worldManager.randTargetPosMap(player, mapType + 2, openMap);
            } else { // 兄弟
                pos = worldManager.randBrotherPosMap(player, mapType, openMap);
            }
        } else if (mapType == 2) {
            if (moveId == 0) { // 当前
                pos = worldManager.randByMapId(player, currentMapId);
            } else if (moveId == 1) { // 降1级
                pos = worldManager.randTargetPosMap(player, mapType - 1, openMap);
            } else if (moveId == 2) { // 升1级
                pos = worldManager.randTargetPosMap(player, mapType + 1, openMap);
            } else { // 兄弟
                pos = worldManager.randBrotherPosMap(player, mapType, openMap);
            }
        } else if (mapType == 3) {
            if (moveId == 0) { // 当前
                pos = worldManager.randByMapId(player, currentMapId);
            } else if (moveId == 1) { // 降1级
                pos = worldManager.randTargetPosMap(player, mapType - 1, openMap);
            } else if (moveId == 2) { // 降2级
                pos = worldManager.randTargetPosMap(player, mapType - 2, openMap);
            }
        }

        if (pos.isError()) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        // 去掉当前玩家的坐标
        MapInfo mapInfo = worldManager.getMapInfo(currentMapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        // 删除坐标
        // 更新playerCity
        Pos playerPos = player.getPos();
        worldManager.changePlayerPos(player, mapInfo, pos);

        WorldPb.MapMoveRs.Builder builder = WorldPb.MapMoveRs.newBuilder();

        playerManager.subGoldOk(player, price, Reason.MAP_MOVE);
        builder.setGold(player.getGold());
        builder.setPos(pos.wrapPb());
        playerManager.subAward(player, AwardType.PROP, req.getPropId(), 1L, Reason.MAP_MOVE);
        Item item = player.getItem(req.getPropId());
        if (item != null) {
            builder.setProp(item.wrapPb());
        }
        handler.sendMsgToPlayer(WorldPb.MapMoveRs.ext, builder.build());
        // 驻防武将回城
        worldManager.handleWallFriendReturn(player);
        // 先回城
        worldManager.removePlayerWar(player, playerPos, MarchReason.LowMove, pos);

    }

    public void handleMiddleMove(WorldPb.MapMoveRq req, ClientHandler handler, int price) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        // 指定mapId随机
        CommonPb.Pos pos = req.getPos();
        int mapId = worldManager.getMapId(pos);

        // 检查mapId的状态
        int isFound = player.isMapCanMoves(mapId);
        switch (isFound) {
            case 1:
                handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
                return;
            case 2:
                handler.sendErrorMsgToPlayer(GameError.CANT_MOVE);
                return;
            default:
                break;
        }
        if (price == 0) {
            int itemId = req.getPropId();
            playerManager.subAward(player, AwardType.PROP, itemId, 1L, Reason.MAP_MOVE);
        }
        FlameMap flameMap = flameWarManager.getFlameMap();
        // 可以迁城
        Pos playerPos = player.getPos();
        int currentMapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(currentMapId);
        PlayerCity playerCity = worldManager.removePlayerCity(playerPos, mapInfo);
        if (currentMapId == MapId.FIRE_MAP && playerCity != null) {
            flameMap.removeNode(playerCity);
        }
        MapInfo newMapInfo = worldManager.getMapInfo(mapId);
        Pos randPos = worldManager.givePlayerPos(newMapInfo);
        if (currentMapId == MapId.FIRE_MAP) {
            randPos = flameMap.getPos(0);
        }
        if (randPos.isError()) {
            return;
        }

        playerManager.changePlayerPos(player, randPos);
        PlayerCity playerCity1 = worldManager.addPlayerCity(randPos, newMapInfo, player);
        if (currentMapId == MapId.FIRE_MAP) {
            flameMap.addNode(playerCity1);
        }
        WorldPb.MapMoveRs.Builder builder = WorldPb.MapMoveRs.newBuilder();

        playerManager.subGoldOk(player, price, Reason.MAP_MOVE);
        builder.setGold(player.getGold());
        builder.setPos(randPos.wrapPb());
        Item item = player.getItem(req.getPropId());
        if (item != null) {
            builder.setProp(item.wrapPb());
        }
        handler.sendMsgToPlayer(WorldPb.MapMoveRs.ext, builder.build());
        // 驻防武将回城
        worldManager.handleWallFriendReturn(player);
        // 迁城之后战斗全部删除
        worldManager.removePlayerWar(player, playerPos, MarchReason.MiddleMove, randPos);
    }

    public void handleHighMove(WorldPb.MapMoveRq req, ClientHandler handler, int price) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        // 检查mapId的状态
        // 指定mapId随机
        CommonPb.Pos pos = req.getPos();
        int mapId = worldManager.getMapId(pos);

        int isFound = player.isMapCanMoves(mapId);
//        Map<Integer, MapStatus> mapStatuses = player.getMapStatusMap();
//        for (MapStatus status : mapStatuses.values()) {
//            if (status.getMapId() == mapId && status.getStatus() == 2) {
//                isFound = true;
//            }
//        }

        // bugs here
//        if (!isFound) {
//            handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
//            return;
//        }

        switch (isFound) {
            case 1:
                handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
                return;
            case 2:
                handler.sendErrorMsgToPlayer(GameError.CANT_MOVE);
                return;
            default:
                break;
        }

        // 检查位置是否被占用
        Pos targetPos = new Pos(pos.getX(), pos.getY());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        FlameMap flameMap = null;
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.info("mapInfo is null");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        if (mapId != MapId.FIRE_MAP) {
            if (!mapInfo.isFreePos(targetPos)) {
                handler.sendErrorMsgToPlayer(GameError.POS_IS_TAKEN);
                return;
            }
        } else {
            flameMap = (FlameMap) mapInfo;
            boolean flag = false;
            Map<Integer, List<Pos>> safePos = flameMap.getSafePos();
            Set<Map.Entry<Integer, List<Pos>>> entries = safePos.entrySet();
            for (Map.Entry<Integer, List<Pos>> entry : entries) {
                Integer key = entry.getKey();
                if (key != player.getCountry() && entry.getValue().contains(targetPos)) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                handler.sendErrorMsgToPlayer(GameError.FIRE_NOT_MOVE_SAFE);
                return;
            }
            Entity node = flameMap.getNode(targetPos);
            if (node != null) {
                handler.sendErrorMsgToPlayer(GameError.FIRE_NOT_MOVE_SAFE);
                return;
            }

        }
        if (price == 0) {
            int itemId = req.getPropId();
            playerManager.subAward(player, AwardType.PROP, itemId, 1L, Reason.MAP_MOVE);
        }
        // 可以迁城
        Pos playerPos = player.getPos();
        // LogHelper.GAME_DEBUG.error("playerPos = " + targetPos);

        // 当前玩家的地图
        int currentMapId = worldManager.getMapId(player);

        MapInfo currentMapInfo = worldManager.getMapInfo(currentMapId);
        PlayerCity playerCity = worldManager.removePlayerCity(playerPos, currentMapInfo);
        if (currentMapId == MapId.FIRE_MAP && playerCity != null) {
            flameMap.removeNode(playerCity);
        }
        playerManager.changePlayerPos(player, targetPos);

        PlayerCity playerCity1 = worldManager.addPlayerCity(targetPos, mapInfo, player);
        if (currentMapId == MapId.FIRE_MAP) {
            flameMap.addNode(playerCity1);
        }

        WorldPb.MapMoveRs.Builder builder = WorldPb.MapMoveRs.newBuilder();

        playerManager.subGoldOk(player, price, Reason.MAP_MOVE);
        builder.setGold(player.getGold());
        builder.setPos(targetPos.wrapPb());
        Item item = player.getItem(req.getPropId());
        if (item != null) {
            builder.setProp(item.wrapPb());
        }

        handler.sendMsgToPlayer(WorldPb.MapMoveRs.ext, builder.build());

        // 驻防武将回城
        worldManager.handleWallFriendReturn(player);
        // 先返回部队
        worldManager.removePlayerWar(player, playerPos, MarchReason.HighMove, targetPos);

        eventManager.highMove(player, Lists.newArrayList(currentMapId, mapId));
    }

    // 超级迁城,无视地图解锁规则
    public void handleTopHighMove(WorldPb.MapMoveRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        // 检查mapId的状态
        // 指定mapId随机
        CommonPb.Pos pos = req.getPos();
        int mapId = worldManager.getMapId(pos);

        // 检查位置是否被占用
        Pos targetPos = new Pos(pos.getX(), pos.getY());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.info("mapInfo is null");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        // 如果采集点有初级采集点,则将初级采集点重新随机位置
        Entity entity = mapInfo.getEntity(targetPos);

        if (!mapInfo.isFreePos(targetPos)) {
            handler.sendErrorMsgToPlayer(GameError.POS_IS_TAKEN);
            return;
        }
        // 可以迁城
        Pos playerPos = player.getPos();
        // LogHelper.GAME_DEBUG.error("playerPos = " + targetPos);

        // 当前玩家的地图
        int currentMapId = worldManager.getMapId(player);
        MapInfo currentMapInfo = worldManager.getMapInfo(currentMapId);
        worldManager.removePlayerCity(playerPos, currentMapInfo);
        playerManager.changePlayerPos(player, targetPos);
        worldManager.addPlayerCity(targetPos, mapInfo, player);
        WorldPb.MapMoveRs.Builder builder = WorldPb.MapMoveRs.newBuilder();
        builder.setGold(player.getGold());
        builder.setPos(targetPos.wrapPb());
        Item item = player.getItem(req.getPropId());
        if (item != null) {
            builder.setProp(item.wrapPb());
        }

        handler.sendMsgToPlayer(WorldPb.MapMoveRs.ext, builder.build());
        // 驻防武将回城
        worldManager.handleWallFriendReturn(player);
        // 先返回部队
        worldManager.removePlayerWar(player, playerPos, MarchReason.TopMiddleMove, targetPos);
        eventManager.highMove(player, Lists.newArrayList(currentMapId, worldManager.getMapId(targetPos)));
    }

    public void getMarchRq(WorldPb.GetMarchRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int mapId;
        if (req.hasMapId()) {
            mapId = req.getMapId();
        } else {
            mapId = playerManager.getMapId(player);
        }
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            WorldPb.GetMarchRs.Builder builder = WorldPb.GetMarchRs.newBuilder();
            handler.sendMsgToPlayer(WorldPb.GetMarchRs.ext, builder.build());
            return;
        }

        try {
            WorldPb.GetMarchRs.Builder builder = WorldPb.GetMarchRs.newBuilder();
            ConcurrentLinkedDeque<March> marches = mapInfo.getMarches();
            for (March march : marches) {
                if (march != null) {
                    if (march.getMarchType() == MarchType.ZERG_DEFEND_WAR) {
                        if (march.getCountry() == player.getCountry()) {
                            builder.addMarch(worldManager.wrapMarchPb(march));
                        }
                    } else {
                        builder.addMarch(worldManager.wrapMarchPb(march));
                    }
                }
            }

            // 玩家的行军发送给玩家
            ConcurrentLinkedDeque<March> playerMarches = player.getMarchList();
            for (March playerMarch : playerMarches) {
                if (playerMarch == null) {
                    continue;
                }

                // 过滤已经发送的邮件
                if (isInMarches(marches, playerMarch.getKeyId())) {
                    continue;
                }

                builder.addMarch(worldManager.wrapMarchPb(playerMarch));
            }
            // 只给自己的行军线
            if (player.getSimpleData() != null) {
                March march = player.getSimpleData().getRiotMarchs();
                if (march != null) {
                    builder.addMarch(worldManager.wrapMarchPb(march));
                }
            }

            handler.sendMsgToPlayer(WorldPb.GetMarchRs.ext, builder.build());
//			LogHelper.MESSAGE_LOGGER.info("GetMarchRs:{}", builder.build());
        } catch (Exception ex) {
            LogHelper.ERROR_LOGGER.error("thread may not safe cause：{}", ex.getMessage(), ex);
        }
    }

    public boolean isInMarches(ConcurrentLinkedDeque<March> marches, int keyId) {
        for (March march : marches) {
            if (keyId == march.getKeyId()) {
                return true;
            }
        }

        return false;
    }

    // 加速
    public void speedMarch(WorldPb.SpeedMarchRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (req.getType() == 1) {
            speedMarchByItem(req, handler);
        } else if (req.getType() == 2) {
            speedMarchByGold(req, handler);
        } else {
            handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
        }
    }

    public boolean isSpeedItem(int propId) {
        return propId == ItemId.HIGH_SPEED || propId == ItemId.MAX_SPEED;
    }

    public boolean isMarchCancelItem(int propId) {
        return propId == ItemId.MARCH_CANCEL || propId == ItemId.HIGH_MARCH_CANCEL;
    }

    // 28 29
    public void speedMarchByItem(WorldPb.SpeedMarchRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int propId = req.getPropId();
        if (!isSpeedItem(propId)) {
            handler.sendErrorMsgToPlayer(GameError.SPEED_ITEM_ERROR);
            return;
        }

        // Item item = player.getItem(propId);
        // if (item == null) {
        // handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
        // return;
        // }
        //
        // if (item.getItemNum() < 1) {
        // handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
        // return;
        // }
        Item item = null;
        int mapId = player.getLord().getMapId();
        boolean flag = false;
        if (mapId == MapId.FIRE_MAP) {
            FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
            Map<Integer, Item> prop = flamePlayer.getProp();
            item = prop.get(propId);
            if (item != null && item.getItemNum() > 0) {
                flameWarManager.subProp(flamePlayer, propId, 1);
                flag = true;
            }
        }
        if (!flag) {
            Map<Integer, Item> itemMap = player.getItemMap();
            item = itemMap.get(propId);
            if (item == null || item.getItemNum() < 1) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
                return;
            }
            playerManager.subAward(player, AwardType.PROP, propId, 1, Reason.CANCEL_MARCH);
        }

        int keyId = req.getKeyId();
        March march = isCanSpeedMarch(handler, player, keyId);
        if (march == null) {
            return;
        }
        if (propId == ItemId.HIGH_SPEED) { // 50%
            long leftTime = march.getEndTime() - System.currentTimeMillis();
            leftTime = Math.max(0, leftTime);
            long reduce = leftTime / 2;
            march.setEndTime(march.getEndTime() - reduce);
        } else if (propId == ItemId.MAX_SPEED) { // 1s
            long leftTime = march.getEndTime() - System.currentTimeMillis();
            leftTime = Math.max(0, leftTime);
            long reduce = leftTime - 1000L;
            reduce = Math.max(0L, reduce);
            march.setEndTime(march.getEndTime() - reduce);
        }

        // playerManager.subAward(player, AwardType.PROP, propId, 1, Reason.SPEED_MARCH);
        WorldPb.SpeedMarchRs.Builder builder = WorldPb.SpeedMarchRs.newBuilder();
        builder.setGold(player.getGold());
        builder.setProp(item.wrapPb());
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.SpeedMarchRs.ext, builder.build());

        // 全区域广播
        // int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo != null) {
            worldManager.synMarch(mapInfo.getMapId(), march);
        }
        eventManager.marchSpeed(player, Lists.newArrayList(propId == ItemId.HIGH_SPEED ? "高级加速" : "顶级加速", march.getMarchType(), 0));
    }

    public void speedMarchByGold(WorldPb.SpeedMarchRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int propId = req.getPropId();
        if (!isSpeedItem(propId)) {
            handler.sendErrorMsgToPlayer(GameError.SPEED_ITEM_ERROR);
            return;
        }

        StaticProp staticProp = staticPropDataMgr.getStaticProp(propId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        int price = staticProp.getPrice();
        if (player.getGold() < price) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        int keyId = req.getKeyId();
        March march = isCanSpeedMarch(handler, player, keyId);
        if (march == null) {
            return;
        }

        if (propId == ItemId.HIGH_SPEED) { // 50%
            long leftTime = march.getEndTime() - System.currentTimeMillis();
            leftTime = Math.max(0, leftTime);
            long reduce = leftTime / 2;
            march.setEndTime(march.getEndTime() - reduce);
        } else if (propId == ItemId.MAX_SPEED) { // 1s
            long leftTime = march.getEndTime() - System.currentTimeMillis();
            leftTime = Math.max(0, leftTime);
            long reduce = leftTime - 1000L;
            reduce = Math.max(0L, reduce);
            march.setEndTime(march.getEndTime() - reduce);
        }

        playerManager.subAward(player, AwardType.GOLD, 0, price, Reason.SPEED_MARCH);
        WorldPb.SpeedMarchRs.Builder builder = WorldPb.SpeedMarchRs.newBuilder();
        builder.setGold(player.getGold());
        Item item = player.getItem(propId);
        if (item != null) {
            builder.setProp(item.wrapPb());
        }
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.SpeedMarchRs.ext, builder.build());

        // 全区域广播
        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo != null) {
            worldManager.synMarch(mapInfo.getMapId(), march);
        }
        eventManager.marchSpeed(player, Lists.newArrayList(propId == ItemId.HIGH_SPEED ? "高级加速" : "顶级加速", march.getMarchType(), price));
    }

    /**
     * 检测是否能够加速
     *
     * @param handler
     * @param player
     * @param keyId
     * @return
     */
    private March isCanSpeedMarch(ClientHandler handler, Player player, int keyId) {
        March march = player.getMarch(keyId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return null;
        }

        if (!isSpeedMarchOk(march)) {
            handler.sendErrorMsgToPlayer(GameError.SPEED_MARCH_TYPE_ERROR);
            return null;
        }

        if (!isMarchStateOk(march)) {
            handler.sendErrorMsgToPlayer(GameError.MARCH_STATE_ERROR);
            return null;
        }
        return march;
    }

    public boolean isSpeedMarchOk(March march) {
        if (march.getState() == MarchState.Begin) {
            if (march.getMarchType() == MarchType.AttackMonster || march.getMarchType() == MarchType.CollectResource || march.getMarchType() == MarchType.SUPER_COLLECT || march.getMarchType() == MarchType.FLAME_COLLECT) {
                return true;
            }
        } else if (march.getState() == MarchState.Back) {
            return true;
        }

        return false;
    }

    public boolean isMarchStateOk(March march) {
        if (march.getState() == MarchState.Begin || march.getState() == MarchState.Back) {
            return true;
        }

        return false;
    }

    public boolean isCancelMarchStateOk(March march) {
        if (march.getState() == MarchState.Begin) {
            return true;
        }

        return false;
    }

    public boolean isCancelMarchWaiting(March march) {
        if (march.getState() == MarchState.Waiting) {
            return true;
        }

        return false;
    }

    public boolean isCancelMarchCollect(March march) {
        if (march.getState() == MarchState.Collect) {
            return true;
        }
        return false;
    }

    public GameError checkMarchState(March march) {
        if (march.getState() == MarchState.Fighting || march.getState() == MarchState.FightOver) {
            return GameError.MARCH_FIGHTING;
        } else if (march.getState() == MarchState.Back) {
            return GameError.MARCH_RETURNED;
        }
        return GameError.OK;
    }

    // 26 27
    public void marchCancelByItem(WorldPb.MarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int propId = req.getPropId();
        if (!isMarchCancelItem(propId)) {
            handler.sendErrorMsgToPlayer(GameError.SPEED_ITEM_ERROR);
            return;
        }
        int mapId = player.getLord().getMapId();
        boolean flag = false;
        Item item = null;
        if (mapId == MapId.FIRE_MAP) {
            FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
            Map<Integer, Item> prop = flamePlayer.getProp();
            item = prop.get(propId);
            if (item != null && item.getItemNum() > 0) {
                flameWarManager.subProp(flamePlayer, propId, 1);
                flag = true;
            }
        }
        if (!flag) {
            Map<Integer, Item> itemMap = player.getItemMap();
            item = itemMap.get(propId);
            if (item == null || item.getItemNum() < 1) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
                return;
            }
            playerManager.subAward(player, AwardType.PROP, propId, 1, Reason.CANCEL_MARCH);
        }
        int keyId = req.getKeyId();
        March march = player.getMarch(keyId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }
        GameError marchState = marchManager.checkMarchState(march);
        if (marchState != GameError.OK) {
            handler.sendErrorMsgToPlayer(marchState);
            return;
        }

        HashSet<Long> players = getMarchPlayers(march);
        handleCancelWar(player, keyId, MarchReason.MarchCancelByItem);
        handleRemoveMarch(march, player);
        if (propId == ItemId.MARCH_CANCEL) { // 返回基地
            worldManager.doMiddleReturn(march, MarchReason.MarchCancelByItem);
        } else if (propId == ItemId.HIGH_MARCH_CANCEL) { // 立刻达到
            doMarchOver(march, player);
        } else {
            synMarchToPlayer(players, march);
        }

        playerManager.subAward(player, AwardType.PROP, propId, 1, Reason.CANCEL_MARCH);
        WorldPb.MarchCancelRs.Builder builder = WorldPb.MarchCancelRs.newBuilder();
        builder.setGold(player.getGold());
        builder.setProp(item.wrapPb());
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.MarchCancelRs.ext, builder.build());
    }

    public HashSet<Long> getMarchPlayers(March march) {
        return worldManager.getMarchPlayers(march);
    }

    public void synMarchToPlayer(HashSet<Long> players, March march) {
        // 行军同步
        WorldPb.SynMarchRq msg = worldManager.createSynMarchRq(march);
        playerManager.getOnlinePlayer().forEach(e -> {
            playerManager.synMarchToPlayer(e, msg);
        });
    }

    // 撤回行军
    public void marchCancelFree(WorldPb.MarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int keyId = req.getKeyId();
        March march = player.getMarch(keyId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }
        GameError marchState = marchManager.checkMarchState(march);
        if (marchState != GameError.OK) {
            handler.sendErrorMsgToPlayer(marchState);
            return;
        }

//		if (!isCancelMarchWaiting(march)) {
//			handler.sendErrorMsgToPlayer(GameError.MARCH_STATE_ERROR);
//			return;
//		}

        // 母巢的战斗
        if (march.getMarchType() == MarchType.BROOD_WAR) {
            broodWarManager.cannelMarch(march);
        } else if (march.getMarchType() == MarchType.ZERG_WAR) {
            zergManager.cannelZergMarch(march);
        } else if (march.getMarchType() == MarchType.ZERG_DEFEND_WAR) {
            zergManager.cannelZergHelpMarch(march);
        }
        HashSet<Long> players = getMarchPlayers(march);
        handleCancelWar(player, keyId, MarchReason.MarchCancelFree);
        handleRemoveMarch(march, player);
        marchManager.doMarchReturn(march, player, MarchReason.MarchCancelFree);
        synMarchToPlayer(players, march);
        WorldPb.MarchCancelRs.Builder builder = WorldPb.MarchCancelRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.MarchCancelRs.ext, builder.build());
    }

    // 采集撤回行军
    public void marchCancelCollect(WorldPb.MarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int keyId = req.getKeyId();
        March march = player.getMarch(keyId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }
        GameError marchState = marchManager.checkMarchState(march);
        if (marchState != GameError.OK) {
            handler.sendErrorMsgToPlayer(marchState);
            return;
        }

        // 结算
        Resource resource = handleMarchResource(player, march);
        handleRemoveMarch(march, player);
        if (resource != null && resource.getCount() > 0) {
            List<Entity> list = new ArrayList<>();
            resource.setFlush(1);
            list.add(resource);
            worldManager.synEntityAddRq(list);
            resource.setFlush(0);
        }
        marchManager.doMarchReturn(march, player, MarchReason.MarchCancelCollect);
        WorldPb.MarchCancelRs.Builder builder = WorldPb.MarchCancelRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.MarchCancelRs.ext, builder.build());
    }

    // 采集撤回行军
    public void marchCancelRebel(WorldPb.MarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int keyId = req.getKeyId();
        March march = player.getMarch(keyId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }
        GameError marchState = marchManager.checkMarchState(march);
        if (marchState != GameError.OK) {
            handler.sendErrorMsgToPlayer(marchState);
            return;
        }
        warManager.handleRebelMarchReturn(march, MarchReason.CANCEL_REBEL_BACK);

        handleRemoveRebelMarch(march);
        int mapId = worldManager.getMapId(player);
        worldManager.synMarch(mapId, march);
        // 回城
        WorldPb.MarchCancelRs.Builder builder = WorldPb.MarchCancelRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.MarchCancelRs.ext, builder.build());
    }

    // 采集撤回行军
    public void marchCancelBigMonster(WorldPb.MarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int keyId = req.getKeyId();
        March march = player.getMarch(keyId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }
        GameError marchState = marchManager.checkMarchState(march);
        if (marchState != GameError.OK) {
            handler.sendErrorMsgToPlayer(marchState);
            return;
        }

        int mapId = worldManager.getMapId(march.getEndPos());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        IWar war = mapInfo.getWarMap().get(march.getWarId());
        if (war != null) {
            war.getAttacker().getMarchList().remove(march);
        }
        // 此处使用 pos作为key有隐患
        marchManager.handleMarchReturn(march, MarchReason.CANCEL_BIGMONSTER_BACK);
        worldManager.synMarch(mapId, march);
        // 回城
        WorldPb.MarchCancelRs.Builder builder = WorldPb.MarchCancelRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.MarchCancelRs.ext, builder.build());
    }

    public Resource handleMarchResource(Player player, March march) {

        int mapId = worldManager.getMapId(march.getEndPos());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.error("handleMarchResource mapInfo is null!");
            return null;
        }

        Entity entity = mapInfo.getEntity(march.getEndPos());
        if (entity == null) {
            LogHelper.CONFIG_LOGGER.error("handleMarchResource resoure is null!");
            return null;
        }

        if (!(entity instanceof Resource)) {
            LogHelper.CONFIG_LOGGER.error("entity is not resource.");
            return null;
        }

        Resource resource = (Resource) entity;

        // 采集时长
        long collectTime = worldLogic.getCollectTime(march);
        Award award = worldManager.caculateResCount(march, resource, collectTime, player, true);
        march.addAwards(award);
        if (resource.getCount() <= 0) {
            worldManager.clearResourcePos(mapInfo, march.getEndPos());
            worldManager.synEntityRemove(resource, mapId, resource.getPos());
        }

        // 采集完成
        List<Award> awards = march.getAwards();
        long count = 0;
        if (awards != null && awards.size() == 1) {
            count = awards.get(0).getCount();
        }

        // 采集撤回行军将状态设置成未 采集状态0
        resource.setStatus(0);
        resource.setPlayer(null);

        if (player != null) {
            List<Integer> heroIds = march.getHeroIds();
            if (heroIds != null && heroIds.size() == 1) {
                int heroId = heroIds.get(0);
                // 计算采集时间
                Hero hero = player.getHero(heroId);
                if (hero != null) {
                    battleMailMgr.sendCollectDone(MailId.COLLECT_CANCEL, resource, collectTime, count, heroId, hero.getHeroLv(), player, false, null);
                }
            }
        }
        return resource;
    }

    public void marchCancelVip(WorldPb.MarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int keyId = req.getKeyId();
        Lord lord = player.getLord();
        StaticVip staticVip = staticVipMgr.getStaticVip(lord.getVip());
        if (staticVip == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int day = GameServer.getInstance().currentDay;
        if (day != lord.getFreeBackDay()) {
            lord.setFreeBackDay(day);
            lord.setFreeBackTimes(0);
        }

        int backTimes = staticVip.getCallArmy();
        int currentTimes = lord.getFreeBackTimes();
        if (currentTimes >= backTimes) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_CALL_BACKTIMES);
            return;
        }

        lord.setFreeBackTimes(lord.getFreeBackTimes() + 1);

        March march = player.getMarch(keyId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }
        GameError marchState = marchManager.checkMarchState(march);
        if (marchState != GameError.OK) {
            handler.sendErrorMsgToPlayer(marchState);
            return;
        }

        HashSet<Long> players = getMarchPlayers(march);
        handleCancelWar(player, keyId, MarchReason.MarchCancelVip);
        handleRemoveMarch(march, player);
        worldManager.doMiddleReturn(march, MarchReason.MarchCancelVip);
        synMarchToPlayer(players, march);
        WorldPb.MarchCancelRs.Builder builder = WorldPb.MarchCancelRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        builder.setVipUseTimes(lord.getFreeBackTimes());
        handler.sendMsgToPlayer(WorldPb.MarchCancelRs.ext, builder.build());
    }

    // 立刻到达
    public void doMarchOver(March march, Player player) {
        int mapId = worldManager.getMapId(march.getEndPos());
        march.swapPos(Reason.FLAME);
        march.setState(MarchState.Back);
        march.setEndTime(1000);
        worldManager.synMarch(mapId, march);
        playerManager.handlerMarch(player);
        // 世界地图也应该删除
        // MapInfo mapInfo = worldManager.getMapInfo(mapId);
        // if (mapInfo != null) {
        // mapInfo.removeMarch(march);
        // }

        doReturnTask(player);
    }

    public void doReturnTask(Player player) {
        taskManager.doTask(TaskType.ARMMY_RETURN, player, null);
    }

    public void marchCancelByGold(WorldPb.MarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int propId = req.getPropId();
        if (!isMarchCancelItem(propId)) {
            handler.sendErrorMsgToPlayer(GameError.SPEED_ITEM_ERROR);
            return;
        }

        StaticProp staticProp = staticPropDataMgr.getStaticProp(propId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        int price = staticProp.getPrice();
        if (player.getGold() < price) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        int keyId = req.getKeyId();
        March march = player.getMarch(keyId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }
        GameError marchState = marchManager.checkMarchState(march);
        if (marchState != GameError.OK) {
            handler.sendErrorMsgToPlayer(marchState);
            return;
        }

        HashSet<Long> players = getMarchPlayers(march);
        handleCancelWar(player, keyId, MarchReason.MarchCancelByGold);
        handleRemoveMarch(march, player);
        if (propId == ItemId.MARCH_CANCEL) { // 返回基地
            doMarchCancelReturn(march, player, MarchReason.MarchCancelByGold);
        } else if (propId == ItemId.HIGH_MARCH_CANCEL) { // 立刻达到
            doMarchOver(march, player);
        } else {
            synMarchToPlayer(players, march);
        }

        playerManager.subAward(player, AwardType.GOLD, 0, price, Reason.CANCEL_MARCH);

        WorldPb.MarchCancelRs.Builder builder = WorldPb.MarchCancelRs.newBuilder();
        builder.setGold(player.getGold());
        Item item = player.getItem(propId);
        if (item != null) {
            builder.setProp(item.wrapPb());
        }
        worldManager.checkCompanion(march);
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.MarchCancelRs.ext, builder.build());
    }

    // 不要和worldManager里面的返程合并
    public void doMarchCancelReturn(March march, Player player, int reason) {
        int mapId = worldManager.getMapId(player);
        handleCancelReturn(march, reason);
        worldManager.synMarch(mapId, march);
    }

    // 仅限于取消
    public void handleCancelReturn(March march, int reason) {
        // 回城
        march.setState(MarchState.FightOver);
        march.swapPos(reason);
        // 计算行军时间
        long leftTime = march.getEndTime() - System.currentTimeMillis();
        long marchTime = march.getPeriod() - leftTime;
        marchTime = Math.max(0, marchTime);
        march.setEndTime(System.currentTimeMillis() + marchTime);
    }

    /**
     * 行军撤回
     *
     * @param req
     * @param handler
     */
    public void marchCancel(WorldPb.MarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (req.getType() == 1) {
            marchCancelByItem(req, handler); // by item
        } else if (req.getType() == 2) {
            marchCancelByGold(req, handler); // by gold
        } else if (req.getType() == 3) {
            marchCancelFree(req, handler); // by cancel
        } else if (req.getType() == 4) {
            marchCancelVip(req, handler); // by vip
        } else if (req.getType() == 5) {
            marchCancelCollect(req, handler);
        } else if (req.getType() == 6) {
            marchCancelRebel(req, handler);
        } else if (req.getType() == 7) {
            marchCancelBigMonster(req, handler);
        } else if (req.getType() == 8) {
            marchCancelSuperRes(req, handler);
        } else {
            handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
        }

        // 这里处理 建筑内兵线和矿点内兵线
        flameWarManager.cancelFlameMarch(req, player);
    }

    /**
     * 战斗支援请求
     *
     * @param req
     * @param handler
     */
    public void cityFightHelpRq(WorldPb.CityFightHelpRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int mapId = playerManager.getMapId(player);

        long warId = req.getKeyId();
        WarInfo warInfo = worldManager.getPvpWarInfo(warId);

        if (warInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.WAR_END_OR_NOT_EXIST);
            return;
        }

        int warType = warInfo.getWarType();
        if (!(warType == WarType.Attack_WARFARE || warType == WarType.ATTACK_FAR || warType == WarType.ATTACK_QUICK)) {
            handler.sendErrorMsgToPlayer(GameError.WAR_CANNT_HELP);
            return;
        }
        if (req.getType() == 1) {
            if (warInfo.getAttackerHelpTime() > 3) { // 支援次数最大为3次
                handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
                return;
            }

            warInfo.setAttackerHelpTime(warInfo.getAttackerHelpTime() + 1);
        } else {
            if (warInfo.getDefencerHelpTime() > 3) { // 支援次数最大为3次
                handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
                return;
            }

            warInfo.setDefencerHelpTime(warInfo.getDefencerHelpTime() + 1);
        }

        Pos pos = warInfo.getDefencerPos();
        // 发送信息到聊天
        if (warInfo.getAttackerId() == player.getLord().getLordId()) {
            Player target = playerManager.getPlayer(warInfo.getDefencerId());
            String tarPos = String.format("%s,%s", pos.getX(), pos.getY());
            String[] p = {String.valueOf(target.getCountry()), target.getNick(), tarPos};
            Chat chat = chatManager.createManShare(player, ChatId.ATTACK_CITY, p);
            chatManager.sendMapShare(chat, mapId, player.getCountry());
        } else if (warInfo.getDefencerId() == player.getLord().getLordId()) {
            Player target = playerManager.getPlayer(warInfo.getAttackerId());
            String mypos = String.format("%s,%s", pos.getX(), pos.getY());
            Chat chat = chatManager.createManShare(player, ChatId.CITY_ATTACK, mypos, String.valueOf(target.getCountry()), target.getNick());
            chatManager.sendMapShare(chat, mapId, player.getCountry());
        } else {
            handler.sendErrorMsgToPlayer(GameError.WAR_END_OR_NOT_EXIST);
            return;
        }

        WorldPb.CityFightHelpRs.Builder builder = WorldPb.CityFightHelpRs.newBuilder();
        handler.sendMsgToPlayer(WorldPb.CityFightHelpRs.ext, builder.build());
    }

    /**
     * @param handler
     */
    public void callTransferRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int mapId = playerManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        Lord lord = player.getLord();

        Pos pos = player.getPos();

        PlayerCity playerCity = mapInfo.getPlayerCityMap().get(pos);
        if (playerCity == null) {
            handler.sendErrorMsgToPlayer(GameError.CITY_NOT_EXISTS);
            return;
        }

        CtyGovern govern = countryManager.getGovern(player);
        if (govern == null || govern.getGovernId() < 1 || govern.getGovernId() > 4) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOVERN);
            return;
        }

        long callEndTime = playerCity.getCallEndTime();
        int callCount = playerCity.getCallCount();
        int callReply = playerCity.getCallReply();
        long currentTime = System.currentTimeMillis();

        WorldPb.CallTransferRs.Builder builder = WorldPb.CallTransferRs.newBuilder();
        // 首次,以及再次召唤
        if (callEndTime == 0 || callEndTime < currentTime || callCount <= callReply) {
            int callDay = lord.getCallDay();
            int callTimes = lord.getCallTimes();
            if (callDay != GameServer.getInstance().currentDay) {
                callTimes = 0;
            }

            // 活动召唤
            int activityCount = activityManager.getActGovernCall();

            // 首次召唤免费
            // 第二次召唤消耗低级城迁
            // 召唤超过2次就不可以再次召唤
            StaticCountryGovern staticGovern = staticCountryMgr.getGovern(govern.getGovernId());
            if (staticGovern == null) {
                handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
                return;
            }
            callCount = staticGovern.getPerson();
            int maxTime = staticGovern.getCount() + activityCount;
            int freeTime = CountryConst.FREE_CALL + activityCount;
            if (callTimes >= maxTime) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_COUNT);
                return;
            }
            if (callTimes >= freeTime) {
                Item item = itemManager.getItem(player, staticGovern.getPropId());
                if (item == null || item.getItemNum() < staticGovern.getPropNum()) {
                    handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
                    return;
                }
                itemManager.subItem(player, staticGovern.getPropId(), staticGovern.getPropNum(), Reason.CALL_TRANSFER);
                builder.setProp(PbHelper.createItemPb(item.getItemId(), item.getItemNum()));
            }
            lord.setCallDay(GameServer.getInstance().currentDay);
            lord.setCallTimes(callTimes + 1);
            lord.setCallCount(callCount);
            lord.setCallReply(0);
            lord.setCallEndTime(currentTime + 10 * 60 * 1000L);

            playerCity.setCallCount(callCount);
            playerCity.setCallReply(0);
            playerCity.setCallEndTime(currentTime + 10 * 60 * 1000L);

            // 通知国家内的玩家，城池召唤
            worldManager.SynPlayerCityCallRq(player, playerCity, mapId);
        }

        builder.setCallTimes(lord.getCallTimes());
        handler.sendMsgToPlayer(WorldPb.CallTransferRs.ext, builder.build());

        String p1 = String.valueOf(player.getCountry());
        String p2 = String.valueOf(mapId);
        String p3 = String.format("%s,%s", player.getPosX(), player.getPosY());
        chatManager.sendCountryChat(player.getCountry(), ChatId.GOVERN_CALL, p1, player.getNick(), p2, p1, p3);
    }

    /**
     * 响应召唤
     *
     * @param req
     * @param handler
     */
    public void replyTransfer(ReplyTransferRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 有将领在外
        if (!player.getMarchList().isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
            return;
        }

        // 要迁移的目标坐标点周围
        Pos pos = new Pos(req.getPos().getX(), req.getPos().getY());
        int mapId = worldManager.getMapId(pos);

        MapInfo newMapInfo = worldManager.getMapInfo(mapId);
        if (newMapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        PlayerCity city = newMapInfo.getPlayerCity(pos);
        if (city == null || city.getCountry() != player.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.NO_TRANSFER);
            return;
        }

        long callEndTime = city.getCallEndTime();
        long currentTime = System.currentTimeMillis();
        if (currentTime >= callEndTime) {
            handler.sendErrorMsgToPlayer(GameError.NO_TRANSFER);
            return;
        }

        int callReply = city.getCallReply();
        int callCount = city.getCallCount();
        if (callReply >= callCount) {
            handler.sendErrorMsgToPlayer(GameError.NO_TRANSFER);
            return;
        }

        int currentMapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(currentMapId);

        Pos playerPos = player.getPos();

        // 目标城池周围活动一个随机坐标
        Pos randPos = worldManager.randPos(city.getPlayer(), 5);
        if (randPos.isError() || !newMapInfo.isFreePos(randPos)) {
            LogHelper.CONFIG_LOGGER.error("rand Pos is error!");
            handler.sendErrorMsgToPlayer(GameError.RAND_POS_ERR);
            return;
        }

        int isFound = player.isMapCanMoves(mapId);
//        Map<Integer, MapStatus> mapStatuses = player.getMapStatusMap();
//        for (MapStatus status : mapStatuses.values()) {
//            if (status.getMapId() == mapId && status.getStatus() == 2) {
//                isFound = true;
//            }
//        }

        // bugs here
//        if (!isFound) {
//            handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
//            return;
//        }

        switch (isFound) {
            case 1:
                handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
                return;
            case 2:
                handler.sendErrorMsgToPlayer(GameError.CANT_MOVE);
                return;
            default:
                break;
        }

        Player target = playerManager.getPlayer(city.getLordId());

        city.setCallReply(callReply + 1);
        target.getLord().setCallReply(callReply + 1);

        // 可以迁城,先移除原来的城池
        // 更改城池坐标
        worldManager.removePlayerCity(playerPos, mapInfo);

        playerManager.changePlayerPos(player, randPos);

        worldManager.addPlayerCity(randPos.clone(), newMapInfo, player);

        // 驻防武将回城
        worldManager.handleWallFriendReturn(player);
        // 先通知玩家返程
        worldManager.removePlayerWar(player, playerPos, MarchReason.Reply, randPos);
        // 迁城之后战斗全部删除

        ReplyTransferRs.Builder builder = ReplyTransferRs.newBuilder();
        builder.setPos(player.getPos().wrapPb());
        handler.sendMsgToPlayer(WorldPb.ReplyTransferRs.ext, builder.build());

        // 通知国家内的玩家，城池召唤召唤数目更新
        worldManager.SynPlayerCityCallRq(target, city, mapId);

        // 通知玩家
//        PlayerCity newPlayerCity = mapInfo.getPlayerCity(randPos);
//        if (newPlayerCity != null) {
//            worldManager.synEntityRq(newPlayerCity, mapId, playerPos);
//        }
    }

    public void getDefenceInfoRq(WorldPb.GetDefenceInfoRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        long lordId = req.getLordId();
        Player target = playerManager.getPlayer(lordId);
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Wall wall = target.getWall();
        if (wall == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        HashMap<Integer, WallFriend> wallFriends = wall.getWallFriends();
        WorldPb.GetDefenceInfoRs.Builder builder = WorldPb.GetDefenceInfoRs.newBuilder();
        for (WallFriend wallFriend : wallFriends.values()) {
            builder.addFriend(playerManager.wrapWallFriend(wallFriend));
        }
        builder.setWallLv(wall.getLv());
        handler.sendMsgToPlayer(WorldPb.GetDefenceInfoRs.ext, builder.build());
    }

    public void handleCancelWar(Player player, int marchKeyId, int reason) {
        // 如果player是战斗发起者，则战斗取消
        long lordId = player.roleId;
        March find = player.getMarch(marchKeyId);
        if (find == null) {
            LogHelper.CONFIG_LOGGER.error("not found march!");
            return;
        }

        long warId = find.getWarId();

        // 攻击者不在这个地图上,所以说要以行军为主
        Pos endPos = find.getEndPos();
        int mapId = worldManager.getMapId(endPos);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.error("handleCancelWar mapInfo is null");
            return;
        }

//		Map<Long, WarInfo> cityWarMap = mapInfo.getCityWarMap();
//		if (find.getMarchType() == MarchType.AttackCityQuick) {
//			cityWarMap = mapInfo.getQuickWarMap();
//		}
//		WarInfo cityWar = cityWarMap.get(warId);
        IWar war = mapInfo.getWar(warId);
        if (war == null) {
            return;
        }
        if (war.getWarType() == WarType.Attack_WARFARE || war.getWarType() == WarType.ATTACK_FAR || war.getWarType() == WarType.ATTACK_QUICK) {
            WarInfo cityWar = (WarInfo) war;
//			if (cityWar != null && cityWar.getAttackerId() == lordId) {
            if (cityWar.getAttacker().getId() == lordId) {

                removeMarch(cityWar, marchKeyId, lordId);
                // 检测玩家是否还有多余的行军
                if (cityWar.hasPlayerMarch(lordId)) {
                    return;
                }

                // 通知所有部队遣返
                Player defencer = playerManager.getPlayer(cityWar.getDefencerId());
                doWarMarchReturn(cityWar, player, defencer, reason);
                // 防守方删除战斗(没有被击飞,只删除当前的战斗)
                long targetId = cityWar.getDefencerId();
                Player target = playerManager.getPlayer(targetId);
                if (target != null) {
                    WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(cityWar);
                    worldManager.synRemoveWar(target, synCityWarRq); // 通知target取消战斗
                }

                // 同步玩家战斗取消
                warManager.handePvpWarRemove(cityWar);
                mapInfo.removeWar(war);
                worldManager.flushWar(cityWar, false, cityWar.getAttackerCountry());
                battleMailMgr.sendCancelWar(player, defencer, target, MailId.CANCEL_DEF);
            }
        }
        // 虫族入侵
        Player target = playerManager.getPlayer(find.getDefencerId());
        if (target != null) {
            WarInfo roitWarInfo = target.getSimpleData().getRiotWarInfo();
            if (roitWarInfo != null) {
                removeMarch(roitWarInfo, marchKeyId, lordId);
            }
        }

        // 巨型虫族
        if (war.getWarType() == WarType.BIGMONSTER_WAR) {
            war.getAttacker().getMarchList().remove(find);
        }
    }

    // 玩家撤回的时候删除国战中的行军[需要考虑跨区域]
    public void handleRemoveMarch(March march, Player player) {
        int mapId = worldManager.getMapId(march.getEndPos());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.error("handleCancelWar mapInfo is null");
            return;
        }

        long warId = march.getWarId();
        long marchId = march.getKeyId();
        long lordId = march.getLordId();

//		Map<Long, WarInfo> countryWarMap = mapInfo.getCountryWarMap();
//		WarInfo countryWar = countryWarMap.get(warId);
        IWar war = mapInfo.getWar(warId);
        if (war != null && war instanceof CountryCityWarInfo) {
            removeMarch((CountryCityWarInfo) war, marchId, lordId);
            // 同步国战兵力
            warManager.synWarInfo(war, war.getAttacker().getCountry(), war.getDefencer().getCountry());
            GameServer.getInstance().mainLogicServer.addCommand(() -> {
                worldManager.sendWar(player);
            });
        }

//		Map<Long, WarInfo> zergWarMap = mapInfo.getZergWarMap();
//		WarInfo zergWar = zergWarMap.get(warId);
        if (war != null && war instanceof ZergWarInfo) {
            zergManager.cannelZergMarch(march);
        }

//		WarInfo pvpWarInfo = worldManager.getPvpWarInfo(mapInfo, warId);
        if (war != null && war instanceof WarInfo) {
            removeMarch((WarInfo) war, marchId, lordId);
            warManager.synWarInfo(war);
            GameServer.getInstance().mainLogicServer.addCommand(() -> {
                worldManager.sendWar(player);
            });
        }
        if (war != null) {
            warManager.synWarInfo(war);
        }
    }

    public void handleRemoveRebelMarch(March march) {
        int mapId = worldManager.getMapId(march.getEndPos());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.error("handleCancelWar mapInfo is null");
            return;
        }
        long warId = march.getWarId();
        Map<Long, WarInfo> rebelWarMap = mapInfo.getRebelWarMap();
        WarInfo rebelWar = rebelWarMap.get(warId);
        if (rebelWar != null) {
            ConcurrentLinkedDeque<March> attacker = rebelWar.getAttackMarches();
            Iterator<March> attackerIterator = attacker.iterator();
            while (attackerIterator.hasNext()) {
                March m = attackerIterator.next();
                if (m == null) {
                    attackerIterator.remove();
                    continue;
                }
                if (march.getKeyId() == m.getKeyId()) {
                    attackerIterator.remove();
                    break;
                }
            }
            worldManager.handleRebelWarSoldier(rebelWar);
            warManager.synRebelWarInfo(rebelWar);
        }
    }

    public void removeMarch(WarInfo warInfo, long marchId, long lordId) {
        ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();
        Iterator<March> attackerIterator = attacker.iterator();
        while (attackerIterator.hasNext()) {
            March march = attackerIterator.next();
            if (march == null) {
                attackerIterator.remove();
                continue;
            }

            if (march.getKeyId() == marchId && march.getLordId() == lordId) {
                attackerIterator.remove();
                continue;
            }
        }

        ConcurrentLinkedDeque<March> defencer = warInfo.getDefenceMarches();
        Iterator<March> defencerIterator = defencer.iterator();
        while (defencerIterator.hasNext()) {
            March march = defencerIterator.next();
            if (march == null) {
                defencerIterator.remove();
                continue;
            }

            if (march.getKeyId() == marchId && march.getLordId() == lordId) {
                defencerIterator.remove();
                continue;
            }
        }
        worldManager.handleWarSoldier(warInfo); // 重新统计兵力
    }

    public void doWarMarchReturn(WarInfo warInfo, Player caller, Player defencer, int reason) {
        // 通知所有部队遣返
        ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();
        for (March march : attacker) {
            long targetId = march.getLordId();
            if (targetId == caller.roleId) {
                continue;
            }
            Player target = playerManager.getPlayer(targetId);
            if (target == null) {
                continue;
            }
            worldManager.doMiddleReturn(march, reason);
            battleMailMgr.sendCancelWar(caller, defencer, target, MailId.CANCEL_ATTACK);
        }
        if (attacker.isEmpty()) {
            long targetId = warInfo.getAttackerId();
            Player target = playerManager.getPlayer(targetId);
            if (target != null) {
                battleMailMgr.sendCancelWar(caller, defencer, target, MailId.CANCEL_ATTACK);
            }
        }

        ConcurrentLinkedDeque<March> defenceMarches = warInfo.getDefenceMarches();
        for (March march : defenceMarches) {
            long targetId = march.getLordId();
            Player target = playerManager.getPlayer(targetId);
            if (target == null) {
                continue;
            }
            worldManager.doMiddleReturn(march, reason);
            battleMailMgr.sendCancelWar(caller, defencer, target, MailId.CANCEL_DEF);
        }
    }

    public void getCityRq(WorldPb.GetCityRq req, ClientHandler handler) {
        int mapId = req.getMapId();
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        if (mapId == 0) {
            mapId = worldManager.getMapId(player);
        }

        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.MAP_INFO_NOT_EXISTS);
            return;
        }

        WorldPb.GetCityRs.Builder builder = WorldPb.GetCityRs.newBuilder();
        ConcurrentHashMap<Integer, City> cityMap = cityManager.getCityMap();
        for (City cityInfo : cityMap.values()) {
            if (cityInfo == null) {
                continue;
            }

            int cityId = cityInfo.getCityId();
            if (staticWorldMgr.getCityMapId(cityId) != mapId) {
                continue;
            }
            StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityInfo.getCityId());
            CommonPb.MapCity.Builder mapCity = CommonPb.MapCity.newBuilder();
            mapCity.setCityId(cityId);
            mapCity.setCityLv(cityInfo.getCityLv());
            mapCity.setCountry(cityInfo.getCountry());
            mapCity.setSoldier(cityManager.getCitySoldier(cityId));
            mapCity.setOwnId(cityInfo.getLordId());
            mapCity.setCityName(cityInfo.getCityName() == null ? staticWorldCity.getName() : cityInfo.getCityName());
            long lordId = cityInfo.getLordId();
            Player owner = playerManager.getPlayer(lordId);
            if (owner != null) {
                mapCity.setOwn(owner.getNick());
            }
            mapCity.setOwnEndTime(cityInfo.getEndTime());
            StaticWorldCity config = staticWorldMgr.getCity(cityId);
            if (config != null) {
                if (cityInfo.getCountry() == 0) {
                    mapCity.setPeriod(0);
                } else {
                    // int period = config.getPeriod();
                    long period = cityManager.getMakePeriod(config.getCityId());
                    mapCity.setPeriod(period);
                }
            }

            if (cityInfo.getAwardNum() <= 0 && mapCity.getPeriod() != 0) {
                mapCity.setEndTime(cityInfo.getMakeItemTime());
            } else {
                mapCity.setEndTime(0);
            }

            // 找到当前的国战信息
            List<IWar> warInfos = worldManager.getCtWar(mapId, cityId);
            for (IWar war : warInfos) {
                CountryCityWarInfo warInfo = (CountryCityWarInfo) war;
                worldManager.handleWarSoldier(warInfo);
                mapCity.addWarInfo(warInfo.wrapPb(warInfo.isJoin(player)));
            }

            Map<Long, CityElection> cityElections = cityManager.getCityElection(cityId);
            if (cityElections != null && !cityElections.isEmpty()) {
                List<CityElection> electionList = new ArrayList<CityElection>();
                for (CityElection election : cityElections.values()) {
                    electionList.add(election);
                }

                Collections.sort(electionList);
                for (CityElection cityElection : electionList) {
                    if (cityElection == null) {
                        continue;
                    }
                    long targetLordId = cityElection.getLordId();
                    Player electioner = playerManager.getPlayer(targetLordId);
                    if (electioner == null) {
                        continue;
                    }
                    CommonPb.CityElection.Builder data = CommonPb.CityElection.newBuilder();
                    data.setName(electioner.getNick());
                    data.setTitle(electioner.getTitle());
                    data.setEndTime(cityElection.getElectionTime());
                    mapCity.addCityElection(data);
                }
            }

            mapCity.setElectionEndTime(cityInfo.getElectionEndTime());

            // 玩家是否可以参加任务
            ConcurrentHashMap<Integer, HashSet<Long>> warAttend = cityManager.getWarAttenders();
            HashSet<Long> attenders = warAttend.get(cityId);
            if (attenders != null && attenders.contains(player.roleId)) {
                mapCity.setCanAttendElection(true);
            } else {
                mapCity.setCanAttendElection(false);
            }

            mapCity.setState(cityInfo.getState());
            mapCity.setPeople(cityInfo.getPeople());
            mapCity.setProtectedTime(cityInfo.getProtectedTime());

            WorldData worldData = worldManager.getWolrdInfo();

            CityRemark cityRemark = worldData.getCityRemark(player.getCountry());
            if (cityRemark != null && cityRemark.getCityId() == cityId) {
                mapCity.setCityRemark(cityRemark.encode());
            }
            builder.addCity(mapCity);
        }

        Map<Pos, PlayerCity> playerCityMap = mapInfo.getPlayerCityMap();
        StaticWorldMap staticWorldMap = staticWorldMgr.getStaticWorldMap(mapId);
        if (staticWorldMap == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int res;
        for (PlayerCity playerCity : playerCityMap.values()) {
            if (playerCity == null) {
                continue;
            }
            Pos pos = playerCity.getPos();
            if (pos.isError()) {
                continue;
            }
            res = playerCity.getLevel() * 10000000 + pos.getX() * 10000 + pos.getY() * 10 + playerCity.getCountry();
            builder.addInfo(res);
        }

        handler.sendMsgToPlayer(WorldPb.GetCityRs.ext, builder.build());
    }

    public void getWorldBoss(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        WorldData worldData = worldManager.getWolrdInfo();
        if (worldData == null) {
            LogHelper.CONFIG_LOGGER.error("worldData is null");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        Lord lord = player.getLord();
        if (lord == null) {
            LogHelper.CONFIG_LOGGER.error("lord is null");
            handler.sendErrorMsgToPlayer(GameError.NO_LORD);
            return;
        }

        WorldPb.GetWorldBossRs.Builder builder = WorldPb.GetWorldBossRs.newBuilder();
        // 找世界boss: 张角和共享
        Map<Integer, WorldBoss> bossMap = worldData.getBossMap(); // 张角
        for (WorldBoss elem : bossMap.values()) {
            if (elem == null || elem.getCountry() != 1) {
                continue;
            }

            builder.addWorldBoss(elem.wrapPb());
        }

        WorldBoss shareBoss = worldData.getShareBoss();
        if (shareBoss != null) {
            builder.addWorldBoss(shareBoss.wrapPb());
        }

        handler.sendMsgToPlayer(WorldPb.GetWorldBossRs.ext, builder.build());
    }

    // 行军时间是否合理, 行军时间不能超过战斗时间
    public boolean isMarchWarOk(long marchPeriod, WarInfo warInfo) {
        if (warInfo == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        long warTimeLeft = warInfo.getEndTime() - now;
        warTimeLeft = Math.max(0, warTimeLeft);
        if (marchPeriod > warTimeLeft) {
            return false;
        }

        return true;
    }

    public void devCityRq(WorldPb.DevCityRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 检查城池的合法性
        int cityId = req.getCityId();
        StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
        if (worldCity == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            LogHelper.CONFIG_LOGGER.error("cityId = " + cityId + " config error!");
            return;
        }

        // 检查城池的类型是否合法
        int cityType = worldCity.getType();
        if (cityType != CityType.SQUARE_FORTRESS) {
            handler.sendErrorMsgToPlayer(GameError.CITY_TYPE_ERROR);
            return;
        }

        // 检查是否跨区域
        int cityMapId = worldCity.getMapId();
        int mapId = worldManager.getMapId(player);
        if (mapId != cityMapId) {
            handler.sendErrorMsgToPlayer(GameError.DONOT_CROSS_MAP_DEV_CITY);
            return;
        }

        // 检查国家是否合法
        City city = cityManager.getCity(cityId);
        if (city == null) {
            handler.sendErrorMsgToPlayer(GameError.CITY_NOT_EXISTS);
            return;
        }

        // 检查玩家国家和城池国家是否相同
        if (player.getCountry() != city.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.CITY_COUNTRY_ERROR);
            return;
        }

        // 检查城池是否满级
        int currentPeople = city.getPeople();
        int maxPeople = staticLimitMgr.getNum(100);
        int cityLv = city.getCityLv();
        if (cityLv >= 3 && currentPeople >= maxPeople) {
            handler.sendErrorMsgToPlayer(GameError.CITY_REACH_MAX_LEVEL);
            return;
        }

        // 检查资源是否充足
        long ironNeed = staticLimitMgr.getNum(102);
        if (player.getIron() < ironNeed) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        // 检查资源是否充足
        long copperNeed = staticLimitMgr.getNum(103);
        if (player.getCopper() < copperNeed) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        // 扣除资源
        playerManager.subAward(player, AwardType.RESOURCE, ResourceType.IRON, ironNeed, Reason.DEV_CITY);
        playerManager.subAward(player, AwardType.RESOURCE, ResourceType.COPPER, copperNeed, Reason.DEV_CITY);

        // 检查当前等级国家的人口上限
        currentPeople += staticLimitMgr.getNum(101);
        currentPeople = Math.min(maxPeople, currentPeople);
        currentPeople = Math.max(0, currentPeople);
        if (currentPeople >= staticLimitMgr.getNum(98) && currentPeople < staticLimitMgr.getNum(99)) {
            city.setCityLv(2);
        } else if (currentPeople >= staticLimitMgr.getNum(99)) {
            city.setCityLv(3);
        }

        city.setPeople(currentPeople);
        WorldPb.DevCityRs.Builder builder = WorldPb.DevCityRs.newBuilder();
        builder.setCityId(cityId);
        builder.setPeople(currentPeople);
        builder.setCityLv(city.getCityLv());
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(WorldPb.DevCityRs.ext, builder.build());
        worldManager.synCityDev(city);
    }

    public int getPeople(int cityLv) {
        if (cityLv == 1) {
            return staticLimitMgr.getNum(98);
        } else if (cityLv == 2) {
            return staticLimitMgr.getNum(99);
        } else if (cityLv == 3) {
            return staticLimitMgr.getNum(100);
        }
        return 0;
    }

    // 先刷名城怪物，再刷四方要塞，再刷世界要塞
    // 地图加载之后再刷怪
    // 只刷type = 1的野怪
    public void flushWorldFortressObject() {
        if (worldManager.isNotOk()) {
            return;
        }

        // cityId = 1 ~ 24
        flushFortressMonster();
        flushFortressResource();
    }

    public void flushFortressMonster() {
        int mapId = 20;
        List<Integer> famouseCity = cityManager.getFamousCity();
        List<Integer> squareFortress = cityManager.getSquareFortress();
        List<Integer> allCity = new ArrayList<Integer>();
        allCity.addAll(famouseCity);
        allCity.addAll(squareFortress);
        allCity.add(25);

        Map<Integer, Integer> fortressMonsterConfig = staticWorldMgr.getFortressMonsterConfig();
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.error("mapInfo is null, mapId = " + mapId);
            return;
        }

        Map<Integer, Integer> monsterLimit = new HashMap<Integer, Integer>();
        int monsterLv;
        int monsterNum;

        int rangeX1;
        int rangeX2;
        int rangeY1;
        int rangeY2;
        Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
        for (Integer cityId : allCity) {
            City city = cityManager.getCity(cityId);
            if (city == null) {
                continue;
            }

            // LogHelper.GAME_DEBUG.error("cityId = " + city.getCityId());
            monsterLimit.clear();
            StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
            rangeX1 = worldCity.getRangex1();
            rangeX2 = worldCity.getRangex2();
            rangeY1 = worldCity.getRangey1();
            rangeY2 = worldCity.getRangey2();

            // 先找到这个区块所有的怪物

            for (Monster monster : monsterMap.values()) {
                if (monster == null) {
                    continue;
                }
                Pos pos = monster.getPos();
                if (pos.getX() >= rangeX1 && pos.getX() <= rangeX2 && pos.getY() >= rangeY1 && pos.getY() <= rangeY2) {
                    Integer num = monsterLimit.get(monster.getLevel());
                    if (num == null) {
                        monsterLimit.put(monster.getLevel(), 1);
                    } else {
                        monsterLimit.put(monster.getLevel(), num + 1);
                    }
                }
            }

            // LogHelper.GAME_DEBUG.error("monsterCount = " + monsterCount);

            Map<Pos, Boolean> freePos = new HashMap<Pos, Boolean>();
            for (int x = rangeX1; x <= rangeX2; x++) {
                for (int y = rangeY1; y <= rangeY2; y++) {
                    Pos pos = new Pos(x, y);
                    if (mapInfo.isFreePos(new Pos(x, y))) {
                        freePos.put(pos, true);
                    }
                }
            }
            // 刷每个区块的野怪
            Random random = new Random(System.nanoTime());
            List<Entity> list = new ArrayList<>();
            for (Map.Entry<Integer, Integer> monsterInfo : fortressMonsterConfig.entrySet()) {
                monsterLv = monsterInfo.getKey();
                monsterNum = monsterInfo.getValue();
                for (int i = 1; i <= monsterNum; i++) {
                    Integer currentNum = monsterLimit.get(monsterLv);
                    if (currentNum == null) {
                        currentNum = 0;
                    }

                    if (currentNum >= monsterNum) {
                        continue;
                    }

                    if (freePos.isEmpty()) {
                        continue;
                    }

                    List<Pos> keys = new ArrayList<Pos>(freePos.keySet());
                    if (keys.isEmpty()) {
                        continue;
                    }
                    Pos randomPos = keys.get(random.nextInt(keys.size()));

                    if (randomPos.isError() || !mapInfo.isFreePos(randomPos)) {
                        continue;
                    }

                    Monster monster = worldManager.addMonster(randomPos, 1000 + monsterLv, monsterLv, mapInfo, AddMonsterReason.ADD_FORTRESS_MONSTER);
                    list.add(monster);

                    monsterLimit.put(monsterLv, currentNum + 1);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            worldManager.synEntityAddRq(list);

            // LogHelper.GAME_DEBUG.error("monsterLimit = " + monsterLimit);

        }

        // LogHelper.GAME_DEBUG.error("monsterMap size = " + monsterMap.size());

    }

    public void flushFortressResource() {
        int mapId = 20;
        List<Integer> famouseCity = cityManager.getFamousCity();
        List<Integer> squareFortress = cityManager.getSquareFortress();
        List<Integer> allCity = new ArrayList<Integer>();
        allCity.addAll(famouseCity);
        allCity.addAll(squareFortress);
        allCity.add(25);
        HashBasedTable<Integer, Integer, Integer> fortressResourceConfig = staticWorldMgr.getFortressResourceConfig();
        Map<Integer, Map<Integer, Integer>> resourceTable = fortressResourceConfig.rowMap();
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.error("mapInfo is null, mapId = " + mapId);
            return;
        }
        float actFactor = activityManager.actDouble(ActivityConst.ACT_IMPERIAL_MINE_COUNT);
        float lastFactor = 1 + actFactor;
        HashBasedTable<Integer, Integer, Integer> resourceLimit = HashBasedTable.create();

        int rangeX1;
        int rangeX2;
        int rangeY1;
        int rangeY2;
        int resType;
        int resLv;
        Map<Pos, Resource> resourceMap = mapInfo.getResourceMap();
        for (Integer cityId : allCity) {
            City city = cityManager.getCity(cityId);
            if (city == null) {
                continue;
            }

            // LogHelper.GAME_DEBUG.error("cityId = " + city.getCityId());
            resourceLimit.clear();
            StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
            if (worldCity == null) {
                continue;
            }
            rangeX1 = worldCity.getRangex1();
            rangeX2 = worldCity.getRangex2();
            rangeY1 = worldCity.getRangey1();
            rangeY2 = worldCity.getRangey2();

            // 先找到这个区块所有的怪物
            // LogHelper.GAME_DEBUG.error("before resourceMap.size = " +
            // resourceMap.size());

            // 清除高级区域资源小于30%的资源点
            Iterator<Resource> iterator = resourceMap.values().iterator();
            while (iterator.hasNext()) {
                Resource r = iterator.next();

                if (r.getStatus() == 1) {
                    continue;
                }

                int resId = (int) r.getId();
                StaticWorldResource worldResource = staticWorldMgr.getStaticWorldResource(resId);
                if (worldResource == null) {
                    continue;
                }

                float left = (float) r.getCount() / (float) worldResource.getResource();
                // 大于0.3资源的比例没刷掉
                if (left > staticLimitMgr.getCenterResFactor()) {
                    continue;
                }
                worldManager.removeResPosOnly(mapInfo, r.getPos());
                worldManager.synEntityRemove(r, mapInfo.getMapId(), r.getPos()); // 同步资源
                iterator.remove();
            }

            int cityResNum = 0;
            for (Resource resource : resourceMap.values()) {
                if (resource == null) {
                    continue;
                }
                Pos pos = resource.getPos();
                if (pos.getX() >= rangeX1 && pos.getX() <= rangeX2 && pos.getY() >= rangeY1 && pos.getY() <= rangeY2) {
                    int resId = (int) resource.getId();
                    StaticWorldResource worldResource = staticWorldMgr.getStaticWorldResource(resId);
                    if (worldResource == null) {
                        LogHelper.CONFIG_LOGGER.error("worldResource is null, resId  = " + resId);
                        continue;
                    }

                    int type = worldResource.getType();

                    if (resource.getLevel() < 7 || resource.getLevel() > 10) {
                        LogHelper.CONFIG_LOGGER.error("error resLv = " + resource.getLevel());
                    }

                    Integer num = resourceLimit.get(type, resource.getLevel());
                    if (num == null) {
                        resourceLimit.put(type, resource.getLevel(), 1);
                    } else {
                        resourceLimit.put(type, resource.getLevel(), num + 1);
                    }
                    ++cityResNum;
                }
            }

//             LogHelper.GAME_DEBUG.error("cityId = " + cityId +
//             ", cityResNum = " + cityResNum);
//             LogHelper.GAME_DEBUG.error("before resourceLimit = " +
//             resourceLimit);
            Map<Pos, Boolean> freePos = new HashMap<Pos, Boolean>();
            for (int x = rangeX1; x <= rangeX2; x++) {
                for (int y = rangeY1; y <= rangeY2; y++) {
                    Pos pos = new Pos(x, y);
                    if (mapInfo.isFreePos(new Pos(x, y))) {
                        freePos.put(pos, true);
                    }
                }
            }

            // 刷每个区块的资源
            Random random = new Random(System.nanoTime());
            // 类型、等级、数量
            int configNum = 0;
            List<Entity> list = new ArrayList<>();
            for (Map.Entry<Integer, Map<Integer, Integer>> resourceInfo : resourceTable.entrySet()) {
                resType = resourceInfo.getKey();
                Map<Integer, Integer> resCount = resourceInfo.getValue();
                for (Map.Entry<Integer, Integer> elem : resCount.entrySet()) {
                    resLv = elem.getKey();
                    configNum = elem.getValue();
                    int lastNum = (int) Math.ceil(lastFactor * (float) configNum);
                    for (int i = 1; i <= lastNum; i++) {
                        Integer currentNum = resourceLimit.get(resType, resLv);
                        if (currentNum == null) {
                            currentNum = 0;
                        }

                        if (currentNum >= lastNum) {
                            continue;
                        }

                        if (freePos.isEmpty()) {
                            continue;
                        }

                        List<Pos> keys = new ArrayList<Pos>(freePos.keySet());
                        if (keys.isEmpty()) {
                            continue;
                        }

                        Pos randomPos = keys.get(random.nextInt(keys.size()));
                        if (randomPos.isError() || !mapInfo.isFreePos(randomPos)) {
                            continue;
                        }

                        Resource resource = worldManager.addResource(randomPos, resLv, resType, mapInfo);

                        // LogHelper.GAME_DEBUG.error(index + ".resourcePos = "
                        // + randomPos);
                        list.add(resource);

                        resourceLimit.put(resType, resLv, currentNum + 1);
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            worldManager.synEntityAddRq(list);
//             LogHelper.GAME_DEBUG.error("resourceLimit = " + resourceLimit);
//             LogHelper.GAME_DEBUG.error(" ------------------------------------"
//             );
        }

//         LogHelper.GAME_DEBUG.error("after resourceMap.size = " +
//         resourceMap.size());
//         LogHelper.GAME_DEBUG.error("resourceMap size = " +
//         resourceMap.size());

    }

    // 获取资源信息
    public void getResInfo(WorldPb.GetResInfoRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        CommonPb.Pos reqPos = req.getPos();
        // 检查是否有资源点
        Pos pos = new Pos(reqPos.getX(), reqPos.getY());
        int mapId = worldManager.getMapId(pos);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.POS_ERROR);
            return;
        }

        // 资源
        Resource resource = mapInfo.getResource(pos);
        if (resource == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_RESOURCE_ENTITY);
            return;
        }

        // 当前资源点是否有行军
        March march = mapInfo.getMarch(pos);
        WorldPb.GetResInfoRs.Builder builder = WorldPb.GetResInfoRs.newBuilder();
        if (march == null) {
            builder.setLeftRes((int) resource.getCount());
            builder.setLordId(0);
        } else {
            builder.setLeftRes((int) resource.getCount());
            Player target = playerManager.getPlayer(march.getLordId());
            if (target != null) {
                builder.setCountry(target.getCountry());
                builder.setLordLv(target.getLevel());
                builder.setName(target.getNick());
                List<Integer> heroIds = march.getHeroIds();
                if (heroIds != null && heroIds.size() == 1) {
                    Integer heroId = heroIds.get(0);
                    Hero hero = target.getHero(heroId);
                    if (hero != null) {
                        builder.setHeroLv(hero.getHeroLv());
                        builder.setHeroId(heroId);
                        builder.setSoldier(hero.getCurrentSoliderNum());
                        builder.setDiviNum(hero.getDiviNum());
                    }
                }
                builder.setLordId(target.roleId);
                builder.setCollectEndTime(march.getEndTime());
                builder.setPeriod(march.getPeriod());
                builder.setPortrait(target.getPortrait());
            }
        }

        handler.sendMsgToPlayer(WorldPb.GetResInfoRs.ext, builder.build());
    }

    public void getCountryWarRq(WorldPb.GetWorldCountryWarRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int cityId = req.getCityId();
        int mapId = req.getMapId();
        if (mapId == 0) {
            City city = cityManager.getCity(cityId);
            if (city == null) {
                handler.sendErrorMsgToPlayer(GameError.CITY_NOT_EXISTS);
                return;
            }
            StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
            if (worldCity == null) {
                handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
                return;
            }
            mapId = worldCity.getMapId();
        }
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

//		Map<Long, WarInfo> zergWarMap = mapInfo.getZergWarMap();
        WorldPb.GetWorldCountryWarRs.Builder builder = WorldPb.GetWorldCountryWarRs.newBuilder();

        List<IWar> countryWarList = mapInfo.getCountryCityWar(cityId);
        for (IWar war : countryWarList) {
            if (war == null) {
                continue;
            }

            CountryCityWarInfo warInfo = (CountryCityWarInfo) war;

            worldManager.handleWarSoldier(warInfo);
            builder.addWarInfo(warInfo.wrapPb(warInfo.isJoin(player)));
        }

//		// 虫族主宰战斗数据
        if (mapId == MapId.CENTER_MAP_ID) {
            mapInfo.getWarList(e -> e.getWarType() == WarType.ATTACK_ZERG).forEach(e -> {
                if (e == null) {
                    return;
                }
                ZergWarInfo zergWarInfo = (ZergWarInfo) e;
//				worldManager.handleZergWarSoldier(zergWarInfo);
                builder.addWarInfo(zergWarInfo.wrapZergPb(e.isJoin(player)));
            });
        }
        builder.setCityId(cityId);
        handler.sendMsgToPlayer(WorldPb.GetWorldCountryWarRs.ext, builder.build());
//		LogHelper.MESSAGE_LOGGER.info("GetWorldCountryWarRs :{}", builder.build());
    }

    /**
     * 发起伏击叛军
     *
     * @param req
     * @param handler
     */
    public void rebelWar(WorldPb.RebelWarRq req, RebelWarHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int pox = req.getPox();
        int poy = req.getPoy();

        // 目标点
        Pos targetPos = new Pos(pox, poy);
        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        RebelMonster rebelMonster = mapInfo.getRebelMap().get(targetPos);
        if (rebelMonster == null) {
            handler.sendErrorMsgToPlayer(GameError.REBEL_MONSTER_ERROR);
            return;
        }
        // 检查当前城池是否有战争,如果有说明已经宣战了
        WarInfo warInfo = worldManager.getRebelWar(player.getCountry(), mapInfo, rebelMonster.getId(), targetPos);
        if (warInfo != null) {
            handler.sendErrorMsgToPlayer(GameError.ALREADY_CALL_REBEL_WAR);
            return;
        }
        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_2);

        StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster((int) rebelMonster.getId());
        // 创建一个战争
        Lord lord = player.getLord();
        Pos playerPos = player.getPos();
        long period = Integer.valueOf(staticWorldMonster.getParams()) * TimeHelper.SECOND_MS;

        if (System.currentTimeMillis() + period > worldActPlan.getEndTime()) {
            handler.sendErrorMsgToPlayer(GameError.MARCH_OVER_TIME);
            return;
        }

        WarInfo rebelWar = warManager.createRebelWar(period, lord.getLordId(), player.getCountry(), staticWorldMonster.getId(), playerPos, targetPos, mapInfo);
        if (rebelWar != null) {
            Map<Long, WarInfo> countryWarMap = mapInfo.getRebelWarMap();
            countryWarMap.put(rebelWar.getWarId(), rebelWar);
        } else {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        rebelMonster.setWarId(rebelWar.getWarId());
        WorldPb.RebelWarRs.Builder builder = WorldPb.RebelWarRs.newBuilder();
        builder.setWarInfo(rebelWar.wrapPb(rebelWar.isJoin(player)));
        handler.sendMsgToPlayer(WorldPb.RebelWarRs.ext, builder.build());
        // 同步给地图上的玩家
        warManager.synRebelWarInfo(rebelWar);
    }

    public void attendRebelWar(AttendRebelWarHandler handler, WorldPb.AttendRebelWarRq req) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 查看当前pos存放的实体
        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        long warId = (long) req.getWarId();
        Map<Long, WarInfo> rebelWarMap = mapInfo.getRebelWarMap();
        WarInfo warInfo = rebelWarMap.get(warId);
        if (warInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.WAR_NOT_EXISTS);
            return;
        }
        Player attacker = playerManager.getPlayer(warInfo.getAttackerId());
        if (attacker.getCountry() != player.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.COUNTRY_ERROR);
            return;
        }
        // 检查伏击叛军是否结束
        long now = System.currentTimeMillis();
        if (warInfo.getEndTime() <= now) {
            handler.sendErrorMsgToPlayer(GameError.WAR_IS_OVER);
            return;
        }
        // 行军英雄
        List<Integer> heroIds = req.getHeroIdList();
        if (heroIds.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH_HEROS);
            return;
        }
        Map<Integer, Hero> heroMap = player.getHeros();
        // 检测英雄是否重复
        HashSet<Integer> checkHero = new HashSet<Integer>();
        // 检查英雄是否上阵
        for (Integer heroId : heroIds) {
            if (!isEmbattle(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
                return;
            }

            if (player.isHeroInMarch(heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
                return;
            }

            checkHero.add(heroId);
        }

        // 有相同的英雄出征
        if (checkHero.size() != heroIds.size()) {
            handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
            return;
        }

        // 检查英雄是否可以出征
        for (Integer heroId : heroIds) {
            Hero hero = heroMap.get(heroId);
            if (hero == null) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
                return;
            }

            if (!playerManager.isHeroFree(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
                return;
            }

            // 检查武将带兵量
            if (hero.getCurrentSoliderNum() <= 0) {
                handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
                return;
            }
        }

        // 生成行军
        March march = worldManager.createRebelMarch(player, heroIds, warInfo.getDefencerPos());
        // 检查行军是否超过战斗时间
        if (!isMarchWarOk(march.getPeriod(), warInfo)) {
            handler.sendErrorMsgToPlayer(GameError.TO_LONG_MARCH);
            return;
        }

        march.setFightTime(march.getEndTime(), MarchReason.ATTEND_REBEL_WAR);
        march.setDefencerId(warInfo.getDefencerId());
        march.setAttackerId(warInfo.getAttackerId());
        march.setSide(1);
        march.setWarId(warId);
        // 添加行军到玩家身上
        player.addMarch(march);
        // 加到世界地图中
        worldManager.addMarch(mapId, march);
        // 添加March 到warInfo
        warInfo.addAttackMarch(march);
        // 返回消息
        WorldPb.AttendRebelWarRs.Builder builder = WorldPb.AttendRebelWarRs.newBuilder();
        worldManager.handleRebelWarSoldier(warInfo);
        CommonPb.WarInfo.Builder wrapPb = warInfo.wrapPb(warInfo.isJoin(player));
        builder.setWarInfo(wrapPb);
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.AttendRebelWarRs.ext, builder.build());
        worldManager.synMarch(mapInfo.getMapId(), march);
    }

    /**
     * 拿到伏击叛军集合
     *
     * @param handler
     */
    public void getRebelWar(GetRebelWarHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 查看当前pos存放的实体
        WorldPb.GetRebelWarRs.Builder builder = WorldPb.GetRebelWarRs.newBuilder();
        player.getWarInfos().getInfos().stream().filter(x -> x.getWarType() == WarType.REBEL_WAR).forEach(warInfo -> {
            CommonPb.WarInfo.Builder wrapPb = warInfo.wrapPb(warInfo.isJoin(player));
            builder.addWarInfo(wrapPb);
        });

        handler.sendMsgToPlayer(WorldPb.GetRebelWarRs.ext, builder.build());
    }

    /**
     * 战斗支援请求
     *
     * @param req
     * @param handler
     */
    public void rebelFightHelpRq(WorldPb.RebelFightHelpRq req, RebelFightHelpHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int mapId = playerManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        long warId = req.getKeyId();
        WarInfo warInfo = mapInfo.getRebelWarMap().get(warId);
        if (warInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.WAR_END_OR_NOT_EXIST);
            return;
        }
        if (warInfo.getAttackerHelpTime() >= 1) {
            handler.sendErrorMsgToPlayer(GameError.WAR_CANNT_HELP);
            return;
        }
        if (warInfo.getAttackerId() != player.getLord().getLordId()) {
            handler.sendErrorMsgToPlayer(GameError.WAR_CANNT_HELP);
            return;
        }
        StaticWorldMonster staticMonster = staticWorldMgr.getMonster((int) warInfo.getDefencerId());
        Pos pos = warInfo.getDefencerPos();
        String tarPos = String.format("%s,%s", pos.getX(), pos.getY());
        String p[] = {staticMonster.getName(), tarPos};
        Chat chat = chatManager.createManShare(player, ChatId.HELP_REBLE_WAR, p);
        chatManager.sendMapShare(chat, mapId, player.getCountry());
        WorldPb.RebelFightHelpRs.Builder builder = WorldPb.RebelFightHelpRs.newBuilder();
        handler.sendMsgToPlayer(WorldPb.RebelFightHelpRs.ext, builder.build());
        warInfo.setAttackerHelpTime(warInfo.getAttackerHelpTime() + 1);
        warManager.synRebelWarInfo(warInfo);
    }

    /**
     * 战斗支援请求
     *
     * @param req
     * @param handler
     */
    public void rebelFightShareRq(WorldPb.RebelFightShareRq req, RebelFightShareHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int mapId = req.getMapId();
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        Pos pos = new Pos(req.getPox(), req.getPoy());
        RebelMonster rebelMonster = mapInfo.getRebelMap().get(pos);
        if (rebelMonster == null) {
            handler.sendErrorMsgToPlayer(GameError.REBEL_MONSTER_ERROR);
            return;
        }
        StaticWorldMonster staticMonster = staticWorldMgr.getMonster((int) rebelMonster.getId());
        String tarPos = String.format("%s,%s", pos.getX(), pos.getY());
        String[] p = {staticMonster.getName(), tarPos};
        Chat chat = chatManager.createManShare(player, ChatId.SHARE_REBLE_WAR, p);
        chatManager.sendMapShare(chat, mapId, player.getCountry());
        WorldPb.RebelFightShareRs.Builder builder = WorldPb.RebelFightShareRs.newBuilder();
        handler.sendMsgToPlayer(WorldPb.RebelFightShareRs.ext, builder.build());
    }

    public void findNearMonster(WorldPb.FindNearMonsterRq req, FindNearMonsterHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int mapId = playerManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        Pos pos = player.getPos();
        if (pos.getX() == -1 || pos.getY() == -1) {
            handler.sendErrorMsgToPlayer(GameError.ERROR_POS);
            return;
        }
        Entity entity = mapInfo.getEntitys(req.getLevel(), player.getPos());
        if (entity == null) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_ENTITY_NOT_EXIST);
            return;
        }
        WorldPb.FindNearMonsterRs.Builder builder = WorldPb.FindNearMonsterRs.newBuilder();
        builder.setEntity(entity.wrapPb());
        handler.sendMsgToPlayer(WorldPb.FindNearMonsterRs.ext, builder.build());
    }

    public void deliveryInit(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int need = staticLimitMgr.getNum(247);
        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan moveN = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_9);
        if (moveN != null && moveN.getState() == WorldActPlanConsts.OPEN) {
            WorldTargetTask worldTargetTask = worldData.getTasks().get(10);
            if (worldTargetTask != null) {
                need = staticLimitMgr.getNum(248);
            }
            handler.sendMsgToPlayer(WorldPb.DeliveryInitRs.ext, WorldPb.DeliveryInitRs.newBuilder().setNeed(need).build());
        } else {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
        }
    }

    public void deliveryMove(WorldPb.DeliveryRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int need = staticLimitMgr.getNum(247);
        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan moveN = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_9);
        if (moveN != null && moveN.getState() == WorldActPlanConsts.OPEN) {
            WorldTargetTask worldTargetTask = worldData.getTasks().get(WorldActivityConsts.ACTIVITY_10);
            if (worldTargetTask != null) {
                need = staticLimitMgr.getNum(248);
            }
        } else {
            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            return;
        }
        // 判断行军状态
        ConcurrentLinkedDeque<March> marches = player.getMarchList();
        if (!marches.isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
            return;
        }
        // 判断玩家位置
        Pos pos = player.getPos();
        int currentMapId = worldManager.getMapId(player);
        StaticWorldMap map = staticWorldMgr.getStaticWorldMap(currentMapId);
        // 只有平原才能迁徙
        if (map.getAreaType() != 1) {
            handler.sendErrorMsgToPlayer(GameError.MAP_ID_ERROR);
            return;
        }
        int newMapId = map.getBelong();

        // 初级迁城令
        int itemId = 30;
        Item item = player.getItem(itemId);
        int hasNum = item != null ? item.getItemNum() : 0;
        int price = 0;
        int realNeed = need;
        if (hasNum < need) {
            // 自动购买
            StaticProp staticProp = staticPropDataMgr.getStaticProp(itemId);
            if (staticProp == null) {
                return;
            }
            price = staticProp.getPrice() * (need - hasNum);
            if (player.getGold() < price) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            realNeed = hasNum;
        }
        // 查找玩家要去的高原
        int mapId = newMapId;

        // 检查mapId的状态
        int isFound = player.isMapCanMoves(mapId);
//        Map<Integer, MapStatus> mapStatuses = player.getMapStatusMap();
//        for (MapStatus status : mapStatuses.values()) {
//            if (status.getMapId() == mapId && status.getStatus() == 2) {
//                isFound = true;
//            }
//        }

        // bugs here
//        if (!isFound) {
//            handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
//            return;
//        }

        switch (isFound) {
            case 1:
                handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
                return;
            case 2:
                handler.sendErrorMsgToPlayer(GameError.CANT_MOVE);
                return;
            default:
                break;
        }

        // 可以迁城
        MapInfo mapInfo = worldManager.getMapInfo(currentMapId);
        worldManager.removePlayerCity(pos, mapInfo);
        MapInfo newMapInfo = worldManager.getMapInfo(mapId);
        Pos randPos = worldManager.givePlayerPos(newMapInfo);
        if (randPos.isError() || !newMapInfo.isFreePos(randPos)) {
            handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
            return;
        }
        playerManager.changePlayerPos(player, randPos);
        worldManager.addPlayerCity(randPos, newMapInfo, player);
        if (price > 0) {
            // 扣钻石
            playerManager.subGoldOk(player, price, Reason.MAP_MOVE);
        }
        // 扣道具
        playerManager.subAward(player, AwardType.PROP, itemId, realNeed, Reason.MAP_MOVE);

        WorldPb.MapMoveRs.Builder builder = WorldPb.MapMoveRs.newBuilder();
        builder.setGold(player.getGold());
        builder.setPos(randPos.wrapPb());
        if (item != null) {
            builder.setProp(item.wrapPb());
        }
        handler.sendMsgToPlayer(WorldPb.MapMoveRs.ext, builder.build());
        // 驻防武将回城
        worldManager.handleWallFriendReturn(player);
        // 迁城之后战斗全部删除
        worldManager.removePlayerWar(player, pos, MarchReason.MiddleMove, randPos);
    }

    public void getRebelPosRq(WorldPb.GetRebelPosRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int mapId = req.getMapId();
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.MAP_INFO_NOT_EXISTS);
            return;
        }

        Map<Pos, RebelMonster> rebelMap = mapInfo.getRebelMap();
        WorldPb.GetRebelPosRs.Builder builder = WorldPb.GetRebelPosRs.newBuilder();
        builder.setMapId(mapId);

        for (Map.Entry<Pos, RebelMonster> rebelMonsterEntry : rebelMap.entrySet()) {
            if (rebelMonsterEntry.getValue() == null) {
                continue;
            }
            Pos pos = rebelMonsterEntry.getKey();
            if (pos.isError()) {
                continue;
            }
            builder.addPos(CommonPb.TwoInt.newBuilder().setV1(pos.getX()).setV2(pos.getY()).build());
        }
        handler.sendMsgToPlayer(WorldPb.GetRebelPosRs.ext, builder.build());
    }

    /**
     * 模拟杀虫
     *
     * @param player
     * @param lv
     * @param count
     */
    public void killMonster(Player player, int lv, int count) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            try {
                SpringUtil.getBean(LoginExecutor.class).add(() -> {
                    // 出征武将
                    List<Integer> heroIds = new ArrayList<>();
                    player.getEmbattleList().forEach(e -> {
                        heroIds.add(Integer.valueOf(e));
                    });
                    // 补兵
                    Iterator<Integer> it = heroIds.iterator();
                    while (it.hasNext()) {
                        boolean hasSolider = addSolider(player, it.next());
                        if (!hasSolider) {
                            it.remove();
                        }
                    }
                    if (heroIds.size() == 0) {
                        return;
                    }
                    // 出兵消耗
                    int totalSoldier = 0;
                    for (Integer heroId : heroIds) {
                        Hero hero = player.getHero(heroId);
                        if (hero == null) {
                            continue;
                        }
                        totalSoldier += hero.getCurrentSoliderNum();
                    }
                    int distance = random.nextInt(8);
                    int oilCost = (int) Math.ceil(totalSoldier / 10) + distance * 30;
                    if (player.getResource(ResourceType.OIL) < oilCost) {
                        return;
                    }
                    SimpleData simpleData = player.getSimpleData();
                    if (simpleData.getKillRebelTimes() >= staticLimitMgr.getNum(11)) {
                        return;
                    }
                    simpleData.setKillRebelTimes(simpleData.getKillRebelTimes() + 1);

                    playerManager.subAward(player, AwardType.RESOURCE, ResourceType.OIL, oilCost, Reason.KILL_WORLD_MONSTER);
                    // 开打
                    March march = worldManager.createMarch(player, heroIds, player.getPos());
                    march.setFightTime(march.getEndTime() + 1000L, MarchReason.KillRebel);
                    march.setMarchType(MarchType.AttackMonster);
                    Optional<StaticWorldMonster> op = staticWorldMgr.getWorldMonsterMap().values().stream().filter(e -> e.getType() == 1 && e.getLevel() == lv).findFirst();
                    if (op.isPresent()) {
                        StaticWorldMonster staticMonster = op.get();
                        Monster monster = worldManager.createMonster(staticMonster.getId(), staticMonster.getLevel(), new Pos(), AddMonsterReason.ADD_PLAYER_MONSTER);
                        worldLogic.handleRebel(staticMonster, player, monster, march, new MapInfo());
                        worldLogic.synRewards(march, new MapInfo());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void killBigMonster(Player player, int lv) {
        if (player.getLevel() < 35) {
            return;
        }
        try {
            SpringUtil.getBean(LoginExecutor.class).add(() -> {
                // 出征武将
                List<Integer> heroIds = gmKillBigMonster(player);
                if (heroIds.isEmpty()) {
                    return;
                }

                int mapId = worldManager.getMapId(player.getPos());
                MapInfo mapInfo = worldManager.getMapInfo(mapId);

                int posX = player.getPosX();
                int posY = player.getPosY();

                int minX = Math.max(0, posX - 5);
                int maxX = Math.min(500, posX + 5);
                int minY = Math.max(0, posY - 5);
                int maxY = Math.min(500, posY + 5);
                Pos checkPos = new Pos();
                Pos targetPos = null;
                for (int i = minX; i <= maxX; i++) {
                    for (int j = minY; j <= maxY; j++) {
                        checkPos.setPos(i, j);
                        if (mapInfo.isFreePos(checkPos)) {
                            targetPos = new Pos(i, j);
                            break;
                        }
                    }
                }
                if (targetPos == null) {
                    return;
                }

                March march = worldManager.createMarch(player, heroIds, targetPos);
                march.setPeriod(1000L);
                march.setEndTime(System.currentTimeMillis() + 10000L);
                march.setFightTime(march.getEndTime() + 10000L, MarchReason.KillRebel);
                march.setMarchType(MarchType.BigWar);
                mapInfo.addMarch(march);
                worldManager.synMarch(mapId, march);
                Optional<StaticGiantZerg> op = staticWorldMgr.getGiantZergMap().values().stream().filter(e -> e.getType() == 1 && e.getLevel() == lv).findAny();
                if (op.isPresent()) {
                    StaticGiantZerg staticGiantZerg = op.get();

                    BigMonster bigMonster = worldManager.addBigMonster(targetPos, staticGiantZerg.getId(), staticGiantZerg.getLevel(), mapInfo, AddMonsterReason.ADD_BIG_MONSTER);

                    bigMonster.setMapId(mapInfo.getMapId());
                    bigMonster.setLeaveTime(march.getFightTime() + 120000);
                    bigMonster.setState(EntityState.SURVIVAL.get());
                    bigMonster.setSoldierType(staticGiantZerg.getSoldierType());

                    StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster(Long.valueOf(staticGiantZerg.getId()).intValue());
                    if (staticWorldMonster == null) {
                        LogHelper.CONFIG_LOGGER.error("staticGiantZerg error->[{}]", staticGiantZerg.getId());
                        return;
                    }
                    List<Entity> monsterList = new ArrayList<>();
                    Team monsterTeam = battleMgr.initMonsterTeam(staticWorldMonster.getMonsterIds(), BattleEntityType.BIG_MONSTER);
                    bigMonster.setTeam(monsterTeam);
                    bigMonster.setTotalHp(monsterTeam.getLessSoldier());
                    monsterList.add(bigMonster);

                    worldManager.synEntityAddRq(monsterList);
                    List<Player> others = playerManager.getPlayers().values().stream().filter(e -> e.getCountry() == player.getCountry() && e.getLevel() > 35).collect(Collectors.toList());
                    int addCount = 0;
                    for (Player e : others) {
                        if (addCount >= 1) {
                            break;
                        }
                        List<Integer> list = gmKillBigMonster(e);
                        if (list.isEmpty()) {
                            continue;
                        }
                        addCount++;
                        March marchother = worldManager.createMarch(e, list, targetPos);
                        marchother.setPeriod(1000L);
                        marchother.setEndTime(System.currentTimeMillis() + 1000L);
                        marchother.setFightTime(marchother.getEndTime() + 10000L, MarchReason.KillRebel);
                        marchother.setMarchType(MarchType.BigWar);
                        mapInfo.addMarch(marchother);
                        worldManager.synMarch(mapId, marchother);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Integer> gmKillBigMonster(Player player) {
        List<Integer> heroIds = new ArrayList<>();
        player.getEmbattleList().forEach(e -> {
            heroIds.add(Integer.valueOf(e));
        });
        // 补兵
        Iterator<Integer> it = heroIds.iterator();
        while (it.hasNext()) {
            boolean hasSolider = addSolider(player, it.next());
            if (!hasSolider) {
                it.remove();
            }
        }
        if (heroIds.size() == 0) {
            return heroIds;
        }
        // 出兵消耗
        int totalSoldier = 0;
        for (Integer heroId : heroIds) {
            Hero hero = player.getHero(heroId);
            if (hero == null) {
                continue;
            }
            totalSoldier += hero.getCurrentSoliderNum();
        }
        Random random = new Random();
        int distance = random.nextInt(8);
        int oilCost = (int) Math.ceil(totalSoldier / 10) + distance * 30;
        if (player.getResource(ResourceType.OIL) < oilCost) {
            return new ArrayList<>();
        }
        return heroIds;
    }

    private boolean addSolider(Player player, int heroId) {
        // 给英雄补兵
        StaticHero staticHero = staticHeroMgr.getStaticHero(heroId);
        if (staticHero == null) {
            return false;
        }
        int soldierType = staticHero.getSoldierType();

        // 获取当前的兵力
        int currentSoldier = soldierManager.getSoldierNum(player, soldierType);
        if (currentSoldier <= 0) {
            return false;
        }
        Hero hero = player.getHero(heroId);
        // 应该增加的英雄的兵力
        heroManager.caculateProp(hero, player);
        int addSoldierNum = hero.getSoldierNum() - hero.getCurrentSoliderNum();
        int diff = Math.min(currentSoldier, addSoldierNum);
        if (diff <= 0) {
            return true;
        }
        playerManager.subAward(player, AwardType.SOLDIER, soldierType, diff, Reason.ADD_SOLDIER);
        hero.setCurrentSoliderNum(hero.getCurrentSoliderNum() + diff);
        return true;
    }

    @Autowired
    StaticSuperResMgr staticSuperResMgr;
    @Autowired
    SuperResService superResService;

    private Lock lock = new ReentrantLock();

    /**
     * 阵营建设
     *
     * @param req
     * @param handler
     */
    public void devFortressRq(WorldPb.GetForTressBuildRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 检查城池的合法性
        int cityId = req.getCityId();
        StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
        if (worldCity == null || worldCity.getType() != CityType.SQUARE_FORTRESS) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            LogHelper.CONFIG_LOGGER.error("cityId = " + cityId + " config error!");
            return;
        }
        City city = cityManager.checkAndGetHome(player.getCountry());
        if (city == null || city.getCityId() != cityId) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            LogHelper.CONFIG_LOGGER.error("city1 = " + city + " config error!");
            return;
        }
        // 检查是否跨区域
        int cityMapId = worldCity.getMapId();
        int mapId = worldManager.getMapId(player);
        if (mapId != cityMapId) {
            handler.sendErrorMsgToPlayer(GameError.DONOT_CROSS_MAP_DEV_CITY);
            return;
        }
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            return;
        }
        Nation fortessNation = player.getFortessNation();
        if (fortessNation.getRefreshTime() != GameServer.getInstance().currentDay) {
            fortessNation.setBuild(0);
            fortessNation.setRefreshTime(GameServer.getInstance().currentDay);
        }
        StaticFortressBuild staticCityDev = staticSuperResMgr.getFortressBuild(fortessNation.getBuild() + 1);
        if (staticCityDev == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }
        List<List<Integer>> needProp = staticCityDev.getNeedProp();
        if (needProp != null && !needProp.isEmpty()) {
            boolean flag = playerManager.checkAndSubItem(player, needProp, Reason.USE_ITEM);
            if (!flag) {
                handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
                return;
            }
        }
        fortessNation.setBuild(fortessNation.getBuild() + 1);// 增加一次建设
        // int need = 0;// 需要刷新的大型矿点
        // int total = 0;
        List<SuperResource> superResources = mapInfo.getSuperResMap().computeIfAbsent(player.getCountry(), x -> new ArrayList<>());
        int exp = staticCityDev.getExp();// 建设经验
        lock.lock();
        try {
            int exp1 = city.getExp() + exp;
            int brfLev = city.getCityLv();
            StaticFortressLv fortressBuild = staticSuperResMgr.getStaticCityDev(city.getCityLv() + 1);
            if (fortressBuild == null || exp1 < fortressBuild.getExp()) {
                city.setExp(exp1);
            } else {
                while (exp1 >= fortressBuild.getExp()) {
                    city.setCityLv(city.getCityLv() + 1);
                    exp1 -= fortressBuild.getExp();
                    city.setExp(exp1);
                    fortressBuild = staticSuperResMgr.getStaticCityDev(city.getCityLv() + 1);
                    if (fortressBuild == null) {
                        break;
                    }
                }
            }
            WorldPb.GetForTressBuildRs.Builder builder = WorldPb.GetForTressBuildRs.newBuilder();// 建设成功后返回
            builder.setCityId(cityId);
            builder.setBuildNum(player.getFortessNation().getBuild());
            builder.setCountry(city.getCountry());
            builder.setLv(city.getCityLv());
            builder.setExp(city.getExp());
            if (fortressBuild == null) {
                fortressBuild = staticSuperResMgr.getStaticCityDev(city.getCityLv());
            }
            if (fortressBuild != null) {
                builder.setMaxExp(fortressBuild.getExp());
            }
            List<List<Integer>> award1 = staticCityDev.getAward();// 建设奖励
            award1.forEach(x -> {
                int keyId = playerManager.addAward(player, x.get(0), x.get(1), x.get(2), Reason.FORTRESS_BUILD);
                CommonPb.Award.Builder award = CommonPb.Award.newBuilder();
                award.setType(x.get(0));
                award.setId(x.get(1));
                award.setCount(x.get(2));
                award.setKeyId(keyId);
                builder.addAward(award);
            });
            needProp.forEach(x -> {
                CommonPb.Prop.Builder builder1 = CommonPb.Prop.newBuilder();
                x.forEach(prop -> {
                    builder1.setPropId(x.get(1));
                    Item item = player.getItem(x.get(1));
                    builder1.setPropNum(item == null ? 0 : item.getItemNum());
                });
                builder.addProp(builder1);
            });
            if (brfLev != city.getCityLv()) {
                StaticFortressLv cruDev = staticSuperResMgr.getStaticCityDev(city.getCityLv());
                int need = 0;
                if (cruDev != null) {
                    need = cruDev.getResourceNum() - superResources.size();
                }
                Map<Pos, Integer> emptyPos = superResService.calcSuperMinePos(mapInfo, need, city.getCountry(), city, cruDev.getResourceNum(), superResources);
                List<Entity> list = new ArrayList<>();
                for (Map.Entry<Pos, Integer> kv : emptyPos.entrySet()) {
                    Pos pos = kv.getKey();
                    StaticSuperRes sSm = staticSuperResMgr.getSuperMineRandom();
                    SuperResource resource = new SuperResource(pos, sSm, kv.getValue(), city.getCountry());
                    worldManager.addSuperResource(mapInfo, pos, resource);// 添加到地图
                    superResources.add(resource);// 添加到阵营
                    list.add(resource);
                }
                worldManager.synEntityAddRq(list);
            }
            superResources.forEach(x -> {
                CommonPb.SuperResource.Builder builder1 = CommonPb.SuperResource.newBuilder();
                builder1.setResId(x.getResId());
                if (x.getState() != SuperResource.STATE_RESET) {
                    builder1.setPos(x.getPos().wrapPb());
                }
                builder1.setNum(x.getCollectArmy().size());
                StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(x.getResId());
                builder1.setMaxNum(staticSuperRes != null ? staticSuperRes.getCollectNum() : 4);
                builder1.setState(x.getState());
                builder1.setTime(x.getNextTime());
                builder1.setCityId(x.getCityId());
                builder.addResource(builder1);

            });
            handler.sendMsgToPlayer(WorldPb.GetForTressBuildRs.ext, builder.build());
            if (city.getCityLv() != brfLev) {
                WorldPb.SynMapCityRq.Builder builder1 = WorldPb.SynMapCityRq.newBuilder();
                CommonPb.CityOwnerInfo.Builder cityOwnerInfo = worldManager.createCityOwner(city);
                cityOwnerInfo.setCanAttendElection(false);
                WorldPb.SynMapCityRq msg = builder1.setInfo(cityOwnerInfo).build();
                playerManager.getOnlinePlayer().forEach(e -> {
                    playerManager.synMapCityRq(e, msg);
                });
            }

        } catch (Exception e) {

        } finally {
            lock.unlock();
        }


    }

    // 拉取要塞数据
    public void queryFortressInfo(WorldPb.GetForTressRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            return;
        }
        int cityId = rq.getCityId();
        City city = cityManager.getCity(cityId);
        if (city == null) {
            return;
        }
//        City city1 = cityManager.checkAndGetHome(player.getCountry());
//        if (city1 == null) {
//            return;
//        }
        StaticWorldCity staticWorldCity = staticWorldMgr.getCity(city.getCityId());
        if (staticWorldCity == null) {
            return;
        }
        StaticFortressLv fortressBuild = staticSuperResMgr.getStaticCityDev(city.getCityLv() + 1);

        MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
        if (mapInfo == null) {
            return;
        }

        Nation fortessNation = player.getFortessNation();
        if (fortessNation.getRefreshTime() != GameServer.getInstance().currentDay) {
            fortessNation.setBuild(0);
            fortessNation.setRefreshTime(GameServer.getInstance().currentDay);
        }
        WorldPb.GetForTressRs.Builder builder = WorldPb.GetForTressRs.newBuilder();
        builder.setCityId(cityId);
        builder.setPos(CommonPb.Pos.newBuilder().setX(staticWorldCity.getX()).setY(staticWorldCity.getY()));
        builder.setCountry(city.getCountry());
        builder.setLv(city.getCityLv());
        builder.setExp(city.getExp());
        builder.setBuildNum(player.getFortessNation().getBuild());
        builder.setName(city.getCityName() == null ? staticWorldCity.getName() : city.getCityName());

        if (fortressBuild == null) {
            fortressBuild = staticSuperResMgr.getStaticCityDev(city.getCityLv());
        }
        if (fortressBuild != null) {
            builder.setMaxExp(fortressBuild.getExp());
        }
        // 自己的要塞
        if (city.getCountry() == player.getCountry()) {
            List<SuperResource> superResources = mapInfo.getSuperResMap().computeIfAbsent(player.getCountry(), x -> new ArrayList<>());
            superResources.forEach(x -> {
                CommonPb.SuperResource.Builder builder1 = CommonPb.SuperResource.newBuilder();
                builder1.setResId(x.getResId());
                if (x.getState() != SuperResource.STATE_RESET) {
                    builder1.setPos(x.getPos().wrapPb());
                }
                builder1.setNum(x.getCollectArmy().size());
                StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(x.getResId());
                builder1.setMaxNum(staticSuperRes != null ? staticSuperRes.getCollectNum() : 4);
                builder1.setState(x.getState());
                builder1.setTime(x.getNextTime());
                builder1.setCityId(x.getCityId());
                builder.addResource(builder1);

            });
        } else {
            if (city.getCountry() != 0) {
                List<SuperResource> superResources = mapInfo.getSuperResMap().computeIfAbsent(city.getCountry(), x -> new ArrayList<>());
                Map<Integer, Map<Integer, List<SuperResource>>> collect = superResources.stream().filter(x -> x.getCityId() != 0).collect(Collectors.groupingBy(SuperResource::getCityId, Collectors.groupingBy(SuperResource::getResType)));
                collect.forEach((x, y) -> {
                    CommonPb.SuperResourceInfo.Builder builder1 = CommonPb.SuperResourceInfo.newBuilder();
                    builder1.setCityId(x);
                    y.forEach((a, b) -> {
                        CommonPb.TwoInt.Builder builder2 = CommonPb.TwoInt.newBuilder();
                        builder2.setV1(a);
                        builder2.setV2(b.size());
                        builder1.addRes(builder2);
                    });
                    builder.addResourceInfo(builder1);
                });
            }
        }
        handler.sendMsgToPlayer(WorldPb.GetForTressRs.ext, builder.build());
    }

    public void updateFortressName(WorldPb.GetForTressUpNameRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            return;
        }
        City city1 = cityManager.checkAndGetHome(player.getCountry());
        if (city1 == null) {
            return;
        }
        String name = req.getName();
        if (!playerManager.isNickOk(name)) {
            handler.sendErrorMsgToPlayer(GameError.SENSITIVE_WORD);
            return;
        }
        List<Integer> addtion = staticLimitMgr.getAddtion(353);
        Item item = player.getItem(addtion.get(1));
        int needGold = 0;
        if (req.getCost() == 1) {
            if (item == null || item.getItemNum() < addtion.get(2)) {
                handler.sendErrorMsgToPlayer(GameError.PROP_NOT_ENOUGH);
                return;
            }
            playerManager.subAward(player, addtion.get(0), addtion.get(1), addtion.get(2), Reason.USE_ITEM);
        } else {
            int num = 0;
            boolean flag = false;
            if (item != null && item.getItemNum() < addtion.get(2)) {
                num = addtion.get(2) - item.getItemNum();
                flag = true;

            }
            StaticProp staticProp = staticPropDataMgr.getStaticProp(addtion.get(1));
            if (staticProp == null) {
                handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
                return;
            }
            // 检测物品是否能够使用
            if (staticProp.getCanUse() != ItemUse.CAN_USE) {
                handler.sendErrorMsgToPlayer(GameError.ITEM_CAN_NOT_USE);
                return;
            }
            // 检查物品价格
            needGold = staticProp.getPrice() * num;
            int owned = player.getGold();
            if (owned < needGold) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            if (flag) {
                playerManager.subAward(player, addtion.get(0), addtion.get(1), item.getItemNum(), Reason.USE_ITEM);
            }
            playerManager.subAward(player, AwardType.GOLD, 0, needGold, Reason.USE_ITEM);
        }
        city1.setCityName(name);
        WorldPb.GetForTressUpNameRs.Builder builder = WorldPb.GetForTressUpNameRs.newBuilder();
        builder.setName(name);
        builder.setProp(CommonPb.Prop.newBuilder().setPropId(addtion.get(1)).setPropNum(item.getItemNum()));
        builder.setGold(player.getGold());
        builder.setCost(req.getCost());
        handler.sendMsgToPlayer(WorldPb.GetForTressUpNameRs.ext, builder.build());

        WorldPb.SynMapCityRq.Builder builder1 = WorldPb.SynMapCityRq.newBuilder();
        CommonPb.CityOwnerInfo.Builder cityOwnerInfo = worldManager.createCityOwner(city1);
        cityOwnerInfo.setCanAttendElection(false);
        WorldPb.SynMapCityRq msg = builder1.setInfo(cityOwnerInfo).build();
        playerManager.getOnlinePlayer().forEach(e -> {
            playerManager.synMapCityRq(e, msg);
        });
    }

    /**
     * 处理大型矿点主动撤军的情况
     *
     * @param req
     * @param handler
     */
    public void marchCancelSuperRes(WorldPb.MarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        int keyId = req.getKeyId();
        March march = player.getMarch(keyId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }
        GameError marchState = marchManager.checkMarchState(march);
        if (marchState != GameError.OK) {
            handler.sendErrorMsgToPlayer(marchState);
            return;
        }
        MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
        Pos endPos = march.getEndPos();
        SuperResource resource = mapInfo.getSuperPosResMap().get(endPos);
        long now = System.currentTimeMillis();
        if (march.getMarchType() == MarchType.SUPER_ASSIST) {
            superResService.retreatHelpArmy(mapInfo, march);
        } else if (march.getMarchType() == MarchType.SUPER_COLLECT) {
            if (resource != null) {
                Iterator<SuperGuard> iterator = resource.getCollectArmy().iterator();
                while (iterator.hasNext()) {
                    SuperGuard superGuard = iterator.next();
                    if (superGuard.getMarch() == march) {
                        int count = superResService.finishCollect(mapInfo, superGuard, now, resource);
                        Hero hero = player.getHero(superGuard.getMarch().getHeroIds().get(0));
                        battleMailMgr.sendCollectDone(MailId.COLLECT_CANCEL, resource, superGuard.calcCollectedTime(now), count, hero.getHeroId(), hero.getHeroLv(), player, false, null);
                        iterator.remove();
                    }
                }
                StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(resource.getResId());
                resource.reCalcAllCollectArmyTime(now, staticSuperRes);// 重新计算分布时间
            } else {
                marchManager.handleMarchReturn(march, MarchReason.CollectDone);
                worldManager.synMarch(mapInfo.getMapId(), march);
            }
        }
        // 回城
        WorldPb.MarchCancelRs.Builder builder = WorldPb.MarchCancelRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WorldPb.MarchCancelRs.ext, builder.build());
    }

    // 获取所有地图中心城市的归属
    public void getAllMainCityCountryRq(ClientHandler handler) {
        GetAllMainCityCountryRs.Builder builder = GetAllMainCityCountryRs.newBuilder();
        for (StaticWorldMap staticWorldMap : staticWorldMgr.getWorldMap().values()) {
            if (staticWorldMap == null) {
                continue;
            }
            int mapId = staticWorldMap.getMapId();
            int centerCityId = staticWorldMap.getCenterCityId();
            City city = cityManager.getCity(centerCityId);
            if (city == null) {
                continue;
            }
            // v1:mapId v2:cityId v3:主城国家
            ThreeInt.Builder builder1 = ThreeInt.newBuilder();
            builder1.setV1(mapId);
            builder1.setV2(centerCityId);
            builder1.setV3(city.getCountry());
            builder.addInfo(builder1);
        }
        handler.sendMsgToPlayer(WorldPb.GetAllMainCityCountryRs.ext, builder.build());
    }

    public void searchResource(WorldPb.SearchEntityRq rq, ClientHandler clientHandler) {
        Player player = playerManager.getPlayer(clientHandler.getRoleId());
        if (player == null) {
            clientHandler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Entity entity = null;
        SimpleData simpleData = player.getSimpleData();
        List<Pos> searchPos = simpleData.getSearchPos();
        try {
            int entityType = rq.getEntityType();
            int level = rq.getLevel();
            int resourceType = rq.getResourceType();
            MapInfo mapInfo = worldManager.getMapInfo(player.getLord().getMapId());

            Pos pos = player.getPos();
            switch (entityType) {
                case EntityType.Monster:
                    Collection<Monster> values = mapInfo.getMonsterMap().values();

                    List<Monster> collect = values.stream().filter(monster -> monster.getLevel() == level && !searchPos.contains(monster.getPos()) && mapInfo.getMarchByPos(monster.getPos()) == null).collect(Collectors.toList());
                    Iterator<Monster> iterator = collect.iterator();
                    while (iterator.hasNext()) {
                        Monster next = iterator.next();
                        next.setDistance(pos);
                        if (entity == null) {
                            entity = next;
                        } else {
                            if (next.getDistance() < entity.getDistance()) {
                                entity = next;
                            }
                        }
                    }
                    break;
                case EntityType.Resource:
                    Collection<Resource> values1 = mapInfo.getResourceMap().values();
                    List<Resource> collect1 = values1.stream().filter(resource -> resource.getType() == resourceType && resource.getLevel() == level && resource.getPlayer() == null && !searchPos.contains(resource.getPos())).collect(Collectors.toList());
                    Iterator<Resource> iterator1 = collect1.iterator();
                    while (iterator1.hasNext()) {
                        Resource next = iterator1.next();
                        next.setDistance(pos);
                        if (entity == null) {
                            entity = next;
                        } else {
                            if (next.getDistance() < entity.getDistance()) {
                                entity = next;
                            }
                        }
                    }
                    break;
                case EntityType.BIG_RESOURCE:
                    List<SuperResource> superResources = mapInfo.getSuperResMap().get(player.getCountry());
                    if (superResources != null) {
                        List<SuperResource> collect2 = superResources.stream().filter(resource -> resource.getState() == SuperResource.STATE_PRODUCED && resource.getResType() == resourceType && resource.getCollectArmy().size() < 4 && !searchPos.contains(resource.getPos())).collect(Collectors.toList());
                        Iterator<SuperResource> iterator3 = collect2.iterator();
                        while (iterator3.hasNext()) {
                            SuperResource next = iterator3.next();
                            next.setDistance(pos);
                            if (entity == null) {
                                entity = next;
                            } else {
                                if (next.getDistance() < entity.getDistance()) {
                                    entity = next;
                                }
                            }
                        }
                    }
                    break;
                case EntityType.BIG_MONSTER:
                    List<BigMonster> collect3 = mapInfo.getBigMonsterMap().values().stream().filter(bigMonster -> bigMonster.getLevel() == level && !searchPos.contains(bigMonster.getPos())).collect(Collectors.toList());
                    Iterator<BigMonster> iterator4 = collect3.iterator();
                    while (iterator4.hasNext()) {
                        BigMonster next = iterator4.next();
                        next.setDistance(pos);
                        if (entity == null) {
                            entity = next;
                        } else {
                            if (next.getDistance() < entity.getDistance()) {
                                entity = next;
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e) {

        } finally {
            WorldPb.SearchEntityRs.Builder builder = WorldPb.SearchEntityRs.newBuilder();
            CommonPb.Pos.Builder builder1 = CommonPb.Pos.newBuilder();
            if (entity != null) {
                builder1.setX(entity.getPos().getX());
                builder1.setY(entity.getPos().getY());
                builder.setPos(builder1);
                simpleData.getSearchPos().add(entity.getPos());
            }
            clientHandler.sendMsgToPlayer(WorldPb.SearchEntityRs.ext, builder.build());
            if (TimeHelper.curentTime() - simpleData.getSearchTime() > 10000) {
                if (simpleData.getSearchTime() != 0) {
                    simpleData.getSearchPos().clear();
                }
                simpleData.setSearchTime(TimeHelper.curentTime());
            }
        }
    }

    public void handleMiddleFLameMove(WorldPb.MapMoveRq req, ClientHandler handler, FlamePlayer flamePlayer) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        // 指定mapId随机
        CommonPb.Pos pos = req.getPos();
        int mapId = worldManager.getMapId(pos);

        // 检查mapId的状态
        int isFound = player.isMapCanMoves(mapId);
        switch (isFound) {
            case 1:
                handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
                return;
            case 2:
                handler.sendErrorMsgToPlayer(GameError.CANT_MOVE);
                return;
            default:
                break;
        }
        int itemId = req.getPropId();
        boolean b = flameWarManager.subProp(flamePlayer, itemId, 1);
        if (!b) {
            return;
        }
        FlameMap flameMap = flameWarManager.getFlameMap();
        // 可以迁城
        Pos playerPos = player.getPos();
        int currentMapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(currentMapId);
        PlayerCity playerCity = worldManager.removePlayerCity(playerPos, mapInfo);
        if (currentMapId == MapId.FIRE_MAP && playerCity != null) {
            flameMap.removeNode(playerCity);
        }
        MapInfo newMapInfo = worldManager.getMapInfo(mapId);
        Pos randPos = worldManager.givePlayerPos(newMapInfo);
        if (currentMapId == MapId.FIRE_MAP) {
            randPos = flameMap.getPos(0);
        }
        if (randPos.isError()) {
            return;
        }
        playerManager.changePlayerPos(player, randPos);
        PlayerCity playerCity1 = worldManager.addPlayerCity(randPos, newMapInfo, player);
        if (currentMapId == MapId.FIRE_MAP) {
            flameMap.addNode(playerCity1);
        }
        WorldPb.MapMoveRs.Builder builder = WorldPb.MapMoveRs.newBuilder();
        // playerManager.subGoldOk(player, price, Reason.MAP_MOVE);
        builder.setGold(player.getGold());
        builder.setPos(randPos.wrapPb());
        Item item = player.getItem(req.getPropId());
        if (item != null) {
            builder.setProp(item.wrapPb());
        }
        handler.sendMsgToPlayer(WorldPb.MapMoveRs.ext, builder.build());
        // 驻防武将回城
        worldManager.handleWallFriendReturn(player);
        // 迁城之后战斗全部删除
        worldManager.removePlayerWar(player, playerPos, MarchReason.MiddleMove, randPos);
    }

    public void handleHighFlameMove(WorldPb.MapMoveRq req, ClientHandler handler, FlamePlayer flamePlayer) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        // 检查mapId的状态
        // 指定mapId随机
        CommonPb.Pos pos = req.getPos();
        int mapId = worldManager.getMapId(pos);
        int isFound = player.isMapCanMoves(mapId);
        switch (isFound) {
            case 1:
                handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
                return;
            case 2:
                handler.sendErrorMsgToPlayer(GameError.CANT_MOVE);
                return;
            default:
                break;
        }
        // 检查位置是否被占用
        Pos targetPos = new Pos(pos.getX(), pos.getY());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        FlameMap flameMap = null;
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.error("mapInfo is null");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        if (mapId != MapId.FIRE_MAP) {
            if (!mapInfo.isFreePos(targetPos)) {
                handler.sendErrorMsgToPlayer(GameError.POS_IS_TAKEN);
                return;
            }
        } else {
            flameMap = (FlameMap) mapInfo;
            boolean flag = false;
            Map<Integer, List<Pos>> safePos = flameMap.getSafePos();
            Set<Map.Entry<Integer, List<Pos>>> entries = safePos.entrySet();
            for (Map.Entry<Integer, List<Pos>> entry : entries) {
                Integer key = entry.getKey();
                if (key != player.getCountry() && entry.getValue().contains(targetPos)) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                handler.sendErrorMsgToPlayer(GameError.FIRE_NOT_MOVE_SAFE);
                return;
            }
            Entity node = flameMap.getNode(targetPos);
            if (node != null) {
                handler.sendErrorMsgToPlayer(GameError.FIRE_NOT_MOVE_SAFE);
                return;
            }
        }
        int itemId = req.getPropId();
        boolean b = flameWarManager.subProp(flamePlayer, itemId, 1);
        if (!b) {
            return;
        }
        // 可以迁城
        Pos playerPos = player.getPos();
        // LogHelper.GAME_DEBUG.error("playerPos = " + targetPos);

        // 当前玩家的地图
        int currentMapId = worldManager.getMapId(player);
        MapInfo currentMapInfo = worldManager.getMapInfo(currentMapId);
        PlayerCity playerCity = worldManager.removePlayerCity(playerPos, currentMapInfo);
        if (currentMapId == MapId.FIRE_MAP && playerCity != null) {
            flameMap.removeNode(playerCity);
        }
        playerManager.changePlayerPos(player, targetPos);

        PlayerCity playerCity1 = worldManager.addPlayerCity(targetPos, mapInfo, player);
        if (currentMapId == MapId.FIRE_MAP) {
            flameMap.addNode(playerCity1);
        }

        WorldPb.MapMoveRs.Builder builder = WorldPb.MapMoveRs.newBuilder();

        // playerManager.subGoldOk(player, price, Reason.MAP_MOVE);
        builder.setGold(player.getGold());
        builder.setPos(targetPos.wrapPb());
        Item item = player.getItem(req.getPropId());
        if (item != null) {
            builder.setProp(item.wrapPb());
        }

        handler.sendMsgToPlayer(WorldPb.MapMoveRs.ext, builder.build());

        // 驻防武将回城
        worldManager.handleWallFriendReturn(player);
        // 先返回部队
        worldManager.removePlayerWar(player, playerPos, MarchReason.HighMove, targetPos);
        // 再删除战斗
        // worldManager.removeAllWar(player);

        eventManager.highMove(player, Lists.newArrayList(currentMapId, mapId));

    }
}
