package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticDepotMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticResPackagerMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticResPackager;
import com.game.log.constant.*;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.cs.GetResourcePacketHandler;
import com.game.message.handler.cs.ResourcePacketHandler;
import com.game.pb.CommonPb;
import com.game.pb.DepotPb;
import com.game.pb.DepotPb.*;
import com.game.season.SeasonManager;
import com.game.season.talent.entity.EffectType;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

@Service
public class DepotService {

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticDepotMgr staticDepotMgr;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private BuildingManager buildingMgr;

    @Autowired
    private StaticResPackagerMgr staticResPackagerMgr;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private SurpriseGiftManager surpriseGiftManager;
    @Autowired
    SeasonManager seasonManager;


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 翻聚宝盆
     *
     * @param req
     * @param handler
     */
    public void openDepotRq(OpenDepotRq req, ClientHandler handler) {
        int grid = req.getGrid();
        if (grid < 1 || grid > 9 || grid == 5) {
            handler.sendErrorMsgToPlayer(GameError.GRID_ERROR);
            return;
        }

        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Building building = player.buildings;
        if (building == null || building.getBuilding(BuildingType.MARKET).getLevel() < 1) {
            logger.error("DepotService openDepotRq error : market level is error ");
            return;
        }
        if (player.getLord().getDepotRefresh() != GameServer.getInstance().currentDay) {
            player.getDepots().clear();
            player.getDepots().addAll(staticDepotMgr.getRandomDeport(player.getLevel()));
            player.getLord().setDepotRefresh(GameServer.getInstance().currentDay);
        }

        Lord lord = player.getLord();
        long depotTime = lord.getDepotTime();
        long now = System.currentTimeMillis();

        int buf = seasonManager.getBuf(player, EffectType.EFFECT_TYPE26);// EFFECT_TYPE26(26, "市场兑换资源单次冷却时间降低（固定数值）"),
        long l = TimeHelper.HOUR_MS - buf;
        if (depotTime + TimeHelper.HOUR_MS >= now) {
            handler.sendErrorMsgToPlayer(GameError.DEPOT_CD);
            return;
        }

        player.getLord().setDepotTime(now);

        Depot depot = null;
        for (Depot entity : player.getDepots()) {
            int engrid = entity.getGrid();
            if (engrid == grid) {
                entity.setState(DepotType.OPEN_YES);
                depot = entity;
                break;
            }
        }
        if (depot == null) {
            handler.sendErrorMsgToPlayer(GameError.DEPOT_GRID_NO_EXIST);
            return;
        }

        // 完成聚宝盆任务
        taskManager.doTask(TaskType.DEPOT, player);

        OpenDepotRs.Builder builder = OpenDepotRs.newBuilder();
        builder.setDepot(depot.ser());
        handler.sendMsgToPlayer(OpenDepotRs.ext, builder.build());

        surpriseGiftManager.doSurpriseGift(player,SuripriseId.MarkerFlop,1,true);
        activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.Marker_Flop, 0, 1);
    }

    /**
     * 聚宝盆购买
     *
     * @param req
     * @param handler
     */
    public void buyDepotRq(BuyDepotRq req, ClientHandler handler) {

        int grid = req.getGrid();
        if (grid < 1 || grid > 9) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Building building = player.buildings;
        if (building == null || building.getBuilding(BuildingType.MARKET).getLevel() < 1) {
            logger.error("DepotService buyDepotRq error : market level is error ");
            return;
        }
        Lord lord = player.getLord();
        if (lord.getDepotBuyTime() == GameServer.getInstance().currentDay) {
            handler.sendErrorMsgToPlayer(GameError.DEPOT_IS_BUY);
            return;
        }

        Depot depot = null;
        for (Depot entity : player.getDepots()) {
            if (entity.getGrid() == grid) {
                depot = entity;
                break;
            }
        }

        if (depot == null) {
            handler.sendErrorMsgToPlayer(GameError.DEPOT_GRID_NO_EXIST);
            return;
        }

        if (depot.getGold() > 0) {
            if (player.getGold() < depot.getGold()) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            playerManager.subAward(player, AwardType.GOLD, 1, depot.getGold(), Reason.BUY_DEPOT);
        }
        if (depot.getIron() > 0) {
            if (player.getResource().getIron() < depot.getIron()) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_IRON);
                return;
            }
            playerManager.subAward(player, AwardType.RESOURCE, ResourceType.IRON, depot.getIron(), Reason.IRON_BUY_DEPOT);

            /**
             * 市场购买资源消耗日志埋点
             */
            com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
            //消耗
            logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(),
                    player.account.getCreateDate(),
                    player.getLevel(),
                    player.getNick(),
                    player.getVip(),
                    player.getCountry(),
                    player.getTitle(),
                    player.getHonor(),
                    player.getResource(ResourceType.IRON),
                    RoleResourceLog.OPERATE_OUT, ResourceType.IRON, ResOperateType.SHOP_BUY_OUT.getInfoType(), depot.getIron(), player.account.getChannel()));
            logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
                    player.getNick(),
                    player.getLevel(),
                    player.getTitle(),
                    player.getHonor(),
                    player.getCountry(),
                    player.getVip(),
                    player.account.getChannel(),
                    1, depot.getIron(), IronOperateType.SHOP_BUY_OUT.getInfoType()) , 1);

        }
        lord.setDepotBuyTime(GameServer.getInstance().currentDay);
        Award award = depot.getAward();
        playerManager.addAward(player, award, Reason.BUY_DEPOT_ITEM);
        BuyDepotRs.Builder builder = BuyDepotRs.newBuilder();
        builder.setAward(award.wrapPb());
        handler.sendMsgToPlayer(BuyDepotRs.ext, builder.build());

        // 完成聚宝盆任务
        taskManager.doTask(TaskType.DEPOT, player);

        //更新通行证活动进度
        ActivityEventManager.getInst().activityTip(EventEnum.MARKET_BUY, player, 1, 0);
