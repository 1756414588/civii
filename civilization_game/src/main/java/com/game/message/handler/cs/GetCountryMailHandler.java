package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;
import com.game.spring.SpringUtil;

/**
 *
 * @date 2020/12/22 15:51
 * @description
 */
public class GetCountryMailHandler extends ClientHandler {
    @Override
    public void action() {
        MailPb.GetCountryMailRq rq = msg.getExtension(MailPb.GetCountryMailRq.ext);
        SpringUtil.getBean(MailService.class).getCountryMailRq(rq, this);
    }
}
