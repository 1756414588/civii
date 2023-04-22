package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.domain.p.Item;
import com.game.server.thread.SaveItemThread;
import com.game.server.thread.SaveServer;
import com.game.server.thread.SaveThread;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @Author 陈奎
 * @Description 道具数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "道具")
@Service
public class SaveItemServer extends SaveServer<Item> {

	public SaveItemServer() {
		super("SAVE_ITEM_SERVER", 128);
	}

	@Override
	public SaveThread createThread(String name) {
		return new SaveItemThread(name);
	}


	@Override
	public void saveData(Item item) {
		SaveThread thread = threadPool.get((int) (item.getLordId() % threadNum));
		thread.add(item);
	}

	@Override
	public void saveAll() {
	}
}
