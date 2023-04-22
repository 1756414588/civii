package com.game.chat.domain;

import com.game.pb.CommonPb;
/**
 * system chat
 */
public class SystemChat extends Chat {
	private long time;
	private int chatId;
	private int country;
	private String[] param;

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

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}

	@Override
	public com.game.pb.CommonPb.Chat ser(int style, int officerId) {
		CommonPb.Chat.Builder builder = CommonPb.Chat.newBuilder();
		builder.setStyle(this.style);
		builder.setChatType(this.chatType);
		builder.setChatId(chatId);
		builder.setTime(time);
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
