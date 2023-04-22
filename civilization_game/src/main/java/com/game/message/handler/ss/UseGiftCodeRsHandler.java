
package com.game.message.handler.ss;

import com.game.message.handler.ServerHandler;
import com.game.pb.InnerPb.UseGiftCodeRs;
import com.game.service.PlayerService;
import com.game.spring.SpringUtil;


public class UseGiftCodeRsHandler extends ServerHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		UseGiftCodeRs req = msg.getExtension(UseGiftCodeRs.ext);
		PlayerService playerService = SpringUtil.getBean(PlayerService.class);
		playerService.useGiftCodeRs(req, this);
	}

}
