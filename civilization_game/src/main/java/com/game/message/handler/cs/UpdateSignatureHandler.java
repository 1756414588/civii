package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb.UpdateSignatureRq;
import com.game.service.ChatService;

/**
 * @Description TODO
 * @Date 2021/1/25 19:31
 **/
public class UpdateSignatureHandler extends ClientHandler {
    @Override
    public void action() {
        ChatService service = getService(ChatService.class);
        UpdateSignatureRq rq = msg.getExtension(UpdateSignatureRq.ext);
        service.updateSignatureRq(rq,this);
    }
}
