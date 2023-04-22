package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.HashMap;
import java.util.Map;
import com.game.domain.s.StaticChatShow;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticChat;

@Component
@LoadData(name = "聊天")
public class StaticChatMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;
	// 国家荣誉
	private Map<Integer, StaticChat> chats = new HashMap<Integer, StaticChat>();
	// 全服走马灯
	private Map<Integer, StaticChatShow> chatShowMap = new HashMap<Integer, StaticChatShow>();
	private HashBasedTable<Integer, Integer, StaticChatShow> chatTable = HashBasedTable.create();

	@Override
	public void load() throws Exception {
		chats = staticDataDao.selectChat();
		chatShowMap = staticDataDao.selectChatShow();
		makeChatTable();
	}

	@Override
	public void init() throws Exception {
	}

	public StaticChat getChat(int chatId) {
		return chats.get(chatId);
	}

	public void makeChatTable() {
		for (StaticChatShow chatShow : chatShowMap.values()) {
			if (chatShow == null) {
				continue;
			}

			chatTable.put(chatShow.getType(), chatShow.getId(), chatShow);
		}
	}

	public StaticChatShow getChatShow(int type, int id) {
		return chatTable.get(type, id);
	}
}
