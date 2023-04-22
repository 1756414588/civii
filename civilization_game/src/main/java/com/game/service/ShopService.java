package com.game.service;

import com.game.chat.domain.SystemChat;
import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.Player;
import com.game.domain.ShopInfo;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.log.constant.*;
import com.game.log.consumer.EventManager;
import com.game.log.domain.RoleItemLog;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.ShopPb;
import com.game.pb.ShopPb.*;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ShopService {

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private BuildingManager buildingManager;

    @Autowired
    private ItemManager itemManager;

    @Autowired
    private StaticPropMgr staticPropMgr;

    @Autowired
    private StaticVipMgr staticVipMgr;

    @Autowired
    private StaticWorkShopMgr staticWorkShopMgr;

    @Autowired
    private ChatManager chatManager;

    @Autowired
    private ChatService chatService;

    @Autowired
    private StaticHeroMgr staticHeroMgr;

    @Autowired
    private SurpriseGiftManager surpriseGiftManager;
    @Autowired
    private EventManager eventManager;

    @Autowired
    TaskManager taskManager;

    // 资源强征集道具
    public boolean isCollectType(int itemType) {
        return itemType == ItemType.COLLECT_IRON_PROP || itemType == ItemType.COLLECT_COPPER_PROP || itemType == ItemType.COLLECT_OIL_PROP
                || itemType == ItemType.COLLECT_STONE_PROP;
    }

    /**
     * 军事商店,其他商店
     *
     * @param handler
     */
    public void getShopRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int level = player.getLevel();

        ShopInfo shopInfo = playerManager.getShopInfo(player);

        GetShopRs.Builder builder = GetShopRs.newBuilder();

        List<StaticProp> shopList = staticPropMgr.getShops();
        for (StaticProp staticProp : shopList) {
            StaticVipBuy staticLimit = staticPropMgr.getShopLimit(staticProp.getShopType(), staticProp.getPropId());
            if (staticLimit == null || staticLimit.getLevelDisplay() > level) {
                continue;
            }
            CommonPb.Shop.Builder shopBuilder = PbHelper.createShop(staticProp);
            if (shopInfo.isDiscount(staticProp)) {// 折扣价格
                shopBuilder.setDiscount(staticProp.getPrice() * 9 / 10);
            }
            builder.addShop(shopBuilder.build());
        }

        handler.sendMsgToPlayer(GetShopRs.ext, builder.build());
    }

    /**
     * 军事商店,其他商店
     *
     * @param req
     * @param handler
     */
    public void buyShopRq(BuyShopRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int propId = req.getPropId();
        int num = req.getNum();
        if (num <= 0) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        StaticProp staticProp = staticPropMgr.getStaticProp(propId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_EXIST_SHOP);
            return;
        }

        if (staticProp.getShopType() != ShopType.MILITARYS_SHOP && staticProp.getShopType() != ShopType.SHOP) {
            handler.sendErrorMsgToPlayer(GameError.NO_EXIST_SHOP);
            return;
        }
        ShopInfo shopInfo = playerManager.getShopInfo(player);

        int price = staticProp.getPrice() * num;
        if (shopInfo.isDiscount(staticProp)) {// 折扣价格
            price = price * 9 / 10;
        }

        if (price <= 0) {// 保护,避免数据库填写错误
            handler.sendErrorMsgToPlayer(GameError.NO_EXIST_SHOP);
            return;
        }

        if (player.getLord().getGold() < price) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        playerManager.subAward(player, AwardType.GOLD, 0, price, Reason.BUY_SHOP);
        com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
        Item item = itemManager.addItem(player, propId, num, Reason.BUY_SHOP);
        // playerManager.addAward(player, AwardType.PROP, propId, num,
        // Reason.BUY_SHOP);
        /**
         * 添加道具获得埋点
         */
        logUser.roleItemLog(new RoleItemLog(player.roleId, propId, num, RoleItemLog.ITEM_ADD, Reason.BUY_SHOP));

        BuyShopRs.Builder builder = BuyShopRs.newBuilder();
        builder.setGold(player.getLord().getGold());
        builder.addAward(PbHelper.createAward(AwardType.PROP, propId, num).build());
        builder.setProp(PbHelper.createItemPb(item.getItemId(), item.getItemNum()));
        handler.sendMsgToPlayer(BuyShopRs.ext, builder.build());
        SpringUtil.getBean(EventManager.class).shopping(player, Lists.newArrayList(
                item.getItemId(),
                staticProp.getPropName(),
                num,
                AwardType.GOLD,
                player.getGold()
        ));
    }

    /**
     * @param handler
     */
    public void getVipGiftRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        GetVipGiftRs.Builder builder = GetVipGiftRs.newBuilder();

        List<Integer> gifts = player.getVipGifts();

        List<StaticVip> vipList = staticVipMgr.getVipList();
        for (StaticVip staticVip : vipList) {
            if (staticVip.getVip() > player.getLord().getVip() + 2) {
                break;
            }
            int vip = staticVip.getVip();
            if (gifts.indexOf(vip) == -1) {
                builder.addVip(vip);
            }
        }
        handler.sendMsgToPlayer(GetVipGiftRs.ext, builder.build());
    }

    /**
     * @param handler
     */
    public void buyVipGiftRq(BuyVipGiftRq req, ClientHandler handler) {
        // Player player = playerManager.getPlayer(293L);
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int vip = req.getVip();

        if (player.getVipGifts().contains(vip)) {
            handler.sendErrorMsgToPlayer(GameError.HAD_BUY_VIP_GIFT);
            return;
        }

        if (player.getLord().getVip() < vip) {
            handler.sendErrorMsgToPlayer(GameError.VIP_NOT_ENOUGH);
            return;
        }

        StaticVip staticVip = staticVipMgr.getStaticVip(vip);
        if (staticVip.getPrice() > player.getGold()) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        List<List<Integer>> giftList = staticVip.getGiftList();
        if (playerManager.isEquipFull(giftList, player)) {
            handler.sendErrorMsgToPlayer(GameError.EQUIP_FULL);
            return;
        }

        playerManager.subAward(player, AwardType.GOLD, 0, staticVip.getPrice(), Reason.BUY_VIP_GIFT);
        com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
        player.getVipGifts().add(vip);

        BuyVipGiftRs.Builder builder = BuyVipGiftRs.newBuilder();
        builder.setGold(player.getGold());

        List<List<Integer>> award = new ArrayList<>(staticVip.getGiftList());
        if (staticVip.getSpecialGift() != null) {
            award.addAll(staticVip.getSpecialGift());
        }
        for (List<Integer> gift : award) {
            int itemType = gift.get(0);
            int itemId = gift.get(1);
            int itemCount = gift.get(2);

            int keyId = 0;
            // 特殊效果物品,不进背包直接产生特效
            if (itemType == AwardType.PROP && itemId == ItemId.VIP_TECH) {
                player.getLord().setVipTech(1);
                builder.setVipTech(player.getLord().getVipTech());
            } else if (itemType == AwardType.PROP && itemId == ItemId.VIP_EQUIP) {
                player.getLord().setVipEquip(1);
                builder.setVipEquip(player.getLord().getVipEquip());
            } else if (itemType == AwardType.PROP && itemId == ItemId.VIP_WORK_SHOP) {
                player.getLord().setVipWorkShop(1);
                builder.setVipWorkShop(player.getLord().getVipWorkShop());
            } else if (itemType == AwardType.PROP && itemId == ItemId.BUY_ITEM1) {
                builder.setBuyItem(itemId);
            } else if (itemType == AwardType.PROP && itemId == ItemId.BUY_ITEM2) {
                builder.setBuyItem(itemId);
            } else if (itemType == AwardType.PROP && itemId == ItemId.BUY_ITEM3) {
                builder.setBuyItem(itemId);
            } else {// 普通物品
                keyId = playerManager.addAward(player, itemType, itemId, itemCount, Reason.BUY_VIP_GIFT);
                if (itemType == AwardType.HERO) {    //商店购买获得英雄
                    StaticHero staticHero = staticHeroMgr.getStaticHero(itemId);
                    String[] params = new String[]{String.valueOf(player.getCountry()), player.getNick(), String.valueOf(staticVip.getVip()), Quality.getName(staticHero.getQuality()), SoldierName.getName(staticHero.getSoldierType()), staticHero.getHeroName(), String.valueOf(staticHero.getHeroId())};
                    SystemChat systemChat = chatManager.createSysChat(ChatId.SYSTEM_SHARE_HEROM, params);
                    StaticChat staticChat = SpringUtil.getBean(StaticChatMgr.class).getChat(ChatId.SYSTEM_SHARE_HEROM);
                    chatService.sendChat(systemChat,staticChat);
                }
                if(itemType == AwardType.PROP && itemId == ItemId.QUICK_MONSTER){
                    taskManager.doTask(TaskType.QUICK_MONSTER,player,null);
                }

                /**
                 * 商店购买资源日志埋点
                 */
                if (itemType == AwardType.RESOURCE) {
                    com.game.log.LogUser logUser1 = SpringUtil.getBean(com.game.log.LogUser.class);
                    logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(itemId), RoleResourceLog.OPERATE_IN, itemId, ResOperateType.SHOP_BUY_IN.getInfoType(), itemCount, player.account.getChannel()));
                    int type = 0;
                    int resType = itemId;
                    switch (resType) {
                        case ResourceType.IRON:
                            type = IronOperateType.SHOP_BUY_IN.getInfoType();
                            break;
                        case ResourceType.COPPER:
                            type = CopperOperateType.SHOP_BUY_IN.getInfoType();
                            break;
                        case ResourceType.OIL:
                            type = OilOperateType.SHOP_BUY_IN.getInfoType();
                            break;
                        case ResourceType.STONE:
                            type = StoneOperateType.SHOP_BUY_IN.getInfoType();
                            break;
                        default:
                            break;
                    }
                    if (type != 0) {
                        logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, itemCount, type), resType);
                    }
                }
                builder.addAward(PbHelper.createAward(player, itemType, itemId, itemCount, keyId));
            }
        }

        handler.sendMsgToPlayer(BuyVipGiftRs.ext, builder.build());
        chatManager.updateChatShow(ChatShowType.VIP_GIFTS, vip, player);

        surpriseGiftManager.doSurpriseGift(player, SuripriseId.VipLevel, vip, true);

        eventManager.buyVipShop(player, Lists.newArrayList(staticVip.getVip()));
    }

    /**
     * 特价商店
     * \\\\\\\\\\0p
     *
     * @param handler
     */
    public void getVipShopRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        List<StaticVipBuy> vipBuyList = staticVipMgr.getVipBuy(player.getLord().getVip());
        if (vipBuyList == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int level = player.getLevel();
        int commandLv = player.getCommandLv();

        Map<Integer, Shop> shops = player.getShops();
        List<Integer> giftList = player.getVipGifts();

        GetVipShopRs.Builder builder = GetVipShopRs.newBuilder();

        int currentDay = GameServer.getInstance().currentDay;
        ResourceInfo resInfo = buildingManager.getTotalRes(player);
        for (StaticVipBuy staticVipBuy : vipBuyList) {
            int limitLv = staticVipBuy.getLevelDisplay();
            int limitGift = staticVipBuy.getGiftDisplay();
            int limtCommonLv = staticVipBuy.getCommandLv();
            int functionLimit = staticVipBuy.getFunctionLimit();
            // 等级限制
            if (limitLv != 0 && limitLv > level) {
                continue;
            }
            // vip礼包购买限制
            if (limitGift != 0 && giftList.indexOf(limitGift) == -1) {
                continue;
            }
            // 司令部限制
            if (limtCommonLv != 0 && limtCommonLv > commandLv) {
                continue;
            }

            if (functionLimit != 0) {

                // 宝石出售
                if (staticVipBuy.getPropId() == 16) {
                    boolean flag = buildingManager.isHadStoneBuild(player);
                    if (!flag) {
                        continue;
                    }
                }
                // 自动补防
                else if (staticVipBuy.getPropId() == 89) {
                    int wallLv = player.getWallLv();
                    if (wallLv == 0) {
                        continue;
                    }
                }
            }

            int propId = staticVipBuy.getPropId();
            Shop shop = shops.get(propId);
            CommonPb.Shop.Builder shopBuilder = CommonPb.Shop.newBuilder();
            shopBuilder.setPropId(propId);
            //当为特价礼包时,将玩家可购买的资源数量发送给前端
            if (staticVipBuy.getAddEffect() == 1){
                List<Integer> effect = staticVipBuy.getEffect();
                int awardType = effect.get(0);
                int awardId = effect.get(1);
                if (awardType == AwardType.RESOURCE){
                    long resource = 0;
                    if (awardId == ResourceType.IRON) {
                        resource = resInfo.getIron() < 4000 ? 4000 : resInfo.getIron();
                    } else if (awardId == ResourceType.COPPER) {
                        resource = resInfo.getCopper() < 3000 ? 3000 : resInfo.getCopper();
                    } else if (awardId == ResourceType.OIL) {
                        resource = resInfo.getOil() < 2000 ? 2000 : resInfo.getOil();
                    } else if (awardId == ResourceType.STONE) {
                        resource = resInfo.getStone() < 500 ? 500 : resInfo.getStone();
                    }
                    shopBuilder.setResourceCount(resource);
                }
            }


            if (shop == null || shop.getTime() != currentDay) {
                shopBuilder.setFree(staticVipBuy.getFree());
                shopBuilder.setBuyCount(staticVipBuy.getCount());
                shopBuilder.setPrice(staticVipBuy.getPrice());
                builder.addShop(shopBuilder.build());
            } else if (shop.getBuyCount() < staticVipBuy.getCount()) {
                shopBuilder.setPrice(staticVipBuy.getPrice());
                shopBuilder.setBuyCount(staticVipBuy.getCount() - shop.getBuyCount());
                shopBuilder.setFree(staticVipBuy.getFree() - shop.getFree());
                builder.addShop(shopBuilder.build());
            } else if (shop.getBuyCount() >= staticVipBuy.getCount()) {
                StaticProp staticProp = staticPropMgr.getStaticProp(staticVipBuy.getPropId());
                shopBuilder.setPrice(staticProp.getPrice());
                shopBuilder.setBuyCount(0);
                shopBuilder.setFree(staticVipBuy.getFree() - shop.getFree());
                builder.addShop(shopBuilder.build());
            }
        }
        handler.sendMsgToPlayer(GetVipShopRs.ext, builder.build());
    }

    /**
     * 特价商店购买
     *
     * @param req
     * @param handler
     */
    public void buyVipShopRq(BuyVipShopRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int buyId = req.getPropId();
        int number = req.getNum();
        int buyType = req.getType();

        StaticProp staticProp = staticPropMgr.getStaticProp(buyId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        int vip = player.getLord().getVip();
        StaticVip staticVip = staticVipMgr.getStaticVip(player.getVip());
        if (staticVip == null) {
            LogHelper.CONFIG_LOGGER.info("staticVip is null, vip = " + player.getVip());
            return;
        }

        StaticVipBuy vipBuy = staticVipMgr.getVipBuy(vip, buyId);

        if (vipBuy == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        BuyVipShopRs.Builder builder = BuyVipShopRs.newBuilder();

        Shop shop = playerManager.getVipShop(player, buyId);

        Item exchangeItem = null;
        if (vipBuy.getExchangePropId() != 0) {
            exchangeItem = player.getItem(vipBuy.getExchangePropId());
        }
        int needGold = 0;
        if (shop.getFree() < vipBuy.getFree()) {// 免费购买
            shop.setFree(shop.getFree() + 1);
            SpringUtil.getBean(EventManager.class).shopping(player, Lists.newArrayList(
                    shop.getPropId(),
                    "",
                    1,
                    AwardType.GOLD,
                    player.getGold()
            ));
//            number = number > 1 ? 1 : number;
        } else if (exchangeItem != null && exchangeItem.getItemNum() >= 1&&buyType!=2) {// 购买符购买
            playerManager.subAward(player, AwardType.PROP, vipBuy.getExchangePropId(), number, Reason.BUY_VIP_SHOP);
//            number = number > 1 ? 1 : number;
            builder.addProp(exchangeItem.wrapPb());
            SpringUtil.getBean(EventManager.class).shopping(player, Lists.newArrayList(
                    shop.getPropId(),
                    "",
                    1,
                    AwardType.PROP,
                    exchangeItem.getItemNum()
            ));
        } else {// 金币购买
            int vipCount = 0;
            int priceCount = 0;
            int shopBuy = shop.getBuyCount();

            if (vipBuy.getCount() == 0) {// vip不限次数
                vipCount = number;
            } else if (shopBuy >= vipBuy.getCount()) {// 原价购买
                priceCount = number;
            } else if (shopBuy < vipBuy.getCount() && shopBuy + number <= vipBuy.getCount()) {// vip购买
                vipCount = number;
            } else if (shopBuy < vipBuy.getCount() && shopBuy + number > vipBuy.getCount()) {// VIP+原价混合购买
                vipCount = vipBuy.getCount() - shopBuy;
                priceCount = number - vipCount;
            }

            if (vipCount <= 0 && priceCount <= 0) {
                handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                return;
            }

            int pirce = vipBuy.getPrice() * vipCount + staticProp.getPrice() * priceCount;
            if (player.getLord().getGold() < pirce || pirce <= 0) {
                handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                return;
            }
            needGold = pirce;
            playerManager.subAward(player, AwardType.GOLD, 0, pirce, Reason.BUY_VIP_SHOP);

            SpringUtil.getBean(EventManager.class).shopping(player, Lists.newArrayList(
                    shop.getPropId(),
                    staticProp.getPropName(),
                    1,
                    AwardType.GOLD,
                    player.getGold()
            ));

            /**
             * VIP礼包购买消耗钻石数量
             */
//    		com.game.log.LogHelper logHelper1 = SpringUtil.getBean(com.game.log.LogHelper.class);
//            logHelper.resourceLog(player, new RoleResourceChangeLog(player.roleId,
//                    player.getNick(),
//                    player.getLevel(),
//                    player.getTitle(),
//                    player.getHonor(),
//                    player.getCountry(),
//                    player.getVip(),
//                    player.account.getChannel(),
//                    RoleResourceLog.OPERATE_OUT, staticVip.getPrice(), GoldOperateType.BUY_VIP_OUT.getInfoType()));


            shop.setBuyCount(shop.getBuyCount() + number);
        }

        if (vipBuy.getAddEffect() == 0) {// 常规道具购买
            int useType = staticProp.getCanUse();
            if (useType == ItemUse.CAN_NOT_USE || useType == ItemUse.CAN_USE) {
                playerManager.addAward(player, AwardType.PROP, buyId, number, Reason.BUY_VIP_SHOP);
                builder.addAward(PbHelper.createAward(AwardType.PROP, buyId, number));
            } else if (useType == ItemUse.ADD_EFFECT) {
                List<List<Long>> effectList = staticProp.getEffectValue();
                for (List<Long> e : effectList) {
                    if (e.size() < 3) {
                        continue;
                    }
                    int type = e.get(0).intValue();
                    int id = e.get(1).intValue();
                    int itemCount = e.get(2).intValue() * number;
                    int keyId = playerManager.addAward(player, type, id, itemCount, Reason.BUY_VIP_SHOP);
                    CommonPb.Award.Builder awardPb = PbHelper.createAward(type, id, itemCount);
                    if (keyId > 0) {
                        awardPb.setKeyId(keyId);
                    }
                    builder.addAward(awardPb.build());
                }
            }
        } else if (vipBuy.getAddEffect() == 1) {// 购买之后添加数值到玩家身上
            List<Integer> effect = vipBuy.getEffect();
            int awardType = effect.get(0);
            int awardId = effect.get(1);
            if (awardType == AwardType.LORD_PROPERTY) {
                int value = effect.get(2);
                int expriod = effect.get(3);
                if (awardId == LordPropertyType.MARCH_SPEED) {// 添加行军速度
                    long endTime = playerManager.addEffect(player, LordPropertyType.MARCH_SPEED, value, expriod * number);
                    builder.setMarchEffect(endTime);
                    eventManager.quicken(player, Lists.newArrayList());
                    taskManager.doTask(TaskType.GOVER, player, null);
                } else if (awardId == LordPropertyType.RECRUIT_SOLDIERS) {// 添加招募速度
                    int speedCollect = staticVip.getSpeedCollect();
                    long endTime = playerManager.addEffect(player, LordPropertyType.RECRUIT_SOLDIERS, speedCollect, expriod * number);
                    builder.setSoilderEffect(endTime);
                    eventManager.soldierQuicken(player, Lists.newArrayList());
                }
            } else if (awardType == AwardType.RESOURCE) {// 直接添加资源
                ResourceInfo resInfo = buildingManager.getTotalRes(player);
                long resource = 0;
                if (awardId == ResourceType.IRON) {
                    resource = resInfo.getIron() < 4000 ? 4000 : resInfo.getIron();
                } else if (awardId == ResourceType.COPPER) {
                    resource = resInfo.getCopper() < 3000 ? 3000 : resInfo.getCopper();
                } else if (awardId == ResourceType.OIL) {
                    resource = resInfo.getOil() < 2000 ? 2000 : resInfo.getOil();
                } else if (awardId == ResourceType.STONE) {
                    resource = resInfo.getStone() < 500 ? 500 : resInfo.getStone();
                }
                if (resource > 0) {
                    resource *= number;
                    player.setPrice(needGold);
                    playerManager.addAward(player, awardType, awardId, (int) resource, Reason.BUY_VIP_SHOP);
                    builder.addAward(PbHelper.createAward(awardType, awardId, (int) resource));

                    /**
                     * 商店购买资源日志埋点
                     */
                    com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
                    logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(),
                            player.account.getCreateDate(),
                            player.getLevel(),
                            player.getNick(),
                            player.getVip(),
                            player.getCountry(),
                            player.getTitle(),
                            player.getHonor(),
                            player.getResource(awardId),
                            RoleResourceLog.OPERATE_IN, awardId, ResOperateType.SHOP_BUY_IN.getInfoType(), (int) resource, player.account.getChannel()));
                    int type = 0;
                    int resType = awardId;
                    switch (resType) {
                        case ResourceType.IRON:
                            type = IronOperateType.SHOP_BUY_IN.getInfoType();
                            break;
                        case ResourceType.COPPER:
                            type = CopperOperateType.SHOP_BUY_IN.getInfoType();
                            break;
                        case ResourceType.OIL:
                            type = OilOperateType.SHOP_BUY_IN.getInfoType();
                            break;
                        case ResourceType.STONE:
                            type = StoneOperateType.SHOP_BUY_IN.getInfoType();
                            break;
                        default:
                            break;
                    }
                    if (type != 0) {
                        logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
                                player.getNick(),
                                player.getLevel(),
                                player.getTitle(),
                                player.getHonor(),
                                player.getCountry(),
                                player.getVip(),
                                player.account.getChannel(),
                                0, (int) resource, type), resType);
                    }
                }
            } else if (awardType == AwardType.PERSON) {
                int commondLv = player.getCommand().getLv();
                int people = staticWorkShopMgr.getLimitPeople(commondLv) / 10;
                if (people > 0) {
                    people *= number;
                    playerManager.addAward(player, awardType, 0, people, Reason.BUY_VIP_SHOP);
                    builder.addAward(PbHelper.createAward(awardType, 0, people));
                }
            } else if (awardType == AwardType.AUTO_WAR_SOILDER) {
                Lord lord = player.getLord();
                int wall = lord.getAutoWallTimes() + vipBuy.getPropNum() * number;
                lord.setAutoWallTimes(wall);

                builder.setOnWall(lord.getOnWall());
                builder.setAutoWallTimes(lord.getAutoWallTimes());
            } else if (awardType == AwardType.AUTO_BUILD) {
                Lord lord = player.getLord();
                int build = lord.getAutoBuildTimes() + vipBuy.getPropNum();
                int autoTimes = staticVip.getAutoBuild();
                build = build > autoTimes ? autoTimes : build;
                player.setAutoBuildTimes(build);
                builder.setOnBuild(lord.getOnBuild());
                builder.setAutoBuildTimes(lord.getAutoBuildTimes());
            }
        }

        builder.setGold(player.getLord().getGold());
        playerManager.synEffects(player);
        handler.sendMsgToPlayer(BuyVipShopRs.ext, builder.build());
    }

    /**
     * 购买并使用
     */
    public void buyAndUseShopRq(ShopPb.BuyAndUseShopRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int propId = req.getPropId();
        int num = req.getNum();
        if (num <= 0) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        StaticProp staticProp = staticPropMgr.getStaticProp(propId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_EXIST_SHOP);
            return;
        }

        ShopInfo shopInfo = playerManager.getShopInfo(player);
        int price = staticProp.getPrice() * num;
        if (shopInfo.isDiscount(staticProp)) {// 折扣价格
            price = price * 9 / 10;
        }

        if (price <= 0) {// 保护,避免数据库填写错误
            handler.sendErrorMsgToPlayer(GameError.NO_EXIST_SHOP);
            return;
        }

        if (player.getLord().getGold() < price) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        if (isSoldierProp(staticProp.getPropType())) {
            handleSoldierItem(player, price, propId, num, handler);
        } else {
            handler.sendErrorMsgToPlayer(GameError.ITEM_TYPE_ERROR);
            return;
        }
    }

    public void handleSoldierItem(Player player, int price, int propId, int num, ClientHandler handler) {
        StaticProp staticProp = staticPropMgr.getStaticProp(propId);
        if (staticProp == null) {
            LogHelper.CONFIG_LOGGER.info("staticProp is null!");
            return;
        }
        int soldierType = getSoldierType(staticProp.getPropType());

        List<List<Long>> effectValue = staticProp.getEffectValue();
        if (effectValue == null || effectValue.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("effectValue == null || effectValue.size() != 1 is error!");
            return;
        }

        List<Long> value = effectValue.get(0);
        if (value == null || value.size() != 3) {
            LogHelper.CONFIG_LOGGER.info("value == null || value.size() != 1 is error!");
            return;
        }
        long addValue = value.get(2);

        // 增加士兵值
        playerManager.addAward(player, AwardType.SOLDIER, soldierType, addValue, Reason.BUY_SHOP);
        playerManager.subAward(player, AwardType.GOLD, 0, price, Reason.BUY_SHOP);


        ShopPb.BuyAndUseShopRs.Builder builder = ShopPb.BuyAndUseShopRs.newBuilder();
        builder.setGold(player.getGold());
        builder.addAward(PbHelper.createAward(AwardType.SOLDIER, soldierType, (int) addValue).build());
        handler.sendMsgToPlayer(ShopPb.BuyAndUseShopRs.ext, builder.build());
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

    public boolean isSoldierProp(int propType) {
        return propType == 43 || propType == 44 || propType == 45;
    }

    public static void main(String[] args) {
        Date now = new Date(1496403421470L);
        //System.out.println(DateHelper.formatDateTime(now, DateHelper.format1));
        //System.out.println();
    }

}
