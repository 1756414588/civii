package com.game.service;

import com.game.constant.AwardType;
import com.game.constant.MarchReason;
import com.game.constant.Reason;
import com.game.constant.RiotBuff;
import com.game.constant.SimpleId;
import com.game.constant.SoldierType;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticRiotMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.p.Hero;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticRoitItemShop;
import com.game.domain.s.StaticRoitScoreShop;
import com.game.domain.s.StaticWorldMonster;
import com.game.manager.*;
import com.game.manager.RiotManager;
import com.game.manager.WorldManager;
import com.game.log.consumer.EventManager;
import com.game.pb.CommonPb;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.chat.domain.Chat;
import com.game.constant.ChatId;
import com.game.constant.GameError;
import com.game.domain.Player;
import com.game.domain.p.SimpleData;
import com.game.message.handler.ClientHandler;
import com.game.pb.RiotPb;

import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Service
public class RiotService {
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private ChatManager chatManager;
    @Autowired
    private StaticRiotMgr staticRoitMgr;
    @Autowired
    private StaticPropMgr staticPropMgr;
    @Autowired
    private StaticLimitMgr staticLimitMgr;
    @Autowired
    private WorldManager worldManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private StaticWorldMgr worldMgr;
    @Autowired
    private RiotManager riotManager;
    @Autowired
    private WarBookManager warBookManager;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private RoitService roitService;


    // 请求支援
    public void riotWarHelpRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        SimpleData simpleData = player.getSimpleData();
        if (simpleData == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        WarInfo warInfo = simpleData.getRiotWarInfo();
        if (warInfo.getDefencerHelpTime() > 3) { // 支援次数最大为3次
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        warInfo.setDefencerHelpTime(warInfo.getDefencerHelpTime() + 1);
        StaticWorldMonster worldMonster = worldMgr.getMonster((int) warInfo.getAttackerId());
        Chat chat = chatManager.createManShare(player,
                ChatId.RIOT_DEFENCE,
                worldMonster.getName(),
                player.getPosStr());
        chatManager.sendCountryShare(player, chat);
        RiotPb.RiotWarHelpRs.Builder builder = RiotPb.RiotWarHelpRs.newBuilder();
        int officeId = SpringUtil.getBean(CountryManager.class).getOfficeId(player);
        CommonPb.Chat b = chatManager.addCountryChat(player.getCountry(), officeId, chat);
        builder.setChat(b);
        builder.setKeyId(warInfo.getWarId());
        handler.sendMsgToPlayer(RiotPb.RiotWarHelpRs.ext, builder.build());
    }

    /**
     * 信物商店信息
     *
     * @param handler
     * @param rq
     */
    public void riotItemShop(ClientHandler handler, RiotPb.RiotItemShopRq rq) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        RiotPb.RiotItemShopRs.Builder builder = RiotPb.RiotItemShopRs.newBuilder();
        Map<Integer, Integer> riotItemExchanges = player.getSimpleData().getRiotItemExchange();
        staticRoitMgr.getRoitItemShopMap().forEach((e, f) -> {
            Integer num = riotItemExchanges.get(e);
            if (num == null) {
                num = 0;
            }
            builder.addItemProps(CommonPb.RiotItemProp.newBuilder().setKeyId(e).setBuyNum(num).build());
        });
        handler.sendMsgToPlayer(RiotPb.RiotItemShopRs.ext, builder.build());
    }

    public void riotItemShopBuy(ClientHandler handler, RiotPb.RiotItemShopBuyRq rq) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int keyId = rq.getKeyId();
        StaticRoitItemShop shop = staticRoitMgr.getRoitItemShopMap().get(keyId);
        if (shop == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        int buyNum = rq.getNum();
        int type = rq.getType();
        Integer num = player.getSimpleData().getRiotItemExchange().get(shop.getKeyId());
        if (num == null) {
            num = 0;
        }
        if (num >= shop.getMaxNum() && shop.getMaxNum() != 0) {
            handler.sendErrorMsgToPlayer(GameError.PROP_BUY_ITEM_NOT_EXISTS);
            return;
        }
        switch (type) {
            case 1:
                buyUseItem(handler, player, shop, buyNum);
                break;
            default:
                buyUseGold(handler, player, shop, buyNum);
                break;
        }
    }

