package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.StaticEquipDataMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticEquip;
import com.game.domain.s.StaticProp;
import com.game.flame.FlameWarManager;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.RoleItemLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.Award.Builder;
import com.game.pb.PropPb;
import com.game.pb.PropPb.GetPropBagRq;
import com.game.pb.PropPb.GetPropBagRs;
import com.game.pb.PropPb.UsePropRq;
import com.game.pb.PropPb.UsePropRs;
import com.game.server.GameServer;
import com.game.util.ItemRandomUtil;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.PlayerCity;
import com.google.common.collect.Lists;

import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

@Service
public class ItemService {
    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticPropMgr staticPropDataMgr;

    @Autowired
    private HeroManager heroManager;

    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private StaticPropMgr staticPropMgr;

    @Autowired
    private EquipManager equipManager;

    @Autowired
    private ChatManager chatManager;

    @Autowired
    private LordManager lordManager;
    @Autowired
    private WorldManager worldManager;
    @Autowired
    private EventManager eventManager;

    // 获取所有物品道具
    public void getItemBagRq(GetPropBagRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        GetPropBagRs.Builder builder = GetPropBagRs.newBuilder();
       Map<Integer, Item> props = player.getItemMap();
        for (Map.Entry<Integer, Item> elem : props.entrySet()) {
            Item item = elem.getValue();
            if (item == null) {
                continue;
            }

            StaticProp staticProp = staticPropMgr.getStaticProp(item.getItemId());
            if (staticProp == null) {
                continue;
            }

            builder.addProp(item.wrapPb());
        }
       // LogHelper.ERROR_LOGGER.error("总共道具数量={}", builder.getPropCount());
        handler.sendMsgToPlayer(GetPropBagRs.ext, builder.build());
    }

    // 使用物品
    // 使用条件系统
    public void useItemRq(UsePropRq req, ClientHandler handler) {
        int cost = req.getCost();
        if (cost == 1) {
            handleUseItem(req, handler);
        } else if (cost == 2) {
            handleGoldUse(req, handler);
        } else {
            handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
            return;
        }
    }

    @Autowired
    FlameWarManager flameWarManager;

