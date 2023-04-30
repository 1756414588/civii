package com.game.service;

import com.game.chat.domain.ManChat;
import com.game.constant.AwardType;
import com.game.constant.MapId;
import com.game.constant.MarchState;
import com.game.constant.Reason;
import com.game.constant.WarType;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticBuildingMgr;
import com.game.dataMgr.StaticEquipDataMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticSuperResMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.dataMgr.StaticWorldNewTargetMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.Award;
import com.game.domain.p.City;
import com.game.domain.p.Equip;
import com.game.domain.p.MapStatus;
import com.game.domain.p.SimpleData;
import com.game.domain.p.TD;
import com.game.domain.p.WorldTargetTask;
import com.game.domain.s.StaticEquip;
import com.game.domain.s.StaticFortressLv;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticSuperRes;
import com.game.domain.s.StaticWorldCity;
import com.game.domain.s.StaticWorldNewTarget;
import com.game.manager.ActManoeuvreManager;
import com.game.manager.BroodWarManager;
import com.game.manager.BuildingManager;
import com.game.manager.ChatManager;
import com.game.manager.CityManager;
import com.game.manager.DailyTaskManager;
import com.game.manager.ItemManager;
import com.game.manager.MissionManager;
import com.game.manager.PlayerManager;
import com.game.manager.ServerManager;
import com.game.manager.WorldManager;
import com.game.pb.BasePb;
import com.game.pb.ChatPb;
import com.game.pb.CommonPb;
import com.game.pb.EquipPb;
import com.game.pb.RolePb;
import com.game.pb.WorldPb;
import com.game.server.GameServer;
import com.game.service.TDService.OpenState;
import com.game.uc.Server;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Pos;
import com.game.worldmap.SuperResource;
import com.game.worldmap.fight.IWar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @date 2020/1/7 19:42
 * @description
 */
@Service
public class MijiService {

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private WorldActPlanService worldActPlanService;

    @Autowired
    private WorldTargetTaskService worldTargetTaskService;

    @Autowired
    private StaticBuildingMgr staticBuildingMgr;

    @Autowired
    private TechService techService;

    @Autowired
    private StaticWorldNewTargetMgr staticWorldNewTargetMgr;

    @Autowired
    private MissionManager missionManager;

    @Autowired
    private KillEquipService killEquipService;
    @Autowired
    private StaticPropMgr staticPropMgr;
    @Autowired
    private StaticEquipDataMgr staticEquipDataMgr;
    @Autowired
    private TDService tdService;
    @Autowired
    private HeroService heroService;
    @Autowired
    private WorldService worldService;
    @Autowired
    private ChatManager chatManager;
    @Autowired
    private DailyTaskManager dailyTaskManager;
    @Autowired
    private ActManoeuvreManager actManoeuvreManager;
    @Autowired
    private BroodWarManager broodWarManager;
    @Autowired
    private FishingService fishingService;
    /**
     * int LORD_PROPERTY = 1; // 角色属性 int GOLD = 2; // 金币 int RESOURCE = 3; // 资源 { IRON = 1;// 铁,int COPPER = 2;// 铜,OIL = 3;// 油;int STONE = 4;// 钻石 int SOLDIER = 4; // 士兵 int PROP = 5; // 道具 int HERO = 6; // 掉落武将 int EQUIP = 7; // 装备 int AUTO_WAR_SOILDER = 8; // 城防自动补充 int AUTO_BUILD = 9; // 自动建造 int HERO_EXP = 10; // 武将经验 int KILL_BUILD_RECRUIT_CD = 11; // 秒建造或者招募cd int PERSON = 12; // 人口 int SPEED_SOLDIER = 15; // 招募加速 int BUILD_LV = 101; // 建筑等级 int EMPTY_LOOT = 16; // 空掉落 int COMMERCIAL_TEAM_TIME = 17; // 商业建造队时长 int ICON = 18; // 头像 int ADD_BUFF = 19; // 伤害加深 int STAFF_SOLDIER = 20; // 预备役士兵 int RIOT_ITEM = 21; // 暴乱战利品 int RIOT_SCORE = 22; // 暴乱积分 int COLLECT_TIMES = 23; // 征收次数
     *
     * @giveMe type, id, count (资源类没有id的传0,对应的type的id去各自的表找，暂时资源道具装备是可以实时更新的，不需要重启客户端,其他要重启客户端生效)
     */
    private static final String GIAVE_ME = "@giveMe";
    /**
     * 打开世界进程 ，开同步开启世界活动，正正常流程 读s_world_act_plan 开启世界活动
     *
     * @openWorldTarget targetId (传世界进程id)
     */
    private static final String OPEN_WORLD_TARGET = "@openWorldTarget";