    private void buyUseItem(ClientHandler handler, Player player, StaticRoitItemShop shop, int buyNum) {
        int needItemNum = shop.getItemNum() * buyNum;
        if (player.getSimpleData().getRiotItem() < needItemNum) {
            handler.sendErrorMsgToPlayer(GameError.RIOT_ITEM_NOT_ENOUGH);
            return;
        }
        //扣除信物
        playerManager.subRiotItem(player, needItemNum, Reason.RIOT_ITEM_BUY);
        //记录兑换记录
        Integer num = player.getSimpleData().getRiotItemExchange().get(shop.getKeyId());
        if (num == null) {
            num = 0;
        }
        num += buyNum;
        player.getSimpleData().getRiotItemExchange().put(shop.getKeyId(), num);
        //购买返回
        RiotPb.RiotItemShopBuyRs.Builder builder = RiotPb.RiotItemShopBuyRs.newBuilder();
        builder.setItemProps(CommonPb.RiotItemProp.newBuilder().setKeyId(shop.getKeyId()).setBuyNum(num).build());
        //推送变化信息
        addAward(shop, player, Reason.RIOT_ITEM_BUY, builder);
        handler.sendMsgToPlayer(RiotPb.RiotItemShopBuyRs.ext, builder.build());
        riotManager.synRiotBuff(player);
    }

    private void buyUseGold(ClientHandler handler, Player player, StaticRoitItemShop shop, int buyNum) {
        int needItemNum = shop.getGold() * buyNum;
        if (player.getGold() < needItemNum) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }
        //扣钻
        playerManager.subGold(player, needItemNum, Reason.RIOT_GOLD_BUY);
        //记录兑换记录
        Integer num = player.getSimpleData().getRiotItemExchange().get(shop.getKeyId());
        if (num == null) {
            num = 0;
        }
        num += buyNum;
        player.getSimpleData().getRiotItemExchange().put(shop.getKeyId(), num);
        //购买返回
        RiotPb.RiotItemShopBuyRs.Builder builder = RiotPb.RiotItemShopBuyRs.newBuilder();
        builder.setItemProps(CommonPb.RiotItemProp.newBuilder().setKeyId(shop.getKeyId()).setBuyNum(num).build());
        addAward(shop, player, Reason.RIOT_GOLD_BUY, builder);
        handler.sendMsgToPlayer(RiotPb.RiotItemShopBuyRs.ext, builder.build());

