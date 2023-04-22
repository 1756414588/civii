package com.game.message.cs;

import com.game.domain.Robot;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Mail;
import com.game.pb.MailPb.GetMailRs;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

/**
 * 获取邮件信息
 */
public class GetMailHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		GetMailRs msg = req.getExtension(GetMailRs.ext);

		Robot robot = getRobot(accountKey);
		Map<Long, Mail> mails = robot.getMails();

		if (msg.getMailCount() <= 0) {
			return;
		}

		msg.getMailList().forEach(e -> {
			mails.put(e.getKeyId(), e);
		});
	}

}
