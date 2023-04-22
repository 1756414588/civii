package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.server.thread.SaveServer;
import com.game.spring.SpringUtil;
import java.util.Iterator;

import com.game.manager.ServerRadioManager;
import com.game.server.thread.SaveServerRadioThread;
import com.game.server.thread.SaveThread;
import com.game.servlet.domain.ServerRadio;
import com.game.util.LogHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 广播数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "广播存储")
@Service
public class SaveServerRadioServer extends SaveServer<ServerRadio> {

	public SaveServerRadioServer() {
		super("SAVE_SERVER_RADIO_SERVER", 2);
	}

	public SaveThread createThread(String name) {
		return new SaveServerRadioThread(name);
	}

	@Override
	public void saveData(ServerRadio ServerRadio) {
		SaveThread thread = threadPool.get((int) (ServerRadio.getKeyId() % threadNum));
		thread.add(ServerRadio);
	}

	@Override
	public void saveAll() {
		ServerRadioManager serverRadioManager = SpringUtil.getBean(ServerRadioManager.class);
		Iterator<ServerRadio> Iterator = serverRadioManager.selectServerRadio().values().iterator();
		int saveCount = 0;
		while (Iterator.hasNext()) {
			try {
				ServerRadio serverRadio = Iterator.next();
				saveCount++;
				saveData(serverRadio);
			} catch (Exception e) {
				LogHelper.ERROR_LOGGER.error("SAVE_SERVER_RADIO_SERVER:{}", e.getMessage(), e);
			}
		}
		LogHelper.SAVE_LOGGER.info(name + " serData count:" + saveCount);
	}

}