    // 直接使用
    public void handleUseItem(UsePropRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 检测物品是否存在背包中, 数量是否合法
        int itemId = req.getPropId();
        int itemNum = req.getPropNum();

        // 物品数量检查
        if (itemNum <= 0) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NUMBER_ERROR);
            return;
        }

        // 检测物品是否存在
        StaticProp staticProp = staticPropDataMgr.getStaticProp(itemId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }

        // 检测物品是否能够使用
        if (staticProp.getCanUse() != ItemUse.CAN_USE) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_CAN_NOT_USE);
            return;
        }

        // 检测背包是否存在
        Map<Integer, Item> items = player.getItemMap();
        Item item = items.get(itemId);
        if (item == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }

        // 检查背包物品是否充足
        if (item.getItemNum() < itemNum) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }

        // 根据effectValue来执行具体得到什么
        List<List<Long>> effectValue = staticProp.getEffectValue();
        if (effectValue == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_EFFECT_VALUE_IS_NULL);
            return;
        }

        int itemType = staticProp.getPropType();
        if (isRandBox(itemType)) {
            if (itemType == ItemType.RAND_BOX && player.getLevel() < staticProp.getLimitLevel()) {
                handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
                return;
            }

            if (itemType == ItemType.COMBAT_POWER_BOX && player.getBattleScore() < staticProp.getLimitBattleScore()) {
                handler.sendErrorMsgToPlayer(GameError.LACK_OF_COMBAT_POWER);
                return;
            }
        }
        // 使用资源道具
        GameError error = GameError.OK;
        Map<Integer, Award> randMap = new HashMap<>();
        List<Award> randList = new ArrayList<>();
        if (isResourceProp(itemType)) {
            error = doAddResource(player, effectValue, itemNum);
        } else if (isHeroExpProp(itemType)) {
            if (req.hasHeroId()) {
                error = addHeroExp(player, req, effectValue, itemNum);
            } else {
                error = GameError.NO_HERO_ID_FILED;
            }
        } else if (isHonorProp(itemType)) {
            error = addHonor(player, effectValue, itemNum);
        } else if (isEnergyType(itemType)) {
            error = addEnergy(player, effectValue, itemNum);
        } else if (isRandBox(itemType) || itemType == ItemType.RARE_ORANGE_BOX) {
            error = randomBox(staticProp, player, effectValue, itemNum, randList);
            checkRareOrangeBox(staticProp, randList);
        } else if (isNoWarProp(itemType)) {
            error = addNoWar(player, effectValue, itemNum);
        } else if (itemType == ItemType.VIP_ITEM) {
            error = addVipDot(player, effectValue, itemNum);
        } else if (isBuffType(itemType)) {
            error = addBuff(player, effectValue, itemNum);
            switch (itemType) {
                case ItemType.ADD_ATTACK:
                    eventManager.attackUp(player, Lists.newArrayList());
                    break;
                case ItemType.ADD_DEFENCE:
                    eventManager.defenceUp(player, Lists.newArrayList());
                    break;
                case ItemType.MARCH_SPEED:
                    eventManager.smallQuicken(player, Lists.newArrayList());
                    break;
                case ItemType.SOLDIER_REC:
                    eventManager.reBuild(player, Lists.newArrayList());
                    break;
                default:
                    break;
            }
        } else if (itemType == ItemType.MAKE_SPEED || itemType == ItemType.MAKE_SOILDER) {
            error = addEffect(player, effectValue, itemNum);
        } else if (itemType == ItemType.RANDOM_RESOURCE_PROP) {
            error = randomResoure(player, itemNum);
        } else if (itemType == ItemType.FIX_BOX) {
            error = fixBox(player, effectValue, itemNum, randList);
        } else if (itemType == ItemType.COMMERCIAL_TEAM) {
            error = addBuildTeamTime(player, effectValue, itemNum);
        } else if (itemType == ItemType.MEET_AN_EMERGENCY_BOX) {
            List<List<Long>> tempList = isGoodsLeast(effectValue, items);
            if (tempList == null || tempList.size() == 0) {
                handler.sendErrorMsgToPlayer(GameError.ADD_RANDOM_BOX_PARAM_ERROR);
                return;
            } else {
                error = randomBox(staticProp, player, tempList, itemNum, randList);
            }
        } else {
            handler.sendErrorMsgToPlayer(GameError.ITEM_CAN_NOT_USE);
            return;
        }

        if (error != GameError.OK) {
            handler.sendErrorMsgToPlayer(error);
            return;
        }

        // 扣除道具
        error = playerManager.subItem(player, itemId, itemNum, Reason.USE_ITEM);
        if (error != GameError.OK) {
            handler.sendErrorMsgToPlayer(error);
            return;
        }

        // 同步资源信息
        UsePropRs.Builder builder = UsePropRs.newBuilder();
        builder.setProp(item.wrapPb());
        builder.setResource(player.wrapResourcePb());
        // 武将信息
        if (req.hasHeroId()) {
            Hero hero = player.getHero(req.getHeroId());
            if (hero != null) {
                builder.setHeroInfo(hero.wrapHeroInfo());
                heroManager.synBattleScoreAndHeroList(player, hero);
            }
        }

        // 体力值
        builder.setEnergy(player.getEnergy());
        builder.setHonor(player.getHonor());

        // 随机箱子掉落
        for (Map.Entry<Integer, Award> lootItem : randMap.entrySet()) {
            if (lootItem == null)
                continue;
            Award itemElem = lootItem.getValue();
            if (itemElem != null) {
                int keyId = playerManager.addAward(player, itemElem, Reason.USE_ITEM);
                itemElem.setKeyId(keyId);
                builder.addRandomAward(itemElem.wrapPb());
            }
        }
        // 随机箱子掉落
        for (Award itemElem : randList) {
            if (itemElem == null) {
                continue;
            }
            int keyId = playerManager.addAward(player, itemElem, Reason.USE_ITEM);
            itemElem.setKeyId(keyId);
            Builder award = PbHelper.createAward(player, itemElem.getType(), itemElem.getId(), itemElem.getCount(), itemElem.getKeyId());
            builder.addRandomAward(award);
        }
        builder.setEnergyCD(playerManager.getEnergyCD(player));
        Lord lord = player.getLord();
        builder.setProtectedTime(lord.getProtectedTime());
        builder.setVipLv(player.getVip());
        builder.setVipExp(player.getVipExp());
        builder.setUseEnergyNum(lord.getUseEnergyNum());
        builder.addAllBuff(player.wrapBuffs());
        builder.addAllEffect(player.wrapEffects());
        builder.setBuildTeamTime(lord.getBuildTeamTime());
        handler.sendMsgToPlayer(UsePropRs.ext, builder.build());
    }

    public void checkRareOrangeBox(StaticProp staticProp, List<Award> items) {
        if (staticProp.getPropType() != ItemType.RARE_ORANGE_BOX) {
            return;
        }
        for (Award item : items) {
            StaticEquipDataMgr staticEquipDataMgr = SpringUtil.getBean(StaticEquipDataMgr.class);
            StaticEquip staticEquip = staticEquipDataMgr.getStaticEquip(item.getId());
            if (staticEquip == null) {
                continue;
            }
            List<StaticEquip> collect = staticEquipDataMgr.getEquipMap().values().stream().filter(e -> e.getQuality() == staticEquip.getQuality() && e.getEquipType() == staticEquip.getEquipType() && e.getSecretSkill() != 0).collect(Collectors.toList());
            if (collect.isEmpty()) {
                continue;
            }
            Collections.shuffle(collect);
            item.setId(collect.get(0).getEquipId());
        }

    }

    public boolean isBuffType(int itemType) {
        return itemType == ItemType.ADD_ATTACK || itemType == ItemType.ADD_DEFENCE || itemType == ItemType.MARCH_SPEED || itemType == ItemType.SOLDIER_REC || itemType == ItemType.GOLD_GAIN || itemType == ItemType.STEEL_GAIN;
    }

    // 购买并使用
    public void handleGoldUse(UsePropRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 检测物品是否存在背包中, 数量是否合法
        int itemId = req.getPropId();
        int itemNum = req.getPropNum();

        // 物品数量检查
        if (itemNum <= 0) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NUMBER_ERROR);
            return;
        }

        // 检测物品是否存在
        StaticProp staticProp = staticPropDataMgr.getStaticProp(itemId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }

        // 物品数量是否超过最大堆叠数
        if (itemNum > staticProp.getStackSize()) {
            handler.sendErrorMsgToPlayer(GameError.BEYOND_MAX_ITEM_STACKSIZE);
            return;
        }

        // 检测物品是否能够使用
        if (staticProp.getCanUse() != ItemUse.CAN_USE) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_CAN_NOT_USE);
            return;
        }

        // 检查物品价格
        int needGold = staticProp.getPrice() * itemNum;
        int owned = player.getGold();
        if (owned < needGold) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        // 根据effectValue来执行具体得到什么
        List<List<Long>> effectValue = staticProp.getEffectValue();
        if (effectValue == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_EFFECT_VALUE_IS_NULL);
            return;
        }

        int itemType = staticProp.getPropType();
        GameError error = GameError.OK;
        if (isHeroExpProp(itemType)) {

            if (req.hasHeroId()) {
                error = addHeroExp(player, req, effectValue, itemNum);
            } else {
                error = GameError.NO_HERO_ID_FILED;
            }
        } else if (isNoWarProp(itemType)) {
            error = addNoWar(player, effectValue, itemNum);
        }

        if (error != GameError.OK) {
            handler.sendErrorMsgToPlayer(error);
            return;
        }

        // 扣除金币
        playerManager.subAward(player, AwardType.GOLD, itemId, needGold, Reason.USE_ITEM);
        UsePropRs.Builder builder = UsePropRs.newBuilder();
        // 武将信息
        if (req.hasHeroId()) {
            Hero hero = player.getHero(req.getHeroId());
            if (hero != null) {
                builder.setHeroInfo(hero.wrapHeroInfo());
                heroManager.synBattleScoreAndHeroList(player, hero);
            }
        }
        builder.setEnergyCD(playerManager.getEnergyCD(player));
        builder.setGold(player.getGold());
        Lord lord = player.getLord();
        builder.setProtectedTime(lord.getProtectedTime());

        handler.sendMsgToPlayer(UsePropRs.ext, builder.build());

        /**
         * 记录下道具获得
         */
        com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
        logUser.roleItemLog(new RoleItemLog(player.roleId, itemId, itemNum, RoleItemLog.ITEM_ADD, Reason.BUY_SHOP));
        logUser.roleItemLog(new RoleItemLog(player.roleId, itemId, itemNum, RoleItemLog.ITEM_USE, Reason.USE_ITEM));

    }

    // 道具类型
    public boolean isResourceProp(int itemType) {
        return itemType == ItemType.IRON_PROP || itemType == ItemType.COPPER_PROP || itemType == ItemType.OIL_PROP || itemType == ItemType.STONE_PROP;
    }

    // 武将经验丹
    public boolean isHeroExpProp(int itemType) {
        return itemType == ItemType.HERO_EXP_PROP;
    }

    // 威望道具
    public boolean isHonorProp(int itemType) {
        return itemType == ItemType.HONOR_PROP;
    }

    // 免战
    public boolean isNoWarProp(int itemType) {
        return itemType == ItemType.WALL_PROTECTED;
    }

    // 体力单类型
    public boolean isEnergyType(int itemType) {
        return itemType == ItemType.ENERGY_PROP;
    }

    // 使用多个物品
    public GameError doAddResource(Player player, List<List<Long>> effectValue, int itemNum) {
        GameError gameError = GameError.OK;
        for (List<Long> item : effectValue) {
            if (item.size() <= 1) {
                continue;
            }
            gameError = playerManager.doAddResource(player, item, itemNum, Reason.USE_ITEM);
        }

        return gameError;
    }

    // 随机箱子
    public boolean isRandBox(int itemType) {
        return itemType == ItemType.RAND_BOX || itemType == ItemType.COMBAT_POWER_BOX;
    }

    // 武将使用多个经验丹
    public GameError addHeroExp(Player player, UsePropRq req, List<List<Long>> effectValue, int itemNum) {
        int heroId = req.getHeroId();
        GameError gameError = GameError.OK;
        for (List<Long> param : effectValue) {
            if (param.size() != 3) {
                gameError = GameError.ADD_HERO_EXP_PARAM_ERROR;
                break;
            }

            int awardType = param.get(0).intValue();
            if (awardType != AwardType.HERO_EXP) {
                gameError = GameError.ADD_HERO_EXP_AWARDTYPE_ERROR;
                break;
            }

            Long count = param.get(2) * itemNum;
            Hero hero = player.getHero(heroId);
            if (hero == null) {
                gameError = GameError.HERO_NULL;
                break;
            }

            heroManager.addExp(hero, player, count, Reason.USE_ITEM);
        }

        return gameError;
    }

    // 增加威望
    public GameError addHonor(Player player, List<List<Long>> effectValue, int itemNum) {
        GameError gameError = GameError.OK;
        for (List<Long> param : effectValue) {
            if (param.size() != 3) {
                gameError = GameError.ADD_HONOR_PARAM_ERROR;
                break;
            }

            int awardType = param.get(0).intValue();
            if (awardType != AwardType.LORD_PROPERTY) {
                gameError = GameError.ADD_HONOR_AWARDTYPE_ERROR;
                break;
            }

            int propertyType = param.get(1).intValue();
            if (propertyType != LordPropertyType.HONOR) {
                gameError = GameError.HONOR_PROPERTY_TYPE_ERROR;
                break;
            }

            Long count = param.get(2) * itemNum;
            playerManager.addHonor(player, count.intValue(), Reason.USE_ITEM);
        }

        return gameError;
    }

    // 增加玩家体力
    public GameError addEnergy(Player player, List<List<Long>> effectValue, int itemNum) {
        GameError gameError = GameError.OK;
        Lord lord = player.getLord();
        // 检查lord的体力是否需要刷新, 条件是凌晨刷新
        int day = GameServer.getInstance().currentDay;
        if (day != lord.getUseEnergyDay()) {
            lord.setUseEnergyNum(0);
            lord.setUseEnergyDay(day);
        }
        if (effectValue.get(0).size() <= 3) {
            if (itemNum + lord.getUseEnergyNum() > staticLimitMgr.getMaxUseEnergy()) {
                return GameError.USE_TO_MUCH_ENERGY;
            }
        }

        for (List<Long> param : effectValue) {
            if (param.size() < 3) {
                gameError = GameError.ADD_ENERGY_PARAM_ERROR;
                break;
            }

            int awardType = param.get(0).intValue();
            if (awardType != AwardType.LORD_PROPERTY) {
                gameError = GameError.ADD_ENERGY_AWARDTYPE_ERROR;
                break;
            }

            int propertyType = param.get(1).intValue();
            if (propertyType != LordPropertyType.ENERGY) {
                gameError = GameError.ENERGY_PROPERTY_TYPE_ERROR;
                break;
            }
            if (param.size() <= 3) {
                lord.setUseEnergyNum(lord.getUseEnergyNum() + itemNum);
            }
            Long count = param.get(2) * itemNum;
            lordManager.addEnergy(player.getLord(), count.intValue(), Reason.USE_ITEM);
        }

        return gameError;
    }

    /**
     * 增加商用建造队时间
     *
     * @param player
     * @param effectValue
     * @param itemNum
     * @return
     */
    private GameError addBuildTeamTime(Player player, List<List<Long>> effectValue, int itemNum) {
        GameError gameError = GameError.OK;
        Lord lord = player.getLord();

        long now = System.currentTimeMillis();
        if (lord.getBuildTeamTime() <= now) {
            lord.setBuildTeamTime(now);
        }
        for (List<Long> param : effectValue) {
            if (param.size() != 3) {
                gameError = GameError.ADD_ADDBUILDTEAMTIME_PARAM_ERROR;
                break;
            }

            int awardType = param.get(0).intValue();
            if (awardType != AwardType.COMMERCIAL_TEAM_TIME) {
                gameError = GameError.ADD_ADDBUILDTEAMTIME_PARAM_ERROR;
                break;
            }

            Long count = param.get(2) * itemNum;
            playerManager.addBuildTeamTime(player, count.intValue(), Reason.USE_ITEM);
        }

        return gameError;
    }

    // 随机箱子掉落
    public GameError randomBox(StaticProp staticProp, Player player, List<List<Long>> effectValue, int itemNum, List<Award> items) {
        GameError gameError = GameError.OK;
        List<Integer> list = new ArrayList<>();
        List<Award> eItems = new ArrayList<>();
        List<Award> randItems = new ArrayList<>();
        for (int i = 0; i < itemNum; i++) {
            List<Award> awards = new ArrayList<>();
            for (List<Long> param : effectValue) {
                if (param.size() != 4) {
                    gameError = GameError.ADD_RANDOM_BOX_PARAM_ERROR;
                    break;
                }
                int awardType = param.get(0).intValue();
                int itemId = param.get(1).intValue();
                int count = param.get(2).intValue();
                int rand = param.get(3).intValue();
                Award award = new Award(awardType, itemId, count);
                award.setMarkId(itemId);
                award.setProbability(rand);
                awards.add(award);
            }
            Award award = ItemRandomUtil.getBoxItem(awards, 1).get(0);

            if (staticProp.getPropId() == 178 && award.getType() == AwardType.PROP && award.getId() >= 70 && award.getId() <= 75) {
                String propName = staticPropMgr.getStaticProp(award.getId()) == null ? "" : staticPropMgr.getStaticProp(award.getId()).getPropName();
                if (!list.contains(award.getId())) {
                    chatManager.sendWorldChat(ChatId.HOURO_PAPER, String.valueOf(player.getCountry()), player.getNick(), propName);
                    list.add(award.getId());
                }
            }

            if (award.getType() == AwardType.EQUIP) {
                eItems.add(award);
            } else {
                randItems.add(award);
            }

//            Award result = mapItem.get(award.getId());
//            if (result == null) {
//                result = award;
//                mapItem.put(result.getId(), result);
//            } else {
//                result.setCount(result.getCount() + award.getCount());
//            }

        }
        if (eItems.size() > equipManager.getFreeSlot(player)) {
            gameError = GameError.NOT_ENOUGH_EQUIP_SLOT;
        }
        items.addAll(eItems);
        items.addAll(PbHelper.finilAward(PbHelper.createAwardList(randItems)));

        return gameError;
    }

    // 固定箱子掉落
    public GameError fixBox(Player player, List<List<Long>> effectValue, int itemNum, List<Award> items) {
        GameError gameError = GameError.OK;
        List<Award> eItems = new ArrayList<>();
        List<Award> randItems = new ArrayList<>();
        for (int i = 0; i < itemNum; i++) {
            for (List<Long> param : effectValue) {
                if (param.size() != 3) {
                    gameError = GameError.ADD_RANDOM_BOX_PARAM_ERROR;
                    break;
                }
                int awardType = param.get(0).intValue();
                int itemId = param.get(1).intValue();
                int count = param.get(2).intValue();
                Award award = new Award(awardType, itemId, count);
                if (awardType == AwardType.EQUIP) {
                    eItems.add(award);
                } else {
                    randItems.add(award);
                }
            }
        }
        if (eItems.size() > equipManager.getFreeSlot(player)) {
            gameError = GameError.NOT_ENOUGH_EQUIP_SLOT;
        }
        items.addAll(eItems);
        items.addAll(PbHelper.finilAward(PbHelper.createAwardList(randItems)));

        return gameError;
    }

    // 随机资源箱规则：金币钢铁食物三种资源随机一种；金币数量=2000*当前指挥官等级；钢铁数量=2000*当前指挥官等级；食物数量=1000*当前指挥官等级
    public GameError randomResoure(Player player, int itemNum) {
        for (int i = 0; i < itemNum; i++) {
            int resId = RandomUtils.nextInt(1, 4);
            int num = 2000;
            if (resId == ResourceType.OIL) {
                num = 1000;
            }
            int count = num * player.getLevel();
            playerManager.addAward(player, AwardType.RESOURCE, resId, count, Reason.OPEN_RESOURCE_BOX);
        }

        return GameError.OK;

    }

    /**
     * Function: 道具出售
     */
    public void selItem(PropPb.SellPropRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 检测物品是否存在背包中, 数量是否合法
        int itemId = req.getPropId();
        int itemNum = req.getPropNum();

        // 物品数量检查
        if (itemNum <= 0) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NUMBER_ERROR);
            return;
        }

        // 检测物品是否存在
        StaticProp staticProp = staticPropDataMgr.getStaticProp(itemId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }

        // 物品数量是否超过最大堆叠数
        if (itemNum > staticProp.getStackSize()) {
            handler.sendErrorMsgToPlayer(GameError.BEYOND_MAX_ITEM_STACKSIZE);
            return;
        }

        // 检测物品是否能够出售
        if (staticProp.getCanSell() == ItemSell.CAN_NOT_SELL) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_CAN_NOT_SELL);
            return;
        }

        // 检测背包是否存在
        Map<Integer, Item> items = player.getItemMap();
        Item item = items.get(itemId);
        if (item == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }

        // 检查背包物品是否充足
        if (item.getItemNum() < itemNum) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }

        // 增加生铁
        long price = itemNum * staticProp.getSellIron();
        GameError gameError = playerManager.addResource(player, ResourceType.IRON, price, Reason.SELL_ITEM);
        if (gameError != GameError.OK) {
            handler.sendErrorMsgToPlayer(gameError);
            return;
        }

        // 扣除道具
        playerManager.subItem(player, itemId, itemNum, Reason.SELL_ITEM);

        PropPb.SellPropRs.Builder builder = PropPb.SellPropRs.newBuilder();
        builder.setPropId(itemId);
        builder.setPropNum(item.getItemNum());
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(PropPb.SellPropRs.ext, builder.build());

    }

    /**
     * Function: 修改玩家名字
     */
    public void changeLordName(PropPb.ChangeLordNameRq req, ClientHandler handler) {
        int cost = req.getCost();
        if (cost == 1) {
            handleItemChange(req, handler);
        } else if (cost == 2) {
            handleGoldUse(req, handler);
        } else {
            handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
            return;
        }

    }

    public void handleItemChange(PropPb.ChangeLordNameRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 检测物品是否存在背包中, 数量是否合法
        int itemId = req.getPropId();
        // 检测物品是否存在
        StaticProp staticProp = staticPropDataMgr.getStaticProp(itemId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }

        // 检测物品是否能够使用
        if (staticProp.getCanUse() != ItemUse.CAN_USE) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_CAN_NOT_USE);
            return;
        }

        // 检测背包是否存在
        Map<Integer, Item> items = player.getItemMap();
        Item item = items.get(itemId);
        if (item == null) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
            return;
        }

        // 检查背包物品是否充足
        if (item.getItemNum() < 1) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
            return;
        }

        // 昵称不合法
        String nick = req.getName();
        String newName = nick;
        String oldName = player.getNick();
        if (!playerManager.isNickOk(nick)) {
            handler.sendErrorMsgToPlayer(GameError.NICK_NAME_ERROR);
            return;
        }

        // 检查名字是否有相同的
        // 昵称是否被占用
        if (playerManager.takeNick(req.getName())) {
            handler.sendErrorMsgToPlayer(GameError.SAME_NICK);
            return;
        }

        // 扣除道具
        GameError error = playerManager.subItem(player, itemId, 1, Reason.USE_ITEM);
        if (error != GameError.OK) {
            handler.sendErrorMsgToPlayer(error);
            return;
        }

        SpringUtil.getBean(NickManager.class).setPlayerNick(player.getLord(), newName);

        PropPb.ChangeLordNameRs.Builder builder = PropPb.ChangeLordNameRs.newBuilder();
        builder.setName(req.getName());
        builder.setPropId(itemId);
        builder.setPropNum(item.getItemNum());
        handler.sendMsgToPlayer(PropPb.ChangeLordNameRs.ext, builder.build());
    }

    public void handleGoldUse(PropPb.ChangeLordNameRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        // 检测物品是否存在背包中, 数量是否合法
        int itemId = req.getPropId();

        // 检测物品是否存在
        StaticProp staticProp = staticPropDataMgr.getStaticProp(itemId);
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
        int needGold = staticProp.getPrice();
        int owned = player.getGold();
        if (owned < needGold) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        // 昵称不合法
        String nick = req.getName();
        String newName = nick;
        String oldName = player.getNick();
        if (!playerManager.isNickOk(nick)) {
            handler.sendErrorMsgToPlayer(GameError.NICK_NAME_ERROR);
            return;
        }

        // 检查名字是否有相同的, 昵称是否被占用
        if (playerManager.takeNick(nick)) {
            handler.sendErrorMsgToPlayer(GameError.SAME_NICK);
            return;
        }

        playerManager.removeNickName(player.getNick());

        playerManager.subAward(player, AwardType.GOLD, itemId, needGold, Reason.USE_ITEM);
        Lord lord = player.getLord();
        SpringUtil.getBean(NickManager.class).setPlayerNick(lord, nick);
        PropPb.ChangeLordNameRs.Builder builder = PropPb.ChangeLordNameRs.newBuilder();
        builder.setName(nick);
        builder.setGold(player.getGold());
        handler.sendMsgToPlayer(PropPb.ChangeLordNameRs.ext, builder.build());

        SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.modify_nick);
    }

    // 免战: 处理战斗取消状态
    public GameError addNoWar(Player player, List<List<Long>> effectValue, int itemNum) {
        GameError gameError = GameError.OK;
        for (List<Long> param : effectValue) {
            if (param.size() != 3) {
                gameError = GameError.ADD_WALL_PROTECTED_PARAM_ERROR;
                break;
            }

            int awardType = param.get(0).intValue();
            if (awardType != AwardType.PROP) {
                gameError = GameError.ADD_WALL_PROTECTED_PARAM_ERROR;
                break;
            }

            Long count = param.get(2) * itemNum;

            Lord lord = player.getLord();
            long now = System.currentTimeMillis();
            if (lord.getProtectedTime() <= now) {
                lord.setProtectedTime(now);
            }
            lord.setProtectedTime(lord.getProtectedTime() + count * TimeHelper.SECOND_MS);
        }

        int mapId = worldManager.getMapId(player);
        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        PlayerCity playerCity = mapInfo.getPlayerCity(player.getPos());
//        WorldEntity worldEntity = new WorldEntity(playerCity);
//        worldEntity.setProtectedTime(player.getProectedTime());
//        if (player != null) {
//            worldEntity.setSkin(player.getLord().getSkin());
//        }
        CommonPb.Pos posPB = player.getPos().wrapPb();
        CommonPb.WorldEntity worldEntityPb = playerCity.wrapPb().build();
        playerManager.getOnlinePlayer().forEach(e -> {
            if (e.getLord().getLordId() != player.getLord().getLordId()) {
                playerManager.synEntityToPlayer(e, worldEntityPb, posPB);
            }
        });
        eventManager.cityProtect(player, Lists.newArrayList());
        achievementService.addAndUpdate(player,AchiType.AT_58,1);
        return gameError;
    }

    @Autowired
    AchievementService achievementService;

    public GameError addVipDot(Player player, List<List<Long>> effectValue, int itemNum) {
        GameError gameError = GameError.OK;
        for (List<Long> param : effectValue) {
            if (param.size() != 3) {
                gameError = GameError.VIP_EXP_ITEM_PARAM_ERROR;
                break;
            }

            int awardType = param.get(0).intValue();
            if (awardType != AwardType.LORD_PROPERTY) {
                gameError = GameError.VIP_EXP_ITEM_PARAM_ERROR;
                break;
            }

            int id = param.get(1).intValue();
            // 数量
            Long count = param.get(2) * itemNum;
            playerManager.addAward(player, awardType, id, count, Reason.USE_ITEM);

        }

        return gameError;
    }

    // [19, 1, 15, 900] 伤害减免 15% [awardType, id, value, time]
    public GameError addBuff(Player player, List<List<Long>> effectValue, int itemNum) {
        GameError gameError = GameError.OK;
        for (List<Long> param : effectValue) {
            if (param.size() != 4) {
                gameError = GameError.PARAM_ERROR;
                break;
            }

            int awardType = param.get(0).intValue();
            if (!isBuffAward(awardType)) {
                LogHelper.CONFIG_LOGGER.info("awardType is error, awardType is = " + awardType);
                gameError = GameError.CONFIG_ERROR;
                break;
            }

            int id = param.get(1).intValue();
            int value = param.get(2).intValue();
            long period = param.get(3).intValue() * TimeHelper.SECOND_MS * itemNum;
            // addBuff(Player player,int type, int id, long period, int value,
            // int count, int reason)
            playerManager.addBuff(player, awardType, id, period, value, itemNum, Reason.USE_ITEM);
        }

        return gameError;
    }

    public GameError addEffect(Player player, List<List<Long>> effectValue, int itemNum) {
        GameError gameError = GameError.OK;
        for (List<Long> param : effectValue) {
            if (param.size() != 4) {
                gameError = GameError.PARAM_ERROR;
                break;
            }

            int id = param.get(1).intValue();
            int value = param.get(2).intValue();
            long period = param.get(3).intValue() * itemNum;
            playerManager.addEffect(player, id, value, period);
        }
        return gameError;
    }

    public boolean isBuffAward(int awardType) {
        return awardType == AwardType.ADD_BUFF;
    }

    // 返回目前玩家最缺少的材料 返回为null为配置错误
    public List<List<Long>> isGoodsLeast(List<List<Long>> effectValue, Map<Integer, Item> items) {
        Map<Integer, Integer> map = new HashMap();
        ArrayList<Integer> tempList = new ArrayList<>();
        for (List<Long> param : effectValue) {
            if (param.size() == 4) {
                int itemId = param.get(1).intValue();
                Item item = items.get(itemId);
                if (item == null) {
                    if (!map.containsKey(itemId)) {
                        map.put(itemId, 0);
                        tempList.add(0);
                    }
                } else {
                    if (!map.containsKey(itemId)) {
                        map.put(itemId, item.getItemNum());
                        tempList.add(item.getItemNum());
                    }
                }
            } else {
                return null;
            }
        }
        Collections.sort(tempList);
        Set<Entry<Integer, Integer>> entries = map.entrySet();
        List<List<Long>> list = new ArrayList<>();
        for (Entry<Integer, Integer> entry : entries) {
            if (entry != null && entry.getValue().intValue() == tempList.get(0).intValue()) {
                for (List<Long> param : effectValue) {
                    if (entry.getKey().intValue() == param.get(1).intValue()) {
                        list.add(param);
                    }
                }
            }
        }
        return list;
    }
}
