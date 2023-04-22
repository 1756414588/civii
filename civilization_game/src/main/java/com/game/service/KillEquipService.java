package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.Player;
import com.game.domain.p.Item;
import com.game.domain.p.KillEquip;
import com.game.domain.s.StaticKillEquip;
import com.game.domain.s.StaticKillEquipLevel;
import com.game.domain.s.StaticProp;
import com.game.log.constant.ResOperateType;
import com.game.log.constant.StoneOperateType;
import com.game.log.consumer.EventManager;
import com.game.log.domain.KillEquipLog;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.KillEquipPb;

import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class KillEquipService {
    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private KillEquipManager killEquipMgr;

    @Autowired
    private ItemManager itemManager;

    @Autowired
    private BuildingManager buildingManager;

    @Autowired
    private TechManager techManager;

    @Autowired
    private HeroManager heroManager;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private StaticActivityMgr staticActivityMgr;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private ActivityManager activityManager;

    private static final int MAX_PROCSS = 100;


    public void compund(KillEquipPb.CompundRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int equipId = req.getEquipId();
        if (!killEquipMgr.isEuqipIdOk(equipId)) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIPID_NOT_FOUND);
            return;
        }

        // 检查碎片和资源是否充足
        StaticKillEquip staticKillEquip = killEquipMgr.getStaticKillEquip(equipId);
        if (staticKillEquip == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        // 检查合成的碎片配置是否存在
        StaticProp staticProp = killEquipMgr.getChipConfig(equipId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIP_CHIP_NOT_EXISTS);
            return;
        }


        // 消耗
        List<List<Integer>> compound = staticKillEquip.getCompound();
        if (compound == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }

        // 检查玩家等级
        int playerLevel = player.getLevel();
        if (playerLevel < staticKillEquip.getOpenLevel()) {
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }

        // 检查是否已经有一件杀器
        if (killEquipMgr.hasEquip(player, equipId)) {
            handler.sendErrorMsgToPlayer(GameError.HAS_KILL_EQUIP);
            return;
        }

        // 至少需要一个碎片
        Item chipItem = player.getItem(staticProp.getPropId());
        if (chipItem == null || chipItem.getItemNum() < 1) {
            handler.sendErrorMsgToPlayer(GameError.GET_ONE_KILL_EQUIP_CHIP);
            return;
        }


        // 检查消耗
        for (List<Integer> need : compound) {
            int type = need.get(0);
            int id = need.get(1);
            int count = need.get(2);

            if (type == AwardType.PROP) {
                if (!itemManager.hasEnoughItem(player, id, count)) {
                    handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
                    return;
                }
            } else if (type == AwardType.RESOURCE) {
                if (!buildingManager.hasEnoughResounce(player, id, count)) {
                    handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
                    return;
                }
            }
        }

        // 创建杀器
        KillEquip killEquip = killEquipMgr.addEquip(player, equipId);
        if (killEquip == null) {
            LogHelper.CONFIG_LOGGER.info("killEquip is null!");
            handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
            return;
        }
        activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.ADD_ARTIFACT + equipId * 100 + killEquip.getLevel(), 0, 1);

        KillEquipPb.CompundRs.Builder builder = KillEquipPb.CompundRs.newBuilder();

        // 扣除道具
        for (List<Integer> need : compound) {
            if (need.size() != 3) {
                continue;
            }
            int type = need.get(0);
            int id = need.get(1);
            int count = need.get(2);
            if (type == AwardType.PROP) {
                playerManager.subAward(player, type, id, count, Reason.COMPOUND_KILL_EQUIP);
                Item item = player.getItem(id);
                if (item != null) {
                    builder.addProp(item.wrapPb());
                }
            } else if (type == AwardType.RESOURCE) {
                playerManager.subAward(player, type, id, count, Reason.COMPOUND_KILL_EQUIP);
            }
        }
        builder.setKillEquip(killEquip.wrapPb());
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(KillEquipPb.CompundRs.ext, builder.build());
        heroManager.synBattleScoreAndHeroList(player,player.getAllHeroList());
        doMakeKillEquip(player, equipId);
        achievementService.addAndUpdate(player,AchiType.AT_36,1);
    }

    @Autowired
    AchievementService achievementService;

    // 国器打造
    public void doMakeKillEquip(Player player, int killEquipId) {
        List<Integer> triggers = new ArrayList<Integer>();
        triggers.add(killEquipId);
        taskManager.doTask(TaskType.MAKE_KILL_EQUIP, player, triggers);
    }

    // 国器升级
    public void doCheckKillLevel(Player player, int killEquipId, int level) {
        List<Integer> triggers = new ArrayList<Integer>();
        triggers.add(killEquipId);
        triggers.add(level);
        taskManager.doTask(TaskType.LEVELUP_KILL_EQUIP, player, triggers);
    }

    // 购买杀器碎片
    public void buyKillEquip(KillEquipPb.BuyKillRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int equipId = req.getEquipId();
        if (!killEquipMgr.isEuqipIdOk(equipId)) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIPID_NOT_FOUND);
            return;
        }

        // 检查是否已经有一件杀器
        if (killEquipMgr.hasEquip(player, equipId)) {
            handler.sendErrorMsgToPlayer(GameError.HAS_KILL_EQUIP);
            return;
        }

        // 检查合成的碎片配置是否存在
        StaticProp staticProp = killEquipMgr.getChipConfig(equipId);
        if (staticProp == null) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIP_CHIP_NOT_EXISTS);
            return;
        }

        if (player.getItemNum(staticProp.getPropId()) <= 0) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIP_CHIP_NOT_EXISTS);
            return;
        }

        // 碎片已满
        if (killEquipMgr.reachMaxChip(player, staticProp.getPropId())) {
            handler.sendErrorMsgToPlayer(GameError.REACH_MAX_EQUIP_CHIPS);
            return;
        }

        // 检查金币
        int price = staticProp.getPrice();
        int own = player.getGold();
        if (own < price) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        playerManager.addAward(player, AwardType.PROP, staticProp.getPropId(), 1, Reason.BUY_KILL_EQUIP);
        playerManager.subAward(player, AwardType.GOLD, 1, price, Reason.BUY_KILL_EQUIP);
        eventManager.getKillEquip(player, Lists.newArrayList("购买", false, price));

        KillEquipPb.BuyKillRs.Builder builder = KillEquipPb.BuyKillRs.newBuilder();
        Item item = player.getItem(staticProp.getPropId());
        if (item != null) {
            builder.setProp(item.wrapPb());
        }

        builder.setGold(player.getGold());
        handler.sendMsgToPlayer(KillEquipPb.BuyKillRs.ext, builder.build());

    }

    // 升级国器
    public void upKillEquip(KillEquipPb.UpKillRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int equipId = req.getEquipId();
        if (!killEquipMgr.isEuqipIdOk(equipId)) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIPID_NOT_FOUND);
            return;
        }

        // 检查是否已经有一件杀器
        if (!killEquipMgr.hasEquip(player, equipId)) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIPID_NOT_FOUND);
            return;
        }

        KillEquip killEquip = killEquipMgr.getKillEquip(player, equipId);
        // 国器是否满级
        if (killEquip.getLevel() >= player.getLevel()) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIP_REACH_MAX__LEVEL);
            return;
        }

        // 检查当前的暴击等级
        int criti = techManager.getCriti(player);
        int nextCriti = staticActivityMgr.getActivityById(ActivityConst.ACT_KILL_EXP) == null ? killEquipMgr.nextCriti(criti, 1) : killEquipMgr.nextCriti(criti, 2);

        Map<Integer, StaticKillEquipLevel> levelKey = killEquipMgr.getLevelKey(equipId);
        if (levelKey == null) {
            LogHelper.CONFIG_LOGGER.info("levelKey is null!");
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        int equipLevel = killEquip.getLevel();
        StaticKillEquipLevel staticKillEquipLevel = levelKey.get(equipLevel);
        if (staticKillEquipLevel.getGold() != null && staticKillEquipLevel.getGold().size() > 0 && killEquip.getIsOpen() == 0) {
            LogHelper.CONFIG_LOGGER.info("staticKillEquipLevel is not open!");
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        staticKillEquipLevel = levelKey.get(equipLevel + 1);
        if (staticKillEquipLevel == null) {
            LogHelper.CONFIG_LOGGER.info("staticKillEquipLevel is null!");
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        int stoneCost = staticKillEquipLevel.getStone();
        if (stoneCost == Integer.MAX_VALUE) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        if (!buildingManager.hasEnoughResounce(player, ResourceType.STONE, stoneCost)) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        playerManager.subAward(player, AwardType.RESOURCE, ResourceType.STONE, stoneCost, Reason.UP_KILL_EQUIP);

        /**
         * 神器升级日志埋点
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
                player.getResource(ResourceType.STONE),
                RoleResourceLog.OPERATE_OUT, ResourceType.STONE, ResOperateType.UP_KILL_EQUIP_OUT.getInfoType(), stoneCost, player.account.getChannel()));
        logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
                player.getNick(),
                player.getLevel(),
                player.getTitle(),
                player.getHonor(),
                player.getCountry(),
                player.getVip(),
                player.account.getChannel(),
                1, stoneCost, StoneOperateType.UP_KILL_EQUIP_OUT.getInfoType()), ResourceType.STONE);


        int currentCriti = killEquip.getCriti();
        int processAdd = currentCriti * staticKillEquipLevel.getProcess();
        int processRes = killEquip.getProcess() + processAdd;
        int levelAdd = processRes / MAX_PROCSS;
        int leftProcess = processRes % MAX_PROCSS;
        int beforeLv = killEquip.getLevel();
        int levelRes = killEquip.getLevel() + levelAdd;
        levelRes = Math.max(1, levelRes);
        levelRes = Math.min(player.getLevel(), levelRes);

        leftProcess = Math.max(0, leftProcess);
        leftProcess = Math.min(MAX_PROCSS, leftProcess);

        killEquip.setLevel(levelRes);
        killEquip.setProcess(leftProcess);
        killEquip.setCriti(nextCriti);

        KillEquipPb.UpKillRs.Builder builder = KillEquipPb.UpKillRs.newBuilder();
        builder.setKillEquip(killEquip.wrapPb());
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(KillEquipPb.UpKillRs.ext, builder.build());

        int afterLv = killEquip.getLevel();
        if (afterLv > beforeLv) {
            heroManager.synBattleScoreAndHeroList(player,player.getAllHeroList());
            doCheckKillLevel(player, equipId, afterLv);
            killEquip.setIsOpen(0);
            activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.ADD_ARTIFACT + equipId * 100 + afterLv, 0, 1);
        }
        /**
         * 神器升级日志埋点
         */

        logUser.killEquipLog(new KillEquipLog(player.roleId,
                player.getNick(),
                player.getLevel(),
                player.getTitle(),
                player.getCountry(),
                player.getVip(),
                player.getStone(),
                player.account.getCreateDate(),
                player.account.getChannel(),
                killEquip.getEquipId(),
                levelRes,
                stoneCost));


        List param = Lists.newArrayList(
                killEquip.getEquipId(),
                killEquip.getEquipId(),
                staticKillEquipLevel.getLevel(),
                0
        );
        SpringUtil.getBean(EventManager.class).magic_weapon_level_up(player, param);
    }

    /**
     * gm 升级神器
     *
     * @param equipId
     * @param level
     * @param player
     */
    public void gmUpSqLevel(int equipId, int level, Player player) {
        if (!killEquipMgr.isEuqipIdOk(equipId)) {
            return;
        }

        // 检查是否已经有一件杀器
        if (!killEquipMgr.hasEquip(player, equipId)) {
            return;
        }

        KillEquip killEquip = killEquipMgr.getKillEquip(player, equipId);
        // 国器是否满级
        if (killEquip.getLevel() + level > player.getLevel()) {
            return;
        }
        int equipLevel = killEquip.getLevel();
        Map<Integer, StaticKillEquipLevel> levelKey = killEquipMgr.getLevelKey(equipId);
        if (levelKey == null) {
            LogHelper.CONFIG_LOGGER.info("levelKey is null!");
            return;
        }
        // 检查当前的暴击等级
        int criti = techManager.getCriti(player);
        // 下一次出暴击的概率
        int nextCriti = staticActivityMgr.getActivityById(ActivityConst.ACT_KILL_EXP) == null ? killEquipMgr.nextCriti(criti, 1) : killEquipMgr.nextCriti(criti, 2);

        StaticKillEquipLevel staticKillEquipLevel = levelKey.get(equipLevel + level);
        if (staticKillEquipLevel == null) {
            return;
        }
        int currentCriti = killEquip.getCriti();
        int processAdd = currentCriti * staticKillEquipLevel.getProcess();
        int processRes = killEquip.getProcess() + processAdd;
        int levelAdd = processRes / MAX_PROCSS;
        int leftProcess = processRes % MAX_PROCSS;
        int beforeLv = killEquip.getLevel();
        int levelRes = killEquip.getLevel() + levelAdd;
        levelRes = Math.max(1, levelRes);
        levelRes = Math.min(player.getLevel(), levelRes);

        leftProcess = Math.max(0, leftProcess);
        leftProcess = Math.min(MAX_PROCSS, leftProcess);

        killEquip.setLevel(equipLevel + level);
        killEquip.setProcess(leftProcess);
        killEquip.setCriti(nextCriti);

        KillEquipPb.UpKillRs.Builder builder = KillEquipPb.UpKillRs.newBuilder();
        builder.setKillEquip(killEquip.wrapPb());
        builder.setResource(player.wrapResourcePb());
        SynHelper.synMsgToPlayer(player, KillEquipPb.UpKillRs.EXT_FIELD_NUMBER, KillEquipPb.UpKillRs.ext,
                builder.build());
        int afterLv = killEquip.getLevel();
        if (afterLv > beforeLv) {
            heroManager.synBattleScoreAndHeroList(player,player.getAllHeroList());
            doCheckKillLevel(player, equipId, afterLv);
            killEquip.setIsOpen(0);
        }
    }

    public void getSkilEquip(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        Map<Integer, KillEquip> killEquipMap = player.getKillEquipMap();
        KillEquipPb.GetKillEquipRs.Builder builder = KillEquipPb.GetKillEquipRs.newBuilder();
        for (KillEquip killEquip : killEquipMap.values()) {
            if (killEquip == null) {
                continue;
            }
            if (killEquip.getLevel() < 1) {
                continue;
            }
            builder.addKillEquip(killEquip.wrapPb());
        }
        handler.sendMsgToPlayer(KillEquipPb.GetKillEquipRs.ext, builder.build());
    }

    public void openKillEquipRq(ClientHandler handler, KillEquipPb.OpenKillEquipRq req) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int equipId = req.getEquipId();
        if (!killEquipMgr.isEuqipIdOk(equipId)) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIPID_NOT_FOUND);
            return;
        }

        // 检查是否已经有一件杀器
        if (!killEquipMgr.hasEquip(player, equipId)) {
            handler.sendErrorMsgToPlayer(GameError.KILL_EQUIPID_NOT_FOUND);
            return;
        }

        KillEquip killEquip = killEquipMgr.getKillEquip(player, equipId);

        int equipLevel = killEquip.getLevel();
        Map<Integer, StaticKillEquipLevel> levelKey = killEquipMgr.getLevelKey(equipId);
        if (levelKey == null) {
            LogHelper.CONFIG_LOGGER.info("levelKey is null!");
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        StaticKillEquipLevel staticKillEquipLevel = levelKey.get(equipLevel);
        if (staticKillEquipLevel == null) {
            LogHelper.CONFIG_LOGGER.info("staticKillEquipLevel is null!");
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        List<List<Integer>> gold = staticKillEquipLevel.getGold();
        if (null == gold || gold.size() == 0) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }

        for (List<Integer> res : gold) {
            if (res.size() != 3) {
                handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
                return;
            }

            if (!buildingManager.hasEnoughResounce(player, res.get(1), res.get(2))) {
                handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
                return;
            }
        }

        for (List<Integer> res : gold) {
            playerManager.subAward(player, AwardType.RESOURCE, res.get(1), res.get(2), Reason.UP_KILL_EQUIP);
        }

        killEquip.setLevel(killEquip.getLevel());
        killEquip.setProcess(killEquip.getProcess());
        killEquip.setCriti(killEquip.getCriti());
        killEquip.setIsOpen(1);

        KillEquipPb.OpenKillEquipRs.Builder builder = KillEquipPb.OpenKillEquipRs.newBuilder();
        builder.setKillEquip(killEquip.wrapPb());
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(KillEquipPb.OpenKillEquipRs.ext, builder.build());
    }
}

