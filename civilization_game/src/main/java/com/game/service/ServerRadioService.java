package com.game.service;

import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.constant.ChatId;
import com.game.manager.ChatManager;
import com.game.manager.ServerRadioManager;
import com.game.servlet.domain.ServerRadio;
import com.game.util.DateHelper;

@Service
public class ServerRadioService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServerRadioManager serverRadioManager;

	@Autowired
	private ChatManager chatManager;

	/**
	 * 全服邮件定时任务执行
	 */
	public void serverRadioTimerLogic() {
		Iterator<ServerRadio> iterator = serverRadioManager.selectServerRadio().values().iterator();

		while (iterator.hasNext()) {
			ServerRadio serverRadio = iterator.next();

			try {
				long startTime = serverRadio.getStartTime().getTime() / 1000;
				long endTime = serverRadio.getEndTime().getTime() / 1000;
				long lastSendTime = serverRadio.getLastSendTime() / 1000;
				Integer frequency = serverRadio.getFrequency();
				long frequencyTime = lastSendTime + frequency;

				long serverTime = DateHelper.getServerTime();

				if (serverRadio.getStatus() != ServerRadio.HAVE_SEND && serverRadio.getRemove() == ServerRadio.UN_REMOVE) {
					if (endTime < serverTime) {
						serverRadio.setStatus(ServerRadio.HAVE_SEND);
					} else if (endTime >= serverTime && startTime <= serverTime) {
						if (frequencyTime <= serverTime) {
							serverRadio.setStatus(ServerRadio.SEND_ING);
							serverRadio.setLastSendTime(new Date().getTime());
							//System.out.println(DateHelper.formatDateTime(new Date(), DateHelper.format1) + serverRadio.getMessage());

							if (serverRadio.getChannelList().size() > 0) {
								serverRadio.getChannelList().forEach(e -> {
									chatManager.sendChannelWorldChat(ChatId.GM_NOTICE, e, serverRadio.getMessage());
								});
							} else {
								chatManager.sendWorldChat(ChatId.GM_NOTICE, serverRadio.getMessage());
							}
						}
					}
				}
			} catch (Exception e) {
				iterator.remove();
				logger.error("ServerRadioService serverRadioTimerLogic : parames {},desc{}", serverRadio, e);
			}
		}
	}
}
