package com.game.acion;

import com.game.domain.Robot;
import com.game.pb.BasePb.Base;
import com.game.pb.ChatPb;

/**
 *
 * @Description收到分享消息,玩家参与协助
 * @Date 2022/10/20 17:33
 **/

public class ChatEvent extends MessageAction {

	private ChatPb chatPb;

	public ChatEvent(ChatPb chatPb) {
		super(null);
		this.chatPb = chatPb;
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
	}
}
