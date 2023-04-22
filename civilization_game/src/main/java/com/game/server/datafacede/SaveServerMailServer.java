package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.server.thread.SaveServer;
import com.game.spring.SpringUtil;
import java.util.Iterator;

import com.game.manager.MailManager;
import com.game.server.thread.SaveServerMailThread;
import com.game.server.thread.SaveThread;
import com.game.servlet.domain.SendMail;
import com.game.util.LogHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 邮件数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "邮件")
@Service
public class SaveServerMailServer extends SaveServer<SendMail> {

	public SaveServerMailServer() {
		super("SAVE_SERVER_MAIL_SERVER", 2);
	}

	public SaveThread createThread(String name) {
		return new SaveServerMailThread(name);
	}

	@Override
	public void saveData(SendMail sendMail) {
		SaveThread thread = threadPool.get((int) (sendMail.getKeyId() % threadNum));
		thread.add(sendMail);
	}

	@Override
	public void saveAll() {
		MailManager mailManager = SpringUtil.getBean(MailManager.class);
		Iterator<SendMail> Iterator = mailManager.selectAllServerMail().values().iterator();
		while (Iterator.hasNext()) {
			try {
				SendMail sendMail = Iterator.next();
				saveData(sendMail);
			} catch (Exception e) {
				LogHelper.ERROR_LOGGER.error("SAVE_SERVER_MAIL_SERVER:{}", e.getMessage(), e);
			}
		}
	}
}
