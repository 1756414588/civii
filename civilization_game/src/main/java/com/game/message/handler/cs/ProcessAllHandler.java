package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb.ProcessAllRq;
import com.game.service.FriendService;
import com.game.spring.SpringUtil;

/**
 * @Description 一键处理 好友或者 师徒 申请
 * @ProjectName halo_server
 * @Date 2021/9/27 11:42
 **/
public class ProcessAllHandler extends ClientHandler {
	@Override
	public void action() {
		SpringUtil.getBean(FriendService.class).processAllRq(this, msg.getExtension(ProcessAllRq.ext));
	}
}
