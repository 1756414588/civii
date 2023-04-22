package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldBoxDataMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.Item;
import com.game.domain.p.PWorldBox;
import com.game.domain.p.WorldBox;
import com.game.domain.s.StaticWorldBox;
import com.game.log.LogUser;
import com.game.log.consumer.EventManager;
import com.game.log.domain.WorldBoxLog;
import com.game.manager.ChatManager;
import com.game.manager.PlayerManager;
import com.game.manager.WorldBoxManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.WorldBoxPb;
import com.game.server.GameServer;
import com.game.util.PbHelper;
import com.game.util.RandomHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cpz
 * @date 2021/1/5 17:47
 * @description
 */
@Service
public class WorldBoxService {
    @Autowired
    private StaticWorldBoxDataMgr worldBoxDataMgr;
    @Autowired
    private WorldBoxManager worldBoxManager;
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private StaticLimitMgr limitMgr;
    @Autowired
    private ChatManager chatManager;
    @Autowired
    private EventManager eventManager;
    @Autowired
    StaticWorldBoxDataMgr staticWorldBoxDataMgr;

    public void checkBoxOpen() {
        GameServer.getInstance().currentDay = TimeHelper.getCurrentDay();
//        List<PWorldBox> list = new ArrayList<>(worldBoxManager.getBoxOpening().values());
        List<Player> players = playerManager.getPlayers().values().parallelStream().filter(e -> {
                    return e.getPWorldBox() != null && e.getPWorldBox().getWorldBoxList().size() > 0;
                }
        ).collect(Collectors.toList());
        for (Player player : players) {
            checkOpenBox(player);
        }
    }

    private synchronized void checkOpenBox(Player player) {
        PWorldBox e = player.getPWorldBox();
        WorldBox worldBox = e.getFirstOpenBox();
        if (worldBox != null) {
            worldBox.addTime();
            checkBoxOpenIng(e, worldBox);
            if (!worldBox.isChanged()){
                worldBox.setChanged(true);
            }
        } else {
            //获取第一个
            worldBox = e.getFirstWaitBox();
            if (worldBox != null) {
                worldBox.setOpenTime(worldBoxManager.getOpenTime(worldBox));
                worldBox.setState(WorldBoxState.OPENING);
            }
        }
    }

    /**
     * 检测宝箱开启
     *
     * @param e
     * @param worldBox
     */
    private void checkBoxOpenIng(PWorldBox e, WorldBox worldBox) {
        if (worldBox.isOpen()) {
            worldBox.setState(WorldBoxState.TURNED_ON);
            //可以开启下一个了
            WorldBox waitBox = e.getFirstWaitBox();
            if (waitBox != null) { //下一个等待的设置成开启中
                waitBox.setOpenTime(worldBoxManager.getOpenTime(waitBox));
                waitBox.setState(WorldBoxState.OPENING);
            }
            Player player = playerManager.getPlayer(e.getLordId());
            if (player != null) {
                WorldBoxPb.SynWorldBoxOpenRs.Builder builder = WorldBoxPb.SynWorldBoxOpenRs.newBuilder();
                player.getPWorldBox().getWorldBoxList().forEach(box -> {
                    builder.addBoxs(WorldBoxPb.WorldBox.newBuilder()
                            .setBoxId(box.getBoxId())
                            .setState(box.getState().getVal())
                            .setOpenTime(box.getOpenTime() * TimeHelper.SECOND_MS)
                            .setChanged(box.isChanged())
                            .build());
                });
                SynHelper.synMsgToPlayer(player, WorldBoxPb.SynWorldBoxOpenRs.EXT_FIELD_NUMBER, WorldBoxPb.SynWorldBoxOpenRs.ext, builder.build());
            }
        }
    }

