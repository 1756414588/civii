package com.game.message.handler.cs;

import com.game.constant.GameError;
import com.game.constant.Reason;
import com.game.dataMgr.StaticAchiMgr;
import com.game.domain.Player;
import com.game.domain.p.AchievementInfo;
import com.game.domain.s.StaticAchiAwardBox;
import com.game.manager.PlayerManager;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.Handler;
import com.game.packet.Packet;
import com.game.pb.ActivityPb;
import com.game.pb.BasePb;
import com.game.service.AchievementService;
import com.game.util.PbHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AchievementBoxAwardHandler extends ClientHandler {

    @Autowired
    PlayerManager playerManager;
    @Autowired
    StaticAchiMgr staticAchiMgr;

    @Override
    public void action() {

    }

//    @Override
//    public void event(BasePb.Base msg, Player player) {
////        Channel channel = ctx.channel();
//
//
//        ActivityPb.AchievementBoxAwardRq req = msg.getExtension(ActivityPb.AchievementBoxAwardRq.ext);
//        int id = req.getId();
//        StaticAchiAwardBox staticAchiAwardBoxById = staticAchiMgr.getStaticAchiAwardBoxById(id);
//        if (staticAchiAwardBoxById == null) {
//            sendErrorMsgToPlayer(GameError.PARAM_ERROR);
//            return;
//        }
//        AchievementInfo achievementInfo = player.getAchievementInfo();
//        if (achievementInfo.getScoreAwardMap().containsKey(id)) {
//            sendErrorMsgToPlayer(GameError.TARGET_AWARD_IS_AWARD);
//            return;
//        }
//        boolean flag = false;
//        if (staticAchiAwardBoxById.getChildType() == 0) {
//            if (achievementInfo.getScore() >= staticAchiAwardBoxById.getCond()) {
//                flag = true;
//            }
//        } else {
//            int orDefault = achievementInfo.getTypeScoreMap().getOrDefault(staticAchiAwardBoxById.getType(), 0);
//            if (orDefault >= staticAchiAwardBoxById.getCond()) {
//                flag = true;
//            }
//        }
//        if (!flag) {
//            sendErrorMsgToPlayer(GameError.SCORE_NOT_ENOUGH);
//            return;
//        }
//        achievementInfo.getScoreAwardMap().put(id, 1);
//        List<List<Integer>> award = staticAchiAwardBoxById.getAward();
//        ActivityPb.AchievementBoxAwardRs.Builder builder = ActivityPb.AchievementBoxAwardRs.newBuilder();
//        if (award != null) {
//            award.forEach(x -> {
//                playerManager.addAward(player, x.get(0), x.get(1), x.get(2), Reason.ACHI);
//                builder.addAward(PbHelper.createAward(x.get(0), x.get(1), x.get(2)));
//            });
//        }
//        sendMsgToPlayer(ctx, ActivityPb.AchievementBoxAwardRs.ext, builder.build());
//
//    }
//
//    @Override
//    public void reg() {
//        add(ActivityPb.AchievementBoxAwardRq.EXT_FIELD_NUMBER, ActivityPb.AchievementBoxAwardRs.EXT_FIELD_NUMBER, this);
//    }
}
