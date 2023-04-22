package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticRebelMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.Award;
import com.game.domain.p.Item;
import com.game.domain.p.SimpleData;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticRebelExchange;
import com.game.domain.s.StaticWorldMonster;
import com.game.log.consumer.EventManager;
import com.game.manager.ItemManager;
import com.game.manager.PlayerManager;
import com.game.manager.WarManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.cs.ExchangeRebelAwardHandler;
import com.game.message.handler.cs.GetExchangeInfoHandler;
import com.game.message.handler.cs.GetRebelScoreRankHandler;
import com.game.message.handler.cs.UseRebelPropHandler;
import com.game.pb.CommonPb;
import com.game.pb.RankPb;
import com.game.pb.RebelPb;
import com.game.pb.RebelPb.RebelGuideAwardRs;
import com.game.rank.CountryScore;
import com.game.rank.RankInfo;
import com.game.rank.RebelScoreRankMgr;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.Pos;
import com.game.worldmap.RebelMonster;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @author jyb
 * @date 2020/4/28 15:55
 * @description
 */
@Service
public class RebelService {

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticPropMgr staticPropMgr;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private StaticWorldMgr staticWorldMgr;


    @Autowired
    private WarManager warManager;


    @Autowired
    private StaticRebelMgr staticRebelMgr;


    @Autowired
    private ItemManager itemManager;


    @Autowired
    private RebelScoreRankMgr rebelScoreRankMgr;


    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private EventManager eventManager;


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 排行榜显示的
     */
    public final int[] ranks = {1, 2, 3, 10, 50, 100, 300};

