package com.game.service;

import com.game.constant.GameError;
import com.game.constant.PersonalSignature;
import com.game.dataMgr.StaticPersonalityMgr;
import com.game.domain.Player;
import com.game.domain.p.Frame;
import com.game.domain.s.StaticPersonality;
import com.game.enumerate.FrameState;
import com.game.manager.PersonalityManager;
import com.game.manager.PlayerManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.RolePb;
import com.game.util.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @date 2021/1/26 19:13
 * @description
 */
@Service
public class FrameService {
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private PersonalityManager personalityManager;

    @Autowired
    private StaticPersonalityMgr staticPersonalityMgr;

    public void getFrameRq(RolePb.getFrameRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Map<Integer, Frame> frameMap = player.getFrameMap();

        RolePb.getFrameRs.Builder builder = RolePb.getFrameRs.newBuilder();
        builder.setDefaultHead(player.getLord().getHeadIndex());
        builder.setDefaultChat(player.getLord().getChatIndex());
        personalityManager.getHeadList().forEach(e -> {
            Frame f = frameMap.get(e.getId());
            if (f != null) {
                boolean show = false;
                if (f.getState() == FrameState.lock) {
                    show = false;
                } else {
                    show = !f.isShow();
                }
                builder.addHeadFrame(CommonPb.Frame.newBuilder().setId(e.getId()).setName(e.getName()).setDesc(e.getDesc()).setExpireTime(frameMap.get(e.getId()).expireTime()).setIcon(e.getIcon()).setState(frameMap.get(e.getId()).getState().getVal()).setIsNew(show).build());

            }
        });
        Map<Integer, List<StaticPersonality>> chatList = personalityManager.getChatList().stream().collect(Collectors.groupingBy(e -> e.getMenu()));
        chatList.forEach((e, f) -> {
            CommonPb.ChatFrame.Builder chatFrame = CommonPb.ChatFrame.newBuilder();
            chatFrame.setTitle(f.get(0).getMenuDesc());
            f.forEach(staticPersonality -> {
                Frame ff = frameMap.get(staticPersonality.getId());
                boolean show = false;
                if (ff.getState() == FrameState.lock) {
                    show = false;
                } else {
                    show = !ff.isShow();
                }
                chatFrame.addFrame(CommonPb.Frame.newBuilder().setId(staticPersonality.getId()).setName(staticPersonality.getName()).setDesc(staticPersonality.getDesc()).setExpireTime(frameMap.get(staticPersonality.getId()).expireTime()).setIcon(staticPersonality.getIcon()).setState(frameMap.get(staticPersonality.getId()).getState().getVal()).setIsNew(show).build());
            });
            builder.addChatFrame(chatFrame);
        });
        String personalSignature = player.getPersonalSignature();
        builder.setPersonalSignature(personalSignature);
        handler.sendMsgToPlayer(RolePb.getFrameRs.ext, builder.build());
    }

    /**
     * 设置头像/聊天框
     *
     * @param req
     * @param handler
     */
    public void setFrameRq(RolePb.setFrameRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        int state = req.getState();
        int id = req.getId();
        Frame frame = player.getFrameMap().get(id);
        if (frame == null) {
            LogHelper.CONFIG_LOGGER.info("frame is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (frame.getState() == FrameState.lock) {
            LogHelper.CONFIG_LOGGER.info("frame is lock.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        switch (state) {
            case 1:
                player.getLord().setHeadIndex(id);
                break;
            default:
                player.getLord().setChatIndex(id);
                break;
        }
        frame.setShow(true);
        handler.sendMsgToPlayer(RolePb.setFrameRs.ext, RolePb.setFrameRs.newBuilder().build());
    }

    public void enterFrameRq(RolePb.enterFrameRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        staticPersonalityMgr.getDataMap().values().stream().filter(e -> e.getType() == req.getType()).forEach(e -> {
            Frame frame = player.getFrameMap().get(e.getId());
            if (frame != null && frame.getState() == FrameState.unlock) {
                frame.setShow(true);
            }
        });
        handler.sendMsgToPlayer(RolePb.enterFrameRs.ext, RolePb.enterFrameRs.newBuilder().build());
    }
}