    private static final String CLEAR_WORLD_TARGET = "@clearWorldTarget";

    /**
     * 强制打开母巢之战 (任何情况下强制打开母巢之战 仅仅提供测试用)
     */
    private static final String OPEN_WORLD_PVP = "@openWorldPvp";

    /**
     * 一键打开所有地图
     */
    private static final String OPEN_ALL_MAP = "@openAllMap";

    /**
     * 一键打开所以建筑
     */
    private static final String OPEN_ALL_BUILD = "@openAllBuild";

    /**
     * 打开某个建筑
     *
     * @openBuild buildId
     */
    private static final String OPEN_BUILD = "@openBuild";

    /**
     * 升级科技某个type 到指定级数
     *
     * @lvUpTech techType level
     */
    private static final String LEVEL_UP_TECH = "@lvUpTech";

    /**
     * 打开所有的关卡
     *
     * @openAllMission
     */
    private static final String OPEN_ALL_MISSION = "@openAllMission";

    /**
     * 开启某一章节的所有关卡
     *
     * @openMission mapId(章节id)
     */
    private static final String OPEN_MISSION = "@openMission";

    /**
     * 升级某个建筑到多少级 注意是要解锁的建筑
     *
     * @upBuildLevel id level
     */
    private static final String UP_BUILD_LEVEL = "@upBuildLevel";

    /**
     * 升级神器等级
     *
     * @upSqLevel id level （注意要遵循神器的升级规则 有限制 否则不能生效）
     */
    private static final String UP_SQ_LEVEL = "@upSqLevel";

    /**
     * 添加所有装备
     *
     * @addAllEq
     */
    private static final String ADD_ALL_EQ = "@addAllEq";

    /**
     * 添加所有道具
     */
    private static final String ADD_ALL_ITEM = "@addAllItem";

    /**
     * 开启塔防关卡
     */
    private static final String OPEN_TD = "@openTD";
    /**
     * 复制玩家
     */
    private static final String COPY_PLAYER = "@copy";

    /**
     * 开启绑定再世界进程上的活动
     */
    private static final String OPEN_WORLD_ACTIVITY = "@openWorldActivity";

    private static final String HERO_LV_UP = "@heroLvUp";

    private static final String CLEAR_WORLD_BOX_SCORE = "@cleanWorldBox";

    private static final String KILL_MONSTER = "@killMonster";

    private static final String KILL_BIG_MONSTER = "@killBigMonster";

    private static final String SHOW_MONSTER = "@showMonster";
    // 完成日常任务
    private static final String DAIL_TASK = "@dailyTask";

    private static final String CLEAR_ITEMS = "@clearItems";

    private static final String CLEAR_ZERG = "@clearZerg";

    private static final String ADD_ZERG_SCORE = "@addZergScore";

    private static final String PUT_TD = "@putTD";// 设置无尽塔防挑战次数

    private static final String PUT_TD_LEVEL = "@putTDLevel"; // 设置塔防经典模式通关关卡

    private static final String CLEAR_EQUIPS = "@clearEquips";
    private static final String CLEAR_WORLD_PLAN = "@clearWorldPlan";

    private static final String SAN_APPLY = "@sanApply";

