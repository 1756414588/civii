package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWallMgr;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticWallMonsterLv;
import com.game.manager.MarchManager;
import com.game.manager.PlayerManager;
import com.game.manager.WallManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.WallPb;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class WallService {

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private StaticWallMgr staticWallMgr;

    @Autowired
    private WallManager wallMgr;

    @Autowired
    private MarchManager marchManager;

    public void getWallInfo(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        Wall wall = player.getWall();
        //List<Integer> defenceHero = wall.getDefenceHero();
        //checkWall(player, wall);
        WallPb.GetWallInfoRs.Builder builder = WallPb.GetWallInfoRs.newBuilder();

        //List<WarDefenseHero> defenseArmyList = player.getDefenseArmyList();
        //List<Integer> collect = defenseArmyList.stream().mapToInt(x -> x.getHeroId()).boxed().collect(Collectors.toList());
        builder.addAllHeroId(player.getEmbattleList());
        builder.setEndTime(wall.getEndTime());
        Map<Integer, WallDefender> defenderMap = wall.getWallDefenders();
        for (WallDefender defender : defenderMap.values()) {
            if (defender == null) {
                continue;
            }
            builder.addDefender(defender.wrapPb());
        }

        Map<Integer, WallFriend> wallFriendMap = wall.getWallFriends();
        // 没有行军的友军应该删除
        Iterator<WallFriend> it = wallFriendMap.values().iterator();
        while (it.hasNext()) {
            WallFriend wallFriend = it.next();
            if (wallFriend == null) {
                continue;
            }
            long lordId = wallFriend.getLordId();
            Player wallPlayer = playerManager.getPlayer(lordId);
            if (wallPlayer.getMarch(wallFriend.getMarchId()) == null) {
                it.remove();
            }
        }

        for (WallFriend wallFriend : wallFriendMap.values()) {
            if (wallFriend == null) {
                continue;
            }

            builder.addFriend(playerManager.wrapWallFriend(wallFriend));
        }

        handler.sendMsgToPlayer(WallPb.GetWallInfoRs.ext, builder.build());
    }

    //public void checkWall(Player player, Wall wall) {
    //	List<Integer> defenceHero = wall.getDefenceHero();
    //	List<Integer> embattleList = player.getEmbattleList();
    //	Iterator<Integer> iterator = defenceHero.iterator();
    //	Map<Integer, Integer> heros = new HashMap<>();
    //	while (iterator.hasNext()) {
    //		Integer heroId = iterator.next();
    //		heros.put(heroId, null);
    //		if (heroId == null) {
    //			continue;
    //		}
    //
    //		Hero hero = player.getHero(heroId);
    //		if (heroId >= 0) {
    //			if (hero == null) {
    //				iterator.remove();
    //			}
    //		}
    //		if (!embattleList.contains(heroId)) {
    //			wall.getDefenceHero().clear();
    //			wall.getDefenceHero().addAll(embattleList);
    //			break;
    //		}
    //	}
    //	int index = 0;
    //	for (int i = 0; i < embattleList.size(); i++) {
    //		if (embattleList.get(i) > 0) {
    //			++index;
    //		}
    //	}
    //
    //	//容错  ，防止英雄id 重复
    //	if (defenceHero.size() != index || heros.size() != defenceHero.size()) {
    //		wall.getDefenceHero().clear();
    //		wall.getDefenceHero().addAll(embattleList);
    //	}
    //
    //}

    // 交换武将位置
    public void changeHeroPos(WallPb.ChangeHeroPosRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 客户端发来的英雄Id
        List<Integer> reqHeroIds = req.getHeroIdList();
        List<Integer> embattleList = player.getEmbattleList();
        long count = embattleList.stream().filter(x -> x > 0).count();
        if (reqHeroIds.size() != count) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
        }
        for (int i = 0; i < reqHeroIds.size(); i++) {
            Integer integer = reqHeroIds.get(i);
            embattleList.set(i, integer);
        }
        WallPb.ChangeHeroPosRs.Builder builder = WallPb.ChangeHeroPosRs.newBuilder();
        handler.sendMsgToPlayer(WallPb.ChangeHeroPosRs.ext, builder.build());
    }

    // 城防军招募
    public void hireDefenceRq(WallPb.HireDefenderRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 检查剩余格子够不够
        Wall wall = player.getWall();
        int wallLv = wall.getLv();
        Map<Integer, WallDefender> wallDefenders = wall.getWallDefenders();
        int defenderCount = wallDefenders.size();
        int leftSize = wallLv - defenderCount;
        if (leftSize <= 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_MORE_POS_FOR_DEFENDER);
            return;
        }

        // 检查cd时间
        long now = System.currentTimeMillis();
        if (wall.getEndTime() > now) {
            handler.sendErrorMsgToPlayer(GameError.WALL_HAS_CD);
            return;
        }

        // 随机一个城防军等级, 城墙的4~5倍等级
        int minLv = wallLv * 4;
        int maxLv = wallLv * 5;
        int randLv = RandomHelper.threadSafeRand(minLv, maxLv);

        int quality = wallMgr.randQuality(wallLv, wallDefenders);
        int soldier = RandomHelper.threadSafeRand(1, 3);
        int id = RandomHelper.threadSafeRand(1, 10);
        WallDefender wallDefender = new WallDefender();
        wallDefender.setKeyId(player.maxKey());
        wallDefender.setId(id); // 头像
        wallDefender.setLevel(randLv);
        if (wallDefender.getQuality() < quality) {
            wallDefender.setQuality(quality);
        }
        wallDefender.setSoldier(soldier);
        wall.setEndTime(staticLimitMgr.getNum(24) * TimeHelper.SECOND_MS + now);
        StaticWallMonsterLv staticWallMonsterLv = staticWallMgr.getWallMonster(randLv, quality);
        if (staticWallMonsterLv == null) {
            LogHelper.CONFIG_LOGGER.info("config error, randLv = " + randLv + ", quality = " + quality);
            return;
        }
        wallDefender.setSoldierNum(staticWallMonsterLv.getSoldier());
        wall.addWallDefender(wallDefender);

        WallPb.HireDefenderRs.Builder builder = WallPb.HireDefenderRs.newBuilder();
        builder.setDefender(wallDefender.wrapPb());
        builder.setCdTime(wall.getEndTime());
        handler.sendMsgToPlayer(WallPb.HireDefenderRs.ext, builder.build());
    }

    // 秒城墙恢复时间
    public void killWallCd(WallPb.KillDefenderCdRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 2分钟一元宝
        int configNum = staticLimitMgr.getNum(25);
        Wall wall = player.getWall();
        long endTime = wall.getEndTime();
        long leftMinutes = TimeHelper.getTotalMinute(endTime);
        long gold = (leftMinutes / configNum) + ((leftMinutes % configNum == 0) ? 0 : 1);
        int own = player.getGold();
        if (own < gold) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        playerManager.subAward(player, AwardType.GOLD, 1, gold, Reason.KILL_WALL_CD);
        wall.setEndTime(System.currentTimeMillis());
        WallPb.KillDefenderCdRs.Builder builder = WallPb.KillDefenderCdRs.newBuilder();
        builder.setEndTime(wall.getEndTime());
        builder.setGold(player.getGold());
        handler.sendMsgToPlayer(WallPb.KillDefenderCdRs.ext, builder.build());
    }

    // 城防军升级
    public void levelUpDefender(WallPb.LevelUpDefenderRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int keyId = req.getKeyId();
        // 检查index的合法性
        // 检查剩余格子够不够
        Wall wall = player.getWall();
        Map<Integer, WallDefender> defenderMap = wall.getWallDefenders();
        WallDefender wallDefender = defenderMap.get(keyId);
        if (wallDefender == null) {
            LogHelper.CONFIG_LOGGER.info("wallDefender is null!");
            handler.sendErrorMsgToPlayer(GameError.DEFENDER_INDEX_ERROR);
            return;
        }

        // 检查金币是否充足
        int cost = staticLimitMgr.getNum(26);
        if (player.getGold() < cost) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        // 是否满级
        int level = wallDefender.getLevel();
        int quality = wallDefender.getQuality();
        int maxLevel = staticLimitMgr.getNum(27);
        int wallLimutLv = wall.getLv() * 5 + 2;
        wallLimutLv = Math.min(maxLevel, wallLimutLv);
        if (level >= wallLimutLv && quality >= Quality.RED.get()) {
            handler.sendErrorMsgToPlayer(GameError.DEFENDER_LEVEL_ENOUGH);
            return;
        }

        int randLv = RandomHelper.threadSafeRand(1, 5);
        if (level >= wallLimutLv && quality < Quality.RED.get()) { // 升级品质
            int qualityRand = RandomHelper.threadSafeRand(1, 100);
            if (qualityRand <= 20) {
                quality += 1;
                if (player.getLevel() >= 90) {
                    quality = Math.min(Quality.RED.get(), quality);
                    quality = Math.max(Quality.BLUE.get(), quality);
                } else {
                    quality = Math.min(Quality.GOLD.get(), quality);
                    quality = Math.max(Quality.BLUE.get(), quality);
                }

            }
        } else {
            level += randLv;
            level = Math.min(wallLimutLv, level);
            int qualityRand = RandomHelper.threadSafeRand(1, 100);
            if (qualityRand <= 10) {
                quality += 1;
                if (player.getLevel() >= 90) {
                    quality = Math.min(Quality.RED.get(), quality);
                    quality = Math.max(Quality.BLUE.get(), quality);
                } else {
                    quality = Math.min(Quality.GOLD.get(), quality);
                    quality = Math.max(Quality.BLUE.get(), quality);
                }
            }

        }

        wallDefender.setQuality(quality);
        wallDefender.setLevel(level);

        playerManager.subAward(player, AwardType.GOLD, 1, cost, Reason.LEVEL_UP_DEFENDER);

        WallPb.LevelUpDefenderRs.Builder builder = WallPb.LevelUpDefenderRs.newBuilder();
        builder.setDefender(wallDefender.wrapPb());
        builder.setGold(player.getGold());
        handler.sendMsgToPlayer(WallPb.LevelUpDefenderRs.ext, builder.build());
    }

    // 到达目的地之后才加到城墙上
    public void friendAssist(WallPb.FriendAssistRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        long targetId = req.getTargetLordId();
        Player target = playerManager.getPlayer(targetId);
        if (target == null) {
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

        Map<Pos, PlayerCity> playerCityMap = mapInfo.getPlayerCityMap();
        if (playerCityMap == null) {
            LogHelper.CONFIG_LOGGER.info("playerCityMap is null");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        PlayerCity playerCity = playerCityMap.get(targetPos);
        if (playerCity == null) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_MONSTER_NOT_FOUND);
            return;
        }

        // 检查target可驻防的武将的数目
        Wall wall = target.getWall();
        int wallLv = wall.getLv();

        // 检查城墙开启驻防的等级
        int openLv = staticLimitMgr.getNum(29);
        if (wallLv < openLv) {
            handler.sendErrorMsgToPlayer(GameError.WALL_LEVEL_NOT_ENOUGH);
            return;
        }
        int maxNum  = (wallLv - openLv) * 2 + staticLimitMgr.getNum(57);
//        if (wallLv >= openLv) {
//
//        } else {
//            maxNum = 0;
//        }

        if (maxNum <= 0) {
            handler.sendErrorMsgToPlayer(GameError.WALL_CANNOT_ASSIST);
            return;
        }

        Map<Integer, WallFriend> wallFriendMap = wall.getWallFriends();

        int currentNum = wallFriendMap.size();
        List<Integer> heroIds = req.getHeroIdsList();
        if (currentNum + heroIds.size() > maxNum) {
            handler.sendErrorMsgToPlayer(GameError.REACH_MAX_DEFENDER);
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
        int oilCost = worldManager.getMarchOil(heroIds, player, targetPos);
        if (player.getResource(ResourceType.OIL) < oilCost) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        // 生成行军
        March march = worldManager.createMarch(player, heroIds, targetPos);
        march.setMarchType(MarchType.CityFriendAssist);
        march.setAssistId(targetId);
        // 添加行军到玩家身上
        player.addMarch(march);
        // 加到世界地图中
        worldManager.addMarch(mapId, march);

        // 行军全区域广播
        worldManager.synMarch(mapInfo.getMapId(), march);

        WallPb.FriendAssistRs.Builder builder = WallPb.FriendAssistRs.newBuilder();
        builder.setResource(player.wrapResourcePb());
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WallPb.FriendAssistRs.ext, builder.build());

    }

    public boolean isEmbattle(Player player, int heroId) {
        List<Integer> embattleList = player.getEmbattleList();
        return embattleList.contains(heroId);
    }

    // 撤回部队
    public void friendMarchCancelRq(WallPb.FriendMarchCancelRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int marchId = req.getMarchId();
        // 找到要撤销的那个玩家
        long targetId = req.getLordId();
        Player target = playerManager.getPlayer(targetId);
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 找到target的城墙
        Wall wall = target.getWall();
        if (wall == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        WallFriend wallFriend = wall.getWallFriend(marchId, player.roleId);
        if (wallFriend == null) {
            //handler.sendErrorMsgToPlayer(GameError.NO_WALL_FRIEND);
            // add defence code
            // 撤销我自己的行军
            March march = player.getMarch(marchId);
            if (march == null) {
                handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
                return;
            }

            marchManager.doMarchReturn(march, player, MarchReason.WallAssistCancelMarch);

            // 删除部队, 我方行军回城，别人队伍没了
            WallPb.FriendMarchCancelRs.Builder builder = WallPb.FriendMarchCancelRs.newBuilder();
            builder.setMarch(worldManager.wrapMarchPb(march));
            handler.sendMsgToPlayer(WallPb.FriendMarchCancelRs.ext, builder.build());
            // 同步对方的城墙信息
            playerManager.synWallInfo(target);
            return;
        }

        if (marchId != wallFriend.getMarchId()) {
            handler.sendErrorMsgToPlayer(GameError.MARCH_ID_ERROR);
            return;
        }

        // 删除对方城墙的友军
        wall.removeWallFriend(marchId, player.roleId);

        // 撤销我自己的行军
        March march = player.getMarch(marchId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }

        marchManager.doMarchReturn(march, player, MarchReason.WallAssistCancelMarch);

        // 删除部队, 我方行军回城，别人队伍没了
        WallPb.FriendMarchCancelRs.Builder builder = WallPb.FriendMarchCancelRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(WallPb.FriendMarchCancelRs.ext, builder.build());

        // 同步对方的城墙信息
        playerManager.synWallInfo(target);

        // 给targetId发出遣返邮件
        // 您在%s的基地驻防的将领%s，由于基地被击飞或迁移，开始返回。
        String name = String.valueOf(target.getNick());
        String heroName = String.valueOf(wallFriend.getHeroId());
        playerManager.sendNormalMail(player, MailId.WALL_KILL_OUT, name, heroName);

    }


    public void kickMarchRq(WallPb.KickMarchRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 踢出别人的友军
        int marchId = req.getMarchId();
        long targetId = req.getLordId();
        Player target = playerManager.getPlayer(targetId);
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 我自己的城墙
        Wall wall = player.getWall();
        if (wall == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }

        WallFriend wallFriend = wall.getWallFriend(marchId, targetId);
        if (wallFriend == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_WALL_FRIEND);
            return;
        }

        if (marchId != wallFriend.getMarchId()) {
            handler.sendErrorMsgToPlayer(GameError.MARCH_ID_ERROR);
            return;
        }

        // 删除对方城墙的友军
        wall.removeWallFriend(marchId, targetId);

        // 踢出别人的行军
        March march = target.getMarch(marchId);
        if (march == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_MARCH);
            return;
        }

        marchManager.doMarchReturn(march, target, MarchReason.WallAssistCancelOthersMarch); // 别人行军回城

        // 我方部队没了
        WallPb.KickMarchRs.Builder builder = WallPb.KickMarchRs.newBuilder();
        builder.setFriend(playerManager.wrapWallFriend(wallFriend));
        handler.sendMsgToPlayer(WallPb.KickMarchRs.ext, builder.build());

        // 给targetId发出遣返邮件
        // 您在%s的基地驻防的将领%s，被基地指挥官遣返。
        String name = String.valueOf(player.getNick());
        String heroName = String.valueOf(wallFriend.getHeroId());
        playerManager.sendNormalMail(target, MailId.WALL_KILL_OUT, name, heroName);
    }

}