//        activityManager.updatePassPortTaskCond(player,ActPassPortTaskType.BUY_DEPOT_PROP,1);
    }

    /**
     * 资源兑换
     *
     * @param req
     * @param handler
     */
    public void exchangeResRq(ExchangeResRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Building building = player.buildings;
        if (building == null || building.getBuilding(BuildingType.MARKET).getLevel() < 2) {
            logger.error("DepotService buyDepotRq error : market level is error ");
            return;
        }
        Lord lord = player.getLord();

        int outId = req.getOutResId();
        int getId = req.getGetResId();
        if (outId == getId) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        if (outId < ResourceType.IRON || outId > ResourceType.OIL) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        if (getId < ResourceType.IRON || getId > ResourceType.OIL) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        long everyCd = staticLimitMgr.getNum(116);
        long maxCfgCd = staticLimitMgr.getNum(117);

        long now = System.currentTimeMillis();
        if (lord.getExchangeRes() <= now) {
            lord.setExchangeRes(now);
        }

        if (lord.getExchangeRes() + everyCd >= now + maxCfgCd) {
            handler.sendErrorMsgToPlayer(GameError.CD_EXCHANGE_RES);
            return;
        }

        // 基础加成
        CommonPb.Resource.Builder resAdd = buildingMgr.getAllResAdd(player);
        long resTarget = 0L;
        if (outId == ResourceType.IRON) {
            resTarget = resAdd.getIron();
        } else if (outId == ResourceType.COPPER) {
            resTarget = resAdd.getCopper();
        } else if (outId == ResourceType.OIL) {
            resTarget = resAdd.getOil();
        }

        long cdTime = lord.getExchangeRes() + everyCd;
        lord.setExchangeRes(cdTime);

        long addValue = (long) ((double) resTarget * staticDepotMgr.getExchange(outId, getId));

        // add && sub
        playerManager.subResource(player, outId, resTarget, Reason.EXCHANGE_RES);
        playerManager.addResource(player, getId, addValue, Reason.EXCHANGE_RES);

        /**
         * 市场兑换资源产出和消耗日志埋点
         */
        com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
        //产出
        logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(),
                player.account.getCreateDate(),
                player.getLevel(),
                player.getNick(),
                player.getVip(),
                player.getCountry(),
                player.getTitle(),
                player.getHonor(),
                player.getResource(getId),
                RoleResourceLog.OPERATE_IN, getId, ResOperateType.SHOP_EXCHANGE_IN.getInfoType(), resTarget, player.account.getChannel()));
        int t = 0;
        int resType = getId;
        switch (resType){
            case ResourceType.IRON:
                t = IronOperateType.SHOP_EXCHANGE_IN.getInfoType();
                break;
            case ResourceType.COPPER:
                t = CopperOperateType.SHOP_EXCHANGE_IN.getInfoType();
                break;
            case ResourceType.OIL:
                t = OilOperateType.SHOP_EXCHANGE_IN.getInfoType();
                break;
            default:
                break;
        }
        if (t!=0){
            logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
                    player.getNick(),
                    player.getLevel(),
                    player.getTitle(),
                    player.getHonor(),
                    player.getCountry(),
                    player.getVip(),
                    player.account.getChannel(),
                    1, resTarget, t) , resType);
        }
        //消耗
        logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(),
                player.account.getCreateDate(),
                player.getLevel(),
                player.getNick(),
                player.getVip(),
                player.getCountry(),
                player.getTitle(),
                player.getHonor(),
                player.getResource(outId),
                RoleResourceLog.OPERATE_OUT, outId, ResOperateType.SHOP_EXCHANGE_OUT.getInfoType(), addValue, player.account.getChannel()));
        int t1 = 0;
        int resType1 = outId;
        switch (resType){
            case ResourceType.IRON:
                t1 = IronOperateType.SHOP_EXCHANGE_OUT.getInfoType();
                break;
            case ResourceType.COPPER:
                t1 = CopperOperateType.SHOP_EXCHANGE_OUT.getInfoType();
                break;
            case ResourceType.OIL:
                t1 = OilOperateType.SHOP_EXCHANGE_OUT.getInfoType();
                break;
            default:
                break;
        }
        if (t1!=0){
            logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
                    player.getNick(),
                    player.getLevel(),
                    player.getTitle(),
                    player.getHonor(),
                    player.getCountry(),
                    player.getVip(),
                    player.account.getChannel(),
                    0, addValue, t1) , resType1);
        }



        ExchangeResRs.Builder builder = ExchangeResRs.newBuilder();
        builder.setExchangeCD(lord.getExchangeRes());
        builder.setResource(player.getResource().wrapPb());
        handler.sendMsgToPlayer(ExchangeResRs.ext, builder.build());
    }

    /**
     * @param handler
     */
    public void getResourcePacket(GetResourcePacketHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        Iterator<Map.Entry<Integer, ResourcePacket>> iterator = player.getResPackets().entrySet().iterator();
        long now = System.currentTimeMillis();
        DepotPb.GetResourcePacketRs.Builder builder = DepotPb.GetResourcePacketRs.newBuilder();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ResourcePacket> entry = iterator.next();
            ResourcePacket resourcePacket = entry.getValue();
            boolean sameDayOfMillis = TimeHelper.isSameDayOfMillis(resourcePacket.getPacketTime(), now);
            if (!sameDayOfMillis) {
                entry.getValue().setPacketNum(0);
                entry.getValue().setPacketTime(now);
            }
            builder.addResPacketInfos(CommonPb.ResPacketInfo.newBuilder().setResId(resourcePacket.getResId()).setPacketNum(resourcePacket.getPacketNum()));
        }

        handler.sendMsgToPlayer(DepotPb.GetResourcePacketRs.ext, builder.build());
    }


    /**
     * 资源打包
     *
     * @param handler
     * @param req
     */
    public void resourcePacket(ResourcePacketHandler handler, DepotPb.ResourcePacketRq req) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Building building = player.buildings;
        if (building == null || building.getBuilding(BuildingType.MARKET).getLevel() < 3) {
            logger.error("DepotService resourcePacket error : market level is error ");
            return;
        }
        ResourcePacket resourcePacket = player.getResPackets().get(req.getResId());
        if (resourcePacket == null) {
            resourcePacket = new ResourcePacket();
            resourcePacket.setResId(req.getResId());
            resourcePacket.setPacketTime(System.currentTimeMillis());
            player.getResPackets().put(resourcePacket.getResId(), resourcePacket);
        }


        long now = System.currentTimeMillis();
        boolean isSameDay = TimeHelper.isSameDayOfMillis(now, resourcePacket.getPacketTime());
        if (!isSameDay) {
            resourcePacket.setPacketNum(0);
            resourcePacket.setPacketTime(now);
        }

        int packetNum = resourcePacket.getPacketNum() + 1;
        //超过15次 按十五次计算
        packetNum = packetNum >= 15 ? 15 : packetNum;

        StaticResPackager staticResPackager = staticResPackagerMgr.getStaticResPackager(req.getResId(), packetNum);
        if (staticResPackager == null) {
            logger.error("DepotService resourcePacket staticResPackager is not exist resId {} ,packetTime {}", req.getResId(), packetNum);
            return;
        }
        //判断元宝够不够
        if (player.getGold() < staticResPackager.getCostGold()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            logger.error("resourcePacket GameError {}", GameError.NOT_ENOUGH_GOLD.toString());
            return;
        }
        long resNum = player.getResource(req.getResId());

        if (resNum < staticResPackager.getResNum()) {
            handler.sendErrorMsgToPlayer(GameError.RES_NOT_ENOUGH);
            logger.error("resourcePacket GameError {} resId {} resNum", GameError.RES_NOT_ENOUGH.toString(), req.getResId(), resNum);
            return;
        }
        //扣钱
        playerManager.subAward(player, AwardType.GOLD, 0, staticResPackager.getCostGold(), Reason.RES_PACKET_COST);
        //扣资源
        playerManager.subAward(player, AwardType.RESOURCE, req.getResId(), staticResPackager.getResNum(), Reason.RES_PACKET_COST);
        com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
        int resType=req.getResId();
        long res=staticResPackager.getResNum();
        /**
         * 市场打包资源消耗日志埋点
         */
        logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(),
                player.account.getCreateDate(),
                player.getLevel(),
                player.getNick(),
                player.getVip(),
                player.getCountry(),
                player.getTitle(),
                player.getHonor(),
                player.getResource(req.getResId()),
                RoleResourceLog.OPERATE_OUT, req.getResId(), ResOperateType.SHOP_PACK_OUT.getInfoType(), staticResPackager.getResNum(), player.account.getChannel()));
        int t = 0;
        switch (req.getResId()){
            case ResourceType.IRON:
                t = IronOperateType.SHOP_PACK_OUT.getInfoType();
                break;
            case ResourceType.COPPER:
                t = CopperOperateType.SHOP_PACK_OUT.getInfoType();
                break;
            case ResourceType.OIL:
                t = OilOperateType.SHOP_PACK_OUT.getInfoType();
                break;
            default:
                break;
        }
        if (t!=0){
            logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
                    player.getNick(),
                    player.getLevel(),
                    player.getTitle(),
                    player.getHonor(),
                    player.getCountry(),
                    player.getVip(),
                    player.account.getChannel(),
                    1, staticResPackager.getResNum(), t) , req.getResId());
        }
        //添加物品
        Award award = staticResPackager.getResAward();
        playerManager.addAward(player, award, Reason.RES_PACKET_ADD);
        resourcePacket.setPacketNum(packetNum);
        resourcePacket.setPacketTime(now);
        DepotPb.ResourcePacketRs.Builder builder = DepotPb.ResourcePacketRs.newBuilder();
        CommonPb.ResPacketInfo.Builder info = CommonPb.ResPacketInfo.newBuilder();
        info.setResId(resourcePacket.getResId());
        info.setPacketNum(packetNum);
        builder.setResPacketInfo(info);
        builder.setGold(player.getGold());
        builder.setAwards(award.wrapPb());
        builder.setResource(player.getResource().wrapPb());
        handler.sendMsgToPlayer(DepotPb.ResourcePacketRs.ext, builder.build());
    }
}
