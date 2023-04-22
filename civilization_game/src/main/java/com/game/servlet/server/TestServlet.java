package com.game.servlet.server;

import com.game.constant.MarchReason;
import com.game.constant.UcCodeEnum;
import com.game.domain.Player;
import com.game.domain.p.BroodWarInfo;
import com.game.domain.p.Hero;
import com.game.manager.*;
import com.game.pb.BroodWarPb;
import com.game.service.*;
import com.game.uc.Message;
import com.game.util.RandomUtil;
import com.game.spring.SpringUtil;
import com.game.worldmap.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author cpz
 * @date 2020/10/21 14:28
 * @description
 */
@Controller
@RequestMapping("test")
public class TestServlet {

    /**
     * 重新加载所有配置
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/testBroodWar.do", method = RequestMethod.POST)
    public Message testBroodWar() {
        //45级以上用户随机迁城到母巢
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
        BroodWarService broodWarService = SpringUtil.getBean(BroodWarService.class);
        HeroManager heroManager = SpringUtil.getBean(HeroManager.class);
        int x = 250;
        int y = 250;
        int minX = x - 10;
        int minY = y - 10;
        int maxX = x + 10;
        int maxY = y + 10;
        List<Pos> posMap = new ArrayList<>();
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                posMap.add(new Pos(i, j));
            }
        }
        //母巢
        int mapId = 20;
        playerManager.getPlayers().values().parallelStream().forEach(player -> {
            if (player.getLevel() < 70) {
                return;
            }
            // 可以迁城
            Pos playerPos = player.getPos();
            int currentMapId = worldManager.getMapId(player);
            if (mapId != currentMapId) {
                MapInfo mapInfo = worldManager.getMapInfo(currentMapId);
                worldManager.removePlayerCity(playerPos, mapInfo);
                MapInfo newMapInfo = worldManager.getMapInfo(mapId);
                Pos randPos = randomPos(posMap, newMapInfo, 0);
                if (randPos == null) {
                    return;
                }
                playerManager.changePlayerPos(player, randPos);
                worldManager.addPlayerCity(randPos, newMapInfo, player);
                // 驻防武将回城
                worldManager.handleWallFriendReturn(player);
                // 迁城之后战斗全部删除
                worldManager.removePlayerWar(player, playerPos, MarchReason.MiddleMove, randPos);
            }

            for (int heroId : player.getEmbattleList()) {
                Hero hero = player.getHero(heroId);
                if (hero == null) {
                    continue;
                }
                int type = RandomUtil.randomBetween(0, 1);
                int cityId = RandomUtil.randomBetween(25, 29);
                if (player.getBroodWarInfo() == null) {
                    player.setBroodWarInfo(new BroodWarInfo());
                }
                // 应该增加的英雄的兵力
                heroManager.caculateProp(hero, player);
                hero.setCurrentSoliderNum(hero.getSoldierNum());
                playerManager.synChange(player, 0);
                broodWarService.attackBrood(BroodWarPb.AttackBroodRq.newBuilder().setHeroId(heroId).setState(type).setCityId(cityId).build(), player.roleId);
            }
        });
        return new Message(UcCodeEnum.SUCCESS);
    }

    public Pos randomPos(List<Pos> posMap, MapInfo newMapInfo, int count) {
        if (count >= 5) {
            return null;
        }
        Pos pos = RandomUtil.getOneRandomElement(posMap);
        if (!newMapInfo.isFreePos(pos)) {
            return randomPos(posMap, newMapInfo, count++);
        }
        return pos;
    }

//    @ResponseBody
//    @RequestMapping(value = "/testMarch", method = RequestMethod.POST)
//    public Message testMarch() {
//        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
//        WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
//        int count = 0;
//        for (Player player : playerManager.getPlayers().values()) {
//            count++;
//            if (count >= 100) {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                count = 0;
//            }
//            int mapId = worldManager.getMapId(player.getPos());
//            MapInfo mapInfo = worldManager.getMapInfo(mapId);
//            Monster m = RandomUtil.getOneRandomElement(new ArrayList<>(mapInfo.getMonsterMap().values()));
//            March march = worldManager.createMarch(player, player.getEmbattleList(), m.getPos());
//            march.setFightTime(march.getEndTime() + 1000L, MarchReason.KillRebel);
//            march.setMarchType(MarchType.AttackMonster);
//            // 添加行军到玩家身上
//            player.addMarch(march);
//            // 加到世界地图中
//            worldManager.addMarch(mapId, march);
//            worldManager.synMarch(0, march);
//        }
//        return new Message(UcCodeEnum.SUCCESS);
//    }
}
