package com.game.chat.domain;

import com.game.domain.Player;
import com.game.domain.p.Lord;
import com.game.pb.CommonPb;

public class ManChat extends Chat {

	private Player player;
	private long time;
	private String msg;

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

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}

	@Override
	public CommonPb.Chat ser(int style, int officerId, int targetCountry) {
		CommonPb.Chat.Builder builder = CommonPb.Chat.newBuilder();
		Lord lord = player.getLord();
		builder.setLordId(lord.getLordId());
		// 判断是否为游戏引导员发的阵营消息
		if (targetCountry != 0 && player.getAccount().getIsGuider() == 1) {
			builder.setCountry(targetCountry);
		} else {
			builder.setCountry(player.getCountry());
		}
		builder.setTitle(player.getTitle());
		builder.setLevel(player.getLevel());
		if (lord.getNick() != null) {
			builder.setName(lord.getNick());
		}
		builder.setPortrait(lord.getPortrait());
		builder.setX(lord.getPosX());
		builder.setY(lord.getPosY());
		builder.setTime(time);
		builder.setStyle(this.style);
		builder.setChatType(this.chatType);
		if (style != 0) {
			builder.setStyle(style);
		}
		builder.setMsg(msg);
		builder.setGm(player.account.getIsGm() != 0);
		builder.setGuider(player.account.getIsGuider() != 0);
		builder.setOfficerId(officerId);
		builder.setChatIndex(player.getLord().getChatIndex());
		builder.setHeadIndex(player.getLord().getHeadIndex());
		return builder.build();
	}

}