    private static final String SAN_CLEAN = "@sanClean";

    private static final String ADD_SAN_SCORE = "@addSanScore";

    private static final String SAN_APPLY_ONE = "@sanApplyOne";

    private static final String CLEAN_MAIL = "@clearYJ";

    private static final String ALL_FISH_BAIT = "@allBait";


    @Autowired
    private BuildingService buildingService;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private BuildingManager buildingManager;

    @Autowired
    CityManager cityManager;

    @Autowired
    StaticWorldMgr staticWorldMgr;

    @Autowired
    StaticSuperResMgr staticSuperResMgr;

    @Autowired
    SuperResService superResService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * gm命令处理
     *
     * @param msg
     * @param player
     */
    public boolean mijiResult(String msg, Player player) {
        boolean flag = true;
        try {
//            if (serverSetting.getGmOpen() != 1) {
//                return;
//            }

            Server server = SpringUtil.getBean(ServerManager.class).getServer();
            int serverType = server.getServerType();

            //if (!server.isGmOpen()) {// 未开放GM命令
            //	if (serverType != 0) {
            //		List<Integer> whiteList = server.getWhiteList();
            //		if (null == whiteList || whiteList.size() == 0 || !whiteList.contains(player.account.getAccountKey())) {
            //			return false;
            //		}
            //	}
            //}

            if (!msg.startsWith("@")) {
                return false;

            }
            String strs[] = msg.split(" ");
            String command = strs[0];
            if (command.equals(GIAVE_ME)) {
                giveMe(strs[1], player);
            } else if (command.equals("@addEquipChip")) {
                int id = Integer.parseInt(strs[1]);
                int count = Integer.parseInt(strs[2]);
                playerManager.addAward(player, AwardType.PROP, id, count, 0);
            } else if (command.equals(OPEN_WORLD_TARGET)) {
                openWorldTarget(Integer.valueOf(strs[1]));
            } else if (command.equals(OPEN_WORLD_PVP)) {
                worldActPlanService.openGmWorldPvp();
            } else if (command.equals(OPEN_ALL_MAP)) {
                List<MapStatus> mapStatuses = worldManager.checkAllMap(player);
                if (!mapStatuses.isEmpty()) {
                    player.updateMapStatuses(mapStatuses);
                }
                worldManager.synWorldMapStatus(player, mapStatuses);
            } else if (command.equals(OPEN_ALL_BUILD)) {
                buildingManager.synBuildings(player, staticBuildingMgr.getBuildIds());

            } else if (command.equals(OPEN_BUILD)) {
                buildingManager.synBuildings(player, Arrays.asList(Integer.valueOf(strs[1])));
            } else if (command.equals(LEVEL_UP_TECH)) {
                techService.gmLevelUpTech(Integer.valueOf(strs[1]), Integer.valueOf(strs[2]), player);
            } else if (command.equals(CLEAR_WORLD_TARGET)) {
                clearWorldTarget();
            } else if (command.equals(OPEN_ALL_MISSION)) {
                missionManager.openAllMission(player);
            } else if (command.equals(OPEN_MISSION)) {
                missionManager.openMission(player, Integer.valueOf(strs[1]));
            } else if (command.equals(UP_BUILD_LEVEL)) {
                buildingService.gmUpBuildLevel(player, Integer.valueOf(strs[1]), Integer.valueOf(strs[2]));
            } else if (command.equals(UP_SQ_LEVEL)) {
                killEquipService.gmUpSqLevel(Integer.valueOf(strs[1]), Integer.valueOf(strs[2]), player);
            } else if (command.equals(ADD_ALL_EQ)) {
                addAllEq(player);
            } else if (command.equals(ADD_ALL_ITEM)) {
                addAllItem(player);
            } else if (command.equals(OPEN_TD)) {
                tdService.openTd(player, Integer.valueOf(strs[1]));
            } else if (command.equals(COPY_PLAYER)) {
                playerManager.copyPlayer(player, Long.parseLong(strs[1]));
            } else if (command.equals(OPEN_WORLD_ACTIVITY)) {
                worldActPlanService.openWorldAct(Integer.parseInt(strs[1]));
            } else if (command.equals(HERO_LV_UP)) {
                heroService.heroLvUp(player, Integer.parseInt(strs[1]), Integer.parseInt(strs[2]));
            } else if (command.equals(CLEAR_WORLD_BOX_SCORE)) {
                player.getPWorldBox().setPoints(0);
                player.getPWorldBox().setTodayPoints(0);
            } else if (command.equals(KILL_MONSTER)) {
                worldService.killMonster(player, Integer.parseInt(strs[1]), Integer.parseInt(strs[2]));
            } else if (command.equals(KILL_BIG_MONSTER)) {
                worldService.killBigMonster(player, Integer.parseInt(strs[1]));
            } else if (command.equals(SHOW_MONSTER)) {
                showBigMonster(player);
            } else if (command.equals(DAIL_TASK)) {
                dailyTaskManager.completeAllTask(player);
            } else if (command.equals("@foreCleanPlayerMarch")) {
                Player target = playerManager.getPlayer(Long.valueOf(strs[1]));
                Iterator<March> it = target.getMarchList().iterator();
                while (it.hasNext()) {
                    March next = it.next();
                    next.setState(MarchState.Back);
                    it.remove();
                }
            } else if (command.equals("@foreCleanMarch")) {
                playerManager.getAllPlayer().forEach((id, target) -> {
                    Iterator<March> it = target.getMarchList().iterator();
                    while (it.hasNext()) {
                        March next = it.next();
                        next.setState(MarchState.Back);
                        it.remove();
                    }
                });
            } else if (command.equals("@cleanBroodMarch")) {
                playerManager.getAllPlayer().forEach((id, target) -> {
                    target.getMarchList().forEach(march -> {
                        if (march.getMarchType() == MarchType.BROOD_WAR) {
                            march.setState(MarchState.Back);
                        }
                    });
                });
            } else if (command.equals("@backMarch")) {
                Player player1 = playerManager.getPlayer(Long.valueOf(strs[1]));
                if (player1 != null) {
                    ConcurrentLinkedDeque<March> marchList = player1.getMarchList();
                    marchList.forEach(x -> {
                        x.setState(MarchState.Back);
                    });
                }
            } else if (command.equals("@clearCity")) {
                ConcurrentHashMap<Integer, City> cityMap = cityManager.getCityMap();
                cityMap.values().forEach(x -> {
                    x.setProtectedTime(System.currentTimeMillis());
                    x.reset();
                    x.setFlush(2);
                    x.setCityLv(0);
                    StaticWorldCity staticWorldCity = staticWorldMgr.getCity(x.getCityId());
                    cityManager.handleCityMonster(x);
                    if (!cityManager.getSquareFortress().contains(x.getCityId())) {
                        x.setCityLv(staticWorldCity.getLevel());
                    } else {
                        x.setCityLv(0);
                    }
                    x.setExp(0);
                    x.setCityName(null);
                });
                playerManager.getAllPlayer().values().forEach(player1 -> {
                    player1.setCityId(0);
                });
                MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
                mapInfo.getSuperResMap().clear();
                Iterator<SuperResource> iterator = mapInfo.getSuperPosResMap().values().iterator();
                while (iterator.hasNext()) {
//                    worldManager.removeResPosOnly(mapInfo, iterator.next().getPos());

                    mapInfo.clearPos(iterator.next().getPos());
                }
                mapInfo.getSuperPosResMap().clear();
            } else if (command.equals("@addCityExp")) {
                logger.info("strs={}", strs);
                int country = Integer.parseInt(strs[1]);
                int exp = Integer.parseInt(strs[2]);
                MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
                List<SuperResource> superResources = mapInfo.getSuperResMap().computeIfAbsent(country, x -> new ArrayList<>());
                City city = cityManager.checkAndGetHome(country);
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
                            SuperResource superResource = worldManager.addSuperResource(mapInfo, pos, resource);// 添加到地图
                            if (superResource == null) {
                                continue;
                            }
                            superResources.add(resource);// 添加到阵营
                            list.add(resource);
                        }
                        worldManager.synEntityAddRq(list);

                        WorldPb.SynMapCityRq.Builder builder1 = WorldPb.SynMapCityRq.newBuilder();
                        CommonPb.CityOwnerInfo.Builder cityOwnerInfo = worldManager.createCityOwner(city);
                        cityOwnerInfo.setCanAttendElection(false);
                        WorldPb.SynMapCityRq msg1 = builder1.setInfo(cityOwnerInfo).build();
                        playerManager.getOnlinePlayer().forEach(e -> {
                            playerManager.synMapCityRq(e, msg1);
                        });
                    }
                } catch (Exception e) {

                }
            } else if (command.equals(CLEAR_ITEMS)) {
                new HashMap<>(player.getItemMap()).forEach((k, v) -> {
                    SpringUtil.getBean(ItemManager.class).removeItem(player, k, v.getItemNum(), 999);
                });

            } else if (command.equals(CLEAR_ZERG)) {
                ConcurrentHashMap<Integer, City> cityMap = cityManager.getCityMap();
                cityMap.values().forEach(x -> {
                    x.setProtectedTime(System.currentTimeMillis());
                    x.reset();
                    x.setFlush(2);
                    x.setCityLv(0);
                    StaticWorldCity staticWorldCity = staticWorldMgr.getCity(x.getCityId());
                    cityManager.handleCityMonster(x);
                    if (!cityManager.getSquareFortress().contains(x.getCityId())) {
                        x.setCityLv(staticWorldCity.getLevel());
                    } else {
                        x.setCityLv(0);
                    }
                    x.setExp(0);
                    x.setCityName(null);
                });
                playerManager.getAllPlayer().values().forEach(player1 -> {
                    player1.setCityId(0);
                });
                MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
                mapInfo.getSuperResMap().clear();
                Iterator<SuperResource> iterator = mapInfo.getSuperPosResMap().values().iterator();
                while (iterator.hasNext()) {
//                    worldManager.removeResPosOnly(mapInfo, iterator.next().getPos());
                    mapInfo.clearPos(iterator.next().getPos());
                }
                mapInfo.getSuperPosResMap().clear();

                WorldData worldData = worldManager.getWolrdInfo();
                worldData.getWorldActPlans().remove(WorldActivityConsts.ACTIVITY_13);
                worldData.setZergData(null);
                MapInfo centerMap = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
                if (centerMap == null) {
                    return false;
                }

                Iterator<Entry<Integer, March>> it1 = centerMap.getMarchMap().entrySet().iterator();
                while (it1.hasNext()) {
                    Entry<Integer, March> entry = it1.next();
                    March march = entry.getValue();
                    if (march.getMarchType() == MarchType.ZERG_WAR || march.getMarchType() == MarchType.ZERG_DEFEND_WAR) {
                        it1.remove();
                    }
                    Player target = playerManager.getPlayer(march.getLordId());
                    if (target != null) {
                        target.getMarchList().remove(march);
                    }
                }

                Iterator<IWar> it = centerMap.getWarMap().values().iterator();
                while (it.hasNext()) {
                    IWar next = it.next();
                    if (next.getWarType() == WarType.ATTACK_ZERG || next.getWarType() == WarType.DEFEND_ZERG) {
                        it.remove();
                    }
                }
            } else if (command.equals(ADD_ZERG_SCORE)) {
                int score = Integer.parseInt(strs[1]);
                player.getSimpleData().addZergScore(score);
            } else if (command.equals(PUT_TD)) {
                player.getEndlessTDInfo().setRemainingTimes(Integer.parseInt(strs[1]));
            } else if (command.equals(PUT_TD_LEVEL)) {
                int value = Integer.parseInt(strs[1]);
                player.getTdMap().values().forEach(e -> {
                    if (value >= e.getLevelId() && (e.getState() == OpenState.CLOSE.val || e.getState() == OpenState.OPEN.val)) {
                        e.setState(OpenState.PASSED.val);
                    }
                });
                TD td = player.getTdMap().get(value + 1);
                if (td != null && td.getState() == OpenState.CLOSE.val) {
                    td.setState(OpenState.OPEN.val);
                }
            } else if (command.equals(CLEAR_WORLD_PLAN)) {
                int activityId = Integer.parseInt(strs[1]);
                WorldData worldData = worldManager.getWolrdInfo();
                worldData.getWorldActPlans().remove(activityId);
            } else if (command.equals(SAN_APPLY)) {
                actManoeuvreManager.testApply();
            } else if (command.equals(SAN_CLEAN)) {
                actManoeuvreManager.testClean();
            } else if (command.equals(ADD_SAN_SCORE)) {
                int score = Integer.parseInt(strs[1]);
                SimpleData simpleData = player.getSimpleData();
                simpleData.setManoeuvreScore(simpleData.getManoeuvreScore() + score);
            } else if (command.equals(SAN_APPLY_ONE)) {
                actManoeuvreManager.testApplyOne(player.getCountry());
            } else if (command.equals(CLEAN_MAIL)) {
                playerManager.getAllPlayer().forEach((k, v) -> {
                    player.getMails().clear();
                });
            } else if (command.equals(CLEAR_EQUIPS)) {
                player.getEquips().clear();
            } else if (command.equals(ALL_FISH_BAIT)) {
                fishingService.getALlFishBaits(player);
            } else if (command.equals("@addScore")) {
                player.setScore(Integer.parseInt(strs[1]));
            } else if (command.equals("@allProp")) {
                allProp(player);
            } else if (command.equals("@cleanAllProp")) {
                player.getItemMap().clear();
            }  else if (command.equals("@updateAchiScore")) {
                player.getAchievementInfo().setScore(Integer.parseInt(strs[1]));
            } else {
                flag = false;
            }
        } catch (Exception e) {
            logger.error("mijiResult error {}", e);
            return false;
        }

        return flag;
    }

    private void showBigMonster(Player player) {
        int mapId = worldManager.getMapId(player.getPos());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        StringBuilder builder = new StringBuilder();
        for (Pos pos : mapInfo.getBigMonsterMap().keySet()) {
            builder.append(pos.toPosStr()).append("/");
        }
        ManChat chat = chatManager.createManChat(player, builder.toString());
        chat.setStyle(0);
        CommonPb.Chat b = chat.ser(0, 0, 0);
        ChatPb.SynChatRq.Builder chatBuild = ChatPb.SynChatRq.newBuilder();
        chatBuild.setChat(b);
        BasePb.Base.Builder baseMsg = PbHelper.createSynBase(ChatPb.SynChatRq.EXT_FIELD_NUMBER, ChatPb.SynChatRq.ext, chatBuild.build());
        GameServer.getInstance().sendMsgToPlayer(player, baseMsg);
    }

    public void clearWorldTarget() {
        WorldData worldData = worldManager.getWolrdInfo();
        worldData.getTasks().clear();
        for (Player p : playerManager.getPlayers().values()) {
            p.getPersonalGoals().clear();
        }
        openWorldTarget(1);
    }

    private void openWorldTarget(int targetId) {
        if (targetId == 1) {
            worldTargetTaskService.openWorldTarget(targetId);
            return;
        }
        for (int i = 1; i <= targetId; i++) {
            WorldData worldData = worldManager.getWolrdInfo();
            WorldTargetTask worldTargetTask = worldData.getTasks().get(i);
            if (worldTargetTask == null) {
                worldTargetTask = worldTargetTaskService.openWorldTarget(i);
            }
            if (i != targetId) {
                StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(i);
                worldTargetTask.setNum(staticWorldNewTarget.getWorldGoal());
                worldTargetTask.setCount(staticWorldNewTarget.getWorldGoal2());
                worldTargetTask.setComplete(1);
            }
        }
        // 同步所有人
//        for (Player player : playerManager.getPlayers().values()) {
//            worldTargetTaskService.synUpdateWorldTargetTask(player);
//        }

    }

    private void giveMe(String content, Player player) {
        String[] result = content.split(";");
        for (String ss : result) {
            String item[] = ss.trim().split(",");
            if (item.length != 3) {
                return;
            }
            int type = Integer.valueOf(item[0]);
            int id = Integer.valueOf(item[1]);
            int account = Integer.valueOf(item[2]);
            if (account > 0) {
                playerManager.addAward(player, type, id, account, Reason.GM_ADD_GOODS);
            } else {
                playerManager.subAward(player, type, id, Math.abs(account), Reason.GM_ADD_GOODS);
            }
            if (type == AwardType.RESOURCE) {
                playerManager.synChange(player, Reason.GM_ADD_GOODS);
            } else if (type == AwardType.EQUIP) {
                SycEquipChange(player);
            } else {
                synChange(player, new Award(type, id, account), Reason.GM_ADD_GOODS);
            }
        }
    }

    private void addAllItem(Player player) {
        int[] item = {10, 13, 22, 36, 37, 51, 50};
        List resultList = Arrays.asList(item);
        for (StaticProp staticProp : staticPropMgr.getPropMap().values()) {
            if (resultList.contains(staticProp.getPropType())) {
                continue;
            }
            playerManager.addAward(player, 3, staticProp.getPropId(), staticProp.getStackSize(), Reason.GM_ADD_GOODS);
            synChange(player, new Award(3, staticProp.getPropId(), staticProp.getStackSize()), Reason.GM_ADD_GOODS);
        }
    }

    public void allProp(Player player) {
        int count = 0;
        for (StaticProp staticProp : staticPropMgr.getPropMap().values()) {
            count++;
            playerManager.addAward(player, AwardType.PROP, staticProp.getPropId(), 1, Reason.GM_ADD_GOODS);
            synChange(player, new Award(AwardType.PROP, staticProp.getPropId(), 1), Reason.GM_ADD_GOODS);
            if (count == 299) {
                return;
            }
        }
    }


    public void addAllEq(Player player) {
        for (StaticEquip staticEquip : staticEquipDataMgr.getEquipMap().values()) {
            playerManager.addAward(player, AwardType.EQUIP, staticEquip.getEquipId(), 1, Reason.GM_ADD_GOODS);
        }
        SycEquipChange(player);
    }

    /**
     * 装备推送
     *
     * @param player
     */
    public void SycEquipChange(Player player) {
        Map<Integer, Equip> equips = player.getEquips();
        EquipPb.GetEquipBagRs.Builder builder = EquipPb.GetEquipBagRs.newBuilder();
        for (java.util.Map.Entry<Integer, Equip> entry : equips.entrySet()) {
            if (entry == null) {
                continue;
            }
            Equip equip = entry.getValue();
            if (equip == null) {
                continue;
            }
            builder.addEquipItem(equip.wrapPb());
        }
        SynHelper.synMsgToPlayer(player, EquipPb.GetEquipBagRs.EXT_FIELD_NUMBER, EquipPb.GetEquipBagRs.ext, builder.build());
    }

    // 同步当前玩家相关变化
    public void synChange(Player player, Award award, int reason) {
        if (player != null && player.isLogin && player.getChannelId() != -1) {
            RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
            // 玩家资源
            builder.setResource(player.wrapResourcePb());
            builder.addAward(award.wrapPb());
            SynHelper.synMsgToPlayer(player, RolePb.SynChangeRq.EXT_FIELD_NUMBER, RolePb.SynChangeRq.ext, builder.build());
        }
    }

}
