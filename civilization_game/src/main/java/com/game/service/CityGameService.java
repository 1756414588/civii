package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.Player;
import com.game.domain.p.SmallCityGame;
import com.game.domain.s.StaticCityGame;
import com.game.log.constant.CopperOperateType;
import com.game.log.constant.IronOperateType;
import com.game.log.constant.OilOperateType;
import com.game.log.constant.StoneOperateType;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.manager.PlayerManager;
import com.game.manager.StaticCityGameManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.BasePb;
import com.game.pb.BuildingPb;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cpz
 * @date 2020/10/29 18:25
 * @description
 */
@Service
public class CityGameService {

    @Autowired
    private PlayerManager playerManager;
    //
    @Autowired
    private StaticCityGameManager cityGameManager;
    @Autowired
    private StaticLimitMgr limitMgr;


    private static List<Integer> posIndex = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

    /**
     * 刷新玩家主城怪物
     */
    public void refush() {
        //>20级出虫子
        long curentTimeSecond = TimeHelper.getCurrentSecond();
        int configTime = limitMgr.getNum(258);
        //上次刷新虫子的时间
        long lastRefushTime = curentTimeSecond - configTime;
        int maxTimes = cityGameManager.getMaxTimes();
        int maxWorms = limitMgr.getNum(SimpleId.CITY_GAME_WORMS);
        List<Player> players = playerManager.getOnlinePlayer().parallelStream().filter(e -> e.getLevel() >= 20).collect(Collectors.toList());
        players.forEach(player -> {
            SmallCityGame smallCityGame = player.getSmallCityGame();
            if (smallCityGame == null) {
                smallCityGame = new SmallCityGame();
                smallCityGame.setLastRefushTime(lastRefushTime);
                smallCityGame.setLastSendTime(curentTimeSecond + 120);
                player.setSmallCityGame(smallCityGame);
            }
            //2分钟推一次
            if (smallCityGame.getLastSendTime() <= curentTimeSecond) {
                if (!smallCityGame.getWorms().isEmpty()) {
                    smallCityGame.setLastSendTime(curentTimeSecond + 120);
                    sendToPlayer(player, smallCityGame);
                }
            }
            //五分钟刷一只
            if (smallCityGame.getLastRefushTime() <= curentTimeSecond) {
                return;
            }
            //奖励是否满了
            if (smallCityGame.getTotal() >= maxTimes) {
                return;
            }
            //虫子是否满了
            if (smallCityGame.getWorms().size() >= maxWorms) {
                return;
            }
            //随机刷一只虫
            int pos = randomWorm(smallCityGame.getWorms());
            if (pos < 0) {
                return;
            }
            smallCityGame.getWorms().put(pos, 1);
            smallCityGame.setLastRefushTime(curentTimeSecond);
            smallCityGame.addTotal();
            sendToPlayer(player, smallCityGame);
        });
    }

    public int randomWorm(Map<Integer, Integer> worms) {
        List<Integer> posList = new ArrayList<>(posIndex);
        worms.forEach((e, f) -> {
            if (posList.contains(e)) {
                posList.remove(e);
            }
        });
        if (posList.size() > 1) {
            Collections.shuffle(posList);
        }
        if (posList.size() > 0) {
            return posList.get(0);
        }
        return -1;
    }

    public void sendToPlayer(Player player, SmallCityGame smallCityGame) {
        if (player.getChannelId() != -1 && smallCityGame != null) {
            BuildingPb.SynSmallCityGame.Builder builder = BuildingPb.SynSmallCityGame.newBuilder();
            smallCityGame.getWorms().forEach((e, f) -> {
                builder.addPos(e);
            });
            BasePb.Base.Builder msg = PbHelper.createSynBase(BuildingPb.SynSmallCityGame.EXT_FIELD_NUMBER, BuildingPb.SynSmallCityGame.ext, builder.build());
            GameServer.getInstance().sendMsgToPlayer(player, msg);
        }
    }

    /**
     * 点击虫子
     *
     * @param handler
     * @param rq
     */
    public void clickWorms(ClientHandler handler, BuildingPb.ClickWormRq rq) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        SmallCityGame cityGame = player.getSmallCityGame();
        if (cityGame == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        LogHelper.CONFIG_LOGGER.info("player smallcity->[{}]",cityGame.getTotal());
        if (!cityGame.getWorms().containsKey(rq.getPos())) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        cityGame.getWorms().remove(rq.getPos());
        //先随机类型1的奖励 类型一的领完了再随机类型2的
        int oneTimes = cityGameManager.getOneTimes();
        //
        int firstType = 1;
        int awardCount = cityGame.getRewards().values().stream().mapToInt(e -> e.intValue()).sum();
        if (awardCount >= oneTimes) {
            firstType = 2;
        }
        int type = firstType;
        List<StaticCityGame> rewards = cityGameManager.getCityGameMap().values().stream().filter(e -> e.getType() == type).collect(Collectors.toList());
        List<StaticCityGame> newRewards = new ArrayList<>();
        for (StaticCityGame cityGameRward : rewards) {
            Integer rewardCount = cityGame.getRewards().get(cityGameRward.getId());
            if (rewardCount == null) {
                newRewards.add(cityGameRward);
            } else {
                if (rewardCount < cityGameRward.getMaxTimes()) {
                    newRewards.add(cityGameRward);
                }
            }
        }
        if(newRewards.size() == 0){
            handler.sendErrorMsgToPlayer(GameError.REACH_MAX_KILL_DIMO_TIMES);
            return;
        }
        Collections.shuffle(newRewards);
        StaticCityGame reward = newRewards.get(0);
        Integer count = cityGame.getRewards().get(reward.getId());
        if (count == null) {
            count = 0;
        }
        count++;
        cityGame.getRewards().put(reward.getId(), count);
        BuildingPb.ClickWormRs.Builder builder = BuildingPb.ClickWormRs.newBuilder();
        com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
        reward.getAward().forEach(e -> {
            playerManager.addAward(player, e.get(0), e.get(1), e.get(2), Reason.CITY_SIMALL_GAME);
            builder.addAward(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
            int t = 0;
            int resType = e.get(1);
            switch (resType) {
                case ResourceType.IRON:
                    t = IronOperateType.CITY_SMALL_GAME.getInfoType();
                    break;
                case ResourceType.COPPER:
                    t = CopperOperateType.CITY_SMALL_GAME.getInfoType();
                    break;
                case ResourceType.OIL:
                    t = OilOperateType.CITY_SMALL_GAME.getInfoType();
                    break;
                case ResourceType.STONE:
                    t = StoneOperateType.CITY_SMALL_GAME.getInfoType();
                    break;
                default:
                    break;
            }
            if (t != 0) {
                logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
                        player.getNick(),
                        player.getLevel(),
                        player.getTitle(),
                        player.getHonor(),
                        player.getCountry(),
                        player.getVip(),
                        player.account.getChannel(),
                        0, e.get(2), t), resType);
            }
        });
        handler.sendMsgToPlayer(BuildingPb.ClickWormRs.ext, builder.build());
    }
}
