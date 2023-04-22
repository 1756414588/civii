package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.MailService;

/**
 * @author jyb
 * @date 2020/7/29 11:23
 * @description
 */
public class GetMailCountHandler extends ClientHandler {
    @Override
    public void action() {
        MailService service = getService(MailService.class);
        service.getMailCount(this);
    }
}
