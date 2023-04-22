package com.game.message.listen;

import com.game.domain.Robot;
import com.game.manager.ChatManager;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.ChatPb.ShareMailRq;
import com.game.pb.CommonPb.Mail;
import com.game.util.BasePbHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 监听分享邮件
 */
public class ListenShareMailHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		RobotManager robotManager = getBean(RobotManager.class);
		ChatManager chatManager = getBean(ChatManager.class);
		List<Robot> robotList = robotManager.getRobotList();
		robotList.forEach(e -> {
			Mail mail = chatManager.getShareMail(e);
			if (mail != null) {
				ShareMailRq.Builder builder = ShareMailRq.newBuilder();
				builder.setMailKeyId((int) mail.getKeyId());
				Base base = BasePbHelper.createRqBase(ShareMailRq.EXT_FIELD_NUMBER, ShareMailRq.ext, builder.build()).build();
				e.sendPacket(PacketCreator.create(base));
			}
		});
	}

}