    /**
     * 使用伏击叛军道具
     *
     * @param req
     * @param handler
     */
    public void useRebelProp(RebelPb.UseRebelPropRq req, UseRebelPropHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        // 检测物品是否存在背包中, 数量是否合法
        int itemId = req.getPropId();
        // 检测物品是否存在
        StaticProp staticProp = staticPropMgr.getStaticProp(itemId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }
        //添加限制，避免玩家改道具id用其他的道具也能开叛军
		if (itemId != RebelConsts.ITEM_ID_1 && itemId != RebelConsts.ITEM_ID_2 && itemId != RebelConsts.ITEM_ID_3) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }
        // 检测物品是否能够使用
        if (staticProp.getCanUse() != ItemUse.CAN_USE) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_CAN_NOT_USE);
            return;
        }
        // 检测背包是否存在
        HashMap<Integer, Item> items = player.getItemMap();
        Item item = items.get(itemId);
        if (item == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }

        if (item.getItemNum() <= 0) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }

        // 根据effectValue来执行具体得到什么+-

        List<List<Long>> effectValue = staticProp.getEffectValue();
        if (effectValue == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_EFFECT_VALUE_IS_NULL);
            return;
        }
        List<Entity> list = new ArrayList<>();
        int mapId = worldManager.getMapId(player);
        Pos pos = new Pos(req.getPox(), req.getPoy());
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (!mapInfo.isFreePos(pos)) {
            handler.sendErrorMsgToPlayer(GameError.POS_NOT_EMPTY);
            Entity entity = mapInfo.getEntity(pos);
            if (entity != null) {
                list.add(entity);
                worldManager.synEntityAddRq(list);
            }
            return;
        }
        long monsterId = effectValue.get(0).get(0);
        StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster((int) monsterId);
        if (staticWorldMonster == null) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_MONSTER_NOT_FOUND);
            return;
        }
        item = itemManager.subItem(player, itemId, 1, Reason.REBEL_ITEM_EXCHANGE);
        RebelPb.UseRebelPropRs.Builder builder = RebelPb.UseRebelPropRs.newBuilder();
        builder.setProp(item.wrapPb());
        handler.sendMsgToPlayer(RebelPb.UseRebelPropRs.ext, builder.build());
        RebelMonster rebelMonster = worldManager.addRebelMonster(pos, staticWorldMonster.getId(), staticWorldMonster.getLevel(), mapInfo, AddMonsterReason.ADD_REBEL_MONSTER, player.getCountry());
        rebelMonster.setCreateTime(System.currentTimeMillis());
        mapInfo.getRebelMap().put(pos, rebelMonster);
        player.addCallRebel();

        list.add(rebelMonster);
        worldManager.synEntityAddRq(list);
        eventManager.worldActRebel(player, 0, Lists.newArrayList(
                WorldActivityConsts.ACTIVITY_2,
                staticWorldMonster.getId(),
                staticWorldMonster.getLevel()));
    }

    public void rebelActivityOver() {
        ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
        Iterator<Map.Entry<Integer, MapInfo>> it = worldMapInfo.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, MapInfo> entry = it.next();
            MapInfo mapInfo = entry.getValue();
            //清除怪物
            Map<Pos, RebelMonster> rebelMap = mapInfo.getRebelMap();
            Iterator<Map.Entry<Pos, RebelMonster>> iterator = rebelMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Pos, RebelMonster> monsterEntry = iterator.next();
                RebelMonster rebelMonster = monsterEntry.getValue();
                clearRebelMonster(mapInfo, rebelMonster);
                //删除怪物
                iterator.remove();
            }
        }
        //活动结束发奖
        rebelAward();
    }


    public void checkMonsterTimeOver(WorldActPlan worldActPlan) {
        ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
        Iterator<Map.Entry<Integer, MapInfo>> it = worldMapInfo.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, MapInfo> entry = it.next();
            MapInfo mapInfo = entry.getValue();
            //清除怪物
            Map<Pos, RebelMonster> rebelMap = mapInfo.getRebelMap();
            Iterator<Map.Entry<Pos, RebelMonster>> iterator = rebelMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Pos, RebelMonster> monsterEntry = iterator.next();
                RebelMonster rebelMonster = monsterEntry.getValue();
                //(活动如果没结束)如果两个小时还没有被打  或者被打死
                boolean isOverTime = System.currentTimeMillis() - rebelMonster.getCreateTime() > 2 * TimeHelper.HOUR_MS;
                if (isOverTime || worldActPlan.getState() == WorldActPlanConsts.END) {
                    clearRebelMonster(mapInfo, rebelMonster);
                    //删除怪物
                    iterator.remove();
                }
            }
        }
    }


    public void clearRebelMonster(MapInfo mapInfo, RebelMonster rebelMonster) {
        // 清除野怪
        worldManager.clearRebelMonsterPos(mapInfo, rebelMonster.getPos());
        // 同步野怪
        worldManager.synEntityRemove(rebelMonster, mapInfo.getMapId(), rebelMonster.getPos());
        warManager.cancelRebelWar(rebelMonster, mapInfo, -1);

    }

    public void refreshRebelActivity() {
        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_2);
        if (worldActPlan == null) {
            return;
        }
        //检查是否结束
        if (worldActPlan.getState() == WorldActPlanConsts.OPEN) {
            //执行活动结束逻辑
            if (System.currentTimeMillis() > worldActPlan.getEndTime()) {
                rebelActivityOver();
                worldActPlan.setState(WorldActPlanConsts.END);
            }
            //检查怪物是否过期
            checkMonsterTimeOver(worldActPlan);
        }
    }

    public void exchangeRebelAward(ExchangeRebelAwardHandler handler, RebelPb.ExchangeRebelAwardRq req) {

        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("exchangeRebelAward player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        WorldActPlan worldActPlan = worldManager.getWorldActPlan(WorldActivityConsts.ACTIVITY_2);
        if (worldActPlan == null || worldActPlan.getState() != WorldActPlanConsts.OPEN) {
            LogHelper.CONFIG_LOGGER.info("exchangeRebelAward player is null {}", GameError.REBEL_ACTIVITY_ERROR);
            handler.sendErrorMsgToPlayer(GameError.REBEL_ACTIVITY_ERROR);
            return;
        }
        StaticRebelExchange staticRebelExchange = staticRebelMgr.getExchanges().get(req.getId());
        if (staticRebelExchange == null) {
            logger.error("StaticRebelExchange is not exist id {}", req.getId());
            return;
        }


        if (player.getLevel() < staticRebelExchange.getLimitLv()) {
            logger.error("exchangeRebelAward  error : level is not enough  ");
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }

        Integer exNum = player.getSimpleData().getRebelExchange().get(req.getId());
        if (exNum != null && exNum.intValue() >= staticRebelExchange.getMaxExNum()) {
            logger.error("exchangeRebelAward  error : exchange num  is not enough  ");
            handler.sendErrorMsgToPlayer(GameError.REBEL_EXCHANGE_NUM_NOT_ENOUGH);
            return;
        }
        boolean flag = itemManager.hasEnoughItem(player, RebelConsts.ITEM_ID, staticRebelExchange.getNeedNum());
        if (!flag) {
            logger.error("exchangeRebelAward  error : item num  is not enough  ");
            handler.sendErrorMsgToPlayer(GameError.ITEM_NUMBER_ERROR);
            return;
        }
        int num = 0;
        if (exNum != null) {
            num = exNum.intValue();
        }
        Item item = itemManager.subItem(player, RebelConsts.ITEM_ID, staticRebelExchange.getNeedNum(), Reason.REBEL_ITEM_EXCHANGE);
        Award award = new Award(staticRebelExchange.getAwardType(), staticRebelExchange.getAwardId(), staticRebelExchange.getAwardNum());
        //添加物品
        int keyId = playerManager.addAward(player, award, Reason.REBEL_ITEM_EXCHANGE);
        award.setKeyId(keyId);
        player.getSimpleData().addRebelExchange(req.getId());
        RebelPb.ExchangeRebelAwardRs.Builder builder = RebelPb.ExchangeRebelAwardRs.newBuilder();
        builder.setAward(award.wrapPb());
        builder.setProp(item.wrapPb());
        builder.setExchangeInfo(RebelPb.ExchangeInfo.newBuilder().setId(req.getId()).setNum(num + 1));
        handler.sendMsgToPlayer(RebelPb.ExchangeRebelAwardRs.ext, builder.build());
        eventManager.worldActRebel(player, 2, Lists.newArrayList(
                WorldActivityConsts.ACTIVITY_2,
                staticRebelExchange.getId(),
                1));
    }


    /**
     * 伏击叛军发放奖励
     */
    public void rebelAward() {
        Collection<RankInfo> rankInfos = rebelScoreRankMgr.getValues(0, rebelScoreRankMgr.getCapacity());
        int rank = 0;
        for (RankInfo r : rankInfos) {
            ++rank;
            List<Award> awards = staticRebelMgr.getRebelRankAward(1, rank);
            Player player = playerManager.getPlayer(r.getKey());
            playerManager.sendAttachMail(player, awards, MailId.REBEL_ACTIVITY_PERSON_AWARD, String.valueOf(player.getSimpleData().getRebelScore()), String.valueOf(rank));

        }

        for (Player player : playerManager.getPlayers().values()) {
            if (player.getSimpleData().getRebelScore() > 60) {
                int countryRank = rebelScoreRankMgr.getCountryRank(player.getCountry());
                if (countryRank == -1) {
                    continue;
                }
                List<Award> awards = staticRebelMgr.getRebelRankAward(2, countryRank);
                playerManager.sendAttachMail(player, awards, MailId.REBEL_ACTIVITY_COUNTRY_AWARD, String.valueOf(countryRank));
            }

            //未使用玩的道具  要转化成资源发给玩家,金币，跟钢铁
            Item item = itemManager.getItem(player, RebelConsts.ITEM_ID);
            int resourceNum = 0;

            int num = staticLimitMgr.getNum(192);
            if (item != null && item.getItemNum() >= 1) {
                resourceNum = item.getItemNum() * num;
                playerManager.subItem(player, item.getItemId(), item.getItemNum(), Reason.REBEL_ITEM_EXCHANGE);
            }

            Item item1 = itemManager.getItem(player, RebelConsts.ITEM_ID_1);
            int num1 = staticLimitMgr.getNum(195);
            if (item1 != null && item1.getItemNum() >= 1) {
                resourceNum = item1.getItemNum() * num1 + resourceNum;
                playerManager.subItem(player, item1.getItemId(), item1.getItemNum(), Reason.REBEL_ITEM_EXCHANGE);
            }

            Item item2 = itemManager.getItem(player, RebelConsts.ITEM_ID_2);
            int num2 = staticLimitMgr.getNum(196);

            if (item2 != null && item2.getItemNum() >= 1) {
                resourceNum = item2.getItemNum() * num2 + resourceNum;
                playerManager.subItem(player, item2.getItemId(), item2.getItemNum(), Reason.REBEL_ITEM_EXCHANGE);
            }

            Item item3 = itemManager.getItem(player, RebelConsts.ITEM_ID_3);
            int num3 = staticLimitMgr.getNum(197);
            if (item3 != null && item3.getItemNum() >= 1) {
                resourceNum = item3.getItemNum() * num3 + resourceNum;
                playerManager.subItem(player, item3.getItemId(), item3.getItemNum(), Reason.REBEL_ITEM_EXCHANGE);
            }

            if (resourceNum > 0) {
                Award award = new Award(AwardType.RESOURCE, ResourceType.IRON, resourceNum);
                Award award1 = new Award(AwardType.RESOURCE, ResourceType.COPPER, resourceNum);
                List<Award> resourceAward = new ArrayList<>();
                resourceAward.add(award);
                resourceAward.add(award1);
                //发送兑换资源邮件
                playerManager.sendAttachMail(player, resourceAward, MailId.REBEL_ITEM_EXCHANGE_RESOURCE);
            }
        }

    }

    /***
     * 拿到伏击叛军的排行
     * @param handler
     */
    public void getRebelScoreRank(GetRebelScoreRankHandler handler) {
        RankPb.GetRebelScoreRankRs.Builder builder = RankPb.GetRebelScoreRankRs.newBuilder();
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("exchangeRebelAward player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        boolean flag = false;
        for (int rank : ranks) {
            RankInfo rankInfo = rebelScoreRankMgr.getByIndex(rank - 1);
            if (rankInfo == null) {
                continue;
            }
            Player p = playerManager.getPlayer(rankInfo.getKey());
            if (player.getLord().getLordId() == p.getLord().getLordId()) {
                flag = true;
            }
            RankPb.RebelScoreRank.Builder rebelScoreRank = RankPb.RebelScoreRank.newBuilder();
            rebelScoreRank.setLordId(p.getLord().getLordId());
            rebelScoreRank.setName(p.getNick());
            rebelScoreRank.setRank(rank);
            rebelScoreRank.setScore(p.getSimpleData().getRebelScore());
            builder.addRank(rebelScoreRank);

        }
        //本人的排名没在rank集合 要单独给过去
        if (!flag) {
            int index = rebelScoreRankMgr.indexOf(player.getLord().getLordId());
            RankPb.RebelScoreRank.Builder rebelScoreRank = RankPb.RebelScoreRank.newBuilder();
            rebelScoreRank.setLordId(player.getLord().getLordId());
            rebelScoreRank.setName(player.getNick());
            rebelScoreRank.setRank(index == -1 ? -1 : index + 1);
            rebelScoreRank.setScore(player.getSimpleData().getRebelScore());
            builder.addRank(rebelScoreRank);
        }

        List<CountryScore> countryScores = rebelScoreRankMgr.getCountryScores();
        for (CountryScore countryScore : countryScores) {
            RankPb.CountryScoreRank.Builder countryScoreRank = RankPb.CountryScoreRank.newBuilder();
            countryScoreRank.setCountryId(countryScore.getCountryId());
            countryScoreRank.setScore(countryScore.getScore());
            builder.addCountryRank(countryScoreRank);

        }
        handler.sendMsgToPlayer(RankPb.GetRebelScoreRankRs.ext, builder.build());

    }

    public void getExchangeInfo(GetExchangeInfoHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("exchangeRebelAward player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        RebelPb.GetExchangeInfoRs.Builder builder = RebelPb.GetExchangeInfoRs.newBuilder();
        for (Map.Entry<Integer, Integer> e : player.getSimpleData().getRebelExchange().entrySet()) {
            RebelPb.ExchangeInfo.Builder exchangeInfo = RebelPb.ExchangeInfo.newBuilder();
            exchangeInfo.setId(e.getKey());
            exchangeInfo.setNum(e.getValue());
            builder.addExchangeInfo(exchangeInfo);
        }
        handler.sendMsgToPlayer(RebelPb.GetExchangeInfoRs.ext, builder.build());
	}

	/**
	 * 首次进入伏击叛军引导奖励
	 **/
	public void rebelGuideAwardRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		SimpleData simpleData = player.getSimpleData();
		RebelGuideAwardRs.Builder builder = RebelGuideAwardRs.newBuilder();
		if (!simpleData.isFirstRebelGuideAward()) {
            playerManager.addAward(player,AwardType.PROP,189,1,Reason.REBEL_ITEM_EXCHANGE);
			builder.setAward(CommonPb.Award.newBuilder().setType(AwardType.PROP).setId(189).setCount(1).build());
			simpleData.setFirstRebelGuideAward(true);
		}
		handler.sendMsgToPlayer(RebelGuideAwardRs.ext, builder.build());
	}
}
