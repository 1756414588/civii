package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ChatPb;
import com.game.service.PlayerService;

/**
 *
 * @date 2020/9/25 16:12
 * @description
 */
public class RecordHandler extends ClientHandler {
    @Override
    public void action() {
        ChatPb.RecordRq rq = msg.getExtension(ChatPb.RecordRq.ext);
        PlayerService service = getService(PlayerService.class);
        service.recordUI(this,rq);
    }
}
