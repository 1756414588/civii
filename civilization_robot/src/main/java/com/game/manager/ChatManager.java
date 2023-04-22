package com.game.manager;

import com.game.domain.Robot;
import com.game.pb.CommonPb.Mail;
import com.game.cache.StaticMailCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatManager {

	@Autowired
	private StaticMailCache mailTemplate;

	public Mail getShareMail(Robot robot) {
		for (Mail mail : robot.getMails().values()) {
			if (mailTemplate.canShare(mail.getMailId())) {
				return mail;
			}
		}
		return null;
	}
}
