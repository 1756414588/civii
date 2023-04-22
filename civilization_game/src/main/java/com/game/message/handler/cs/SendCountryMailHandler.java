package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;
import com.game.spring.SpringUtil;

/**
 * @author cpz
 * @date 2020/12/22 15:51
 * @description
 */
public class SendCountryMailHandler extends ClientHandler {
    @Override
    public void action() {
        MailPb.SendCountryMailRq rq = msg.getExtension(MailPb.SendCountryMailRq.ext);
        SpringUtil.getBean(MailService.class).sendCountryMailRq(rq, this);
    }
}
