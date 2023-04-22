package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.domain.p.Chat;
import com.game.manager.ChatManager;
import com.game.pb.CommonPb;
import com.game.server.thread.SaveChatThread;
import com.game.server.thread.SaveServer;
import com.game.server.thread.SaveThread;
import com.game.util.LogHelper;

import com.game.spring.SpringUtil;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 聊天数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "聊天存储")
@Service
public class SaveChatServer extends SaveServer<Chat> {

	public SaveChatServer() {
		super("SAVE_CHAT_SERVER", 1);
	}

	@Override
	public SaveThread createThread(String name) {
		return new SaveChatThread(name);
	}

	@Override
	public void saveData(Chat chat) {
		SaveThread thread = threadPool.get((chat.getChatId() % threadNum));
		thread.add(chat);
	}

	@Override
	public void saveAll() {
		try {

			/**
			 * 保存阵营聊天数据
			 */
			ChatManager chatManager = SpringUtil.getBean(ChatManager.class);
			chatManager.cleanChat();
			Map<Integer, ConcurrentLinkedDeque<CommonPb.Chat>> countrys = chatManager.getCountrys();
			for (ConcurrentLinkedDeque<CommonPb.Chat> value : countrys.values()) {
				value.forEach(x -> {
					Chat chatCountry = new Chat(x);
					chatCountry.setType(Chat.COUNTRYS);
					saveData(chatCountry);
				});
			}
			/**
			 * 保存区域聊天数据
			 */
			Map<Integer, ConcurrentLinkedDeque<com.game.pb.CommonPb.Chat>> mapChat = chatManager.getMapChat();
			Iterator<ConcurrentLinkedDeque<com.game.pb.CommonPb.Chat>> iteratorMap = mapChat.values().iterator();
			while (iteratorMap.hasNext()) {
				ConcurrentLinkedDeque<com.game.pb.CommonPb.Chat> linkedList = iteratorMap.next();
				for (com.game.pb.CommonPb.Chat chat : linkedList) {
					Chat chatMap = new Chat(chat);
					chatMap.setType(Chat.MAPCHAT);
					saveData(chatMap);
				}
			}
			/**
			 * 保存世界聊天
			 */
			ConcurrentLinkedDeque<CommonPb.Chat> worldChats = chatManager.getWorld();
			for (com.game.pb.CommonPb.Chat chat : worldChats) {
				Chat worldChat = new Chat(chat);
				worldChat.setType(Chat.WORLD);
				saveData(worldChat);
			}

			/**
			 * 保存Vip聊天
			 *
			 */
			ConcurrentLinkedDeque<com.game.pb.CommonPb.Chat> vipChats = chatManager.getVipChat();
			for (com.game.pb.CommonPb.Chat chat : vipChats) {
				Chat vipChat = new Chat(chat);
				vipChat.setType(Chat.VIPCHAT);
				saveData(vipChat);
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("SAVE_CHAT_SERVER:{}", e.getMessage(), e);
		}
	}
}
