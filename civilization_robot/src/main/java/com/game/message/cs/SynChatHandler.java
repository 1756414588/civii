package com.game.message.cs;

import com.game.cache.ChatCacheManager;
import com.game.constant.ChatId;
import com.game.domain.ChatShare;
import com.game.domain.Robot;
import com.game.manager.MessageManager;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.ChatPb.SynChatRq;
import com.game.pb.CommonPb.Chat;
import com.game.spring.SpringUtil;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.RandomUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @Description 聊天分享
 * @Date 2022/9/22 10:20
 **/
public class SynChatHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		SynChatRq msg = req.getExtension(SynChatRq.ext);

		Chat chat = msg.getChat();
		int chatId = chat.getChatId();
		switch (chatId) {
			case ChatId.BIG_MONSTER_HELP:// 巨型虫族请求支援
				bigMonsterShare(chat);
			default:
		}
	}

	private void bigMonsterShare(Chat chat) {

		String pos = chat.getParam(1);
		ChatCacheManager chatCacheManager = SpringUtil.getBean(ChatCacheManager.class);
		String[] poss = pos.split(",");
		int x = Integer.valueOf(poss[0]);
		int y = Integer.valueOf(poss[1]);
		int id = x * 1000 + y;

		if (chatCacheManager.contain(id)) {
			return;
		}

		long delay = 10000 + RandomUtil.getRandomNumber(480000) + System.currentTimeMillis();

		ChatShare chatShare = new ChatShare();
		chatShare.setId(id);
		chatShare.setPos(pos);
		chatShare.setShareId(chat.getLordId());
		chatShare.setPosX(x);
		chatShare.setPosY(y);
		chatShare.setCountry(chat.getCountry());
		chatShare.setChatId(chat.getChatId());
		chatShare.setDelayTime(delay);
		chatShare.setShareTime(System.currentTimeMillis() + 1800000);// 删除时间
		chatShare.setRan(RandomUtil.getRandomNumber(100));

		chatCacheManager.put(chatShare);
		LogHelper.CHANNEL_LOGGER.info("巨型虫族 pos:{} country:{} 攻打时间:{} 概率:{}", pos, chatShare.getCountry(), DateHelper.getDate(delay), chatShare.getRan());
	}
}