    /**
     * 获取宝箱列表
     *
     * @param rq
     * @param handler
     */
    public void getWorldBox(WorldBoxPb.GetWorldBoxRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        PWorldBox pWorldBox = player.getPWorldBox();
        WorldBoxPb.GetWorldBoxRs.Builder builder = WorldBoxPb.GetWorldBoxRs.newBuilder();
        builder.setPoints(pWorldBox.getPoints());
        builder.setCount(pWorldBox.getCount());
        builder.setTodayPoints(pWorldBox.getTodayPoints());
        List<WorldBox> list = pWorldBox.getWorldBoxList().stream().sorted(Comparator.comparingInt(e -> e.getState().getVal())).collect(Collectors.toList());
        list.forEach(e -> {
            builder.addBoxs(WorldBoxPb.WorldBox.newBuilder()
                    .setBoxId(e.getBoxId())
                    .setState(e.getState().getVal())
                    .setOpenTime(worldBoxManager.getOpenTime(e) * TimeHelper.SECOND_MS)
                    .setChanged(e.isChanged())
                    .build());
        });
        handler.sendMsgToPlayer(WorldBoxPb.GetWorldBoxRs.ext, builder.build());
    }

    /**
     * 领取宝箱
     *
     * @param rq
     * @param handler
     */
    public void receiveWorldBox(WorldBoxPb.ReceiveWorldBoxRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        PWorldBox pWorldBox = player.getPWorldBox();
        int needCoind = limitMgr.getNum(SimpleId.WORLD_BOX_EXCHANGE);
        if (pWorldBox.getPoints() < needCoind) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_BOX_COIN_NOT_ENOUGH);
            return;
        }

