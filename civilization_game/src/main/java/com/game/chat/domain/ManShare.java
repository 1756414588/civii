package com.game.chat.domain;

import com.game.domain.Player;
import com.game.domain.p.Lord;
import com.game.pb.CommonPb;

public class ManShare extends Chat {
	private Player player;
	private long time;
	private int chatId;
	private String[] param;
	private int mailKeyId;

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getChatId() {
		return chatId;
	}

	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

	public String[] getParam() {
		return param;
	}

	public void setParam(String[] param) {
		this.param = param;
	}

	public int getMailKeyId() {
		return mailKeyId;
	}

	public void setMailKeyId(int mailKeyId) {
		this.mailKeyId = mailKeyId;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}

	@Override
	public CommonPb.Chat ser(int style, int officerId, int targetCountry) {
		CommonPb.Chat.Builder builder = CommonPb.Chat.newBuilder();
		Lord lord = player.getLord();
		builder.setStyle(this.style);
		builder.setChatType(this.chatType);
		builder.setLordId(lord.getLordId());
		builder.setCountry(player.getCountry());
		builder.setTitle(player.getTitle());
		builder.setLevel(player.getLevel());
		if (lord.getNick() != null) {
			builder.setName(lord.getNick());
        }
		builder.setPortrait(lord.getPortrait());
		builder.setX(lord.getPosX());
		builder.setY(lord.getPosY());
		builder.setTime(time);
		builder.setOfficerId(officerId);
		builder.setChatIndex(player.getLord().getChatIndex());
		builder.setHeadIndex(player.getLord().getHeadIndex());
		if (mailKeyId != 0) {
			builder.setMailKeyId(mailKeyId);
		}
		if (chatId != 0) {
			builder.setChatId(chatId);
		}

		if (param != null) {
			for (int i = 0; i < param.length; i++) {
				if (param[i] != null) {
					builder.addParam(param[i]);
				}
			}
		}

		return builder.build();
	}

}