        //推送变化信息
        riotManager.synRiotBuff(player);
    }

    private void addAward(StaticRoitItemShop shop, Player player, int reason, RiotPb.RiotItemShopBuyRs.Builder builder) {
        int effectType = shop.getEffect().get(0);
        int effectCount = shop.getEffect().get(1);
        SimpleData data = player.getSimpleData();
        switch (effectType) {
            case 1: //增加虫族入侵所有英雄攻击
                Integer attact = data.getRiotBuff().get(RiotBuff.ATTACK);
                attact = attact == null ? 0 : attact;
                attact += effectCount;
                data.getRiotBuff().put(RiotBuff.ATTACK, attact);
                builder.setAward(PbHelper.createAward(AwardType.PROP, shop.getPropId(), 1).build());
                break;
            case 2: //增加虫族入侵所有英雄防御
                Integer defence = data.getRiotBuff().get(RiotBuff.DEFENCE);
                defence = defence == null ? 0 : defence;
                defence += effectCount;
                data.getRiotBuff().put(RiotBuff.DEFENCE, defence);
                builder.setAward(PbHelper.createAward(AwardType.PROP, shop.getPropId(), 1).build());
                break;
            case 3://减少下拨虫族入侵宾利
                Integer lessTroops = data.getRiotBuff().get(RiotBuff.LESSTROOPS);
                lessTroops = lessTroops == null ? 0 : lessTroops;
                lessTroops += effectCount;
                data.getRiotBuff().put(RiotBuff.LESSTROOPS, lessTroops);
                builder.setAward(PbHelper.createAward(AwardType.PROP, shop.getPropId(), 1).build());
                break;
            case 4://步兵
            case 5://坦克兵
            case 6://炮兵
                StaticProp staticProp = staticPropMgr.getStaticProp(shop.getPropId());
                int soldierType = getSoldierType(staticProp.getPropType());
                playerManager.addAward(player, AwardType.SOLDIER, soldierType, effectCount, reason);
                builder.setAward(PbHelper.createAward(AwardType.SOLDIER, soldierType, effectCount).build());
                break;
        }
        WorldActPlan worldActPlan = roitService.getWorldRoitActPlan();
        eventManager.worldActRiot(player, 1,
                Lists.newArrayList(
                        worldActPlan.getId(),
                        effectType,
                        shop.getPropId(),
                        effectCount
                ));
    }

    /**
     * 虫族入侵参加防守
     *
     * @param handler
     */
    public void attendRiotCityRq(RiotPb.AttendRiotCityRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Player target = playerManager.getPlayer(req.getRoleId());
        if (target == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (player.roleId == target.roleId) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        // 增加等级限制
        int playerLevel = player.getLevel();
        if (playerLevel < staticLimitMgr.getNum(SimpleId.REBAL_ATTACK_LV)) {
            handler.sendErrorMsgToPlayer(GameError.CITY_WAR_LEVEL_NOT_ENOUGH);
            return;
        }
        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        int warMapId = worldManager.getMapId(target.getPos());
        WarInfo warInfo = target.getSimpleData().getRiotWarInfo();
        if (warInfo == null) {
            handler.sendErrorMsgToPlayer(GameError.WAR_IS_OVER);
            return;
        }
        // 相同国家不能发生城战
        int defencerCountry = target.getCountry();
        int myCountry = player.getCountry();
        if (defencerCountry != myCountry) {
            handler.sendErrorMsgToPlayer(GameError.NOT_SAME_MAP);
            return;
        }
        int side = 2;
        // 是否在一个区域
        if (mapId != warMapId) { //不在同一个地图
            handler.sendErrorMsgToPlayer(GameError.NOT_SAME_MAP);
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
            if (!worldService.isEmbattle(player, heroId)) {
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
        Pos targetPos = target.getPos();

        // 检查行军时间
        Pos playerPos = player.getPos();

        //兵书对行军的影响值
        float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);
        long period = worldManager.getPeriod(player, playerPos, targetPos,bookEffectMarch);
        long attackPeriod = staticLimitMgr.getNum(23) * TimeHelper.SECOND_MS;

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
        if (!worldService.isMarchWarOk(march.getPeriod(), warInfo)) {
            handler.sendErrorMsgToPlayer(GameError.TO_LONG_MARCH);
            return;
        }

        march.setDefencerId(warInfo.getDefencerId());
        march.setAttackerId(warInfo.getAttackerId());
        march.setSide(side);
        march.setMarchType(MarchType.RiotWar);

        march.setWarId(warInfo.getWarId());
        march.setFightTime(warInfo.getEndTime(), MarchReason.AttendPvpWar);
        // add march to player
        player.addMarch(march);
        // attack or defence
        worldManager.synAddCityWar(target, warInfo);
        // add world map
        worldManager.addMarch(mapId, march);
        warInfo.addDefenceMarch(march);

        // return msg
        RiotPb.AttendRiotCityRs.Builder builder = RiotPb.AttendRiotCityRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        handler.sendMsgToPlayer(RiotPb.AttendRiotCityRs.ext, builder.build());
        worldManager.synMarch(mapInfo.getMapId(), march);
    }


    public int getSoldierType(int propType) {
        if (propType == 43) {
            return SoldierType.ROCKET_TYPE;
        } else if (propType == 44) {
            return SoldierType.TANK_TYPE;
        } else if (propType == 45) {
            return SoldierType.WAR_CAR;
        }

        return 0;
    }

    public void riotScoreShopBuy(ClientHandler handler, RiotPb.RiotScoreShopBuyRq rq) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int num = rq.getNum();
        StaticRoitScoreShop shop = staticRoitMgr.getRoitScoreShopMap().get(rq.getKeyId());
        int needScore = num * shop.getScore();
        if (num * shop.getScore() > player.getSimpleData().getRiotScore()) {
            handler.sendErrorMsgToPlayer(GameError.RIOT_SCORE_NOT_ENOUGH);
            return;
        }
        playerManager.subRiotScore(player, needScore, Reason.RIOT_SCORE_BUY);
        playerManager.addItem(player, shop.getPropId(), num, Reason.RIOT_SCORE_BUY);
        RiotPb.RiotScoreShopBuyRs.Builder builder = RiotPb.RiotScoreShopBuyRs.newBuilder();
        builder.setAward(PbHelper.createAward(AwardType.PROP, shop.getPropId(), num));
        handler.sendMsgToPlayer(RiotPb.RiotScoreShopBuyRs.ext, builder.build());
        riotManager.synRiotBuff(player);
    }
}