//        if (pWorldBox.getCount() >= limitMgr.getNum(SimpleId.WORLD_BOX_DAY_OPEN)) {
//            handler.sendErrorMsgToPlayer(GameError.WORLD_BOX_TO_MORE);
//            return;
//        }
        boolean isDouble = false;
        int num = player.getLord().getWordBoxNum();
        List<Integer> firstTimes = limitMgr.getAddtion(SimpleId.WORLD_BOX_THREE_TIMES);
        if (num == firstTimes.size() - 1) {
            isDouble = true;
        } else if (num > firstTimes.size()) {
            isDouble = RandomHelper.randomInSize(100) < limitMgr.getNum(SimpleId.WORLD_BOX_DOUBLE);
        }
        WorldBox worldBox = worldBoxManager.randomBox(player);
        WorldBox extraBox = null;
        if (isDouble) {
            extraBox = WorldBox.builder()
                    .openTime(worldBox.getOpenTime())
                    .boxId(worldBox.getBoxId())
                    .state(WorldBoxState.WAIT)
                    .build();
        }
        if (worldBox == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }
        pWorldBox.delPoints(needCoind);
        pWorldBox.addCount();
        pWorldBox.addWorldBox(worldBox);
        checkOpenBox(player);
        player.getLord().addWorldBoxNum();
        WorldBoxPb.ReceiveWorldBoxRs.Builder builder = WorldBoxPb.ReceiveWorldBoxRs.newBuilder();
        builder.setPoints(pWorldBox.getPoints());
        if (worldBox != null) {
            builder.addBoxs(WorldBoxPb.WorldBox.newBuilder()
                    .setBoxId(worldBox.getBoxId())
                    .setState(worldBox.getState().getVal())
                    .setOpenTime(worldBoxManager.getOpenTime(worldBox) * TimeHelper.SECOND_MS)
                    .setChanged(worldBox.isChanged())
                    .build());
        }
        StaticWorldBox staticWorldBox = worldBoxDataMgr.getStaticWorldBox(worldBox.getBoxId());
        eventManager.worldBox(player, Lists.newArrayList(
                staticWorldBox.getQuality()
        ));
        //是否触发双倍奖励
        if (pWorldBox.getWorldBoxList().size() < limitMgr.getNum(SimpleId.WORLD_BOX_MAX) && extraBox != null) {
            pWorldBox.addWorldBox(extraBox);
            player.getLord().addWorldBoxNum();
            builder.addBoxs(WorldBoxPb.WorldBox.newBuilder()
                    .setBoxId(extraBox.getBoxId())
                    .setState(extraBox.getState().getVal())
                    .setOpenTime(worldBoxManager.getOpenTime(extraBox) * TimeHelper.SECOND_MS)
                    .setChanged(extraBox.isChanged())
                    .build());
            eventManager.worldBox(player, Lists.newArrayList(
                    staticWorldBox.getQuality()
            ));
        }
        SpringUtil.getBean(LogUser.class).world_box_log(WorldBoxLog.builder()
                .lordId(player.roleId)
                .nick(player.getNick())
                .level(player.getLevel())
                .vip(player.getVip())
                .count(-needCoind)
                .reason(0)
                .cur(player.getPWorldBox().getPoints())
                .num(builder.getBoxsCount())
                .build());
        handler.sendMsgToPlayer(WorldBoxPb.ReceiveWorldBoxRs.ext, builder.build());
    }

    /**
     * 开启宝箱
     *
     * @param rq
     * @param handler
     */
    public void openWorldBox(WorldBoxPb.OpenWorldBoxRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        PWorldBox pWorldBox = player.getPWorldBox();
        int index = rq.getIndex();
        if (index >= pWorldBox.getWorldBoxList().size()) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        WorldBoxPb.OpenWorldBoxRs.Builder builder = WorldBoxPb.OpenWorldBoxRs.newBuilder();
        WorldBox worldBox = pWorldBox.getWorldBoxList().get(rq.getIndex());

        if (worldBox.getState() != WorldBoxState.TURNED_ON) {
            StaticWorldBox staticWorldBox = staticWorldBoxDataMgr.getStaticWorldBox(worldBox.getBoxId());

            if (staticWorldBox != null) {
                List<List<Integer>> need = staticWorldBox.getNeed();
                if(need!=null){
                    boolean b = playerManager.checkAndSubItem(player, need, Reason.WORLD_BOX_OPEN);
                    if (!b) {
                    handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
                    return;
                }
                    // playerManager.subAward(player, AwardType.GOLD, 0, staticWorldBox.getNeed(), Reason.WORLD_BOX_OPEN);
                builder.setGold(player.getGold());
                    need.forEach(x -> {
                        Integer integer = x.get(0);
                        if (integer == AwardType.PROP) {
                            Item item = player.getItem(x.get(1));
                            builder.addKey(item.wrapPb());
                        }
                    });
                }
            }
        }
        if (!limitMgr.getAddtion(SimpleId.WORLD_BOX_OPEN_KEY).contains(rq.getKey())) {
            handler.sendErrorMsgToPlayer(GameError.ITEM_TYPE_ERROR);
            return;
        }
        List<Award> awards = worldBoxManager.openWorldBox(worldBox);
        pWorldBox.getWorldBoxList().remove(rq.getIndex());
        StaticWorldBox staticWorldBox = worldBoxDataMgr.getStaticWorldBox(worldBox.getBoxId());
        for (Award award : awards) {
            int keyId = playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), Reason.WORLD_BOX_OPEN);
            builder.addAward(PbHelper.createAward(player, award.getType(), award.getId(), award.getCount(), keyId));
            for (List<Integer> chats : staticWorldBox.getNeedChat()) {
                if (award.getType() == chats.get(0) && award.getId() == chats.get(1)) {
                    chatManager.sendWorldChat(ChatId.WORLD_BOX_CHAT, player.getNick(),
                            staticWorldBox.getQuality() + "", staticWorldBox.getBoxId() + "",
                            award.getType() + "", award.getId() + "");
                }
            }
        }

        worldBoxManager.calcuPoints(WorldBoxTask.CAMP_SYNERGY, player, 1);
        checkOpenBox(player);
        builder.setGold(player.getGold());
        List<WorldBox> list = pWorldBox.getWorldBoxList().stream().sorted(Comparator.comparingInt(e -> e.getState().getVal())).collect(Collectors.toList());
        list.forEach(e -> {
            builder.addBoxs(WorldBoxPb.WorldBox.newBuilder()
                    .setBoxId(e.getBoxId())
                    .setState(e.getState().getVal())
                    .setOpenTime(worldBoxManager.getOpenTime(e) * TimeHelper.SECOND_MS)
                    .setChanged(e.isChanged())
                    .build());
        });
        handler.sendMsgToPlayer(WorldBoxPb.OpenWorldBoxRs.ext, builder.build());
    }

    /**
     * 宝箱置顶
     *
     * @param rq
     * @param handler
     */
    public void topWolrdBox(WorldBoxPb.TopWolrdBoxRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        PWorldBox pWorldBox = player.getPWorldBox();
        int index = rq.getIndex();
        if (index >= pWorldBox.getWorldBoxList().size()) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        long num = pWorldBox.getWorldBoxList().stream().filter(e -> e.getState() != WorldBoxState.TURNED_ON).count();
        if (num < 2) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_BOX_IN_OPEN);
            return;
        }
        WorldBox worldBox = pWorldBox.getWorldBoxList().get(index);
        if (worldBox.getState() == WorldBoxState.OPENING) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_BOX_IN_OPEN);
            return;
        }
        worldBox = pWorldBox.getWorldBoxList().remove(index);
        worldBox.setOpenTime(worldBoxManager.getOpenTime(worldBox));
        worldBox.setState(WorldBoxState.OPENING);
