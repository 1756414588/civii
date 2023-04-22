package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;

/**
 * @author CaoBing
 * @date 2021/2/18 17:00
 */
public class GetPersonMailHandler extends ClientHandler {
    @Override
    public void action() {
        MailService service = getService(MailService.class);
        MailPb.GetPersonMailRq req = msg.getExtension(MailPb.GetPersonMailRq.ext);
        service.GetPersonMailRq(req, this);
    }
}
