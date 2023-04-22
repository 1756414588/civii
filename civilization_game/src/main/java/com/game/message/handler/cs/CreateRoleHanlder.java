/**
 * @Title: CreateRoleHanlder.java
 * @Package ccom.game.message.handler
 * @Description: TODO
 * @author ZhangJun
 * @date 2015年8月3日 下午12:48:25
 * @version V1.0
 */
package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb.CreateRoleRq;
import com.game.service.PlayerService;
import com.game.util.LogHelper;

public class CreateRoleHanlder extends ClientHandler {

	/**
	 * Overriding: action
	 *
	 * @see com.game.message.handler.Handler#action()
	 */
	@Override
	public void action() {
		CreateRoleRq req = msg.getExtension(CreateRoleRq.ext);
		PlayerService playerService = getService(PlayerService.class);
		playerService.createRole(req, this);

		LogHelper.MESSAGE_LOGGER.info("CreateRoleRq req:{}", req);
	}

}