//        System.out.println("置顶箱子倒计时：" + worldBoxManager.getOpenTime(worldBox));
        WorldBox firstOpenBox = pWorldBox.getFirstOpenBox();
        firstOpenBox.setState(WorldBoxState.WAIT);

        pWorldBox.insertAfterOpening(worldBox);
//        pWorldBox.getWorldBoxList().add(0, worldBox);
//        System.out.println("替换箱子倒计时：" + worldBoxManager.getOpenTime(firstOpenBox));
        checkOpenBox(player);
        WorldBoxPb.TopWolrdBoxRs.Builder builder = WorldBoxPb.TopWolrdBoxRs.newBuilder();
        List<WorldBox> list = pWorldBox.getWorldBoxList().stream().sorted(Comparator.comparingInt(e -> e.getState().getVal())).collect(Collectors.toList());
        list.forEach(e -> {
            builder.addBoxs(WorldBoxPb.WorldBox.newBuilder()
                    .setBoxId(e.getBoxId())
                    .setState(e.getState().getVal())
                    .setOpenTime(worldBoxManager.getOpenTime(e) * TimeHelper.SECOND_MS)
                    .setChanged(e.isChanged())
                    .build());
        });
        handler.sendMsgToPlayer(WorldBoxPb.TopWolrdBoxRs.ext, builder.build());
    }

    /**
     * 宝箱丢弃
     *
     * @param rq
     * @param handler
     */
    public void dropWolrdBox(WorldBoxPb.DropWolrdBoxRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        PWorldBox pWorldBox = player.getPWorldBox();
        int index = rq.getIndex();
        if (index >= pWorldBox.getWorldBoxList().size()) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        WorldBox worldBox = pWorldBox.getWorldBoxList().get(index);
        if (worldBox.getState() == WorldBoxState.TURNED_ON
                || worldBox.getState() == WorldBoxState.OPENING) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        pWorldBox.getWorldBoxList().remove(index);
        StaticWorldBox staticWorldBox = worldBoxDataMgr.getStaticWorldBox(worldBox.getBoxId());
        WorldBox openBox = pWorldBox.getFirstOpenBox();
        if (openBox != null) {
            openBox.delayOpenTime(staticWorldBox.getAbandonTime());
            checkBoxOpenIng(pWorldBox, openBox);
        }
        checkOpenBox(player);
        WorldBoxPb.DropWolrdBoxRs.Builder builder = WorldBoxPb.DropWolrdBoxRs.newBuilder();
        pWorldBox.getWorldBoxList().stream().sorted(Comparator.comparingInt(e -> e.getState().getVal())).forEach(e -> {
            builder.addBoxs(WorldBoxPb.WorldBox.newBuilder()
                    .setBoxId(e.getBoxId())
                    .setState(e.getState().getVal())
                    .setOpenTime(worldBoxManager.getOpenTime(e) * TimeHelper.SECOND_MS)
                    .setChanged(e.isChanged())
                    .build());
        });
        handler.sendMsgToPlayer(WorldBoxPb.DropWolrdBoxRs.ext, builder.build());
    }
}
