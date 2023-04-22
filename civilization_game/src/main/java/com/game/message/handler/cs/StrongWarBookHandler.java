package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.JourneyPb;
import com.game.pb.WarBookPb;
import com.game.service.JourneyService;
import com.game.service.WarBookService;

/**
 * @author CaoBing
 * @date 2020/12/17 17:25
 */
public class StrongWarBookHandler extends ClientHandler {
    @Override
    public void action () {
        WarBookService service = getService(WarBookService.class);
        WarBookPb.StrongWarBookRq req = msg.getExtension(WarBookPb.StrongWarBookRq.ext);
        service.strongWarBookRq(req, this);
    }
}
